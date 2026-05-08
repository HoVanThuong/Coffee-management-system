package dao.impl;

import dao.TaiKhoanDao;
import db.JPAUtil;
import entity.TaiKhoan;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;

public class TaiKhoanDaoImpl implements TaiKhoanDao {

    @Override
    public TaiKhoan findByUsername(String username) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<TaiKhoan> query = em.createQuery(
                "SELECT t FROM TaiKhoan t LEFT JOIN FETCH t.nhanVien WHERE t.tenDangNhap = :username", 
                TaiKhoan.class
            );
            query.setParameter("username", username);
            return query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        } finally {
            em.close();
        }
    }

    @Override
    public TaiKhoan findById(String id) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.find(TaiKhoan.class, id);
        } finally {
            em.close();
        }
    }

    @Override
    public boolean update(TaiKhoan tk) {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tr = em.getTransaction();
        try {
            tr.begin();
            em.merge(tk);
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
