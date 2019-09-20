package demo.storage.entities;

import demo.storage.OperationType;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.Date;

/**
 * Table for saving transfer operation history
 */
@Entity
@Data
@ToString
@NoArgsConstructor
@Table(name = "OPERATION",
        uniqueConstraints = { @UniqueConstraint(columnNames = { "ID" }) })
public class OperationEntity {

    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    @Column(name = "ID", nullable = false, insertable = false, updatable = false, length = 36)
    private String id;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "TIMESTAMP", nullable = false)
    private Date timestamp;

    @Enumerated
    @Column(name = "TYPE", nullable = false)
    private OperationType type;

    @Column(name = "ACCOUNT_ID", nullable = false, length = 36)
    private String accountId;

    @Column(name = "TRANSFER_ACCOUNT_ID", length = 36)
    private String transferAccountId;

    @Column(name = "SUM", nullable = false)
    private Integer sum;

    @Column(name = "DESCRIPTION", length = 280)
    private String description;
}
