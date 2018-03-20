package demo.datasource;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

public interface DBManager {

    String updateAccount(Account account) throws SQLException;

    String addAccount(Account account) throws SQLException;

    String deleteAccount(String accountId) throws SQLException;

    Account getAccountById(String accountId) throws SQLException;

    String procceedTransfer(TransferOperation operation) throws SQLException;

    TransferOperation getTransferOperationById(String operationId) throws SQLException;

    List<TransferOperation> getTransferOperationsByDates(Date after, Date before, String accountID) throws SQLException;

    List<TransferOperation> getTransferOperationsSinceDate(Date after, String accountID) throws SQLException;

    String procceedTopUp(TransferOperation operation) throws SQLException;

    String procceedWithdraw (TransferOperation operation) throws SQLException;
}
