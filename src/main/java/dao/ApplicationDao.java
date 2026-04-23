package dao;

import entity.Application;

import java.util.List;

public interface ApplicationDao extends GenericDao<Application, Application.ApplicationId>{

//    + findBySkillInOpenJobs(skill: String):List<Object[]>
//            (Kết quả trả về gồm: Ứng viên, tiêu đề công việc và ngày ứng tuyển)

    List<Object[]> findBySkillInOpenJobs(String skill);
}
