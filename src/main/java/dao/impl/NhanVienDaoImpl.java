package dao.impl;

import dao.NhanVienDao;
import entity.NhanVien;
import db.JPAUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import java.util.List;

public class NhanVienDaoImpl implements NhanVienDao {

    public NhanVienDaoImpl() {
    }

    @Override
    public List<NhanVien> findAll() {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.createQuery("SELECT nv FROM NhanVien nv", NhanVien.class).getResultList();
        } finally {
            em.close();
        }
    }

    public List<NhanVien> findAllWithAccount(boolean includeFired) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            String jpql = "SELECT n FROM NhanVien n LEFT JOIN FETCH n.taiKhoan";
            if (!includeFired) {
                jpql += " WHERE n.ngayThoiViec IS NULL";
            }
            return em.createQuery(jpql, NhanVien.class).getResultList();
        } finally {
            em.close();
        }
    }

    @Override
    public boolean insert(NhanVien nv) {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tr = em.getTransaction();
        try {
            tr.begin();
            em.persist(nv);
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
    public boolean update(NhanVien nv) {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tr = em.getTransaction();
        try {
            tr.begin();
            em.merge(nv);
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
            NhanVien nv = em.find(NhanVien.class, id);
            if (nv != null) {
                em.remove(nv);
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
    public boolean insertWithAccount(NhanVien nv, entity.TaiKhoan tk) {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tr = em.getTransaction();
        try {
            tr.begin();
            em.persist(nv);
            if (tk != null) {
                tk.setNhanVien(nv);
                em.persist(tk);
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
    public boolean updateWithAccount(NhanVien nv, entity.TaiKhoan tk) {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tr = em.getTransaction();
        try {
            tr.begin();
            NhanVien dbEmp = em.find(NhanVien.class, nv.getMaNhanVien());
            if (dbEmp != null) {
                dbEmp.setHoTen(nv.getHoTen());
                dbEmp.setSdt(nv.getSdt());
                dbEmp.setEmail(nv.getEmail());
                dbEmp.setNgaySinh(nv.getNgaySinh());
                dbEmp.setNgayVaoLam(nv.getNgayVaoLam());
                dbEmp.setNgayThoiViec(nv.getNgayThoiViec());
                dbEmp.setChucVu(nv.getChucVu());
                em.merge(dbEmp);

                if (tk != null) {
                    try {
                        entity.TaiKhoan dbTaiKhoan = em.createQuery("SELECT t FROM TaiKhoan t WHERE t.nhanVien.maNhanVien = :maNhanVien", entity.TaiKhoan.class)
                            .setParameter("maNhanVien", dbEmp.getMaNhanVien())
                            .getSingleResult();
                        dbTaiKhoan.setTenDangNhap(tk.getTenDangNhap());
                        if (tk.getMatKhau() != null && !tk.getMatKhau().isEmpty()) {
                            dbTaiKhoan.setMatKhau(tk.getMatKhau());
                        }
                        dbTaiKhoan.setTaiKhoanQuanLi(tk.isTaiKhoanQuanLi());
                        em.merge(dbTaiKhoan);
                    } catch (jakarta.persistence.NoResultException e) {
                        tk.setNhanVien(dbEmp);
                        em.persist(tk);
                    }
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
    public NhanVien findById(String id) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.find(NhanVien.class, id);
        } finally {
            em.close();
        }
    }
}