package service;

import dto.ChiTietHoaDonDTO;
import dto.HoaDonDTO;
import java.util.List;

public interface HoaDonService {
    List<HoaDonDTO> getAllInvoices();
    boolean createInvoice(HoaDonDTO hdDto);
    List<HoaDonDTO> getInvoicesByDate(java.time.LocalDate date);
    List<HoaDonDTO> findInvoicesByDateRange(java.time.LocalDate fromDate, java.time.LocalDate toDate);
    HoaDonDTO getActiveOrderForTable(String maBan);
    boolean handleOrderFood(HoaDonDTO phieuDto, List<ChiTietHoaDonDTO> cartDto);
    boolean handlePayment(HoaDonDTO hoaDonDto);
}