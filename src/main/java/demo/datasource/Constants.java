package demo.datasource;

/**
 * Created by rodio on 3/20/2018.
 */
public class Constants {

    public interface Messages {
        String REQUIRED_PARAMS_NOT_PRESENT = "Required parameters are not present.";
        String ONLY_BALANCE_OPERATIONS = "Only balance operations can change balance";
        String INSUFFICIENT_FUNDS_DETAILS = "insufficient funds";
        String ACCOUNT_NOT_FOUND = "Account %s not found";
        String OPERATION_NOT_FOUND = "Operation %s not found";
        String ACCOUNT_DELETED = "Account %s sucessfully deleted";
        String ACCOUNT_UPDATED = "Account %s sucessfully updated";
        String OPERATION_PROCESSED = "Operation %s successfully processed";
        String OPERATION_NOT_COMPLETED = "Operation %s not completed. Detailes: %s";
    }

    public interface Request {

        String RESULT = "result";
        String TYPE = "type";
        String CODE = "code";

    }

    public interface Account {
         String FIRST_NAME_FIELD = "first_name";
         String LAST_NAME_FIELD = "last_name";
         String CREATED_WHEN_FIELD = "created_when";
         String BALANCE_FIELD = "balance";
         String ID_FIELD = "id";
    }

    public interface TransferOperation {
        String CREATED_WHEN_FIELD = "created_when";
        String TYPE_FIELD = "type";
        String ACCOUNT_ID_FIELD = "account_id";
        String TRANSFER_ACCOUNT_ID_FIELD = "transfer_account_id";
        String SUM_FIELD = "sum";
        String DESCRIPTION_FIELD = "description";
        String STATUS_FIELD = "status";
        String DETAILS_FIELD = "details";
        String ID_FIELD = "id";

        String STATUS_COMPLETED = "completed";
        String STATUS_FAILED = "failed";
        String LAST_OPERATION_NUMBER = "last";
    }
}
