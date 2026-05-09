package ui;

import dto.*;
import network.Client;
import network.CommandType;
import network.Request;
import network.Response;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import ui.components.SimpleBarChart;
import ui.components.SettingsPanel;
import java.io.File;
import java.nio.file.Files;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.time.temporal.TemporalAdjusters;
import dto.ThongKeDTO;
import dto.ThongKeDoUongDTO;

public class ManagerDashboard extends JFrame {

    private Client client;
    private TaiKhoanDTO taiKhoan;

    private CardLayout cardLayout;
    private JPanel mainContentPanel;
    private JButton activeNavButton = null;
    private List<HoaDonDTO> currentInvoices;

    // ── Palette ──────────────────────────────────────────────
    private static final Color C_SIDEBAR_BG = new Color(15, 17, 23); // near-black
    private static final Color C_SIDEBAR_SEC = new Color(22, 26, 35); // panel header
    private static final Color C_NAV_HOVER = new Color(30, 35, 48);
    private static final Color C_NAV_ACTIVE = new Color(99, 179, 237); // sky-blue accent
    private static final Color C_ACCENT = new Color(99, 179, 237);
    private static final Color C_ACCENT_DARK = new Color(66, 153, 225);
    private static final Color C_SUCCESS = new Color(72, 187, 120);
    private static final Color C_WARNING = new Color(237, 169, 38);
    private static final Color C_DANGER = new Color(245, 101, 96);
    private static final Color C_PAGE_BG = new Color(246, 248, 252);
    private static final Color C_CARD_BG = Color.WHITE;
    private static final Color C_TEXT_PRIMARY = new Color(26, 32, 44);
    private static final Color C_TEXT_MUTED = new Color(113, 128, 150);
    private static final Color C_BORDER = new Color(226, 232, 240);
    private static final Color C_TH_BG = new Color(247, 250, 252);
    private static final Color C_TR_STRIPE = new Color(252, 253, 255);

    // ── Fonts ────────────────────────────────────────────────
    private static final Font F_TITLE = new Font("Segoe UI", Font.BOLD, 26);
    private static final Font F_SECTION = new Font("Segoe UI", Font.BOLD, 13);
    private static final Font F_NAV = new Font("Segoe UI Semibold", Font.PLAIN, 14);
    private static final Font F_BODY = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font F_LABEL = new Font("Segoe UI", Font.PLAIN, 12);
    private static final Font F_BADGE = new Font("Segoe UI", Font.BOLD, 11);
    private static final Font F_BRAND = new Font("Segoe UI", Font.BOLD, 16);

    // ── Nav items ─────────────────────────────────────────────
    private static final String[][] NAV_ITEMS = {
            { "Dash Board", "DASHBOARD" },
            { "Bàn", "TABLES" },
            { "Thực Đơn", "MENU" },
            { "Nhân Viên", "STAFF" },
            { "Hóa Đơn", "INVOICES" },
            { "Thống Kê", "THONG_KE" },
            { "Cài Đặt", "SETTINGS" },
    };

    public ManagerDashboard(Client client, TaiKhoanDTO taiKhoan) {
        this.client = client;
        this.taiKhoan = taiKhoan;

        setTitle("Coffee Manager");
        setSize(1280, 800);
        setMinimumSize(new Dimension(1024, 640));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getRootPane().putClientProperty("apple.awt.fullWindowContent", true);

        add(buildSidebar(), BorderLayout.WEST);

        cardLayout = new CardLayout();
        mainContentPanel = new JPanel(cardLayout);
        mainContentPanel.setBackground(C_PAGE_BG);
        mainContentPanel.add(createDashboardPanel(), "DASHBOARD");
        mainContentPanel.add(createTableManagementPanel(), "TABLES");
        mainContentPanel.add(createMenuManagementPanel(), "MENU");
        mainContentPanel.add(createEmployeeManagementPanel(), "STAFF");
        mainContentPanel.add(createInvoiceManagementPanel(), "INVOICES");
        mainContentPanel.add(createThongKePanel(), "THONG_KE");
        mainContentPanel.add(new SettingsPanel(client, taiKhoan), "SETTINGS");
        add(mainContentPanel, BorderLayout.CENTER);
    }

    // ══════════════════════════════════════════════════════════
    // QUẢN LÝ BÀN
    // ══════════════════════════════════════════════════════════
    private JPanel createTableManagementPanel() {
        JPanel page = buildPageShell("", "Quản Lý Bàn", "Quản lý danh sách các bàn trong quán");

        JPanel body = new JPanel(new BorderLayout(0, 16));
        body.setBackground(C_PAGE_BG);
        body.setBorder(new EmptyBorder(24, 32, 24, 32));

        // Toolbar: Thêm bàn và Tìm kiếm
        JPanel toolbar = new JPanel(new BorderLayout(12, 0));
        toolbar.setOpaque(false);
        toolbar.setBorder(new EmptyBorder(0, 0, 16, 0));

        JButton btnAdd = mkButton("Thêm Bàn Mới", C_SUCCESS);

        JPanel actionsRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actionsRight.setOpaque(false);
        JTextField txtSearch = new JTextField(20);
        txtSearch.putClientProperty("JTextField.placeholderText", "Tìm mã bàn, vị trí...");
        styleField(txtSearch);
        JButton btnRef = mkButton("Làm Mới", C_ACCENT);

        actionsRight.add(txtSearch);
        actionsRight.add(btnRef);
        toolbar.add(btnAdd, BorderLayout.WEST);
        toolbar.add(actionsRight, BorderLayout.EAST);

        // Bảng dữ liệu
        String[] cols = { "Mã Bàn", "Vị Trí (Khu vực)", "Trạng Thái", "Hành động" };
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) {
                return c == 3;
            }
        };
        JTable table = buildTable(model);

        // Xử lý bộ lọc tìm kiếm (Sorter)
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);
        txtSearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                search();
            }

            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                search();
            }

            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                search();
            }

            private void search() {
                String text = txtSearch.getText();
                sorter.setRowFilter(text.trim().isEmpty() ? null : RowFilter.regexFilter("(?i)" + text));
            }
        });

        // Thiết lập cột Hành động (Edit/Delete)
        TableActionEvent event = new TableActionEvent() {
            @Override
            public void onEdit(int row) {
                int modelRow = table.convertRowIndexToModel(row);
                BanDTO b = new BanDTO();
                b.setMaBan((String) model.getValueAt(modelRow, 0));
                b.setViTri((String) model.getValueAt(modelRow, 1));
                b.setTrangThai((String) model.getValueAt(modelRow, 2));
                showTableFormDialog(b, model);
            }

            @Override
            public void onDelete(int row) {
                if (table.isEditing())
                    table.getCellEditor().stopCellEditing();
                int modelRow = table.convertRowIndexToModel(row);
                String ma = (String) model.getValueAt(modelRow, 0);
                if (confirm("Bạn có chắc muốn xóa bàn [" + ma + "]?")) {
                    try {
                        Response res = client.sendRequest(new Request(CommandType.MANAGE_TABLE_DELETE, ma));
                        if (res.isSuccess()) {
                            info("Xóa bàn thành công!");
                            loadTableData(model);
                        } else
                            err(res.getMessage());
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        };

        table.getColumnModel().getColumn(3).setCellRenderer(new TableActionCellRenderer());
        table.getColumnModel().getColumn(3).setCellEditor(new TableActionCellEditor(event));

        btnAdd.addActionListener(e -> showTableFormDialog(null, model));
        btnRef.addActionListener(e -> loadTableData(model));

        body.add(toolbar, BorderLayout.NORTH);
        body.add(styledScroll(table), BorderLayout.CENTER);

        loadTableData(model);
        page.add(body, BorderLayout.CENTER);
        return page;
    }

    private void showTableFormDialog(BanDTO existing, DefaultTableModel model) {
        JDialog dlg = buildDialog(existing == null ? "Thêm Bàn Mới" : "Cập Nhật Bàn", 400, 300);
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(C_CARD_BG);
        form.setBorder(new EmptyBorder(20, 25, 10, 25));
        GridBagConstraints gbc = formGbc();

        JTextField txtMa = styledField();
        txtMa.setEditable(false);
        txtMa.setForeground(C_TEXT_MUTED);

        JComboBox<String> cbViTri = styledCombo(new String[] { "Lầu 1", "Lầu 2" });

        JTextField txtStatus = styledField();
        txtStatus.setEditable(false);
        txtStatus.setForeground(C_TEXT_MUTED);

        if (existing != null) {
            txtMa.setText(existing.getMaBan());
            cbViTri.setSelectedItem(existing.getViTri());
            txtStatus.setText(existing.getTrangThai());
        } else {
            txtMa.setText(generateNextTableId(model));
            txtStatus.setText("Trống");
        }

        addFormRow(form, gbc, 0, "Mã Bàn:", txtMa);
        addFormRow(form, gbc, 1, "Vị Trí/Khu vực:", cbViTri);
        addFormRow(form, gbc, 2, "Trạng Thái:", txtStatus);

        JButton btnSave = mkButton("Lưu Dữ Liệu", C_ACCENT);
        btnSave.addActionListener(e -> {
            BanDTO b = new BanDTO();
            b.setMaBan(txtMa.getText().trim());
            b.setViTri(cbViTri.getSelectedItem().toString());
            b.setTrangThai(txtStatus.getText().trim());

            CommandType cmd = (existing == null) ? CommandType.MANAGE_TABLE_ADD : CommandType.MANAGE_TABLE_UPDATE;
            try {
                Response res = client.sendRequest(new Request(cmd, b));
                if (res.isSuccess()) {
                    info(res.getMessage());
                    loadTableData(model);
                    dlg.dispose();
                } else {
                    err(res.getMessage());
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                err("Lỗi kết nối Server!");
            }
        });

        dlg.add(form, BorderLayout.CENTER);
        dlg.add(wrapBtn(btnSave), BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    private void loadTableData(DefaultTableModel model) {
        model.setRowCount(0);
        try {
            Response res = client.sendRequest(new Request(CommandType.GET_TABLES, null));
            if (res.isSuccess() && res.getData() != null) {
                @SuppressWarnings("unchecked")
                List<BanDTO> list = (List<BanDTO>) res.getData();
                for (BanDTO b : list) {
                    model.addRow(new Object[] {
                            b.getMaBan(),
                            b.getViTri(),
                            b.getTrangThai(),
                            "" // Cột hành động
                    });
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // ══════════════════════════════════════════════════════════
    // SIDEBAR
    // ══════════════════════════════════════════════════════════
    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setPreferredSize(new Dimension(240, getHeight()));
        sidebar.setBackground(C_SIDEBAR_BG);

        // Brand header
        JPanel brand = new JPanel();
        brand.setLayout(new BoxLayout(brand, BoxLayout.Y_AXIS));
        brand.setBackground(C_SIDEBAR_BG);
        brand.setBorder(new EmptyBorder(28, 24, 20, 24));

        JLabel ico = new JLabel();
        ico.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 28));
        ico.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel name = new JLabel("Coffee Manager");
        name.setFont(F_BRAND);
        name.setForeground(Color.WHITE);
        name.setAlignmentX(Component.LEFT_ALIGNMENT);

        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(40, 46, 64));
        sep.setMaximumSize(new Dimension(999, 1));
        sep.setAlignmentX(Component.LEFT_ALIGNMENT);

        brand.add(ico);
        brand.add(Box.createRigidArea(new Dimension(0, 6)));
        brand.add(name);
        brand.add(Box.createRigidArea(new Dimension(0, 18)));
        brand.add(sep);

        sidebar.add(brand, BorderLayout.NORTH);

        // Nav
        JPanel nav = new JPanel();
        nav.setLayout(new BoxLayout(nav, BoxLayout.Y_AXIS));
        nav.setBackground(C_SIDEBAR_BG);
        nav.setBorder(new EmptyBorder(10, 12, 10, 12));

        JLabel sectionLabel = new JLabel("MENU CHÍNH");
        sectionLabel.setFont(F_BADGE);
        sectionLabel.setForeground(new Color(74, 85, 104));
        sectionLabel.setBorder(new EmptyBorder(8, 8, 10, 0));
        sectionLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        nav.add(sectionLabel);

        for (String[] item : NAV_ITEMS) {
            JButton btn = buildNavButton(item[0], item[1]);
            nav.add(btn);
            nav.add(Box.createRigidArea(new Dimension(0, 2)));
            if (activeNavButton == null) {
                setNavActive(btn, true);
                activeNavButton = btn;
            }
        }

        sidebar.add(nav, BorderLayout.CENTER);

        // Bottom: user card + logout
        JPanel bottom = new JPanel();
        bottom.setLayout(new BoxLayout(bottom, BoxLayout.Y_AXIS));
        bottom.setBackground(C_SIDEBAR_BG);
        bottom.setBorder(new EmptyBorder(0, 12, 20, 12));

        JSeparator sep2 = new JSeparator();
        sep2.setForeground(new Color(40, 46, 64));
        sep2.setMaximumSize(new Dimension(999, 1));
        sep2.setAlignmentX(Component.LEFT_ALIGNMENT);
        bottom.add(sep2);
        bottom.add(Box.createRigidArea(new Dimension(0, 14)));

        // User pill
        JPanel userCard = new JPanel(new BorderLayout(10, 0));
        userCard.setBackground(new Color(22, 26, 38));
        userCard.setBorder(new CompoundBorder(
                new LineBorder(new Color(40, 46, 64), 1, true),
                new EmptyBorder(10, 12, 10, 12)));
        userCard.setMaximumSize(new Dimension(999, 56));
        userCard.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel avatar = new JLabel(initials(taiKhoan.getNhanVien().getHoTen()));
        avatar.setFont(new Font("Segoe UI", Font.BOLD, 13));
        avatar.setForeground(C_ACCENT);
        avatar.setOpaque(true);
        avatar.setBackground(new Color(30, 40, 60));
        avatar.setHorizontalAlignment(SwingConstants.CENTER);
        avatar.setBorder(new EmptyBorder(0, 0, 0, 0));
        avatar.setPreferredSize(new Dimension(34, 34));

        JPanel userInfo = new JPanel(new GridLayout(2, 1, 0, 1));
        userInfo.setOpaque(false);
        JLabel lbName = new JLabel(taiKhoan.getNhanVien().getHoTen());
        lbName.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbName.setForeground(Color.WHITE);
        JLabel lbRole = new JLabel("Quản Lý");
        lbRole.setFont(F_LABEL);
        lbRole.setForeground(new Color(113, 128, 150));
        userInfo.add(lbName);
        userInfo.add(lbRole);

        userCard.add(avatar, BorderLayout.WEST);
        userCard.add(userInfo, BorderLayout.CENTER);
        bottom.add(userCard);
        bottom.add(Box.createRigidArea(new Dimension(0, 10)));

        // Logout
        JButton btnLogout = new JButton("Đăng xuất");
        btnLogout.setFont(F_LABEL);
        btnLogout.setForeground(new Color(252, 129, 129));
        btnLogout.setBackground(new Color(255, 245, 245, 18));
        btnLogout.setBorder(new CompoundBorder(
                new LineBorder(new Color(245, 101, 96, 60), 1, true),
                new EmptyBorder(8, 14, 8, 14)));
        btnLogout.setFocusPainted(false);
        btnLogout.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnLogout.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnLogout.setMaximumSize(new Dimension(999, 38));
        btnLogout.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                btnLogout.setBackground(new Color(245, 101, 96, 40));
            }

            public void mouseExited(MouseEvent e) {
                btnLogout.setBackground(new Color(255, 245, 245, 18));
            }
        });
        btnLogout.addActionListener(e -> {
            int ok = JOptionPane.showConfirmDialog(this,
                    "Bạn có chắc chắn muốn đăng xuất?", "Xác nhận đăng xuất",
                    JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (ok == JOptionPane.YES_OPTION) {
                dispose();
                new LoginFrame(client).setVisible(true);
            }
        });
        bottom.add(btnLogout);

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
        btn.setContentAreaFilled(true);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBorder(new EmptyBorder(10, 14, 10, 14));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                if (btn != activeNavButton)
                    btn.setBackground(C_NAV_HOVER);
            }

            public void mouseExited(MouseEvent e) {
                if (btn != activeNavButton) {
                    btn.setBackground(C_SIDEBAR_BG);
                    btn.setForeground(new Color(160, 174, 192));
                }
            }
        });

        btn.addActionListener(e -> {
            if (activeNavButton != null)
                setNavActive(activeNavButton, false);
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

    private String initials(String name) {
        if (name == null || name.isEmpty())
            return "?";
        String[] parts = name.trim().split("\\s+");
        if (parts.length == 1)
            return String.valueOf(parts[0].charAt(0)).toUpperCase();
        return String.valueOf(parts[0].charAt(0)).toUpperCase()
                + String.valueOf(parts[parts.length - 1].charAt(0)).toUpperCase();
    }

    // ══════════════════════════════════════════════════════════
    // PAGE SHELL
    // ══════════════════════════════════════════════════════════
    private JPanel buildPageShell(String icon, String title, String subtitle) {
        JPanel page = new JPanel(new BorderLayout(0, 0));
        page.setBackground(C_PAGE_BG);

        // Top bar
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(C_CARD_BG);
        topBar.setBorder(new CompoundBorder(
                new MatteBorder(0, 0, 1, 0, C_BORDER),
                new EmptyBorder(20, 32, 20, 32)));

        JPanel titleGroup = new JPanel();
        titleGroup.setLayout(new BoxLayout(titleGroup, BoxLayout.Y_AXIS));
        titleGroup.setOpaque(false);

        JLabel lbIcon = new JLabel(icon + "  " + title);
        lbIcon.setFont(F_TITLE);
        lbIcon.setForeground(C_TEXT_PRIMARY);
        lbIcon.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lbSub = new JLabel(subtitle);
        lbSub.setFont(F_LABEL);
        lbSub.setForeground(C_TEXT_MUTED);
        lbSub.setAlignmentX(Component.LEFT_ALIGNMENT);

        titleGroup.add(lbIcon);
        titleGroup.add(Box.createRigidArea(new Dimension(0, 3)));
        titleGroup.add(lbSub);

        topBar.add(titleGroup, BorderLayout.WEST);
        page.add(topBar, BorderLayout.NORTH);
        return page;
    }

    // ══════════════════════════════════════════════════════════
    // THỐNG KÊ (DASHBOARD)
    // ══════════════════════════════════════════════════════════
    private JPanel createDashboardPanel() {
        JPanel page = buildPageShell("", "Dashboard", "Tổng quan hoạt động kinh doanh");

        // Main content wrapper with ScrollPane for responsiveness
        JPanel scrollContent = new JPanel();
        scrollContent.setLayout(new BoxLayout(scrollContent, BoxLayout.Y_AXIS));
        scrollContent.setBackground(C_PAGE_BG);
        scrollContent.setBorder(new EmptyBorder(24, 32, 24, 32));

        // 1. Stat Cards Row
        JPanel statsRow = new JPanel(new GridLayout(1, 3, 24, 0));
        statsRow.setOpaque(false);
        statsRow.setMaximumSize(new Dimension(1200, 140));

        JPanel cardRev = createStatCard("Doanh thu", "0 VNĐ", C_SUCCESS);
        JPanel cardOrder = createStatCard("Số lượng hóa đơn", "0", C_ACCENT);
        JPanel cardTable = createStatCard("Bàn đang phục vụ", "0", C_WARNING);

        statsRow.add(cardRev);
        statsRow.add(cardOrder);
        statsRow.add(cardTable);

        scrollContent.add(statsRow);
        scrollContent.add(Box.createRigidArea(new Dimension(0, 32)));

        // 2. Chart Section (Filter + Chart)
        JPanel chartContainer = new JPanel(new BorderLayout(0, 16));
        chartContainer.setBackground(Color.WHITE);
        chartContainer.setBorder(new CompoundBorder(
                new LineBorder(C_BORDER, 1, true),
                new EmptyBorder(20, 24, 20, 24)));
        chartContainer.setMaximumSize(new Dimension(1200, 500));

        // Chart Header (Title + Filter)
        JPanel chartHeader = new JPanel(new BorderLayout());
        chartHeader.setOpaque(false);

        JLabel lblChartTitle = new JLabel("Phân tích dữ liệu");
        lblChartTitle.setFont(F_SECTION);
        lblChartTitle.setForeground(C_TEXT_PRIMARY);

        JPanel filterGroup = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        filterGroup.setOpaque(false);
        JLabel lblF = new JLabel("Lọc theo:");
        lblF.setFont(F_LABEL);
        JComboBox<String> cbTime = styledCombo(new String[] { "Hôm nay", "7 Ngày qua", "Tháng này", "Năm này" });
        filterGroup.add(lblF);
        filterGroup.add(cbTime);

        chartHeader.add(lblChartTitle, BorderLayout.WEST);
        chartHeader.add(filterGroup, BorderLayout.EAST);

        SimpleBarChart chart = new SimpleBarChart("Biểu đồ doanh thu");
        chart.setPreferredSize(new Dimension(800, 350));
        chart.setBarColor(C_ACCENT);

        chartContainer.add(chartHeader, BorderLayout.NORTH);
        chartContainer.add(chart, BorderLayout.CENTER);

        scrollContent.add(chartContainer);

        // Logic
        loadDashboardData(cardRev, cardOrder, cardTable, chart, "Hôm nay");
        cbTime.addActionListener(
                e -> loadDashboardData(cardRev, cardOrder, cardTable, chart, (String) cbTime.getSelectedItem()));

        // Add refresh button for dashboard in topBar
        JPanel topBar = (JPanel) page.getComponent(0);
        JButton btnRef = mkButton("Làm Mới", C_ACCENT);
        btnRef.addActionListener(
                e -> loadDashboardData(cardRev, cardOrder, cardTable, chart, (String) cbTime.getSelectedItem()));
        topBar.add(btnRef, BorderLayout.EAST);

        // Tự động làm mới dữ liệu mỗi 10 giây
        Timer autoRefreshTimer = new Timer(10000, e -> {
            if (page.isShowing()) {
                loadDashboardData(cardRev, cardOrder, cardTable, chart, (String) cbTime.getSelectedItem());
            }
        });
        autoRefreshTimer.start();

        JScrollPane scroll = new JScrollPane(scrollContent);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        page.add(scroll, BorderLayout.CENTER);
        return page;
    }

    private JPanel createStatCard(String title, String initialValue, Color accent) {
        JPanel card = new JPanel(new BorderLayout(0, 10));
        card.setBackground(Color.WHITE);
        card.setPreferredSize(new Dimension(280, 120));
        card.setBorder(new CompoundBorder(
                new MatteBorder(4, 0, 0, 0, accent),
                new EmptyBorder(20, 20, 20, 20)));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(F_SECTION);
        lblTitle.setForeground(C_TEXT_MUTED);

        JLabel lblValue = new JLabel(initialValue);
        lblValue.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblValue.setForeground(C_TEXT_PRIMARY);

        card.add(lblTitle, BorderLayout.NORTH);
        card.add(lblValue, BorderLayout.CENTER);

        card.putClientProperty("valueLabel", lblValue);
        return card;
    }

    private void loadDashboardData(JPanel cardRev, JPanel cardOrder, JPanel cardTable, SimpleBarChart chart,
            String filter) {
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                try {
                    // 1. Table Stat (Always current)
                    Response resTables = client.sendRequest(new Request(CommandType.GET_TABLES, null));
                    if (resTables.isSuccess() && resTables.getData() != null) {
                        List<BanDTO> listBan = (List<BanDTO>) resTables.getData();
                        long activeTables = listBan.stream().filter(b -> "Có khách".equals(b.getTrangThai())).count();
                        SwingUtilities.invokeLater(() -> ((JLabel) cardTable.getClientProperty("valueLabel"))
                                .setText(String.valueOf(activeTables)));
                    }

                    // 2. Revenue & Orders (Use Server Statistics)
                    LocalDate today = LocalDate.now();
                    LocalDate from = today, to = today;
                    String title = "Biểu đồ doanh thu (VNĐ)";

                    if ("7 Ngày qua".equals(filter)) {
                        from = today.minusDays(6);
                        title = "Biểu đồ doanh thu 7 ngày qua (VNĐ)";
                    } else if ("Tháng này".equals(filter)) {
                        from = today.with(java.time.temporal.TemporalAdjusters.firstDayOfMonth());
                        title = "Biểu đồ doanh thu tháng " + today.getMonthValue() + " (VNĐ)";
                    } else if ("Năm này".equals(filter) || "Năm nay".equals(filter)) {
                        from = today.with(java.time.temporal.TemporalAdjusters.firstDayOfYear());
                        to = today.with(java.time.temporal.TemporalAdjusters.lastDayOfYear());
                        title = "Biểu đồ doanh thu năm " + today.getYear() + " (VNĐ)";
                    }

                    Response res = client.sendRequest(new Request(CommandType.GET_THONG_KE, new Object[] { from, to }));
                    if (res.isSuccess() && res.getData() != null) {
                        ThongKeDTO tk = (ThongKeDTO) res.getData();
                        String finalTitle = title;
                        SwingUtilities.invokeLater(() -> {
                            ((JLabel) cardRev.getClientProperty("valueLabel"))
                                    .setText(String.format("%,.0f VNĐ", tk.getTongDoanhThu()));
                            ((JLabel) cardOrder.getClientProperty("valueLabel"))
                                    .setText(String.valueOf(tk.getTongHoaDon()));
                            chart.setTitle(finalTitle);
                            chart.updateData(tk.getDoanhThuTheoNgay());
                        });
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                return null;
            }
        }.execute();
    }

    // ══════════════════════════════════════════════════════════
    // QUẢN LÝ THỰC ĐƠN
    // ══════════════════════════════════════════════════════════
    private JPanel createMenuManagementPanel() {
        JPanel page = buildPageShell("", "Thực Đơn", "Quản lý danh sách đồ uống của quán");

        JPanel body = new JPanel(new BorderLayout(0, 16));
        body.setBackground(C_PAGE_BG);
        body.setBorder(new EmptyBorder(24, 32, 24, 32));

        // Toolbar
        JPanel toolbar = new JPanel(new BorderLayout(12, 0));
        toolbar.setOpaque(false);
        toolbar.setBorder(new EmptyBorder(0, 0, 16, 0));

        // Left Actions
        JPanel actionsLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        actionsLeft.setOpaque(false);
        JButton btnAdd = mkButton("Thêm Món", C_SUCCESS);
        actionsLeft.add(btnAdd);

        // Right Actions (Search + Refresh)
        JPanel actionsRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actionsRight.setOpaque(false);

        JTextField txtSearch = new JTextField(20);
        txtSearch.putClientProperty("JTextField.placeholderText", "Tìm kiếm món...");
        styleField(txtSearch);

        JButton btnRef = mkButton("Làm Mới", C_ACCENT);

        actionsRight.add(txtSearch);
        actionsRight.add(btnRef);

        toolbar.add(actionsLeft, BorderLayout.WEST);
        toolbar.add(actionsRight, BorderLayout.EAST);

        String[] menuCols = { "Mã Đồ Uống", "Tên Đồ Uống", "Giá Tiền (VNĐ)", "Loại", "Hành động" };
        DefaultTableModel menuModel = new DefaultTableModel(menuCols, 0) {
            public boolean isCellEditable(int r, int c) {
                return c == 4;
            }
        };
        JTable menuTable = buildTable(menuModel);

        // Setup Search Sorter
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(menuModel);
        menuTable.setRowSorter(sorter);
        txtSearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                search();
            }

            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                search();
            }

            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                search();
            }

            private void search() {
                String text = txtSearch.getText();
                if (text.trim().length() == 0)
                    sorter.setRowFilter(null);
                else
                    sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
            }
        });

        // Center price column
        centerColumn(menuTable, 2);

        // Setup Action Column
        TableActionEvent event = new TableActionEvent() {
            @Override
            public void onEdit(int row) {
                int modelRow = menuTable.convertRowIndexToModel(row);
                DoUongDTO d = new DoUongDTO();
                d.setMaDoUong((String) menuModel.getValueAt(modelRow, 0));
                d.setTenDoUong((String) menuModel.getValueAt(modelRow, 1));
                d.setGiaTien(((String) menuModel.getValueAt(modelRow, 2)).replace(",", ""));
                d.setLoaiDoUong((String) menuModel.getValueAt(modelRow, 3));
                showDoUongFormDialog(d, menuModel);
            }

            @Override
            public void onDelete(int row) {
                if (menuTable.isEditing())
                    menuTable.getCellEditor().stopCellEditing();
                int modelRow = menuTable.convertRowIndexToModel(row);
                String ma = (String) menuModel.getValueAt(modelRow, 0);
                String ten = (String) menuModel.getValueAt(modelRow, 1);
                if (confirm("Chắc chắn xóa món: " + ten + "?")) {
                    try {
                        Response res = client.sendRequest(new Request(CommandType.MANAGE_MENU_DELETE, ma));
                        if (res.isSuccess()) {
                            info("Xóa thành công!");
                            loadMenuData(menuModel);
                        } else
                            err(res.getMessage());
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        };
        menuTable.getColumnModel().getColumn(4).setCellRenderer(new TableActionCellRenderer());
        menuTable.getColumnModel().getColumn(4).setCellEditor(new TableActionCellEditor(event));
        menuTable.getColumnModel().getColumn(4).setPreferredWidth(120);
        menuTable.setRowHeight(40);

        btnAdd.addActionListener(e -> showDoUongFormDialog(null, menuModel));
        btnRef.addActionListener(e -> loadMenuData(menuModel));

        body.add(toolbar, BorderLayout.NORTH);

        JScrollPane sp = styledScroll(menuTable);
        body.add(sp, BorderLayout.CENTER);

        loadMenuData(menuModel);
        page.add(body, BorderLayout.CENTER);
        return page;
    }

    private void showDoUongFormDialog(DoUongDTO existing, DefaultTableModel model) {
        JDialog dlg = buildDialog(existing == null ? "Thêm Món Mới" : "Sửa Thông Tin Món", 450, 480);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(C_CARD_BG);
        form.setBorder(new EmptyBorder(24, 28, 8, 28));
        GridBagConstraints gbc = formGbc();

        JTextField txtMa = styledField();
        txtMa.setEditable(false);
        txtMa.setForeground(C_TEXT_MUTED);
        JTextField txtTen = styledField();
        JTextField txtGia = styledField();
        JComboBox<String> cbLoai = styledCombo(new String[] { "Cà phê", "Trà", "Sinh tố", "Nước ép", "Khác" });

        if (existing != null) {
            txtMa.setText(existing.getMaDoUong());
            txtTen.setText(existing.getTenDoUong());
            txtGia.setText(existing.getGiaTien());
            cbLoai.setSelectedItem(existing.getLoaiDoUong());
        } else {
            // Fetch real ID from server
            try {
                Response res = client.sendRequest(new Request(CommandType.GENERATE_ID, "DO_UONG"));
                if (res.isSuccess())
                    txtMa.setText((String) res.getData());
                else
                    txtMa.setText("DU" + System.currentTimeMillis() % 100000);
            } catch (Exception ex) {
                txtMa.setText("DU" + System.currentTimeMillis() % 100000);
            }
        }

        // --- Ảnh đồ uống ---
        final byte[][] imgBytesContainer = new byte[1][1];
        if (existing != null && existing.getHinhAnh() != null) {
            imgBytesContainer[0] = existing.getHinhAnh();
        }

        JLabel lblImgPreview = new JLabel("Chưa có ảnh", SwingConstants.CENTER);
        lblImgPreview.setPreferredSize(new Dimension(100, 100));
        lblImgPreview.setBorder(new LineBorder(C_BORDER));
        if (imgBytesContainer[0] != null) {
            ImageIcon icon = new ImageIcon(imgBytesContainer[0]);
            Image scaled = icon.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH);
            lblImgPreview.setIcon(new ImageIcon(scaled));
            lblImgPreview.setText("");
        }

        JButton btnChooseImage = mkButton("Chọn Ảnh...", C_ACCENT_DARK);
        btnChooseImage.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileFilter(new FileNameExtensionFilter("Images (JPG, PNG)", "jpg", "jpeg", "png"));
            if (chooser.showOpenDialog(dlg) == JFileChooser.APPROVE_OPTION) {
                File f = chooser.getSelectedFile();
                try {
                    // Đọc và nén ảnh (scale xuống max 150x150) để tránh nặng network
                    BufferedImage originalImage = ImageIO.read(f);
                    int type = originalImage.getType() == 0 ? BufferedImage.TYPE_INT_ARGB : originalImage.getType();

                    int maxDim = 150;
                    int width = originalImage.getWidth();
                    int height = originalImage.getHeight();
                    if (width > maxDim || height > maxDim) {
                        float ratio = Math.min((float) maxDim / width, (float) maxDim / height);
                        width = Math.round(width * ratio);
                        height = Math.round(height * ratio);
                    }

                    BufferedImage resizedImage = new BufferedImage(width, height, type);
                    Graphics2D g = resizedImage.createGraphics();
                    g.drawImage(originalImage, 0, 0, width, height, null);
                    g.dispose();

                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    ImageIO.write(resizedImage, "png", baos);
                    byte[] newBytes = baos.toByteArray();
                    imgBytesContainer[0] = newBytes;

                    // Hiển thị preview
                    ImageIcon icon = new ImageIcon(newBytes);
                    Image scaledPreview = icon.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH);
                    lblImgPreview.setIcon(new ImageIcon(scaledPreview));
                    lblImgPreview.setText("");
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(dlg, "Lỗi đọc file ảnh!");
                }
            }
        });

        JPanel imgPanel = new JPanel(new BorderLayout(10, 0));
        imgPanel.setOpaque(false);
        imgPanel.add(lblImgPreview, BorderLayout.WEST);
        imgPanel.add(btnChooseImage, BorderLayout.CENTER);

        addFormRow(form, gbc, 0, "Mã Đồ Uống", txtMa);
        addFormRow(form, gbc, 1, "Tên Đồ Uống", txtTen);
        addFormRow(form, gbc, 2, "Giá Tiền (VNĐ)", txtGia);
        addFormRow(form, gbc, 3, "Loại", cbLoai);
        addFormRow(form, gbc, 4, "Hình Ảnh", imgPanel);

        JButton btnSave = mkButton("Lưu thông tin", C_ACCENT);
        btnSave.addActionListener(e -> {
            if (txtTen.getText().isEmpty() || txtGia.getText().isEmpty()) {
                JOptionPane.showMessageDialog(dlg, "Vui lòng nhập đầy đủ thông tin!", "Thiếu thông tin",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            try {
                Double.parseDouble(txtGia.getText());
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dlg, "Giá tiền phải là số hợp lệ!", "Dữ liệu không hợp lệ",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            DoUongDTO d = (existing == null) ? new DoUongDTO() : existing;
            d.setMaDoUong(txtMa.getText());
            d.setTenDoUong(txtTen.getText());
            d.setGiaTien(txtGia.getText());
            d.setLoaiDoUong((String) cbLoai.getSelectedItem());
            d.setHinhAnh(imgBytesContainer[0]);
            CommandType cmd = existing == null ? CommandType.MANAGE_MENU_ADD : CommandType.MANAGE_MENU_UPDATE;
            try {
                Response res = client.sendRequest(new Request(cmd, d));
                if (res.isSuccess()) {
                    JOptionPane.showMessageDialog(dlg, "Thành công!");
                    loadMenuData(model);
                    dlg.dispose();
                } else
                    JOptionPane.showMessageDialog(dlg, res.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        dlg.add(form, BorderLayout.CENTER);
        dlg.add(wrapBtn(btnSave), BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    private void loadMenuData(DefaultTableModel model) {
        model.setRowCount(0);
        try {
            Response res = client.sendRequest(new Request(CommandType.GET_MENU, null));
            if (res.isSuccess() && res.getData() != null) {
                @SuppressWarnings("unchecked")
                List<DoUongDTO> list = (List<DoUongDTO>) res.getData();
                for (DoUongDTO d : list) {
                    String price;
                    try {
                        price = String.format("%,.0f", Double.parseDouble(d.getGiaTien()));
                    } catch (NumberFormatException e) {
                        price = d.getGiaTien();
                    }
                    model.addRow(new Object[] { d.getMaDoUong(), d.getTenDoUong(), price, d.getLoaiDoUong(), "" });
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // ══════════════════════════════════════════════════════════
    // QUẢN LÝ NHÂN VIÊN
    // ══════════════════════════════════════════════════════════
    private JPanel createEmployeeManagementPanel() {
        JPanel page = buildPageShell("", "Nhân Viên", "Quản lý hồ sơ và tài khoản nhân viên");

        JPanel body = new JPanel(new BorderLayout(0, 16));
        body.setBackground(C_PAGE_BG);
        body.setBorder(new EmptyBorder(24, 32, 24, 32));

        String[] cols = { "Mã NV", "Họ Tên", "Số ĐT", "Chức Vụ", "Ngày Vào Làm", "Ngày Nghỉ", "Hành động" };
        DefaultTableModel empModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) {
                return c == 6;
            }
        };
        JTable empTable = buildTable(empModel);

        // Highlight fired rows
        empTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                Component comp = super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                String ngayNghi = (String) t.getModel().getValueAt(r, 5);
                if (ngayNghi != null && !ngayNghi.isEmpty()) {
                    comp.setForeground(sel ? Color.WHITE : new Color(160, 174, 192));
                } else {
                    comp.setForeground(sel ? Color.WHITE : C_TEXT_PRIMARY);
                }
                comp.setBackground(sel ? C_ACCENT_DARK : (r % 2 == 0 ? Color.WHITE : C_TR_STRIPE));
                ((JLabel) comp).setBorder(new EmptyBorder(0, 12, 0, 12));
                return comp;
            }
        });

        JPanel toolbar = new JPanel(new BorderLayout(12, 0));
        toolbar.setOpaque(false);
        toolbar.setBorder(new EmptyBorder(0, 0, 16, 0));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        left.setOpaque(false);

        JButton btnAdd = mkButton("Thêm NV", C_SUCCESS);
        left.add(btnAdd);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);

        JTextField txtSearch = new JTextField(20);
        txtSearch.putClientProperty("JTextField.placeholderText", "Tìm kiếm nhân viên...");
        styleField(txtSearch);

        JToggleButton toggleFired = new JToggleButton("Hiện NV đã nghỉ");
        toggleFired.setFont(F_LABEL);
        toggleFired.setForeground(C_TEXT_MUTED);
        toggleFired.setBackground(C_CARD_BG);
        toggleFired.setBorder(new CompoundBorder(
                new LineBorder(C_BORDER, 1, true),
                new EmptyBorder(6, 14, 6, 14)));
        toggleFired.setFocusPainted(false);
        toggleFired.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JButton btnRef = mkButton("Làm Mới", C_ACCENT);

        right.add(toggleFired);
        right.add(txtSearch);
        right.add(btnRef);

        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(empModel);
        empTable.setRowSorter(sorter);
        txtSearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                search();
            }

            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                search();
            }

            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                search();
            }

            private void search() {
                String text = txtSearch.getText();
                if (text.trim().length() == 0)
                    sorter.setRowFilter(null);
                else
                    sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
            }
        });

        loadEmployeeData(empModel, false);
        toggleFired.addActionListener(e -> loadEmployeeData(empModel, toggleFired.isSelected()));
        btnRef.addActionListener(e -> loadEmployeeData(empModel, toggleFired.isSelected()));
        btnAdd.addActionListener(e -> showEmployeeFormDialog(null, empModel, toggleFired.isSelected()));

        TableActionEvent event = new TableActionEvent() {
            @Override
            public void onEdit(int row) {
                int modelRow = empTable.convertRowIndexToModel(row);
                String maNV = (String) empModel.getValueAt(modelRow, 0);
                NhanVienDTO nv = new NhanVienDTO();
                nv.setMaNhanVien(maNV);
                nv.setHoTen((String) empModel.getValueAt(modelRow, 1));
                nv.setSdt((String) empModel.getValueAt(modelRow, 2));
                nv.setChucVu((String) empModel.getValueAt(modelRow, 3));
                TaiKhoanDTO tk = new TaiKhoanDTO();
                tk.setTenDangNhap(maNV + "_user");
                tk.setTaiKhoanQuanLi("Manager".equals(nv.getChucVu()));
                showEmployeeFormDialog(new Object[] { nv, tk }, empModel, toggleFired.isSelected());
            }

            @Override
            public void onDelete(int row) {
                if (empTable.isEditing())
                    empTable.getCellEditor().stopCellEditing();
                int modelRow = empTable.convertRowIndexToModel(row);
                if (empModel.getValueAt(modelRow, 5) != null
                        && !empModel.getValueAt(modelRow, 5).toString().isEmpty()) {
                    info("Nhân viên này đã nghỉ việc!");
                    return;
                }
                String maNV = (String) empModel.getValueAt(modelRow, 0);
                if (confirm("Chắc chắn cho nhân viên " + maNV + " thôi việc?")) {
                    try {
                        Response res = client.sendRequest(new Request(CommandType.MANAGE_EMPLOYEE_DELETE, maNV));
                        if (res.isSuccess()) {
                            info("Đã cập nhật trạng thái nghỉ việc!");
                            loadEmployeeData(empModel, toggleFired.isSelected());
                        } else
                            err(res.getMessage());
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        };

        empTable.getColumnModel().getColumn(6).setCellRenderer(new TableActionCellRenderer());
        empTable.getColumnModel().getColumn(6).setCellEditor(new TableActionCellEditor(event));
        empTable.getColumnModel().getColumn(6).setPreferredWidth(120);

        toolbar.add(left, BorderLayout.WEST);
        toolbar.add(right, BorderLayout.EAST);
        body.add(toolbar, BorderLayout.NORTH);
        body.add(styledScroll(empTable), BorderLayout.CENTER);

        page.add(body, BorderLayout.CENTER);
        return page;
    }

    private void loadEmployeeData(DefaultTableModel model, boolean includeFired) {
        model.setRowCount(0);
        try {
            Response res = client.sendRequest(new Request(CommandType.GET_EMPLOYEES, includeFired));
            if (res.isSuccess() && res.getData() != null) {
                @SuppressWarnings("unchecked")
                List<NhanVienDTO> list = (List<NhanVienDTO>) res.getData();
                for (NhanVienDTO n : list) {
                    model.addRow(new Object[] {
                            n.getMaNhanVien(), n.getHoTen(), n.getSdt(), n.getChucVu(),
                            n.getNgayVaoLam() != null ? n.getNgayVaoLam().toString() : "",
                            n.getNgayThoiViec() != null ? n.getNgayThoiViec().toString() : "",
                            ""
                    });
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void showEmployeeFormDialog(Object[] existingData, DefaultTableModel model, boolean chkState) {
        JDialog dlg = buildDialog(existingData == null ? "Thêm Nhân Viên" : "Sửa Nhân Viên", 440, 420);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(C_CARD_BG);
        form.setBorder(new EmptyBorder(24, 28, 8, 28));
        GridBagConstraints gbc = formGbc();

        JTextField txtMaNV = styledField();
        txtMaNV.setEditable(false);
        txtMaNV.setForeground(C_TEXT_MUTED);
        JTextField txtHoTen = styledField();
        JTextField txtSdt = styledField();
        JComboBox<String> cbChucVu = styledCombo(new String[] { "Nhân viên", "Quản lý" });

        JCheckBox chkMgr = new JCheckBox("Tài khoản Quản lý");
        chkMgr.setFont(F_BODY);
        chkMgr.setOpaque(false);
        chkMgr.setForeground(C_TEXT_PRIMARY);

        if (existingData != null) {
            NhanVienDTO nv = (NhanVienDTO) existingData[0];
            TaiKhoanDTO tk = (TaiKhoanDTO) existingData[1];
            txtMaNV.setText(nv.getMaNhanVien());
            txtHoTen.setText(nv.getHoTen());
            txtSdt.setText(nv.getSdt());
            cbChucVu.setSelectedItem(nv.getChucVu());
            chkMgr.setSelected(tk.isTaiKhoanQuanLi());
        } else {
            try {
                Response resNV = client.sendRequest(new Request(CommandType.GENERATE_ID, "NHAN_VIEN"));
                if (resNV.isSuccess())
                    txtMaNV.setText((String) resNV.getData());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        addFormRow(form, gbc, 0, "Mã Nhân Viên", txtMaNV);
        addFormRow(form, gbc, 1, "Họ Tên", txtHoTen);
        addFormRow(form, gbc, 2, "Số Điện Thoại", txtSdt);
        addFormRow(form, gbc, 3, "Chức Vụ", cbChucVu);
        addFormRow(form, gbc, 4, "Quyền", chkMgr);

        // Note label
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(20, 0, 10, 0);
        JLabel lblNote = new JLabel("<html><i>* Ghi chú: Tài khoản mặc định là SĐT, mật khẩu: 123</i></html>");
        lblNote.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblNote.setForeground(C_ACCENT);
        form.add(lblNote, gbc);

        JButton btnSave = mkButton("Lưu thông tin", C_ACCENT);
        btnSave.addActionListener(e -> {
            if (txtHoTen.getText().trim().isEmpty() || txtSdt.getText().trim().isEmpty()) {
                warn("Vui lòng điền đầy đủ Họ tên và SĐT!");
                return;
            }

            NhanVienDTO nv = new NhanVienDTO();
            nv.setMaNhanVien(txtMaNV.getText());
            nv.setHoTen(txtHoTen.getText().trim());
            nv.setSdt(txtSdt.getText().trim());
            nv.setChucVu((String) cbChucVu.getSelectedItem());
            if (existingData == null)
                nv.setNgayVaoLam(LocalDate.now());

            TaiKhoanDTO tk = new TaiKhoanDTO();
            if (existingData != null) {
                tk.setMaTaiKhoan(((TaiKhoanDTO) existingData[1]).getMaTaiKhoan());
            } else {
                try {
                    Response resTK = client.sendRequest(new Request(CommandType.GENERATE_ID, "TAI_KHOAN"));
                    if (resTK.isSuccess())
                        tk.setMaTaiKhoan((String) resTK.getData());
                    else
                        tk.setMaTaiKhoan("TK" + System.currentTimeMillis() % 100000);
                } catch (Exception ex) {
                    tk.setMaTaiKhoan("TK" + System.currentTimeMillis() % 100000);
                }
            }
            tk.setTenDangNhap(nv.getSdt()); // Username là SDT
            tk.setMatKhau("123"); // Mật khẩu mặc định
            tk.setTaiKhoanQuanLi(chkMgr.isSelected());

            CommandType cmd = (existingData == null) ? CommandType.MANAGE_EMPLOYEE_ADD
                    : CommandType.MANAGE_EMPLOYEE_UPDATE;
            try {
                Response res = client.sendRequest(new Request(cmd, new Object[] { nv, tk }));
                if (res.isSuccess()) {
                    info("Lưu nhân viên thành công!");
                    loadEmployeeData(model, chkState);
                    dlg.dispose();
                } else {
                    err(res.getMessage());
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        dlg.add(form, BorderLayout.CENTER);
        dlg.add(wrapBtn(btnSave), BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    // ══════════════════════════════════════════════════════════
    // LỊCH SỬ HÓA ĐƠN
    // ══════════════════════════════════════════════════════════
    private JPanel createInvoiceManagementPanel() {
        JPanel page = buildPageShell("", "Lịch Sử Hóa Đơn", "Xem toàn bộ giao dịch đã thanh toán");

        JPanel body = new JPanel(new BorderLayout(0, 16));
        body.setBackground(C_PAGE_BG);
        body.setBorder(new EmptyBorder(24, 32, 24, 32));

        String[] cols = { "Mã Hóa Đơn", "Ngày Thanh Toán", "Mã Bàn", "Nhân Viên", "Tổng Tiền (VNĐ)", "Trạng Thái",
                "Ghi Chú" };
        DefaultTableModel invModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        JTable invTable = buildTable(invModel);
        centerColumn(invTable, 4);
        centerColumn(invTable, 5);

        // Highlight "Trạng thái" with color
        invTable.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                l.setHorizontalAlignment(SwingConstants.CENTER);
                String val = (v != null) ? v.toString() : "";
                if (sel) {
                    l.setForeground(Color.WHITE);
                } else {
                    if ("Đã thanh toán".equals(val))
                        l.setForeground(C_SUCCESS);
                    else if ("Chưa thanh toán".equals(val))
                        l.setForeground(C_DANGER);
                    else
                        l.setForeground(C_TEXT_PRIMARY);
                }
                return l;
            }
        });

        invTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = invTable.getSelectedRow();
                    if (row != -1) {
                        int modelRow = invTable.convertRowIndexToModel(row);
                        if (currentInvoices != null && modelRow < currentInvoices.size()) {
                            HoaDonDTO h = currentInvoices.get(modelRow);
                            new ReceiptDialog(ManagerDashboard.this, client, h, h.getTongTien()).setVisible(true);
                        }
                    }
                }
            }
        });

        JPanel toolbar = new JPanel(new BorderLayout(12, 0));
        toolbar.setOpaque(false);
        toolbar.setBorder(new EmptyBorder(0, 0, 16, 0));

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);

        JTextField txtSearch = new JTextField(20);
        txtSearch.putClientProperty("JTextField.placeholderText", "Tìm kiếm hóa đơn...");
        styleField(txtSearch);

        JButton btnRef = mkButton("Cập Nhật Dữ Liệu", C_ACCENT);

        right.add(txtSearch);
        right.add(btnRef);
        toolbar.add(right, BorderLayout.EAST);

        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(invModel);
        invTable.setRowSorter(sorter);
        txtSearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                search();
            }

            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                search();
            }

            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                search();
            }

            private void search() {
                String text = txtSearch.getText();
                if (text.trim().length() == 0)
                    sorter.setRowFilter(null);
                else
                    sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
            }
        });

        btnRef.addActionListener(e -> loadInvoiceData(invModel));

        loadInvoiceData(invModel);

        body.add(toolbar, BorderLayout.NORTH);
        body.add(styledScroll(invTable), BorderLayout.CENTER);
        page.add(body, BorderLayout.CENTER);
        return page;
    }

    private void loadInvoiceData(DefaultTableModel model) {
        model.setRowCount(0);
        try {
            Response res = client.sendRequest(new Request(CommandType.GET_INVOICES, null));
            if (res.isSuccess() && res.getData() != null) {
                @SuppressWarnings("unchecked")
                List<HoaDonDTO> list = (List<HoaDonDTO>) res.getData();
                currentInvoices = list;
                DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                for (HoaDonDTO h : currentInvoices) {
                    String ngay = h.getNgayTao() != null ? h.getNgayTao().format(fmt) : "";
                    String maBan = (h.getBan() != null) ? h.getBan().getMaBan() : "N/A";
                    String nv = (h.getNhanVien() != null) ? h.getNhanVien().getHoTen() : "N/A";
                    String tt = h.getTrangThai() != null ? h.getTrangThai() : "Đã thanh toán";
                    model.addRow(new Object[] { h.getMaHoaDon(), ngay, maBan, nv,
                            String.format("%,.0f", h.getTongTien()), tt, h.getGhiChu() });
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // ══════════════════════════════════════════════════════════
    // THỐNG KÊ DOANH THU
    // ══════════════════════════════════════════════════════════
    private JPanel createThongKePanel() {
        JPanel page = buildPageShell("", "Thống Kê Doanh Thu", "Phân tích doanh thu chi tiết theo thời gian");

        JPanel body = new JPanel(new BorderLayout(0, 20));
        body.setBackground(C_PAGE_BG);
        body.setBorder(new EmptyBorder(24, 32, 24, 32));

        // ── Filter Bar: ComboBox giống Dashboard ─────────────
        JPanel filterBar = new JPanel(new BorderLayout(16, 0));
        filterBar.setBackground(C_CARD_BG);
        filterBar.setBorder(new CompoundBorder(
                new LineBorder(C_BORDER, 1, true),
                new EmptyBorder(14, 20, 14, 20)));

        JLabel lblFilter = new JLabel("Lọc theo:");
        lblFilter.setFont(F_LABEL);
        lblFilter.setForeground(C_TEXT_MUTED);

        JComboBox<String> cbFilter = styledCombo(
                new String[] { "Hôm nay", "Tuần", "Tháng", "Năm" });
        cbFilter.setPreferredSize(new Dimension(160, 32));

        JPanel filterLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        filterLeft.setOpaque(false);
        filterLeft.add(lblFilter);
        filterLeft.add(cbFilter);

        filterBar.add(filterLeft, BorderLayout.WEST);

        // ── 4 Thẻ tổng quan ──────────────────────────────────
        JPanel cardRow = new JPanel(new GridLayout(1, 4, 16, 0));
        cardRow.setOpaque(false);

        JPanel cardRev = createStatCard("Tổng Doanh Thu", "0 VNĐ", C_SUCCESS);
        JPanel cardOrders = createStatCard("Số Hóa Đơn", "0", C_ACCENT);
        JPanel cardAvg = createStatCard("Doanh Thu TB/Đơn", "0 VNĐ", C_WARNING);
        JPanel cardTables = createStatCard("Số Bàn Đã Phục Vụ", "0", C_DANGER);

        cardRow.add(cardRev);
        cardRow.add(cardOrders);
        cardRow.add(cardAvg);
        cardRow.add(cardTables);

        // ── Phần dưới: biểu đồ + bảng top món ──────────────
        JSplitPane splitBottom = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitBottom.setDividerLocation(620);
        splitBottom.setDividerSize(8);
        splitBottom.setBorder(null);

        // Biểu đồ
        JPanel chartPanel = new JPanel(new BorderLayout(0, 10));
        chartPanel.setBackground(C_CARD_BG);
        chartPanel.setBorder(new CompoundBorder(
                new LineBorder(C_BORDER, 1, true),
                new EmptyBorder(16, 20, 16, 20)));

        JLabel lblChartTitle = new JLabel("Biểu đồ doanh thu (VNĐ)");
        lblChartTitle.setFont(F_SECTION);
        lblChartTitle.setForeground(C_TEXT_PRIMARY);

        SimpleBarChart barChart = new SimpleBarChart("Doanh thu");
        barChart.setBarColor(C_ACCENT);
        barChart.setPreferredSize(new Dimension(500, 300));

        chartPanel.add(lblChartTitle, BorderLayout.NORTH);
        chartPanel.add(barChart, BorderLayout.CENTER);

        // Bảng Top Món
        JPanel topMonPanel = new JPanel(new BorderLayout(0, 10));
        topMonPanel.setBackground(C_CARD_BG);
        topMonPanel.setBorder(new CompoundBorder(
                new LineBorder(C_BORDER, 1, true),
                new EmptyBorder(16, 20, 16, 20)));

        JLabel lblTopTitle = new JLabel("Top 10 Món Bán Chạy");
        lblTopTitle.setFont(F_SECTION);
        lblTopTitle.setForeground(C_TEXT_PRIMARY);

        String[] topCols = { "Tên Đồ Uống", "Loại", "SL Bán", "Doanh Thu (VNĐ)" };
        DefaultTableModel topModel = new DefaultTableModel(topCols, 0) {
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        JTable topTable = buildTable(topModel);
        centerColumn(topTable, 2);
        centerColumn(topTable, 3);
        topTable.getColumnModel().getColumn(0).setPreferredWidth(160);
        topTable.getColumnModel().getColumn(1).setPreferredWidth(80);

        topMonPanel.add(lblTopTitle, BorderLayout.NORTH);
        topMonPanel.add(styledScroll(topTable), BorderLayout.CENTER);

        splitBottom.setLeftComponent(chartPanel);
        splitBottom.setRightComponent(topMonPanel);

        // ── Lắp vào body ─────────────────────────────────────
        JPanel topSection = new JPanel(new BorderLayout(0, 16));
        topSection.setOpaque(false);
        topSection.add(filterBar, BorderLayout.NORTH);
        topSection.add(cardRow, BorderLayout.SOUTH);

        body.add(topSection, BorderLayout.NORTH);
        body.add(splitBottom, BorderLayout.CENTER);
        page.add(body, BorderLayout.CENTER);

        // ── Action: load dữ liệu khi đổi combobox ───────────
        Runnable loadData = () -> {
            String selected = (String) cbFilter.getSelectedItem();
            LocalDate today = LocalDate.now();
            LocalDate from, to;
            switch (selected) {
                case "7 Ngày qua":
                    from = today.minusDays(6);
                    to = today;
                    break;
                case "Tháng này":
                    from = today.with(TemporalAdjusters.firstDayOfMonth());
                    to = today;
                    break;
                case "Năm nay":
                    from = today.with(TemporalAdjusters.firstDayOfYear());
                    to = today.with(TemporalAdjusters.lastDayOfYear());
                    break;
                default: // "Hôm nay"
                    from = today;
                    to = today;
                    break;
            }
            // Cập nhật tiêu đề biểu đồ
            String title;
            if ("Năm nay".equals(selected)) {
                title = "Biểu đồ doanh thu năm " + today.getYear() + " (VNĐ)";
            } else if ("Tháng này".equals(selected)) {
                title = "Biểu đồ doanh thu tháng " + today.getMonthValue() + " (VNĐ)";
            } else {
                title = "Biểu đồ doanh thu - " + selected + " (VNĐ)";
            }
            lblChartTitle.setText(title);
            loadThongKe(from, to, cardRev, cardOrders, cardAvg, cardTables, barChart, topModel);
        };

        cbFilter.addActionListener(e -> loadData.run());

        // Load lần đầu (mặc định: Tháng này)
        cbFilter.setSelectedItem("Tháng");
        loadData.run();

        return page;
    }

    private void loadThongKe(LocalDate from, LocalDate to,
            JPanel cardRev, JPanel cardOrders, JPanel cardAvg, JPanel cardTables,
            SimpleBarChart barChart, DefaultTableModel topModel) {
        new SwingWorker<ThongKeDTO, Void>() {
            @Override
            protected ThongKeDTO doInBackground() throws Exception {
                Response res = client.sendRequest(new Request(CommandType.GET_THONG_KE, new Object[] { from, to }));
                if (res.isSuccess() && res.getData() != null) {
                    return (ThongKeDTO) res.getData();
                }
                return null;
            }

            @Override
            protected void done() {
                try {
                    ThongKeDTO tk = get();
                    if (tk == null)
                        return;

                    // Cập nhật 4 thẻ tổng quan
                    ((JLabel) cardRev.getClientProperty("valueLabel"))
                            .setText(String.format("%,.0f VNĐ", tk.getTongDoanhThu()));
                    ((JLabel) cardOrders.getClientProperty("valueLabel"))
                            .setText(String.valueOf(tk.getTongHoaDon()));
                    ((JLabel) cardAvg.getClientProperty("valueLabel"))
                            .setText(String.format("%,.0f VNĐ", tk.getDoanhThuTrungBinhMoiDon()));
                    ((JLabel) cardTables.getClientProperty("valueLabel"))
                            .setText(String.valueOf(tk.getTongBanDaPhucVu()));

                    // Cập nhật biểu đồ
                    if (tk.getDoanhThuTheoNgay() != null) {
                        barChart.updateData(tk.getDoanhThuTheoNgay());
                    }

                    // Cập nhật bảng Top Món
                    topModel.setRowCount(0);
                    if (tk.getTopMonBanChay() != null) {
                        java.text.DecimalFormat df = new java.text.DecimalFormat("#,##0");
                        for (ThongKeDoUongDTO m : tk.getTopMonBanChay()) {
                            topModel.addRow(new Object[] {
                                    m.getTenDoUong(),
                                    m.getLoaiDoUong(),
                                    m.getSoLuongDaBan(),
                                    df.format(m.getDoanhThu())
                            });
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }.execute();
    }

    // ══════════════════════════════════════════════════════════
    // UI HELPERS
    // ══════════════════════════════════════════════════════════

    private JTable buildTable(DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setFont(F_BODY);
        table.setRowHeight(40);
        table.setShowVerticalLines(false);
        table.setGridColor(C_BORDER);
        table.setBackground(Color.WHITE);
        table.setSelectionBackground(new Color(235, 248, 255));
        table.setSelectionForeground(C_TEXT_PRIMARY);
        table.setIntercellSpacing(new Dimension(0, 0));

        JTableHeader header = table.getTableHeader();
        header.setFont(F_SECTION);
        header.setBackground(C_TH_BG);
        header.setForeground(C_TEXT_MUTED);
        header.setBorder(new MatteBorder(0, 0, 1, 0, C_BORDER));
        header.setPreferredSize(new Dimension(100, 44));
        header.setReorderingAllowed(false);

        // Stripe + padding renderer
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                setBorder(new EmptyBorder(0, 14, 0, 14));
                if (sel) {
                    setBackground(new Color(235, 248, 255));
                    setForeground(C_TEXT_PRIMARY);
                } else {
                    setBackground(r % 2 == 0 ? Color.WHITE : C_TR_STRIPE);
                    setForeground(C_TEXT_PRIMARY);
                }
                return this;
            }
        });
        return table;
    }

    private JScrollPane styledScroll(JTable table) {
        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(new CompoundBorder(
                new LineBorder(C_BORDER, 1, true),
                BorderFactory.createEmptyBorder()));
        sp.getViewport().setBackground(Color.WHITE);
        sp.setBackground(Color.WHITE);
        return sp;
    }

    private void centerColumn(JTable table, int col) {
        DefaultTableCellRenderer r = new DefaultTableCellRenderer();
        r.setHorizontalAlignment(JLabel.CENTER);
        table.getColumnModel().getColumn(col).setCellRenderer(r);
    }

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
            Color orig = bg;

            public void mouseEntered(MouseEvent e) {
                btn.setBackground(orig.darker());
            }

            public void mouseExited(MouseEvent e) {
                btn.setBackground(orig);
            }
        });
        return btn;
    }

    private JDialog buildDialog(String title, int w, int h) {
        JDialog dlg = new JDialog(this, title, true);
        dlg.setSize(w, h);
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(new BorderLayout());
        dlg.getRootPane().setBorder(BorderFactory.createEmptyBorder());
        ((JPanel) dlg.getContentPane()).setBackground(C_CARD_BG);
        return dlg;
    }

    private JTextField styledField() {
        JTextField f = new JTextField();
        styleField(f);
        return f;
    }

    private void styleField(JTextField f) {
        f.setFont(F_BODY);
        f.setForeground(C_TEXT_PRIMARY);
        f.setBorder(new CompoundBorder(
                new LineBorder(C_BORDER, 1, true),
                new EmptyBorder(6, 10, 6, 10)));
        f.setBackground(Color.WHITE);
    }

    private JComboBox<String> styledCombo(String[] items) {
        JComboBox<String> cb = new JComboBox<>(items);
        cb.setFont(F_BODY);
        cb.setBackground(Color.WHITE);
        cb.setBorder(new LineBorder(C_BORDER, 1, true));
        return cb;
    }

    private GridBagConstraints formGbc() {
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6, 0, 6, 0);
        g.anchor = GridBagConstraints.WEST;
        g.fill = GridBagConstraints.HORIZONTAL;
        return g;
    }

    private void addFormRow(JPanel form, GridBagConstraints gbc, int row, String label, Component field) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        JLabel lbl = new JLabel(label);
        lbl.setFont(F_LABEL);
        lbl.setForeground(C_TEXT_MUTED);
        lbl.setBorder(new EmptyBorder(0, 0, 0, 16));
        lbl.setPreferredSize(new Dimension(130, 32));
        form.add(lbl, gbc);
        gbc.gridx = 1;
        gbc.weightx = 1;
        form.add(field, gbc);
    }

    private JPanel wrapBtn(JButton btn) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT, 24, 16));
        p.setBackground(C_CARD_BG);
        p.setBorder(new MatteBorder(1, 0, 0, 0, C_BORDER));
        p.add(btn);
        return p;
    }

    // Short dialog helpers
    private void warn(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Thông báo", JOptionPane.WARNING_MESSAGE);
    }

    private void info(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Thông báo", JOptionPane.INFORMATION_MESSAGE);
    }

    private void err(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Lỗi", JOptionPane.ERROR_MESSAGE);
    }

    private boolean confirm(String msg) {
        return JOptionPane.showConfirmDialog(this, msg, "Xác nhận",
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }

    private String generateNextTableId(DefaultTableModel model) {
        int maxId = 0;
        for (int i = 0; i < model.getRowCount(); i++) {
            String maBan = (String) model.getValueAt(i, 0); // Ví dụ: "B05"
            try {
                int id = Integer.parseInt(maBan.substring(1));
                if (id > maxId)
                    maxId = id;
            } catch (Exception e) {
            }
        }
        return String.format("B%02d", maxId + 1);
    }
}
