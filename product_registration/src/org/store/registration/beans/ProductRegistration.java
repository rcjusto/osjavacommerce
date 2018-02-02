package org.store.registration.beans;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.store.core.beans.BaseBean;
import org.store.core.beans.User;
import org.store.core.beans.utils.StoreBean;

import javax.persistence.*;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "t_product_registration")
public class ProductRegistration extends BaseBean implements StoreBean {

    private static final String MODEL_NUMBER_SEPARATOR = ", ";
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Tienda en la q esta configurado el fee
    @Column(length = 10)
    private String inventaryCode;

    @Lob
    private String modelNumber;

    private Date purchaseDate;

    @Column(length = 512)
    private String purchaseCountry;

    @Column(length = 512)
    private String purchaseCity;

    @Column(length = 512)
    private String purchasePlace;

    @Column(length = 255)
    private String invoiceNumber;

    @Lob
    private String details;

    @ManyToOne
    private User user;

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

    public String getModelNumber() {
        return modelNumber;
    }

    public void setModelNumber(String modelNumber) {
        this.modelNumber = modelNumber;
    }

    public Date getPurchaseDate() {
        return purchaseDate;
    }

    public void setPurchaseDate(Date purchaseDate) {
        this.purchaseDate = purchaseDate;
    }

    public String getPurchasePlace() {
        return purchasePlace;
    }

    public void setPurchasePlace(String purchasePlace) {
        this.purchasePlace = purchasePlace;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public String getPurchaseCountry() {
        return purchaseCountry;
    }

    public void setPurchaseCountry(String purchaseCountry) {
        this.purchaseCountry = purchaseCountry;
    }

    public String getPurchaseCity() {
        return purchaseCity;
    }

    public void setPurchaseCity(String purchaseCity) {
        this.purchaseCity = purchaseCity;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void addModelNumber(String mn) {
        if (StringUtils.isNotEmpty(mn)) {
            if (StringUtils.isNotEmpty(this.modelNumber)) modelNumber += MODEL_NUMBER_SEPARATOR;
            this.modelNumber += mn;
        }
    }

    public List<String> getModelNumbers() {
        if (StringUtils.isNotEmpty(this.modelNumber)) {
            String[] arr = StringUtils.split(this.modelNumber, MODEL_NUMBER_SEPARATOR);
            return Arrays.asList(arr);
        }
        return null;
    }

    public void setModelNumbers(List<String> list) {
        StringBuilder b = new StringBuilder();
        for(String cad : list) {
            if (StringUtils.isNotEmpty(b.toString())) b.append(MODEL_NUMBER_SEPARATOR);
            b.append(cad);
        }
        this.modelNumber = b.toString();
    }

    public String toString() {
        return new ToStringBuilder(this)
                .append("id", getId())
                .toString();
    }

    public boolean equals(Object other) {
        if ((this == other)) return true;
        if (!(other instanceof ProductRegistration)) return false;
        ProductRegistration castOther = (ProductRegistration) other;
        return new EqualsBuilder()
                .append(this.getId(), castOther.getId())
                .isEquals();
    }


}
