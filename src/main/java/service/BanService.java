package service;

import entity.Ban;
import java.util.List;

public interface BanService {
    List<Ban> findAll();
    Ban findById(String maBan);
    boolean updateStatus(String maBan, String status);
    boolean addBan(Ban ban);
    boolean updateBan(Ban ban);
    boolean deleteBan(String maBan);
}
