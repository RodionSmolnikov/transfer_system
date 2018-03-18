package demo.engine;

import demo.datasource.DBManager;
import demo.datasource.HibernateDBManager;
import demo.datasource.QueueRequest;

import java.util.HashMap;
import java.util.Map;

public class BuisnessOperationExecutor {

    volatile private int currentRequestCounter = 0;
    volatile private int lastExecutedRequest = 0;
    volatile static private RequestDispatcher instance = null;
    private DBManager dbManager;

    public void executeRequestByQueueNumbers(int startNubmer, int endNumber) {
    }

    public void queueAccountChangeRequest(QueueRequest request) {

    }

    public void queueBuisynessChangeRequest(QueueRequest request) {

    }

    /**
     * Singleton point of entering for change requests. Synchronization to keep data integrity
     * @param operation - buisness operation type
     * @param request - pojo with request field
     * @return number of the request in the queue
     */
    public synchronized int queueChangeRequest(Operations operation, QueueRequest request) {
        request.setId(currentRequestCounter);
        switch (operation) {
            case CREATE_ACCOUNT:
            case DELETE_ACCOUNT:
                queueAccountChangeRequest(request);
                break;
            case TRANSFER:
            case WITHDRAW:
            case TOP_UP_BALANCE:
                queueBuisynessChangeRequest(request);
        }
        return currentRequestCounter++;
    }

    /**
     * Singleton point of entering for change requests. Synchronization to keep data integrity
     * @param queueNumber - number of response (equals to request nubmer) REST action is waited by.
     * @return response for the request
     */
    public synchronized String getResultByQueueNumber(int queueNumber) {
        executeRequestByQueueNumbers(lastExecutedRequest, queueNumber);
        lastExecutedRequest = queueNumber;
        return dbManager.getResponceForQueueNumber(queueNumber);
    }

    private enum Operations {

        //change operations
        CREATE_ACCOUNT("create_account"),
        DELETE_ACCOUNT("delete_account"),
        TOP_UP_BALANCE("top_up_balance"),
        WITHDRAW("withdraw"),
        TRANSFER("transfer");

        private static Map<String, Operations> revertMapping = new HashMap<String, Operations>() {
            {put("create_account", CREATE_ACCOUNT);
                put("delete_account", DELETE_ACCOUNT);
                put("top_up_balance", TOP_UP_BALANCE);
                put("withdraw", WITHDRAW);
                put("transfer", TRANSFER);
            }
        };

        public String dbRepresentation;

        Operations (String dbRepresentation) {
            this.dbRepresentation = dbRepresentation;
        }

        Operations getOperationByName (String dbRepresentation) {
            if (dbRepresentation == null) return null;
            return revertMapping.getOrDefault(dbRepresentation, null);
        }

    }

}
