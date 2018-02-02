package org.store.flashpromo.beans;

import org.store.core.beans.BaseBean;
import org.store.core.beans.utils.StoreBean;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * Rogelio Caballero
 * 11/01/12 17:53
 */
@Entity
@Table(name = "t_flash_page_config")
public class FlashPageConfig extends BaseBean implements StoreBean {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(length = 50)
    private String page;
    private Integer showAfter;
    private Integer hideAfter;
    private Boolean modal;
    private Boolean active;
    private Boolean onlyOnce;

    @Column(length = 10)
    private String inventaryCode;

    @ManyToOne
    private FlashPromoConfig promo;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPage() {
        return page;
    }

    public void setPage(String page) {
        this.page = page;
    }

    public Integer getShowAfter() {
        return (showAfter!=null && showAfter>0) ? showAfter : 0;
    }

    public void setShowAfter(Integer showAfter) {
        this.showAfter = showAfter;
    }

    public Integer getHideAfter() {
        return (hideAfter!=null && hideAfter>0) ? hideAfter : 0;
    }

    public void setHideAfter(Integer hideAfter) {
        this.hideAfter = hideAfter;
    }

    public Boolean getModal() {
        return modal!=null ? modal : false;
    }

    public void setModal(Boolean modal) {
        this.modal = modal;
    }

    public Boolean getActive() {
        return active!=null ? active : false;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Boolean getOnlyOnce() {
        return onlyOnce!=null ? onlyOnce : false;
    }

    public void setOnlyOnce(Boolean onlyOnce) {
        this.onlyOnce = onlyOnce;
    }

    public String getInventaryCode() {
        return inventaryCode;
    }

    public void setInventaryCode(String inventaryCode) {
        this.inventaryCode = inventaryCode;
    }

    public FlashPromoConfig getPromo() {
        return promo;
    }

    public void setPromo(FlashPromoConfig promo) {
        this.promo = promo;
    }
}
