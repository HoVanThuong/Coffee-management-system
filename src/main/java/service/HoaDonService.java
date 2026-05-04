package service;

import entity.ChiTietHoaDon;
import entity.HoaDon;
import java.util.List;

public interface HoaDonService {
    List<HoaDon> getAllInvoices();
    boolean createInvoice(HoaDon hd);
    List<HoaDon> getInvoicesByDate(java.time.LocalDate date);
    List<HoaDon> findInvoicesByDateRange(java.time.LocalDate fromDate, java.time.LocalDate toDate);
    HoaDon getActiveOrderForTable(String maBan);
    boolean handleOrderFood(HoaDon phieu, List<ChiTietHoaDon> cart);
    boolean handlePayment(HoaDon hoaDon);
}