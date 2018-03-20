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
    private Account account;
    private Account transferAccount;

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
    @Column(name = "CREATED_WHEN", nullable = false, insertable = true, updatable = true)
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
    @Column(name = "ACCOUNT_ID", nullable = false, insertable = true, updatable = true, length = 36)
    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    @Basic
    @Column(name = "TRANSFER_ACCOUNT_ID", nullable = false, insertable = true, updatable = true, length = 36)
    public String getTransferAccountId() {
        return transferAccountId;
    }

    public void setTransferAccountId(String transferAccountId) {
        this.transferAccountId = transferAccountId;
    }

    @Basic
    @Column(name = "SUM", nullable = false, insertable = true, updatable = true, length = 50)
    public double getSum() {
        return sum;
    }

    public void setSum(double sum) {
        this.sum = sum;
    }

    @Basic
    @Column(name = "DESCRIPTION", nullable = false, insertable = true, updatable = true, length = 280)
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Basic
    @Column(name = "STATUS", nullable = false, insertable = true, updatable = true, length = 30)
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Basic
    @Column(name = "DETAILS", nullable = false, insertable = true, updatable = true)
    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "ID", nullable = false)
//    public Account getAccount() {
//        return account;
//    }
//
//    public void setAccount(Account account) {
//        this.account = account;
//    }
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "ID", nullable = false)
//    public Account getTransferAccount() {
//        return transferAccount;
//    }
//
//    public void setTransferAccount(Account transferAccount) {
//        this.transferAccount = transferAccount;
//    }

}
