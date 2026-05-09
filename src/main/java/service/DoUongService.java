package service;

import dto.DoUongDTO;
import java.util.List;

public interface DoUongService {
    List<DoUongDTO> getAllDrinks();
    boolean addDrink(DoUongDTO duDto);
    boolean updateDrink(DoUongDTO duDto);
    boolean deleteDrink(String id);
    DoUongDTO findDrinkById(String id);
}