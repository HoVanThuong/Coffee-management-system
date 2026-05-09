package service.impl;

import dao.NhanVienDao;
import dao.impl.NhanVienDaoImpl;
import dto.NhanVienDTO;
import dto.TaiKhoanDTO;
import entity.NhanVien;
import entity.TaiKhoan;
import mapper.Mapper;
import service.NhanVienService;
import java.util.List;
import java.util.stream.Collectors;

public class NhanVienServiceImpl implements NhanVienService {
    private NhanVienDao nhanVienDao;

    public NhanVienServiceImpl() {
        this.nhanVienDao = new NhanVienDaoImpl();
    }

    @Override
    public List<NhanVienDTO> getAllEmployees(boolean includeFired) {
        return nhanVienDao.findAllWithAccount(includeFired).stream()
                .map(nv -> Mapper.map(nv, NhanVienDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public boolean addEmployee(NhanVienDTO nvDto) {
        NhanVien nv = Mapper.map(nvDto, NhanVien.class);
        return nhanVienDao.insert(nv);
    }

    @Override
    public boolean updateEmployee(NhanVienDTO nvDto) {
        NhanVien nv = Mapper.map(nvDto, NhanVien.class);
        return nhanVienDao.update(nv);
    }

    @Override
    public boolean addEmployeeWithAccount(NhanVienDTO nvDto, TaiKhoanDTO tkDto) {
        NhanVien nv = Mapper.map(nvDto, NhanVien.class);
        TaiKhoan tk = Mapper.map(tkDto, TaiKhoan.class);
        return nhanVienDao.insertWithAccount(nv, tk);
    }

    @Override
    public boolean updateEmployeeWithAccount(NhanVienDTO nvDto, TaiKhoanDTO tkDto) {
        NhanVien nv = Mapper.map(nvDto, NhanVien.class);
        TaiKhoan tk = Mapper.map(tkDto, TaiKhoan.class);
        return nhanVienDao.updateWithAccount(nv, tk);
    }

    @Override
    public boolean deleteEmployee(String id) {
        NhanVien nv = nhanVienDao.findById(id);
        if (nv != null) {
            nv.setNgayThoiViec(java.time.LocalDate.now());
            return nhanVienDao.update(nv);
        }
        return false;
    }

    @Override
    public boolean terminateEmployee(String maNV) {
        NhanVien nv = nhanVienDao.findById(maNV);
        if (nv != null) {
            nv.setNgayThoiViec(java.time.LocalDate.now()); 
            
            dao.TaiKhoanDao taiKhoanDao = new dao.impl.TaiKhoanDaoImpl();
            TaiKhoan actualTk = nv.getTaiKhoan();
            if (actualTk != null) {
                actualTk.setMatKhau(java.util.UUID.randomUUID().toString());
                taiKhoanDao.update(actualTk);
            }
            
            return nhanVienDao.update(nv);
        }
        return false;
    }
}