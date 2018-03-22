package demo.engine;

import demo.datasource.*;
import org.eclipse.jetty.http.HttpStatus;

import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;

public class BuisnessOperationExecutor {

    private static final int defaultLastOperationNumberList = 10;
    volatile private long currentRequestCounter = 0;
    volatile private long lastExecutedRequest = -1;
    volatile private ConcurrentNavigableMap<Long, Map<String,Object>> requestQueue;

    private static BuisnessOperationExecutor instance = null;

    private DBManager dbManager;

    private BuisnessOperationExecutor() {
        this.requestQueue = new ConcurrentSkipListMap<>();
        this.dbManager = new HibernateDBManager();
    }

    /**
     * Constructor for tests
     */
    protected BuisnessOperationExecutor(DBManager manager, ConcurrentNavigableMap<Long, Map<String,Object>> requestQueue) {
        this.requestQueue = requestQueue;
        this.dbManager = manager;
    }

    //singletone a core api interface
    public static synchronized BuisnessOperationExecutor getInstance() {
         if (instance == null) {
              instance = new BuisnessOperationExecutor();
         }
         return instance;
    }

    /**
     * validate params and create account in db
     * @param params - request params (first_name and balance required)
     * @return request result
     */
    public Map<String, Object> createAccount(Map<String, Object> params) {
        if (!params.containsKey(Constants.Account.FIRST_NAME_FIELD) ||
                !params.containsKey(Constants.Account.BALANCE_FIELD)) {
            params.put(Constants.Request.RESULT,  Constants.Messages.REQUIRED_PARAMS_NOT_PRESENT + " Needed " + Constants.Account.BALANCE_FIELD
                    + ", " + Constants.Account.FIRST_NAME_FIELD);
            params.put(Constants.Request.CODE, HttpStatus.Code.BAD_REQUEST);
            return params;
        }

        params.put(Constants.Account.CREATED_WHEN_FIELD, Calendar.getInstance().getTime());
        long requestNumber = queueChangeRequest(Operations.CREATE_ACCOUNT, params);
        return getResultByQueueNumber(requestNumber);
    }

    /**
     * validate params and update account params except balance
     * @param params - request params (first_name and balance required)
     * @return request result
     */
    public Map<String, Object> updateAccount(Map<String, Object> params) {
        if (!params.containsKey(Constants.Account.ID_FIELD)) {
            params.put(Constants.Request.RESULT,  Constants.Messages.REQUIRED_PARAMS_NOT_PRESENT + " Needed " + Constants.Account.ID_FIELD);
            params.put(Constants.Request.CODE, HttpStatus.Code.BAD_REQUEST);
            return params;
        }

        if (params.containsKey(Constants.Account.BALANCE_FIELD)) {
            params.put(Constants.Request.RESULT,  Constants.Messages.ONLY_BALANCE_OPERATIONS);
            params.put(Constants.Request.CODE, HttpStatus.Code.BAD_REQUEST);
            return params;
        }

        params.put(Constants.Account.CREATED_WHEN_FIELD, Calendar.getInstance().getTime());
        long requestNumber = queueChangeRequest(Operations.UPDATE_ACCOUNT, params);
        return getResultByQueueNumber(requestNumber);
    }

    /**
     * validate params and delete account in db
     * @param params - request params (account id required)
     * @return request result
     */
    public Map<String, Object> deleteAccount(Map<String, Object> params) {
        if (!params.containsKey(Constants.Account.ID_FIELD)) {
            params.put(Constants.Request.RESULT,  Constants.Messages.REQUIRED_PARAMS_NOT_PRESENT + " Needed " + Constants.Account.ID_FIELD);
            params.put(Constants.Request.CODE,  HttpStatus.Code.BAD_REQUEST);
            return params;
        }

        long requestNumber = queueChangeRequest(Operations.DELETE_ACCOUNT, params);
        return getResultByQueueNumber(requestNumber);
    }

    /**
     * return string representation of the account
     * @param accountId - account id
     * @return request result
     */
    public Map<String, Object> getAccount(String accountId) {
        Map<String, Object> result = new HashMap<>();
        if (accountId == null) {
            result.put(Constants.Request.RESULT,  Constants.Messages.REQUIRED_PARAMS_NOT_PRESENT + " Needed " + Constants.Account.ID_FIELD);
            result.put(Constants.Request.CODE,  HttpStatus.Code.BAD_REQUEST);
            return result;
        }

        Account account = null;
        try {
            account = dbManager.getAccountById(accountId);
        } catch (SQLException e) {
            result.put(Constants.Request.RESULT, e.getMessage());
            result.put(Constants.Request.CODE, HttpStatus.Code.INTERNAL_SERVER_ERROR);
            return result;
        }

        if (account == null) {
            result.put(Constants.Request.RESULT, String.format(Constants.Messages.ACCOUNT_NOT_FOUND, accountId));
            result.put(Constants.Request.CODE, HttpStatus.Code.BAD_REQUEST);
        } else {
            result.put(Constants.Request.RESULT, account.toString());
            result.put(Constants.Request.CODE, HttpStatus.Code.OK);
        }
        return result;
    }

    /**
     * return string representation of the Transfer Operation
     * @param operationId - account id
     * @return request result
     */
    public Map<String, Object> getOperation(String operationId) {
        Map<String, Object> result = new HashMap<>();
        if (operationId == null) {
            result.put(Constants.Request.RESULT,  Constants.Messages.REQUIRED_PARAMS_NOT_PRESENT + " Needed " + Constants.TransferOperation.ID_FIELD);
            result.put(Constants.Request.CODE,  HttpStatus.Code.BAD_REQUEST);
            return result;
        }

        TransferOperation operation = null;
        try {
            operation = dbManager.getTransferOperationById(operationId);
        } catch (SQLException e) {
            result.put(Constants.Request.RESULT, e.getMessage());
            result.put(Constants.Request.CODE, HttpStatus.Code.INTERNAL_SERVER_ERROR);
            return result;
        }

        if (operation == null) {
            result.put(Constants.Request.RESULT, String.format(Constants.Messages.OPERATION_NOT_FOUND, operationId));
            result.put(Constants.Request.CODE, HttpStatus.Code.BAD_REQUEST);
        } else {
            result.put(Constants.Request.RESULT, operation.toString());
            result.put(Constants.Request.CODE, HttpStatus.Code.OK);
        }
        return result;
    }

    /**
     * return string representation of the Transfer Operation
     * @param params - search params, account id required
     * @return request result
     */
    public Map<String, Object> getOperations(Map<String, Object> params) {
        if (!params.containsKey(Constants.TransferOperation.ACCOUNT_ID_FIELD)) {
            params.put(Constants.Request.RESULT,  Constants.Messages.REQUIRED_PARAMS_NOT_PRESENT + " Needed " + Constants.TransferOperation.ACCOUNT_ID_FIELD);
            params.put(Constants.Request.CODE,  HttpStatus.Code.BAD_REQUEST);
            return params;
        }
        int last = defaultLastOperationNumberList;
        if (params.containsKey(Constants.TransferOperation.LAST_OPERATION_NUMBER)) {
            last = (Integer) params.get(Constants.TransferOperation.LAST_OPERATION_NUMBER);
            if (last <= 0) last = defaultLastOperationNumberList;
        }

        List<TransferOperation> operations = null;
        try{
            operations = dbManager.getLastTransferOperations(
                    (String) params.get(Constants.TransferOperation.ACCOUNT_ID_FIELD),
                    last);
        } catch (SQLException e) {
            params.put(Constants.Request.RESULT, e.getMessage());
            params.put(Constants.Request.CODE, HttpStatus.Code.INTERNAL_SERVER_ERROR);
            return params;
        }

        params.put(Constants.Request.RESULT, formatOperationList(operations, (String) params.get(Constants.TransferOperation.ACCOUNT_ID_FIELD)));
        params.put(Constants.Request.CODE, HttpStatus.Code.OK);

        return params;
    }

    private String formatOperationList(List<TransferOperation> operations, final String accountId) {
        final StringBuilder result = new StringBuilder();
        result.append("List Operation for ").append(accountId).append("\n");
        operations.stream().forEach(operation -> {
            String sign = null;
            String direction = null;
            switch (Operations.valueOf(operation.getType())) {
                case TOP_UP_BALANCE:
                    sign = "+";
                    break;
                case WITHDRAW:
                    sign = "-";
                    break;
                case TRANSFER:
                    if (operation.getAccountId().equals(accountId)) {
                        sign = "-";
                        direction = "to   " + operation.getTransferAccountId();
                    }
                    else {
                        sign = "+";
                        direction = "from " + operation.getAccountId();
                    }
            }

            result.append(operation.getId()).append(" ");
            result.append(String.format("%8s", sign + operation.getSum())).append(" ");
            result.append(String.format("%25s", operation.getCreatedWhen())).append(" ");
            result.append(String.format("%12s", operation.getStatus())).append(" ");
            if (operation.getTransferAccountId() != null)
                result.append(String.format("%42s", direction)).append("\n");
            else
                result.append("\n");

        });
        return result.toString();
    }



    /**
     * validate params and increase balance of the account
     * @param params - request params (account id and sum required)
     * @return request result
     */
    public Map<String, Object> topUpBalance(Map<String, Object> params) {
        if (!params.containsKey(Constants.TransferOperation.ACCOUNT_ID_FIELD)
         || !params.containsKey(Constants.TransferOperation.SUM_FIELD)) {
            params.put(Constants.Request.RESULT,  Constants.Messages.REQUIRED_PARAMS_NOT_PRESENT + " Needed " + Constants.TransferOperation.ACCOUNT_ID_FIELD
                                                                                                      + ", "  + Constants.TransferOperation.SUM_FIELD);
            params.put(Constants.Request.CODE,  HttpStatus.Code.BAD_REQUEST);
            return params;
        }

        if (params.containsKey(Constants.TransferOperation.TRANSFER_ACCOUNT_ID_FIELD)) {
            params.remove(Constants.TransferOperation.TRANSFER_ACCOUNT_ID_FIELD);
        }
        params.put(Constants.TransferOperation.TYPE_FIELD, Operations.TOP_UP_BALANCE);
        params.put(Constants.TransferOperation.CREATED_WHEN_FIELD,  Calendar.getInstance().getTime());

        long requestNumber = queueChangeRequest(Operations.TOP_UP_BALANCE, params);
        return getResultByQueueNumber(requestNumber);
    }

    /**
     * validate params and decrease balance of the account if it's possible
     * @param params - request params (account id and sum required)
     * @return request result
     */
    public Map<String, Object> withdraw(Map<String, Object> params) {
        if (!params.containsKey(Constants.TransferOperation.ACCOUNT_ID_FIELD)
                || !params.containsKey(Constants.TransferOperation.SUM_FIELD)) {
            params.put(Constants.Request.RESULT,  Constants.Messages.REQUIRED_PARAMS_NOT_PRESENT + " Needed " + Constants.TransferOperation.ACCOUNT_ID_FIELD
                    + ", "  + Constants.TransferOperation.SUM_FIELD);
            params.put(Constants.Request.CODE,  HttpStatus.Code.BAD_REQUEST);
            return params;
        }

        if (params.containsKey(Constants.TransferOperation.TRANSFER_ACCOUNT_ID_FIELD)) {
            params.remove(Constants.TransferOperation.TRANSFER_ACCOUNT_ID_FIELD);
        }

        params.put(Constants.TransferOperation.TYPE_FIELD, Operations.WITHDRAW);
        params.put(Constants.TransferOperation.CREATED_WHEN_FIELD,  Calendar.getInstance().getTime());

        long requestNumber = queueChangeRequest(Operations.WITHDRAW, params);
        return getResultByQueueNumber(requestNumber);
    }

    /**
     * validate params and move sum from account to transfer account if it's possible
     * @param params - request params (account id and sum required)
     * @return request result
     */
    public Map<String, Object> transfer(Map<String, Object> params) {
        if (!params.containsKey(Constants.TransferOperation.ACCOUNT_ID_FIELD)
                || !params.containsKey(Constants.TransferOperation.SUM_FIELD)
                || !params.containsKey(Constants.TransferOperation.TRANSFER_ACCOUNT_ID_FIELD)) {
            params.put(Constants.Request.RESULT,  Constants.Messages.REQUIRED_PARAMS_NOT_PRESENT + " Needed " + Constants.TransferOperation.ACCOUNT_ID_FIELD
                    + ", "  + Constants.TransferOperation.SUM_FIELD
                    + ", "  + Constants.TransferOperation.TRANSFER_ACCOUNT_ID_FIELD);
            params.put(Constants.Request.CODE,  HttpStatus.Code.BAD_REQUEST);
            return params;
        }

        if (params.get(Constants.TransferOperation.ACCOUNT_ID_FIELD)
                .equals(params.get(Constants.TransferOperation.TRANSFER_ACCOUNT_ID_FIELD))) {
            params.put(Constants.Request.RESULT,  Constants.Messages.CANT_TRANSFER_TO_THE_SAME_ACCOUNT);
            params.put(Constants.Request.CODE,  HttpStatus.Code.BAD_REQUEST);
            return params;
        }

        params.put(Constants.TransferOperation.TYPE_FIELD, Operations.TRANSFER);
        params.put(Constants.TransferOperation.CREATED_WHEN_FIELD,  Calendar.getInstance().getTime());

        long requestNumber = queueChangeRequest(Operations.TRANSFER, params);
        return getResultByQueueNumber(requestNumber);
    }

    /**
     * main execution method - execute number of request since lastExecutedRequest to queueNumber
     * @param queueNumber - number of request in request queue execution will be performed to
     */
    protected synchronized void bulkExecuteRequestToNumber(long queueNumber) {
        //internal check
        if(queueNumber < lastExecutedRequest && requestQueue.containsKey(queueNumber)) {
            return;
        }

        final Map<Long, Map<String, Object>> requestsToExecute = requestQueue.subMap(lastExecutedRequest, false, queueNumber, true);
        requestsToExecute.keySet().stream().forEach(number -> {
            Map<String, Object> params  = requestsToExecute.get(number);
            String result = null;
            HttpStatus.Code code = null;
            try {
                switch ((Operations) params.get(Constants.Request.TYPE)) {
                    case CREATE_ACCOUNT:
                        result = dbManager.addAccount(fillAccount(params));
                        code = HttpStatus.Code.CREATED;
                        break;
                    case DELETE_ACCOUNT:
                        result = dbManager.deleteAccount((String)params.get(Constants.Account.ID_FIELD));
                        break;
                    case UPDATE_ACCOUNT:
                        result = dbManager.updateAccount(fillAccount(params));
                        break;
                    case TOP_UP_BALANCE:
                        result = dbManager.procceedTopUp(fillOperation(params));
                        break;
                    case WITHDRAW:
                        result = dbManager.procceedWithdraw(fillOperation(params));
                        break;
                    case TRANSFER:
                        result = dbManager.procceedTransfer(fillOperation(params));
                        break;
                }
            } catch (SQLException e) {
                result = e.getMessage();
                code = HttpStatus.Code.INTERNAL_SERVER_ERROR; /*internal server error*/
            }
            if (code == null) {
                if(result.contains("not found")) code = HttpStatus.Code.NOT_FOUND;
                else if(result.contains("not completed")) code = HttpStatus.Code.CONFLICT;
                else code = HttpStatus.Code.OK;
            }
            params.put(Constants.Request.RESULT, result);
            params.put(Constants.Request.CODE, code);
        });
        lastExecutedRequest = queueNumber;
    }

    private Account fillAccount(Map<String, Object> params) {
        Account account = new Account();
        params.keySet().stream().forEach(field -> {
            switch (field) {
                case (Constants.Account.ID_FIELD):
                    account.setId((String) params.get(field));
                    break;
                case (Constants.Account.CREATED_WHEN_FIELD):
                    account.setCreatedWhen((Date) params.get(field));
                    break;
                case (Constants.Account.FIRST_NAME_FIELD):
                    account.setFirstName((String) params.get(field));
                    break;
                case (Constants.Account.LAST_NAME_FIELD):
                    account.setLastName((String) params.get(field));
                    break;
                case (Constants.Account.BALANCE_FIELD):
                    account.setBalance((Double) params.get(field));
                    break;
            }
        });
        return account;
    }

    private TransferOperation fillOperation(Map<String, Object> params) {
        TransferOperation to = new TransferOperation();
        params.keySet().stream().forEach(field -> {
            switch (field) {
                case (Constants.TransferOperation.ID_FIELD):
                    to.setId((String) params.get(field));
                    break;
                case (Constants.TransferOperation.CREATED_WHEN_FIELD):
                    to.setCreatedWhen((Date) params.get(field));
                    break;
                case (Constants.TransferOperation.STATUS_FIELD):
                    to.setStatus((String) params.get(field));
                    break;
                case (Constants.TransferOperation.TYPE_FIELD):
                    to.setType(params.get(field).toString());
                    break;
                case (Constants.TransferOperation.ACCOUNT_ID_FIELD):
                    to.setAccountId((String) params.get(field));
                    break;
                case (Constants.TransferOperation.TRANSFER_ACCOUNT_ID_FIELD):
                    to.setTransferAccountId((String) params.get(field));
                    break;
                case (Constants.TransferOperation.SUM_FIELD):
                    to.setSum((Double) params.get(field));
                    break;
                case (Constants.TransferOperation.DESCRIPTION_FIELD):
                    to.setDescription((String) params.get(field));
                    break;
                case (Constants.TransferOperation.DETAILS_FIELD):
                    to.setDetails((String) params.get(field));
                    break;
            }
        });
        return to;
    }

    /**
     * Singleton point for change requests entering. Synchronization to keep data integrity
     * @param operation - buisness operation type
     * @param request - map with request params
     * @return number of the request in the queue
     */
    protected synchronized long queueChangeRequest(Operations operation, Map<String, Object> request) {
        request.put(Constants.Request.TYPE, operation);
        requestQueue.put(currentRequestCounter, request);
        return currentRequestCounter++;
    }

    /**
     * return result of REST request
     * @param queueNumber - number of response (equals to request nubmer) REST action is waited by.
     * @return response for the request
     */
    protected Map<String, Object> getResultByQueueNumber(long queueNumber) {
        //check before synchronization block
        String result;
        if(queueNumber >= lastExecutedRequest && requestQueue.containsKey(queueNumber)) {
            bulkExecuteRequestToNumber(queueNumber);
        }
        return requestQueue.remove(queueNumber);
    }

    public enum Operations {
        CREATE_ACCOUNT,
        DELETE_ACCOUNT,
        UPDATE_ACCOUNT,
        TOP_UP_BALANCE,
        WITHDRAW,
        TRANSFER;
    }
}
