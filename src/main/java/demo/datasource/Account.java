package demo.datasource;
import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "accounts", schema = "transfer")
public class Account {

    private String id;
    private String firstName;
    private String lastName;
    private Date createdWhen;
    private double balance;

    @Id
    @Column(name = "id", nullable = false, insertable = true, updatable = true, length = 36)
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Basic
    @Column(name = "first_name", nullable = false, insertable = true, updatable = true, length = 100)
    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    @Basic
    @Column(name = "last_name", nullable = true, insertable = true, updatable = true, length = 100)
    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_when", nullable = false, insertable = true, updatable = true)
    public Date getCreatedWhen() {
        return createdWhen;
    }

    public void setCreatedWhen(Date createdWhen) {
        this.createdWhen = createdWhen;
    }

    @Basic
    @Column(name = "balance", nullable = false, insertable = true, updatable = true)
    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

}
