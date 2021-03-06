package demo.datasource;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.JoinFormula;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "ACCOUNTS",
        uniqueConstraints = { @UniqueConstraint(columnNames = { "ID" }) })
public class Account {

    private String id;
    private String firstName;
    private String lastName;
    private Date createdWhen;
    private double balance;

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

    @Basic
    @Column(name = "FIRST_NAME", nullable = false, length = 100)
    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    @Basic
    @Column(name = "LAST_NAME", length = 100)
    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
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
    @Column(name = "BALANCE", nullable = false)
    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    @Override
    public String toString() {
        return "Account{" + "\n" +
                "id=" + id + ",\n" +
                " firstName=" + firstName + ",\n" +
                " lastName=" + lastName + ",\n" +
                " createdWhen=" + createdWhen + ",\n" +
                " balance=" + balance + ",\n" +
                '}';
    }
}
