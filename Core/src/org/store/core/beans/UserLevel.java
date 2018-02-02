package org.store.core.beans;

import org.store.core.beans.utils.ExportedBean;
import org.store.core.beans.utils.StoreBean;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.struts2.json.JSONException;
import org.apache.struts2.json.JSONUtil;

import javax.persistence.*;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Nivel de usuario, descuento q se aplica a los productos cuando esta conectado.
 */
@Entity
@Table(name="t_user_level", uniqueConstraints = {@UniqueConstraint(columnNames={"code","inventaryCode"})})
public class UserLevel extends BaseBean implements StoreBean, ExportedBean {
    public static final String DEFAULT_LEVEL = "end_user";
    public static final String AFFILIATE_LEVEL = "affiliate";
    public static final String ANONYMOUS_LEVEL = "anonymous";

    /** identifier field */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 50)
    private String code;
    /* Nombre del nivel */
    @Lob
    private String name;
    @Transient
    private Map<String,String> nameValues;

    /** % de descuento a aplicar al nivel */
    private Double discountPercent;

    // Tienda en la q esta configurado el nivel
    @Column(length = 10)
    private String inventaryCode;

    /** Para ordenar en el combo **/
    private Integer levelOrder;

    /** Deshabilitar volume price. Esto permite usar volume price solo para los reseller **/
    private Boolean disableVolume;

    /** Necesita aprobacion del administrador **/
    private Boolean needApproval;

    @ElementCollection
    @JoinTable(name = "t_user_level_payment",
            joinColumns = @JoinColumn(name = "t_user_level_id")
    )
    private Set<String> paymentMethods;

    public UserLevel() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getInventaryCode() {
        return inventaryCode;
    }

    public void setInventaryCode(String inventaryCode) {
        this.inventaryCode = inventaryCode;
    }

    public Double getDiscountPercent() {
        return discountPercent;
    }

    public void setDiscountPercent(Double discountPercent) {
        this.discountPercent = discountPercent;
    }

    public Integer getLevelOrder() {
        return levelOrder;
    }

    public void setLevelOrder(Integer levelOrder) {
        this.levelOrder = levelOrder;
    }

    public Boolean getDisableVolume() {
        return disableVolume!=null && disableVolume;
    }

    public void setDisableVolume(Boolean disableVolume) {
        this.disableVolume = disableVolume;
    }

    public Boolean getNeedApproval() {
        return needApproval!=null && needApproval;
    }

    public void setNeedApproval(Boolean needApproval) {
        this.needApproval = needApproval;
    }

    public Set<String> getPaymentMethods() {
        if (paymentMethods==null) paymentMethods = new HashSet<String>();
        return paymentMethods;
    }

    public void setPaymentMethods(Set<String> paymentMethods) {
        this.paymentMethods = paymentMethods;
    }

    public boolean allPaymentMethods() {
        return paymentMethods==null || paymentMethods.isEmpty();
    }

    public boolean canPayWith(String paymentName) {
        return !StringUtils.isEmpty(paymentName) && (allPaymentMethods() || paymentMethods.contains(paymentName));
    }

    public String getName(String lang) {
        if (nameValues == null || nameValues.size() < 1) deserialize();
        return (nameValues != null && nameValues.containsKey(lang)) ? nameValues.get(lang) : null;
    }

    public void setName(String lang, String value) {
        if (nameValues == null) nameValues = new HashMap<String, String>();
        nameValues.put(lang, value);
        serialize();
    }

    public void serialize() {
        try {
            name = (nameValues != null && nameValues.size() > 0) ? JSONUtil.serialize(nameValues) : null;
        } catch (JSONException e) {
            log.error(e.getMessage(), e); 
        }
    }

    public void deserialize() {
        try {
            nameValues = (!isEmpty(name)) ? (HashMap) JSONUtil.deserialize(name) : null;
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
        if (other==null) return false;
        if ((this == other)) return true;
        if (!(other instanceof UserLevel)) return false;
        UserLevel castOther = (UserLevel) other;
        return new EqualsBuilder()
                .append(this.getId(), castOther.getId())
                .isEquals();
    }

    public Map<String, Object> toMap(String lang) {
        Map res = new HashMap();
        try {
            Map m = describe();
            res.putAll(m);

        } catch (IllegalAccessException e) {
            log.error(e.getMessage(), e); 
        } catch (InvocationTargetException e) {
            log.error(e.getMessage(), e); 
        } catch (NoSuchMethodException e) {
            log.error(e.getMessage(), e); 
        }

        return res;
    }

    public void fromMap(Map<String, String> m) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

}