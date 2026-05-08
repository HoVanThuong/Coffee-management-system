package service;

import dto.TaiKhoanDTO;

public interface TaiKhoanService {
    TaiKhoanDTO login(String username, String password);
    boolean changePassword(String maTaiKhoan, String oldPw, String newPw);
}
