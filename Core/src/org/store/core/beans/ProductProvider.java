package org.store.core.beans;

import org.apache.commons.lang.StringUtils;
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
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Proveedor del producto
 */
@Entity
@Table(name="t_product_provider")
public class ProductProvider extends BaseBean {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Costo del producto para este proveedor
    private Double costPrice;

    @ManyToOne
    private Currency costCurrency;

    // Stock del producto para este proveedor
    private Long stock;

    // ETA del producto para este proveedor
    private Date eta;

    // Warehouse info
    @Lob
    private String warehousesData;
    @Transient
    private List<Map<String,Object>> warehouses;

    // codigo del producto para este proveedor
    @Column(length = 250)
    private String sku;

    @Lob
    private String lastError;

    private Date lastUpdate;

    private Boolean active;

    // Proveedor
    @ManyToOne
    private Provider provider;

    // producto
    @ManyToOne
    private Product product;

    public ProductProvider() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Boolean getActive() {
        return active!=null && active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Double getCostPrice() {
        return costPrice;
    }

    public void setCostPrice(Double costPrice) {
        this.costPrice = costPrice;
    }

    public Long getStock() {
        return stock;
    }

    public void setStock(Long stock) {
        this.stock = stock;
    }

    public Date getEta() {
        return eta;
    }

    public void setEta(Date eta) {
        this.eta = eta;
    }

    public String getLastError() {
        return lastError;
    }

    public void setLastError(String lastError) {
        this.lastError = lastError;
    }

    public Date getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(Date lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public Provider getProvider() {
        return provider;
    }

    public void setProvider(Provider provider) {
        this.provider = provider;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getWarehousesData() {
        return warehousesData;
    }

    public void setWarehousesData(String warehousesData) {
        this.warehousesData = warehousesData;
    }

    public Currency getCostCurrency() {
        return costCurrency;
    }

    public void setCostCurrency(Currency costCurrency) {
        this.costCurrency = costCurrency;
    }

    public List<Map<String, Object>> getWarehouses() {
        if (warehouses==null || warehouses.isEmpty()) {
            try {
                this.warehouses = (StringUtils.isNotEmpty(this.warehousesData)) ? (List<Map<String, Object>>) JSONUtil.deserialize(this.warehousesData) : null;
            } catch (JSONException e) {
                log.error(e.getMessage(), e);
            }
        }
        return warehouses;
    }

    public void setWarehouses(List<Map<String, Object>> warehouses) {
        this.warehouses = warehouses;
        try {
            this.warehousesData = (warehouses!=null && !warehouses.isEmpty()) ? JSONUtil.serialize(warehouses) : null;
        } catch (JSONException e) {
            log.error(e.getMessage(), e);
         }
    }

    public String getPartNumberForSuppliers() {
        if (product!=null) {
            return (!isEmpty(product.getMfgPartnumber())) ? product.getMfgPartnumber() : product.getPartNumber();
        }
        return null;
    }

    public String toString() {
        return new ToStringBuilder(this)
            .append("id", getId())
            .toString();
    }

    public boolean equals(Object other) {
        if ( (this == other ) ) return true;
        if ( !(other instanceof ProductProvider) ) return false;
        ProductProvider castOther = (ProductProvider) other;
        return new EqualsBuilder()
            .append(this.getProduct(), castOther.getProduct())
            .append(this.getProvider(), castOther.getProvider())
            .isEquals();
    }

    public int hashCode() {
        return new HashCodeBuilder()
            .append(getId())
            .toHashCode();
    }

}
