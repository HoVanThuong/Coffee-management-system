package dao;

import entity.DoUong;
import java.util.List;

public interface DoUongDao {
    List<DoUong> findAll();
    boolean insert(DoUong du);
    boolean update(DoUong du);
    boolean delete(String id);
    DoUong findById(String id);
}