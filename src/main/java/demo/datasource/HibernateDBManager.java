package demo.datasource;

import org.hibernate.*;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.query.Query;

import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class HibernateDBManager implements DBManager {

    private static SessionFactory sessionFactory = buildSessionFactory();

    private static final String SELECT_TRANSFER_OPERATIONS_BY_DATES = "FROM TRANSFER_OPERATION" +
            "WHERE CREATED_WHEN < :before " +
            "AND CREATED_WHEN > :after " +
            "AND (ACCOUNT_ID = :accountId or TRANSFER_ACCOUNT_ID = :accountId)";

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
    public String updateAccount(final Account account) throws SQLException {
        final StringBuilder result = new StringBuilder();
        executeInTransaction(session -> {
            Account existingAccount = session.get(Account.class, account.getId());
            if (existingAccount == null) {
                result.append(String.format(Constants.Messages.ACCOUNT_NOT_FOUND, account.getId()));
                return;
            }
                session.update(account);
                result.append(String.format(Constants.Messages.ACCOUNT_UPDATED, account.getId()));
        });
        return result.toString();
    }

    @Override
    public String addAccount(final Account account) throws SQLException {
        final StringBuilder accountId = new StringBuilder();
        executeInTransaction(session -> accountId.append((String)session.save(account)));
        return accountId.toString();
    }

    @Override
    public String deleteAccount(String accountId) throws SQLException {
        final StringBuilder result = new StringBuilder();
        executeInTransaction(session -> {
            Account account = session.get(Account.class, accountId);
            if (account == null) {
                result.append(String.format(Constants.Messages.ACCOUNT_NOT_FOUND, accountId));
                return;
            }
            session.delete(account);
            result.append(String.format(Constants.Messages.ACCOUNT_DELETED, accountId));
        });
        return result.toString();
    }

    @FunctionalInterface
    private interface SQLOperation {
        void execute(Session session) throws SQLException;
    }

    private void executeInTransaction(SQLOperation operation) throws SQLException {
        final Session session = getSessionFactory().openSession();
        Transaction transaction = null;
        try {
            transaction = session.beginTransaction();
            operation.execute(session);
            transaction.commit();
        } catch (Exception e) {
            try {
                if (transaction != null) {
                    session.getTransaction().rollback();
                }
            } catch (Exception rolle) {
                rolle.addSuppressed(e);
                throw new SQLException(rolle);
            }
            throw new SQLException(e);
        } finally {
            session.close();
        }
    }

    @Override
    public TransferOperation getTransferOperationById(String id) {
        Session session = getSessionFactory().openSession();
        TransferOperation result = session.get(TransferOperation.class, id);
        session.close();
        return result;
    }

    @Override
    public List<TransferOperation> getTransferOperationsByDates(Date after, Date before, String accountId) throws SQLException {
        Session session = getSessionFactory().openSession();
        Query query =  session.createQuery(SELECT_TRANSFER_OPERATIONS_BY_DATES, TransferOperation.class);
        query.setParameter("after", after);
        query.setParameter("before", before);
        query.setParameter("accountId", accountId);
        session.close();
        return query.getResultList();
    }

    @Override
    public List<TransferOperation> getTransferOperationsSinceDate(Date after, String accountId) throws SQLException {
        return  getTransferOperationsByDates(after, Calendar.getInstance().getTime(), accountId);
    }

    @Override
    public Account getAccountById(String id) {
        Session session = getSessionFactory().openSession();
        Account result = session.get(Account.class, id);
        session.close();
        return result;
    }

    @Override
    public String procceedTransfer(final TransferOperation operation) throws SQLException {
        StringBuilder operationId = new StringBuilder();
        executeInTransaction(session -> {
            Account account = session.load(Account.class, operation.getAccountId());
            Account transferAccount = session.load(Account.class, operation.getTransferAccountId());
            double accountBalance = account.getBalance();
            double sum = operation.getSum();
            if (accountBalance >= sum) {
                double transferCccountBalance = transferAccount.getBalance();
                account.setBalance(accountBalance - sum);
                transferAccount.setBalance(transferCccountBalance + sum);
                operation.setStatus(Constants.TransferOperation.STATUS_COMPLETED);
                session.update(account);
                session.update(transferAccount);
            } else {
                operation.setStatus(Constants.TransferOperation.STATUS_FAILED);
                operation.setDetails(Constants.Messages.FAILED_DETAILED_INSUFFICIENT_FUNDS);
            }

            transferAccount.setBalance(transferAccount.getBalance() + operation.getSum());
            operationId.append(session.save(operation));

        });
        return operationId.toString();
    }

    @Override
    public String procceedTopUp(final TransferOperation operation) throws SQLException {
        final StringBuilder result = new StringBuilder();
        executeInTransaction(session -> {
            Account account = session.get(Account.class, operation.getAccountId());
            if (account != null) {
                account.setBalance(account.getBalance() + operation.getSum());
                session.update(account);
                result.append(String.format(Constants.Messages.OPERATION_PROCESSED, session.save(operation)));
            } else {
                result.append(String.format(Constants.Messages.ACCOUNT_NOT_FOUND, operation.getAccountId()));
            }
        });
        return result.toString();
    }

    @Override
    public String procceedWithdraw(final TransferOperation operation) throws SQLException {
        StringBuilder operationId = new StringBuilder();
        executeInTransaction(session -> {
            Account account = session.load(Account.class, operation.getAccountId());
            double balance = account.getBalance();
            double sum = operation.getSum();
            if (balance >= sum) {
                account.setBalance(balance - sum);
                operation.setStatus(Constants.TransferOperation.STATUS_COMPLETED);
                session.update(account);
            } else {
                operation.setStatus(Constants.TransferOperation.STATUS_FAILED);
                operation.setDetails(Constants.Messages.FAILED_DETAILED_INSUFFICIENT_FUNDS);
            }
            operationId.append(session.save(operation));
        });
        return operationId.toString();
    }
}

