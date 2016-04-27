package org.store.core.beans;

import org.store.core.beans.utils.StoreBean;
import org.apache.commons.lang.builder.EqualsBuilder;
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
@Table(name = "t_insurance")
public class Insurance extends BaseBean implements StoreBean {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Tienda en la q esta configurado el fee
    @Column(length = 10)
    private String inventaryCode;

    private Double minTotal;
    private Double maxTotal;
    private Double insuranceValue;
    private Boolean active;
    @Lob
    private String text;
    @Transient
    private Map<String,String> data;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getInventaryCode() {
        return inventaryCode;
    }

    public void setInventaryCode(String inventaryCode) {
        this.inventaryCode = inventaryCode;
    }

    public Double getMinTotal() {
        return minTotal;
    }

    public void setMinTotal(Double minTotal) {
        this.minTotal = minTotal;
    }

    public Double getMaxTotal() {
        return maxTotal;
    }

    public void setMaxTotal(Double maxTotal) {
        this.maxTotal = maxTotal;
    }

    public Double getInsuranceValue() {
        return insuranceValue;
    }

    public void setInsuranceValue(Double insuranceValue) {
        this.insuranceValue = insuranceValue;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getText(String lang) {
        if (data == null || data.size() < 1) deserialize();
        return (data != null && data.containsKey(lang)) ? data.get(lang) : null;
    }

    public void setText(String lang, String value) {
        if (data==null) data = new HashMap<String,String>();
        data.put(lang, value);
        serialize();
    }

    public void serialize() {
        try {
            text =  (data!=null && data.size()>0) ? JSONUtil.serialize(data) :null;
        } catch (JSONException e) {
            log.error(e.getMessage(), e);
        }
    }

    public void deserialize() {
        try {
            data =  (!isEmpty(text)) ? (HashMap) JSONUtil.deserialize(text) : null;
        } catch (JSONException e) {
            log.error(e.getMessage(), e);
        }
    }


    public String toString() {
        return new ToStringBuilder(this)
                .append("id", getId())
                .toString();
    }

    public boolean equals(Object other) {
        if ((this == other)) return true;
        if (!(other instanceof Insurance)) return false;
        Insurance castOther = (Insurance) other;
        return new EqualsBuilder()
                .append(this.getId(), castOther.getId())
                .isEquals();
    }

}