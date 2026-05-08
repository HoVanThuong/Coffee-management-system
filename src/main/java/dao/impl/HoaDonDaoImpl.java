package dao.impl;

import dao.HoaDonDao;
import entity.HoaDon;
import db.JPAUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import java.util.List;

public class HoaDonDaoImpl implements HoaDonDao {

    public HoaDonDaoImpl() {
    }

    @Override
    public List<HoaDon> findAll() {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.createQuery("SELECT DISTINCT hd FROM HoaDon hd " +
                    "LEFT JOIN FETCH hd.ban " +
                    "LEFT JOIN FETCH hd.nhanVien " +
                    "LEFT JOIN FETCH hd.chiTietHoaDons ct " +
                    "LEFT JOIN FETCH ct.doUong " +
                    "ORDER BY hd.ngayTao DESC", HoaDon.class).getResultList();
        } finally {
            em.close();
        }
    }

    @Override
    public boolean insert(HoaDon hd) {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tr = em.getTransaction();
        try {
            tr.begin();
            em.persist(hd);
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
    public boolean update(HoaDon hd) {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tr = em.getTransaction();
        try {
            tr.begin();
            em.merge(hd);
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
    public boolean delete(String id) {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tr = em.getTransaction();
        try {
            tr.begin();
            HoaDon hd = em.find(HoaDon.class, id);
            if (hd != null) {
                em.remove(hd);
                tr.commit();
                return true;
            }
            return false;
        } catch (Exception e) {
            if (tr.isActive()) tr.rollback();
            e.printStackTrace();
            return false;
        } finally {
            em.close();
        }
    }

    @Override
    public HoaDon findById(String id) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.find(HoaDon.class, id);
        } finally {
            em.close();
        }
    }

    @Override
    public List<HoaDon> findByDate(java.time.LocalDate date) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.createQuery("SELECT hd FROM HoaDon hd WHERE hd.ngayTao = :date", HoaDon.class)
                    .setParameter("date", date)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public List<HoaDon> findByDateRange(java.time.LocalDate fromDate, java.time.LocalDate toDate) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.createQuery("SELECT hd FROM HoaDon hd WHERE hd.ngayTao BETWEEN :start AND :end", HoaDon.class)
                    .setParameter("start", fromDate)
                    .setParameter("end", toDate)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public List<HoaDon> findByStatus(String status) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.createQuery("SELECT hd FROM HoaDon hd WHERE hd.trangThai = :status", HoaDon.class)
                    .setParameter("status", status)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public HoaDon getActiveOrderForTable(String maBan) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.createQuery("SELECT p FROM HoaDon p " +
                    "LEFT JOIN FETCH p.ban " +
                    "LEFT JOIN FETCH p.nhanVien " +
                    "LEFT JOIN FETCH p.chiTietHoaDons ct " +
                    "LEFT JOIN FETCH ct.doUong " +
                    "WHERE p.ban.maBan = :maBan AND p.trangThai = 'Chưa thanh toán'", HoaDon.class)
                    .setParameter("maBan", maBan)
                    .getSingleResult();
        } catch (jakarta.persistence.NoResultException e) {
            return null;
        } finally {
            em.close();
        }
    }

    public boolean updatePayment(String maHoaDon, String trangThai, String phuongThucTT, String ghiChu) {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tr = em.getTransaction();
        try {
            tr.begin();
            HoaDon hd = em.find(HoaDon.class, maHoaDon);
            if (hd == null) {
                tr.rollback();
                return false;
            }
            hd.setTrangThai(trangThai);
            hd.setPhuongThucTT(phuongThucTT);
            hd.setGhiChu(ghiChu);
            em.merge(hd);
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
