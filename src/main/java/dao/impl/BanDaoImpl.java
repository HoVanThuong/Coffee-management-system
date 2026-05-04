package dao.impl;

import dao.BanDao;
import entity.Ban;
import db.JPAUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

import java.util.List;

public class BanDaoImpl implements BanDao {

    public BanDaoImpl() {
    }

    @Override
    public List<Ban> findAll() {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.createQuery("SELECT b FROM Ban b", Ban.class).getResultList();
        } finally {
            em.close();
        }
    }

    @Override
    public boolean insert(Ban ban) {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tr = em.getTransaction();
        try {
            tr.begin();
            em.persist(ban);
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
    public boolean update(Ban ban) {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tr = em.getTransaction();
        try {
            tr.begin();
            em.merge(ban);
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
    public boolean delete(String maBan) {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tr = em.getTransaction();
        try {
            tr.begin();
            Ban ban = em.find(Ban.class, maBan);
            if (ban != null) {
                em.remove(ban);
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
    public Ban findById(String maBan) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.find(Ban.class, maBan);
        } finally {
            em.close();
        }
    }

    @Override
    public boolean updateTrangThaiBan(String maBan, String trangThaiMoi) {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tr = em.getTransaction();
        try {
            tr.begin();
            Ban ban = em.find(Ban.class, maBan);
            if (ban != null) {
                ban.setTrangThai(trangThaiMoi);
                em.merge(ban);
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
}
