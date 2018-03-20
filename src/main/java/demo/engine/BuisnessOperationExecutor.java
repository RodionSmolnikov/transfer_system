package demo.engine;

import demo.datasource.*;
import org.eclipse.jetty.http.HttpStatus;

import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;

public class BuisnessOperationExecutor {

    volatile private long currentRequestCounter = 0;
    volatile private long lastExecutedRequest = -1;
    volatile private ConcurrentNavigableMap<Long, Map<String,Object>> requestQueue;

    private static BuisnessOperationExecutor instance = null;

    private DBManager dbManager;

    private BuisnessOperationExecutor() {
        this.requestQueue = new ConcurrentSkipListMap<>();
        this.dbManager = new HibernateDBManager();
    }

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
            params.put(Constants.Request.CODE, HttpStatus.Code.INTERNAL_SERVER_ERROR);
        }

        params.put(Constants.Account.CREATED_WHEN_FIELD, Calendar.getInstance().getTime());
        long requestNumber = queueChangeRequest(Operations.CREATE_ACCOUNT, params);
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

        params.put(Constants.TransferOperation.CREATED_WHEN_FIELD,  Calendar.getInstance().getTime());

        long requestNumber = queueChangeRequest(Operations.TOP_UP_BALANCE, params);
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
                        if (result.matches(".* not found")) {
                            code = HttpStatus.Code.NOT_FOUND;
                        } else {
                            code = HttpStatus.Code.OK;
                        }
                        break;
                    case UPDATE_ACCOUNT:
                        result = dbManager.updateAccount(fillAccount(params));
                        if (result.matches(".*not found"))
                            code = HttpStatus.Code.NOT_FOUND;
                        else
                            code = HttpStatus.Code.OK;
                        break;
                    case TOP_UP_BALANCE:
                        result = dbManager.procceedTopUp(fillOperation(params));
                    case WITHDRAW:
                    case TRANSFER:
                }
            } catch (SQLException e) {
                result = e.getMessage();
                code = HttpStatus.Code.INTERNAL_SERVER_ERROR; /*internal server error*/
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
    public synchronized long queueChangeRequest(Operations operation, Map<String, Object> request) {
        request.put(Constants.Request.TYPE, operation);
        requestQueue.put(currentRequestCounter, request);
        return currentRequestCounter++;
    }

    /**
     * return result of REST request
     * @param queueNumber - number of response (equals to request nubmer) REST action is waited by.
     * @return response for the request
     */
    public Map<String, Object> getResultByQueueNumber(long queueNumber) {
        //check before synchronization block
        String result;
        if(queueNumber >= lastExecutedRequest && requestQueue.containsKey(queueNumber)) {
            bulkExecuteRequestToNumber(queueNumber);
        }
        return requestQueue.remove(queueNumber);
    }

    private enum Operations {
        CREATE_ACCOUNT,
        DELETE_ACCOUNT,
        UPDATE_ACCOUNT,
        TOP_UP_BALANCE,
        WITHDRAW,
        TRANSFER
    }
}
