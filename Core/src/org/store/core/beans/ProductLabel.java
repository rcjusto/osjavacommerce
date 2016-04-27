package org.store.core.beans;

import org.store.core.beans.utils.StoreBean;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
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
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Etiquetas q se pueden aplicar a un producto
 */
@Entity
@Table(name="t_productlabel")
public class ProductLabel extends BaseBean implements StoreBean {
    public static final String LABEL_NEW = "new";
    public static final String LABEL_HOT = "hot";
    public static final String LABEL_SPECIAL = "special";
    public static final String LABEL_FREE_SHIPPING = "free_shipping";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 50)
    private String code;

    // html que se pone donde debe aparecer la etiqueta
    @Lob
    private String name;
    @Transient
    private Map<String,String> mapName;

    // html que se pone donde debe aparecer la etiqueta
    @Lob
    private String contentList;
    @Transient
    private Map<String,String> mapList;

    // html que se pone donde debe aparecer la etiqueta
    @Lob
    private String contentDetail;
    @Transient
    private Map<String,String> mapDetail;

    // Tienda en que esta configurado
    @Column(length = 10)
    private String inventaryCode;

    private Boolean filterInListing;

    @ManyToMany( fetch = FetchType.LAZY)
    @JoinTable(name = "t_product_t_productlabel",
            joinColumns = @JoinColumn(name = "labels_id"),
            inverseJoinColumns = @JoinColumn(name = "t_product_idProduct"))
    private Set<Product> products;

    @ManyToMany
    @JoinTable(name = "t_promotional_code_apply_labels",
            joinColumns = @JoinColumn(name = "applyToLabels_id"),
            inverseJoinColumns = @JoinColumn(name = "t_promotional_code_idPromotion"))
    private Set<PromotionalCode> applyPromotions;

    @ManyToMany
    @JoinTable(name = "t_promotional_code_not_apply_labels",
            joinColumns = @JoinColumn(name = "notApplyToLabels_id"),
            inverseJoinColumns = @JoinColumn(name = "t_promotional_code_idPromotion"))
    private Set<PromotionalCode> notApplyPromotions;



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

    public String getContentList() {
        return contentList;
    }

    public void setContentList(String contentList) {
        this.contentList = contentList;
    }

    public String getContentDetail() {
        return contentDetail;
    }

    public void setContentDetail(String contentDetail) {
        this.contentDetail = contentDetail;
    }

    public String getInventaryCode() {
        return inventaryCode;
    }

    public void setInventaryCode(String inventaryCode) {
        this.inventaryCode = inventaryCode;
    }

    public Set<Product> getProducts() {
        return products;
    }

    public void setProducts(Set<Product> products) {
        this.products = products;
    }

    public Boolean getFilterInListing() {
        return filterInListing!=null && filterInListing;
    }

    public void setFilterInListing(Boolean filterInListing) {
        this.filterInListing = filterInListing;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName(String lang) {
        if (mapName == null || mapName.size() < 1) deserializeName();
        return (mapName != null && mapName.containsKey(lang)) ? mapName.get(lang) : null;
    }

    public void setName(String lang, String value) {
        if (mapName==null) mapName = new HashMap<String,String>();
        mapName.put(lang, value);
        serializeName();
    }

    public String getContentList(String lang) {
        if (mapList == null || mapList.size() < 1) deserializeList();
        return (mapList != null && mapList.containsKey(lang)) ? mapList.get(lang) : null;
    }
    public String getContentDetail(String lang) {
        if (mapDetail == null || mapDetail.size() < 1) deserializeDetail();
        return (mapDetail != null && mapDetail.containsKey(lang)) ? mapDetail.get(lang) : null;
    }

    public void setContentList(String lang, String value)  {
        if (mapList==null) mapList = new HashMap<String,String>();
        mapList.put(lang, value);
        serializeList();
    }
    public void setContentDetail(String lang, String value)  {
        if (mapDetail==null) mapDetail = new HashMap<String,String>();
        mapDetail.put(lang, value);
        serializeDetail();
    }

    public void serializeName() {
        try {name = (mapName!=null && mapName.size()>0) ? JSONUtil.serialize(mapName) : null;}
        catch (JSONException e) {log.error(e.getMessage(), e); }
    }
    public void serializeList() {
        try {contentList = (mapList!=null && mapList.size()>0) ? JSONUtil.serialize(mapList) : null;}
        catch (JSONException e) {log.error(e.getMessage(), e); }
    }
    public void serializeDetail() {
        try {contentDetail = (mapDetail!=null && mapDetail.size()>0) ? JSONUtil.serialize(mapDetail) : null;}
        catch (JSONException e) {log.error(e.getMessage(), e); }
    }

    public void deserializeName() {
        try { mapName = (StringUtils.isNotEmpty(name)) ? (HashMap) JSONUtil.deserialize(name) : null; }
        catch (JSONException e) { log.error(e.getMessage(), e);  }
    }
    public void deserializeList() {
        try { mapList = (StringUtils.isNotEmpty(contentList)) ? (HashMap) JSONUtil.deserialize(contentList) : null; }
        catch (JSONException e) { log.error(e.getMessage(), e);  }
    }

    public void deserializeDetail() {
        try { mapDetail = (StringUtils.isNotEmpty(contentDetail)) ? (HashMap) JSONUtil.deserialize(contentDetail) : null; }
        catch (JSONException e) { log.error(e.getMessage(), e);  }
    }

    public Set<PromotionalCode> getApplyPromotions() {
        return applyPromotions;
    }

    public void setApplyPromotions(Set<PromotionalCode> applyPromotions) {
        this.applyPromotions = applyPromotions;
    }

    public Set<PromotionalCode> getNotApplyPromotions() {
        return notApplyPromotions;
    }

    public void setNotApplyPromotions(Set<PromotionalCode> notApplyPromotions) {
        this.notApplyPromotions = notApplyPromotions;
    }

    public boolean equals(Object other) {
        if ((this == other)) return true;
        if (!(other instanceof ProductLabel)) return false;
        ProductLabel castOther = (ProductLabel) other;
        return new EqualsBuilder()
                .append(this.getCode(), castOther.getCode())
                .isEquals();
    }

    public String toString() {
        return new ToStringBuilder(this)
            .append("code", getCode())
            .toString();
    }

}