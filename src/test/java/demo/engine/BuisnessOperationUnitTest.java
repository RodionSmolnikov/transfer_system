package demo.engine;

import demo.engine.exeption.OperationException;
import demo.engine.model.Account;
import demo.engine.model.Operation;
import demo.storage.HibernateDBManager;
import demo.storage.DBManager;
import demo.storage.OperationType;
import org.junit.Before;
import org.junit.Test;

import java.sql.SQLException;

import static org.junit.Assert.*;

public class BuisnessOperationUnitTest {

    BuisnessOperationExecutor executor;
    DBManager manager;

    @Before
    public void before() {
        manager = new HibernateDBManager();
        executor = new BuisnessOperationExecutor(5);
    }

    @Test(expected = OperationException.class)
    public void createAccountTest_accountNameNull() throws Exception{
        Account a = new Account();
        a.setBalance(100);
        executor.createAccount(a).get();
    }

    @Test(expected = OperationException.class)
    public void createAccountTest_balanceNull() throws Exception {
        Account a = new Account();
        a.setName("testName");
        executor.createAccount(a).get();
    }

    @Test(expected = OperationException.class)
    public void createAccountTest_balanceBelowZero() throws Exception {
        Account a = new Account();
        a.setName("testName");
        a.setBalance(-50);
        executor.createAccount(a).get();
    }

    @Test
    public void createAccountTest() throws Exception {
        Account a = new Account();
        a.setName("testName");
        a.setBalance(100);
        a = executor.createAccount(a).get();

        assertNotNull(a.getCreatedWhen());
        assertNotNull(a.getId());
    }

    @Test(expected = OperationException.class)
    public void updateAccountTest_emptyName() throws Exception {
        Account a = new Account();
        a.setName("name");
        a.setBalance(100);
        a = executor.createAccount(a).get();

        a.setName("");
        executor.updateAccount(a).get();
    }

    @Test(expected = OperationException.class)
    public void updateAccountTest_emptyId() throws Exception {
        Account a = new Account();
        a.setName("name");
        a.setBalance(100);
        a = executor.createAccount(a).get();

        a.setId("");
        executor.updateAccount(a).get();
    }

    @Test
    public void updateAccountTest() throws Exception {
        Account a = new Account();
        a.setName("name");
        a.setBalance(100);
        a = executor.createAccount(a).get();

        a.setName("newName");
        a = executor.updateAccount(a).get();

        Account selected = executor.getAccount(a.getId());
        assertEquals("newName", selected.getName());
    }

    @Test(expected = OperationException.class)
    public void getAccountTest_notFound() throws Exception {
        executor.getAccount("NOT_EXIST_ID");
    }

    @Test(expected = OperationException.class)
    public void getOperationTest_notFound() throws Exception {
        executor.getOperation("NOT_EXIST_ID");
    }

    @Test
    public void topupTest() throws Exception {
        Account a = new Account();
        a.setName("name");
        a.setBalance(1000);
        a = executor.createAccount(a).get();

        Operation o = new Operation();
        o.setAccountId(a.getId());
        o.setSum(100);
        o.setType(OperationType.TOP_UP_BALANCE);

        executor.transferMoney(o).get();

        Account selected = executor.getAccount(a.getId());
        assertNotNull(selected);
        assertEquals(Integer.valueOf(1100), selected.getBalance());
    }

    @Test(expected = OperationException.class)
    public void topupTest_emptySum() throws Exception {
        Account a = new Account();
        a.setName("name");
        a.setBalance(1000);
        a = executor.createAccount(a).get();

        Operation o = new Operation();
        o.setAccountId(a.getId());
        o.setType(OperationType.TOP_UP_BALANCE);

        executor.transferMoney(o).get();
    }

    @Test(expected = OperationException.class)
    public void topupTest_sumBelowZero() throws Exception {
        Account a = new Account();
        a.setName("name");
        a.setBalance(1000);
        a = executor.createAccount(a).get();

        Operation o = new Operation();
        o.setAccountId(a.getId());
        o.setSum(-100);
        o.setType(OperationType.TOP_UP_BALANCE);

        executor.transferMoney(o).get();
    }

    @Test(expected = OperationException.class)
    public void topupTest_accountIdNull() throws Exception {
        Account a = new Account();
        a.setName("name");
        a.setBalance(1000);
        executor.createAccount(a).get();

        Operation o = new Operation();
        o.setSum(100);
        o.setType(OperationType.TOP_UP_BALANCE);

        executor.transferMoney(o).get();
    }

    @Test(expected = OperationException.class)
    public void operationTest_operationTypeNull() throws Exception {
        Account a = new Account();
        a.setName("name");
        a.setBalance(1000);
        executor.createAccount(a).get();

        Operation o = new Operation();
        o.setSum(100);

        executor.transferMoney(o).get();
    }

    @Test(expected = OperationException.class)
    public void withdrawTest_sumNull() throws Exception {
        Account a = new Account();
        a.setName("name");
        a.setBalance(1000);
        a = executor.createAccount(a).get();

        Operation o = new Operation();
        o.setAccountId(a.getId());
        o.setType(OperationType.WITHDRAW);

        executor.transferMoney(o).get();
    }

    @Test(expected = OperationException.class)
    public void withdrawTest_sumBelowZero() throws Exception {
        Account a = new Account();
        a.setName("name");
        a.setBalance(1000);
        a = executor.createAccount(a).get();

        Operation o = new Operation();
        o.setAccountId(a.getId());
        o.setSum(-100);
        o.setType(OperationType.WITHDRAW);

        executor.transferMoney(o).get();
    }

    @Test(expected = OperationException.class)
    public void withdrawTest_accountIdNull() throws Exception {
        Account a = new Account();
        a.setName("name");
        a.setBalance(1000);
        a = executor.createAccount(a).get();

        Operation o = new Operation();
        o.setSum(100);
        o.setType(OperationType.WITHDRAW);

        executor.transferMoney(o).get();
    }

    @Test
    public void withdrawTest() throws Exception {
        Account a = new Account();
        a.setName("name");
        a.setBalance(1000);
        a = executor.createAccount(a).get();

        Operation o = new Operation();
        o.setAccountId(a.getId());
        o.setSum(100);
        o.setType(OperationType.WITHDRAW);

        executor.transferMoney(o).get();

        Account selected = executor.getAccount(a.getId());
        assertNotNull(selected);
        assertEquals(Integer.valueOf(900), selected.getBalance());
    }

    @Test(expected = Exception.class)
    public void withdrawTest_lowBalance() throws Exception {
        Account a = new Account();
        a.setName("name");
        a.setBalance(10);
        a = executor.createAccount(a).get();

        Operation o = new Operation();
        o.setAccountId(a.getId());
        o.setSum(100);
        o.setType(OperationType.WITHDRAW);

        executor.transferMoney(o).get();
    }

    @Test
    public void transferTest() throws Exception {
        Account a = new Account();
        a.setName("name");
        a.setBalance(500);
        a = executor.createAccount(a).get();

        Account b = new Account();
        b.setName("name");
        b.setBalance(1000);
        b = executor.createAccount(b).get();

        Operation o = new Operation();
        o.setAccountId(a.getId());
        o.setTransferAccountId(b.getId());
        o.setSum(100);
        o.setType(OperationType.TRANSFER);

        executor.transferMoney(o).get();

        Account selected = executor.getAccount(a.getId());
        assertNotNull(selected);
        assertEquals(Integer.valueOf(400), selected.getBalance());

        selected = executor.getAccount(b.getId());
        assertNotNull(selected);
        assertEquals(Integer.valueOf(1100), selected.getBalance());
    }

    @Test(expected = Exception.class)
    public void transferTest_notEnoughForTransfer() throws Exception {
        Account a = new Account();
        a.setName("name");
        a.setBalance(500);
        a = executor.createAccount(a).get();

        Account b = new Account();
        b.setName("name");
        b.setBalance(1000);
        b = executor.createAccount(b).get();

        Operation o = new Operation();
        o.setAccountId(a.getId());
        o.setTransferAccountId(b.getId());
        o.setSum(600);
        o.setType(OperationType.TRANSFER);

        executor.transferMoney(o).get();
    }

    @Test(expected = Exception.class)
    public void transferTest_notExistAcc() throws Exception {
        Account a = new Account();
        a.setName("name");
        a.setBalance(500);
        a = executor.createAccount(a).get();

        Account b = new Account();
        b.setName("name");
        b.setBalance(1000);
        b = executor.createAccount(b).get();

        Operation o = new Operation();
        o.setAccountId("NOT_EXIST");
        o.setTransferAccountId(b.getId());
        o.setSum(100);
        o.setType(OperationType.TRANSFER);

        executor.transferMoney(o).get();
    }

    @Test(expected = Exception.class)
    public void transferTest_notExistTransAcc() throws Exception {
        Account a = new Account();
        a.setName("name");
        a.setBalance(500);
        a = executor.createAccount(a).get();

        Account b = new Account();
        b.setName("name");
        b.setBalance(1000);
        b = executor.createAccount(b).get();

        Operation o = new Operation();
        o.setAccountId(a.getId());
        o.setTransferAccountId("NOT_EXIST");
        o.setSum(100);
        o.setType(OperationType.TRANSFER);

        executor.transferMoney(o).get();
    }

    @Test
    public void sequenceTest() throws Exception {
        StringBuilder executionA = new StringBuilder();
        StringBuilder executionB = new StringBuilder();

        executor = new BuisnessOperationExecutor(2, new HibernateDBManager() {
            @Override
            public Operation topUp(Operation operation) throws SQLException, OperationException {
                if (operation.getDescription().startsWith("a")) {
                    executionA.append(operation.getDescription().substring(1));
                } else {
                    executionB.append(operation.getDescription().substring(1));
                }
                return operation;
            }
        });

        StringBuilder checkString = new StringBuilder();
        for (int i = 0; i < 100; i++) checkString.append(i);

        Account a = new Account();
        a.setName("name");
        a.setBalance(0);
        a = executor.createAccount(a).get();

        Account b = new Account();
        b.setName("name");
        b.setBalance(0);
        b = executor.createAccount(b).get();

        for (int i = 0; i < 100; i++) {
            Operation opA = new Operation();
            opA.setAccountId(a.getId());
            opA.setSum(100);
            opA.setType(OperationType.TOP_UP_BALANCE);

            Operation opB = new Operation();
            opB.setAccountId(b.getId());
            opB.setSum(100);
            opB.setType(OperationType.TOP_UP_BALANCE);

            opA.setDescription("a" + i);
            opB.setDescription("b" + i);
            executor.transferMoney(opA).get();
            executor.transferMoney(opB).get();
        }

        assertEquals(checkString.toString(), executionA.toString());
        assertEquals(checkString.toString(), executionB.toString());

    }
}