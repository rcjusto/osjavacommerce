package org.store.core.beans;

import org.store.core.beans.utils.MultiLangBean;
import org.store.core.beans.utils.StoreBean;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.Calendar;
import java.util.Date;
import java.util.Set;

/**
 * Textos estaticos
 */
@Entity
@Table(name = "t_static_text")
public class StaticText extends BaseBean implements MultiLangBean, StoreBean {

    public static final String TYPE_NEWS = "news";
    public static final String TYPE_PAGE = "page";
    public static final String TYPE_BLOCK = "block";

    public static final String BLOCK_ORDER_APPROVED = "order.approved.text";
    public static final String BLOCK_ORDER_REJECTED = "order.rejected.text";
    public static final String BLOCK_ORDER_PENDING = "order.pending.text";

    public StaticText() {
        Date d = Calendar.getInstance().getTime();
        setLastModified(d);
        setContentDate(d);
    }


    // Codigo q identifica el texto
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 255)
    private String code;

    // Codigo de la tienda a la que pertenece la categoria
    @Column(length = 10)
    private String inventaryCode;

    @Column(length = 50)
    private String textType;

    private Date contentDate;

    @Column(length = 1024)
    private String contentUrl;

    private Date lastModified;

    @Column(length = 512)
    private String urlCode;

    @OneToMany(mappedBy = "staticText", cascade = CascadeType.REMOVE)
    private Set<StaticTextLang> staticTextLangs;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getInventaryCode() {
        return inventaryCode;
    }

    public void setInventaryCode(String inventaryCode) {
        this.inventaryCode = inventaryCode;
    }

    public Date getContentDate() {
        return contentDate;
    }

    public void setContentDate(Date contentDate) {
        this.contentDate = contentDate;
    }

    public Date getLastModified() {
        return lastModified;
    }

    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }

    public String getTextType() {
        return (textType!=null) ? textType : "";
    }

    public void setTextType(String textType) {
        this.textType = textType;
    }

    public String getContentUrl() {
        return contentUrl;
    }

    public void setContentUrl(String contentUrl) {
        this.contentUrl = contentUrl;
    }

    public Set<StaticTextLang> getStaticTextLangs() {
        return staticTextLangs;
    }

    public void setStaticTextLangs(Set<StaticTextLang> staticTextLangs) {
        this.staticTextLangs = staticTextLangs;
    }

    public String getUrlCode() {
        return urlCode;
    }

    public void setUrlCode(String urlCode) {
        this.urlCode = urlCode;
    }

    public String toString() {
        return new ToStringBuilder(this)
                .append("id", getId())
                .toString();
    }

    public StaticTextLang getLanguage(String lang) {
        if (getStaticTextLangs() != null) {
            for (StaticTextLang textLang : getStaticTextLangs()) {
                if (lang.equals(textLang.getStaticLang())) return textLang;
            }
        }
        return null;
    }

    public StaticTextLang getLanguage(String lang, String defLang) {
        if (getStaticTextLangs() != null) {
            for (StaticTextLang textLang : getStaticTextLangs()) {
                if (lang.equals(textLang.getStaticLang()) && textLang.getValue() != null) {
                    return textLang;
                }
            }
            for (StaticTextLang textLang : getStaticTextLangs()) {
                if (defLang.equals(textLang.getStaticLang())) {
                    return textLang;
                }
            }
        }
        return null;
    }
    public String getTitle(String lang) {
        if (getStaticTextLangs() != null) {
            for (StaticTextLang textLang : getStaticTextLangs()) {
                if (lang.equals(textLang.getStaticLang()) && StringUtils.isNotEmpty(textLang.getTitle())) {
                    return textLang.getTitle();
                }
            }
            for (StaticTextLang textLang : getStaticTextLangs()) {
                if (StringUtils.isNotEmpty(textLang.getTitle())) {
                    return textLang.getTitle();
                }
            }
        }
        return "";
    }

    public String getContentValue(String lang) {
        if (getStaticTextLangs() != null) {
            for (StaticTextLang textLang : getStaticTextLangs()) {
                if (lang.equals(textLang.getStaticLang()) && StringUtils.isNotEmpty(textLang.getValue())) {
                    return textLang.getValue();
                }
            }
            for (StaticTextLang textLang : getStaticTextLangs()) {
                if (StringUtils.isNotEmpty(textLang.getValue())) {
                    return textLang.getValue();
                }
            }
        }
        return "";
    }

    public boolean equals(Object other) {
        if ((this == other)) return true;
        if (!(other instanceof StaticText)) return false;
        StaticText castOther = (StaticText) other;
        return new EqualsBuilder()
                .append(this.getId(), castOther.getId())
                .isEquals();
    }

    public int hashCode() {
        return new HashCodeBuilder()
                .append(getId())
                .toHashCode();
    }

}