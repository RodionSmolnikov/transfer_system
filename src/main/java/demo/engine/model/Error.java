package demo.engine.model;

import demo.engine.exeption.OperationException;
import lombok.Data;

@Data
public class Error {

    public Error(Throwable e) {
        this.error = e.getClass().getName();
        this.details = e.getLocalizedMessage();
    }

    public Error(OperationException e) {
        this.error = e.getClass().getName();
        this.details = e.getLocalizedMessage();
        this.accountId = e.getAccountId();
    }

    String error;
    String details;
    String accountId;
}
