package service;

import dto.ApplicationDto;
import entity.Application;

import java.util.List;

public interface ApplicationService {
    ApplicationDto create(ApplicationDto applicationDto);
    ApplicationDto update(ApplicationDto applicationDto);
    boolean delete(Application.ApplicationId applicationId);
    ApplicationDto findById(Application.ApplicationId applicationId);
    List<ApplicationDto> loadAll();
    List<ApplicationDto> findBySkillInOpenJobs(String skill);

}
