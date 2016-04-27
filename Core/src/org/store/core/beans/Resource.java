package org.store.core.beans;

import org.store.core.beans.utils.StoreBean;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.struts2.json.JSONException;
import org.apache.struts2.json.JSONUtil;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Biblioteca de recursos.
 * Archvos que se suben para ser usados en cualquier cosa que se les ocurra
 * Pueden ser asignados a los productos
 */
@Entity
@Table(name = "t_resources")
public class Resource extends BaseBean implements StoreBean {

    public static final String TYPE_REBATE = "rebate";
    public static final String TYPE_IMAGE = "image";
    public static final String TYPE_PREVIEW = "preview";
    public static final String TYPE_DOWNLOAD = "download";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Codigo de la tienda a la que pertenece el menu
    @Column(length = 10)
    private String inventaryCode;

    // Resource Type
    @Column(length = 50)
    private String resourceType;

    // Resource Name
    @Lob
    private String resourceName;

    // Resource Description
    @Lob
    private String resourceDescription;

    @Transient
    private Map<String,String> nameValues;

    @Transient
    private Map<String,String> descValues;

    // Resources Hits
    private Long hits;

    // Filename
    @Column(length = 512)
    private String fileName;

    private Date resourceDate;

    private Double resourceValue;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "t_product_t_resources",
            joinColumns = @JoinColumn(name = "productResources_id"),
            inverseJoinColumns = @JoinColumn(name = "t_product_idProduct"))
    private Set<Product> products;

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

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public String getResourceName() {
        return resourceName;
    }

    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }

    public Long getHits() {
        return hits;
    }

    public void setHits(Long hits) {
        this.hits = hits;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getResourceDescription() {
        return resourceDescription;
    }

    public void setResourceDescription(String resourceDescription) {
        this.resourceDescription = resourceDescription;
    }

    public Date getResourceDate() {
        return resourceDate;
    }

    public void setResourceDate(Date resourceDate) {
        this.resourceDate = resourceDate;
    }

    public Double getResourceValue() {
        return resourceValue;
    }

    public void setResourceValue(Double resourceValue) {
        this.resourceValue = resourceValue;
    }

    public String getResourceName(String lang) {
        String res = null;
        if (nameValues == null || nameValues.size() < 1) deserializeName();
        if (nameValues != null && nameValues.containsKey(lang)) res = nameValues.get(lang);
        return res;
    }

    public void setResourceName(String lang, String value) {
        if (nameValues == null) nameValues = new HashMap<String, String>();
        nameValues.put(lang, value);
        serializeName();
    }

    public void serializeName() {
        try {
            if (nameValues!=null && nameValues.size()>0) resourceName = JSONUtil.serialize(nameValues);
            else resourceName = null;
        } catch (JSONException e) {
            log.error(e.getMessage(), e);
        }
    }

    public void deserializeName() {
        try {
            if (!isEmpty(resourceName)) nameValues = (HashMap) JSONUtil.deserialize(resourceName);
            else nameValues = null;
        } catch (JSONException e) {
            log.error(e.getMessage(), e);
        }
    }

    public String getResourceDescription(String lang) {
        String res = null;
        if (descValues == null || descValues.size() < 1) deserializeDescription();
        if (descValues != null && descValues.containsKey(lang)) res = descValues.get(lang);
        return res;
    }

    public void setResourceDescription(String lang, String value) {
        if (descValues == null) descValues = new HashMap<String, String>();
        descValues.put(lang, value);
        serializeDescription();
    }

    public void serializeDescription() {
        try {
            if (descValues!=null && descValues.size()>0) resourceDescription = JSONUtil.serialize(descValues);
            else resourceDescription = null;
        } catch (JSONException e) {
            log.error(e.getMessage(), e);
        }
    }

    public void deserializeDescription() {
        try {
            if (!isEmpty(resourceDescription)) descValues = (HashMap) JSONUtil.deserialize(resourceDescription);
            else descValues = null;
        } catch (JSONException e) {
            log.error(e.getMessage(), e);
        }
    }

    public boolean isValid() {
        return resourceDate==null || !resourceDate.before(Calendar.getInstance().getTime());
    }

    public Set<Product> getProducts() {
        return products;
    }

    public void setProducts(Set<Product> products) {
        this.products = products;
    }

    public String getResourceFileName() {
        if (id!=null && StringUtils.isNotEmpty(fileName)) {
            return id.toString()+"."+ FilenameUtils.getExtension(fileName);
        }
        return null;
    }

}