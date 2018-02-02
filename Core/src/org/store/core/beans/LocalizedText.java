package org.store.core.beans;

import org.store.core.beans.utils.StoreBean;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.struts2.json.JSONException;
import org.apache.struts2.json.JSONUtil;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.util.HashMap;

/**
 * Textos estaticos
 */
@Entity
@Table(name = "t_localized_text")
public class LocalizedText extends BaseBean implements StoreBean {

    // Codigo q identifica el texto
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 255)
    private String code;

    // Codigo de la tienda a la que pertenece la categoria
    @Column(length = 10)
    private String inventaryCode;

    // Categoria de texto estatico
    @Column(length = 255)
    private String category;

    // Valores del texto
    @Lob
    private String value;

    @Transient
    private HashMap<String, String> values;

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

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getValue(String lang) {
        String res = getValueLang(lang);
        if (isEmpty(res) && values != null) {
            for (String v : values.values()) {
                if (!isEmpty(v)) res = v;
            }
        }
        return res;
    }

    public String getValueLang(String lang) {
        if (values == null || values.size() < 1) deserialize();
        return (values != null && values.containsKey(lang)) ? values.get(lang) : null;
    }

    public void addValue(String lang, String value)  {
        if (values==null) values = new HashMap<String,String>();
        values.put(lang, value);
        serialize();
    }

    public String toString() {
        return new ToStringBuilder(this)
                .append("id", getId())
                .toString();
    }

    public void serialize() {
        try {
            value =  (values!=null && values.size()>0) ? JSONUtil.serialize(values) :null;
        } catch (JSONException e) {
            log.error(e.getMessage(), e);
        }
    }

    public void deserialize() {
        try {
            values =  (!isEmpty(value)) ? (HashMap) JSONUtil.deserialize(value) : null;
        } catch (Exception ignored) {}
    }

}