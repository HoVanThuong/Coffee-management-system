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
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ManagerDashboard extends JFrame {
    private Client client;
    private TaiKhoan taiKhoan;
    
    private CardLayout cardLayout;
    private JPanel mainContentPanel;
    
    // Colors for modern UI
    private final Color PRIMARY_COLOR = new Color(44, 62, 80); // Dark Blue/Gray
    private final Color HOVER_COLOR = new Color(52, 73, 94);
    private final Color ACCENT_COLOR = new Color(41, 128, 185); // Blue
    private final Color BACKGROUND_COLOR = new Color(245, 246, 250); // Light Gray
    private final Color TEXT_COLOR_LIGHT = new Color(236, 240, 241);
    
    public ManagerDashboard(Client client, TaiKhoan taiKhoan) {
        this.client = client;
        this.taiKhoan = taiKhoan;

        setTitle("Coffee Manager - Dashboard Quản Lý");
        setSize(1100, 750);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(BACKGROUND_COLOR);

        // 1. Sidebar (Menu điều hướng)
        JPanel sidebar = createSidebar();
        add(sidebar, BorderLayout.WEST);

        // 2. Main Content Area (Khu vực nội dung chính)
        cardLayout = new CardLayout();
        mainContentPanel = new JPanel(cardLayout);
        mainContentPanel.setBackground(BACKGROUND_COLOR);
        
        // Thêm các trang vào CardLayout
        mainContentPanel.add(createMenuManagementPanel(), "MENU");
        mainContentPanel.add(createEmployeeManagementPanel(), "STAFF");
        mainContentPanel.add(createInvoiceManagementPanel(), "INVOICES");

        add(mainContentPanel, BorderLayout.CENTER);
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BorderLayout());
        sidebar.setPreferredSize(new Dimension(250, getHeight()));
        sidebar.setBackground(PRIMARY_COLOR);

        // Logo/Brand khu vực trên cùng của sidebar
        JPanel brandPanel = new JPanel(new BorderLayout());
        brandPanel.setBackground(PRIMARY_COLOR);
        brandPanel.setBorder(new EmptyBorder(30, 20, 30, 20));
        
        JLabel lblBrand = new JLabel("COFFEE MANAGER");
        lblBrand.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblBrand.setForeground(TEXT_COLOR_LIGHT);
        lblBrand.setHorizontalAlignment(SwingConstants.CENTER);
        brandPanel.add(lblBrand, BorderLayout.CENTER);
        
        JLabel lblUser = new JLabel("Xin chào, " + taiKhoan.getNhanVien().getHoTen());
        lblUser.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblUser.setForeground(new Color(189, 195, 199));
        lblUser.setHorizontalAlignment(SwingConstants.CENTER);
        brandPanel.add(lblUser, BorderLayout.SOUTH);

        sidebar.add(brandPanel, BorderLayout.NORTH);

        // Các nút menu
        JPanel menuPanel = new JPanel();
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));
        menuPanel.setBackground(PRIMARY_COLOR);
        menuPanel.setBorder(new EmptyBorder(10, 0, 10, 0));

        JButton btnMenu = createSidebarButton("Quản Lý Thực Đơn", "MENU");
        JButton btnStaff = createSidebarButton("Quản Lý Nhân Viên", "STAFF");
        JButton btnInvoices = createSidebarButton("Lịch Sử Hóa Đơn", "INVOICES");

        menuPanel.add(btnMenu);
        menuPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        menuPanel.add(btnStaff);
        menuPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        menuPanel.add(btnInvoices);

        sidebar.add(menuPanel, BorderLayout.CENTER);

        // Nút đăng xuất ở dưới cùng
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(PRIMARY_COLOR);
        bottomPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        JButton btnLogout = new JButton("Đăng Xuất");
        btnLogout.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnLogout.setForeground(Color.WHITE);
        btnLogout.setBackground(new Color(192, 57, 43)); // Red
        btnLogout.setFocusPainted(false);
        btnLogout.setBorderPainted(false);
        btnLogout.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLogout.setPreferredSize(new Dimension(200, 40));
        btnLogout.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc chắn muốn đăng xuất?", "Xác nhận", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                dispose();
                new LoginFrame(client).setVisible(true);
            }
        });
        
        bottomPanel.add(btnLogout, BorderLayout.CENTER);
        sidebar.add(bottomPanel, BorderLayout.SOUTH);

        return sidebar;
    }

    private JButton createSidebarButton(String text, String cardName) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btn.setForeground(TEXT_COLOR_LIGHT);
        btn.setBackground(PRIMARY_COLOR);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBorder(new EmptyBorder(15, 25, 15, 20));
        btn.setMaximumSize(new Dimension(250, 50));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(HOVER_COLOR);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(PRIMARY_COLOR);
            }
        });

        btn.addActionListener(e -> cardLayout.show(mainContentPanel, cardName));

        return btn;
    }

    // --------------------------------------------------------
    // QUẢN LÝ THỰC ĐƠN
    // --------------------------------------------------------
    private JPanel createMenuManagementPanel() {
        JPanel panel = new JPanel(new BorderLayout(20, 20));
        panel.setBackground(BACKGROUND_COLOR);
        panel.setBorder(new EmptyBorder(30, 30, 30, 30));

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(BACKGROUND_COLOR);
        
        JLabel lblTitle = new JLabel("Quản Lý Thực Đơn");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblTitle.setForeground(PRIMARY_COLOR);
        headerPanel.add(lblTitle, BorderLayout.WEST);

        panel.add(headerPanel, BorderLayout.NORTH);

        String[] cols = {"Mã Đồ Uống", "Tên Đồ Uống", "Giá Tiền (VNĐ)", "Loại"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        JTable table = new JTable(model);
        styleTable(table);
        
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        table.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(223, 230, 233), 1));
        scrollPane.getViewport().setBackground(Color.WHITE);
        panel.add(scrollPane, BorderLayout.CENTER);

        loadMenuData(model);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        actionPanel.setBackground(BACKGROUND_COLOR);
        
        JButton btnAdd = createActionButton("Thêm Món", new Color(39, 174, 96));
        JButton btnEdit = createActionButton("Sửa Món", new Color(243, 156, 18));
        JButton btnDel = createActionButton("🗑Xóa Món", new Color(192, 57, 43));
        JButton btnRefresh = createActionButton("Làm Mới", ACCENT_COLOR);
        
        btnAdd.addActionListener(e -> showDoUongFormDialog(null, model));
        
        btnEdit.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn món để sửa!", "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }
            DoUong d = new DoUong();
            d.setMaDoUong((String) model.getValueAt(row, 0));
            d.setTenDoUong((String) model.getValueAt(row, 1));
            String rawPrice = (String) model.getValueAt(row, 2);
            d.setGiaTien(rawPrice.replace(",", ""));
            d.setLoaiDoUong((String) model.getValueAt(row, 3));
            
            showDoUongFormDialog(d, model);
        });

        btnDel.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn món để xóa!", "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String maMon = (String) model.getValueAt(row, 0);
            String tenMon = (String) model.getValueAt(row, 1);
            int confirm = JOptionPane.showConfirmDialog(this, "Chắc chắn xóa món: " + tenMon + "?", "Xác nhận", JOptionPane.YES_NO_OPTION);
            
            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    Response res = client.sendRequest(new Request(CommandType.MANAGE_MENU_DELETE, maMon));
                    if (res.isSuccess()) {
                        JOptionPane.showMessageDialog(this, "Xóa thành công!");
                        loadMenuData(model);
                    } else {
                        JOptionPane.showMessageDialog(this, res.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        btnRefresh.addActionListener(e -> loadMenuData(model));

        actionPanel.add(btnAdd);
        actionPanel.add(btnEdit);
        actionPanel.add(btnDel);
        actionPanel.add(btnRefresh);

        panel.add(actionPanel, BorderLayout.SOUTH);
        return panel;
    }

    private void showDoUongFormDialog(DoUong existing, DefaultTableModel model) {
        JDialog dialog = new JDialog(this, existing == null ? "Thêm Món Mới" : "Sửa Món", true);
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel formPanel = new JPanel(new GridLayout(4, 2, 10, 15));
        formPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JTextField txtMaMon = new JTextField();
        JTextField txtTenMon = new JTextField();
        JTextField txtGia = new JTextField();
        JComboBox<String> cbLoai = new JComboBox<>(new String[]{"Cà phê", "Trà", "Sinh tố", "Nước ép", "Khác"});

        if (existing != null) {
            txtMaMon.setText(existing.getMaDoUong());
            txtMaMon.setEditable(false);
            txtTenMon.setText(existing.getTenDoUong());
            txtGia.setText(existing.getGiaTien());
            cbLoai.setSelectedItem(existing.getLoaiDoUong());
        } else {
            txtMaMon.setText("DU" + System.currentTimeMillis() % 10000);
        }

        formPanel.add(new JLabel("Mã Đồ Uống:")); formPanel.add(txtMaMon);
        formPanel.add(new JLabel("Tên Đồ Uống:")); formPanel.add(txtTenMon);
        formPanel.add(new JLabel("Giá Tiền (VNĐ):")); formPanel.add(txtGia);
        formPanel.add(new JLabel("Loại:")); formPanel.add(cbLoai);

        JButton btnSave = new JButton("Lưu");
        btnSave.addActionListener(e -> {
            if(txtTenMon.getText().isEmpty() || txtGia.getText().isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Vui lòng nhập đầy đủ thông tin!", "Lỗi", JOptionPane.WARNING_MESSAGE);
                return;
            }
            try {
                Double.parseDouble(txtGia.getText());
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Giá tiền phải là số hợp lệ!", "Lỗi", JOptionPane.WARNING_MESSAGE);
                return;
            }

            DoUong d = new DoUong();
            d.setMaDoUong(txtMaMon.getText());
            d.setTenDoUong(txtTenMon.getText());
            d.setGiaTien(txtGia.getText());
            d.setLoaiDoUong((String) cbLoai.getSelectedItem());

            CommandType cmd = existing == null ? CommandType.MANAGE_MENU_ADD : CommandType.MANAGE_MENU_UPDATE;
            try {
                Response res = client.sendRequest(new Request(cmd, d));
                if (res.isSuccess()) {
                    JOptionPane.showMessageDialog(dialog, "Thành công!");
                    loadMenuData(model);
                    dialog.dispose();
                } else {
                    JOptionPane.showMessageDialog(dialog, "Lỗi: " + res.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        JPanel btnPanel = new JPanel();
        btnPanel.add(btnSave);

        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(btnPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }
    
    private void loadMenuData(DefaultTableModel model) {
        model.setRowCount(0); 
        try {
            Response res = client.sendRequest(new Request(CommandType.GET_MENU, null));
            if (res.isSuccess() && res.getData() != null) {
                List<DoUong> menu = (List<DoUong>) res.getData();
                for (DoUong d : menu) {
                    String formattedPrice;
                    try {
                        formattedPrice = String.format("%,.0f", Double.parseDouble(d.getGiaTien()));
                    } catch(NumberFormatException e) {
                        formattedPrice = d.getGiaTien();
                    }
                    model.addRow(new Object[]{d.getMaDoUong(), d.getTenDoUong(), formattedPrice, d.getLoaiDoUong()});
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // --------------------------------------------------------
    // QUẢN LÝ NHÂN VIÊN
    // --------------------------------------------------------
    private JPanel createEmployeeManagementPanel() {
        JPanel panel = new JPanel(new BorderLayout(20, 20));
        panel.setBackground(BACKGROUND_COLOR);
        panel.setBorder(new EmptyBorder(30, 30, 30, 30));

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(BACKGROUND_COLOR);
        JLabel lblTitle = new JLabel("Quản Lý Nhân Viên");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblTitle.setForeground(PRIMARY_COLOR);
        headerPanel.add(lblTitle, BorderLayout.WEST);

        JCheckBox chkShowFired = new JCheckBox("Hiển thị nhân viên đã nghỉ");
        chkShowFired.setBackground(BACKGROUND_COLOR);
        chkShowFired.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        headerPanel.add(chkShowFired, BorderLayout.EAST);
        panel.add(headerPanel, BorderLayout.NORTH);

        String[] cols = {"Mã NV", "Họ Tên", "Số ĐT", "Chức Vụ", "Ngày Vào Làm", "Ngày Nghỉ"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        JTable table = new JTable(model);
        styleTable(table);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(223, 230, 233), 1));
        panel.add(scrollPane, BorderLayout.CENTER);

        loadEmployeeData(model, false);
        chkShowFired.addActionListener(e -> loadEmployeeData(model, chkShowFired.isSelected()));

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        actionPanel.setBackground(BACKGROUND_COLOR);
        
        JButton btnAdd = createActionButton("Thêm NV", new Color(39, 174, 96));
        JButton btnEdit = createActionButton("Sửa NV", new Color(243, 156, 18));
        JButton btnDel = createActionButton("Đuổi Việc", new Color(192, 57, 43));
        JButton btnRefresh = createActionButton("Làm Mới", ACCENT_COLOR);
        
        btnAdd.addActionListener(e -> showEmployeeFormDialog(null, model, chkShowFired.isSelected()));
        
        btnEdit.addActionListener(e -> {
            int row = table.getSelectedRow();
            if(row == -1) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn nhân viên để sửa!", "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String maNV = (String) model.getValueAt(row, 0);
            NhanVien nv = new NhanVien();
            nv.setMaNhanVien(maNV);
            nv.setHoTen((String) model.getValueAt(row, 1));
            nv.setSdt((String) model.getValueAt(row, 2));
            nv.setChucVu((String) model.getValueAt(row, 3));
            
            // For full edit, a dedicated fetch should happen, but we'll use placeholder TK for now
            TaiKhoan tk = new TaiKhoan(); 
            tk.setTenDangNhap(maNV + "_user");
            tk.setTaiKhoanQuanLi("Manager".equals(nv.getChucVu()));
            
            showEmployeeFormDialog(new Object[]{nv, tk}, model, chkShowFired.isSelected());
        });

        btnDel.addActionListener(e -> {
            int row = table.getSelectedRow();
            if(row == -1) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn nhân viên!", "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (model.getValueAt(row, 5) != null && !model.getValueAt(row, 5).toString().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Nhân viên này đã nghỉ việc rồi!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            String maNV = (String) model.getValueAt(row, 0);
            int confirm = JOptionPane.showConfirmDialog(this, "Chắc chắn cho nhân viên " + maNV + " thôi việc?", "Xác nhận", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    Response res = client.sendRequest(new Request(CommandType.MANAGE_EMPLOYEE_DELETE, maNV));
                    if (res.isSuccess()) {
                        JOptionPane.showMessageDialog(this, "Đã cập nhật trạng thái nghỉ việc thành công!");
                        loadEmployeeData(model, chkShowFired.isSelected());
                    } else {
                        JOptionPane.showMessageDialog(this, res.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        btnRefresh.addActionListener(e -> loadEmployeeData(model, chkShowFired.isSelected()));

        actionPanel.add(btnAdd);
        actionPanel.add(btnEdit);
        actionPanel.add(btnDel);
        actionPanel.add(btnRefresh);

        panel.add(actionPanel, BorderLayout.SOUTH);
        return panel;
    }

    private void loadEmployeeData(DefaultTableModel model, boolean includeFired) {
        model.setRowCount(0);
        try {
            Response res = client.sendRequest(new Request(CommandType.GET_EMPLOYEES, includeFired));
            if (res.isSuccess() && res.getData() != null) {
                List<NhanVien> list = (List<NhanVien>) res.getData();
                for (NhanVien n : list) {
                    String ngayVao = n.getNgayVaoLam() != null ? n.getNgayVaoLam().toString() : "";
                    String ngayNghi = n.getNgayThoiViec() != null ? n.getNgayThoiViec().toString() : "";
                    model.addRow(new Object[]{ n.getMaNhanVien(), n.getHoTen(), n.getSdt(), n.getChucVu(), ngayVao, ngayNghi });
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void showEmployeeFormDialog(Object[] existingData, DefaultTableModel model, boolean chkState) {
        JDialog dialog = new JDialog(this, existingData == null ? "Thêm Nhân Viên" : "Sửa Nhân Viên", true);
        dialog.setSize(400, 450);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel formPanel = new JPanel(new GridLayout(7, 2, 10, 10));
        formPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JTextField txtMaNV = new JTextField();
        JTextField txtHoTen = new JTextField();
        JTextField txtSdt = new JTextField();
        JComboBox<String> cbChucVu = new JComboBox<>(new String[]{"Staff", "Manager"});
        JTextField txtUsername = new JTextField();
        JPasswordField txtPassword = new JPasswordField();
        JCheckBox chkIsManager = new JCheckBox("Tài khoản Quản lý");

        if (existingData != null) {
            NhanVien nv = (NhanVien) existingData[0];
            TaiKhoan tk = (TaiKhoan) existingData[1];
            txtMaNV.setText(nv.getMaNhanVien());
            txtMaNV.setEditable(false);
            txtHoTen.setText(nv.getHoTen());
            txtSdt.setText(nv.getSdt());
            cbChucVu.setSelectedItem(nv.getChucVu());
            txtUsername.setText(tk.getTenDangNhap());
            chkIsManager.setSelected(tk.isTaiKhoanQuanLi());
        } else {
            txtMaNV.setText("NV" + System.currentTimeMillis() % 10000);
        }

        formPanel.add(new JLabel("Mã Nhân Viên:")); formPanel.add(txtMaNV);
        formPanel.add(new JLabel("Họ Tên:")); formPanel.add(txtHoTen);
        formPanel.add(new JLabel("Số Điện Thoại:")); formPanel.add(txtSdt);
        formPanel.add(new JLabel("Chức Vụ:")); formPanel.add(cbChucVu);
        formPanel.add(new JLabel("Tên Đăng Nhập:")); formPanel.add(txtUsername);
        formPanel.add(new JLabel("Mật Khẩu:")); formPanel.add(txtPassword);
        formPanel.add(new JLabel("Quyền Quản Lý:")); formPanel.add(chkIsManager);

        JButton btnSave = new JButton("Lưu");
        btnSave.addActionListener(e -> {
            NhanVien nv = new NhanVien();
            nv.setMaNhanVien(txtMaNV.getText());
            nv.setHoTen(txtHoTen.getText());
            nv.setSdt(txtSdt.getText());
            nv.setChucVu((String) cbChucVu.getSelectedItem());
            if (existingData == null) nv.setNgayVaoLam(LocalDate.now());

            TaiKhoan tk = new TaiKhoan();
            tk.setMaTaiKhoan("TK_" + nv.getMaNhanVien());
            tk.setTenDangNhap(txtUsername.getText());
            tk.setMatKhau(new String(txtPassword.getPassword()));
            tk.setTaiKhoanQuanLi(chkIsManager.isSelected());

            CommandType cmd = existingData == null ? CommandType.MANAGE_EMPLOYEE_ADD : CommandType.MANAGE_EMPLOYEE_UPDATE;
            try {
                Response res = client.sendRequest(new Request(cmd, new Object[]{nv, tk}));
                if (res.isSuccess()) {
                    JOptionPane.showMessageDialog(dialog, "Thành công!");
                    loadEmployeeData(model, chkState);
                    dialog.dispose();
                } else {
                    JOptionPane.showMessageDialog(dialog, "Lỗi: " + res.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        JPanel btnPanel = new JPanel();
        btnPanel.add(btnSave);

        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(btnPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    // --------------------------------------------------------
    // LỊCH SỬ HÓA ĐƠN (Chỉ xem)
    // --------------------------------------------------------
    private JPanel createInvoiceManagementPanel() {
        JPanel panel = new JPanel(new BorderLayout(20, 20));
        panel.setBackground(BACKGROUND_COLOR);
        panel.setBorder(new EmptyBorder(30, 30, 30, 30));

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(BACKGROUND_COLOR);
        JLabel lblTitle = new JLabel("Lịch Sử Hóa Đơn");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblTitle.setForeground(PRIMARY_COLOR);
        headerPanel.add(lblTitle, BorderLayout.WEST);

        panel.add(headerPanel, BorderLayout.NORTH);

        String[] cols = {"Mã Hóa Đơn", "Ngày Thanh Toán", "Mã Bàn", "Nhân Viên Thu Ngân", "Tổng Tiền (VNĐ)", "Ghi Chú"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        JTable table = new JTable(model);
        styleTable(table);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        table.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(223, 230, 233), 1));
        panel.add(scrollPane, BorderLayout.CENTER);

        loadInvoiceData(model);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        actionPanel.setBackground(BACKGROUND_COLOR);
        
        JButton btnRefresh = createActionButton("Cập Nhật Dữ Liệu", ACCENT_COLOR);
        btnRefresh.addActionListener(e -> loadInvoiceData(model));
        
        actionPanel.add(btnRefresh);
        panel.add(actionPanel, BorderLayout.SOUTH);
        
        return panel;
    }

    private void loadInvoiceData(DefaultTableModel model) {
        model.setRowCount(0);
        try {
            Response res = client.sendRequest(new Request(CommandType.GET_INVOICES, null));
            if (res.isSuccess() && res.getData() != null) {
                List<HoaDon> list = (List<HoaDon>) res.getData();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                for (HoaDon h : list) {
                    String ngay = h.getNgayTao() != null ? h.getNgayTao().format(formatter) : "";
                    String maBan = (h.getPhieuGoiMon() != null && h.getPhieuGoiMon().getBan() != null) ? h.getPhieuGoiMon().getBan().getMaBan() : "N/A";
                    String nv = (h.getPhieuGoiMon() != null && h.getPhieuGoiMon().getNhanVien() != null) ? h.getPhieuGoiMon().getNhanVien().getHoTen() : "N/A";
                    String tien = String.format("%,.0f", h.getTongTien());
                    model.addRow(new Object[]{
                        h.getMaHoaDon(), ngay, maBan, nv, tien, h.getGhiChu()
                    });
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // --------------------------------------------------------
    // UI UTILITIES
    // --------------------------------------------------------
    private void styleTable(JTable table) {
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setRowHeight(35);
        table.setSelectionBackground(new Color(236, 240, 241));
        table.setSelectionForeground(Color.BLACK);
        table.setShowVerticalLines(false);
        table.setGridColor(new Color(223, 230, 233));
        
        JTableHeader tableHeader = table.getTableHeader();
        tableHeader.setFont(new Font("Segoe UI", Font.BOLD, 14));
        tableHeader.setBackground(PRIMARY_COLOR);
        tableHeader.setForeground(Color.WHITE);
        tableHeader.setPreferredSize(new Dimension(100, 40));
    }

    private JButton createActionButton(String text, Color bgColor) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setForeground(Color.WHITE);
        btn.setBackground(bgColor);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(180, 40));
        
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) { btn.setBackground(bgColor.brighter()); }
            @Override
            public void mouseExited(MouseEvent e) { btn.setBackground(bgColor); }
        });
        
        return btn;
    }
}