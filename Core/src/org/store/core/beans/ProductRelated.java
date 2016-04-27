package org.store.core.beans;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;


@Entity
@Table(name = "t_product_related")
public class ProductRelated extends BaseBean {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double combinedPrice;

    @ManyToOne
    private Product product;

    @ManyToOne
    private Product related;

    public ProductRelated() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Double getCombinedPrice() {
        return combinedPrice;
    }

    public void setCombinedPrice(Double combinedPrice) {
        this.combinedPrice = combinedPrice;
    }

    public Product getRelated() {
        return related;
    }

    public void setRelated(Product related) {
        this.related = related;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public String toString() {
        return new ToStringBuilder(this)
                .append("id", getId())
                .toString();
    }

    @Override
    public boolean equals(Object other) {
        if ((this == other)) return true;
        if (!(other instanceof ProductRelated)) return false;
        ProductRelated castOther = (ProductRelated) other;
        return new EqualsBuilder()
                .append(this.getProduct(), castOther.getProduct())
                .append(this.getRelated(), castOther.getRelated())
                .isEquals();
    }

}