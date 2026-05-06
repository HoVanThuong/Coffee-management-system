package ui;

import entity.Ban;
import entity.TaiKhoan;
import network.Client;
import network.CommandType;
import network.Request;
import network.Response;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import ui.components.SimpleBarChart;
import ui.components.WrapLayout;
import ui.components.SettingsPanel;
import java.util.stream.Collectors;

public class StaffDashboard extends JFrame {

    private final Client client;
    private final TaiKhoan currentUser;

    private CardLayout cardLayout;
    private JPanel mainContentPanel;
    private JButton activeNavButton = null;
    private JPanel gridTableContainer;   // kept for compat
    private JTabbedPane tableTabs;

    // ── Palette (Thống nhất với Manager) ──────────────────────────
    private static final Color C_SIDEBAR_BG   = new Color(15,  17,  23);
    private static final Color C_NAV_ACTIVE   = new Color(99, 179, 237);
    private static final Color C_PAGE_BG      = new Color(246, 248, 252);
    private static final Color C_CARD_BG      = Color.WHITE;
    private static final Color C_TEXT_PRIMARY = new Color(26,  32,  44);
    private static final Color C_TEXT_MUTED   = new Color(113, 128, 150);
    private static final Color C_BORDER       = new Color(226, 232, 240);
    private static final Color C_SUCCESS      = new Color(72, 187, 120);
    private static final Color C_DANGER       = new Color(245, 101,  96);
    private static final Color C_ACCENT       = new Color(99, 179, 237);
    private static final Color C_WARNING      = new Color(237, 169, 38);

    // ── Fonts ────────────────────────────────────────────────
    private static final Font F_TITLE   = new Font("Segoe UI", Font.BOLD, 26);
    private static final Font F_NAV     = new Font("Segoe UI Semibold", Font.PLAIN, 14);
    private static final Font F_LABEL   = new Font("Segoe UI", Font.PLAIN, 12);
    private static final Font F_BRAND   = new Font("Segoe UI", Font.BOLD, 16);

    public StaffDashboard(Client client, TaiKhoan currentUser) {
        this.client = client;
        this.currentUser = currentUser;

        setTitle("Coffee Staff - " + currentUser.getNhanVien().getHoTen());
        setSize(1280, 800);
        setMinimumSize(new Dimension(1100, 700));
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Xử lý đóng app
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (confirm("Bạn có chắc muốn thoát ứng dụng?")) System.exit(0);
            }
        });

        add(buildSidebar(), BorderLayout.WEST);

        cardLayout = new CardLayout();
        mainContentPanel = new JPanel(cardLayout);
        mainContentPanel.setBackground(C_PAGE_BG);

        // Màn hình Thống Kê
        mainContentPanel.add(createDashboardPanel(), "DASHBOARD");
        mainContentPanel.add(createTableLayoutPage(), "TABLE_MAP");
        mainContentPanel.add(new SettingsPanel(client, currentUser), "SETTINGS");

        add(mainContentPanel, BorderLayout.CENTER);
        loadTableData();
    }

    // ══════════════════════════════════════════════════════════
    // SIDEBAR (Re-use Manager Design)
    // ══════════════════════════════════════════════════════════
    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setPreferredSize(new Dimension(240, getHeight()));
        sidebar.setBackground(C_SIDEBAR_BG);

        // Header
        JPanel brand = new JPanel();
        brand.setLayout(new BoxLayout(brand, BoxLayout.Y_AXIS));
        brand.setBackground(C_SIDEBAR_BG);
        brand.setBorder(new EmptyBorder(28, 24, 20, 24));
        JLabel name = new JLabel("Coffee Staff");
        name.setFont(F_BRAND);
        name.setForeground(Color.WHITE);
        brand.add(name);
        sidebar.add(brand, BorderLayout.NORTH);

        // Navigation
        JPanel nav = new JPanel();
        nav.setLayout(new BoxLayout(nav, BoxLayout.Y_AXIS));
        nav.setBackground(C_SIDEBAR_BG);
        nav.setBorder(new EmptyBorder(10, 12, 10, 12));

        JButton btnDash     = buildNavButton("Thống Kê",   "DASHBOARD");
        JButton btnMap      = buildNavButton("Sơ Đồ Bàn", "TABLE_MAP");
        JButton btnSettings = buildNavButton("Cài Đặt",    "SETTINGS");
        
        nav.add(btnDash);
        nav.add(Box.createRigidArea(new Dimension(0, 2)));
        nav.add(btnMap);
        nav.add(Box.createRigidArea(new Dimension(0, 2)));
        nav.add(btnSettings);
        
        setNavActive(btnDash, true);
        activeNavButton = btnDash;

        sidebar.add(nav, BorderLayout.CENTER);

        // Bottom User Card
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setBackground(C_SIDEBAR_BG);
        bottom.setBorder(new EmptyBorder(0, 12, 20, 12));

        JButton btnLogout = mkButton("Đăng xuất", C_DANGER);
        btnLogout.addActionListener(e -> {
            if (confirm("Xác nhận đăng xuất?")) {
                this.dispose();
                new LoginFrame(client).setVisible(true);
            }
        });
        bottom.add(btnLogout, BorderLayout.SOUTH);
        sidebar.add(bottom, BorderLayout.SOUTH);

        return sidebar;
    }

    private JButton buildNavButton(String label, String card) {
        JButton btn = new JButton(label);
        btn.setFont(F_NAV);
        btn.setForeground(new Color(160, 174, 192));
        btn.setBackground(C_SIDEBAR_BG);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBorder(new EmptyBorder(10, 14, 10, 14));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> {
            if (activeNavButton != null) setNavActive(activeNavButton, false);
            setNavActive(btn, true);
            activeNavButton = btn;
            cardLayout.show(mainContentPanel, card);
        });
        return btn;
    }

    private void setNavActive(JButton btn, boolean active) {
        if (active) {
            btn.setBackground(new Color(30, 44, 70));
            btn.setForeground(C_NAV_ACTIVE);
        } else {
            btn.setBackground(C_SIDEBAR_BG);
            btn.setForeground(new Color(160, 174, 192));
        }
    }

    // ══════════════════════════════════════════════════════════
    // THỐNG KÊ (DASHBOARD)
    // ══════════════════════════════════════════════════════════
    private JPanel createDashboardPanel() {
        JPanel page = new JPanel(new BorderLayout());
        page.setBackground(C_PAGE_BG);

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(C_CARD_BG);
        header.setBorder(new CompoundBorder(new MatteBorder(0, 0, 1, 0, C_BORDER), new EmptyBorder(20, 32, 20, 32)));

        JLabel lbTitle = new JLabel("Thống Kê");
        lbTitle.setFont(F_TITLE);
        lbTitle.setForeground(C_TEXT_PRIMARY);
        header.add(lbTitle, BorderLayout.WEST);

        page.add(header, BorderLayout.NORTH);

        JPanel body = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 20));
        body.setBackground(C_PAGE_BG);
        body.setBorder(new EmptyBorder(24, 32, 24, 32));

        JPanel cardRev = createStatCard("Doanh thu hôm nay", "0 VNĐ", C_SUCCESS);
        JPanel cardOrder = createStatCard("Hóa đơn hôm nay", "0", C_ACCENT);
        JPanel cardTable = createStatCard("Bàn đang phục vụ", "0", C_WARNING);

        body.add(cardRev);
        body.add(cardOrder);
        body.add(cardTable);
        
        SimpleBarChart chart = new SimpleBarChart("Số lượng đơn hàng 7 ngày gần nhất");
        chart.setPreferredSize(new Dimension(880, 400));
        chart.setBorder(new CompoundBorder(
            new LineBorder(C_BORDER, 1, true),
            new EmptyBorder(10, 10, 10, 10)
        ));
        chart.setBarColor(C_WARNING);
        body.add(chart);
        
        loadDashboardData(cardRev, cardOrder, cardTable, chart);

        JButton btnRef = mkButton("Làm Mới", C_ACCENT);
        btnRef.addActionListener(e -> loadDashboardData(cardRev, cardOrder, cardTable, chart));
        header.add(btnRef, BorderLayout.EAST);

        page.add(body, BorderLayout.CENTER);
        return page;
    }

    private JPanel createStatCard(String title, String initialValue, Color accent) {
        JPanel card = new JPanel(new BorderLayout(0, 10));
        card.setBackground(Color.WHITE);
        card.setPreferredSize(new Dimension(280, 120));
        card.setBorder(new CompoundBorder(
            new MatteBorder(4, 0, 0, 0, accent),
            new EmptyBorder(20, 20, 20, 20)
        ));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 13));
        lblTitle.setForeground(C_TEXT_MUTED);

        JLabel lblValue = new JLabel(initialValue);
        lblValue.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblValue.setForeground(C_TEXT_PRIMARY);

        card.add(lblTitle, BorderLayout.NORTH);
        card.add(lblValue, BorderLayout.CENTER);
        
        card.putClientProperty("valueLabel", lblValue);
        return card;
    }

    private void loadDashboardData(JPanel cardRev, JPanel cardOrder, JPanel cardTable, SimpleBarChart chart) {
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                try {
                    Response resTables = client.sendRequest(new Request(CommandType.GET_TABLES, null));
                    if (resTables.isSuccess() && resTables.getData() != null) {
                        List<entity.Ban> listBan = (List<entity.Ban>) resTables.getData();
                        long activeTables = listBan.stream().filter(b -> "Có khách".equals(b.getTrangThai())).count();
                        SwingUtilities.invokeLater(() -> ((JLabel)cardTable.getClientProperty("valueLabel")).setText(String.valueOf(activeTables)));
                    }

                    Response resInvoices = client.sendRequest(new Request(CommandType.GET_INVOICES, null));
                    if (resInvoices.isSuccess() && resInvoices.getData() != null) {
                        List<entity.HoaDon> listHD = (List<entity.HoaDon>) resInvoices.getData();
                        LocalDate today = LocalDate.now();
                        long count = 0;
                        double rev = 0;
                        
                        // Prepare chart data (last 7 days order count)
                        Map<String, Double> chartData = new LinkedHashMap<>();
                        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM");
                        for (int i = 6; i >= 0; i--) {
                            chartData.put(today.minusDays(i).format(fmt), 0.0);
                        }
                        
                        for (entity.HoaDon hd : listHD) {
                            if (hd.getNgayTao() != null) {
                                if (hd.getNgayTao().equals(today)) {
                                    count++;
                                    rev += hd.getTongTien();
                                }
                                
                                String dateStr = hd.getNgayTao().format(fmt);
                                if (chartData.containsKey(dateStr)) {
                                    chartData.put(dateStr, chartData.get(dateStr) + 1.0); // count orders
                                }
                            }
                        }
                        
                        final long fCount = count;
                        final double fRev = rev;
                        SwingUtilities.invokeLater(() -> {
                            ((JLabel)cardOrder.getClientProperty("valueLabel")).setText(String.valueOf(fCount));
                            ((JLabel)cardRev.getClientProperty("valueLabel")).setText(String.format("%,.0f VNĐ", fRev));
                            chart.updateData(chartData);
                        });
                    }
                } catch (Exception ex) { ex.printStackTrace(); }
                return null;
            }
        }.execute();
    }

    // ══════════════════════════════════════════════════════════
    // SƠ ĐỒ BÀN — Phân Tab theo Khu vực + Wrap Layout
    // ══════════════════════════════════════════════════════════
    private JPanel createTableLayoutPage() {
        JPanel page = new JPanel(new BorderLayout());
        page.setBackground(C_PAGE_BG);

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(C_CARD_BG);
        header.setBorder(new CompoundBorder(new MatteBorder(0, 0, 1, 0, C_BORDER), new EmptyBorder(18, 32, 18, 32)));

        JPanel headerLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        headerLeft.setOpaque(false);
        JLabel lbTitle = new JLabel("Sơ Đồ Bàn");
        lbTitle.setFont(F_TITLE);
        lbTitle.setForeground(C_TEXT_PRIMARY);
        headerLeft.add(lbTitle);
        // Legend
        headerLeft.add(createLegendItem(" Trống ", C_SUCCESS, new Color(230, 250, 236)));
        headerLeft.add(createLegendItem(" Có Khách ", C_DANGER, new Color(254, 235, 236)));
        header.add(headerLeft, BorderLayout.WEST);

        JButton btnRefresh = mkButton("Làm Mới", C_ACCENT);
        btnRefresh.addActionListener(e -> loadTableData());
        header.add(btnRefresh, BorderLayout.EAST);
        page.add(header, BorderLayout.NORTH);

        // TabbedPane — sẽ được điền nội dung khi loadTableData() chạy
        tableTabs = new JTabbedPane(JTabbedPane.TOP);
        tableTabs.setBackground(C_PAGE_BG);
        tableTabs.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 14));
        page.add(tableTabs, BorderLayout.CENTER);

        // giữ field này trỏ vào panel hiện tại của tab đầu
        gridTableContainer = new JPanel(new WrapLayout(FlowLayout.LEFT, 16, 16));

        return page;
    }

    private JLabel createLegendItem(String text, Color textColor, Color bgColor) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(textColor);
        lbl.setBackground(bgColor);
        lbl.setOpaque(true);
        lbl.setBorder(new CompoundBorder(
            new LineBorder(textColor, 1, true),
            new EmptyBorder(4, 10, 4, 10)
        ));
        return lbl;
    }

    private void loadTableData() {
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                try {
                    Response res = client.sendRequest(new Request(CommandType.GET_TABLES, null));
                    if (res.isSuccess() && res.getData() != null) {
                        List<Ban> listBan = (List<Ban>) res.getData();
                        SwingUtilities.invokeLater(() -> updateTabbedPane(listBan));
                    }
                } catch (Exception ex) { ex.printStackTrace(); }
                return null;
            }
        }.execute();
    }

    private void updateTabbedPane(List<Ban> listBan) {
        if (tableTabs == null) return;
        int prevSelected = tableTabs.getSelectedIndex();
        tableTabs.removeAll();

        // Nhóm theo vi_tri, giữ thứ tự xuất hiện
        Map<String, List<Ban>> grouped = listBan.stream()
            .collect(Collectors.groupingBy(
                b -> (b.getViTri() != null && !b.getViTri().isBlank()) ? b.getViTri() : "Khác",
                LinkedHashMap::new,
                Collectors.toList()
            ));

        for (Map.Entry<String, List<Ban>> entry : grouped.entrySet()) {
            String zone = entry.getKey();
            List<Ban> zoneBans = entry.getValue();

            JPanel tabPanel = new JPanel(new WrapLayout(FlowLayout.LEFT, 16, 16));
            tabPanel.setBackground(C_PAGE_BG);
            tabPanel.setBorder(new EmptyBorder(20, 28, 20, 28));

            for (Ban b : zoneBans) {
                tabPanel.add(createTableCard(b));
            }

            JScrollPane sp = new JScrollPane(tabPanel);
            sp.setBorder(null);
            sp.getVerticalScrollBar().setUnitIncrement(16);
            
            // Count badge in tab title
            long occupied = zoneBans.stream().filter(b -> "Có khách".equals(b.getTrangThai())).count();
            String tabTitle = zone + "  (" + occupied + "/" + zoneBans.size() + " bàn)"; 
            tableTabs.addTab(tabTitle, sp);
            
            // colourize tab header: red if any occupied
            int idx = tableTabs.indexOfTab(tabTitle);
            if (occupied > 0) {
                tableTabs.setForegroundAt(idx, C_DANGER);
            }
        }

        // restore selected tab
        if (prevSelected >= 0 && prevSelected < tableTabs.getTabCount()) {
            tableTabs.setSelectedIndex(prevSelected);
        }

        tableTabs.revalidate();
        tableTabs.repaint();
    }

    private JPanel createTableCard(Ban ban) {
        boolean isOccupied = "Có khách".equals(ban.getTrangThai());

        JPanel card = new JPanel(new BorderLayout(0, 8));
        card.setPreferredSize(new Dimension(140, 120));
        
        // Highlight background for occupied tables
        Color bgColor = isOccupied ? new Color(254, 235, 236) : C_CARD_BG;
        Color hoverBgColor = isOccupied ? new Color(252, 220, 222) : new Color(247, 250, 252);
        
        card.setBackground(bgColor);
        card.setBorder(new CompoundBorder(
                new LineBorder(isOccupied ? C_DANGER : C_BORDER, isOccupied ? 2 : 1, true),
                new EmptyBorder(16, 12, 16, 12)
        ));
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JLabel lblIcon = new JLabel(isOccupied ? "☕" : "🪑", SwingConstants.CENTER);
        lblIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 28));

        JLabel lblName = new JLabel(ban.getMaBan(), SwingConstants.CENTER);
        lblName.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblName.setForeground(isOccupied ? new Color(197, 48, 48) : C_TEXT_PRIMARY);

        JLabel lblStatus = new JLabel(ban.getTrangThai(), SwingConstants.CENTER);
        lblStatus.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblStatus.setForeground(isOccupied ? C_DANGER : C_SUCCESS);

        card.add(lblIcon, BorderLayout.NORTH);
        card.add(lblName, BorderLayout.CENTER);
        card.add(lblStatus, BorderLayout.SOUTH);

        // Hover Effect
        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                card.setBackground(hoverBgColor);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                card.setBackground(bgColor);
            }
            @Override
            public void mouseClicked(MouseEvent e) {
                openOrder(ban);
            }
        });

        return card;
    }

    private void openOrder(Ban ban) {
        OrderPanel orderPanel = new OrderPanel(client, currentUser, ban, this);
        mainContentPanel.add(orderPanel, "ORDER_VIEW");
        cardLayout.show(mainContentPanel, "ORDER_VIEW");
    }

    public void showTablesView() {
        loadTableData();
        cardLayout.show(mainContentPanel, "TABLE_MAP");
    }

    // ══════════════════════════════════════════════════════════
    // UI HELPERS (Tương đương Manager)
    // ══════════════════════════════════════════════════════════
    private JButton mkButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(F_LABEL);
        btn.setForeground(Color.WHITE);
        btn.setBackground(bg);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(8, 18, 8, 18));
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.setBackground(bg.darker()); }
            public void mouseExited (MouseEvent e) { btn.setBackground(bg); }
        });
        return btn;
    }

    private boolean confirm(String msg) {
        return JOptionPane.showConfirmDialog(this, msg, "Xác nhận", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }
}