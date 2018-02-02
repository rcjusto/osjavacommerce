package org.store.core.beans;

import org.store.core.beans.utils.PageMeta;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.struts2.json.JSONException;
import org.apache.struts2.json.JSONUtil;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.util.HashMap;
import java.util.Map;

/**
 * Datos especificos del idioma
 */
@Entity
@Table(name="t_product_lang")
public class ProductLang extends BaseBean {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Idioma de los datos
    @Column(length = 3)
    private String productLang;

    // Nombre del producto
    @Column(length = 1024)
    private String productName;

    // Descripcion del producto
    @Lob
    private String description;

    // Mas descripcion
    @Lob
    private String features;

    // Mas informacion
    @Lob
    private String information;

    //metas de la pagina
    @Lob
    private String metas;
    @Transient
    private Map<String, PageMeta> metasMap;

    // Nombre de opciones obligatorias a seleccionar para comprar el producto
    @Column(length = 255)
    private String caract1;
    @Column(length = 255)
    private String caract2;
    @Column(length = 255)
    private String caract3;
    
    private Boolean indexed;

    // Producto
    @ManyToOne(fetch = FetchType.LAZY)
    private Product product;

    public ProductLang() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getProductLang() {
        return productLang;
    }

    public void setProductLang(String productLang) {
        this.productLang = productLang;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getFeatures() {
        return features;
    }

    public void setFeatures(String features) {
        this.features = features;
    }

    public String getInformation() {
        return information;
    }

    public void setInformation(String information) {
        this.information = information;
    }

    public String getMetas() {
        return metas;
    }

    public void setMetas(String metas) {
        this.metas = metas;
    }

    public String getCaract1() {
        return caract1;
    }

    public void setCaract1(String caract1) {
        this.caract1 = caract1;
    }

    public String getCaract2() {
        return caract2;
    }

    public void setCaract2(String caract2) {
        this.caract2 = caract2;
    }

    public String getCaract3() {
        return caract3;
    }

    public void setCaract3(String caract3) {
        this.caract3 = caract3;
    }

    public Boolean getIndexed() {
        return indexed;
    }

    public void setIndexed(Boolean indexed) {
        this.indexed = indexed;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public String toString() {
        return new ToStringBuilder(this)
            .append("Id", getId())
            .toString();
    }

    public boolean equals(Object other) {
        if ( (this == other ) ) return true;
        if ( !(other instanceof ProductLang) ) return false;
        ProductLang castOther = (ProductLang) other;
        return new EqualsBuilder()
            .append(this.getProduct(), castOther.getProduct())
            .append(this.getProductLang(), castOther.getProductLang())
            .isEquals();
    }

    public int hashCode() {
        return new HashCodeBuilder()
            .append(getId())
            .toHashCode();
    }

    public PageMeta getMeta(String metaName) {
        if (metasMap == null || metasMap.size() < 1) deserialize();
        if (metasMap != null && metasMap.containsKey(metaName)) {
            Object res = metasMap.get(metaName);
            if (res instanceof PageMeta) return (PageMeta) res;
            else if (res instanceof Map) {
                return new PageMeta((Map)res);
            }
        }
        return null;
    }

    public String getMetaValue(String metaName) {
        PageMeta p = getMeta(metaName);
        return (p!=null) ? p.getMetaValue() : null;
    }

    public void addMeta(String metaName, String metaValue, Boolean appendToParent)  {
        if (metasMap==null) metasMap = new HashMap<String,PageMeta>();
        metasMap.put(metaName, new PageMeta(metaName,metaValue,appendToParent));
        serialize();
    }

    public void serialize() {
        try {
            if (metasMap!=null && metasMap.size()>0) metas = JSONUtil.serialize(metasMap);
            else metas = null;
        } catch (JSONException e) {
            log.error(e.getMessage(), e);
        }
    }

    public void deserialize() {
        try {
            if (!isEmpty(metas)) metasMap = (HashMap) JSONUtil.deserialize(metas);
            else metasMap = null;
        } catch (JSONException e) {
            log.error(e.getMessage(), e);
        }
    }

    public void resetMetas() {
        metas = null;
        if (metasMap!=null) metasMap.clear();
        else metasMap = new HashMap<String,PageMeta>();
    }
}
