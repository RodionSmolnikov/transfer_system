package demo.storage.entities;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.Date;

@Data
@ToString
@Entity
@NoArgsConstructor
@Table(name = "ACCOUNTS",
        uniqueConstraints = { @UniqueConstraint(columnNames = { "ID" }) })
public class AccountEntity {

    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    @Column(name = "ID", nullable = false, insertable = false, updatable = false, length = 36)
    private String id;

    @Column(name = "NAME", nullable = false, length = 100)
    private String name;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "CREATED_WHEN", nullable = false)
    private Date createdWhen;

    @Column(name = "BALANCE", nullable = false)
    private Integer balance;

}
