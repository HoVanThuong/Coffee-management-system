package service.impl;

import dao.NhanVienDao;
import dao.impl.NhanVienDaoImpl;
import dto.NhanVienDTO;
import entity.NhanVien;
import mapper.Mapper;
import service.NhanVienService;
import java.util.List;

public class NhanVienServiceImpl implements NhanVienService {
    private NhanVienDao nhanVienDao;

    public NhanVienServiceImpl() {
        this.nhanVienDao = new NhanVienDaoImpl();
    }
    @Override
    public List<NhanVien> getAllEmployees(boolean includeFired) {
        return nhanVienDao.findAllWithAccount(includeFired);
    }

    @Override
    public boolean addEmployee(NhanVien nv) {
        // logic kiểm tra
        return nhanVienDao.insert(nv);
    }

    @Override
    public boolean updateEmployee(NhanVien nv) {
        return nhanVienDao.update(nv);
    }

    @Override
    public boolean addEmployeeWithAccount(NhanVien nv, entity.TaiKhoan tk) {
        return nhanVienDao.insertWithAccount(nv, tk);
    }

    @Override
    public boolean updateEmployeeWithAccount(NhanVien nv, entity.TaiKhoan tk) {
        return nhanVienDao.updateWithAccount(nv, tk);
    }

    @Override
    public boolean deleteEmployee(String id) {
        // cập nhật ngày thôi việc là ngày hôm nay
        NhanVien nv = nhanVienDao.findById(id);
        if (nv != null) {
            nv.setNgayThoiViec(java.time.LocalDate.now());
            return nhanVienDao.update(nv);
        }
        return false;
    }
    // Cập nhật trạng thái nghỉ việc cho nhân viên
    @Override
    public boolean terminateEmployee(String maNV) {
        NhanVien nv = nhanVienDao.findById(maNV);
        if (nv != null) {
            nv.setNgayThoiViec(java.time.LocalDate.now()); // Đánh dấu ngày nghỉ là hôm nay
            return nhanVienDao.update(nv); // Lưu lại vào MariaDB
        }
        return false;
    }
}