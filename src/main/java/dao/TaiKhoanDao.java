package dao;

import entity.TaiKhoan;

public interface TaiKhoanDao {
    TaiKhoan findByUsername(String username);
    TaiKhoan findById(String id);
    boolean update(TaiKhoan tk);
}
