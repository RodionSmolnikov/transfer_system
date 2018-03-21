package demo.datasource;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.Date;

/**
 * Table for saving transfer operation history
 */
@Entity
@Table(name = "TRANSFER_OPERATION",
        uniqueConstraints = { @UniqueConstraint(columnNames = { "ID" }) })
public class TransferOperation {

    private String id;
    private Date createdWhen;
    private String type;
    private String accountId;
    private String transferAccountId;
    private double sum;
    private String description;
    private String status;
    private String details;

    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    @Column(name = "ID", nullable = false, insertable = false, updatable = false, length = 36)
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "CREATED_WHEN", nullable = false)
    public Date getCreatedWhen() {
        return createdWhen;
    }

    public void setCreatedWhen(Date createdWhen) {
        this.createdWhen = createdWhen;
    }

    @Basic
    @Column(name = "TYPE", nullable = false, insertable = true, updatable = true, length = 50)
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Basic
    @Column(name = "ACCOUNT_ID", nullable = false, length = 36)
    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    @Basic
    @Column(name = "TRANSFER_ACCOUNT_ID", length = 36)
    public String getTransferAccountId() {
        return transferAccountId;
    }

    public void setTransferAccountId(String transferAccountId) {
        this.transferAccountId = transferAccountId;
    }

    @Basic
    @Column(name = "SUM", nullable = false, length = 50)
    public double getSum() {
        return sum;
    }

    public void setSum(double sum) {
        this.sum = sum;
    }

    @Basic
    @Column(name = "DESCRIPTION", length = 280)
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Basic
    @Column(name = "STATUS", nullable = false, length = 30)
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Basic
    @Column(name = "DETAILS", length = 280)
    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    @Override
    public String toString() {
        return "TransferOperation{" + "\n" +
                "id=" + id + ",\n" +
                " createdWhen=" + createdWhen + ",\n" +
                " type=" + type + ",\n" +
                " accountId=" + accountId + ",\n" +
                " transferAccountId=" + transferAccountId + ",\n" +
                " sum=" + sum + ",\n" +
                " description=" + description + ",\n" +
                " status=" + status + ",\n" +
                " details=" + details + ",\n" +
                '}';
    }
}
