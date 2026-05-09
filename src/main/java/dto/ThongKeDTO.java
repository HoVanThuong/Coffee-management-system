package dto;

import lombok.*;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * DTO chứa toàn bộ dữ liệu thống kê trả về cho Client
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ThongKeDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private double tongDoanhThu;
    private int tongHoaDon;
    private double doanhThuTrungBinhMoiDon;
    private int tongBanDaPhucVu;

    // Doanh thu từng ngày: key = "dd/MM/yyyy", value = doanh thu
    private Map<String, Double> doanhThuTheoNgay;

    // Top món bán chạy
    private List<ThongKeDoUongDTO> topMonBanChay;
}
