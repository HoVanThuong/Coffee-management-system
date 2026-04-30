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
import java.util.List;

public class StaffDashboard extends JFrame {

    private final Client client;
    private final TaiKhoan currentUser;

    private CardLayout cardLayout;
    private JPanel mainContentPanel;
    private JButton activeNavButton = null;
    private JPanel gridTableContainer;

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

        // Màn hình sơ đồ bàn
        mainContentPanel.add(createTableLayoutPage(), "TABLE_MAP");

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

        JButton btnMap = buildNavButton("Sơ Đồ Bàn", "TABLE_MAP");
        nav.add(btnMap);
        setNavActive(btnMap, true);
        activeNavButton = btnMap;

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
    // SƠ ĐỒ BÀN (Lưới 10x10)
    // ══════════════════════════════════════════════════════════
    private JPanel createTableLayoutPage() {
        JPanel page = new JPanel(new BorderLayout());
        page.setBackground(C_PAGE_BG);

        // Header (Thống nhất UI Manager)
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(C_CARD_BG);
        header.setBorder(new CompoundBorder(new MatteBorder(0, 0, 1, 0, C_BORDER), new EmptyBorder(20, 32, 20, 32)));

        JLabel lbTitle = new JLabel("Sơ Đồ Bàn");
        lbTitle.setFont(F_TITLE);
        lbTitle.setForeground(C_TEXT_PRIMARY);
        header.add(lbTitle, BorderLayout.WEST);

        JButton btnRefresh = mkButton("Làm Mới", C_ACCENT);
        btnRefresh.addActionListener(e -> loadTableData());
        header.add(btnRefresh, BorderLayout.EAST);

        page.add(header, BorderLayout.NORTH);

        // Grid 10x10
        gridTableContainer = new JPanel(new GridLayout(10, 10, 12, 12));
        gridTableContainer.setBackground(C_PAGE_BG);
        gridTableContainer.setBorder(new EmptyBorder(24, 32, 24, 32));

        JScrollPane sp = new JScrollPane(gridTableContainer);
        sp.setBorder(null);
        page.add(sp, BorderLayout.CENTER);

        return page;
    }

    private void loadTableData() {
        try {
            Response res = client.sendRequest(new Request(CommandType.GET_TABLES, null));
            if (res.isSuccess() && res.getData() != null) {
                List<Ban> listBan = (List<Ban>) res.getData();
                updateGrid(listBan);
            }
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    private void updateGrid(List<Ban> listBan) {
        gridTableContainer.removeAll();

        // Luôn tạo 100 ô (10x10)
        for (int i = 0; i < 100; i++) {
            if (i < listBan.size()) {
                gridTableContainer.add(createTableCard(listBan.get(i)));
            } else {
                // Ô trống để giữ layout
                JPanel empty = new JPanel();
                empty.setOpaque(false);
                gridTableContainer.add(empty);
            }
        }
        gridTableContainer.revalidate();
        gridTableContainer.repaint();
    }

    private JPanel createTableCard(Ban ban) {
        boolean isOccupied = "Có khách".equals(ban.getTrangThai());

        JPanel card = new JPanel(new BorderLayout(0, 5));
        card.setBackground(C_CARD_BG);
        card.setBorder(new CompoundBorder(
                new LineBorder(isOccupied ? C_DANGER : C_BORDER, 1, true),
                new EmptyBorder(12, 8, 12, 8)
        ));
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JLabel lblIcon = new JLabel(isOccupied ? "👥" : "🪑", SwingConstants.CENTER);
        lblIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 24));

        JLabel lblName = new JLabel(ban.getMaBan(), SwingConstants.CENTER);
        lblName.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblName.setForeground(C_TEXT_PRIMARY);

        JLabel lblStatus = new JLabel(ban.getTrangThai(), SwingConstants.CENTER);
        lblStatus.setFont(F_LABEL);
        lblStatus.setForeground(isOccupied ? C_DANGER : C_SUCCESS);

        card.add(lblIcon, BorderLayout.NORTH);
        card.add(lblName, BorderLayout.CENTER);
        card.add(lblStatus, BorderLayout.SOUTH);

        // Hover Effect
        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                card.setBackground(new Color(247, 250, 252));
            }
            @Override
            public void mouseExited(MouseEvent e) {
                card.setBackground(C_CARD_BG);
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