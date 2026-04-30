import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public class CreateDBSchema {
    public static void main(String[] args) {
        /*
         * Dòng lệnh Persistence.createEntityManagerFactory("mariadb-pu") bên dưới
         * sẽ tự động tạo các bảng dựa trên cấu hình trong file persistence.xml.
         *
         * Tuy nhiên, nếu bạn muốn tự tạo thủ công hoặc xem các câu lệnh SQL,
         * dưới đây là toàn bộ các lệnh cần thiết.
         */
        try {
            // Sử dụng tên persistence unit đã định nghĩa trong persistence.xml
            EntityManagerFactory emf = Persistence.createEntityManagerFactory("mariadb-pu");
            emf.createEntityManager(); // Tạo EntityManager để kích hoạt việc tạo schema
            emf.close();
            System.out.println("Database schema created/updated successfully!");
        } catch (Exception e) {
            System.err.println("Error creating/updating database schema: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /*
    -- Xóa dữ liệu cũ (Tùy chọn: nếu bạn muốn làm sạch bảng trước khi test)
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE `tai_khoan`;
TRUNCATE TABLE `nhan_vien`;
TRUNCATE TABLE `ban`;
TRUNCATE TABLE `do_uong`;
TRUNCATE TABLE `phieu_goi_mon`;
TRUNCATE TABLE `chi_tiet_phieu_goi`;
TRUNCATE TABLE `hoa_don`;
SET FOREIGN_KEY_CHECKS = 1;

-- 1. Chèn dữ liệu Nhân Viên
INSERT INTO `nhan_vien` (`ma_nhan_vien`, `ho_ten`, `chuc_vu`) VALUES
('NV01', 'Admin Quản Lý', 'Manager'),
('NV02', 'Nhân Viên Ca Sáng', 'Staff');

-- 2. Chèn dữ liệu Tài Khoản (Đã thêm ma_tai_khoan)
INSERT INTO `tai_khoan` (`ma_tai_khoan`, `ten_dang_nhap`, `mat_khau`, `tai_khoan_quan_li`, `ma_nhan_vien`) VALUES
('TK01', 'admin', '123', 1, 'NV01'),
('TK02', 'staff', '123', 0, 'NV02');

-- 3. Chèn dữ liệu Bàn
INSERT INTO `ban` (`ma_ban`, `trang_thai`, `vi_tri`) VALUES
('B01', 'Trống', 'Tầng 1'),
('B02', 'Trống', 'Tầng 1'),
('B03', 'Trống', 'Tầng 2'),
('B04', 'Trống', 'Tầng 2');

-- 4. Chèn dữ liệu Đồ Uống
INSERT INTO `do_uong` (`ma_do_uong`, `ten_do_uong`, `gia_tien`, `loai_do_uong`) VALUES
('DU01', 'Cà phê đen', 20000, 'Cà phê'),
('DU02', 'Bạc xỉu', 25000, 'Cà phê'),
('DU03', 'Trà đào cam sả', 35000, 'Trà'),
('DU04', 'Nước ép cam', 30000, 'Nước ép');
     */
}
