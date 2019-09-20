package demo.storage;

import demo.engine.exeption.OperationException;
import demo.engine.model.Account;
import demo.engine.model.Operation;
import demo.storage.entities.AccountEntity;
import demo.storage.entities.OperationEntity;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.*;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.query.Query;

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class HibernateDBManager implements DBManager {

    private static SessionFactory sessionFactory;

    private static final String SELECT_TRANSFER_OPERATIONS_BY_DATES = "FROM OperationEntity " +
            "where (ACCOUNT_ID = :accountId or TRANSFER_ACCOUNT_ID = :transfer_accountId) " +
            "order by TIMESTAMP desc";

    private SessionFactory buildSessionFactory() {
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

    protected SessionFactory getSessionFactory() {
        if(sessionFactory == null) sessionFactory = buildSessionFactory();
        return sessionFactory;
    }

    private void executeInTransaction(SQLOperation operation) throws OperationException, SQLException {
        final Session session = getSessionFactory().openSession();
        Transaction transaction = null;
        try {
            transaction = session.beginTransaction();
            operation.execute(session);
            transaction.commit();
        } catch (SQLException e) {
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

    @FunctionalInterface
    private interface SQLOperation {
        void execute(Session session) throws SQLException, OperationException;
    }

    @Override
    public Account updateAccount(final Account account) throws SQLException, OperationException {
        executeInTransaction(session -> {
            AccountEntity existingAccount = session.get(AccountEntity.class, account.getId());
            if (existingAccount == null) {
                throw new OperationException(Constants.Messages.ACCOUNT_NOT_FOUND, account.getId());
            }
            existingAccount.setName(account.getName());
            session.update(existingAccount);
            Account.fillFromEntity(account, existingAccount);
            log.info(String.format(Constants.Messages.ACCOUNT_UPDATED, account.getId()));
        });
        return account;
    }

    @Override
    public Account addAccount(final Account account) throws SQLException, OperationException {
        executeInTransaction(session -> {
            AccountEntity entity = new AccountEntity();
            account.setId((String) session.save(Account.fillToEntity(account, entity)));
            account.setCreatedWhen(entity.getCreatedWhen());
            log.info(String.format(Constants.Messages.ACCOUNT_CREATED, account.getId()));
            }
        );
        return account;
    }

    @Override
    public Operation getTransferOperationById(String id) throws OperationException {
        Session session = getSessionFactory().openSession();
        OperationEntity result = session.get(OperationEntity.class, id);
        if (result == null) {
            throw new OperationException(String.format(Constants.Messages.OPERATION_NOT_FOUND, id));
        }
        session.close();
        return Operation.fillFromEntity(new Operation(), result);
    }

    @Override
    public List<Operation> getLastTransferOperations(String accountId) {
        Session session = getSessionFactory().openSession();
        Query query =  session.createQuery(SELECT_TRANSFER_OPERATIONS_BY_DATES, OperationEntity.class);
        //100 last operations
        query.setMaxResults(100);
        query.setParameter("accountId", accountId);
        query.setParameter("transfer_accountId", accountId);
        List<OperationEntity> result = query.list();
        session.close();
        return result.stream()
                .map(entity -> Operation.fillFromEntity(new Operation(), entity))
                .collect(Collectors.toList());
    }

    @Override
    public AccountEntity getAccountById(String id) throws OperationException {
        Session session = getSessionFactory().openSession();
        AccountEntity result = session.get(AccountEntity.class, id);
        if (result == null) {
            throw new OperationException(Constants.Messages.ACCOUNT_NOT_FOUND, id);
        }
        session.close();
        return result;
    }

    @Override
    public Operation transfer(final Operation operation) throws SQLException, OperationException {
        executeInTransaction(session -> {
            AccountEntity account = session.get(AccountEntity.class, operation.getAccountId());
            if (account == null) {
                throw new OperationException(Constants.Messages.ACCOUNT_NOT_FOUND, operation.getAccountId());
            }

            AccountEntity transferAccount = session.get(AccountEntity.class, operation.getTransferAccountId());
            if (transferAccount == null) {
                throw new OperationException(Constants.Messages.ACCOUNT_NOT_FOUND, operation.getTransferAccountId());
            }

            Integer accountBalance = account.getBalance();
            Integer sum = operation.getSum();
            if (accountBalance >= sum) {
                account.setBalance(accountBalance - sum);
                transferAccount.setBalance(transferAccount.getBalance() + sum);
                session.update(account);
                session.update(transferAccount);
                OperationEntity entity = new OperationEntity();
                operation.setId((String) session.save(Operation.fillToEntity(operation, new OperationEntity())));
                operation.setTimestamp(entity.getTimestamp());
                log.info(String.format(Constants.Messages.OPERATION_PROCESSED, operation.getId()));
            } else {
                throw new OperationException(Constants.Messages.INSUFFICIENT_FUNDS_DETAILS, operation.getAccountId());
            }
        });
        return operation;
    }

    @Override
    public Operation topUp(final Operation operation) throws SQLException, OperationException {
        executeInTransaction(session -> {
            AccountEntity account = session.get(AccountEntity.class, operation.getAccountId());
            if (account != null) {
                account.setBalance(account.getBalance() + operation.getSum());
                session.update(account);
                OperationEntity entity = new OperationEntity();
                operation.setId((String) session.save(Operation.fillToEntity(operation, new OperationEntity())));
                operation.setTimestamp(entity.getTimestamp());
                log.info(String.format(Constants.Messages.OPERATION_PROCESSED, operation.getId()));
            } else {
                throw new OperationException(Constants.Messages.ACCOUNT_NOT_FOUND, operation.getAccountId());
            }
        });
        return operation;
    }

    @Override
    public Operation withdraw(Operation operation) throws SQLException, OperationException {
        executeInTransaction(session -> {
            AccountEntity account = session.get(AccountEntity.class, operation.getAccountId());
            if (account == null) {
                throw new OperationException(Constants.Messages.ACCOUNT_NOT_FOUND, operation.getAccountId());
            }
            Integer balance = account.getBalance();
            Integer sum = operation.getSum();
            if (balance >= sum) {
                account.setBalance(balance - sum);
                session.update(account);
                OperationEntity entity = new OperationEntity();
                operation.setId((String) session.save(Operation.fillToEntity(operation, new OperationEntity())));
                operation.setTimestamp(entity.getTimestamp());
                log.info(String.format(Constants.Messages.OPERATION_PROCESSED, operation.getId()));
            } else {
                throw new OperationException(Constants.Messages.INSUFFICIENT_FUNDS_DETAILS, operation.getAccountId());
            }
        });
        return operation;
    }
}

