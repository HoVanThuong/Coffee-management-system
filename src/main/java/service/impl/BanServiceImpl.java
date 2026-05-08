package service.impl;

import dao.BanDao;
import dto.BanDTO;
import entity.Ban;
import mapper.Mapper;
import service.BanService;

import java.util.List;
import java.util.stream.Collectors;

public class BanServiceImpl implements BanService {
    private final BanDao banDao;

    public BanServiceImpl(BanDao banDao) {
        this.banDao = banDao;
    }

    @Override
    public List<BanDTO> findAll() {
        return banDao.findAll().stream()
                .map(b -> Mapper.map(b, BanDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public BanDTO findById(String maBan) {
        if (maBan == null || maBan.trim().isEmpty()) {
            return null;
        }
        Ban b = banDao.findById(maBan);
        return Mapper.map(b, BanDTO.class);
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
    public boolean addBan(BanDTO banDto) {
        if (banDto == null || banDto.getMaBan() == null) return false;
        Ban b = Mapper.map(banDto, Ban.class);
        return banDao.insert(b);
    }

    @Override
    public boolean updateBan(BanDTO banDto) {
        if (banDto == null || banDto.getMaBan() == null) return false;
        Ban b = Mapper.map(banDto, Ban.class);
        return banDao.update(b);
    }

    @Override
    public boolean deleteBan(String maBan) {
        if (maBan == null || maBan.isEmpty()) return false;
        return banDao.delete(maBan);
    }
}
