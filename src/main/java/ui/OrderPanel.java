package ui;

import entity.*;
import network.Client;
import network.CommandType;
import network.Request;
import network.Response;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class OrderPanel extends JPanel {
    private final Client client;
    private final TaiKhoan currentUser;
    private final Ban ban;
    private final StaffDashboard parentFrame;
    private PhieuGoiMon activeOrder;
    private List<ChiTietPhieuGoi> currentCart = new ArrayList<>();

    private JPanel menuGrid;
    private DefaultTableModel cartModel;
    private JTable cartTable;
    private JLabel lblTotal;
    private JButton btnPay;
    private final DecimalFormat df = new DecimalFormat("#,##0");

    // Colors
    private final Color BG_LIGHT = new Color(250, 251, 253);
    private final Color ACCENT_BLUE = new Color(0, 150, 255);
    private final Color SUCCESS_GREEN = new Color(46, 204, 113);
    private final Color TEXT_DARK = new Color(30, 30, 40);

    public OrderPanel(Client client, TaiKhoan currentUser, Ban ban, StaffDashboard parent) {
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

        JLabel lblSearch = new JLabel("Thực đơn đồ uống");
        lblSearch.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblSearch.setBorder(new EmptyBorder(0, 0, 15, 0));
        panel.add(lblSearch, BorderLayout.NORTH);

        menuGrid = new JPanel(new GridLayout(0, 3, 15, 15));
        menuGrid.setBackground(BG_LIGHT);

        JScrollPane scroll = new JScrollPane(menuGrid);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
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

        // Bảng giỏ hàng
        String[] cols = {"Món", "SL", "Giá"};
        cartModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return c == 1; }
        };
        cartTable = new JTable(cartModel);
        styleCartTable();

        JScrollPane scroll = new JScrollPane(cartTable);
        scroll.setBorder(new EmptyBorder(0, 10, 0, 10));
        scroll.getViewport().setBackground(Color.WHITE);
        panel.add(scroll, BorderLayout.CENTER);

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

    private void styleCartTable() {
        cartTable.setRowHeight(45);
        cartTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cartTable.setSelectionBackground(new Color(245, 246, 250));
        cartTable.setShowVerticalLines(false);
        cartTable.setIntercellSpacing(new Dimension(0, 0));

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        cartTable.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);

        cartTable.getTableHeader().setBackground(Color.WHITE);
        cartTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        cartTable.getTableHeader().setForeground(new Color(150, 150, 160));
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

    private void loadMenu() {
        try {
            Response response = client.sendRequest(new Request(CommandType.GET_MENU, null));
            if (response.isSuccess()) {
                List<DoUong> menuItems = (List<DoUong>) response.getData();
                menuGrid.removeAll();
                for (DoUong item : menuItems) {
                    menuGrid.add(createDrinkCard(item));
                }
                menuGrid.revalidate();
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private JPanel createDrinkCard(DoUong drink) {
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

    private void addToCart(DoUong drink) {
        for (ChiTietPhieuGoi ct : currentCart) {
            if (ct.getDoUong().getMaDoUong().equals(drink.getMaDoUong())) {
                ct.setSoLuong(ct.getSoLuong() + 1);
                updateCartTable();
                return;
            }
        }
        ChiTietPhieuGoi newCt = new ChiTietPhieuGoi();
        newCt.setDoUong(drink);
        newCt.setSoLuong(1);
        newCt.setDonGia(Double.parseDouble(drink.getGiaTien()));
        currentCart.add(newCt);
        updateCartTable();
    }

    private void updateCartTable() {
        cartModel.setRowCount(0);
        double total = 0;
        for (ChiTietPhieuGoi ct : currentCart) {
            double lineTotal = ct.getSoLuong() * ct.getDonGia();
            cartModel.addRow(new Object[]{
                    ct.getDoUong().getTenDoUong(),
                    ct.getSoLuong(),
                    df.format(lineTotal)
            });
            total += lineTotal;
        }
        lblTotal.setText(df.format(total) + " VND");
        btnPay.setVisible(activeOrder != null);
    }

    // Giữ nguyên logic loadData, loadActiveOrder, confirmOrder và handlePayment từ code gốc của bạn
    private void loadData() { loadMenu(); loadActiveOrder(); }

    private void loadActiveOrder() {
        try {
            Response response = client.sendRequest(new Request(CommandType.GET_ORDER, ban.getMaBan()));
            if (response.isSuccess() && response.getData() != null) {
                activeOrder = (PhieuGoiMon) response.getData();
                currentCart = activeOrder.getChiTietPhieuGois();
                updateCartTable();
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void confirmOrder() {
        if (currentCart.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Giỏ hàng trống!");
            return;
        }

        double total = 0;
        for (ChiTietPhieuGoi ct : currentCart) total += ct.getSoLuong() * ct.getDonGia();

        if (activeOrder == null) {
            activeOrder = new PhieuGoiMon();
            activeOrder.setMaPhieu("PG" + System.currentTimeMillis());
            activeOrder.setBan(ban);
            activeOrder.setNhanVien(currentUser.getNhanVien());
            activeOrder.setTrangThai("Chưa thanh toán");
        }
        activeOrder.setChiTietPhieuGois(currentCart);
        activeOrder.setTongTien(total);
        activeOrder.setNgayTao(java.time.LocalDate.now().toString());

        List<ChiTietPhieuGoi> newItems = new ArrayList<>();
        for (ChiTietPhieuGoi ct : currentCart) {
            if (ct.getId() == null) newItems.add(ct);
        }

        try {
            Response res = client.sendRequest(new Request(CommandType.ORDER_FOOD, new Object[]{activeOrder, newItems}));
            if (res.isSuccess()) {
                JOptionPane.showMessageDialog(this, "Đã xác nhận order!");
                parentFrame.showTablesView();
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void handlePayment() {
        if (activeOrder != null) {
            PaymentDialog paymentDialog = new PaymentDialog(parentFrame, client, activeOrder);
            paymentDialog.setVisible(true);
            parentFrame.showTablesView();
        }
    }
}