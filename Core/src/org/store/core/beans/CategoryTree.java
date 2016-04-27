package org.store.core.beans;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * Posicion que ocupa la categoria en el menu
 */
@Entity
@Table(name = "t_category_tree")
public class CategoryTree extends BaseBean {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer position;

    // Categoria padre
    @ManyToOne(fetch = FetchType.EAGER)
    private Category parent;

    // Categoria hija
    @ManyToOne(fetch = FetchType.EAGER)
    private Category child;


    public CategoryTree() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }

    public Category getParent() {
        return parent;
    }

    public void setParent(Category parent) {
        this.parent = parent;
    }

    public Category getChild() {
        return child;
    }

    public void setChild(Category child) {
        this.child = child;
    }

    public String toString() {
        return new ToStringBuilder(this)
                .append("Parent", getParent())
                .append("Child", getChild())
                .toString();
    }

    public boolean equals(Object other) {
        if ((this == other)) return true;
        if (!(other instanceof CategoryTree)) return false;
        CategoryTree castOther = (CategoryTree) other;
        return new EqualsBuilder()
                .append(this.getParent(), castOther.getParent())
                .append(this.getChild(), castOther.getChild())
                .isEquals();
    }

    public int hashCode() {
        return new HashCodeBuilder()
                .append(getParent())
                .append(getChild())
                .toHashCode();
    }



}