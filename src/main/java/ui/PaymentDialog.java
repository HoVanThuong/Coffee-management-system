package ui;

import entity.ChiTietPhieuGoi;
import entity.HoaDon;
import entity.PhieuGoiMon;
import network.Client;
import network.CommandType;
import network.Request;
import network.Response;
import org.mariadb.jdbc.plugin.codec.LocalDateCodec;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.Locale;

public class PaymentDialog extends JDialog {
    private Client client;
    private PhieuGoiMon phieuGoiMon;
    private StaffDashboard parentFrame;
    private double finalAmount;

    public PaymentDialog(StaffDashboard parent, Client client, PhieuGoiMon phieuGoiMon) {
        super(parent, "Thanh toán cho Bàn " + phieuGoiMon.getBan().getMaBan(), true);
        this.parentFrame = parent;
        this.client = client;
        this.phieuGoiMon = phieuGoiMon;
        this.finalAmount = phieuGoiMon.getTongTien();

        setSize(600, 500);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));

        // Table to show order details
        String[] columnNames = {"Tên món", "Số lượng", "Đơn giá", "Thành tiền"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0);
        JTable table = new JTable(model);

        NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

        for (ChiTietPhieuGoi ct : phieuGoiMon.getChiTietPhieuGois()) {
            double lineTotal = ct.getSoLuong() * ct.getDonGia();
            model.addRow(new Object[]{
                    ct.getDoUong().getTenDoUong(),
                    ct.getSoLuong(),
                    currencyFormatter.format(ct.getDonGia()),
                    currencyFormatter.format(lineTotal)
            });
        }

        add(new JScrollPane(table), BorderLayout.CENTER);

        // Panel for total, discount, and payment
        JPanel southPanel = new JPanel(new GridLayout(4, 2, 5, 5));
        southPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel lblSubTotal = new JLabel("Tổng cộng:");
        JLabel valSubTotal = new JLabel(currencyFormatter.format(phieuGoiMon.getTongTien()));

        JLabel lblDiscount = new JLabel("Giảm giá (%):");
        JSpinner spnDiscount = new JSpinner(new SpinnerNumberModel(0, 0, 100, 1));

        JLabel lblFinalAmount = new JLabel("Thành tiền:");
        JLabel valFinalAmount = new JLabel(currencyFormatter.format(finalAmount));
        valFinalAmount.setFont(new Font("Arial", Font.BOLD, 16));
        valFinalAmount.setForeground(Color.RED);

        spnDiscount.addChangeListener(e -> {
            int discountPercent = (int) spnDiscount.getValue();
            finalAmount = phieuGoiMon.getTongTien() * (100 - discountPercent) / 100.0;
            valFinalAmount.setText(currencyFormatter.format(finalAmount));
        });

        southPanel.add(lblSubTotal);
        southPanel.add(valSubTotal);
        southPanel.add(lblDiscount);
        southPanel.add(spnDiscount);
        southPanel.add(lblFinalAmount);
        southPanel.add(valFinalAmount);

        JButton btnPay = new JButton("Xác nhận Thanh toán");
        btnPay.addActionListener(e -> {
            HoaDon hoaDon = new HoaDon();
            hoaDon.setPhieuGoiMon(phieuGoiMon);
            hoaDon.setTongTien(finalAmount);
            hoaDon.setNgayTao(LocalDate.now());
            hoaDon.setPhuongThucTT("Tiền mặt"); // Giả sử

            Response res = client.sendRequest(new Request(CommandType.PAY_BILL, hoaDon));
            if (res.isSuccess()) {
                JOptionPane.showMessageDialog(this, "Thanh toán thành công!");
                dispose();
                parentFrame.showTablesView(); // Refresh the tables view
            } else {
                JOptionPane.showMessageDialog(this, "Lỗi thanh toán: " + res.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });

        JButton btnCancel = new JButton("Hủy");
        btnCancel.addActionListener(e -> dispose());
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(btnPay);
        buttonPanel.add(btnCancel);
        
        southPanel.add(new JLabel()); // placeholder
        southPanel.add(buttonPanel);

        add(southPanel, BorderLayout.SOUTH);
    }
}
