package demo.engine;

import demo.engine.exeption.OperationException;
import demo.engine.model.Account;
import demo.engine.model.Operation;
import demo.storage.*;
import lombok.extern.slf4j.Slf4j;
import org.h2.util.StringUtils;

import java.util.*;
import java.util.concurrent.*;

@Slf4j
public class BuisnessOperationExecutor {

    private List<ExecutorService> queues;
    private int executorNumber;
    private DBManager dbManager;

    public BuisnessOperationExecutor(int executorNumber) {
        this.executorNumber = executorNumber;
        dbManager = new HibernateDBManager();
        queues = new ArrayList<>(executorNumber);
        for (int i = 0; i < executorNumber; i++) {
            queues.add(Executors.newSingleThreadExecutor());
        }
    }

    public BuisnessOperationExecutor(int executorNumber, DBManager dbManager) {
        this.executorNumber = executorNumber;
        this.dbManager = dbManager;
        queues = new ArrayList<>(executorNumber);
        for (int i = 0; i < executorNumber; i++) {
            queues.add(Executors.newSingleThreadExecutor());
        }
    }

    public Account getAccount(String accountId) throws OperationException {
        return Account.fillFromEntity(new Account(), dbManager.getAccountById(accountId));
    }

    public Operation getOperation(String operationId) throws OperationException {
        return dbManager.getTransferOperationById(operationId);
    }

    public List<Operation> getOperationsForAccount(String accountId) {
        return dbManager.getLastTransferOperations(accountId);
    }

    protected void validateOperation (Operation operation) throws OperationException {
        //account
        if (StringUtils.isNullOrEmpty(operation.getAccountId())) {
           throw new OperationException("Account id is empty");
        }
        if (operation.getType() == null) {
            throw new OperationException("Operation type id is empty");
        }
        //sum
        if (operation.getSum() == null) {
            throw new OperationException("Sum is empty");
        }
        if (operation.getSum() < 0) {
            throw new OperationException("Sum is below zero");
        }
        //transfer account
        if (operation.getType() == OperationType.TRANSFER) {
            if (StringUtils.isNullOrEmpty(operation.getAccountId())) {
                throw new OperationException("Transfer account id is empty");
            }
            if (operation.getAccountId().equals(operation.getTransferAccountId())) {
                throw new OperationException("Transfer to the same account");
            }
        }
    }

    protected void validateAccount (Account account, boolean isNew) throws OperationException {
        if (StringUtils.isNullOrEmpty(account.getName())) {
            throw new OperationException("Name is empty");
        }
        if (isNew) {
            if (account.getBalance() == null) {
                throw new OperationException("Balance is empty");
            }
            if (account.getBalance() < 0) {
                throw new OperationException("Balance below zero");
            }
        } else {
            if (StringUtils.isNullOrEmpty(account.getId())) {
                throw new OperationException("Account id is empty");
            }
        }
    }

    public Future<Account> createAccount(Account account) throws OperationException {
        validateAccount(account, true);
        ExecutorService service = queues.get(Math.abs(account.hashCode()) % executorNumber);
        return service.submit(() -> dbManager.addAccount(account));
    }

    public Future<Account> updateAccount(Account account) throws OperationException {
        validateAccount(account, false);
        ExecutorService service = queues.get(Math.abs(account.getId().hashCode()) % executorNumber);
        return service.submit(() -> dbManager.updateAccount(account));
    }

    public Future<Operation> transferMoney(Operation operation) throws OperationException {
        Future<Operation> response = null;
        validateOperation(operation);
        log.info(String.valueOf(Math.abs(operation.getAccountId().hashCode()) % executorNumber));
        ExecutorService service = queues.get(Math.abs(operation.getAccountId().hashCode()) % executorNumber);
        switch (operation.getType()) {
            case WITHDRAW:
                response = service.submit(() -> dbManager.withdraw(operation));
                break;
            case TOP_UP_BALANCE:
                response = service.submit(() -> dbManager.topUp(operation));
                break;
            case TRANSFER:
                response = service.submit(() -> dbManager.transfer(operation));
                break;
        }
        return response;
    }
}
