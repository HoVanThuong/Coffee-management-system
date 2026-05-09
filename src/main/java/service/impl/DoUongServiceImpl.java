package service.impl;

import dao.DoUongDao;
import dao.impl.DoUongDaoImpl;
import dto.DoUongDTO;
import entity.DoUong;
import mapper.Mapper;
import service.DoUongService;
import java.util.List;
import java.util.stream.Collectors;

public class DoUongServiceImpl implements DoUongService {
    private DoUongDao doUongDao = new DoUongDaoImpl();

    @Override
    public List<DoUongDTO> getAllDrinks() {
        return doUongDao.findAll().stream()
                .map(d -> Mapper.map(d, DoUongDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public boolean addDrink(DoUongDTO duDto) {
        if (Double.parseDouble(duDto.getGiaTien()) < 0) {
            return false;
        }
        DoUong du = Mapper.map(duDto, DoUong.class);
        return doUongDao.insert(du);
    }

    @Override
    public boolean updateDrink(DoUongDTO duDto) {
        DoUong du = Mapper.map(duDto, DoUong.class);
        return doUongDao.update(du);
    }

    @Override
    public boolean deleteDrink(String id) {
        return doUongDao.delete(id);
    }

    @Override
    public DoUongDTO findDrinkById(String id) {
        DoUong du = doUongDao.findById(id);
        return Mapper.map(du, DoUongDTO.class);
    }
}