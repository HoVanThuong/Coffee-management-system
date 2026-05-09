package dao;

import entity.HoaDon;
import java.util.List;
public interface HoaDonDao {
    List<HoaDon> findAll();
    boolean insert(HoaDon hd);
    boolean update(HoaDon hd);
    boolean delete(String id);
    HoaDon findById(String id);
    List<HoaDon> findByDate(java.time.LocalDate date);
    List<HoaDon> findByDateRange(java.time.LocalDate fromDate, java.time.LocalDate toDate);
    HoaDon getActiveOrderForTable(String maBan);
}