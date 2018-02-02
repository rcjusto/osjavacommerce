package org.store.flashpromo.beans;

import org.store.core.beans.BaseBean;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * Rogelio Caballero
 * 11/01/12 17:53
 */
@Entity
@Table(name = "t_flash_promo_config_lang")
public class FlashPromoConfigLang extends BaseBean {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(length = 5)
    private String lang;
    @Lob
    private String content;
    @ManyToOne
    private FlashPromoConfig config;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public FlashPromoConfig getConfig() {
        return config;
    }

    public void setConfig(FlashPromoConfig config) {
        this.config = config;
    }
}
