package service;

import dto.NhanVienDTO;
import dto.TaiKhoanDTO;
import java.util.List;

public interface NhanVienService {
    List<NhanVienDTO> getAllEmployees(boolean includeFired);
    boolean addEmployee(NhanVienDTO nvDto);
    boolean addEmployeeWithAccount(NhanVienDTO nvDto, TaiKhoanDTO tkDto);
    boolean updateEmployee(NhanVienDTO nvDto);
    boolean updateEmployeeWithAccount(NhanVienDTO nvDto, TaiKhoanDTO tkDto);
    boolean deleteEmployee(String id);
    boolean terminateEmployee(String maNV);
}