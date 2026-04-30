package ui;

import entity.DoUong;
import entity.HoaDon;
import entity.NhanVien;
import entity.TaiKhoan;
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

public class ManagerDashboard extends JFrame {

    private Client client;
    private TaiKhoan taiKhoan;

    private CardLayout cardLayout;
    private JPanel mainContentPanel;
    private JButton activeNavButton = null;

    // ── Palette ──────────────────────────────────────────────
    private static final Color C_SIDEBAR_BG   = new Color(15,  17,  23);   // near-black
    private static final Color C_SIDEBAR_SEC  = new Color(22,  26,  35);   // panel header
    private static final Color C_NAV_HOVER    = new Color(30,  35,  48);
    private static final Color C_NAV_ACTIVE   = new Color(99, 179, 237);   // sky-blue accent
    private static final Color C_ACCENT       = new Color(99, 179, 237);
    private static final Color C_ACCENT_DARK  = new Color(66, 153, 225);
    private static final Color C_SUCCESS      = new Color(72, 187, 120);
    private static final Color C_WARNING      = new Color(237, 169, 38);
    private static final Color C_DANGER       = new Color(245, 101,  96);
    private static final Color C_PAGE_BG      = new Color(246, 248, 252);
    private static final Color C_CARD_BG      = Color.WHITE;
    private static final Color C_TEXT_PRIMARY = new Color(26,  32,  44);
    private static final Color C_TEXT_MUTED   = new Color(113, 128, 150);
    private static final Color C_BORDER       = new Color(226, 232, 240);
    private static final Color C_TH_BG        = new Color(247, 250, 252);
    private static final Color C_TR_STRIPE    = new Color(252, 253, 255);

    // ── Fonts ────────────────────────────────────────────────
    private static final Font F_TITLE   = new Font("Segoe UI",        Font.BOLD,  26);
    private static final Font F_SECTION = new Font("Segoe UI",        Font.BOLD,  13);
    private static final Font F_NAV     = new Font("Segoe UI Semibold",Font.PLAIN, 14);
    private static final Font F_BODY    = new Font("Segoe UI",        Font.PLAIN, 13);
    private static final Font F_LABEL   = new Font("Segoe UI",        Font.PLAIN, 12);
    private static final Font F_BADGE   = new Font("Segoe UI",        Font.BOLD,  11);
    private static final Font F_BRAND   = new Font("Segoe UI",        Font.BOLD,  16);

    // ── Nav items ─────────────────────────────────────────────
    private static final String[][] NAV_ITEMS = {
            { "Thực Đơn",    "MENU" },
            { "Nhân Viên",  "STAFF" },
            { "Hóa Đơn",    "INVOICES" },
    };

    public ManagerDashboard(Client client, TaiKhoan taiKhoan) {
        this.client   = client;
        this.taiKhoan = taiKhoan;

        setTitle("Coffee Manager");
        setSize(1280, 800);
        setMinimumSize(new Dimension(1024, 640));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getRootPane().putClientProperty("apple.awt.fullWindowContent", true);

        add(buildSidebar(), BorderLayout.WEST);

        cardLayout       = new CardLayout();
        mainContentPanel = new JPanel(cardLayout);
        mainContentPanel.setBackground(C_PAGE_BG);
        mainContentPanel.add(createMenuManagementPanel(),     "MENU");
        mainContentPanel.add(createEmployeeManagementPanel(), "STAFF");
        mainContentPanel.add(createInvoiceManagementPanel(),  "INVOICES");
        add(mainContentPanel, BorderLayout.CENTER);
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

        JLabel ico = new JLabel("☕");
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
                new EmptyBorder(10, 12, 10, 12)
        ));
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
                new EmptyBorder(8, 14, 8, 14)
        ));
        btnLogout.setFocusPainted(false);
        btnLogout.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnLogout.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnLogout.setMaximumSize(new Dimension(999, 38));
        btnLogout.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btnLogout.setBackground(new Color(245, 101, 96, 40)); }
            public void mouseExited (MouseEvent e) { btnLogout.setBackground(new Color(255, 245, 245, 18)); }
        });
        btnLogout.addActionListener(e -> {
            int ok = JOptionPane.showConfirmDialog(this,
                    "Bạn có chắc chắn muốn đăng xuất?", "Xác nhận đăng xuất",
                    JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (ok == JOptionPane.YES_OPTION) { dispose(); new LoginFrame(client).setVisible(true); }
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
                if (btn != activeNavButton) btn.setBackground(C_NAV_HOVER);
            }
            public void mouseExited(MouseEvent e) {
                if (btn != activeNavButton) { btn.setBackground(C_SIDEBAR_BG); btn.setForeground(new Color(160, 174, 192)); }
            }
        });

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

    private String initials(String name) {
        if (name == null || name.isEmpty()) return "?";
        String[] parts = name.trim().split("\\s+");
        if (parts.length == 1) return String.valueOf(parts[0].charAt(0)).toUpperCase();
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
                new EmptyBorder(20, 32, 20, 32)
        ));

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

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        actions.setOpaque(false);

        String[] menuCols = {"Mã Đồ Uống", "Tên Đồ Uống", "Giá Tiền (VNĐ)", "Loại"};
        DefaultTableModel menuModel = new DefaultTableModel(menuCols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable menuTable = buildTable(menuModel);
        // Center price column
        centerColumn(menuTable, 2);

        JButton btnAdd  = mkButton("Thêm Món",  C_SUCCESS);
        JButton btnEdit = mkButton("Sửa Món",   C_WARNING);
        JButton btnDel  = mkButton("Xóa Món",   C_DANGER);
        JButton btnRef  = mkButton("Làm Mới",    C_ACCENT);

        btnAdd.addActionListener(e -> showDoUongFormDialog(null, menuModel));
        btnEdit.addActionListener(e -> {
            int row = menuTable.getSelectedRow();
            if (row < 0) { warn("Vui lòng chọn món để sửa!"); return; }
            DoUong d = new DoUong();
            d.setMaDoUong((String) menuModel.getValueAt(row, 0));
            d.setTenDoUong((String) menuModel.getValueAt(row, 1));
            d.setGiaTien(((String) menuModel.getValueAt(row, 2)).replace(",", ""));
            d.setLoaiDoUong((String) menuModel.getValueAt(row, 3));
            showDoUongFormDialog(d, menuModel);
        });
        btnDel.addActionListener(e -> {
            int row = menuTable.getSelectedRow();
            if (row < 0) { warn("Vui lòng chọn món để xóa!"); return; }
            String ma  = (String) menuModel.getValueAt(row, 0);
            String ten = (String) menuModel.getValueAt(row, 1);
            if (confirm("Chắc chắn xóa món: " + ten + "?")) {
                try {
                    Response res = client.sendRequest(new Request(CommandType.MANAGE_MENU_DELETE, ma));
                    if (res.isSuccess()) { info("Xóa thành công!"); loadMenuData(menuModel); }
                    else err(res.getMessage());
                } catch (Exception ex) { ex.printStackTrace(); }
            }
        });
        btnRef.addActionListener(e -> loadMenuData(menuModel));

        for (JButton b : new JButton[]{btnAdd, btnEdit, btnDel, btnRef}) actions.add(b);
        toolbar.add(actions, BorderLayout.WEST);
        body.add(toolbar, BorderLayout.NORTH);

        JScrollPane sp = styledScroll(menuTable);
        body.add(sp, BorderLayout.CENTER);

        loadMenuData(menuModel);
        page.add(body, BorderLayout.CENTER);
        return page;
    }

    private void showDoUongFormDialog(DoUong existing, DefaultTableModel model) {
        JDialog dlg = buildDialog(existing == null ? "Thêm Món Mới" : "Sửa Thông Tin Món", 420, 340);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(C_CARD_BG);
        form.setBorder(new EmptyBorder(24, 28, 8, 28));
        GridBagConstraints gbc = formGbc();

        JTextField txtMa   = styledField();
        JTextField txtTen  = styledField();
        JTextField txtGia  = styledField();
        JComboBox<String> cbLoai = styledCombo(new String[]{"Cà phê","Trà","Sinh tố","Nước ép","Khác"});

        if (existing != null) {
            txtMa.setText(existing.getMaDoUong()); txtMa.setEditable(false); txtMa.setForeground(C_TEXT_MUTED);
            txtTen.setText(existing.getTenDoUong());
            txtGia.setText(existing.getGiaTien());
            cbLoai.setSelectedItem(existing.getLoaiDoUong());
        } else {
            txtMa.setText("DU" + System.currentTimeMillis() % 10000);
        }

        addFormRow(form, gbc, 0, "Mã Đồ Uống",      txtMa);
        addFormRow(form, gbc, 1, "Tên Đồ Uống",     txtTen);
        addFormRow(form, gbc, 2, "Giá Tiền (VNĐ)",  txtGia);
        addFormRow(form, gbc, 3, "Loại",             cbLoai);

        JButton btnSave = mkButton("Lưu thông tin", C_ACCENT);
        btnSave.addActionListener(e -> {
            if (txtTen.getText().isEmpty() || txtGia.getText().isEmpty()) {
                JOptionPane.showMessageDialog(dlg, "Vui lòng nhập đầy đủ thông tin!", "Thiếu thông tin", JOptionPane.WARNING_MESSAGE);
                return;
            }
            try { Double.parseDouble(txtGia.getText()); }
            catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dlg, "Giá tiền phải là số hợp lệ!", "Dữ liệu không hợp lệ", JOptionPane.WARNING_MESSAGE);
                return;
            }
            DoUong d = new DoUong();
            d.setMaDoUong(txtMa.getText()); d.setTenDoUong(txtTen.getText());
            d.setGiaTien(txtGia.getText()); d.setLoaiDoUong((String) cbLoai.getSelectedItem());
            CommandType cmd = existing == null ? CommandType.MANAGE_MENU_ADD : CommandType.MANAGE_MENU_UPDATE;
            try {
                Response res = client.sendRequest(new Request(cmd, d));
                if (res.isSuccess()) { JOptionPane.showMessageDialog(dlg, "Thành công!"); loadMenuData(model); dlg.dispose(); }
                else JOptionPane.showMessageDialog(dlg, res.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) { ex.printStackTrace(); }
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
                for (DoUong d : (List<DoUong>) res.getData()) {
                    String price;
                    try { price = String.format("%,.0f", Double.parseDouble(d.getGiaTien())); }
                    catch (NumberFormatException e) { price = d.getGiaTien(); }
                    model.addRow(new Object[]{d.getMaDoUong(), d.getTenDoUong(), price, d.getLoaiDoUong()});
                }
            }
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    // ══════════════════════════════════════════════════════════
    // QUẢN LÝ NHÂN VIÊN
    // ══════════════════════════════════════════════════════════
    private JPanel createEmployeeManagementPanel() {
        JPanel page = buildPageShell("", "Nhân Viên", "Quản lý hồ sơ và tài khoản nhân viên");

        JPanel body = new JPanel(new BorderLayout(0, 16));
        body.setBackground(C_PAGE_BG);
        body.setBorder(new EmptyBorder(24, 32, 24, 32));

        String[] cols = {"Mã NV", "Họ Tên", "Số ĐT", "Chức Vụ", "Ngày Vào Làm", "Ngày Nghỉ"};
        DefaultTableModel empModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
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
                ((JLabel)comp).setBorder(new EmptyBorder(0, 12, 0, 12));
                return comp;
            }
        });

        JPanel toolbar = new JPanel(new BorderLayout(12, 0));
        toolbar.setOpaque(false);

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        left.setOpaque(false);

        JButton btnAdd  = mkButton("Thêm NV",  C_SUCCESS);
        JButton btnEdit = mkButton("Sửa NV",   C_WARNING);
        JButton btnDel  = mkButton("Cho Nghỉ", C_DANGER);
        JButton btnRef  = mkButton("Làm Mới",  C_ACCENT);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        right.setOpaque(false);

        JToggleButton toggleFired = new JToggleButton("Hiện NV đã nghỉ");
        toggleFired.setFont(F_LABEL);
        toggleFired.setForeground(C_TEXT_MUTED);
        toggleFired.setBackground(C_CARD_BG);
        toggleFired.setBorder(new CompoundBorder(
                new LineBorder(C_BORDER, 1, true),
                new EmptyBorder(6, 14, 6, 14)
        ));
        toggleFired.setFocusPainted(false);
        toggleFired.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        loadEmployeeData(empModel, false);
        toggleFired.addActionListener(e -> loadEmployeeData(empModel, toggleFired.isSelected()));

        btnAdd.addActionListener(e -> showEmployeeFormDialog(null, empModel, toggleFired.isSelected()));
        btnEdit.addActionListener(e -> {
            int row = empTable.getSelectedRow();
            if (row < 0) { warn("Vui lòng chọn nhân viên để sửa!"); return; }
            String maNV = (String) empModel.getValueAt(row, 0);
            NhanVien nv = new NhanVien();
            nv.setMaNhanVien(maNV);
            nv.setHoTen((String) empModel.getValueAt(row, 1));
            nv.setSdt((String) empModel.getValueAt(row, 2));
            nv.setChucVu((String) empModel.getValueAt(row, 3));
            TaiKhoan tk = new TaiKhoan();
            tk.setTenDangNhap(maNV + "_user");
            tk.setTaiKhoanQuanLi("Manager".equals(nv.getChucVu()));
            showEmployeeFormDialog(new Object[]{nv, tk}, empModel, toggleFired.isSelected());
        });
        btnDel.addActionListener(e -> {
            int row = empTable.getSelectedRow();
            if (row < 0) { warn("Vui lòng chọn nhân viên!"); return; }
            if (empModel.getValueAt(row, 5) != null && !empModel.getValueAt(row, 5).toString().isEmpty()) {
                info("Nhân viên này đã nghỉ việc rồi!"); return;
            }
            String maNV = (String) empModel.getValueAt(row, 0);
            if (confirm("Chắc chắn cho nhân viên " + maNV + " thôi việc?")) {
                try {
                    Response res = client.sendRequest(new Request(CommandType.MANAGE_EMPLOYEE_DELETE, maNV));
                    if (res.isSuccess()) { info("Đã cập nhật trạng thái nghỉ việc!"); loadEmployeeData(empModel, toggleFired.isSelected()); }
                    else err(res.getMessage());
                } catch (Exception ex) { ex.printStackTrace(); }
            }
        });
        btnRef.addActionListener(e -> loadEmployeeData(empModel, toggleFired.isSelected()));

        for (JButton b : new JButton[]{btnAdd, btnEdit, btnDel, btnRef}) left.add(b);
        right.add(toggleFired);
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
                for (NhanVien n : (List<NhanVien>) res.getData()) {
                    model.addRow(new Object[]{
                            n.getMaNhanVien(), n.getHoTen(), n.getSdt(), n.getChucVu(),
                            n.getNgayVaoLam()  != null ? n.getNgayVaoLam().toString()  : "",
                            n.getNgayThoiViec()!= null ? n.getNgayThoiViec().toString(): ""
                    });
                }
            }
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    private void showEmployeeFormDialog(Object[] existingData, DefaultTableModel model, boolean chkState) {
        JDialog dlg = buildDialog(existingData == null ? "Thêm Nhân Viên" : "Sửa Nhân Viên", 440, 460);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(C_CARD_BG);
        form.setBorder(new EmptyBorder(24, 28, 8, 28));
        GridBagConstraints gbc = formGbc();

        JTextField     txtMaNV    = styledField();
        JTextField     txtHoTen   = styledField();
        JTextField     txtSdt     = styledField();
        JComboBox<String> cbChucVu = styledCombo(new String[]{"Staff","Manager"});
        JTextField     txtUser    = styledField();
        JPasswordField txtPass    = new JPasswordField();
        styleField(txtPass);
        JCheckBox      chkMgr     = new JCheckBox("Tài khoản Quản lý");
        chkMgr.setFont(F_BODY); chkMgr.setOpaque(false); chkMgr.setForeground(C_TEXT_PRIMARY);

        if (existingData != null) {
            NhanVien nv = (NhanVien) existingData[0];
            TaiKhoan tk = (TaiKhoan) existingData[1];
            txtMaNV.setText(nv.getMaNhanVien()); txtMaNV.setEditable(false); txtMaNV.setForeground(C_TEXT_MUTED);
            txtHoTen.setText(nv.getHoTen()); txtSdt.setText(nv.getSdt());
            cbChucVu.setSelectedItem(nv.getChucVu());
            txtUser.setText(tk.getTenDangNhap()); chkMgr.setSelected(tk.isTaiKhoanQuanLi());
        } else {
            txtMaNV.setText("NV" + System.currentTimeMillis() % 10000);
        }

        addFormRow(form, gbc, 0, "Mã Nhân Viên",   txtMaNV);
        addFormRow(form, gbc, 1, "Họ Tên",          txtHoTen);
        addFormRow(form, gbc, 2, "Số Điện Thoại",   txtSdt);
        addFormRow(form, gbc, 3, "Chức Vụ",         cbChucVu);
        addFormRow(form, gbc, 4, "Tên Đăng Nhập",   txtUser);
        addFormRow(form, gbc, 5, "Mật Khẩu",        txtPass);
        addFormRow(form, gbc, 6, "Quyền",            chkMgr);

        JButton btnSave = mkButton("Lưu thông tin", C_ACCENT);
        btnSave.addActionListener(e -> {
            NhanVien nv = new NhanVien();
            nv.setMaNhanVien(txtMaNV.getText()); nv.setHoTen(txtHoTen.getText());
            nv.setSdt(txtSdt.getText()); nv.setChucVu((String) cbChucVu.getSelectedItem());
            if (existingData == null) nv.setNgayVaoLam(LocalDate.now());
            TaiKhoan tk = new TaiKhoan();
            tk.setMaTaiKhoan("TK_" + nv.getMaNhanVien());
            tk.setTenDangNhap(txtUser.getText());
            tk.setMatKhau(new String(txtPass.getPassword()));
            tk.setTaiKhoanQuanLi(chkMgr.isSelected());
            CommandType cmd = existingData == null ? CommandType.MANAGE_EMPLOYEE_ADD : CommandType.MANAGE_EMPLOYEE_UPDATE;
            try {
                Response res = client.sendRequest(new Request(cmd, new Object[]{nv, tk}));
                if (res.isSuccess()) { JOptionPane.showMessageDialog(dlg, "Thành công!"); loadEmployeeData(model, chkState); dlg.dispose(); }
                else JOptionPane.showMessageDialog(dlg, res.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) { ex.printStackTrace(); }
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

        String[] cols = {"Mã Hóa Đơn","Ngày Thanh Toán","Mã Bàn","Nhân Viên","Tổng Tiền (VNĐ)","Ghi Chú"};
        DefaultTableModel invModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable invTable = buildTable(invModel);
        centerColumn(invTable, 4);

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        toolbar.setOpaque(false);
        JButton btnRef = mkButton("Cập Nhật Dữ Liệu", C_ACCENT);
        btnRef.addActionListener(e -> loadInvoiceData(invModel));
        toolbar.add(btnRef);

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
                DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                for (HoaDon h : (List<HoaDon>) res.getData()) {
                    String ngay  = h.getNgayTao() != null ? h.getNgayTao().format(fmt) : "";
                    String maBan = (h.getPhieuGoiMon() != null && h.getPhieuGoiMon().getBan() != null)    ? h.getPhieuGoiMon().getBan().getMaBan()      : "N/A";
                    String nv    = (h.getPhieuGoiMon() != null && h.getPhieuGoiMon().getNhanVien() != null)? h.getPhieuGoiMon().getNhanVien().getHoTen()  : "N/A";
                    model.addRow(new Object[]{h.getMaHoaDon(), ngay, maBan, nv, String.format("%,.0f", h.getTongTien()), h.getGhiChu()});
                }
            }
        } catch (Exception ex) { ex.printStackTrace(); }
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
                BorderFactory.createEmptyBorder()
        ));
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
            public void mouseEntered(MouseEvent e) { btn.setBackground(orig.darker()); }
            public void mouseExited (MouseEvent e) { btn.setBackground(orig); }
        });
        return btn;
    }

    private JDialog buildDialog(String title, int w, int h) {
        JDialog dlg = new JDialog(this, title, true);
        dlg.setSize(w, h);
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(new BorderLayout());
        dlg.getRootPane().setBorder(BorderFactory.createEmptyBorder());
        ((JPanel)dlg.getContentPane()).setBackground(C_CARD_BG);
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
                new EmptyBorder(6, 10, 6, 10)
        ));
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
        g.fill   = GridBagConstraints.HORIZONTAL;
        return g;
    }

    private void addFormRow(JPanel form, GridBagConstraints gbc, int row, String label, Component field) {
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        JLabel lbl = new JLabel(label);
        lbl.setFont(F_LABEL); lbl.setForeground(C_TEXT_MUTED);
        lbl.setBorder(new EmptyBorder(0, 0, 0, 16));
        lbl.setPreferredSize(new Dimension(130, 32));
        form.add(lbl, gbc);
        gbc.gridx = 1; gbc.weightx = 1;
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
    private void warn(String msg)  { JOptionPane.showMessageDialog(this, msg, "Thông báo", JOptionPane.WARNING_MESSAGE); }
    private void info(String msg)  { JOptionPane.showMessageDialog(this, msg, "Thông báo", JOptionPane.INFORMATION_MESSAGE); }
    private void err(String msg)   { JOptionPane.showMessageDialog(this, msg, "Lỗi", JOptionPane.ERROR_MESSAGE); }
    private boolean confirm(String msg) {
        return JOptionPane.showConfirmDialog(this, msg, "Xác nhận", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }
}