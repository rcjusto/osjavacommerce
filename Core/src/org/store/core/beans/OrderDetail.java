package org.store.core.beans;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import java.util.List;


@Entity
@Table(name = "t_order_detail")
public class OrderDetail extends BaseBean {

    /**
     * identifier field
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idDetail;

    private Double price;

    private Integer quantity;

    @Column(length = 50)
    private String seldate;
    @Column(length = 50)
    private String seltime;

    // Nombre de la promocion de tipo FREE PRODUCT
    @Column(length = 50)
    private String promotionCode;
    @Column(length = 512)
    private String promotionName;

    @Column(length = 512)
    private String complementName;
    private Double complementValue;

    @OneToMany(mappedBy = "orderDetail")
    @OrderBy(value = "id asc")
    private List<OrderDetailProduct> orderDetailProducts;

    @ManyToOne
    private Order order;


    public OrderDetail() {
    }

    public Long getIdDetail() {
        return idDetail;
    }

    public void setIdDetail(Long idDetail) {
        this.idDetail = idDetail;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Integer getQuantity() {
        return quantity != null ? quantity : 0;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public String getSeldate() {
        return seldate;
    }

    public void setSeldate(String seldate) {
        this.seldate = seldate;
    }

    public String getSeltime() {
        return seltime;
    }

    public void setSeltime(String seltime) {
        this.seltime = seltime;
    }

    public List<OrderDetailProduct> getOrderDetailProducts() {
        return orderDetailProducts;
    }

    public void setOrderDetailProducts(List<OrderDetailProduct> orderDetailProducts) {
        this.orderDetailProducts = orderDetailProducts;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public String getPromotionCode() {
        return promotionCode;
    }

    public void setPromotionCode(String promotionCode) {
        this.promotionCode = promotionCode;
    }

    public String getPromotionName() {
        return promotionName;
    }

    public void setPromotionName(String promotionName) {
        this.promotionName = promotionName;
    }

    public String getComplementName() {
        return complementName;
    }

    public void setComplementName(String complementName) {
        this.complementName = complementName;
    }

    public Double getComplementValue() {
        return complementValue;
    }

    public void setComplementValue(Double complementValue) {
        this.complementValue = complementValue;
    }

    public Double getSubTotal() {
        return (quantity != null && price != null) ? quantity * price : null;
    }

    public OrderDetailProduct getFirstDetailProducts() {
        return (orderDetailProducts != null && !orderDetailProducts.isEmpty()) ? orderDetailProducts.get(0) : null;
    }

    public String getDetailPartNumber() {
        StringBuffer buff = new StringBuffer();
        if (orderDetailProducts != null && !orderDetailProducts.isEmpty()) {
            for (OrderDetailProduct odp : orderDetailProducts) {
                if (StringUtils.isNotEmpty(buff.toString())) buff.append(", ");
                buff.append(odp.getProduct().getPartNumber());
            }
        }
        return buff.toString();
    }

    public String getDetailName(String lang) {
        StringBuffer buff = new StringBuffer();
        if (orderDetailProducts != null && !orderDetailProducts.isEmpty()) {
            for (OrderDetailProduct odp : orderDetailProducts) {
                if (StringUtils.isNotEmpty(buff.toString())) buff.append(", ");
                buff.append(odp.getProduct().getProductName(lang));
            }
        }
        return buff.toString();
    }

    public Double getDetailCost() {
        double res = 0.0;
        if (orderDetailProducts != null && !orderDetailProducts.isEmpty()) {
            for (OrderDetailProduct odp : orderDetailProducts) {
                if (odp.getCostPrice() != null)
                    res += odp.getCostPrice();
                else if (odp.getProduct() != null && odp.getProduct().getCostPrice() != null)
                    res += odp.getProduct().getCostPrice();
            }
        }
        return res;
    }

    public Double getSumPrice() {
        double res = 0.0;
        if (orderDetailProducts != null && !orderDetailProducts.isEmpty()) {
            for (OrderDetailProduct odp : orderDetailProducts) {
                res += odp.getProduct().getBasePrice();
            }
        }
        return res;
    }

    public String toString() {
        return new ToStringBuilder(this)
                .append("id", getIdDetail())
                .toString();
    }

}