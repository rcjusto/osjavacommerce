package org.store.core.beans;

import org.store.core.beans.utils.StoreBean;
import org.apache.commons.lang.builder.ToStringBuilder;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import java.util.List;

/**
 * Taxes asignados a un producto y cliente
 */
@Entity
@Table(name = "t_product_user_tax")
public class ProductUserTax extends BaseBean implements StoreBean {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Tienda en la q esta configurado el producto
    @Column(length = 10)
    private String inventaryCode;

    private String familyProduct;
    private String categoryUser;

    @ManyToMany
    @JoinTable(name = "t_product_user_t_tax",
            joinColumns = @JoinColumn(name = "product_user_id"),
            inverseJoinColumns = @JoinColumn(name = "tax_id"))
    private List<TaxPerFamily> taxes;

    @Column(length = 50)
    private String externalCode;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFamilyProduct() {
        return familyProduct;
    }

    public void setFamilyProduct(String familyProduct) {
        this.familyProduct = familyProduct;
    }

    public String getCategoryUser() {
        return categoryUser;
    }

    public void setCategoryUser(String categoryUser) {
        this.categoryUser = categoryUser;
    }

    public List<TaxPerFamily> getTaxes() {
        return taxes;
    }

    public void setTaxes(List<TaxPerFamily> taxes) {
        this.taxes = taxes;
    }

    public String getExternalCode() {
        return externalCode;
    }

    public void setExternalCode(String externalCode) {
        this.externalCode = externalCode;
    }

    public String getInventaryCode() {
        return inventaryCode;
    }

    public void setInventaryCode(String inventaryCode) {
        this.inventaryCode = inventaryCode;
    }

    public String toString() {
        return new ToStringBuilder(this)
                .append("id", getId())
                .toString();
    }

}