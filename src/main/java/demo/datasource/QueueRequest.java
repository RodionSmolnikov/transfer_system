package demo.datasource;

import javax.persistence.*;
/**
 * Table for REST API operations
 */
@Entity
@Table(name = "queue_requests", schema = "transfer")
public class QueueRequest {

    private int id;
    private String type;

    //number of columns instead of JSON wrap/unwrap routine
    private String field1;
    private String field2;
    private String field3;
    private String field4;

    private String response;

    @Id
    @Column(name = "id", nullable = false, insertable = true, updatable = true, length = 50)
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Basic
    @Column(name = "type", nullable = false, insertable = true, updatable = true, length = 50)
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    /**
     * @return User id for balance operations. Name for user operations
     */
    @Basic
    @Column(name = "field1", insertable = true, updatable = true, length = 100)
    public String getField1() {
        return field1;
    }

    public void setField1(String field1) {
        this.field1 = field1;
    }

    /**
     * @return User id for balance operations. Name for user operations
     */
    @Basic
    @Column(name = "field2", insertable = true, updatable = true, length = 100)
    public String getField2() {
        return field2;
    }

    public void setField2(String field2) {
        this.field2 = field2;
    }

    /**
     * @return balance or balance change for all operations
     */
    @Basic
    @Column(name = "field3", insertable = true, updatable = true, length = 100)
    public String getField3() {
        return field3;
    }

    public void setField3(String field3) {
        this.field3 = field3;
    }

    /**
     * @return operation description
     */
    @Basic
    @Column(name = "field4", insertable = true, updatable = true, length = 500)
    public String getField4() {
        return field4;
    }

    public void setField4(String field4) {
        this.field4 = field4;
    }

    @Basic
    @Column(name = "response", insertable = true, updatable = true, length = 500)
    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }
}
