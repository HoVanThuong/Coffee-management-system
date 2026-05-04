package ui;

import entity.*;
import entity.enums.TrangThaiHoaDon;
import network.Client;
import network.CommandType;
import network.Request;
import network.Response;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.text.DecimalFormat;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class OrderPanel extends JPanel {

    // ── Constants ────────────────────────────────────────────────
    private static final Color BG_LIGHT      = new Color(246, 248, 252);
    private static final Color C_WHITE       = Color.WHITE;
    private static final Color C_ACCENT      = new Color(99,  179, 237);
    private static final Color C_SUCCESS     = new Color(72,  187, 120);
    private static final Color C_DANGER      = new Color(245, 101,  96);
    private static final Color C_WARNING     = new Color(237, 169,  38);
    private static final Color C_BORDER      = new Color(226, 232, 240);
    private static final Color C_TEXT_DARK   = new Color(26,   32,  44);
    private static final Color C_TEXT_MUTED  = new Color(113, 128, 150);

    private static final Font F_TITLE   = new Font("Segoe UI", Font.BOLD,   20);
    private static final Font F_SECTION = new Font("Segoe UI", Font.BOLD,   15);
    private static final Font F_BODY    = new Font("Segoe UI", Font.PLAIN,  13);
    private static final Font F_BOLD    = new Font("Segoe UI", Font.BOLD,   13);

    // ── Dependencies ─────────────────────────────────────────────
    private final Client client;
    private final TaiKhoan currentUser;
    private final Ban ban;
    private final StaffDashboard parentFrame;

    // ── State ────────────────────────────────────────────────────
    private HoaDon activeOrder;
    private List<ChiTietHoaDon> currentCart = new ArrayList<>();
    private List<DoUong> allMenu = new ArrayList<>();
    private String activeCategory = "Tất cả";

    // ── UI refs ──────────────────────────────────────────────────
    private JPanel menuGrid;
    private DefaultTableModel cartModel;
    private JTable cartTable;
    private JLabel lblTotal;
    private JLabel lblOrderBadge;
    private JButton btnConfirm;
    private JTextField txtSearch;

    private final DecimalFormat df = new DecimalFormat("#,##0");

    // ════════════════════════════════════════════════════════════
    public OrderPanel(Client client, TaiKhoan currentUser, Ban ban, StaffDashboard parent) {
        this.client      = client;
        this.currentUser = currentUser;
        this.ban         = ban;
        this.parentFrame = parent;

        setLayout(new BorderLayout());
        setBackground(BG_LIGHT);

        add(createHeader(), BorderLayout.NORTH);

        JSplitPane split = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                createMenuSection(),
                createCartSection());
        split.setDividerLocation(680);
        split.setDividerSize(1);
        split.setBorder(null);
        add(split, BorderLayout.CENTER);

        loadData();
    }

    // ══════════════════════════════════════════════════════════
    // HEADER
    // ══════════════════════════════════════════════════════════
    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(C_WHITE);
        header.setBorder(new CompoundBorder(
                new MatteBorder(0, 0, 1, 0, C_BORDER),
                new EmptyBorder(0, 20, 0, 20)));
        header.setPreferredSize(new Dimension(0, 66));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        left.setOpaque(false);

        JButton btnBack = new JButton("← Quay lại");
        btnBack.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnBack.setContentAreaFilled(false);
        btnBack.setBorderPainted(false);
        btnBack.setForeground(C_ACCENT);
        btnBack.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnBack.addActionListener(e -> parentFrame.showTablesView());

        JLabel lblTitle = new JLabel("Bàn " + ban.getMaBan());
        lblTitle.setFont(F_TITLE);
        lblTitle.setForeground(C_TEXT_DARK);

        // Badge trạng thái đơn
        lblOrderBadge = new JLabel();
        lblOrderBadge.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblOrderBadge.setOpaque(true);
        lblOrderBadge.setBorder(new EmptyBorder(4, 12, 4, 12));
        lblOrderBadge.setVisible(false);

        left.add(btnBack);
        left.add(lblTitle);
        left.add(lblOrderBadge);
        header.add(left, BorderLayout.WEST);

        return header;
    }

    private void updateOrderBadge() {
        if (activeOrder != null) {
            lblOrderBadge.setText("  ● Đang phục vụ  ");
            lblOrderBadge.setForeground(C_WHITE);
            lblOrderBadge.setBackground(C_DANGER);
            lblOrderBadge.setVisible(true);
            btnConfirm.setText("THÊM MÓN");
        } else {
            lblOrderBadge.setVisible(false);
            btnConfirm.setText("XÁC NHẬN ĐƠN");
        }
    }

    // ══════════════════════════════════════════════════════════
    // MENU SECTION (bên trái)
    // ══════════════════════════════════════════════════════════
    private JPanel createMenuSection() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBackground(BG_LIGHT);
        panel.setBorder(new EmptyBorder(20, 24, 20, 16));

        // ── Toolbar: filter + search ──────────────────────────
        JPanel topBar = new JPanel(new BorderLayout(8, 0));
        topBar.setOpaque(false);

        // Placeholder cho nút category — sẽ được điền sau khi load menu
        JPanel filterRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        filterRow.setOpaque(false);
        filterRow.setName("filterRow");
        topBar.add(filterRow, BorderLayout.CENTER);

        // Thanh tìm kiếm
        txtSearch = new JTextField(15);
        txtSearch.putClientProperty("JTextField.placeholderText", "Tìm món...");
        txtSearch.setFont(F_BODY);
        txtSearch.setBorder(new CompoundBorder(
                new LineBorder(C_BORDER, 1, true),
                new EmptyBorder(6, 10, 6, 10)));
        txtSearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e)  { filterMenu(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e)  { filterMenu(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filterMenu(); }
        });
        topBar.add(txtSearch, BorderLayout.EAST);

        panel.add(topBar, BorderLayout.NORTH);

        // ── Grid món ─────────────────────────────────────────
        menuGrid = new JPanel(new GridLayout(0, 3, 12, 12));
        menuGrid.setBackground(BG_LIGHT);
        JScrollPane scroll = new JScrollPane(menuGrid);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    private void buildCategoryFilter(JPanel filterRow, List<DoUong> menu) {
        filterRow.removeAll();

        Set<String> categories = new LinkedHashSet<>();
        categories.add("Tất cả");
        menu.forEach(d -> { if (d.getLoaiDoUong() != null) categories.add(d.getLoaiDoUong()); });

        for (String cat : categories) {
            JButton btn = createFilterButton(cat);
            if (cat.equals(activeCategory)) setFilterActive(btn, true);
            btn.addActionListener(e -> {
                activeCategory = cat;
                for (Component c : filterRow.getComponents()) {
                    if (c instanceof JButton) setFilterActive((JButton) c, false);
                }
                setFilterActive(btn, true);
                filterMenu();
            });
            filterRow.add(btn);
        }
        filterRow.revalidate();
        filterRow.repaint();
    }

    private JButton createFilterButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btn.setFocusPainted(false);
        btn.setBorderPainted(true);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new CompoundBorder(
                new LineBorder(C_BORDER, 1, true),
                new EmptyBorder(5, 14, 5, 14)));
        btn.setBackground(C_WHITE);
        btn.setForeground(C_TEXT_MUTED);
        return btn;
    }

    private void setFilterActive(JButton btn, boolean active) {
        if (active) {
            btn.setBackground(C_ACCENT);
            btn.setForeground(C_WHITE);
            btn.setBorder(new CompoundBorder(
                    new LineBorder(C_ACCENT, 1, true),
                    new EmptyBorder(5, 14, 5, 14)));
        } else {
            btn.setBackground(C_WHITE);
            btn.setForeground(C_TEXT_MUTED);
            btn.setBorder(new CompoundBorder(
                    new LineBorder(C_BORDER, 1, true),
                    new EmptyBorder(5, 14, 5, 14)));
        }
    }

    private void filterMenu() {
        String keyword = txtSearch == null ? "" : txtSearch.getText().trim().toLowerCase();
        List<DoUong> filtered = allMenu.stream()
                .filter(d -> {
                    boolean catMatch = "Tất cả".equals(activeCategory)
                            || activeCategory.equals(d.getLoaiDoUong());
                    boolean searchMatch = keyword.isEmpty()
                            || d.getTenDoUong().toLowerCase().contains(keyword);
                    return catMatch && searchMatch;
                })
                .collect(Collectors.toList());

        menuGrid.removeAll();
        for (DoUong d : filtered) menuGrid.add(createDrinkCard(d));
        menuGrid.revalidate();
        menuGrid.repaint();
    }

    // ══════════════════════════════════════════════════════════
    // CART SECTION (bên phải)
    // ══════════════════════════════════════════════════════════
    private JPanel createCartSection() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(C_WHITE);
        panel.setBorder(new MatteBorder(0, 1, 0, 0, C_BORDER));

        JLabel lblCart = new JLabel("Chi tiết đơn hàng");
        lblCart.setFont(F_SECTION);
        lblCart.setForeground(C_TEXT_DARK);
        lblCart.setBorder(new EmptyBorder(20, 20, 12, 20));
        panel.add(lblCart, BorderLayout.NORTH);

        // ── Bảng giỏ hàng ─────────────────────────────────────
        String[] cols = {"Tên món", "Đ.giá", "SL", "Thành tiền", ""};
        cartModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return c == 4; }
            @Override public Class<?> getColumnClass(int c) { return c == 4 ? JPanel.class : Object.class; }
        };
        cartTable = new JTable(cartModel);
        styleCartTable();

        // Gắn Renderer/Editor cho cột hành động (cột 4)
        cartTable.getColumnModel().getColumn(4).setCellRenderer(new CartActionRenderer());
        cartTable.getColumnModel().getColumn(4).setCellEditor(new CartActionEditor());

        JScrollPane scroll = new JScrollPane(cartTable);
        scroll.setBorder(new EmptyBorder(0, 8, 0, 8));
        scroll.getViewport().setBackground(C_WHITE);
        panel.add(scroll, BorderLayout.CENTER);

        // ── Footer: Tổng + Nút ────────────────────────────────
        JPanel footer = new JPanel(new BorderLayout(0, 14));
        footer.setBackground(C_WHITE);
        footer.setBorder(new EmptyBorder(16, 20, 28, 20));

        JPanel totalRow = new JPanel(new BorderLayout());
        totalRow.setOpaque(false);
        JLabel lblTotalText = new JLabel("TỔNG CỘNG");
        lblTotalText.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTotalText.setForeground(C_TEXT_MUTED);
        lblTotal = new JLabel("0 VND");
        lblTotal.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTotal.setForeground(C_ACCENT);
        totalRow.add(lblTotalText, BorderLayout.NORTH);
        totalRow.add(lblTotal, BorderLayout.SOUTH);

        JPanel btnGroup = new JPanel(new GridLayout(1, 2, 10, 0));
        btnGroup.setOpaque(false);

        btnConfirm = styledBtn("XÁC NHẬN ĐƠN", C_SUCCESS);
        btnConfirm.addActionListener(e -> confirmOrder());

        JButton btnPay = styledBtn("THANH TOÁN", C_ACCENT);
        btnPay.addActionListener(e -> handlePayment());

        btnGroup.add(btnConfirm);
        btnGroup.add(btnPay);

        footer.add(totalRow, BorderLayout.NORTH);
        footer.add(btnGroup, BorderLayout.SOUTH);
        panel.add(footer, BorderLayout.SOUTH);

        return panel;
    }

    private void styleCartTable() {
        cartTable.setRowHeight(46);
        cartTable.setFont(F_BODY);
        cartTable.setSelectionBackground(new Color(245, 246, 250));
        cartTable.setShowVerticalLines(false);
        cartTable.setIntercellSpacing(new Dimension(0, 0));
        cartTable.getTableHeader().setBackground(C_WHITE);
        cartTable.getTableHeader().setFont(F_BOLD);
        cartTable.getTableHeader().setForeground(C_TEXT_MUTED);

        // Kích thước cột
        cartTable.getColumnModel().getColumn(0).setPreferredWidth(140);
        cartTable.getColumnModel().getColumn(1).setPreferredWidth(70);
        cartTable.getColumnModel().getColumn(2).setPreferredWidth(55);
        cartTable.getColumnModel().getColumn(3).setPreferredWidth(90);
        cartTable.getColumnModel().getColumn(4).setPreferredWidth(90);

        // Căn giữa cột SL và giá
        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);
        cartTable.getColumnModel().getColumn(1).setCellRenderer(center);
        cartTable.getColumnModel().getColumn(2).setCellRenderer(center);
        cartTable.getColumnModel().getColumn(3).setCellRenderer(center);
    }

    // ── Cart action cell renderer ─────────────────────────────
    private class CartActionRenderer implements TableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            return buildCartActionPanel(row, false);
        }
    }

    // ── Cart action cell editor ──────────────────────────────
    private class CartActionEditor extends AbstractCellEditor implements TableCellEditor {
        private JPanel panel;
        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSelected, int row, int column) {
            panel = buildCartActionPanel(row, true);
            return panel;
        }
        @Override public Object getCellEditorValue() { return null; }
    }

    private JPanel buildCartActionPanel(int row, boolean addListeners) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 3, 6));
        p.setBackground(C_WHITE);

        JButton btnMinus = smallBtn("−", C_WARNING);
        JButton btnPlus  = smallBtn("+", C_SUCCESS);
        JButton btnDel   = smallBtn("🗑", C_DANGER);

        if (addListeners) {
            // editor reference là outer scope của CartActionEditor.getTableCellEditorComponent
            // Gọi qua cách dùng actionCommand
            btnMinus.setActionCommand("MINUS_" + row);
            btnPlus.setActionCommand("PLUS_" + row);
            btnDel.setActionCommand("DEL_" + row);

            btnMinus.addActionListener(e -> { changeQty(row, -1); cartTable.getCellEditor().stopCellEditing(); });
            btnPlus.addActionListener(e  -> { changeQty(row, +1); cartTable.getCellEditor().stopCellEditing(); });
            btnDel.addActionListener(e   -> { removeFromCart(row); cartTable.getCellEditor().stopCellEditing(); });
        }

        p.add(btnMinus);
        p.add(btnPlus);
        p.add(btnDel);
        return p;
    }

    private JButton smallBtn(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setForeground(C_WHITE);
        btn.setBackground(bg);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(30, 28));
        return btn;
    }

    // ══════════════════════════════════════════════════════════
    // DRINK CARD
    // ══════════════════════════════════════════════════════════
    private JPanel createDrinkCard(DoUong drink) {
        JPanel card = new JPanel(new BorderLayout(0, 4));
        card.setBackground(C_WHITE);
        card.setBorder(new CompoundBorder(
                new LineBorder(C_BORDER, 1, true),
                new EmptyBorder(10, 8, 10, 8)));
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JLabel lblName = new JLabel(drink.getTenDoUong(), SwingConstants.CENTER);
        lblName.setFont(F_BOLD);
        lblName.setForeground(C_TEXT_DARK);

        JLabel lblIcon = new JLabel("☕", SwingConstants.CENTER);
        lblIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 28));

        JLabel lblPrice = new JLabel(df.format(Double.parseDouble(drink.getGiaTien())) + " đ", SwingConstants.CENTER);
        lblPrice.setForeground(C_ACCENT);
        lblPrice.setFont(new Font("Segoe UI", Font.BOLD, 12));

        // Badge loại
        if (drink.getLoaiDoUong() != null && !drink.getLoaiDoUong().isBlank()) {
            JLabel lblCat = new JLabel(drink.getLoaiDoUong(), SwingConstants.CENTER);
            lblCat.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            lblCat.setForeground(C_TEXT_MUTED);
            card.add(lblCat, BorderLayout.NORTH);
        }

        card.add(lblIcon,  drink.getLoaiDoUong() == null ? BorderLayout.NORTH : BorderLayout.CENTER);
        card.add(lblName,  BorderLayout.CENTER);
        card.add(lblPrice, BorderLayout.SOUTH);

        card.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) { addToCart(drink); }
            public void mouseEntered(MouseEvent e) { card.setBorder(new CompoundBorder(new LineBorder(C_ACCENT, 2, true), new EmptyBorder(10, 8, 10, 8))); }
            public void mouseExited(MouseEvent e)  { card.setBorder(new CompoundBorder(new LineBorder(C_BORDER, 1, true), new EmptyBorder(10, 8, 10, 8))); }
        });

        return card;
    }

    // ══════════════════════════════════════════════════════════
    // CART LOGIC
    // ══════════════════════════════════════════════════════════
    private void addToCart(DoUong drink) {
        for (ChiTietHoaDon ct : currentCart) {
            if (ct.getDoUong().getMaDoUong().equals(drink.getMaDoUong())) {
                ct.setSoLuong(ct.getSoLuong() + 1);
                updateCartTable();
                return;
            }
        }
        ChiTietHoaDon newCt = new ChiTietHoaDon();
        newCt.setDoUong(drink);
        newCt.setSoLuong(1);
        newCt.setDonGia(Double.parseDouble(drink.getGiaTien()));
        currentCart.add(newCt);
        updateCartTable();
    }

    private void changeQty(int row, int delta) {
        if (row < 0 || row >= currentCart.size()) return;
        ChiTietHoaDon ct = currentCart.get(row);
        int newQty = ct.getSoLuong() + delta;
        if (newQty <= 0) {
            removeFromCart(row);
        } else {
            ct.setSoLuong(newQty);
            updateCartTable();
        }
    }

    private void removeFromCart(int row) {
        if (row < 0 || row >= currentCart.size()) return;
        currentCart.remove(row);
        updateCartTable();
    }

    private void updateCartTable() {
        cartModel.setRowCount(0);
        double total = 0;
        for (ChiTietHoaDon ct : currentCart) {
            double lineTotal = ct.getSoLuong() * ct.getDonGia();
            cartModel.addRow(new Object[]{
                    ct.getDoUong().getTenDoUong(),
                    df.format(ct.getDonGia()),
                    ct.getSoLuong(),
                    df.format(lineTotal),
                    null  // rendered by CartActionRenderer
            });
            total += lineTotal;
        }
        lblTotal.setText(df.format(total) + " VND");
    }

    // ══════════════════════════════════════════════════════════
    // DATA LOADING
    // ══════════════════════════════════════════════════════════
    private void loadData() {
        new SwingWorker<Void, Void>() {
            List<DoUong> menu = new ArrayList<>();
            HoaDon order = null;

            @Override
            protected Void doInBackground() {
                try {
                    Response rMenu = client.sendRequest(new Request(CommandType.GET_MENU, null));
                    if (rMenu.isSuccess() && rMenu.getData() != null) {
                        menu = (List<DoUong>) rMenu.getData();
                    }
                    Response rOrder = client.sendRequest(new Request(CommandType.GET_ORDER, ban.getMaBan()));
                    if (rOrder.isSuccess() && rOrder.getData() != null) {
                        order = (HoaDon) rOrder.getData();
                    }
                } catch (Exception ex) { ex.printStackTrace(); }
                return null;
            }

            @Override
            protected void done() {
                allMenu = menu;

                // Tìm filterRow panel và xây dựng nút lọc
                Component topBar = ((JPanel)((JSplitPane) getComponent(1)).getLeftComponent())
                        .getComponent(0);
                if (topBar instanceof JPanel) {
                    for (Component c : ((JPanel)topBar).getComponents()) {
                        if (c instanceof JPanel && "filterRow".equals(((JPanel)c).getName())) {
                            buildCategoryFilter((JPanel) c, allMenu);
                            break;
                        }
                    }
                }

                filterMenu();

                if (order != null) {
                    activeOrder = order;
                    currentCart = activeOrder.getChiTietHoaDons() != null
                            ? new ArrayList<>(activeOrder.getChiTietHoaDons())
                            : new ArrayList<>();
                    updateCartTable();
                }
                updateOrderBadge();
            }
        }.execute();
    }

    // ══════════════════════════════════════════════════════════
    // ACTIONS
    // ══════════════════════════════════════════════════════════
    private void confirmOrder() {
        if (currentCart.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Giỏ hàng trống!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        double total = currentCart.stream().mapToDouble(ct -> ct.getSoLuong() * ct.getDonGia()).sum();

        if (activeOrder == null) {
            activeOrder = new HoaDon();
            activeOrder.setMaHoaDon("HD" + System.currentTimeMillis());
            activeOrder.setBan(ban);
            activeOrder.setNhanVien(currentUser.getNhanVien());
            activeOrder.setTrangThaiEnum(TrangThaiHoaDon.CHUA_THANH_TOAN);
        }
        activeOrder.setChiTietHoaDons(currentCart);
        activeOrder.setTongTien(total);
        activeOrder.setNgayTao(java.time.LocalDate.now());

        // Chỉ gửi các món mới (chưa có id)
        List<ChiTietHoaDon> newItems = currentCart.stream()
                .filter(ct -> ct.getId() == null)
                .collect(Collectors.toList());

        try {
            Response res = client.sendRequest(new Request(CommandType.ORDER_FOOD, new Object[]{activeOrder, newItems}));
            if (res.isSuccess()) {
                JOptionPane.showMessageDialog(this, "Đặt hàng thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                parentFrame.showTablesView();
            } else {
                JOptionPane.showMessageDialog(this, res.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    private void handlePayment() {
        if (activeOrder == null) {
            JOptionPane.showMessageDialog(this, "Bàn này chưa có đơn hàng nào!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        PaymentDialog dlg = new PaymentDialog(parentFrame, client, activeOrder);
        dlg.setVisible(true);
        parentFrame.showTablesView();
    }

    // ── Helpers ──────────────────────────────────────────────
    private JButton styledBtn(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(F_BOLD);
        btn.setForeground(C_WHITE);
        btn.setBackground(bg);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(0, 46));
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.setBackground(bg.darker()); }
            public void mouseExited(MouseEvent e)  { btn.setBackground(bg); }
        });
        return btn;
    }
}