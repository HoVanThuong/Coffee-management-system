package ui;

import entity.*;
import network.Client;
import network.CommandType;
import network.Request;
import network.Response;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class OrderPanel extends JPanel {
    private final Client client;
    private final TaiKhoan currentUser;
    private final Ban ban;
    private final StaffDashboard parentFrame;
    private PhieuGoiMon activeOrder;
    private List<ChiTietPhieuGoi> currentCart = new ArrayList<>();

    private DefaultTableModel menuModel;
    private JTable menuTable;
    private DefaultTableModel cartModel;
    private JTable cartTable;
    private JLabel lblTotal;
    private JButton btnPay;
    private final DecimalFormat df = new DecimalFormat("#,##0 VND");

    public OrderPanel(Client client, TaiKhoan currentUser, Ban ban, StaffDashboard parent) {
        this.client = client;
        this.currentUser = currentUser;
        this.ban = ban;
        this.parentFrame = parent;

        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(10, 10, 10, 10));

        // Title
        JLabel lblTitle = new JLabel("Order cho Bàn " + ban.getMaBan(), SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        add(lblTitle, BorderLayout.NORTH);

        // Main content split pane
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, createMenuPanel(), createCartPanel());
        splitPane.setResizeWeight(0.6);
        add(splitPane, BorderLayout.CENTER);

        loadData();
    }

    private void loadData() {
        loadMenu();
        loadActiveOrder();
    }

    private void loadMenu() {
        try {
            Response response = client.sendRequest(new Request(CommandType.GET_MENU, null));
            if (response.isSuccess()) {
                List<DoUong> menuItems = (List<DoUong>) response.getData();
                menuModel.setRowCount(0);
                for (DoUong item : menuItems) {
                    menuModel.addRow(new Object[]{item.getMaDoUong(), item.getTenDoUong(), df.format(Double.parseDouble(item.getGiaTien())), item});
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi khi tải thực đơn: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadActiveOrder() {
        try {
            Response response = client.sendRequest(new Request(CommandType.GET_ORDER, ban.getMaBan()));
            if (response.isSuccess() && response.getData() != null) {
                activeOrder = (PhieuGoiMon) response.getData();
                currentCart = activeOrder.getChiTietPhieuGois();
                updateCartTable();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private JPanel createMenuPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Thực Đơn"));

        menuModel = new DefaultTableModel(new String[]{"Mã", "Tên Món", "Giá", ""}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 3) return DoUong.class;
                return String.class;
            }
        };
        menuTable = new JTable(menuModel);
        menuTable.setRowHeight(30);
        menuTable.getColumnModel().getColumn(3).setMinWidth(0);
        menuTable.getColumnModel().getColumn(3).setMaxWidth(0);
        menuTable.getColumnModel().getColumn(3).setWidth(0);

        menuTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                int row = menuTable.rowAtPoint(evt.getPoint());
                if (row >= 0) {
                    DoUong selectedDrink = (DoUong) menuModel.getValueAt(row, 3);
                    addToCart(selectedDrink);
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(menuTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createCartPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createTitledBorder("Món order"));

        cartModel = new DefaultTableModel(new String[]{"Tên Món", "SL", "Đơn Giá", "Thành Tiền"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 1; // Allow editing quantity
            }
        };
        cartTable = new JTable(cartModel);
        cartTable.setRowHeight(30);

        cartTable.getModel().addTableModelListener(e -> {
            if (e.getType() == javax.swing.event.TableModelEvent.UPDATE) {
                int row = e.getFirstRow();
                int col = e.getColumn();
                if (col == 1) { // Quantity column
                    try {
                        int newQuantity = Integer.parseInt(cartModel.getValueAt(row, 1).toString());
                        if (newQuantity <= 0) {
                            currentCart.remove(row);
                        } else {
                            currentCart.get(row).setSoLuong(newQuantity);
                        }
                        updateCartTable();
                    } catch (NumberFormatException ex) {
                        // Handle invalid input if necessary
                        JOptionPane.showMessageDialog(this, "Số lượng không hợp lệ!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                        updateCartTable(); // Revert to old value
                    }
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(cartTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        // South panel for total and buttons
        JPanel southPanel = new JPanel(new BorderLayout());
        lblTotal = new JLabel("Tổng cộng: 0 VND");
        lblTotal.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTotal.setBorder(new EmptyBorder(10, 10, 10, 10));
        southPanel.add(lblTotal, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton btnConfirm = new JButton("Xác nhận Order");
        btnConfirm.setBackground(new Color(21, 139, 2));
        btnConfirm.setForeground(Color.WHITE);
        btnConfirm.addActionListener(e -> confirmOrder());
        buttonPanel.add(btnConfirm);

        // NÚT THANH TOÁN MỚI
        btnPay = new JButton("Thanh toán");
        btnPay.setBackground(new Color(0, 123, 255));
        btnPay.setForeground(Color.WHITE);
        btnPay.addActionListener(e -> {
            if (activeOrder != null) {
                PaymentDialog paymentDialog = new PaymentDialog(parentFrame, client, activeOrder);
                paymentDialog.setVisible(true);
                // Sau khi dialog đóng, refresh lại view chính
                parentFrame.showTablesView();
            } else {
                JOptionPane.showMessageDialog(this, "Chưa có order để thanh toán. Vui lòng xác nhận order trước.", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        // Chỉ hiển thị nút thanh toán nếu đã có order
        btnPay.setVisible(activeOrder != null);
        buttonPanel.add(btnPay);

        southPanel.add(buttonPanel, BorderLayout.CENTER);
        panel.add(southPanel, BorderLayout.SOUTH);

        return panel;
    }

    private void addToCart(DoUong doUong) {
        for (ChiTietPhieuGoi ct : currentCart) {
            if (ct.getDoUong().getMaDoUong().equals(doUong.getMaDoUong())) {
                ct.setSoLuong(ct.getSoLuong() + 1);
                updateCartTable();
                return;
            }
        }
        ChiTietPhieuGoi newCt = new ChiTietPhieuGoi();
        newCt.setDoUong(doUong);
        newCt.setSoLuong(1);
        try {
            newCt.setDonGia(Double.parseDouble(doUong.getGiaTien()));
        } catch (NumberFormatException e) {
            newCt.setDonGia(0.0);
        }
        currentCart.add(newCt);
        updateCartTable();
    }

    private void updateCartTable() {
        cartModel.setRowCount(0);
        double total = 0;
        for (ChiTietPhieuGoi ct : currentCart) {
            double lineTotal = ct.getSoLuong() * ct.getDonGia();
            cartModel.addRow(new Object[]{
                    ct.getDoUong().getTenDoUong(),
                    ct.getSoLuong(),
                    df.format(ct.getDonGia()),
                    df.format(lineTotal)
            });
            total += lineTotal;
        }
        lblTotal.setText("Tổng cộng: " + df.format(total));

        if (btnPay != null) {
            btnPay.setVisible(activeOrder != null);
        }
    }

    private void confirmOrder() {
        if (currentCart.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Giỏ hàng trống!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        double total = 0;
        for (ChiTietPhieuGoi ct : currentCart) {
            total += ct.getSoLuong() * ct.getDonGia();
        }

        if (activeOrder == null) {
            activeOrder = new PhieuGoiMon();
            activeOrder.setMaPhieu("PG" + System.currentTimeMillis());
            activeOrder.setBan(ban);
            activeOrder.setNhanVien(currentUser.getNhanVien());
            activeOrder.setTrangThai("Chưa thanh toán");
        }
        activeOrder.setChiTietPhieuGois(currentCart);
        activeOrder.setTongTien(total);
        activeOrder.setNgayTao(java.time.LocalDate.now().toString());


        // Lọc ra những món chưa có ID (món mới thêm)
        List<ChiTietPhieuGoi> newItems = new ArrayList<>();
        for (ChiTietPhieuGoi ct : currentCart) {
            if (ct.getId() == null) {
                newItems.add(ct);
            }
        }

        if (newItems.isEmpty() && activeOrder.getMaPhieu() != null) {
             JOptionPane.showMessageDialog(this, "Không có món mới nào để thêm hoặc cập nhật!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
             return;
        }


        try {
            Response res = client.sendRequest(new Request(CommandType.ORDER_FOOD, new Object[]{activeOrder, newItems}));
            if (res.isSuccess()) {
                JOptionPane.showMessageDialog(this, "Đã xác nhận order thành công!");
                parentFrame.showTablesView(); // Quay lại màn hình bàn
            } else {
                JOptionPane.showMessageDialog(this, "Lỗi khi xác nhận order: " + res.getMessage(), "Lỗi Server", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi kết nối: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
}
