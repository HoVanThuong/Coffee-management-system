package service.impl;

import dao.impl.BanDaoImpl;
import dao.impl.HoaDonDaoImpl;
import db.JPAUtil;
import entity.Ban;
import entity.ChiTietHoaDon;
import entity.HoaDon;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import service.HoaDonService;

import java.util.List;

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
    public List<HoaDon> getAllInvoices() {
        return hoaDonDao.findAll();
    }

    @Override
    public boolean createInvoice(HoaDon hd) {
        return hoaDonDao.insert(hd);
    }

    @Override
    public List<HoaDon> getInvoicesByDate(java.time.LocalDate date) {
        return hoaDonDao.findByDate(date);
    }

    @Override
    public List<HoaDon> findInvoicesByDateRange(java.time.LocalDate fromDate, java.time.LocalDate toDate) {
        return hoaDonDao.findByDateRange(fromDate, toDate);
    }

    @Override
    public HoaDon getActiveOrderForTable(String maBan) {
        return hoaDonDao.getActiveOrderForTable(maBan);
    }

    @Override
        public boolean handleOrderFood(HoaDon phieu, List<ChiTietHoaDon> cart) {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tr = em.getTransaction();
        try {
            tr.begin();

            Ban managedBan = em.find(Ban.class, phieu.getBan().getMaBan());
            entity.NhanVien managedNV = em.find(entity.NhanVien.class, phieu.getNhanVien().getMaNhanVien());

            TypedQuery<HoaDon> queryOrder = em.createQuery(
                    "SELECT p FROM HoaDon p WHERE p.ban.maBan = :maBan AND p.trangThai = 'Chưa thanh toán'", HoaDon.class);
            queryOrder.setParameter("maBan", phieu.getBan().getMaBan());

            HoaDon existingPhieu = null;
            try {
                existingPhieu = queryOrder.getSingleResult();
            } catch (NoResultException e) { }

            if (existingPhieu != null) {

                for (ChiTietHoaDon ct : cart) {
                    if (ct.getId() == null) {
                        TypedQuery<ChiTietHoaDon> checkQuery = em.createQuery(
                                "SELECT c FROM ChiTietHoaDon c WHERE c.hoaDon.maHoaDon = :maHD AND c.doUong.maDoUong = :maDU", ChiTietHoaDon.class);
                        checkQuery.setParameter("maHD", existingPhieu.getMaHoaDon());
                        checkQuery.setParameter("maDU", ct.getDoUong().getMaDoUong());

                        List<ChiTietHoaDon> existingCts = checkQuery.getResultList();
                        if (!existingCts.isEmpty()) {
                            ChiTietHoaDon existingCt = existingCts.get(0);
                            existingCt.setSoLuong(existingCt.getSoLuong() + ct.getSoLuong());
                            em.merge(existingCt);
                        } else {
                            ct.setHoaDon(existingPhieu);
                            if (ct.getDoUong() != null) {
                                entity.DoUong managedDoUong = em.find(entity.DoUong.class, ct.getDoUong().getMaDoUong());
                                ct.setDoUong(managedDoUong);
                            }
                            em.persist(ct);
                        }
                    }
                }
                existingPhieu.setTongTien(phieu.getTongTien());
                em.merge(existingPhieu);
            } else {
                phieu.setBan(managedBan);
                phieu.setNhanVien(managedNV);
                phieu.setTrangThai(TRANG_THAI_CHUA_THANH_TOAN);

                em.persist(phieu); // Lưu hóa đơn trước

                for (ChiTietHoaDon ct : cart) {
                    ct.setHoaDon(phieu);
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
            if (tr.isActive()) tr.rollback();
            e.printStackTrace();
            return false;
        } finally {
            em.close();
        }
    }

    @Override
    public boolean handlePayment(HoaDon hoaDon) {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tr = em.getTransaction();
        try {
            tr.begin();

            // Tìm trực tiếp bằng ID thay vì merge object phức tạp từ client
            HoaDon managed = em.find(HoaDon.class, hoaDon.getMaHoaDon());
            if (managed == null) {
                tr.rollback();
                return false;
            }

            managed.setTrangThai(TRANG_THAI_DA_THANH_TOAN);
            managed.setTongTien(hoaDon.getTongTien());
            if (hoaDon.getNgayTao() != null)      managed.setNgayTao(hoaDon.getNgayTao());
            if (hoaDon.getPhuongThucTT() != null) managed.setPhuongThucTT(hoaDon.getPhuongThucTT());

            Ban banToFree = em.find(Ban.class, managed.getBan().getMaBan());
            if (banToFree != null) {
                banToFree.setTrangThai(TRANG_THAI_BAN_TRONG);
            }

            tr.commit();
            return true;
        } catch (Exception e) {
            if (tr.isActive()) tr.rollback();
            e.printStackTrace();
            return false;
        } finally {
            em.close();
        }
    }
}
