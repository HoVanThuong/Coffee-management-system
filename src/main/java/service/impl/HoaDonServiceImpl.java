package service.impl;

import dao.impl.BanDaoImpl;
import dao.impl.HoaDonDaoImpl;
import db.JPAUtil;
import dto.ChiTietHoaDonDTO;
import dto.HoaDonDTO;
import entity.Ban;
import entity.ChiTietHoaDon;
import entity.HoaDon;
import entity.DoUong;
import entity.NhanVien;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import mapper.Mapper;
import service.HoaDonService;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.time.format.DateTimeFormatter;
import dto.ThongKeDTO;
import dto.ThongKeDoUongDTO;

public class HoaDonServiceImpl implements HoaDonService {
    private static final String TRANG_THAI_CHUA_THANH_TOAN = "Chưa thanh toán";
    private static final String TRANG_THAI_DA_THANH_TOAN = "Đã thanh toán";
    private static final String TRANG_THAI_BAN_TRONG = "Trống";
    private static final String TRANG_THAI_BAN_CO_KHACH = "Có khách";

    private final HoaDonDaoImpl hoaDonDao;
    private final BanDaoImpl banDao;

    public HoaDonServiceImpl() {
        this.hoaDonDao = new HoaDonDaoImpl();
        this.banDao = new BanDaoImpl();
    }

    @Override
    public List<HoaDonDTO> getAllInvoices() {
        return hoaDonDao.findAll().stream()
                .map(h -> Mapper.map(h, HoaDonDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public boolean createInvoice(HoaDonDTO hdDto) {
        HoaDon hd = Mapper.map(hdDto, HoaDon.class);
        return hoaDonDao.insert(hd);
    }

    @Override
    public List<HoaDonDTO> getInvoicesByDate(java.time.LocalDate date) {
        return hoaDonDao.findByDate(date).stream()
                .map(h -> Mapper.map(h, HoaDonDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<HoaDonDTO> findInvoicesByDateRange(java.time.LocalDate fromDate, java.time.LocalDate toDate) {
        return hoaDonDao.findByDateRange(fromDate, toDate).stream()
                .map(h -> Mapper.map(h, HoaDonDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public HoaDonDTO getActiveOrderForTable(String maBan) {
        HoaDon hd = hoaDonDao.getActiveOrderForTable(maBan);
        return hd != null ? Mapper.map(hd, HoaDonDTO.class) : null;
    }

    @Override
    public boolean handleOrderFood(HoaDonDTO phieuDto, List<ChiTietHoaDonDTO> cartDto) {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tr = em.getTransaction();
        try {
            tr.begin();

            Ban managedBan = em.find(Ban.class, phieuDto.getBan().getMaBan());
            NhanVien managedNV = em.find(NhanVien.class, phieuDto.getNhanVien().getMaNhanVien());

            TypedQuery<HoaDon> queryOrder = em.createQuery(
                    "SELECT p FROM HoaDon p WHERE p.ban.maBan = :maBan AND p.trangThai = 'Chưa thanh toán'",
                    HoaDon.class);
            queryOrder.setParameter("maBan", phieuDto.getBan().getMaBan());

            HoaDon existingPhieu = null;
            try {
                existingPhieu = queryOrder.getSingleResult();
            } catch (NoResultException e) {
            }

            if (existingPhieu != null) {
                // Fetch max index once for the current date/prefix
                String cthdPrefix = "CTHD"
                        + java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("ddMMyyyy"));
                String maxIdQuery = "SELECT MAX(c.id) FROM ChiTietHoaDon c WHERE c.id LIKE :prefix";
                String maxIdStr = em.createQuery(maxIdQuery, String.class).setParameter("prefix", cthdPrefix + "%")
                        .getSingleResult();
                int nextIndex = 1;
                if (maxIdStr != null && maxIdStr.length() > 12) {
                    try {
                        nextIndex = Integer.parseInt(maxIdStr.substring(12)) + 1;
                    } catch (Exception e) {
                    }
                }

                for (ChiTietHoaDonDTO ctDto : cartDto) {
                    if (ctDto.getId() == null) {
                        TypedQuery<ChiTietHoaDon> checkQuery = em.createQuery(
                                "SELECT c FROM ChiTietHoaDon c WHERE c.hoaDon.maHoaDon = :maHD AND c.doUong.maDoUong = :maDU",
                                ChiTietHoaDon.class);
                        checkQuery.setParameter("maHD", existingPhieu.getMaHoaDon());
                        checkQuery.setParameter("maDU", ctDto.getDoUong().getMaDoUong());

                        List<ChiTietHoaDon> existingCts = checkQuery.getResultList();
                        if (!existingCts.isEmpty()) {
                            ChiTietHoaDon existingCt = existingCts.get(0);
                            existingCt.setSoLuong(existingCt.getSoLuong() + ctDto.getSoLuong());
                            em.merge(existingCt);
                        } else {
                            ChiTietHoaDon ct = Mapper.map(ctDto, ChiTietHoaDon.class);
                            ct.setHoaDon(existingPhieu);
                            ct.setId(String.format("%s%03d", cthdPrefix, nextIndex++));
                            if (ct.getDoUong() != null) {
                                DoUong managedDoUong = em.find(DoUong.class, ct.getDoUong().getMaDoUong());
                                ct.setDoUong(managedDoUong);
                            }
                            em.persist(ct);
                        }
                    }
                }
                existingPhieu.setTongTien(phieuDto.getTongTien());
                em.merge(existingPhieu);
            } else {
                HoaDon phieu = Mapper.map(phieuDto, HoaDon.class);
                phieu.setBan(managedBan);
                phieu.setNhanVien(managedNV);
                phieu.setTrangThai(TRANG_THAI_CHUA_THANH_TOAN);
                phieu.setMaHoaDon(util.IdGenerator.generateHoaDonId());

                em.persist(phieu);

                String cthdPrefix = "CTHD"
                        + java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("ddMMyyyy"));
                String maxIdQuery = "SELECT MAX(c.id) FROM ChiTietHoaDon c WHERE c.id LIKE :prefix";
                String maxIdStr = em.createQuery(maxIdQuery, String.class).setParameter("prefix", cthdPrefix + "%")
                        .getSingleResult();
                int nextIndex = 1;
                if (maxIdStr != null && maxIdStr.length() > 12) {
                    try {
                        nextIndex = Integer.parseInt(maxIdStr.substring(12)) + 1;
                    } catch (Exception e) {
                    }
                }

                for (ChiTietHoaDonDTO ctDto : cartDto) {
                    ChiTietHoaDon ct = Mapper.map(ctDto, ChiTietHoaDon.class);
                    ct.setHoaDon(phieu);
                    ct.setId(String.format("%s%03d", cthdPrefix, nextIndex++));
                    if (ct.getDoUong() != null) {
                        ct.setDoUong(em.merge(ct.getDoUong()));
                    }
                    em.persist(ct);
                }

                if (managedBan != null) {
                    managedBan.setTrangThai(TRANG_THAI_BAN_CO_KHACH);
                    em.merge(managedBan);
                }
            }

            tr.commit();
            return true;
        } catch (Exception e) {
            if (tr.isActive())
                tr.rollback();
            e.printStackTrace();
            return false;
        } finally {
            em.close();
        }
    }

    @Override
    public boolean handlePayment(HoaDonDTO hoaDonDto) {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tr = em.getTransaction();
        try {
            tr.begin();

            HoaDon managed = em.find(HoaDon.class, hoaDonDto.getMaHoaDon());
            if (managed == null) {
                tr.rollback();
                return false;
            }

            managed.setTrangThai(TRANG_THAI_DA_THANH_TOAN);
            managed.setTongTien(hoaDonDto.getTongTien());
            if (hoaDonDto.getNgayTao() != null)
                managed.setNgayTao(hoaDonDto.getNgayTao());
            if (hoaDonDto.getPhuongThucTT() != null)
                managed.setPhuongThucTT(hoaDonDto.getPhuongThucTT());

            Ban banToFree = em.find(Ban.class, managed.getBan().getMaBan());
            if (banToFree != null) {
                banToFree.setTrangThai(TRANG_THAI_BAN_TRONG);
            }

            tr.commit();
            return true;
        } catch (Exception e) {
            if (tr.isActive())
                tr.rollback();
            e.printStackTrace();
            return false;
        } finally {
            em.close();
        }
    }

    @Override
    public ThongKeDTO getThongKe(java.time.LocalDate fromDate, java.time.LocalDate toDate) {
        List<HoaDon> allInvoices = hoaDonDao.findByDateRange(fromDate, toDate);
        List<HoaDon> paidInvoices = allInvoices.stream()
                .filter(h -> TRANG_THAI_DA_THANH_TOAN.equals(h.getTrangThai()))
                .collect(Collectors.toList());

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

        long daySpan = java.time.temporal.ChronoUnit.DAYS.between(fromDate, toDate) + 1;
        Map<String, Double> doanhThuTheoNgay;

        if (daySpan <= 31) {
            doanhThuTheoNgay = new LinkedHashMap<>();
            java.time.LocalDate d = fromDate;
            DateTimeFormatter dayFmt = DateTimeFormatter.ofPattern("dd/MM");
            while (!d.isAfter(toDate)) {
                doanhThuTheoNgay.put(d.format(dayFmt), 0.0);
                d = d.plusDays(1);
            }
            for (HoaDon hd : paidInvoices) {
                if (hd.getNgayTao() != null) {
                    String key = hd.getNgayTao().format(dayFmt);
                    doanhThuTheoNgay.computeIfPresent(key,
                            (k, v) -> v + (hd.getTongTien() != null ? hd.getTongTien() : 0.0));
                }
            }
        } else {
            doanhThuTheoNgay = new LinkedHashMap<>();
            for (int m = 1; m <= 12; m++) {
                doanhThuTheoNgay.put(String.format("Tháng %02d", m), 0.0);
            }
            for (HoaDon hd : paidInvoices) {
                if (hd.getNgayTao() == null)
                    continue;
                String key = String.format("Tháng %02d", hd.getNgayTao().getMonthValue());
                doanhThuTheoNgay.merge(key, hd.getTongTien() != null ? hd.getTongTien() : 0.0, Double::sum);
            }
        }

        Map<String, ThongKeDoUongDTO> monBanChayMap = new LinkedHashMap<>();
        for (HoaDon hd : paidInvoices) {
            if (hd.getChiTietHoaDons() == null)
                continue;
            for (entity.ChiTietHoaDon ct : hd.getChiTietHoaDons()) {
                if (ct.getDoUong() == null)
                    continue;
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
}
