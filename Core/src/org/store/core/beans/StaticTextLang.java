package org.store.core.beans;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * Textos estaticos
 */
@Entity
@Table(name = "t_static_text_lang")
public class StaticTextLang extends BaseBean {

    // Codigo q identifica el texto
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Idioma de texto estatico
    @Column(length = 5)
    private String staticLang;

    // Valores del texto
    @Lob
    private String title;

    // Valores del texto
    @Lob
    private String value;

    @ManyToOne
    private StaticText staticText;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getStaticLang() {
        return staticLang;
    }

    public void setStaticLang(String staticLang) {
        this.staticLang = staticLang;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public StaticText getStaticText() {
        return staticText;
    }

    public void setStaticText(StaticText staticText) {
        this.staticText = staticText;
    }

    public String toString() {
        return new ToStringBuilder(this)
                .append("text", getStaticText())
                .append("lang", getStaticLang())
                .toString();
    }

    public boolean equals(Object other) {
        if ((this == other)) return true;
        if (!(other instanceof StaticTextLang)) return false;
        StaticTextLang castOther = (StaticTextLang) other;
        return new EqualsBuilder()
                .append(this.getStaticText(), castOther.getStaticText())
                .append(this.getStaticLang(), castOther.getStaticLang())
                .isEquals();
    }

    public int hashCode() {
        return new HashCodeBuilder()
                .append(getStaticText())
                .append(getStaticLang())
                .toHashCode();
    }


}