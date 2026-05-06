package ui;

import entity.ChiTietHoaDon;
import entity.HoaDon;
import network.Client;
import network.CommandType;
import network.Request;
import network.Response;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.concurrent.*;
import javax.imageio.ImageIO;

public class PaymentDialog extends JDialog {

    // ── Thông tin tài khoản nhận tiền ─────────────────────────────
    // TODO: Thay thế bằng thông tin thực của quán
    private static final String BANK_ID = "ACB";
    private static final String ACCOUNT_NO = "35132747";
    private static final String ACCOUNT_NAME = "Hồ Vạn Thương";

    private static final int WIDTH_NORMAL = 550;
    private static final int WIDTH_QR = 870;
    private static final int HEIGHT = 680;

    // ── Fields ─────────────────────────────────────────────────────
    private final Client client;
    private final HoaDon hoaDon;
    private final StaffDashboard parentFrame;
    private double finalAmount;
    private final DecimalFormat df = new DecimalFormat("#,##0");

    private String selectedMethod = "CASH";
    private JLabel valFinal;
    private JToggleButton btnCash, btnQr;
    private JPanel qrSidePanel;
    private JLabel lblQrImage;
    private JLabel lblQrStatus;
    private JPanel centerWrapper;

    // ── Colors ─────────────────────────────────────────────────────
    private static final Color ACCENT_BLUE = new Color(0, 150, 255);
    private static final Color TEXT_DARK = new Color(30, 30, 40);
    private static final Color TEXT_MUTED = new Color(120, 120, 130);
    private static final Color C_BORDER = new Color(230, 230, 235);
    private static final Color QR_BG = new Color(248, 250, 255);
    private static final Color QR_BORDER = new Color(200, 220, 255);

    public PaymentDialog(StaffDashboard parent, Client client, HoaDon hoaDon) {
        super(parent, "Xác nhận thanh toán", true);
        this.parentFrame = parent;
        this.client = client;
        this.hoaDon = hoaDon;
        this.finalAmount = hoaDon.getTongTien();

        setSize(WIDTH_NORMAL, HEIGHT);
        setLocationRelativeTo(parent);
        setResizable(false);
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.WHITE);

        getContentPane().setBackground(Color.WHITE);

        // ── QR side panel (LEFT, hidden initially) ─────────────────
        qrSidePanel = buildQrSidePanel();
        qrSidePanel.setVisible(false);
        add(qrSidePanel, BorderLayout.WEST);

        // ── Right side: header + table + footer ────────────────────
        JPanel rightSide = new JPanel(new BorderLayout());
        rightSide.setBackground(Color.WHITE);
        rightSide.add(createHeader(), BorderLayout.NORTH);
        rightSide.add(createBillTable(), BorderLayout.CENTER);
        rightSide.add(createPaymentFooter(), BorderLayout.SOUTH);
        add(rightSide, BorderLayout.CENTER);
    }

    // ══════════════════════════════════════════════════════════════
    // QR SIDE PANEL (LEFT COLUMN)
    // ══════════════════════════════════════════════════════════════
    private JPanel buildQrSidePanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBackground(QR_BG);
        panel.setPreferredSize(new Dimension(300, HEIGHT));
        panel.setBorder(new CompoundBorder(
                new MatteBorder(0, 0, 0, 1, C_BORDER),
                new EmptyBorder(24, 20, 24, 20)));

        // Title
        JLabel title = new JLabel("Chuyển Khoản QR", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 15));
        title.setForeground(TEXT_DARK);

        // QR Image
        lblQrImage = new JLabel("Đang tải mã QR...", SwingConstants.CENTER);
        lblQrImage.setPreferredSize(new Dimension(260, 260));
        lblQrImage.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblQrImage.setForeground(TEXT_MUTED);
        lblQrImage.setBorder(new LineBorder(QR_BORDER, 1, true));
        lblQrImage.setBackground(Color.WHITE);
        lblQrImage.setOpaque(true);

        JPanel imgWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        imgWrapper.setOpaque(false);
        imgWrapper.add(lblQrImage);

        // Status
        lblQrStatus = new JLabel("<html><center>Quét mã QR để thanh toán,<br>sau đó bấm Xác nhận.</center></html>",
                SwingConstants.CENTER);
        lblQrStatus.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblQrStatus.setForeground(TEXT_MUTED);

        // Bank info hint
        JPanel bankInfo = buildBankInfo();

        JPanel bottom = new JPanel();
        bottom.setLayout(new BoxLayout(bottom, BoxLayout.Y_AXIS));
        bottom.setOpaque(false);
        bottom.add(bankInfo);
        bottom.add(Box.createRigidArea(new Dimension(0, 10)));
        bottom.add(lblQrStatus);

        panel.add(title, BorderLayout.NORTH);
        panel.add(imgWrapper, BorderLayout.CENTER);
        panel.add(bottom, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildBankInfo() {
        JPanel p = new JPanel(new GridLayout(3, 1, 0, 3));
        p.setOpaque(false);
        p.setBorder(new CompoundBorder(
                new LineBorder(QR_BORDER, 1, true),
                new EmptyBorder(10, 12, 10, 12)));
        p.add(mkInfoLabel("Ngân hàng: " + BANK_ID));
        p.add(mkInfoLabel("STK: " + ACCOUNT_NO));
        p.add(mkInfoLabel("Chủ TK: " + ACCOUNT_NAME));
        return p;
    }

    private JLabel mkInfoLabel(String text) {
        JLabel l = new JLabel(text, SwingConstants.CENTER);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        l.setForeground(TEXT_DARK);
        return l;
    }

    // ══════════════════════════════════════════════════════════════
    // HEADER
    // ══════════════════════════════════════════════════════════════
    private JPanel createHeader() {
        JPanel header = new JPanel(new GridLayout(2, 1));
        header.setBackground(Color.WHITE);
        header.setBorder(new EmptyBorder(22, 28, 14, 28));

        JLabel lblTitle = new JLabel("HÓA ĐƠN THANH TOÁN");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(TEXT_DARK);

        JLabel lblSub = new JLabel("Bàn: " + hoaDon.getBan().getMaBan()
                + "  |  Mã HĐ: " + hoaDon.getMaHoaDon());
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSub.setForeground(TEXT_MUTED);

        header.add(lblTitle);
        header.add(lblSub);
        return header;
    }

    // ══════════════════════════════════════════════════════════════
    // BILL TABLE
    // ══════════════════════════════════════════════════════════════
    private JPanel createBillTable() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setBorder(new EmptyBorder(0, 28, 0, 28));

        String[] cols = { "Món uống", "SL", "Đơn giá", "Thành tiền" };
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        for (ChiTietHoaDon ct : hoaDon.getChiTietHoaDons()) {
            model.addRow(new Object[] {
                    ct.getDoUong().getTenDoUong(),
                    ct.getSoLuong(),
                    df.format(ct.getDonGia()),
                    df.format(ct.getSoLuong() * ct.getDonGia())
            });
        }

        JTable table = new JTable(model);
        table.setRowHeight(38);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setShowVerticalLines(false);
        table.setSelectionBackground(new Color(245, 246, 250));
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setBorder(null);

        DefaultTableCellRenderer centerR = new DefaultTableCellRenderer();
        centerR.setHorizontalAlignment(JLabel.CENTER);
        table.getColumnModel().getColumn(1).setCellRenderer(centerR);

        table.getTableHeader().setBackground(Color.WHITE);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.getTableHeader().setForeground(TEXT_MUTED);
        table.getTableHeader().setBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, C_BORDER));

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(240, 240, 245)));
        scroll.getViewport().setBackground(Color.WHITE);
        wrapper.add(scroll, BorderLayout.CENTER);
        return wrapper;
    }

    // ══════════════════════════════════════════════════════════════
    // PAYMENT FOOTER
    // ══════════════════════════════════════════════════════════════
    private JPanel createPaymentFooter() {
        JPanel footer = new JPanel(new BorderLayout(0, 12));
        footer.setBackground(Color.WHITE);
        footer.setBorder(new EmptyBorder(14, 28, 22, 28));

        JPanel topArea = new JPanel();
        topArea.setLayout(new BoxLayout(topArea, BoxLayout.Y_AXIS));
        topArea.setOpaque(false);
        topArea.add(createCalcPanel());
        topArea.add(Box.createRigidArea(new Dimension(0, 12)));
        topArea.add(createMethodSelector());

        footer.add(topArea, BorderLayout.CENTER);
        footer.add(createButtonRow(), BorderLayout.SOUTH);
        return footer;
    }

    private JPanel createCalcPanel() {
        JPanel p = new JPanel(new BorderLayout(0, 0));
        p.setOpaque(false);

        JSeparator sep = new JSeparator();
        sep.setForeground(C_BORDER);
        p.add(sep, BorderLayout.NORTH);

        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setBorder(new EmptyBorder(8, 0, 0, 0));

        JLabel lblFinalLbl = new JLabel("THÀNH TIỀN");
        lblFinalLbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblFinalLbl.setForeground(TEXT_DARK);

        valFinal = new JLabel(df.format(finalAmount) + " đ", SwingConstants.RIGHT);
        valFinal.setFont(new Font("Segoe UI", Font.BOLD, 22));
        valFinal.setForeground(ACCENT_BLUE);

        row.add(lblFinalLbl, BorderLayout.WEST);
        row.add(valFinal, BorderLayout.EAST);
        p.add(row, BorderLayout.CENTER);
        return p;
    }

    private JPanel createMethodSelector() {
        JPanel section = new JPanel(new BorderLayout(0, 6));
        section.setOpaque(false);
        section.setAlignmentX(Component.LEFT_ALIGNMENT);
        section.setMaximumSize(new Dimension(Integer.MAX_VALUE, 72));

        JLabel lbl = new JLabel("Phương thức thanh toán");
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(TEXT_MUTED);
        section.add(lbl, BorderLayout.NORTH);

        JPanel btnGrp = new JPanel(new GridLayout(1, 2, 8, 0));
        btnGrp.setOpaque(false);
        btnCash = buildMethodToggle("Tiền Mặt", true);
        btnQr = buildMethodToggle("Chuyển Khoản", false);
        btnCash.addActionListener(e -> selectMethod("CASH"));
        btnQr.addActionListener(e -> selectMethod("QR"));
        btnGrp.add(btnCash);
        btnGrp.add(btnQr);
        section.add(btnGrp, BorderLayout.CENTER);
        return section;
    }

    private JToggleButton buildMethodToggle(String text, boolean selected) {
        JToggleButton btn = new JToggleButton(text, selected);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(0, 38));
        styleToggle(btn, selected);
        return btn;
    }

    private void styleToggle(JToggleButton btn, boolean active) {
        if (active) {
            btn.setBackground(ACCENT_BLUE);
            btn.setForeground(Color.WHITE);
            btn.setBorder(BorderFactory.createLineBorder(ACCENT_BLUE, 2));
        } else {
            btn.setBackground(Color.WHITE);
            btn.setForeground(TEXT_DARK);
            btn.setBorder(BorderFactory.createLineBorder(C_BORDER, 1));
        }
    }

    private void selectMethod(String method) {
        selectedMethod = method;
        boolean isQr = "QR".equals(method);

        btnCash.setSelected(!isQr);
        btnQr.setSelected(isQr);
        styleToggle(btnCash, !isQr);
        styleToggle(btnQr, isQr);

        if (isQr) {
            qrSidePanel.setVisible(true);
            setSize(WIDTH_QR, HEIGHT);
            loadQrCode();
        } else {
            qrSidePanel.setVisible(false);
            setSize(WIDTH_NORMAL, HEIGHT);
        }
        revalidate();
        repaint();
        setLocationRelativeTo(parentFrame);
    }

    // ── Fetch QR từ VietQR ────────────────────────────────────────
    private void loadQrCode() {
        lblQrImage.setIcon(null);
        lblQrImage.setText("Đang tải mã QR...");
        new SwingWorker<ImageIcon, Void>() {
            @Override
            protected ImageIcon doInBackground() throws Exception {
                String info = "Thanh toan ban " + hoaDon.getBan().getMaBan();
                String urlStr = String.format(
                        "https://img.vietqr.io/image/%s-%s-compact2.png?amount=%d&addInfo=%s&accountName=%s",
                        BANK_ID, ACCOUNT_NO, (long) finalAmount,
                        java.net.URLEncoder.encode(info, "UTF-8"),
                        java.net.URLEncoder.encode(ACCOUNT_NAME, "UTF-8"));
                BufferedImage img = ImageIO.read(new URL(urlStr));
                if (img == null)
                    return null;
                Image scaled = img.getScaledInstance(258, 258, Image.SCALE_SMOOTH);
                return new ImageIcon(scaled);
            }

            @Override
            protected void done() {
                try {
                    ImageIcon icon = get();
                    if (icon != null) {
                        lblQrImage.setIcon(icon);
                        lblQrImage.setText("");
                    } else {
                        lblQrImage.setText("<html><center>Không tải được QR</center></html>");
                    }
                } catch (Exception e) {
                    lblQrImage.setText("<html><center>Không tải được QR</center></html>");
                }
            }
        }.execute();
    }


    // ── Hàng nút bấm ─────────────────────────────────────────────
    private JPanel createButtonRow() {
        JPanel p = new JPanel(new GridLayout(1, 2, 12, 0));
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(8, 0, 0, 0));

        JButton btnCancel = new JButton("Hủy");
        styleButton(btnCancel, Color.WHITE, TEXT_DARK, true);
        btnCancel.addActionListener(e -> {
            dispose();
        });

        JButton btnConfirm = new JButton("XÁC NHẬN THANH TOÁN");
        styleButton(btnConfirm, ACCENT_BLUE, Color.WHITE, false);
        btnConfirm.addActionListener(e -> processPayment());

        p.add(btnCancel);
        p.add(btnConfirm);
        return p;
    }

    // ══════════════════════════════════════════════════════════════
    // PAYMENT PROCESSING
    // ══════════════════════════════════════════════════════════════
    private void processPayment() {
        hoaDon.setTongTien(finalAmount);
        hoaDon.setNgayTao(LocalDate.now());
        hoaDon.setPhuongThucTT("QR".equals(selectedMethod) ? "Chuyển khoản" : "Tiền mặt");
        try {
            Response res = client.sendRequest(new Request(CommandType.PAY_BILL, hoaDon));
            if (res.isSuccess()) {
                // Hiển thị hóa đơn sau khi thanh toán thành công
                ReceiptDialog receipt = new ReceiptDialog(this, client, hoaDon, finalAmount);
                receipt.setVisible(true);
                dispose();
                parentFrame.showTablesView();
            } else {
                JOptionPane.showMessageDialog(this, "Lỗi: " + res.getMessage(),
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void styleButton(JButton btn, Color bg, Color fg, boolean hasBorder) {
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(0, 44));
        if (hasBorder)
            btn.setBorder(new LineBorder(C_BORDER, 1));
        else
            btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }
}