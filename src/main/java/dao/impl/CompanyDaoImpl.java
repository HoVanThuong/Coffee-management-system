package dao.impl;

import dao.CompanyDao;
import entity.Company;

public class CompanyDaoImpl extends AbstractGenericDaoImpl<Company, String> implements CompanyDao {

    public CompanyDaoImpl(){
        super(Company.class);
    }

    public static void main(String[] args) {
        CompanyDao companyDao = new CompanyDaoImpl();
//        companyDao.loadAll()
//                .forEach(c -> System.out.println(c));

        Company company = companyDao.findById("CP2");
        System.out.println(company);
    }

}
