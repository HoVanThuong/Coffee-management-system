package dao;

import entity.Ban;
import java.util.List;

public interface BanDao {
    List<Ban> findAll();
    boolean insert(Ban ban);
    boolean update(Ban ban);
    boolean delete(String maBan);
    Ban findById(String maBan);
    boolean updateTrangThaiBan(String maBan, String trangThaiMoi);
}
