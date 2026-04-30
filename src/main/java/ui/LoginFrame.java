package ui;

import com.formdev.flatlaf.FlatLightLaf;
import entity.TaiKhoan;
import network.Client;
import network.CommandType;
import network.Request;
import network.Response;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.File;
import java.net.URL;

public class LoginFrame extends JFrame {
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private Client client;

    public LoginFrame(Client client) {
        this.client = client;
        initComponent();
    }

    private void initComponent() {
        setTitle("Đăng nhập Hệ thống Quản lý Quán Cà phê");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
            SwingUtilities.updateComponentTreeUI(this);
        } catch (Exception ex) {
            System.err.println("Failed to initialize LaF");
        }

        // Main container
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(400);
        splitPane.setEnabled(false); // Disable resizing

        // Left Panel (Image)
        JPanel imagePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                try {
                    URL imageURL = getClass().getResource("/img/login_background.png");
                    if (imageURL != null) {
                        ImageIcon imageIcon = new ImageIcon(imageURL);
                        Image image = imageIcon.getImage();
                        g.drawImage(image, 0, 0, getWidth(), getHeight(), this);
                    } else {
                        g.setColor(Color.WHITE);
                        g.fillRect(0, 0, getWidth(), getHeight());
                        g.setColor(Color.BLACK);
                        g.drawString("Image not found", 10, 20);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        imagePanel.setLayout(new BorderLayout());

        // Right Panel (Login Form)
        JPanel loginPanel = new JPanel();
        loginPanel.setLayout(new GridBagLayout());
        loginPanel.setBackground(Color.WHITE);
        loginPanel.setBorder(BorderFactory.createEmptyBorder(50, 50, 50, 50));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;

        JLabel lblTitle = new JLabel("ĐĂNG NHẬP");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 32));
        lblTitle.setForeground(new Color(38, 50, 56)); // Dark gray
        loginPanel.add(lblTitle, gbc);

        gbc.gridy++;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Username field with placeholder
        txtUsername = new JTextField(20);
        addPlaceholderStyle(txtUsername, "Tên đăng nhập");
        txtUsername.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        gbc.gridy++;
        loginPanel.add(txtUsername, gbc);

        // Password field with placeholder
        txtPassword = new JPasswordField(20);
        addPlaceholderStyle(txtPassword, "Mật khẩu");
        txtPassword.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        gbc.gridy++;
        loginPanel.add(txtPassword, gbc);

        // Login Button
        gbc.gridy++;
        gbc.anchor = GridBagConstraints.CENTER;
        JButton btnLogin = new JButton("Đăng Nhập");
        btnLogin.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btnLogin.setBackground(new Color(0, 123, 255)); // Blue
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setFocusPainted(false);
        btnLogin.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLogin.putClientProperty("JButton.buttonType", "roundRect");
        btnLogin.setPreferredSize(new Dimension(150, 40));
        btnLogin.addActionListener(e -> handleLogin());
        loginPanel.add(btnLogin, gbc);

//        // Forgot password link
//        gbc.gridy++;
//        JLabel lblForgotPassword = new JLabel("Quên mật khẩu?");
//        lblForgotPassword.setFont(new Font("Segoe UI", Font.PLAIN, 12));
//        lblForgotPassword.setForeground(Color.BLUE);
//        lblForgotPassword.setCursor(new Cursor(Cursor.HAND_CURSOR));
//        loginPanel.add(lblForgotPassword, gbc);

        splitPane.setLeftComponent(imagePanel);
        splitPane.setRightComponent(loginPanel);

        add(splitPane);
    }

    private void addPlaceholderStyle(JTextField textField, String placeholder) {
        textField.setText(placeholder);
        textField.setForeground(Color.GRAY);
        
        if (textField instanceof JPasswordField) {
            ((JPasswordField) textField).setEchoChar((char) 0);
        }

        textField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (textField.getText().equals(placeholder)) {
                    textField.setText("");
                    textField.setForeground(Color.BLACK);
                    if (textField instanceof JPasswordField) {
                        ((JPasswordField) textField).setEchoChar('•');
                    }
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (textField.getText().isEmpty()) {
                    textField.setText(placeholder);
                    textField.setForeground(Color.GRAY);
                    if (textField instanceof JPasswordField) {
                        ((JPasswordField) textField).setEchoChar((char) 0);
                    }
                }
            }
        });
    }

    private void handleLogin() {
        String username = txtUsername.getText();
        String password = new String(txtPassword.getPassword());

        if (username.equals("Tên đăng nhập") || password.equals("Mật khẩu")) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ thông tin!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        TaiKhoan tk = new TaiKhoan();
        tk.setTenDangNhap(username);
        tk.setMatKhau(password);

        Request req = new Request(CommandType.LOGIN, tk);
        Response res = client.sendRequest(req);

        if (res.isSuccess()) {
            TaiKhoan loggedInTk = (TaiKhoan) res.getData();
            JOptionPane.showMessageDialog(this, "Đăng nhập thành công! Chào mừng " + loggedInTk.getNhanVien().getHoTen());
            this.dispose();

            if (loggedInTk.isTaiKhoanQuanLi()) {
                new ManagerDashboard(client, loggedInTk).setVisible(true);
            } else {
                new StaffDashboard(client, loggedInTk).setVisible(true);
            }
        } else {
            JOptionPane.showMessageDialog(this, res.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        try {
            Client client = new Client();
            client.connect();
            SwingUtilities.invokeLater(() -> new LoginFrame(client).setVisible(true));
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Không thể kết nối đến Server! Vui lòng kiểm tra lại.", "Lỗi Kết Nối", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
}
