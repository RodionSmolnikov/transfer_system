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

    private static final String SELECT_TRANSFER_OPERATIONS_BY_DATES = "FROM TransferOperation " +
            "where (ACCOUNT_ID = :accountId or TRANSFER_ACCOUNT_ID = :transfer_accountId) " +
            "order by CREATED_WHEN desc";

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
            if (account.getLastName() != null) {
                existingAccount.setLastName(account.getLastName());
            }
            if (account.getFirstName() != null) {
                existingAccount.setFirstName(account.getFirstName());
            }
            session.update(existingAccount);
            result.append(existingAccount.toString());
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
    public List<TransferOperation> getLastTransferOperations(String accountId , int last) throws SQLException {
        Session session = getSessionFactory().openSession();
        Query query =  session.createQuery(SELECT_TRANSFER_OPERATIONS_BY_DATES, TransferOperation.class);
        query.setMaxResults(last);
        //query.setFirstResult(last);
        //query.setFetchSize(last);
        query.setParameter("accountId", accountId);
        query.setParameter("transfer_accountId", accountId);
        List<TransferOperation> result = query.list();
        session.close();
        return result;
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
        final StringBuilder result = new StringBuilder();
        executeInTransaction(session -> {
            Account account = session.get(Account.class, operation.getAccountId());
            if (account == null) {
                result.append(String.format(Constants.Messages.ACCOUNT_NOT_FOUND, operation.getAccountId()));
                return;
            }

            Account transferAccount = session.get(Account.class, operation.getTransferAccountId());
            if (transferAccount == null) {
                result.append(String.format(Constants.Messages.ACCOUNT_NOT_FOUND, operation.getTransferAccountId()));
                return;
            }

            double accountBalance = account.getBalance();
            double sum = operation.getSum();
            if (accountBalance >= sum) {
                account.setBalance(accountBalance - sum);
                transferAccount.setBalance(transferAccount.getBalance() + sum);
                session.update(account);
                session.update(transferAccount);
                operation.setStatus(Constants.TransferOperation.STATUS_COMPLETED);
                result.append(String.format(Constants.Messages.OPERATION_PROCESSED, session.save(operation)));
            } else {
                operation.setStatus(Constants.TransferOperation.STATUS_FAILED);
                operation.setDetails(Constants.Messages.INSUFFICIENT_FUNDS_DETAILS);
                result.append(String.format(Constants.Messages.OPERATION_NOT_COMPLETED, session.save(operation), Constants.Messages.INSUFFICIENT_FUNDS_DETAILS));
            }
        });
        return result.toString();
    }

    @Override
    public String procceedTopUp(final TransferOperation operation) throws SQLException {
        final StringBuilder result = new StringBuilder();
        executeInTransaction(session -> {
            Account account = session.get(Account.class, operation.getAccountId());
            if (account != null) {
                account.setBalance(account.getBalance() + operation.getSum());
                session.update(account);
                operation.setStatus(Constants.TransferOperation.STATUS_COMPLETED);
                result.append(String.format(Constants.Messages.OPERATION_PROCESSED, session.save(operation)));
            } else {
                result.append(String.format(Constants.Messages.ACCOUNT_NOT_FOUND, operation.getAccountId()));
            }
        });
        return result.toString();
    }

    @Override
    public String procceedWithdraw(final TransferOperation operation) throws SQLException {
        final StringBuilder result = new StringBuilder();
        executeInTransaction(session -> {
            Account account = session.get(Account.class, operation.getAccountId());
            if (account == null) {
                result.append(String.format(Constants.Messages.ACCOUNT_NOT_FOUND, operation.getAccountId()));
                return;
            }

            double balance = account.getBalance();
            double sum = operation.getSum();
            if (balance >= sum) {
                account.setBalance(balance - sum);
                session.update(account);
                operation.setStatus(Constants.TransferOperation.STATUS_COMPLETED);
                result.append(String.format(Constants.Messages.OPERATION_PROCESSED, session.save(operation)));
            } else {
                operation.setStatus(Constants.TransferOperation.STATUS_FAILED);
                operation.setDetails(Constants.Messages.INSUFFICIENT_FUNDS_DETAILS);
                result.append(String.format(Constants.Messages.OPERATION_NOT_COMPLETED, session.save(operation), Constants.Messages.INSUFFICIENT_FUNDS_DETAILS));
            }
        });
        return result.toString();
    }
}

