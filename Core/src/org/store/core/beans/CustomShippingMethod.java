package org.store.core.beans;

import org.store.core.beans.utils.StoreBean;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.struts2.json.JSONException;
import org.apache.struts2.json.JSONUtil;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Fees
 */
@Entity
@Table(name = "t_custom_shipping")
public class CustomShippingMethod extends BaseBean implements StoreBean {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 10)
    private String inventaryCode;

    private Integer days;

    @Column(length = 512)
    private String code;

    @Lob
    private String name;
    @Transient
    private Map<String, String> mapName;

    @OneToMany(mappedBy = "method", cascade = CascadeType.ALL)
    @OrderBy(value = "ruleOrder asc")
    private List<CustomShippingMethodRule> rules;


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

    public Integer getDays() {
        return days;
    }

    public void setDays(Integer days) {
        this.days = days;
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

    public String getName(String lang) {
        if (mapName == null || mapName.size() < 1) deserialize();
        return (mapName != null && mapName.containsKey(lang)) ? mapName.get(lang) : null;
    }

    public void setName(String lang, String value) {
        if (mapName == null) mapName = new HashMap<String, String>();
        mapName.put(lang, value);
        serialize();
    }

    public void serialize() {
        try {
            name =  (mapName!=null && mapName.size()>0) ? JSONUtil.serialize(mapName) :null;
        } catch (JSONException e) {
            log.error(e.getMessage(), e);
        }
    }

    public void deserialize() {
        try {
            mapName =  (!isEmpty(name)) ? (HashMap) JSONUtil.deserialize(name) : null;
        } catch (JSONException e) {
            log.error(e.getMessage(), e);
        }
    }

    public List<CustomShippingMethodRule> getRules() {
        return rules;
    }

    public void setRules(List<CustomShippingMethodRule> rules) {
        this.rules = rules;
    }

    public String toString() {
        return new ToStringBuilder(this)
                .append("id", getId())
                .toString();
    }

    public boolean equals(Object other) {
        if ((this == other)) return true;
        if (!(other instanceof CustomShippingMethod)) return false;
        CustomShippingMethod castOther = (CustomShippingMethod) other;
        return new EqualsBuilder()
                .append(this.getId(), castOther.getId())
                .isEquals();
    }

}