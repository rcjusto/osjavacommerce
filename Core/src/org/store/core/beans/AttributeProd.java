package org.store.core.beans;

import org.store.core.beans.utils.StoreBean;
import org.store.core.globals.SomeUtils;
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
import java.util.List;
import java.util.Map;


@Entity
@Table(name="t_attribute_prod")
public class AttributeProd extends BaseBean implements StoreBean {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Lob
    private String attributeName;
    @Transient
    private Map<String,String> mapNames;

    // Tienda en la q esta configurado el attributo
    @Column(length = 10)
    private String inventaryCode;

    private Boolean hideInProduct;

    @Column(length = 255)
    private String attributeGroup;
    @Lob
    private String attributeOptions;


    public AttributeProd() {
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

    public String getAttributeName() {
        return attributeName;
    }

    public void setAttributeName(String attributeName) {
        this.attributeName = attributeName;
    }

    public String getAttributeGroup() {
        return attributeGroup;
    }

    public void setAttributeGroup(String attributeGroup) {
        this.attributeGroup = attributeGroup;
    }

    public Boolean getHideInProduct() {
        return hideInProduct!=null && hideInProduct;
    }

    public void setHideInProduct(Boolean hideInProduct) {
        this.hideInProduct = hideInProduct;
    }

    public String getAttributeOptions() {
        return attributeOptions;
    }

    public void setAttributeOptions(String attributeOptions) {
        this.attributeOptions = attributeOptions;
    }

    public List<String> getOptions() {
        return SomeUtils.getLines(attributeOptions);
    }

    public String getAttributeName(String lang) {
        if (mapNames == null || mapNames.size() < 1) deserialize();
        return (mapNames != null && mapNames.containsKey(lang)) ? mapNames.get(lang) : null;
    }

    public void setAttributeName(String lang, String value) {
        if (mapNames==null) mapNames = new HashMap<String,String>();
        mapNames.put(lang, value);
        serialize();
    }

    public void serialize() {
        try {
            attributeName =  (mapNames!=null && mapNames.size()>0) ? JSONUtil.serialize(mapNames) :null;
        } catch (JSONException e) {
            log.error(e.getMessage(), e);
        }
    }

    public void deserialize() {
        try {
            mapNames =  (!isEmpty(attributeName)) ? (HashMap) JSONUtil.deserialize(attributeName) : null;
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
    public boolean equals(Object other) {
        if ((this == other)) return true;
        if (!(other instanceof AttributeProd)) return false;
        AttributeProd castOther = (AttributeProd) other;
        return new EqualsBuilder()
                .append(this.getId(), castOther.getId())
                .isEquals();
    }


}