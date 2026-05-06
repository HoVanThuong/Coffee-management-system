package ui;

import entity.ChiTietHoaDon;
import entity.HoaDon;
import entity.NhanVien;
import network.Client;
import network.CommandType;
import network.Request;
import network.Response;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.print.*;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ReceiptDialog extends JDialog {

    private static final String SHOP_NAME = "ZENTA COFFEE";
    private static final String SHOP_ADDRESS = "360 Nguyễn Văn Nghi, Phường 7, Gò Vấp, TP.HCM";
    private static final String SHOP_PHONE = "0909 123 456";

    private final Client client;
    private final HoaDon hoaDon;
    private final double finalAmount;
    private String employeeName = "—";
    private final DecimalFormat df = new DecimalFormat("#,##0");

    private static final Color C_BG = new Color(245, 246, 250);
    private static final Color C_WHITE = Color.WHITE;
    private static final Color C_BORDER = new Color(220, 220, 230);
    private static final Color C_DARK = new Color(20, 20, 30);
    private static final Color C_MUTED = new Color(110, 110, 125);
    private static final Color C_ACCENT = new Color(0, 140, 255);
    private static final Color C_SUCCESS = new Color(39, 174, 96);
    private static final Color C_DASH = new Color(200, 200, 210);

    public ReceiptDialog(Window parent, Client client, HoaDon hoaDon, double finalAmount) {
        super(parent, "Hóa Đơn Thanh Toán", ModalityType.APPLICATION_MODAL);
        this.client = client;
        this.hoaDon = hoaDon;
        this.finalAmount = finalAmount;
        fetchEmployeeName();
        build();
    }

    private void build() {
        setSize(420, 640);
        setLocationRelativeTo(getParent());
        setResizable(false);
        setLayout(new BorderLayout());
        getContentPane().setBackground(C_BG);

        JScrollPane scroll = new JScrollPane(buildReceiptCard());
        scroll.setBorder(null);
        scroll.setBackground(C_BG);
        scroll.getViewport().setBackground(C_BG);
        scroll.getVerticalScrollBar().setUnitIncrement(12);
        add(scroll, BorderLayout.CENTER);
        add(buildButtonBar(), BorderLayout.SOUTH);
    }

    private JPanel buildReceiptCard() {

        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setBackground(C_BG);
        wrapper.setBorder(new EmptyBorder(16, 20, 16, 20));

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(C_WHITE);
        card.setBorder(new CompoundBorder(
                new LineBorder(C_BORDER, 1, true),
                new EmptyBorder(22, 22, 22, 22)));

        addSection(card, buildShopHeader());
        addDash(card);
        addSection(card, buildInvoiceInfo());
        addDash(card);
        addSection(card, buildItemsSection());
        addDash(card);
        addSection(card, buildTotalsSection());
        addDash(card);
        addSection(card, buildFooter());

        wrapper.add(card);
        return wrapper;
    }

    private void addSection(JPanel card, JPanel section) {
        card.add(Box.createRigidArea(new Dimension(0, 10)));
        card.add(section);
        card.add(Box.createRigidArea(new Dimension(0, 10)));
    }

    private void addDash(JPanel card) {
        card.add(new DashedLine());
    }

    private JPanel buildShopHeader() {
        JPanel p = new JPanel(new GridLayout(0, 1, 0, 2));
        p.setOpaque(false);

        JLabel lbName = centeredLabel(SHOP_NAME, new Font("Segoe UI", Font.BOLD, 17), C_ACCENT);
        JLabel lbAddr = centeredLabel(SHOP_ADDRESS, new Font("Segoe UI", Font.PLAIN, 11), C_MUTED);
        JLabel lbTel = centeredLabel("Tel: " + SHOP_PHONE, new Font("Segoe UI", Font.PLAIN, 11), C_MUTED);
        JLabel lbType = centeredLabel("HÓA ĐƠN THANH TOÁN", new Font("Segoe UI", Font.BOLD, 12), C_DARK);

        p.add(lbName);
        p.add(lbAddr);
        p.add(lbTel);
        p.add(Box.createVerticalStrut(4));
        p.add(lbType);
        return p;
    }

    private JPanel buildInvoiceInfo() {
        JPanel p = new JPanel(new GridLayout(0, 2, 6, 5));
        p.setOpaque(false);

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String ngay = hoaDon.getNgayTao() != null ? hoaDon.getNgayTao().format(fmt) : "—";

        row(p, "Mã hóa đơn:", hoaDon.getMaHoaDon());
        row(p, "Bàn:", hoaDon.getBan().getMaBan());
        row(p, "Ngày:", ngay);
        row(p, "Thu ngân:", safeGetNhanVienName());
        row(p, "Phương thức:", hoaDon.getPhuongThucTT() != null
                ? hoaDon.getPhuongThucTT()
                : "Tiền mặt");
        return p;
    }

    private JPanel buildItemsSection() {
        JPanel p = new JPanel(new GridLayout(0, 4, 4, 5));
        p.setOpaque(false);

        // Header row
        headerCell(p, "Món");
        headerCell(p, "SL");
        headerCell(p, "Đơn giá");
        headerCell(p, "T.Tiền");

        for (ChiTietHoaDon ct : hoaDon.getChiTietHoaDons()) {
            String name = ct.getDoUong().getTenDoUong();
            if (name.length() > 16)
                name = name.substring(0, 14) + "..";
            double lineTotal = ct.getSoLuong() * ct.getDonGia();

            dataCell(p, name, SwingConstants.LEFT);
            dataCell(p, String.valueOf(ct.getSoLuong()), SwingConstants.CENTER);
            dataCell(p, df.format(ct.getDonGia()), SwingConstants.RIGHT);
            dataCell(p, df.format(lineTotal), SwingConstants.RIGHT);
        }
        return p;
    }

    private JPanel buildTotalsSection() {
        JPanel p = new JPanel(new GridLayout(0, 2, 4, 5));
        p.setOpaque(false);
        totalRow(p, "TỔNG CỘNG:", df.format(finalAmount) + " đ", true);
        return p;
    }

    private JPanel buildFooter() {
        JPanel p = new JPanel(new GridLayout(0, 1, 0, 3));
        p.setOpaque(false);
        p.add(centeredLabel("Cảm ơn quý khách!", new Font("Segoe UI", Font.BOLD, 13), C_SUCCESS));
        p.add(centeredLabel("Hẹn gặp lại tại " + SHOP_NAME,
                new Font("Segoe UI", Font.ITALIC, 11), C_MUTED));
        return p;
    }

    private void row(JPanel p, String label, String value) {
        JLabel l = new JLabel(label);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        l.setForeground(C_MUTED);

        JLabel v = new JLabel(value, SwingConstants.RIGHT);
        v.setFont(new Font("Segoe UI", Font.BOLD, 12));
        v.setForeground(C_DARK);

        p.add(l);
        p.add(v);
    }

    private void headerCell(JPanel p, String text) {
        JLabel l = new JLabel(text, SwingConstants.CENTER);
        l.setFont(new Font("Segoe UI", Font.BOLD, 11));
        l.setForeground(C_MUTED);
        p.add(l);
    }

    private void dataCell(JPanel p, String text, int align) {
        JLabel l = new JLabel(text, align);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        l.setForeground(C_DARK);
        p.add(l);
    }

    private void totalRow(JPanel p, String label, String value, boolean bold) {
        Font f = bold
                ? new Font("Segoe UI", Font.BOLD, 15)
                : new Font("Segoe UI", Font.PLAIN, 12);
        Color fg = bold ? C_ACCENT : C_DARK;

        JLabel l = new JLabel(label);
        l.setFont(f);
        l.setForeground(bold ? C_DARK : C_MUTED);

        JLabel v = new JLabel(value, SwingConstants.RIGHT);
        v.setFont(f);
        v.setForeground(fg);

        p.add(l);
        p.add(v);
    }

    private JLabel centeredLabel(String text, Font font, Color fg) {
        JLabel l = new JLabel(text, SwingConstants.CENTER);
        l.setFont(font);
        l.setForeground(fg);
        return l;
    }

    private class DashedLine extends JComponent {
        DashedLine() {
            setPreferredSize(new Dimension(100, 10));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 10));
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setColor(C_DASH);
            float[] dash = { 5f, 4f };
            g2.setStroke(new BasicStroke(1f, BasicStroke.CAP_BUTT,
                    BasicStroke.JOIN_MITER, 10f, dash, 0f));
            g2.drawLine(0, 5, getWidth(), 5);
            g2.dispose();
        }
    }

    private JPanel buildButtonBar() {
        JPanel bar = new JPanel(new GridLayout(1, 2, 12, 0));
        bar.setBackground(C_WHITE);
        bar.setBorder(new CompoundBorder(
                new MatteBorder(1, 0, 0, 0, C_BORDER),
                new EmptyBorder(12, 24, 12, 24)));

        JButton btnPrint = mkButton("In Hóa Đơn", C_ACCENT);
        btnPrint.addActionListener(e -> printReceipt());

        JButton btnClose = mkButton("Đóng", new Color(100, 110, 130));
        btnClose.addActionListener(e -> dispose());

        bar.add(btnPrint);
        bar.add(btnClose);
        return bar;
    }

    private void printReceipt() {
        JPanel printable = buildReceiptCard();
        printable.setSize(400, 800);
        printable.doLayout();

        PrinterJob job = PrinterJob.getPrinterJob();
        job.setPrintable((g, pageFormat, pageIndex) -> {
            if (pageIndex > 0)
                return Printable.NO_SUCH_PAGE;
            Graphics2D g2 = (Graphics2D) g;
            g2.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
            double sx = pageFormat.getImageableWidth() / printable.getWidth();
            double sy = pageFormat.getImageableHeight() / printable.getHeight();
            g2.scale(Math.min(sx, sy), Math.min(sx, sy));
            printable.printAll(g);
            return Printable.PAGE_EXISTS;
        });

        if (job.printDialog()) {
            try {
                job.print();
                JOptionPane.showMessageDialog(this, "In thành công!", "OK",
                        JOptionPane.INFORMATION_MESSAGE);
            } catch (PrinterException ex) {
                JOptionPane.showMessageDialog(this, "Lỗi in: " + ex.getMessage(),
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void fetchEmployeeName() {
        if (hoaDon.getNhanVien() == null)
            return;
        String id = hoaDon.getNhanVien().getMaNhanVien();
        try {
            Response res = client.sendRequest(new Request(CommandType.GET_EMPLOYEES, null));
            if (res.isSuccess()) {
                List<NhanVien> list = (List<NhanVien>) res.getData();
                for (NhanVien nv : list) {
                    if (nv.getMaNhanVien().equals(id)) {
                        employeeName = nv.getHoTen();
                        break;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String safeGetNhanVienName() {
        return employeeName;
    }

    private JButton mkButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(0, 42));
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
