package ui.components;

import entity.NhanVien;
import entity.TaiKhoan;
import network.Client;
import network.CommandType;
import network.Request;
import network.Response;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;

/**
 * Panel hiển thị thông tin cá nhân và chức năng đổi mật khẩu.
 * Dùng chung cho cả ManagerDashboard và StaffDashboard.
 */
public class SettingsPanel extends JPanel {

    private final Client client;
    private final TaiKhoan taiKhoan;

    // ── Palette (inherit from dashboards) ──────────────────────
    private static final Color C_PAGE_BG = new Color(246, 248, 252);
    private static final Color C_CARD_BG = Color.WHITE;
    private static final Color C_TEXT_PRIMARY = new Color(26, 32, 44);
    private static final Color C_TEXT_MUTED = new Color(113, 128, 150);
    private static final Color C_BORDER = new Color(226, 232, 240);
    private static final Color C_ACCENT = new Color(99, 179, 237);
    private static final Color C_SUCCESS = new Color(72, 187, 120);
    private static final Color C_DANGER = new Color(245, 101, 96);

    // ── Fonts ──────────────────────────────────────────────────
    private static final Font F_TITLE = new Font("Segoe UI", Font.BOLD, 26);
    private static final Font F_SECTION = new Font("Segoe UI", Font.BOLD, 14);
    private static final Font F_BODY = new Font("Segoe UI", Font.PLAIN, 14);
    private static final Font F_LABEL = new Font("Segoe UI", Font.PLAIN, 12);
    private static final Font F_FIELD = new Font("Segoe UI", Font.PLAIN, 14);

    public SettingsPanel(Client client, TaiKhoan taiKhoan) {
        this.client = client;
        this.taiKhoan = taiKhoan;
        setLayout(new BorderLayout());
        setBackground(C_PAGE_BG);
        build();
    }

    private void build() {
        // ── Top Bar ────────────────────────────────────────────
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(C_CARD_BG);
        topBar.setBorder(new CompoundBorder(
                new MatteBorder(0, 0, 1, 0, C_BORDER),
                new EmptyBorder(20, 32, 20, 32)));
        JLabel lbTitle = new JLabel("Cài Đặt");
        lbTitle.setFont(F_TITLE);
        lbTitle.setForeground(C_TEXT_PRIMARY);
        JLabel lbSub = new JLabel("Thông tin cá nhân và bảo mật tài khoản");
        lbSub.setFont(F_LABEL);
        lbSub.setForeground(C_TEXT_MUTED);
        JPanel titleGrp = new JPanel();
        titleGrp.setLayout(new BoxLayout(titleGrp, BoxLayout.Y_AXIS));
        titleGrp.setOpaque(false);
        titleGrp.add(lbTitle);
        titleGrp.add(Box.createRigidArea(new Dimension(0, 3)));
        titleGrp.add(lbSub);
        topBar.add(titleGrp, BorderLayout.WEST);
        add(topBar, BorderLayout.NORTH);

        // ── Body ───────────────────────────────────────────────
        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBackground(C_PAGE_BG);
        body.setBorder(new EmptyBorder(32, 48, 32, 48));

        body.add(buildInfoCard());
        body.add(Box.createRigidArea(new Dimension(0, 24)));
        body.add(buildPasswordCard());

        JScrollPane scroll = new JScrollPane(body);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);
    }

    // ── Card: Thông tin cá nhân ────────────────────────────────
    private JPanel buildInfoCard() {
        JPanel card = createCard("Thông Tin Cá Nhân");

        NhanVien nv = taiKhoan.getNhanVien();

        JPanel grid = new JPanel(new GridBagLayout());
        grid.setOpaque(false);
        grid.setBorder(new EmptyBorder(0, 0, 8, 0));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 0, 8, 24);
        gbc.anchor = GridBagConstraints.WEST;

        int row = 0;
        addInfoRow(grid, gbc, row++, "Mã nhân viên", nv.getMaNhanVien());
        addInfoRow(grid, gbc, row++, "Họ và tên", nv.getHoTen());
        addInfoRow(grid, gbc, row++, "Số điện thoại", nv.getSdt() != null ? nv.getSdt() : "—");
        addInfoRow(grid, gbc, row++, "Chức vụ", nv.getChucVu() != null ? nv.getChucVu() : "—");
        addInfoRow(grid, gbc, row++, "Ngày vào làm", nv.getNgayVaoLam() != null ? nv.getNgayVaoLam().toString() : "—");
        addInfoRow(grid, gbc, row++, "Tên đăng nhập", taiKhoan.getTenDangNhap());

        card.add(grid);
        return card;
    }

    private void addInfoRow(JPanel grid, GridBagConstraints gbc, int row, String label, String value) {
        // Label column
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        JLabel lbl = new JLabel(label);
        lbl.setFont(F_LABEL);
        lbl.setForeground(C_TEXT_MUTED);
        lbl.setPreferredSize(new Dimension(150, 24));
        grid.add(lbl, gbc);

        // Value column
        gbc.gridx = 1;
        gbc.weightx = 1;
        JLabel val = new JLabel(value);
        val.setFont(F_BODY);
        val.setForeground(C_TEXT_PRIMARY);
        grid.add(val, gbc);
    }

    // ── Card: Đổi mật khẩu ────────────────────────────────────
    private JPanel buildPasswordCard() {
        JPanel card = createCard("Đổi Mật Khẩu");

        JPasswordField txtOld = styledPasswordField();
        JPasswordField txtNew = styledPasswordField();
        JPasswordField txtConfirm = styledPasswordField();

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 0, 8, 16);
        gbc.anchor = GridBagConstraints.WEST;

        // Row 0 — Mật khẩu cũ
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        form.add(mkLabel("Mật khẩu hiện tại"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        form.add(txtOld, gbc);

        // Row 1 — Mật khẩu mới
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        form.add(mkLabel("Mật khẩu mới"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        form.add(txtNew, gbc);

        // Row 2 — Xác nhận mật khẩu mới
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        form.add(mkLabel("Nhập lại mật khẩu mới"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        form.add(txtConfirm, gbc);

        // Nút
        JButton btnChange = mkButton("Đổi Mật Khẩu", C_ACCENT);
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 12));
        btnRow.setOpaque(false);
        btnRow.add(btnChange);

        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        form.add(btnRow, gbc);

        btnChange.addActionListener(e -> handleChangePassword(txtOld, txtNew, txtConfirm));

        card.add(form);
        return card;
    }

    private void handleChangePassword(JPasswordField txtOld, JPasswordField txtNew, JPasswordField txtConfirm) {
        String oldPw = new String(txtOld.getPassword()).trim();
        String newPw = new String(txtNew.getPassword()).trim();
        String confirmPw = new String(txtConfirm.getPassword()).trim();

        if (oldPw.isEmpty() || newPw.isEmpty() || confirmPw.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Vui lòng điền đầy đủ tất cả các ô mật khẩu!", "Thiếu thông tin",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!newPw.equals(confirmPw)) {
            JOptionPane.showMessageDialog(this,
                    "Mật khẩu mới và nhập lại không khớp!", "Lỗi xác nhận",
                    JOptionPane.ERROR_MESSAGE);
            txtNew.setText("");
            txtConfirm.setText("");
            txtNew.requestFocus();
            return;
        }
        if (newPw.length() < 6) {
            JOptionPane.showMessageDialog(this,
                    "Mật khẩu mới phải có ít nhất 6 ký tự!", "Mật khẩu quá ngắn",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            Object[] payload = { taiKhoan.getMaTaiKhoan(), oldPw, newPw };
            Response res = client.sendRequest(new Request(CommandType.CHANGE_PASSWORD, payload));
            if (res.isSuccess()) {
                JOptionPane.showMessageDialog(this,
                        "Đổi mật khẩu thành công!", "Thành công",
                        JOptionPane.INFORMATION_MESSAGE);
                txtOld.setText("");
                txtNew.setText("");
                txtConfirm.setText("");
            } else {
                JOptionPane.showMessageDialog(this,
                        res.getMessage(), "Thất bại",
                        JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Không thể kết nối Server!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ── Helpers ───────────────────────────────────────────────
    private JPanel createCard(String title) {
        JPanel outer = new JPanel();
        outer.setLayout(new BoxLayout(outer, BoxLayout.Y_AXIS));
        outer.setBackground(C_CARD_BG);
        outer.setBorder(new CompoundBorder(
                new LineBorder(C_BORDER, 1, true),
                new EmptyBorder(24, 28, 24, 28)));
        outer.setAlignmentX(Component.LEFT_ALIGNMENT);
        outer.setMaximumSize(new Dimension(720, Integer.MAX_VALUE));

        JLabel lbTitle = new JLabel(title);
        lbTitle.setFont(F_SECTION);
        lbTitle.setForeground(C_TEXT_PRIMARY);
        lbTitle.setBorder(new MatteBorder(0, 0, 1, 0, C_BORDER));
        lbTitle.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        lbTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        outer.add(lbTitle);
        outer.add(Box.createRigidArea(new Dimension(0, 16)));
        return outer;
    }

    private JLabel mkLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(F_LABEL);
        l.setForeground(C_TEXT_MUTED);
        l.setPreferredSize(new Dimension(190, 24));
        return l;
    }

    private JPasswordField styledPasswordField() {
        JPasswordField f = new JPasswordField(20);
        f.setFont(F_FIELD);
        f.setBorder(new CompoundBorder(
                new LineBorder(C_BORDER, 1, true),
                new EmptyBorder(8, 12, 8, 12)));
        f.setPreferredSize(new Dimension(280, 38));
        return f;
    }

    private JButton mkButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(F_BODY);
        btn.setForeground(Color.WHITE);
        btn.setBackground(bg);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(10, 24, 10, 24));
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                btn.setBackground(bg.darker());
            }

            public void mouseExited(java.awt.event.MouseEvent e) {
                btn.setBackground(bg);
            }
        });
        return btn;
    }
}
