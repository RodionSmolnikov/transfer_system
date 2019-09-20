package demo.storage;

import demo.engine.exeption.OperationException;
import demo.engine.model.Account;
import demo.engine.model.Operation;
import demo.storage.entities.AccountEntity;

import java.sql.SQLException;
import java.util.List;

public interface DBManager {

    Account updateAccount(Account account) throws SQLException, OperationException;

    Account addAccount(Account account) throws SQLException, OperationException;

    AccountEntity getAccountById(String accountId) throws OperationException;

    Operation transfer(Operation operation) throws SQLException, OperationException;

    Operation getTransferOperationById(String operationId) throws OperationException;

    List<Operation> getLastTransferOperations(String accountID);

    Operation topUp(Operation operation) throws SQLException, OperationException;

    Operation withdraw (Operation operation) throws SQLException, OperationException;
}
