package demo.engine.model;

import demo.storage.OperationType;
import demo.storage.entities.OperationEntity;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Date;

@Data
@ToString
@NoArgsConstructor
public class Operation {
    public String id;
    public String accountId;
    public Integer sum;
    public String transferAccountId;
    public OperationType type;
    public String description;
    public Date timestamp;

    public static OperationEntity fillToEntity (Operation operation, OperationEntity entity) {
        entity.setAccountId(operation.getAccountId());
        entity.setDescription(operation.getDescription());
        entity.setId(operation.getId());
        entity.setSum(operation.getSum());
        entity.setTimestamp(operation.getTimestamp() == null ? new Date() : operation.getTimestamp());
        entity.setType(operation.getType());
        entity.setTransferAccountId(operation.getTransferAccountId());
        return entity;
    }

    public static Operation fillFromEntity (Operation operation, OperationEntity entity) {
        operation.setAccountId(entity.getAccountId());
        operation.setDescription(entity.getDescription());
        operation.setId(entity.getId());
        operation.setSum(entity.getSum());
        operation.setTimestamp(entity.getTimestamp());
        operation.setType(entity.getType());
        operation.setTransferAccountId(entity.getTransferAccountId());
        return operation;
    }
}
