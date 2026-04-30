package network;

import db.JPAUtil;
import entity.*;
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
        EntityManager em = JPAUtil.getEntityManager();
        Response response = new Response();

        try {
            switch (req.getCommandType()) {
                case LOGIN:
                    TaiKhoan inputTk = (TaiKhoan) req.getData();
                    try {
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
                    }
                    break;

                case GET_TABLES:
                    List<Ban> tables = em.createQuery("SELECT b FROM Ban b", Ban.class).getResultList();
                    response.setSuccess(true);
                    response.setData(tables);
                    break;

                case GET_MENU:
                    List<DoUong> menu = em.createQuery("SELECT d FROM DoUong d", DoUong.class).getResultList();
                    response.setSuccess(true);
                    response.setData(menu);
                    break;

                case ORDER_FOOD:
                    em.getTransaction().begin();
                    Object[] orderData = (Object[]) req.getData();
                    PhieuGoiMon phieu = (PhieuGoiMon) orderData[0];
                    List<ChiTietPhieuGoi> cart = (List<ChiTietPhieuGoi>) orderData[1];

                    TypedQuery<PhieuGoiMon> queryOrder = em.createQuery("SELECT p FROM PhieuGoiMon p WHERE p.ban.maBan = :maBan AND p.trangThai = 'Chưa thanh toán'", PhieuGoiMon.class);
                    queryOrder.setParameter("maBan", phieu.getBan().getMaBan());
                    PhieuGoiMon existingPhieu = null;
                    try {
                        existingPhieu = queryOrder.getSingleResult();
                    } catch (NoResultException e) {
                    }

                    if (existingPhieu != null) {
                        for (ChiTietPhieuGoi ct : cart) {
                            ct.setPhieuGoiMon(existingPhieu);
                            if (ct.getDoUong() != null) {
                                ct.setDoUong(em.merge(ct.getDoUong())); // Tránh lỗi detached entity
                            }
                            if (ct.getId() != null) {
                                em.merge(ct);
                            } else {
                                em.persist(ct);
                            }
                        }
                        existingPhieu.setTongTien(phieu.getTongTien());
                        em.merge(existingPhieu);
                    } else {
                        // Tránh lỗi detached entity khi persist PhieuGoiMon mới
                        if (phieu.getBan() != null) phieu.setBan(em.merge(phieu.getBan()));
                        if (phieu.getNhanVien() != null) phieu.setNhanVien(em.merge(phieu.getNhanVien()));
                        
                        phieu.setTrangThai("Chưa thanh toán");
                        em.persist(phieu);
                        
                        for (ChiTietPhieuGoi ct : cart) {
                            ct.setPhieuGoiMon(phieu);
                            if (ct.getDoUong() != null) {
                                ct.setDoUong(em.merge(ct.getDoUong())); // Tránh lỗi detached entity
                            }
                            em.persist(ct);
                        }
                        
                        Ban ban = em.find(Ban.class, phieu.getBan().getMaBan());
                        if (ban != null) {
                            ban.setTrangThai("Có khách");
                            em.merge(ban);
                        }
                    }
                    em.getTransaction().commit();
                    response.setSuccess(true);
                    response.setMessage("Cập nhật phiếu gọi món thành công!");
                    break;

                case GET_ORDER:
                    String maBan = (String) req.getData();
                    TypedQuery<PhieuGoiMon> getOrderQuery = em.createQuery("SELECT p FROM PhieuGoiMon p LEFT JOIN FETCH p.chiTietPhieuGois ct LEFT JOIN FETCH ct.doUong WHERE p.ban.maBan = :maBan AND p.trangThai = 'Chưa thanh toán'", PhieuGoiMon.class);
                    getOrderQuery.setParameter("maBan", maBan);
                    try {
                        PhieuGoiMon activeOrder = getOrderQuery.getSingleResult();
                        response.setSuccess(true);
                        response.setData(activeOrder);
                    } catch (NoResultException e) {
                        response.setSuccess(false);
                        response.setMessage("Bàn này không có phiếu gọi món nào chưa thanh toán.");
                    }
                    break;

                case PAY_BILL:
                    em.getTransaction().begin();
                    HoaDon hoaDon = (HoaDon) req.getData();
                    PhieuGoiMon attachedPhieu = em.merge(hoaDon.getPhieuGoiMon());
                    attachedPhieu.setTrangThai("Đã thanh toán");
                    hoaDon.setPhieuGoiMon(attachedPhieu);
                    em.persist(hoaDon);

                    Ban banToFree = em.find(Ban.class, attachedPhieu.getBan().getMaBan());
                    if (banToFree != null) {
                        banToFree.setTrangThai("Trống");
                        em.merge(banToFree);
                    }
                    em.getTransaction().commit();
                    response.setSuccess(true);
                    response.setMessage("Thanh toán thành công!");
                    break;

                // Employee Management Cases
                case GET_EMPLOYEES:
                    boolean includeFired = req.getData() != null && (Boolean) req.getData();
                    String jpql = "SELECT n FROM NhanVien n LEFT JOIN FETCH n.taiKhoan";
                    if (!includeFired) {
                        jpql += " WHERE n.ngayThoiViec IS NULL";
                    }
                    List<NhanVien> employees = em.createQuery(jpql, NhanVien.class).getResultList();
                    response.setSuccess(true);
                    response.setData(employees);
                    break;

                case MANAGE_EMPLOYEE_ADD:
                    em.getTransaction().begin();
                    Object[] addEmpData = (Object[]) req.getData();
                    NhanVien newEmp = (NhanVien) addEmpData[0];
                    TaiKhoan newTk = (TaiKhoan) addEmpData[1];

                    em.persist(newEmp);
                    if (newTk != null) {
                        newTk.setNhanVien(newEmp);
                        em.persist(newTk);
                    }

                    em.getTransaction().commit();
                    response.setSuccess(true);
                    response.setMessage("Thêm nhân viên thành công!");
                    break;

                case MANAGE_EMPLOYEE_UPDATE:
                    em.getTransaction().begin();
                    Object[] updateEmpData = (Object[]) req.getData();
                    NhanVien updatedEmp = (NhanVien) updateEmpData[0];
                    TaiKhoan updatedTk = (TaiKhoan) updateEmpData[1];

                    NhanVien dbEmp = em.find(NhanVien.class, updatedEmp.getMaNhanVien());
                    if (dbEmp != null) {
                        dbEmp.setHoTen(updatedEmp.getHoTen());
                        dbEmp.setSdt(updatedEmp.getSdt());
                        dbEmp.setEmail(updatedEmp.getEmail());
                        dbEmp.setNgaySinh(updatedEmp.getNgaySinh());
                        dbEmp.setNgayVaoLam(updatedEmp.getNgayVaoLam());
                        dbEmp.setNgayThoiViec(updatedEmp.getNgayThoiViec());
                        dbEmp.setChucVu(updatedEmp.getChucVu());
                        em.merge(dbEmp);

                        if (updatedTk != null) {
                            TypedQuery<TaiKhoan> queryTk = em.createQuery("SELECT t FROM TaiKhoan t WHERE t.nhanVien.maNhanVien = :maNhanVien", TaiKhoan.class);
                            queryTk.setParameter("maNhanVien", dbEmp.getMaNhanVien());
                            try {
                                TaiKhoan dbTaiKhoan = queryTk.getSingleResult();
                                dbTaiKhoan.setTenDangNhap(updatedTk.getTenDangNhap());
                                if (updatedTk.getMatKhau() != null && !updatedTk.getMatKhau().isEmpty()) {
                                    dbTaiKhoan.setMatKhau(updatedTk.getMatKhau());
                                }
                                dbTaiKhoan.setTaiKhoanQuanLi(updatedTk.isTaiKhoanQuanLi());
                                em.merge(dbTaiKhoan);
                            } catch (NoResultException e) {
                                updatedTk.setNhanVien(dbEmp);
                                em.persist(updatedTk);
                            }
                        }
                    }

                    em.getTransaction().commit();
                    response.setSuccess(true);
                    response.setMessage("Cập nhật nhân viên thành công!");
                    break;

                case MANAGE_EMPLOYEE_DELETE:
                    em.getTransaction().begin();
                    String maNhanVienToFire = (String) req.getData();
                    NhanVien empToFire = em.find(NhanVien.class, maNhanVienToFire);
                    if (empToFire != null) {
                        empToFire.setNgayThoiViec(LocalDate.now());

                        TypedQuery<TaiKhoan> queryTk = em.createQuery("SELECT t FROM TaiKhoan t WHERE t.nhanVien.maNhanVien = :maNhanVien", TaiKhoan.class);
                        queryTk.setParameter("maNhanVien", empToFire.getMaNhanVien());
                        try {
                            TaiKhoan tkToDisable = queryTk.getSingleResult();
                            tkToDisable.setMatKhau(UUID.randomUUID().toString());
                            em.merge(tkToDisable);
                        } catch (NoResultException e) {
                        }
                        em.merge(empToFire);
                        response.setSuccess(true);
                        response.setMessage("Đã cho nhân viên thôi việc và vô hiệu hóa tài khoản!");
                    } else {
                        response.setSuccess(false);
                        response.setMessage("Không tìm thấy nhân viên!");
                    }
                    em.getTransaction().commit();
                    break;

                case MANAGE_MENU_ADD:
                    em.getTransaction().begin();
                    DoUong newDoUong = (DoUong) req.getData();
                    em.persist(newDoUong);
                    em.getTransaction().commit();
                    response.setSuccess(true);
                    response.setMessage("Thêm món thành công!");
                    break;

                case MANAGE_MENU_UPDATE:
                    em.getTransaction().begin();
                    DoUong updatedDoUong = (DoUong) req.getData();
                    DoUong dbDoUong = em.find(DoUong.class, updatedDoUong.getMaDoUong());
                    if (dbDoUong != null) {
                        dbDoUong.setTenDoUong(updatedDoUong.getTenDoUong());
                        dbDoUong.setGiaTien(updatedDoUong.getGiaTien());
                        dbDoUong.setLoaiDoUong(updatedDoUong.getLoaiDoUong());
                        em.merge(dbDoUong);
                        response.setSuccess(true);
                        response.setMessage("Cập nhật món thành công!");
                    } else {
                        response.setSuccess(false);
                        response.setMessage("Không tìm thấy món để cập nhật!");
                    }
                    em.getTransaction().commit();
                    break;

                case MANAGE_MENU_DELETE:
                    em.getTransaction().begin();
                    String maMonToDelete = (String) req.getData();
                    DoUong doUongToDelete = em.find(DoUong.class, maMonToDelete);
                    if (doUongToDelete != null) {
                        em.remove(doUongToDelete);
                        response.setSuccess(true);
                        response.setMessage("Xóa món thành công!");
                    } else {
                        response.setSuccess(false);
                        response.setMessage("Không tìm thấy món để xóa!");
                    }
                    em.getTransaction().commit();
                    break;

                case GET_INVOICES:
                    String invoiceQuery = "SELECT h FROM HoaDon h LEFT JOIN FETCH h.phieuGoiMon p LEFT JOIN FETCH p.ban LEFT JOIN FETCH p.nhanVien ORDER BY h.ngayTao DESC";
                    List<HoaDon> invoices = em.createQuery(invoiceQuery, HoaDon.class).getResultList();
                    response.setSuccess(true);
                    response.setData(invoices);
                    break;
            }
        } catch (Exception e) {
            if (em.getTransaction() != null && em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            e.printStackTrace();
            response.setSuccess(false);
            response.setMessage("Lỗi Server: " + e.getMessage());
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
        return response;
    }
}