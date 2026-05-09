package service.impl;

import dao.TaiKhoanDao;
import dao.impl.TaiKhoanDaoImpl;
import dto.TaiKhoanDTO;
import entity.TaiKhoan;
import mapper.Mapper;
import service.TaiKhoanService;

public class TaiKhoanServiceImpl implements TaiKhoanService {
    private TaiKhoanDao taiKhoanDao;

    public TaiKhoanServiceImpl() {
        this.taiKhoanDao = new TaiKhoanDaoImpl();
    }

    @Override
    public TaiKhoanDTO login(String username, String password) {
        System.out.println("Searching for user: " + username);
        TaiKhoan tk = taiKhoanDao.findByUsername(username);
        if (tk != null) {
            System.out.println("User found. Checking password...");
            if (tk.getMatKhau().equals(password)) {
                // Check if already logged in
                if ("Đang online".equals(tk.getTrangThai())) {
                    System.out.println("Login failed: Account already logged in elsewhere.");
                    return TaiKhoanDTO.builder().maTaiKhoan("ALREADY_LOGGED_IN").build();
                }

                // Check if employee is still working
                if (tk.getNhanVien() != null && tk.getNhanVien().getNgayThoiViec() != null) {
                    System.out.println("Login failed: Employee terminated.");
                    return null;
                }

                // Set status to online
                tk.setTrangThai("Đang online");
                taiKhoanDao.update(tk);

                System.out.println("Password match. Login success.");
                return Mapper.map(tk, TaiKhoanDTO.class);
            } else {
                System.out.println("Login failed: Password mismatch.");
            }
        } else {
            System.out.println("Login failed: User not found.");
        }
        return null;
    }

    @Override
    public boolean changePassword(String maTaiKhoan, String oldPw, String newPw) {
        TaiKhoan tk = taiKhoanDao.findById(maTaiKhoan);
        if (tk != null && tk.getMatKhau().equals(oldPw)) {
            tk.setMatKhau(newPw);
            return taiKhoanDao.update(tk);
        }
        return false;
    }

    @Override
    public void updateStatus(String maTaiKhoan, String status) {
        TaiKhoan tk = taiKhoanDao.findById(maTaiKhoan);
        if (tk != null) {
            tk.setTrangThai(status);
            taiKhoanDao.update(tk);
        }
    }
}
