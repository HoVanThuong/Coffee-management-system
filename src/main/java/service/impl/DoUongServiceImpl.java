package service.impl;

import dao.DoUongDao;
import dao.impl.DoUongDaoImpl;
import dto.DoUongDTO;
import entity.DoUong;
import mapper.Mapper;
import service.DoUongService;
import java.util.List;

public class DoUongServiceImpl implements DoUongService {
    private DoUongDao doUongDao = new DoUongDaoImpl();

    @Override
    public List<DoUong> getAllDrinks() {
        return doUongDao.findAll();
    }

    @Override
    public boolean addDrink(DoUong du) {

        if (Double.parseDouble(du.getGiaTien()) < 0) {
            return false;
        }
        return doUongDao.insert(du);
    }

    @Override
    public boolean updateDrink(DoUong du) {
        return doUongDao.update(du);
    }

    @Override
    public boolean deleteDrink(String id) {
        return doUongDao.delete(id);
    }

    @Override
    public DoUong findDrinkById(String id) {
        return doUongDao.findById(id);
    }
}