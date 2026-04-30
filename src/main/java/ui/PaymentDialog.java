package ui;

import entity.ChiTietPhieuGoi;
import entity.HoaDon;
import entity.PhieuGoiMon;
import network.Client;
import network.CommandType;
import network.Request;
import network.Response;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.DecimalFormat;
import java.time.LocalDate;

public class PaymentDialog extends JDialog {
    private final Client client;
    private final PhieuGoiMon phieuGoiMon;
    private final StaffDashboard parentFrame;
    private double finalAmount;
    private final DecimalFormat df = new DecimalFormat("#,##0");

    // Colors
    private final Color ACCENT_BLUE = new Color(0, 150, 255);
    private final Color TEXT_DARK = new Color(30, 30, 40);
    private final Color TEXT_MUTED = new Color(120, 120, 130);

    public PaymentDialog(StaffDashboard parent, Client client, PhieuGoiMon phieuGoiMon) {
        super(parent, "Xác nhận thanh toán", true);
        this.parentFrame = parent;
        this.client = client;
        this.phieuGoiMon = phieuGoiMon;
        this.finalAmount = phieuGoiMon.getTongTien();

        setSize(550, 650);
        setLocationRelativeTo(parent);
        setResizable(false);
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.WHITE);

        // 1. HEADER
        add(createHeader(), BorderLayout.NORTH);

        // 2. BILL CONTENT (Table)
        add(createBillTable(), BorderLayout.CENTER);

        // 3. CALCULATION & ACTIONS (South)
        add(createPaymentFooter(), BorderLayout.SOUTH);
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new GridLayout(2, 1));
        header.setBackground(Color.WHITE);
        header.setBorder(new EmptyBorder(25, 30, 15, 30));

        JLabel lblTitle = new JLabel("HÓA ĐƠN THANH TOÁN");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitle.setForeground(TEXT_DARK);

        JLabel lblSubTitle = new JLabel("Bàn: " + phieuGoiMon.getBan().getMaBan() + " | Mã phiếu: " + phieuGoiMon.getMaPhieu());
        lblSubTitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblSubTitle.setForeground(TEXT_MUTED);

        header.add(lblTitle);
        header.add(lblSubTitle);
        return header;
    }

    private JPanel createBillTable() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setBorder(new EmptyBorder(0, 30, 0, 30));

        String[] cols = {"Món uống", "SL", "Đơn giá", "Thành tiền"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        for (ChiTietPhieuGoi ct : phieuGoiMon.getChiTietPhieuGois()) {
            model.addRow(new Object[]{
                    ct.getDoUong().getTenDoUong(),
                    ct.getSoLuong(),
                    df.format(ct.getDonGia()),
                    df.format(ct.getSoLuong() * ct.getDonGia())
            });
        }

        JTable table = new JTable(model);
        table.setRowHeight(40);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setShowVerticalLines(false);
        table.setSelectionBackground(new Color(245, 246, 250));
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setBorder(null);

        // Căn giữa cột Số lượng
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        table.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);

        // Header table
        table.getTableHeader().setBackground(Color.WHITE);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.getTableHeader().setForeground(TEXT_MUTED);
        table.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(230, 230, 235)));

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(240, 240, 245)));
        scroll.getViewport().setBackground(Color.WHITE);

        wrapper.add(scroll, BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel createPaymentFooter() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(Color.WHITE);
        footer.setBorder(new EmptyBorder(20, 30, 30, 30));

        // Area for calculations
        JPanel calcPanel = new JPanel(new GridBagLayout());
        calcPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 0, 5, 0);

        // Tổng cộng
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 1;
        JLabel lblSub = new JLabel("Tổng cộng");
        lblSub.setForeground(TEXT_MUTED);
        calcPanel.add(lblSub, gbc);

        gbc.gridx = 1; gbc.weightx = 0;
        JLabel valSub = new JLabel(df.format(phieuGoiMon.getTongTien()) + " đ", SwingConstants.RIGHT);
        valSub.setFont(new Font("Segoe UI", Font.BOLD, 14));
        calcPanel.add(valSub, gbc);

        // Giảm giá
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 1;
        JLabel lblDis = new JLabel("Giảm giá (%)");
        lblDis.setForeground(TEXT_MUTED);
        calcPanel.add(lblDis, gbc);

        gbc.gridx = 1; gbc.weightx = 0;
        JSpinner spnDiscount = new JSpinner(new SpinnerNumberModel(0, 0, 100, 1));
        spnDiscount.setPreferredSize(new Dimension(80, 25));
        calcPanel.add(spnDiscount, gbc);

        // Thành tiền (Final)
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 1;
        gbc.insets = new Insets(15, 0, 15, 0);
        JLabel lblFinal = new JLabel("THÀNH TIỀN");
        lblFinal.setFont(new Font("Segoe UI", Font.BOLD, 15));
        calcPanel.add(lblFinal, gbc);

        gbc.gridx = 1;
        JLabel valFinal = new JLabel(df.format(finalAmount) + " đ", SwingConstants.RIGHT);
        valFinal.setFont(new Font("Segoe UI", Font.BOLD, 22));
        valFinal.setForeground(ACCENT_BLUE);
        calcPanel.add(valFinal, gbc);

        // Event xử lý giảm giá
        spnDiscount.addChangeListener(e -> {
            int discountPercent = (int) spnDiscount.getValue();
            finalAmount = phieuGoiMon.getTongTien() * (100 - discountPercent) / 100.0;
            valFinal.setText(df.format(finalAmount) + " đ");
        });

        // Buttons
        JPanel btnPanel = new JPanel(new GridLayout(1, 2, 15, 0));
        btnPanel.setOpaque(false);

        JButton btnCancel = new JButton("Hủy");
        styleButton(btnCancel, Color.WHITE, TEXT_DARK, true);
        btnCancel.addActionListener(e -> dispose());

        JButton btnConfirm = new JButton("XÁC NHẬN THANH TOÁN");
        styleButton(btnConfirm, ACCENT_BLUE, Color.WHITE, false);
        btnConfirm.addActionListener(e -> processPayment());

        btnPanel.add(btnCancel);
        btnPanel.add(btnConfirm);

        footer.add(calcPanel, BorderLayout.NORTH);
        footer.add(btnPanel, BorderLayout.SOUTH);
        return footer;
    }

    private void styleButton(JButton btn, Color bg, Color fg, boolean hasBorder) {
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(0, 45));
        if (hasBorder) {
            btn.setBorder(new LineBorder(new Color(230, 230, 235), 1));
        } else {
            btn.setBorderPainted(false);
        }
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private void processPayment() {
        HoaDon hoaDon = new HoaDon();
        hoaDon.setPhieuGoiMon(phieuGoiMon);
        hoaDon.setTongTien(finalAmount);
        hoaDon.setNgayTao(LocalDate.now());
        hoaDon.setPhuongThucTT("Tiền mặt");

        try {
            Response res = client.sendRequest(new Request(CommandType.PAY_BILL, hoaDon));
            if (res.isSuccess()) {
                JOptionPane.showMessageDialog(this, "Thanh toán thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                dispose();
                parentFrame.showTablesView();
            } else {
                JOptionPane.showMessageDialog(this, "Lỗi: " + res.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}