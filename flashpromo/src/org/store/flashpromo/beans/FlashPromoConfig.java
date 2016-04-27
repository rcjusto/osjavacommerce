package org.store.flashpromo.beans;

import org.store.core.beans.BaseBean;
import org.store.core.beans.utils.StoreBean;
import org.apache.commons.lang.StringUtils;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Rogelio Caballero
 * 11/01/12 17:53
 */
@Entity
@Table(name = "t_flash_promo_config")
public class FlashPromoConfig extends BaseBean implements StoreBean {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Integer width;
    @Column(length = 255)
    private String name;
    @Column(length = 10)
    private String horizontal;
    @Column(length = 10)
    private String vertical;

    private Date created;
    private Boolean subscription;
    private Long subscriptionGroup;

    @Column(length = 10)
    private String inventaryCode;

    @OneToMany(mappedBy = "config",fetch = FetchType.LAZY)
    private List<FlashPromoConfigLang> langs;

    public FlashPromoConfig() {
        this.created = Calendar.getInstance().getTime();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHorizontal() {
        return (StringUtils.isNotEmpty(horizontal)) ? horizontal : "right";
    }

    public void setHorizontal(String horizontal) {
        this.horizontal = horizontal;
    }

    public String getVertical() {
        return (StringUtils.isNotEmpty(vertical)) ? vertical : "bottom";
    }

    public void setVertical(String vertical) {
        this.vertical = vertical;
    }

    public Integer getWidth() {
        return (width!=null && width>0) ? width : 250;
    }

    public void setWidth(Integer width) {
        this.width = width;
    }

    public Boolean getSubscription() {
        return subscription;
    }

    public void setSubscription(Boolean subscription) {
        this.subscription = subscription;
    }

    public Long getSubscriptionGroup() {
        return subscriptionGroup;
    }

    public void setSubscriptionGroup(Long subscriptionGroup) {
        this.subscriptionGroup = subscriptionGroup;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public String getInventaryCode() {
        return inventaryCode;
    }

    public void setInventaryCode(String inventaryCode) {
        this.inventaryCode = inventaryCode;
    }

    public List<FlashPromoConfigLang> getLangs() {
        return langs;
    }

    public void setLangs(List<FlashPromoConfigLang> langs) {
        this.langs = langs;
    }

    public FlashPromoConfigLang getLanguage(String lang) {
        if (StringUtils.isNotEmpty(lang) && langs!=null && !langs.isEmpty()) {
            for(FlashPromoConfigLang l : langs) {
                if (lang.equalsIgnoreCase(l.getLang())) return l;
            }
        }
        return null;
    }

    public String getContent(String lang) {
        if (StringUtils.isNotEmpty(lang) && langs!=null && !langs.isEmpty()) {
            // buscar la del idioma
            for(FlashPromoConfigLang l : langs) {
                if (lang.equalsIgnoreCase(l.getLang()) && StringUtils.isNotEmpty(l.getContent()))
                    return l.getContent();
            }
            // sino hay del idioma, devolver la primera q tenga algo
            for(FlashPromoConfigLang l : langs) {
                if (StringUtils.isNotEmpty(l.getContent()))
                    return l.getContent();
            }
        }
        return null;
    }


}
