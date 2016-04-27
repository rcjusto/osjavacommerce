package org.store.core.beans;

import org.store.core.beans.utils.StoreBean;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.struts2.json.JSONException;
import org.apache.struts2.json.JSONUtil;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

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
 * Grupo de complementos
 */
@Entity
@Table(name = "t_complement_group")
@OnDelete(action = OnDeleteAction.CASCADE)
public class ComplementGroup extends BaseBean implements StoreBean {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idGroup;

    // Tienda en la q esta configurado el complemento
    @Column(length = 10)
    private String inventaryCode;

    // No se pa q es esto
    @Lob
    private String groupName;
    @Transient
    private Map<String, String> nameValues;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "t_product_t_complement_group",
            joinColumns = @JoinColumn(name = "relatedGroups_idGroup"),
            inverseJoinColumns = @JoinColumn(name = "t_product_idProduct"))
    private Set<Product> products;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "t_category_t_complement_group",
            joinColumns = @JoinColumn(name = "relatedGroups_idGroup"),
            inverseJoinColumns = @JoinColumn(name = "t_category_idCategory"))
    private Set<Category> categories;


    public ComplementGroup() {
    }


    public Long getIdGroup() {
        return this.idGroup;
    }

    public void setIdGroup(Long idGroup) {
        this.idGroup = idGroup;
    }

    public String getInventaryCode() {
        return inventaryCode;
    }

    public void setInventaryCode(String inventaryCode) {
        this.inventaryCode = inventaryCode;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public Map<String, String> getNameValues() {
        return nameValues;
    }

    public void setNameValues(Map<String, String> nameValues) {
        this.nameValues = nameValues;
    }

    public Set<Product> getProducts() {
        return products;
    }

    public void setProducts(Set<Product> products) {
        this.products = products;
    }

    public Set<Category> getCategories() {
        return categories;
    }

    public void setCategories(Set<Category> categories) {
        this.categories = categories;
    }

    public String getGroupName(String lang) {
        if (nameValues == null || nameValues.size() < 1) deserialize();
        return (nameValues != null && nameValues.containsKey(lang)) ? nameValues.get(lang) : null;
    }

    public void setGroupName(String lang, String value) {
        if (nameValues == null) nameValues = new HashMap<String, String>();
        nameValues.put(lang, value);
        serialize();
    }

    public void serialize() {
        try {
            groupName = (nameValues != null && nameValues.size() > 0) ? JSONUtil.serialize(nameValues) : null;
        } catch (JSONException e) {
            log.error(e.getMessage(), e);
        }
    }

    public void deserialize() {
        try {
            nameValues = (!isEmpty(groupName)) ? (HashMap) JSONUtil.deserialize(groupName) : null;
        } catch (JSONException e) {
            log.error(e.getMessage(), e);
        }
    }

    public String toString() {
        return new ToStringBuilder(this)
                .append("idGroup", getIdGroup())
                .toString();
    }

    @Override
    public boolean equals(Object other) {
        if ((this == other)) return true;
        if (!(other instanceof ComplementGroup)) return false;
        ComplementGroup castOther = (ComplementGroup) other;
        return new EqualsBuilder()
                .append(this.getIdGroup(), castOther.getIdGroup())
                .isEquals();
    }

}
