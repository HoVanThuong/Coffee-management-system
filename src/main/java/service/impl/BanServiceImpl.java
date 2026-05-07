package service.impl;

import dao.BanDao;
import entity.Ban;
import service.BanService;

import java.util.List;

public class BanServiceImpl implements BanService {
    private final BanDao banDao;

    public BanServiceImpl(BanDao banDao) {
        this.banDao = banDao;
    }

    @Override
    public List<Ban> findAll() {
        return banDao.findAll();
    }

    @Override
    public Ban findById(String maBan) {
        if (maBan == null || maBan.trim().isEmpty()) {
            return null;
        }
        return banDao.findById(maBan);
    }

    @Override
    public boolean updateStatus(String maBan, String status) {
        if (maBan == null || maBan.trim().isEmpty() || status == null) {
            return false;
        }
        String standardizedStatus = status.substring(0, 1).toUpperCase() + status.substring(1).toLowerCase();

        return banDao.updateTrangThaiBan(maBan, standardizedStatus);
    }
    @Override
    public boolean addBan(Ban ban) {
        if (ban == null || ban.getMaBan() == null) return false;
        return banDao.insert(ban);
    }

    @Override
    public boolean updateBan(Ban ban) {
        if (ban == null || ban.getMaBan() == null) return false;
        return banDao.update(ban);
    }

    @Override
    public boolean deleteBan(String maBan) {
        if (maBan == null || maBan.isEmpty()) return false;
        return banDao.delete(maBan);
    }
}
