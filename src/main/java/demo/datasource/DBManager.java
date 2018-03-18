package demo.datasource;

public interface DBManager {

    /**
     * Create rows in queue_requests and transfer_operation tables
     */
    void queueBalanceChangeRequest(QueueRequest request);

    void queueAccountChangeRequest(QueueRequest request);

    /**
     * Create db scheme
     */
    void initTables();

    /**
     * @param requestNumber number in queue_requests table
     * @return response field for the request with requestNumder
     */
    String getResponceForQueueNumber(int requestNumber);

}
