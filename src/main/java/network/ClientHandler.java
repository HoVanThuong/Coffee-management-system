package network;

import entity.*;
import service.BanService;
import service.DoUongService;
import service.HoaDonService;
import service.NhanVienService;
import service.impl.BanServiceImpl;
import service.impl.DoUongServiceImpl;
import service.impl.HoaDonServiceImpl;
import service.impl.NhanVienServiceImpl;
import db.JPAUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class ClientHandler implements Runnable {
    private Socket socket;
    
    private final HoaDonService hoaDonService = new HoaDonServiceImpl();
    private final BanService banService = new BanServiceImpl(new dao.impl.BanDaoImpl());
    private final DoUongService doUongService = new DoUongServiceImpl();
    private final NhanVienService nhanVienService = new NhanVienServiceImpl();

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
        }
    }

    private Response processRequest(Request req) {
        Response response = new Response();

        try {
            switch (req.getCommandType()) {
                case LOGIN:
                    // LOGIN doesn't have a dedicated service, so we use EntityManager locally for now.
                    EntityManager em = JPAUtil.getEntityManager();
                    try {
                        TaiKhoan inputTk = (TaiKhoan) req.getData();
                        TypedQuery<TaiKhoan> query = em.createQuery("SELECT t FROM TaiKhoan t JOIN FETCH t.nhanVien WHERE t.tenDangNhap = :username", TaiKhoan.class);
                        query.setParameter("username", inputTk.getTenDangNhap());
                        TaiKhoan dbTk = query.getSingleResult();

                        if (dbTk.getNhanVien() != null && dbTk.getNhanVien().getNgayThoiViec() != null) {
                            response.setSuccess(false);
                            response.setMessage("Tài khoản này đã bị vô hiệu hóa!");
                        } else if (dbTk.getMatKhau().equals(inputTk.getMatKhau())) {
                            response.setSuccess(true);
                            response.setData(dbTk);
                        } else {
                            response.setSuccess(false);
                            response.setMessage("Sai mật khẩu!");
                        }
                    } catch (NoResultException e) {
                        response.setSuccess(false);
                        response.setMessage("Tài khoản không tồn tại!");
                    } finally {
                        em.close();
                    }
                    break;

                case GET_TABLES:
                    List<Ban> tables = banService.findAll();
                    response.setSuccess(true);
                    response.setData(tables);
                    break;

                case GET_MENU:
                    List<DoUong> menu = doUongService.getAllDrinks();
                    response.setSuccess(true);
                    response.setData(menu);
                    break;

                case ORDER_FOOD:
                    Object[] orderData = (Object[]) req.getData();
                    HoaDon phieu = (HoaDon) orderData[0];
                    List<ChiTietHoaDon> cart = (List<ChiTietHoaDon>) orderData[1];
                    
                    boolean orderSuccess = hoaDonService.handleOrderFood(phieu, cart);
                    if (orderSuccess) {
                        response.setSuccess(true);
                        response.setMessage("Cập nhật hóa đơn thành công!");
                    } else {
                        response.setSuccess(false);
                        response.setMessage("Lỗi cập nhật hóa đơn!");
                    }
                    break;

                case GET_ORDER:
                    String maBan = (String) req.getData();
                    HoaDon activeOrder = hoaDonService.getActiveOrderForTable(maBan);
                    if (activeOrder != null) {
                        response.setSuccess(true);
                        response.setData(activeOrder);
                    } else {
                        response.setSuccess(false);
                        response.setMessage("Bàn này không có hóa đơn nào chưa thanh toán.");
                    }
                    break;

                case PAY_BILL:
                    HoaDon hoaDon = (HoaDon) req.getData();
                    boolean paySuccess = hoaDonService.handlePayment(hoaDon);
                    if (paySuccess) {
                        response.setSuccess(true);
                        response.setMessage("Thanh toán thành công!");
                    } else {
                        response.setSuccess(false);
                        response.setMessage("Lỗi thanh toán!");
                    }
                    break;

                // Employee Management Cases
                case GET_EMPLOYEES:
                    boolean includeFired = req.getData() != null && (Boolean) req.getData();
                    List<NhanVien> employees = nhanVienService.getAllEmployees(includeFired);
                    response.setSuccess(true);
                    response.setData(employees);
                    break;

                case MANAGE_EMPLOYEE_ADD:
                    Object[] addEmpData = (Object[]) req.getData();
                    NhanVien newEmp = (NhanVien) addEmpData[0];
                    TaiKhoan newTk = (TaiKhoan) addEmpData[1];
                    
                    boolean addEmpSuccess = nhanVienService.addEmployeeWithAccount(newEmp, newTk);
                    if (addEmpSuccess) {
                        response.setSuccess(true);
                        response.setMessage("Thêm nhân viên thành công!");
                    } else {
                        response.setSuccess(false);
                        response.setMessage("Thêm nhân viên thất bại!");
                    }
                    break;

                case MANAGE_EMPLOYEE_UPDATE:
                    Object[] updateEmpData = (Object[]) req.getData();
                    NhanVien updatedEmp = (NhanVien) updateEmpData[0];
                    TaiKhoan updatedTk = (TaiKhoan) updateEmpData[1];
                    
                    boolean updateEmpSuccess = nhanVienService.updateEmployeeWithAccount(updatedEmp, updatedTk);
                    if (updateEmpSuccess) {
                        response.setSuccess(true);
                        response.setMessage("Cập nhật nhân viên thành công!");
                    } else {
                        response.setSuccess(false);
                        response.setMessage("Cập nhật nhân viên thất bại!");
                    }
                    break;

                case MANAGE_EMPLOYEE_DELETE:
                    String maNhanVienToFire = (String) req.getData();
                    
                    // We can just use terminateEmployee from service, but it doesn't disable account there.
                    // Wait, NhanVienService terminateEmployee needs to disable account?
                    // The old code disabled account by changing password to a UUID. 
                    // Let's implement that in DAO or here. For now, doing it here with EM is faster, or we update the Service.
                    // Let's update Service later, for now just use EM here to match old behavior.
                    EntityManager emDelete = JPAUtil.getEntityManager();
                    emDelete.getTransaction().begin();
                    try {
                        NhanVien empToFire = emDelete.find(NhanVien.class, maNhanVienToFire);
                        if (empToFire != null) {
                            empToFire.setNgayThoiViec(LocalDate.now());

                            TypedQuery<TaiKhoan> queryTk = emDelete.createQuery("SELECT t FROM TaiKhoan t WHERE t.nhanVien.maNhanVien = :maNhanVien", TaiKhoan.class);
                            queryTk.setParameter("maNhanVien", empToFire.getMaNhanVien());
                            try {
                                TaiKhoan tkToDisable = queryTk.getSingleResult();
                                tkToDisable.setMatKhau(UUID.randomUUID().toString());
                                emDelete.merge(tkToDisable);
                            } catch (NoResultException e) {
                            }
                            emDelete.merge(empToFire);
                            response.setSuccess(true);
                            response.setMessage("Đã cho nhân viên thôi việc và vô hiệu hóa tài khoản!");
                        } else {
                            response.setSuccess(false);
                            response.setMessage("Không tìm thấy nhân viên!");
                        }
                        emDelete.getTransaction().commit();
                    } catch (Exception e) {
                        if (emDelete.getTransaction().isActive()) emDelete.getTransaction().rollback();
                        response.setSuccess(false);
                        response.setMessage("Lỗi xóa nhân viên");
                    } finally {
                        emDelete.close();
                    }
                    break;

                case MANAGE_MENU_ADD:
                    DoUong newDoUong = (DoUong) req.getData();
                    boolean addMenuSuccess = doUongService.addDrink(newDoUong);
                    if (addMenuSuccess) {
                        response.setSuccess(true);
                        response.setMessage("Thêm món thành công!");
                    } else {
                        response.setSuccess(false);
                        response.setMessage("Thêm món thất bại!");
                    }
                    break;

                case MANAGE_MENU_UPDATE:
                    DoUong updatedDoUong = (DoUong) req.getData();
                    boolean updateMenuSuccess = doUongService.updateDrink(updatedDoUong);
                    if (updateMenuSuccess) {
                        response.setSuccess(true);
                        response.setMessage("Cập nhật món thành công!");
                    } else {
                        response.setSuccess(false);
                        response.setMessage("Cập nhật món thất bại!");
                    }
                    break;

                case MANAGE_MENU_DELETE:
                    String maMonToDelete = (String) req.getData();
                    boolean delMenuSuccess = doUongService.deleteDrink(maMonToDelete);
                    if (delMenuSuccess) {
                        response.setSuccess(true);
                        response.setMessage("Xóa món thành công!");
                    } else {
                        response.setSuccess(false);
                        response.setMessage("Xóa món thất bại!");
                    }
                    break;

                case GET_INVOICES:
                    List<HoaDon> invoices = hoaDonService.getAllInvoices();
                    response.setSuccess(true);
                    response.setData(invoices);
                    break;

                case CHANGE_PASSWORD:
                    Object[] pwData = (Object[]) req.getData();
                    String maTaiKhoan = (String) pwData[0];
                    String oldPw = (String) pwData[1];
                    String newPw = (String) pwData[2];

                    EntityManager emPw = JPAUtil.getEntityManager();
                    emPw.getTransaction().begin();
                    try {
                        TaiKhoan tk = emPw.find(TaiKhoan.class, maTaiKhoan);
                        if (tk == null) {
                            response.setSuccess(false);
                            response.setMessage("Tài khoản không tồn tại!");
                        } else if (!tk.getMatKhau().equals(oldPw)) {
                            response.setSuccess(false);
                            response.setMessage("Mật khẩu cũ không đúng!");
                        } else {
                            tk.setMatKhau(newPw);
                            emPw.merge(tk);
                            response.setSuccess(true);
                            response.setMessage("Đổi mật khẩu thành công!");
                        }
                        emPw.getTransaction().commit();
                    } catch (Exception e) {
                        if (emPw.getTransaction().isActive()) emPw.getTransaction().rollback();
                        response.setSuccess(false);
                        response.setMessage("Lỗi đổi mật khẩu: " + e.getMessage());
                    } finally {
                        emPw.close();
                    }
                    break;

                case GENERATE_ID:
                    String type = (String) req.getData();
                    String generatedId = "";
                    if ("HOA_DON".equals(type)) generatedId = util.IdGenerator.generateHoaDonId();
                    else if ("CTHD".equals(type)) generatedId = util.IdGenerator.generateChiTietHoaDonId();
                    else if ("NHAN_VIEN".equals(type)) generatedId = util.IdGenerator.generateNhanVienId();
                    else if ("TAI_KHOAN".equals(type)) generatedId = util.IdGenerator.generateTaiKhoanId();
                    else if ("DO_UONG".equals(type)) generatedId = util.IdGenerator.generateDoUongId();
                    
                    response.setSuccess(true);
                    response.setData(generatedId);
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