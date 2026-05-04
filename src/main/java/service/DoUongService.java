package service;

import dto.DoUongDTO;
import entity.DoUong;
import java.util.List;

public interface DoUongService {
    List<DoUong> getAllDrinks();
    boolean addDrink(DoUong du);
    boolean updateDrink(DoUong du);
    boolean deleteDrink(String id);
    DoUong findDrinkById(String id);
}