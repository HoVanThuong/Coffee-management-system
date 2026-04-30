package ui;

import entity.Ban;
import entity.TaiKhoan;
import network.Client;
import network.CommandType;
import network.Request;
import network.Response;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;

public class StaffDashboard extends JFrame {
    private final TaiKhoan currentUser;
    private final Client client;

    private JPanel mainContentPanel;
    private CardLayout cardLayout;

    private JPanel tableLayoutContainer;

    public StaffDashboard(Client client, TaiKhoan currentUser) {
        this.client = client;
        this.currentUser = currentUser;
        initComponents();
        loadTableData();
    }

    private void initComponents() {
        setTitle("Coffee Manager - Nhân Viên: " + currentUser.getNhanVien().getHoTen());
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Xử lý đóng cửa sổ
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                int confirm = JOptionPane.showConfirmDialog(StaffDashboard.this,
                        "Bạn có chắc chắn muốn thoát ứng dụng?", "Xác nhận", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    System.exit(0);
                }
            }
        });

        // 1. Sidebar bên trái
        JPanel sidebar = createSidebar();
        add(sidebar, BorderLayout.WEST);

        // 2. Khu vực trung tâm dùng CardLayout để chuyển đổi
        cardLayout = new CardLayout();
        mainContentPanel = new JPanel(cardLayout);

        // Card 1: Sơ đồ bàn
        tableLayoutContainer = new JPanel(new BorderLayout());
        tableLayoutContainer.setBackground(new Color(245, 246, 250));
        
        JLabel lblTitle = new JLabel("SƠ ĐỒ BÀN", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblTitle.setForeground(new Color(44, 62, 80));
        lblTitle.setBorder(new EmptyBorder(20, 0, 20, 0));
        tableLayoutContainer.add(lblTitle, BorderLayout.NORTH);

        mainContentPanel.add(tableLayoutContainer, "TableLayout");

        add(mainContentPanel, BorderLayout.CENTER);
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(new Color(44, 62, 80));
        sidebar.setPreferredSize(new Dimension(250, 0));
        sidebar.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Info User
        JLabel lblUser = new JLabel("COFFEE STAFF");
        lblUser.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblUser.setForeground(Color.WHITE);
        lblUser.setAlignmentX(Component.CENTER_ALIGNMENT);
        sidebar.add(lblUser);

        JLabel lblRole = new JLabel(currentUser.getTenDangNhap());
        lblRole.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblRole.setForeground(new Color(189, 195, 199));
        lblRole.setAlignmentX(Component.CENTER_ALIGNMENT);
        sidebar.add(lblRole);

        sidebar.add(Box.createRigidArea(new Dimension(0, 40)));

        // Buttons
        JButton btnTables = createSidebarButton("Sơ đồ bàn");
        btnTables.addActionListener(e -> showTablesView());
        sidebar.add(btnTables);

        sidebar.add(Box.createVerticalGlue()); // Đẩy nút Đăng xuất xuống cuối

        JButton btnLogout = createSidebarButton("Đăng Xuất");
        btnLogout.setBackground(new Color(192, 57, 43)); // Màu đỏ
        btnLogout.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc chắn muốn đăng xuất?", "Xác nhận", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                this.dispose();
                new LoginFrame(client).setVisible(true);
            }
        });
        sidebar.add(btnLogout);

        return sidebar;
    }

    private JButton createSidebarButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btn.setForeground(Color.WHITE);
        btn.setBackground(new Color(52, 73, 94));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setMaximumSize(new Dimension(220, 45));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                if (!btn.getBackground().equals(new Color(192, 57, 43))) { // Khác màu đỏ
                    btn.setBackground(new Color(41, 128, 185)); // Màu xanh sáng khi hover
                }
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                if (!btn.getBackground().equals(new Color(192, 57, 43))) {
                    btn.setBackground(new Color(52, 73, 94));
                }
            }
        });
        return btn;
    }

    public void showTablesView() {
        loadTableData(); // Tải lại để cập nhật màu sắc
        cardLayout.show(mainContentPanel, "TableLayout");
    }

    private void loadTableData() {
        try {
            Response res = client.sendRequest(new Request(CommandType.GET_TABLES, null));
            if (res.isSuccess() && res.getData() != null) {
                List<Ban> listBan = (List<Ban>) res.getData();
                displayTables(listBan);
            } else {
                JOptionPane.showMessageDialog(this, "Không thể tải danh sách bàn.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void displayTables(List<Ban> listBan) {
        // Cố định lưới thành 10 hàng x 10 cột (100 ô)
        JPanel gridPanel = new JPanel(new GridLayout(10, 10, 10, 10));
        gridPanel.setBackground(new Color(245, 246, 250));
        gridPanel.setBorder(new EmptyBorder(10, 20, 20, 20));
        
        // Đảm bảo chiều cao của grid panel đủ lớn để các ô luôn giữ tỷ lệ vuông vức hoặc cố định
        gridPanel.setPreferredSize(new Dimension(1000, 1000));

        // Lặp qua 100 vị trí. Chèn bàn nếu có, nếu không thì chèn ô trống (Placeholder)
        for (int i = 0; i < 100; i++) {
            if (i < listBan.size()) {
                gridPanel.add(createTableCard(listBan.get(i)));
            } else {
                // Tạo một panel trong suốt để chiếm chỗ trống, giữ cấu trúc lưới 10x10
                JPanel emptySlot = new JPanel();
                emptySlot.setOpaque(false);
                gridPanel.add(emptySlot);
            }
        }

        // Tạo JScrollPane bọc ngoài Grid để có thể cuộn xem hết
        JScrollPane scrollPane = new JScrollPane(gridPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16); 

        // Xóa layout cũ và thay bằng cái mới
        if (tableLayoutContainer.getComponentCount() > 1) {
            tableLayoutContainer.remove(1); 
        }
        tableLayoutContainer.add(scrollPane, BorderLayout.CENTER);
        tableLayoutContainer.revalidate();
        tableLayoutContainer.repaint();
    }

    private JPanel createTableCard(Ban ban) {
        boolean isOccupied = ban.getTrangThai().equals("Có khách");

        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(isOccupied ? new Color(255, 234, 234) : new Color(234, 255, 234));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(isOccupied ? new Color(231, 76, 60) : new Color(46, 204, 113), 2),
                new EmptyBorder(5, 5, 5, 5) // Giảm padding để có không gian
        ));
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Tên Mã Bàn (Top)
        JLabel lblMaBan = new JLabel(ban.getMaBan(), SwingConstants.CENTER);
        lblMaBan.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblMaBan.setForeground(new Color(44, 62, 80));

        // Icon Emoji lớn ở giữa (Center)
        JLabel lblIcon = new JLabel(isOccupied ? "👥" : "🪑", SwingConstants.CENTER);
        lblIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 36)); 

        // Trạng thái bàn (Bottom)
        JLabel lblTrangThai = new JLabel(ban.getTrangThai(), SwingConstants.CENTER);
        lblTrangThai.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblTrangThai.setForeground(isOccupied ? new Color(231, 76, 60) : new Color(46, 204, 113));

        card.add(lblMaBan, BorderLayout.NORTH);
        card.add(lblIcon, BorderLayout.CENTER);
        card.add(lblTrangThai, BorderLayout.SOUTH);

        // Thêm sự kiện Click vào Card
        card.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                openOrderPanelForTable(ban);
            }
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                card.setBackground(isOccupied ? new Color(255, 210, 210) : new Color(210, 255, 210));
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                card.setBackground(isOccupied ? new Color(255, 234, 234) : new Color(234, 255, 234));
            }
        });

        return card;
    }

    private void openOrderPanelForTable(Ban ban) {
        OrderPanel orderPanel = new OrderPanel(client, currentUser, ban, this);
        mainContentPanel.add(orderPanel, "OrderPanel");
        cardLayout.show(mainContentPanel, "OrderPanel");
    }
}