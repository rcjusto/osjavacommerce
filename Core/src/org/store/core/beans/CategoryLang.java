package org.store.core.beans;

import org.store.core.beans.utils.PageMeta;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.struts2.json.JSONException;
import org.apache.struts2.json.JSONUtil;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.util.HashMap;
import java.util.Map;


@Entity
@Table(name = "t_category_lang")
public class CategoryLang extends BaseBean {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Idioma de los datos
    @Column(length = 3)
    private String categoryLang;

    // Nombre de la categoria
    @Column(length = 512)
    private String categoryName;

    // Descripcion de la categoria
    @Lob
    private String description;

    // Metas de la pagina
    @Lob
    private String metas;
    @Transient
    private Map<String, PageMeta> metasMap;

    // Opciones obligatorias de los producto
    @Column(length = 255)
    private String caract1;
    @Column(length = 255)
    private String caract2;
    @Column(length = 255)
    private String caract3;

    // Categoria a la que pertenecen lod datos
    @ManyToOne
    private Category category;


    public CategoryLang() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCategoryLang() {
        return categoryLang;
    }

    public void setCategoryLang(String categoryLang) {
        this.categoryLang = categoryLang;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public String getCategoryName() {
        return this.categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getMetas() {
        return metas;
    }

    public void setMetas(String metas) {
        this.metas = metas;
    }

    public String getCaract1() {
        return this.caract1;
    }

    public void setCaract1(String caract1) {
        this.caract1 = caract1;
    }

    public String getCaract2() {
        return this.caract2;
    }

    public void setCaract2(String caract2) {
        this.caract2 = caract2;
    }

    public String getCaract3() {
        return this.caract3;
    }

    public void setCaract3(String caract3) {
        this.caract3 = caract3;
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

    public void addMeta(String metaName, String metaValue, Boolean appendToParent) {
        if (metasMap == null) metasMap = new HashMap<String, PageMeta>();
        metasMap.put(metaName, new PageMeta(metaName, metaValue, appendToParent));
        serialize();
    }

    public void serialize() {
        try {
            if (metasMap != null && metasMap.size() > 0) metas = JSONUtil.serialize(metasMap);
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

    public String toString() {
        return new ToStringBuilder(this)
                .append("Category", getCategory())
                .append("Language", getCategoryLang())
                .toString();
    }

    public boolean equals(Object other) {
        if ((this == other)) return true;
        if (!(other instanceof CategoryLang)) return false;
        CategoryLang castOther = (CategoryLang) other;
        return new EqualsBuilder()
                .append(this.getCategory(), castOther.getCategory())
                .append(this.getCategoryLang(), castOther.getCategoryLang())
                .isEquals();
    }

    public int hashCode() {
        return new HashCodeBuilder()
                .append(getCategory())
                .append(getCategoryLang())
                .toHashCode();
    }


    public void resetMetas() {
        metas = null;
        if (metasMap!=null) metasMap.clear();
        else metasMap = new HashMap<String,PageMeta>();
    }
}
