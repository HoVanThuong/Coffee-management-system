package service.impl;

import dao.impl.HoaDonDaoImpl;
import dto.ThongKeDTO;
import dto.ThongKeDoUongDTO;
import entity.ChiTietHoaDon;
import entity.HoaDon;
import service.ThongKeService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Triển khai logic thống kê doanh thu từ tầng DAO
 */
public class ThongKeServiceImpl implements ThongKeService {

    private final HoaDonDaoImpl hoaDonDao = new HoaDonDaoImpl();
    private static final String TRANG_THAI_DA_THANH_TOAN = "Đã thanh toán";
    private static final DateTimeFormatter DAY_FMT   = DateTimeFormatter.ofPattern("dd/MM");
    private static final DateTimeFormatter MONTH_FMT = DateTimeFormatter.ofPattern("MM/yyyy");

    @Override
    public ThongKeDTO getThongKe(LocalDate fromDate, LocalDate toDate) {
        // Lấy toàn bộ hóa đơn (đã JOIN FETCH đầy đủ) từ DAO
        List<HoaDon> allInvoices = hoaDonDao.findByDateRange(fromDate, toDate);

        // Lọc chỉ lấy hóa đơn đã thanh toán
        List<HoaDon> paidInvoices = allInvoices.stream()
                .filter(h -> TRANG_THAI_DA_THANH_TOAN.equals(h.getTrangThai()))
                .collect(Collectors.toList());

        System.out.println("[ThongKe] Total invoices in range: " + allInvoices.size()
                + " | Paid: " + paidInvoices.size());

        // --- Tổng hợp dữ liệu tổng quan ---
        double tongDoanhThu = paidInvoices.stream()
                .mapToDouble(h -> h.getTongTien() != null ? h.getTongTien() : 0.0)
                .sum();
        int tongHoaDon = paidInvoices.size();
        double doanhThuTrungBinh = tongHoaDon > 0 ? tongDoanhThu / tongHoaDon : 0.0;

        long tongBanPhucVu = paidInvoices.stream()
                .filter(h -> h.getBan() != null)
                .map(h -> h.getBan().getMaBan())
                .distinct()
                .count();

        // --- Tổng hợp doanh thu theo thời gian (tự động điều chỉnh granularity) ---
        long daySpan = fromDate.until(toDate).getDays() + 1;
        Map<String, Double> doanhThuTheoNgay;

        if (daySpan <= 31) {
            // Chế độ ngày: mỗi cột là 1 ngày (dd/MM)
            doanhThuTheoNgay = initDayMap(fromDate, toDate);
            for (HoaDon hd : paidInvoices) {
                if (hd.getNgayTao() != null) {
                    String key = hd.getNgayTao().format(DAY_FMT);
                    doanhThuTheoNgay.computeIfPresent(key, (k, v) -> v + safeDouble(hd.getTongTien()));
                }
            }
        } else {
            // Chế độ tháng: luôn hiện đủ 12 cột T1–T12 (dù khoảng lọc nhỏ hơn 1 năm)
            doanhThuTheoNgay = new LinkedHashMap<>();
            for (int m = 1; m <= 12; m++) {
                doanhThuTheoNgay.put("T" + m, 0.0);
            }
            for (HoaDon hd : paidInvoices) {
                if (hd.getNgayTao() == null) continue;
                String key = "T" + hd.getNgayTao().getMonthValue();
                doanhThuTheoNgay.merge(key, safeDouble(hd.getTongTien()), Double::sum);
            }
        }

        // --- Tổng hợp Top Món Bán Chạy ---
        Map<String, ThongKeDoUongDTO> monBanChayMap = new LinkedHashMap<>();
        for (HoaDon hd : paidInvoices) {
            if (hd.getChiTietHoaDons() == null) continue;
            for (ChiTietHoaDon ct : hd.getChiTietHoaDons()) {
                if (ct.getDoUong() == null) continue;
                String ma = ct.getDoUong().getMaDoUong();
                ThongKeDoUongDTO tkDU = monBanChayMap.computeIfAbsent(ma, k -> {
                    ThongKeDoUongDTO dto = new ThongKeDoUongDTO();
                    dto.setMaDoUong(ma);
                    dto.setTenDoUong(ct.getDoUong().getTenDoUong());
                    dto.setLoaiDoUong(ct.getDoUong().getLoaiDoUong());
                    dto.setSoLuongDaBan(0);
                    dto.setDoanhThu(0.0);
                    return dto;
                });
                tkDU.setSoLuongDaBan(tkDU.getSoLuongDaBan() + ct.getSoLuong());
                tkDU.setDoanhThu(tkDU.getDoanhThu() + ct.getSoLuong() * ct.getDonGia());
            }
        }

        List<ThongKeDoUongDTO> topMonBanChay = monBanChayMap.values().stream()
                .sorted(Comparator.comparingInt(ThongKeDoUongDTO::getSoLuongDaBan).reversed())
                .limit(10)
                .collect(Collectors.toList());

        return ThongKeDTO.builder()
                .tongDoanhThu(tongDoanhThu)
                .tongHoaDon(tongHoaDon)
                .doanhThuTrungBinhMoiDon(doanhThuTrungBinh)
                .tongBanDaPhucVu((int) tongBanPhucVu)
                .doanhThuTheoNgay(doanhThuTheoNgay)
                .topMonBanChay(topMonBanChay)
                .build();
    }

    // ── Helpers ──────────────────────────────────────────────────────
    private Map<String, Double> initDayMap(LocalDate from, LocalDate to) {
        Map<String, Double> map = new LinkedHashMap<>();
        LocalDate d = from;
        while (!d.isAfter(to)) {
            map.put(d.format(DAY_FMT), 0.0);
            d = d.plusDays(1);
        }
        return map;
    }

    private double safeDouble(Double v) {
        return v != null ? v : 0.0;
    }
}
