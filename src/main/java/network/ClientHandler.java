package network;

import dto.*;
import mapper.Mapper;
import service.*;
import service.impl.*;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.time.LocalDate;
import java.util.List;

public class ClientHandler implements Runnable {
    private Socket socket;
    private String currentAccountId;

    private final HoaDonService hoaDonService = new HoaDonServiceImpl();
    private final BanService banService = new BanServiceImpl(new dao.impl.BanDaoImpl());
    private final DoUongService doUongService = new DoUongServiceImpl();
    private final NhanVienService nhanVienService = new NhanVienServiceImpl();
    private final TaiKhoanService taiKhoanService = new TaiKhoanServiceImpl();
    private final ThongKeService thongKeService = new ThongKeServiceImpl();

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try (
                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(socket.getInputStream())
        ) {
            while (true) {
                Request req = (Request) in.readObject();
                Response res = processRequest(req);
                out.writeObject(res);
                out.flush();
            }
        } catch (Exception e) {
            System.out.println("Client disconnected: " + e.getMessage());
        } finally {
            if (currentAccountId != null) {
                System.out.println("Cleaning up session for account: " + currentAccountId);
                taiKhoanService.updateStatus(currentAccountId, "Offline");
            }
            try {
                if (socket != null && !socket.isClosed()) socket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private Response processRequest(Request req) {
        Response response = new Response();

        try {
            switch (req.getCommandType()) {
                case LOGIN:
                    try {
                        TaiKhoanDTO loginDto = Mapper.map(req.getData(), TaiKhoanDTO.class);
                        System.out.println("Server: Processing login for: " + loginDto.getTenDangNhap());
                        TaiKhoanDTO result = taiKhoanService.login(loginDto.getTenDangNhap(), loginDto.getMatKhau());
                        
                        if (result != null) {
                            if ("ALREADY_LOGGED_IN".equals(result.getMaTaiKhoan())) {
                                System.out.println("Server: Login blocked - already logged in: " + loginDto.getTenDangNhap());
                                response.setSuccess(false);
                                response.setMessage("Tài khoản này đang được đăng nhập ở một nơi khác!");
                            } else {
                                System.out.println("Server: Login success for " + loginDto.getTenDangNhap());
                                this.currentAccountId = result.getMaTaiKhoan();
                                response.setSuccess(true);
                                response.setData(result);
                            }
                        } else {
                            System.out.println("Server: Login failed for " + loginDto.getTenDangNhap());
                            response.setSuccess(false);
                            response.setMessage("Sai tên đăng nhập hoặc mật khẩu, hoặc tài khoản đã bị vô hiệu hóa!");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        response.setSuccess(false);
                        response.setMessage("Lỗi đăng nhập: " + e.getMessage());
                    }
                    break;

                case GET_TABLES:
                    response.setData(banService.findAll());
                    response.setSuccess(true);
                    break;

                case MANAGE_TABLE_ADD:
                    try {
                        BanDTO tableDto = Mapper.map(req.getData(), BanDTO.class);
                        boolean added = banService.addBan(tableDto);
                        response.setSuccess(added);
                        response.setMessage(added ? "Thêm bàn thành công!" : "Bàn đã tồn tại!");
                    } catch (Exception e) {
                        response.setSuccess(false);
                        response.setMessage("Lỗi Server: " + e.getMessage());
                    }
                    break;

                case MANAGE_TABLE_UPDATE:
                    try {
                        BanDTO tableDto = Mapper.map(req.getData(), BanDTO.class);
                        boolean updated = banService.updateBan(tableDto);
                        response.setSuccess(updated);
                        response.setMessage(updated ? "Cập nhật thành công!" : "Lỗi cập nhật!");
                    } catch (Exception e) {
                        response.setSuccess(false);
                        response.setMessage("Lỗi Server: " + e.getMessage());
                    }
                    break;

                case MANAGE_TABLE_DELETE:
                    String idDel = Mapper.map(req.getData(), String.class);
                    boolean delRes = banService.deleteBan(idDel);
                    response.setSuccess(delRes);
                    response.setMessage(delRes ? "Xóa thành công" : "Xóa thất bại");
                    break;

                case GET_MENU:
                    response.setData(doUongService.getAllDrinks());
                    response.setSuccess(true);
                    break;

                case ORDER_FOOD:
                    try {
                        Object[] orderData = (Object[]) req.getData();
                        HoaDonDTO phieuDto = Mapper.map(orderData[0], HoaDonDTO.class);
                        @SuppressWarnings("unchecked")
                        List<Object> cartData = (List<Object>) orderData[1];
                        List<ChiTietHoaDonDTO> cartDto = cartData.stream()
                                .map(c -> Mapper.map(c, ChiTietHoaDonDTO.class))
                                .toList();

                        boolean orderSuccess = hoaDonService.handleOrderFood(phieuDto, cartDto);
                        response.setSuccess(orderSuccess);
                        response.setMessage(orderSuccess ? "Cập nhật hóa đơn thành công!" : "Lỗi cập nhật hóa đơn!");
                    } catch (Exception e) {
                        e.printStackTrace();
                        response.setSuccess(false);
                        response.setMessage("Lỗi Order: " + e.getMessage());
                    }
                    break;

                case GET_ORDER:
                    String maBan = Mapper.map(req.getData(), String.class);
                    HoaDonDTO activeOrder = hoaDonService.getActiveOrderForTable(maBan);
                    if (activeOrder != null) {
                        response.setSuccess(true);
                        response.setData(activeOrder);
                    } else {
                        response.setSuccess(false);
                        response.setMessage("Bàn này không có hóa đơn nào chưa thanh toán.");
                    }
                    break;

                case PAY_BILL:
                    try {
                        HoaDonDTO hoaDonDto = Mapper.map(req.getData(), HoaDonDTO.class);
                        boolean paySuccess = hoaDonService.handlePayment(hoaDonDto);
                        response.setSuccess(paySuccess);
                        response.setMessage(paySuccess ? "Thanh toán thành công!" : "Lỗi thanh toán!");
                    } catch (Exception e) {
                        response.setSuccess(false);
                        response.setMessage("Lỗi thanh toán: " + e.getMessage());
                    }
                    break;

                case GET_EMPLOYEES:
                    boolean includeFired = req.getData() != null && Mapper.map(req.getData(), Boolean.class);
                    response.setData(nhanVienService.getAllEmployees(includeFired));
                    response.setSuccess(true);
                    break;

                case MANAGE_EMPLOYEE_ADD:
                    try {
                        Object[] data = (Object[]) req.getData();
                        NhanVienDTO nvDto = Mapper.map(data[0], NhanVienDTO.class);
                        TaiKhoanDTO tkDto = Mapper.map(data[1], TaiKhoanDTO.class);
                        boolean ok = nhanVienService.addEmployeeWithAccount(nvDto, tkDto);
                        response.setSuccess(ok);
                        response.setMessage(ok ? "Thêm thành công!" : "Mã NV hoặc Username bị trùng!");
                    } catch (Exception e) {
                        response.setSuccess(false);
                        response.setMessage("Lỗi Server: " + e.getMessage());
                    }
                    break;

                case MANAGE_EMPLOYEE_UPDATE:
                    try {
                        Object[] data = (Object[]) req.getData();
                        NhanVienDTO nvDto = Mapper.map(data[0], NhanVienDTO.class);
                        TaiKhoanDTO tkDto = Mapper.map(data[1], TaiKhoanDTO.class);
                        boolean ok = nhanVienService.updateEmployeeWithAccount(nvDto, tkDto);
                        response.setSuccess(ok);
                        response.setMessage(ok ? "Cập nhật thành công!" : "Lỗi cập nhật!");
                    } catch (Exception e) {
                        response.setSuccess(false);
                        response.setMessage("Lỗi Server: " + e.getMessage());
                    }
                    break;

                case MANAGE_EMPLOYEE_DELETE:
                    String maNhanVienToFire = Mapper.map(req.getData(), String.class);
                    boolean fireOk = nhanVienService.terminateEmployee(maNhanVienToFire);
                    response.setSuccess(fireOk);
                    response.setMessage(fireOk ? "Đã cho nhân viên thôi việc và vô hiệu hóa tài khoản!" : "Lỗi khi xử lý thôi việc!");
                    break;

                case MANAGE_MENU_ADD:
                    try {
                        DoUongDTO dDto = Mapper.map(req.getData(), DoUongDTO.class);
                        boolean ok = doUongService.addDrink(dDto);
                        response.setSuccess(ok);
                        response.setMessage(ok ? "Thêm món thành công!" : "Mã món đã tồn tại!");
                    } catch (Exception e) {
                        response.setSuccess(false);
                        response.setMessage(e.getMessage());
                    }
                    break;

                case MANAGE_MENU_UPDATE:
                    try {
                        DoUongDTO dDto = Mapper.map(req.getData(), DoUongDTO.class);
                        boolean ok = doUongService.updateDrink(dDto);
                        response.setSuccess(ok);
                        response.setMessage(ok ? "Cập nhật thành công!" : "Lỗi!");
                    } catch (Exception e) {
                        response.setSuccess(false);
                        response.setMessage(e.getMessage());
                    }
                    break;

                case MANAGE_MENU_DELETE:
                    String maMonToDelete = Mapper.map(req.getData(), String.class);
                    boolean delMenuSuccess = doUongService.deleteDrink(maMonToDelete);
                    response.setSuccess(delMenuSuccess);
                    response.setMessage(delMenuSuccess ? "Xóa món thành công!" : "Xóa món thất bại!");
                    break;

                case GET_INVOICES:
                    response.setData(hoaDonService.getAllInvoices());
                    response.setSuccess(true);
                    break;

                case CHANGE_PASSWORD:
                    try {
                        Object[] pwData = (Object[]) req.getData();
                        String maTK = Mapper.map(pwData[0], String.class);
                        String oldPw = Mapper.map(pwData[1], String.class);
                        String newPw = Mapper.map(pwData[2], String.class);

                        boolean changeOk = taiKhoanService.changePassword(maTK, oldPw, newPw);
                        response.setSuccess(changeOk);
                        response.setMessage(changeOk ? "Đổi mật khẩu thành công!" : "Mật khẩu cũ không đúng hoặc tài khoản không tồn tại!");
                    } catch (Exception e) {
                        response.setSuccess(false);
                        response.setMessage("Lỗi Server: " + e.getMessage());
                    }
                    break;

                case GENERATE_ID:
                    String type = Mapper.map(req.getData(), String.class);
                    String generatedId = "";
                    if ("HOA_DON".equals(type)) generatedId = util.IdGenerator.generateHoaDonId();
                    else if ("CTHD".equals(type)) generatedId = util.IdGenerator.generateChiTietHoaDonId();
                    else if ("NHAN_VIEN".equals(type)) generatedId = util.IdGenerator.generateNhanVienId();
                    else if ("TAI_KHOAN".equals(type)) generatedId = util.IdGenerator.generateTaiKhoanId();
                    else if ("DO_UONG".equals(type)) generatedId = util.IdGenerator.generateDoUongId();

                    response.setSuccess(true);
                    response.setData(generatedId);
                    break;

                case GET_THONG_KE:
                    try {
                        Object[] dateRange = (Object[]) req.getData();
                        LocalDate fromDate = Mapper.map(dateRange[0], LocalDate.class);
                        LocalDate toDate = Mapper.map(dateRange[1], LocalDate.class);
                        dto.ThongKeDTO thongKe = thongKeService.getThongKe(fromDate, toDate);
                        response.setSuccess(true);
                        response.setData(thongKe);
                    } catch (Exception e) {
                        response.setSuccess(false);
                        response.setMessage("Lỗi thống kê: " + e.getMessage());
                    }
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.setSuccess(false);
            response.setMessage("Lỗi Server: " + e.getMessage());
        }
        return response;
    }
}
