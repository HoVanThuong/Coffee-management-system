package service;

import entity.NhanVien;
import java.util.List;

public interface NhanVienService {
    List<NhanVien> getAllEmployees(boolean includeFired);
    boolean addEmployee(NhanVien nv);
    boolean addEmployeeWithAccount(NhanVien nv, entity.TaiKhoan tk);
    boolean updateEmployee(NhanVien nv);
    boolean updateEmployeeWithAccount(NhanVien nv, entity.TaiKhoan tk);
    boolean deleteEmployee(String id);
    boolean terminateEmployee(String maNV);
}