package demo.datasource;

import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;

public class HibernateDBManager implements DBManager{

    private static SessionFactory sessionFactory = buildSessionFactory();

    private static SessionFactory buildSessionFactory() {
        // A SessionFactory is set up once for an application!
        final StandardServiceRegistry registry = new StandardServiceRegistryBuilder()
                .configure() // configures settings from hibernate.cfg.xml
                .build();
        try {
            sessionFactory = new MetadataSources(registry).buildMetadata().buildSessionFactory();
        }
        catch (Exception e) {
            // The registry would be destroyed by the SessionFactory, but we had trouble building the SessionFactory
            // so destroy it manually.
            StandardServiceRegistryBuilder.destroy( registry );

            throw new ExceptionInInitializerError("Initial SessionFactory failed" + e);
        }
        return sessionFactory;
    }

    private static SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    @Override
    public void queueBalanceChangeRequest(QueueRequest request, TransferOperation operation) {

    }

    @Override
    public void queueAccountChangeRequest(QueueRequest request) {

    }

    @Override
    public void initTables() {

    }

    @Override
    public String getResponceForQueueNumber(int requestNumber) {
        return null;
    }
}
