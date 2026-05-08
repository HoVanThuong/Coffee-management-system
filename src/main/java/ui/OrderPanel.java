package ui;

import dto.*;
import network.Client;
import network.CommandType;
import network.Request;
import network.Response;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.*;
import java.awt.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class OrderPanel extends JPanel {
    private final Client client;
    private final TaiKhoanDTO currentUser;
    private final BanDTO ban;
    private final StaffDashboard parentFrame;
    private HoaDonDTO activeOrder;
    private List<ChiTietHoaDonDTO> existingCart = new ArrayList<>();
    private List<ChiTietHoaDonDTO> newCart = new ArrayList<>();
    private List<DoUongDTO> allMenuItems = new ArrayList<>();
    private JTextField txtSearch;
    private JComboBox<String> cbCategory;

    private JPanel menuGrid;
    private DefaultTableModel existingCartModel;
    private JTable existingCartTable;
    private DefaultTableModel newCartModel;
    private JTable newCartTable;
    private JLabel lblTotal;
    private JButton btnPay;
    private final DecimalFormat df = new DecimalFormat("#,##0");

    // Colors
    private final Color BG_LIGHT = new Color(250, 251, 253);
    private final Color ACCENT_BLUE = new Color(0, 150, 255);
    private final Color SUCCESS_GREEN = new Color(46, 204, 113);
    private final Color TEXT_DARK = new Color(30, 30, 40);

    public OrderPanel(Client client, TaiKhoanDTO currentUser, BanDTO ban, StaffDashboard parent) {
        this.client = client;
        this.currentUser = currentUser;
        this.ban = ban;
        this.parentFrame = parent;

        setLayout(new BorderLayout());
        setBackground(BG_LIGHT);

        // 1. TOP BAR (Tiêu đề và nút quay lại)
        add(createHeader(), BorderLayout.NORTH);

        // 2. CENTER CONTENT (Split Pane)
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, createMenuSection(), createCartSection());
        splitPane.setDividerLocation(700);
        splitPane.setDividerSize(1);
        splitPane.setBorder(null);
        add(splitPane, BorderLayout.CENTER);

        loadData();
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(230, 230, 235)));
        header.setPreferredSize(new Dimension(0, 70));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 15));
        left.setOpaque(false);

        JButton btnBack = new JButton("← Quay lại");
        btnBack.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnBack.setContentAreaFilled(false);
        btnBack.setBorderPainted(false);
        btnBack.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnBack.addActionListener(e -> parentFrame.showTablesView());

        JLabel lblTitle = new JLabel("ORDER: BÀN " + ban.getMaBan());
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitle.setForeground(TEXT_DARK);

        left.add(btnBack);
        left.add(lblTitle);
        header.add(left, BorderLayout.WEST);

        return header;
    }

    private JPanel createMenuSection() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG_LIGHT);
        panel.setBorder(new EmptyBorder(20, 30, 20, 20));

        JPanel topPanel = new JPanel(new BorderLayout(10, 0));
        topPanel.setOpaque(false);
        topPanel.setBorder(new EmptyBorder(0, 0, 15, 0));

        JLabel lblSearch = new JLabel("Thực đơn đồ uống");
        lblSearch.setFont(new Font("Segoe UI", Font.BOLD, 16));
        
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        filterPanel.setOpaque(false);
        
        txtSearch = new JTextField(15);
        txtSearch.putClientProperty("JTextField.placeholderText", "Tìm kiếm...");
        txtSearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filterMenu(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filterMenu(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filterMenu(); }
        });
        
        cbCategory = new JComboBox<>(new String[]{"Tất cả"});
        cbCategory.addActionListener(e -> filterMenu());
        
        filterPanel.add(new JLabel("Tìm:"));
        filterPanel.add(txtSearch);
        filterPanel.add(new JLabel("Loại:"));
        filterPanel.add(cbCategory);
        
        topPanel.add(lblSearch, BorderLayout.WEST);
        topPanel.add(filterPanel, BorderLayout.EAST);

        panel.add(topPanel, BorderLayout.NORTH);

        menuGrid = new JPanel(new GridLayout(0, 3, 15, 15));
        menuGrid.setBackground(BG_LIGHT);

        JScrollPane scroll = new JScrollPane(menuGrid);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    private void filterMenu() {
        String keyword = txtSearch.getText().toLowerCase().trim();
        String category = (String) cbCategory.getSelectedItem();
        if (category == null) category = "Tất cả";
        
        menuGrid.removeAll();
        for (DoUongDTO item : allMenuItems) {
            boolean matchKeyword = item.getTenDoUong().toLowerCase().contains(keyword);
            boolean matchCategory = "Tất cả".equals(category) || (item.getLoaiDoUong() != null && item.getLoaiDoUong().equals(category));
            
            if (matchKeyword && matchCategory) {
                menuGrid.add(createDrinkCard(item));
            }
        }
        menuGrid.revalidate();
        menuGrid.repaint();
    }

    private JPanel createCartSection() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, new Color(230, 230, 235)));

        // Title Giỏ hàng
        JLabel lblCart = new JLabel("Chi tiết đơn hàng");
        lblCart.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblCart.setBorder(new EmptyBorder(20, 20, 15, 20));
        panel.add(lblCart, BorderLayout.NORTH);

        // Existing Cart Table
        String[] existingCols = {"Món (Đã đặt)", "SL", "Giá"};
        existingCartModel = new DefaultTableModel(existingCols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        existingCartTable = new JTable(existingCartModel);
        
        // New Cart Table
        String[] newCols = {"Món (Mới gọi)", "SL", "Giá", "Thao Tác"};
        newCartModel = new DefaultTableModel(newCols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return c == 3; }
        };
        newCartTable = new JTable(newCartModel);
        
        styleCartTables();

        JPanel tableContainer = new JPanel(new GridLayout(2, 1, 0, 10));
        tableContainer.setOpaque(false);
        tableContainer.setBorder(new EmptyBorder(0, 10, 0, 10));
        
        JScrollPane existingScroll = new JScrollPane(existingCartTable);
        existingScroll.getViewport().setBackground(Color.WHITE);
        existingScroll.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(230, 230, 235)), "Đã Đặt"));
        
        JScrollPane newScroll = new JScrollPane(newCartTable);
        newScroll.getViewport().setBackground(Color.WHITE);
        newScroll.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(230, 230, 235)), "Gọi Thêm"));
        
        tableContainer.add(existingScroll);
        tableContainer.add(newScroll);

        panel.add(tableContainer, BorderLayout.CENTER);

        // Khu vực thanh toán (South)
        JPanel footer = new JPanel(new BorderLayout(0, 15));
        footer.setBackground(Color.WHITE);
        footer.setBorder(new EmptyBorder(20, 20, 30, 20));

        JPanel totalRow = new JPanel(new BorderLayout());
        totalRow.setOpaque(false);
        JLabel lblTotalText = new JLabel("TỔNG CỘNG");
        lblTotalText.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTotalText.setForeground(new Color(120, 120, 130));

        lblTotal = new JLabel("0 VND");
        lblTotal.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTotal.setForeground(ACCENT_BLUE);

        totalRow.add(lblTotalText, BorderLayout.NORTH);
        totalRow.add(lblTotal, BorderLayout.SOUTH);

        JPanel btnGroup = new JPanel(new GridLayout(1, 2, 10, 0));
        btnGroup.setOpaque(false);

        JButton btnConfirm = createStyledButton("XÁC NHẬN", SUCCESS_GREEN);
        btnConfirm.addActionListener(e -> confirmOrder());

        btnPay = createStyledButton("THANH TOÁN", ACCENT_BLUE);
        btnPay.addActionListener(e -> handlePayment());

        btnGroup.add(btnConfirm);
        btnGroup.add(btnPay);

        footer.add(totalRow, BorderLayout.NORTH);
        footer.add(btnGroup, BorderLayout.SOUTH);
        panel.add(footer, BorderLayout.SOUTH);

        return panel;
    }

    private void styleCartTables() {
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);

        // Style Existing Cart
        existingCartTable.setRowHeight(45);
        existingCartTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        existingCartTable.setSelectionBackground(new Color(245, 246, 250));
        existingCartTable.setShowVerticalLines(false);
        existingCartTable.setIntercellSpacing(new Dimension(0, 0));
        existingCartTable.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);
        existingCartTable.getTableHeader().setBackground(Color.WHITE);
        existingCartTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        existingCartTable.getTableHeader().setForeground(new Color(150, 150, 160));

        // Style New Cart
        newCartTable.setRowHeight(45);
        newCartTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        newCartTable.setSelectionBackground(new Color(245, 246, 250));
        newCartTable.setShowVerticalLines(false);
        newCartTable.setIntercellSpacing(new Dimension(0, 0));
        newCartTable.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);
        
        ActionPanelEditorRenderer actionRenderer = new ActionPanelEditorRenderer();
        newCartTable.getColumnModel().getColumn(3).setCellRenderer(actionRenderer);
        newCartTable.getColumnModel().getColumn(3).setCellEditor(actionRenderer);
        newCartTable.getColumnModel().getColumn(3).setPreferredWidth(120);
        newCartTable.getColumnModel().getColumn(3).setMinWidth(120);
        
        newCartTable.getTableHeader().setBackground(Color.WHITE);
        newCartTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        newCartTable.getTableHeader().setForeground(new Color(150, 150, 160));
    }

    private JButton createStyledButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setForeground(Color.WHITE);
        btn.setBackground(bg);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(0, 45));
        return btn;
    }

    private void changeQuantityByRow(int row, int delta) {
        if (row >= 0 && row < newCart.size()) {
            ChiTietHoaDonDTO ct = newCart.get(row);
            int newQty = ct.getSoLuong() + delta;
            if (newQty > 0) {
                ct.setSoLuong(newQty);
                updateCartTable();
            } else if (newQty == 0) {
                removeCartItemByRow(row);
            }
        }
    }

    private void removeCartItemByRow(int row) {
        if (row >= 0 && row < newCart.size()) {
            newCart.remove(row);
            updateCartTable();
        }
    }

    class ActionPanelEditorRenderer extends AbstractCellEditor implements TableCellRenderer, TableCellEditor {
        private JPanel renderPanel;
        private JPanel editPanel;
        private int currentRow;

        public ActionPanelEditorRenderer() {
            renderPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
            renderPanel.setOpaque(true);
            renderPanel.add(createCartButton("-", ACCENT_BLUE));
            renderPanel.add(createCartButton("+", SUCCESS_GREEN));
            renderPanel.add(createCartButton("X", new Color(231, 76, 60)));

            editPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
            editPanel.setOpaque(true);
            JButton btnMinusEdit = createCartButton("-", ACCENT_BLUE);
            JButton btnPlusEdit = createCartButton("+", SUCCESS_GREEN);
            JButton btnDeleteEdit = createCartButton("X", new Color(231, 76, 60));

            btnMinusEdit.addActionListener(e -> {
                fireEditingStopped();
                changeQuantityByRow(currentRow, -1);
            });
            btnPlusEdit.addActionListener(e -> {
                fireEditingStopped();
                changeQuantityByRow(currentRow, 1);
            });
            btnDeleteEdit.addActionListener(e -> {
                fireEditingStopped();
                removeCartItemByRow(currentRow);
            });

            editPanel.add(btnMinusEdit);
            editPanel.add(btnPlusEdit);
            editPanel.add(btnDeleteEdit);
        }

        private JButton createCartButton(String text, Color bg) {
            JButton btn = new JButton(text);
            btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
            btn.setForeground(Color.WHITE);
            btn.setBackground(bg);
            btn.setFocusPainted(false);
            btn.setBorderPainted(false);
            btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btn.setMargin(new Insets(2, 5, 2, 5));
            return btn;
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            renderPanel.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
            return renderPanel;
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            currentRow = row;
            editPanel.setBackground(table.getSelectionBackground());
            return editPanel;
        }

        @Override
        public Object getCellEditorValue() {
            return null;
        }
    }

    private void loadMenu() {
        try {
            Response response = client.sendRequest(new Request(CommandType.GET_MENU, null));
            if (response.isSuccess()) {
                @SuppressWarnings("unchecked")
                List<DoUongDTO> list = (List<DoUongDTO>) response.getData();
                allMenuItems = list;
                
                cbCategory.removeAllItems();
                cbCategory.addItem("Tất cả");
                List<String> categories = new ArrayList<>();
                for (DoUongDTO item : allMenuItems) {
                    String cat = item.getLoaiDoUong();
                    if (cat != null && !cat.trim().isEmpty() && !categories.contains(cat)) {
                        categories.add(cat);
                        cbCategory.addItem(cat);
                    }
                }
                
                filterMenu();
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private JPanel createDrinkCard(DoUongDTO drink) {
        JPanel card = new JPanel(new BorderLayout(0, 5));
        card.setBackground(Color.WHITE);
        card.setBorder(new LineBorder(new Color(230, 230, 235), 1, true));
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JLabel lblName = new JLabel(drink.getTenDoUong(), SwingConstants.CENTER);
        lblName.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblName.setBorder(new EmptyBorder(10, 5, 0, 5));

        JLabel lblPrice = new JLabel(df.format(Double.parseDouble(drink.getGiaTien())) + " đ", SwingConstants.CENTER);
        lblPrice.setForeground(ACCENT_BLUE);
        lblPrice.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblPrice.setBorder(new EmptyBorder(0, 0, 10, 0));

        // Placeholder Icon
        JLabel lblIcon = new JLabel("☕");
        lblIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 30));
        lblIcon.setHorizontalAlignment(SwingConstants.CENTER);

        card.add(lblName, BorderLayout.NORTH);
        card.add(lblIcon, BorderLayout.CENTER);
        card.add(lblPrice, BorderLayout.SOUTH);

        card.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) { addToCart(drink); }
            public void mouseEntered(java.awt.event.MouseEvent e) { card.setBorder(new LineBorder(ACCENT_BLUE, 1)); }
            public void mouseExited(java.awt.event.MouseEvent e) { card.setBorder(new LineBorder(new Color(230, 230, 235), 1)); }
        });

        return card;
    }

    private void addToCart(DoUongDTO drink) {
        for (ChiTietHoaDonDTO ct : newCart) {
            if (ct.getDoUong().getMaDoUong().equals(drink.getMaDoUong())) {
                ct.setSoLuong(ct.getSoLuong() + 1);
                updateCartTable();
                return;
            }
        }
        ChiTietHoaDonDTO newCt = new ChiTietHoaDonDTO();
        newCt.setDoUong(drink);
        newCt.setSoLuong(1);
        newCt.setDonGia(Double.parseDouble(drink.getGiaTien()));
        newCart.add(newCt);
        updateCartTable();
    }

    private void updateCartTable() {
        existingCartModel.setRowCount(0);
        newCartModel.setRowCount(0);
        double total = 0;
        
        for (ChiTietHoaDonDTO ct : existingCart) {
            double lineTotal = ct.getSoLuong() * ct.getDonGia();
            existingCartModel.addRow(new Object[]{
                    ct.getDoUong().getTenDoUong(),
                    ct.getSoLuong(),
                    df.format(lineTotal)
            });
            total += lineTotal;
        }

        for (ChiTietHoaDonDTO ct : newCart) {
            double lineTotal = ct.getSoLuong() * ct.getDonGia();
            newCartModel.addRow(new Object[]{
                    ct.getDoUong().getTenDoUong(),
                    ct.getSoLuong(),
                    df.format(lineTotal),
                    ""
            });
            total += lineTotal;
        }

        lblTotal.setText(df.format(total) + " VND");
        btnPay.setVisible(activeOrder != null);
        btnPay.setEnabled(newCart.isEmpty());
    }

    // Giữ nguyên logic loadData, loadActiveOrder, confirmOrder và handlePayment từ code gốc của bạn
    private void loadData() { loadMenu(); loadActiveOrder(); }

    private void loadActiveOrder() {
        try {
            Response response = client.sendRequest(new Request(CommandType.GET_ORDER, ban.getMaBan()));
            if (response.isSuccess() && response.getData() != null) {
                activeOrder = (HoaDonDTO) response.getData();
                
                // Gộp các món trùng lặp từ server (nếu DB đang có dữ liệu cũ bị trùng)
                existingCart.clear();
                if (activeOrder.getChiTietHoaDons() != null) {
                    for (ChiTietHoaDonDTO ct : activeOrder.getChiTietHoaDons()) {
                        boolean found = false;
                        for (ChiTietHoaDonDTO existing : existingCart) {
                            if (existing.getDoUong().getMaDoUong().equals(ct.getDoUong().getMaDoUong())) {
                                existing.setSoLuong(existing.getSoLuong() + ct.getSoLuong());
                                found = true;
                                break;
                            }
                        }
                        if (!found) {
                            existingCart.add(ct);
                        }
                    }
                }
                
                newCart.clear();
                updateCartTable();
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

     private void confirmOrder() {
        if (existingCart.isEmpty() && newCart.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Giỏ hàng đang trống, hãy chọn món trước!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (newCart.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Không có món nào mới được gọi thêm!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Xác nhận gửi Order cho bàn " + ban.getMaBan() + "?",
                "Xác nhận",
                JOptionPane.YES_NO_OPTION
        );
        if (confirm != JOptionPane.YES_OPTION) return;
        
        double total = 0;
        for (ChiTietHoaDonDTO ct : existingCart) total += ct.getSoLuong() * ct.getDonGia();
        for (ChiTietHoaDonDTO ct : newCart) total += ct.getSoLuong() * ct.getDonGia();

        if (activeOrder == null) {
            activeOrder = new HoaDonDTO();
            // Fetch standardized ID from server
            try {
                Response res = client.sendRequest(new Request(CommandType.GENERATE_ID, "HOA_DON"));
                if (res.isSuccess()) activeOrder.setMaHoaDon((String) res.getData());
                else activeOrder.setMaHoaDon("HD_TEMP_" + System.currentTimeMillis());
            } catch (Exception ex) { activeOrder.setMaHoaDon("HD_TEMP_" + System.currentTimeMillis()); }
            activeOrder.setBan(ban);
            activeOrder.setNhanVien(currentUser.getNhanVien());
            activeOrder.setTrangThai("Chưa thanh toán");
        }
        activeOrder.setTongTien(total);
        activeOrder.setNgayTao(java.time.LocalDate.now());

        List<ChiTietHoaDonDTO> itemsToSend = new ArrayList<>();
        for (ChiTietHoaDonDTO item : newCart) {
            ChiTietHoaDonDTO cleanItem = new ChiTietHoaDonDTO();
            cleanItem.setId(null);
            cleanItem.setSoLuong(item.getSoLuong());
            cleanItem.setDonGia(item.getDonGia());
            cleanItem.setDoUong(item.getDoUong());
            itemsToSend.add(cleanItem);
        }

        try {
            Object[] dataToSend = new Object[]{ activeOrder, itemsToSend };
            Request request = new Request(CommandType.ORDER_FOOD, dataToSend);

            Response res = client.sendRequest(request);

            if (res.isSuccess()) {
                JOptionPane.showMessageDialog(this, "Đã gửi Order thành công!");
                parentFrame.showTablesView();
            } else {
                JOptionPane.showMessageDialog(this, "Server từ chối: " + res.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi kết nối máy chủ!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    private void handlePayment() {
        if (activeOrder != null) {
            PaymentDialog paymentDialog = new PaymentDialog(parentFrame, client, activeOrder);
            paymentDialog.setVisible(true);
            parentFrame.showTablesView();
        }
    }
}
