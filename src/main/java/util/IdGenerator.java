package util;

import db.JPAUtil;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class IdGenerator {

    public static String generateHoaDonId() {
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("ddMMyyyy"));

        EntityManager em = JPAUtil.getEntityManager();
        try {
            String query = "SELECT MAX(h.maHoaDon) FROM HoaDon h WHERE h.maHoaDon LIKE :prefix";
            String prefix = "HD" + dateStr;
            String maxId = em.createQuery(query, String.class)
                    .setParameter("prefix", prefix + "%")
                    .getSingleResult();

            int index = 1;
            if (maxId != null && maxId.length() >= 10) {
                try {
                    String numericPart = maxId.substring(10);
                    if (!numericPart.isEmpty()) {
                        index = Integer.parseInt(numericPart) + 1;
                    }
                } catch (Exception e) {
                    index = 1;
                }
            }
            return String.format("%s%03d", prefix, index);
        } finally {
            em.close();
        }
    }

    public static String generateChiTietHoaDonId() {
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("ddMMyyyy"));
        EntityManager em = JPAUtil.getEntityManager();
        try {
            // Since ChiTietHoaDon might not have an explicit ID in some designs, 
            // but here it is a String ID in entity.
            String query = "SELECT MAX(ct.id) FROM ChiTietHoaDon ct WHERE ct.id LIKE :prefix";
            String prefix = "CTHD" + dateStr;
            String maxId = em.createQuery(query, String.class)
                             .setParameter("prefix", prefix + "%")
                             .getSingleResult();
            
            int index = 1;
            if (maxId != null && maxId.length() > 12) {
                try {
                    index = Integer.parseInt(maxId.substring(12)) + 1;
                } catch (Exception e) {}
            }
            return String.format("%s%03d", prefix, index);
        } finally {
            em.close();
        }
    }

    public static String generateNhanVienId() {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            String query = "SELECT MAX(n.maNhanVien) FROM NhanVien n WHERE n.maNhanVien LIKE 'NV%'";
            String maxId = em.createQuery(query, String.class).getSingleResult();
            
            int index = 1;
            if (maxId != null && maxId.length() > 2) {
                try {
                    index = Integer.parseInt(maxId.substring(2)) + 1;
                } catch (Exception e) {}
            }
            return String.format("NV%05d", index);
        } finally {
            em.close();
        }
    }

    public static String generateTaiKhoanId() {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            String query = "SELECT MAX(t.maTaiKhoan) FROM TaiKhoan t WHERE t.maTaiKhoan LIKE 'TK%'";
            String maxId = em.createQuery(query, String.class).getSingleResult();
            
            int index = 1;
            if (maxId != null && maxId.length() > 2) {
                try {
                    index = Integer.parseInt(maxId.substring(2)) + 1;
                } catch (Exception e) {}
            }
            return String.format("TK%05d", index);
        } finally {
            em.close();
        }
    }

    public static String generateDoUongId() {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            String query = "SELECT MAX(d.maDoUong) FROM DoUong d WHERE d.maDoUong LIKE 'DU%'";
            String maxId = em.createQuery(query, String.class).getSingleResult();
            
            int index = 1;
            if (maxId != null && maxId.length() > 2) {
                try {
                    index = Integer.parseInt(maxId.substring(2)) + 1;
                } catch (Exception e) {}
            }
            return String.format("DU%05d", index);
        } finally {
            em.close();
        }
    }
}
