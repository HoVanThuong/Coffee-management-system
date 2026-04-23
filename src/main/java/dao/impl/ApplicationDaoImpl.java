package dao.impl;

import dao.ApplicationDao;
import entity.Application;
import entity.JobStatus;

import java.util.List;

public class ApplicationDaoImpl extends AbstractGenericDaoImpl<Application, Application.ApplicationId> implements ApplicationDao {

    public ApplicationDaoImpl(){
        super(Application.class);
    }


    @Override
    public List<Object[]> findBySkillInOpenJobs(String skill) {

        String query = "Select c, j.title, app.appliedDate " +
                "from Candidate c " +
                "join c.applications app " +
                "join app.job j " +
                "join j.skills jsk " +
                "join c.skills csk " +
                "where j.status = :status " +
                "and jsk.name = :skill " +
                "and csk.name = :skill ";

        return doInTransaction(em -> {
            return em.createQuery(query)
                    .setParameter("status", JobStatus.OPEN)
                    .setParameter("skill", skill)
                    .getResultList();
        });
    }

    public static void main(String[] args) {

        ApplicationDao applicationDao = new ApplicationDaoImpl();
        Application.ApplicationId applicationId = new Application.ApplicationId("C2", "J2");
//        Application application = applicationDao.findById(applicationId);
//        System.out.println(application);
//
//        applicationDao.loadAll()
//                .forEach(app -> System.out.println(app));
        List<Object[]> list = applicationDao.findBySkillInOpenJobs("Java");
        for(Object[] arr: list) {
            System.out.println(arr[0]);
            System.out.println(arr[1]);
            System.out.println(arr[2]);
            System.out.println("===");
        }
    }

}
