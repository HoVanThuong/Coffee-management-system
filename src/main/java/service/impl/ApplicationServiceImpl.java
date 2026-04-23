package service.impl;

import dao.ApplicationDao;
import dao.impl.ApplicationDaoImpl;
import dto.ApplicationDto;
import entity.Application;
import entity.Candidate;
import mapper.Mapper;
import service.ApplicationService;

import java.time.LocalDate;
import java.util.List;

public class ApplicationServiceImpl implements ApplicationService {

    private ApplicationDao applicationDao;

    public ApplicationServiceImpl(){
        applicationDao = new ApplicationDaoImpl();
    }

    @Override
    public ApplicationDto create(ApplicationDto applicationDto) {
        return null;
    }

    @Override
    public ApplicationDto update(ApplicationDto applicationDto) {
        return null;
    }

    @Override
    public boolean delete(Application.ApplicationId applicationId) {
        return false;
    }

    @Override
    public ApplicationDto findById(Application.ApplicationId applicationId) {
        Application application = applicationDao.findById(applicationId);
        ApplicationDto applicationDto = Mapper.map(application);
        return applicationDto;



    }

    @Override
    public List<ApplicationDto> loadAll() {
        return null;
    }

    @Override
    public List<ApplicationDto> findBySkillInOpenJobs(String skill) {
        List<Object[]> list = applicationDao.findBySkillInOpenJobs("Java");
        return list.stream()
                .map(arr -> {
                    Candidate candidate = (Candidate) arr[0];
                    String jobTitle = (String) arr[1];
                    LocalDate appliedDate = (LocalDate) arr[2];
                    return ApplicationDto.builder()
                            .candidateId(candidate.getId())
                            .candidateName(candidate.getName())
                            .jobTitle(jobTitle)
                            .appliedDate(appliedDate)
                            .build();
                }).toList();
    }

    public static void main(String[] args) {
        ApplicationService applicationService = new ApplicationServiceImpl();
        Application.ApplicationId applicationId = new Application.ApplicationId("C2", "J2");
//        ApplicationDto applicationDto = applicationService.findById(applicationId);
//        System.out.println(applicationDto);

        applicationService.findBySkillInOpenJobs("Java")
                .forEach(app -> System.out.println(app));
    }
}
