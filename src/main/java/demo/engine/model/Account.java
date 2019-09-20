package demo.engine.model;

import demo.storage.entities.AccountEntity;
import lombok.Data;
import lombok.ToString;

import java.util.Date;

@Data
@ToString
public class Account {
    String name;
    String id;
    Integer balance;
    Date createdWhen;

    public static AccountEntity fillToEntity (Account account, AccountEntity entity) {
        entity.setId(account.getId());
        entity.setBalance(account.getBalance());
        entity.setCreatedWhen(account.getCreatedWhen() == null ? new Date() : account.getCreatedWhen());
        entity.setName(account.getName());
        return entity;
    }

    public static Account fillFromEntity(Account account, AccountEntity entity) {
        account.setId(entity.getId());
        account.setName(entity.getName());
        account.setBalance(entity.getBalance());
        account.setCreatedWhen(entity.getCreatedWhen());
        return account;
    }
}


