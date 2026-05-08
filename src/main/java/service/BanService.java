package service;

import dto.BanDTO;
import java.util.List;

public interface BanService {
    List<BanDTO> findAll();
    BanDTO findById(String maBan);
    boolean updateStatus(String maBan, String status);
    boolean addBan(BanDTO banDto);
    boolean updateBan(BanDTO banDto);
    boolean deleteBan(String maBan);
}
