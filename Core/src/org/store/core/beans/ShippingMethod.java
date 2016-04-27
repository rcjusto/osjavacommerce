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
 * Metodo de transportacion
 */
@Entity
@Table(name = "t_shipping_method")
public class ShippingMethod extends BaseBean implements StoreBean {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Tienda donde esta configurado
    @Column(length = 10)
    private String inventaryCode;

    // Carrier name
    @Column(length = 50)
    private String carrierName;

    // Codigo interno del metodo para el distribuidor
    @Column(length = 250)
    private String methodCode;

    // Nombre del metodo
    @Lob
    private String methodName;
    @Transient
    private Map<String,String> nameValues;

    // Metodo activo para el live rate
    private Boolean active;

    // Metodo default para el live rate
    private Boolean defaultMethod;


    public ShippingMethod() {
    }


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

    public String getCarrierName() {
        return carrierName;
    }

    public void setCarrierName(String carrierName) {
        this.carrierName = carrierName;
    }

    public String getMethodCode() {
        return methodCode;
    }

    public void setMethodCode(String methodCode) {
        this.methodCode = methodCode;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public Boolean getActive() {
        return active!=null && active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Boolean getDefaultMethod() {
        return defaultMethod;
    }

    public void setDefaultMethod(Boolean defaultMethod) {
        this.defaultMethod = defaultMethod;
    }

    public String getMethodName(String lang) {
        String res = null;
        if (nameValues == null || nameValues.size() < 1) deserialize();
        if (nameValues != null && nameValues.containsKey(lang)) res = nameValues.get(lang);
        return res;
    }

    public void setMethodName(String lang, String value) {
        if (nameValues==null) nameValues = new HashMap<String,String>();
        if (value!=null) nameValues.put(lang, value);
        else if (nameValues.containsKey(lang)) nameValues.remove(lang);
        serialize();
    }

    public void serialize() {
        try {
            methodName =  (nameValues!=null && nameValues.size()>0) ? JSONUtil.serialize(nameValues) : null;
        } catch (JSONException e) {
            log.error(e.getMessage(), e);
        }
    }

    public void deserialize() {
        try {
            nameValues =  (!isEmpty(methodName)) ? (HashMap<String,String>) JSONUtil.deserialize(methodName) : null;
        } catch (JSONException e) {
            log.error(e.getMessage(), e);
        }
    }


    public boolean equals(Object other) {
        if ((this == other)) return true;
        if (!(other instanceof ShippingMethod)) return false;
        ShippingMethod castOther = (ShippingMethod) other;
        return new EqualsBuilder()
                .append(this.getId(), castOther.getId())
                .isEquals();
    }

    public String toString() {
        return new ToStringBuilder(this)
                .append("id", getId())
                .toString();
    }

}