package dao.impl;

import dao.DoUongDao;
import entity.DoUong;
import db.JPAUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import java.util.List;

public class DoUongDaoImpl implements DoUongDao {

    @Override
    public List<DoUong> findAll() {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.createQuery("SELECT d FROM DoUong d", DoUong.class).getResultList();
        } finally {
            em.close();
        }
    }

    @Override
    public boolean insert(DoUong du) {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tr = em.getTransaction();
        try {
            tr.begin();
            em.persist(du);
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
    public boolean update(DoUong du) {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tr = em.getTransaction();
        try {
            tr.begin();
            em.merge(du);
            tr.commit();
            return true;
        } catch (Exception e) {
            if (tr.isActive()) tr.rollback();
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
            DoUong du = em.find(DoUong.class, id);
            if (du != null) {
                em.remove(du);
                tr.commit();
                return true;
            }
            return false;
        } catch (Exception e) {
            if (tr.isActive()) tr.rollback();
            return false;
        } finally {
            em.close();
        }
    }

    @Override
    public DoUong findById(String id) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.find(DoUong.class, id);
        } finally {
            em.close();
        }
    }
}