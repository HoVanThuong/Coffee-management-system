package dao;

import entity.NhanVien;
import java.util.List;
public interface NhanVienDao {
    List<NhanVien> findAll();
    boolean insert(NhanVien nv);
    boolean update(NhanVien nv);
    boolean delete(String id);
    NhanVien findById(String id);
    List<NhanVien> findAllWithAccount(boolean includeFired);
    boolean insertWithAccount(NhanVien nv, entity.TaiKhoan tk);
    boolean updateWithAccount(NhanVien nv, entity.TaiKhoan tk);
}