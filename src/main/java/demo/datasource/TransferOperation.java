package demo.datasource;

import javax.persistence.*;
import java.util.Date;

/**
 * Table for saving transfer operation history
 */
@Entity
@Table(name = "transfer_operation", schema = "transfer")
public class TransferOperation {

    private String id;
    private Date createdWhen;
    private Date completeWhen;
    private String type;
    private String accountId;
    private String transferAccountId;
    private String summ;
    private String description;
    private String status;



    @Id
    @Column(name = "id", nullable = false, insertable = true, updatable = true, length = 36)
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_when", nullable = false, insertable = true, updatable = true)
    public Date getCreatedWhen() {
        return createdWhen;
    }

    public void setCreatedWhen(Date createdWhen) {
        this.createdWhen = createdWhen;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "completed_when", insertable = true, updatable = true)
    public Date getCompleteWhen() {
        return completeWhen;
    }

    public void setCompleteWhen(Date completeWhen) {
        this.completeWhen = completeWhen;
    }

    @Basic
    @Column(name = "type", nullable = false, insertable = true, updatable = true, length = 50)
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Basic
    @Column(name = "type", nullable = false, insertable = true, updatable = true, length = 50)
    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    @Basic
    @Column(name = "type", nullable = false, insertable = true, updatable = true, length = 50)
    public String getTransferAccountId() {
        return transferAccountId;
    }

    public void setTransferAccountId(String transferAccountId) {
        this.transferAccountId = transferAccountId;
    }

    @Basic
    @Column(name = "type", nullable = false, insertable = true, updatable = true, length = 50)
    public String getSumm() {
        return summ;
    }

    public void setSumm(String summ) {
        this.summ = summ;
    }

    @Basic
    @Column(name = "type", nullable = false, insertable = true, updatable = true, length = 50)
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Basic
    @Column(name = "type", nullable = false, insertable = true, updatable = true, length = 50)
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
