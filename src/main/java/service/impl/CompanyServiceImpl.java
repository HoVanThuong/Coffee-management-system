package service.impl;

import dao.CompanyDao;
import dao.impl.CompanyDaoImpl;
import dto.CompanyDto;
import entity.Company;
import mapper.Mapper;
import service.CompanyService;

import java.util.List;
import java.util.Map;

public class CompanyServiceImpl implements CompanyService {

    private CompanyDao companyDao;

    public CompanyServiceImpl(){
        companyDao = new CompanyDaoImpl();
    }

    @Override
    public CompanyDto create(CompanyDto companyDto) {
        Company company = Mapper.map(companyDto, Company.class);
        company = companyDao.create(company);
        return Mapper.map(company, CompanyDto.class);
    }

    @Override
    public CompanyDto update(CompanyDto companyDto) {
        return null;
    }

    @Override
    public boolean delete(String companyId) {
        return false;
    }

    @Override
    public CompanyDto findById(String companyId) {

        if(companyId == null || companyId.isBlank())
            return null;

        Company company = companyDao.findById(companyId);
        return Mapper.map(company, CompanyDto.class);
    }

    @Override
    public List<CompanyDto> loadAll() {
        List<Company> companies = companyDao.loadAll();
        return companies
                .stream()
                .map(company -> Mapper.map(company, CompanyDto.class))
                .toList();
    }

    public static void main(String[] args) {
        CompanyService companyService = new CompanyServiceImpl();
        List<CompanyDto> companyDtos = companyService.loadAll();
        companyDtos.forEach(x -> System.out.println(x));
    }
}
