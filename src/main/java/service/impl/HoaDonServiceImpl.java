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

            TypedQuery<HoaDon> queryOrder = em.createQuery("SELECT p FROM HoaDon p WHERE p.ban.maBan = :maBan AND p.trangThai = 'Chưa thanh toán'", HoaDon.class);
            queryOrder.setParameter("maBan", phieu.getBan().getMaBan());
            HoaDon existingPhieu = null;
            try {
                existingPhieu = queryOrder.getSingleResult();
            } catch (NoResultException e) {
            }

            if (existingPhieu != null) {
                for (ChiTietHoaDon ct : cart) {
                    ct.setHoaDon(existingPhieu);
                    if (ct.getDoUong() != null) {
                        ct.setDoUong(em.merge(ct.getDoUong())); 
                    }
                    if (ct.getId() != null) {
                        em.merge(ct);
                    } else {
                        em.persist(ct);
                    }
                }
                existingPhieu.setTongTien(phieu.getTongTien());
                em.merge(existingPhieu);
            } else {
                if (phieu.getBan() != null) phieu.setBan(em.merge(phieu.getBan()));
                if (phieu.getNhanVien() != null) phieu.setNhanVien(em.merge(phieu.getNhanVien()));
                
                phieu.setTrangThai(TRANG_THAI_CHUA_THANH_TOAN);
                em.persist(phieu);
                
                for (ChiTietHoaDon ct : cart) {
                    ct.setHoaDon(phieu);
                    if (ct.getDoUong() != null) {
                        ct.setDoUong(em.merge(ct.getDoUong())); 
                    }
                    em.persist(ct);
                }
                
                Ban ban = em.find(Ban.class, phieu.getBan().getMaBan());
                if (ban != null) {
                    ban.setTrangThai(TRANG_THAI_BAN_CO_KHACH);
                    em.merge(ban);
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
            HoaDon attachedHoaDon = em.merge(hoaDon);
            attachedHoaDon.setTrangThai(TRANG_THAI_DA_THANH_TOAN);
            em.persist(attachedHoaDon);

            Ban banToFree = em.find(Ban.class, attachedHoaDon.getBan().getMaBan());
            if (banToFree != null) {
                banToFree.setTrangThai(TRANG_THAI_BAN_TRONG);
                em.merge(banToFree);
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