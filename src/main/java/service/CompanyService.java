package service;

import dto.CompanyDto;

import java.util.List;

public interface CompanyService {

    CompanyDto create(CompanyDto companyDto);
    CompanyDto update(CompanyDto companyDto);
    boolean delete(String companyId);
    CompanyDto findById(String companyId);
    List<CompanyDto> loadAll();
}
