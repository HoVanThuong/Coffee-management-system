package service;

import dto.ThongKeDTO;
import java.time.LocalDate;

/**
 * Service interface cho chức năng thống kê doanh thu
 */
public interface ThongKeService {
    /**
     * Lấy dữ liệu thống kê trong khoảng thời gian
     * @param fromDate ngày bắt đầu
     * @param toDate ngày kết thúc
     * @return ThongKeDTO chứa toàn bộ dữ liệu thống kê
     */
    ThongKeDTO getThongKe(LocalDate fromDate, LocalDate toDate);
}
