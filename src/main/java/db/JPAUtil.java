package db;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public class JPAUtil {
    private static final String PERSISTENCE_UNIT_NAME = "mariadb-pu";
    private static EntityManagerFactory emf;
    private EntityManager entityManager; //not thread safe

    static {
        emf = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME);
    }

    public static EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public static void close(){
        if(emf != null)
            emf.close();
    }
}
