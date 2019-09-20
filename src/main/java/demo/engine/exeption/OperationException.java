package demo.engine.exeption;

import lombok.Data;

@Data
public class OperationException extends Exception {

    String accountId;

    public OperationException(String message, String accountId) {
        super(message);
        this.accountId = accountId;
    }

    public OperationException(String message) {
        super(message);
        this.accountId = null;
    }

}
