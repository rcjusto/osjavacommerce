package org.store.core.beans;

import org.store.core.beans.utils.StoreBean;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * Fees
 */
@Entity
@Table(name = "t_banners")
public class Banner extends BaseBean implements StoreBean {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(length = 50)
    private String bannerZone;
    @Column(length = 1024)
    private String bannerText;
    @Column(length = 1024)
    private String bannerUrl;
    @Column(length = 20)
    private String bannerTarget;

    // Tienda en la q esta configurado el banner
    @Column(length = 10)
    private String inventaryCode;


    private Integer bannerWidth;
    private Integer bannerHeight;
    private Long bannerHits;

    private Boolean active;

    @ManyToOne
    @NotFound(action = NotFoundAction.IGNORE)
    private Category forCategory;
    @ManyToOne
    @NotFound(action = NotFoundAction.IGNORE)
    private Product forProduct;

    public Banner() {
        bannerHits = 0l;
        bannerTarget = "_blank";
        active = false;
    }

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

    public String getBannerZone() {
        return bannerZone;
    }

    public void setBannerZone(String bannerZone) {
        this.bannerZone = bannerZone;
    }

    public String getBannerText() {
        return bannerText;
    }

    public void setBannerText(String bannerText) {
        this.bannerText = bannerText;
    }

    public String getBannerUrl() {
        return bannerUrl;
    }

    public void setBannerUrl(String bannerUrl) {
        this.bannerUrl = bannerUrl;
    }

    public String getBannerTarget() {
        return bannerTarget;
    }

    public void setBannerTarget(String bannerTarget) {
        this.bannerTarget = bannerTarget;
    }

    public Integer getBannerWidth() {
        return bannerWidth;
    }

    public void setBannerWidth(Integer bannerWidth) {
        this.bannerWidth = bannerWidth;
    }

    public Integer getBannerHeight() {
        return bannerHeight;
    }

    public void setBannerHeight(Integer bannerHeight) {
        this.bannerHeight = bannerHeight;
    }

    public Long getBannerHits() {
        return bannerHits;
    }

    public void setBannerHits(Long bannerHits) {
        this.bannerHits = bannerHits;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Category getForCategory() {
        return forCategory;
    }

    public void setForCategory(Category forCategory) {
        this.forCategory = forCategory;
    }

    public Product getForProduct() {
        return forProduct;
    }

    public void setForProduct(Product forProduct) {
        this.forProduct = forProduct;
    }

    public String toString() {
        return new ToStringBuilder(this)
                .append("id", getId())
                .toString();
    }

    public boolean equals(Object other) {
        if ((this == other)) return true;
        if (!(other instanceof Banner)) return false;
        Banner castOther = (Banner) other;
        return new EqualsBuilder()
                .append(this.getId(), castOther.getId())
                .isEquals();
    }


    public void addHit() {
        if (bannerHits==null) bannerHits = 0l;
        bannerHits ++;
    }
}