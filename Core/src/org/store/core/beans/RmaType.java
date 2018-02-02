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
 * RMA Types
 */
@Entity
@Table(name = "t_rma_type")
public class RmaType extends BaseBean implements StoreBean {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 50)
    private String codeMerchant;

    // Tienda en la q esta configurado el producto
    @Column(length = 10)
    private String inventaryCode;

    // Tipo del RMA
    @Lob
    private String name;
    @Transient
    private Map<String,String> nameValues;

    private Integer maxDays;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCodeMerchant() {
        return codeMerchant;
    }

    public void setCodeMerchant(String codeMerchant) {
        this.codeMerchant = codeMerchant;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getMaxDays() {
        return maxDays;
    }

    public void setMaxDays(Integer maxDays) {
        this.maxDays = maxDays;
    }

    public String getInventaryCode() {
        return inventaryCode;
    }

    public void setInventaryCode(String inventaryCode) {
        this.inventaryCode = inventaryCode;
    }

    public String getName(String lang) {
        if (nameValues == null || nameValues.size() < 1) deserialize();
        return (nameValues != null && nameValues.containsKey(lang)) ? nameValues.get(lang) : null;
    }

    public void setName(String lang, String value) {
        if (nameValues==null) nameValues = new HashMap<String,String>();
        nameValues.put(lang, value);
        serialize();
    }

    public void serialize() {
        try {
            name =  (nameValues!=null && nameValues.size()>0) ? JSONUtil.serialize(nameValues) :null;
        } catch (JSONException e) {
            log.error(e.getMessage(), e);
        }
    }

    public void deserialize() {
        try {
            nameValues =  (!isEmpty(name)) ? (HashMap) JSONUtil.deserialize(name) : null;
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
        if (!(other instanceof RmaType)) return false;
        RmaType castOther = (RmaType) other;
        return new EqualsBuilder()
                .append(this.getId(), castOther.getId())
                .isEquals();
    }


}