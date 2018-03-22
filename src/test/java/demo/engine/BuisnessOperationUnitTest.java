package demo.engine;

import demo.datasource.Account;
import demo.datasource.Constants;
import demo.datasource.DBManager;
import demo.datasource.TransferOperation;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.Before;
import org.junit.Test;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class BuisnessOperationUnitTest {

    BuisnessOperationExecutor executor;
    DBManager manager;
    ConcurrentNavigableMap<Long, Map<String,Object>> requestQueue;

    @Before
    public void before() {
        manager = mock(DBManager.class);
        requestQueue = new ConcurrentSkipListMap<>();
        executor = new BuisnessOperationExecutor(manager, requestQueue);
    }

    @Test
    public void createAccountTest() throws Exception {
        when(manager.addAccount(any())).thenReturn("Success");

        Map<String, Object> params = new HashMap<>();

        //not enough params
        executor.createAccount(params);
        assertEquals(HttpStatus.Code.BAD_REQUEST, params.get(Constants.Request.CODE));

        params.clear();
        params.put(Constants.Account.FIRST_NAME_FIELD, "Test");
        executor.createAccount(params);
        assertEquals(HttpStatus.Code.BAD_REQUEST, params.get(Constants.Request.CODE));

        params.clear();
        params.put(Constants.Account.BALANCE_FIELD, 100.0);
        executor.createAccount(params);
        assertEquals(HttpStatus.Code.BAD_REQUEST, params.get(Constants.Request.CODE));

        //not enough params
        params.clear();
        params.put(Constants.Account.FIRST_NAME_FIELD, "Test");
        params.put(Constants.Account.BALANCE_FIELD, 100.0);
        executor.createAccount(params);
        assertEquals(HttpStatus.Code.CREATED, params.get(Constants.Request.CODE));

        params.clear();
        params.put(Constants.Account.FIRST_NAME_FIELD, "Test");
        params.put(Constants.Account.LAST_NAME_FIELD, "Test");
        params.put(Constants.Account.BALANCE_FIELD, 100.0);
        executor.createAccount(params);
        assertEquals(HttpStatus.Code.CREATED, params.get(Constants.Request.CODE));

        verify(manager, times(2)).addAccount(any());

        //sql error
        when(manager.addAccount(any())).thenThrow(new SQLException());
        params.clear();
        params.put(Constants.Account.FIRST_NAME_FIELD, "Test");
        params.put(Constants.Account.LAST_NAME_FIELD, "Test");
        params.put(Constants.Account.BALANCE_FIELD, 100.0);
        executor.createAccount(params);
        assertEquals(HttpStatus.Code.INTERNAL_SERVER_ERROR, params.get(Constants.Request.CODE));
    }

    @Test
    public void updateAccountTest() throws Exception {
        when(manager.updateAccount(any())).thenReturn("Success");

        Map<String, Object> params = new HashMap<>();

        //not enough params
        executor.updateAccount(params);
        assertEquals(HttpStatus.Code.BAD_REQUEST, params.get(Constants.Request.CODE));

        //balance can be changed only by transfer operations
        params.clear();
        params.put(Constants.Account.ID_FIELD, "Test");
        params.put(Constants.Account.BALANCE_FIELD, 100.0);
        executor.updateAccount(params);
        assertEquals(HttpStatus.Code.BAD_REQUEST, params.get(Constants.Request.CODE));

        //enough params
        params.clear();
        params.put(Constants.Account.ID_FIELD, "Test");
        executor.updateAccount(params);
        assertEquals(HttpStatus.Code.OK, params.get(Constants.Request.CODE));

        params.clear();
        params.put(Constants.Account.ID_FIELD, "Test");
        params.put(Constants.Account.FIRST_NAME_FIELD, "Test");
        params.put(Constants.Account.LAST_NAME_FIELD, "Test");
        executor.updateAccount(params);
        assertEquals(HttpStatus.Code.OK, params.get(Constants.Request.CODE));

        verify(manager, times(2)).updateAccount(any());

        //sql error
        when(manager.updateAccount(any())).thenThrow(new SQLException());
        params.clear();
        params.put(Constants.Account.ID_FIELD, "Test");
        executor.updateAccount(params);
        assertEquals(HttpStatus.Code.INTERNAL_SERVER_ERROR, params.get(Constants.Request.CODE));
    }

    @Test
    public void deleteAccountTest() throws Exception {
        when(manager.deleteAccount(any())).thenReturn("Success");

        Map<String, Object> params = new HashMap<>();

        //not enough params
        executor.deleteAccount(params);
        assertEquals(HttpStatus.Code.BAD_REQUEST, params.get(Constants.Request.CODE));

        //enough params
        params.clear();
        params.put(Constants.Account.ID_FIELD, "Test");
        executor.deleteAccount(params);
        assertEquals(HttpStatus.Code.OK, params.get(Constants.Request.CODE));

        verify(manager, times(1)).deleteAccount(any());

        //sql error
        when(manager.deleteAccount(any())).thenThrow(new SQLException());
        params.clear();
        params.put(Constants.Account.ID_FIELD, "Test");
        executor.deleteAccount(params);
        assertEquals(HttpStatus.Code.INTERNAL_SERVER_ERROR, params.get(Constants.Request.CODE));
    }

    @Test
    public void getAccountTest() throws Exception {
        Account account = new Account();
        when(manager.getAccountById(any())).thenReturn(account);

        Map<String, Object> params;

        //not enough params
        params = executor.getAccount(null);
        assertEquals(HttpStatus.Code.BAD_REQUEST, params.get(Constants.Request.CODE));

        //enough params
        params = executor.getAccount("testId");
        assertEquals(HttpStatus.Code.OK, params.get(Constants.Request.CODE));

        verify(manager, times(1)).getAccountById(any());

        //sql error
        when(manager.getAccountById(any())).thenThrow(new SQLException());
        params = executor.getAccount("test");
        assertEquals(HttpStatus.Code.INTERNAL_SERVER_ERROR, params.get(Constants.Request.CODE));
    }

    @Test
    public void getOpearationTest() throws Exception {
        when(manager.getTransferOperationById(any())).thenReturn(new TransferOperation());

        Map<String, Object> params;

        //not enough params
        params = executor.getOperation(null);
        assertEquals(HttpStatus.Code.BAD_REQUEST, params.get(Constants.Request.CODE));

        //enough params
        params = executor.getOperation("testId");
        assertEquals(HttpStatus.Code.OK, params.get(Constants.Request.CODE));

        verify(manager, times(1)).getTransferOperationById(any());

        //sql error
        when(manager.getTransferOperationById(any())).thenThrow(new SQLException());
        params = executor.getOperation("test");
        assertEquals(HttpStatus.Code.INTERNAL_SERVER_ERROR, params.get(Constants.Request.CODE));
    }

    @Test
    public void topUpBalanceTest() throws Exception {
        when(manager.procceedTopUp(any())).thenReturn("Success");

        Map<String, Object> params = new HashMap<>();

        //not enough params
        executor.topUpBalance(params);
        assertEquals(HttpStatus.Code.BAD_REQUEST, params.get(Constants.Request.CODE));

        params.clear();
        params.put(Constants.TransferOperation.ACCOUNT_ID_FIELD, "Test");
        executor.topUpBalance(params);
        assertEquals(HttpStatus.Code.BAD_REQUEST, params.get(Constants.Request.CODE));

        //enough params
        params.clear();
        params.put(Constants.TransferOperation.ACCOUNT_ID_FIELD, "Test");
        params.put(Constants.TransferOperation.SUM_FIELD, 100.0);
        executor.topUpBalance(params);
        assertEquals(HttpStatus.Code.OK, params.get(Constants.Request.CODE));

        params.clear();
        params.put(Constants.TransferOperation.ACCOUNT_ID_FIELD, "Test");
        params.put(Constants.TransferOperation.SUM_FIELD, 100.0);
        params.put(Constants.TransferOperation.DESCRIPTION_FIELD, "Test");
        executor.topUpBalance(params);
        assertEquals(HttpStatus.Code.OK, params.get(Constants.Request.CODE));

        verify(manager, times(2)).procceedTopUp(any());

        //sql error
        when(manager.procceedTopUp(any())).thenThrow(new SQLException());
        params.clear();
        params.put(Constants.TransferOperation.ACCOUNT_ID_FIELD, "Test");
        params.put(Constants.TransferOperation.SUM_FIELD, 100.0);
        executor.topUpBalance(params);
        assertEquals(HttpStatus.Code.INTERNAL_SERVER_ERROR, params.get(Constants.Request.CODE));
    }

    @Test
    public void withdrawTest() throws Exception {
        when(manager.procceedWithdraw(any())).thenReturn("Success");

        Map<String, Object> params = new HashMap<>();

        //not enough params
        executor.withdraw(params);
        assertEquals(HttpStatus.Code.BAD_REQUEST, params.get(Constants.Request.CODE));

        params.clear();
        params.put(Constants.TransferOperation.ACCOUNT_ID_FIELD, "Test");
        executor.withdraw(params);
        assertEquals(HttpStatus.Code.BAD_REQUEST, params.get(Constants.Request.CODE));

        //enough params
        params.clear();
        params.put(Constants.TransferOperation.ACCOUNT_ID_FIELD, "Test");
        params.put(Constants.TransferOperation.SUM_FIELD, 100.0);
        executor.withdraw(params);
        assertEquals(HttpStatus.Code.OK, params.get(Constants.Request.CODE));

        params.clear();
        params.put(Constants.TransferOperation.ACCOUNT_ID_FIELD, "Test");
        params.put(Constants.TransferOperation.SUM_FIELD, 100.0);
        params.put(Constants.TransferOperation.DESCRIPTION_FIELD, "Test");
        executor.withdraw(params);
        assertEquals(HttpStatus.Code.OK, params.get(Constants.Request.CODE));

        verify(manager, times(2)).procceedWithdraw(any());

        //sql error
        when(manager.procceedWithdraw(any())).thenThrow(new SQLException());
        params.clear();
        params.put(Constants.TransferOperation.ACCOUNT_ID_FIELD, "Test");
        params.put(Constants.TransferOperation.SUM_FIELD, 100.0);
        executor.withdraw(params);
        assertEquals(HttpStatus.Code.INTERNAL_SERVER_ERROR, params.get(Constants.Request.CODE));
    }

    @Test
    public void transferTest() throws Exception {
        when(manager.procceedTransfer(any())).thenReturn("Success");

        Map<String, Object> params = new HashMap<>();

        //not enough params
        executor.transfer(params);
        assertEquals(HttpStatus.Code.BAD_REQUEST, params.get(Constants.Request.CODE));
        params.clear();
        params.put(Constants.TransferOperation.ACCOUNT_ID_FIELD, "Test");
        executor.withdraw(params);
        assertEquals(HttpStatus.Code.BAD_REQUEST, params.get(Constants.Request.CODE));

        //same account
        params.clear();
        params.put(Constants.TransferOperation.ACCOUNT_ID_FIELD, "Test");
        params.put(Constants.TransferOperation.TRANSFER_ACCOUNT_ID_FIELD, "Test");
        params.put(Constants.TransferOperation.SUM_FIELD, 100.0);
        executor.transfer(params);
        assertEquals(HttpStatus.Code.BAD_REQUEST, params.get(Constants.Request.CODE));

        //enough params
        params.clear();
        params.put(Constants.TransferOperation.ACCOUNT_ID_FIELD, "Test");
        params.put(Constants.TransferOperation.TRANSFER_ACCOUNT_ID_FIELD, "Test1");
        params.put(Constants.TransferOperation.SUM_FIELD, 100.0);
        params.put(Constants.TransferOperation.DESCRIPTION_FIELD, "Test");
        executor.transfer(params);
        assertEquals(HttpStatus.Code.OK, params.get(Constants.Request.CODE));

        verify(manager, times(1)).procceedTransfer(any());

        //sql error
        when(manager.procceedTransfer(any())).thenThrow(new SQLException());
        params.clear();
        params.put(Constants.TransferOperation.ACCOUNT_ID_FIELD, "Test");
        params.put(Constants.TransferOperation.TRANSFER_ACCOUNT_ID_FIELD, "Test1");
        params.put(Constants.TransferOperation.SUM_FIELD, 100.0);
        params.put(Constants.TransferOperation.DESCRIPTION_FIELD, "Test");
        executor.transfer(params);
        assertEquals(HttpStatus.Code.INTERNAL_SERVER_ERROR, params.get(Constants.Request.CODE));
    }

    @Test
    public void getOpearationsTest() throws Exception {
        when(manager.getLastTransferOperations(any(), anyInt())).thenReturn(new ArrayList<>());

        Map<String, Object> params = new HashMap<>();

        //not enough params
        executor.getOperations(params);
        assertEquals(HttpStatus.Code.BAD_REQUEST, params.get(Constants.Request.CODE));

        //enough params
        params.clear();
        params.put(Constants.TransferOperation.ACCOUNT_ID_FIELD, "test");
        params.put(Constants.TransferOperation.LAST_OPERATION_NUMBER, 3);
        executor.getOperations(params);
        assertEquals(HttpStatus.Code.OK, params.get(Constants.Request.CODE));

        verify(manager, times(1)).getLastTransferOperations(any(), anyInt());

        //sql error
        when(manager.getLastTransferOperations(any(), anyInt())).thenThrow(new SQLException());
        executor.getOperations(params);
        assertEquals(HttpStatus.Code.INTERNAL_SERVER_ERROR, params.get(Constants.Request.CODE));
    }
}