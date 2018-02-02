package org.store.core.beans;

import org.store.core.beans.utils.StoreBean;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.struts2.json.JSONException;
import org.apache.struts2.json.JSONUtil;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.util.HashMap;
import java.util.Map;

/**
 * Fees
 */
@Entity
@Table(name = "t_order_status")
public class OrderStatus extends BaseBean implements StoreBean {
    public static final String STATUS_DEFAULT = "pending";
    public static final String STATUS_PAYMENT_VALIDATION = "payment_validation";
    public static final String STATUS_APPROVED = "approved";
    public static final String STATUS_REJECTED = "rejected";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 50)
    private String statusCode;

    // Tienda en la q esta configurado el status
    @Column(length = 10)
    private String inventaryCode;

    @Column(length = 50)
    private String statusType;

    private Boolean sendEmail;

    private Integer statusOrder;

    @Lob
    private String statusName;

    @Transient
    private Map<String, String> nameValues;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(String statusCode) {
        this.statusCode = statusCode;
    }

    public String getStatusName() {
        return statusName;
    }

    public void setStatusName(String statusName) {
        this.statusName = statusName;
    }

    public Integer getStatusOrder() {
        return statusOrder;
    }

    public void setStatusOrder(Integer statusOrder) {
        this.statusOrder = statusOrder;
    }

    public String getInventaryCode() {
        return inventaryCode;
    }

    public void setInventaryCode(String inventaryCode) {
        this.inventaryCode = inventaryCode;
    }

    public String getStatusType() {
        return statusType;
    }

    public void setStatusType(String statusType) {
        this.statusType = statusType;
    }

    public Boolean getSendEmail() {
        return sendEmail!=null && sendEmail;
    }

    public void setSendEmail(Boolean sendEmail) {
        this.sendEmail = sendEmail;
    }

    public String getStatusName(String lang) {
        String res = null;
        if (nameValues == null || nameValues.size() < 1) deserialize();
        if (nameValues != null && nameValues.containsKey(lang)) res = nameValues.get(lang);
        return res;
    }

    public void setStatusName(String lang, String value) {
        if (nameValues == null) nameValues = new HashMap<String, String>();
        nameValues.put(lang, value);
        serialize();
    }

    public void serialize() {
        try {
            statusName = (nameValues != null && nameValues.size() > 0) ? JSONUtil.serialize(nameValues) : null;
        } catch (JSONException e) {
            log.error(e.getMessage(), e);
        }
    }

    public void deserialize() {
        try {
            nameValues = (!isEmpty(statusName)) ? (HashMap) JSONUtil.deserialize(statusName) : null;
        } catch (JSONException e) {
            log.error(e.getMessage(), e);
        }
    }

    public String toString() {
        return new ToStringBuilder(this)
                .append("id", getId())
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrderStatus that = (OrderStatus) o;
        return !(id != null ? !id.equals(that.id) : that.id != null);

    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}