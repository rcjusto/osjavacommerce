package org.store.core.beans;

import org.store.core.beans.utils.StoreBean;
import org.apache.commons.lang.builder.EqualsBuilder;
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
import java.util.Map;

/**
 * Campaign
 */
@Entity
@Table(name = "t_codes")
public class Codes extends BaseBean implements StoreBean {


    public static final String TYPE_BIC = "BIC";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 10)
    private String inventaryCode;

    @Column(length = 250)
    private String code;

    @Column(length = 50)
    private String type;

    private Boolean active;

    @Lob
    private String text;
    @Transient
    private Map<String,String> textMap;

    public Codes() {
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

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Boolean getActive() {
        return active!=null && active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getText(String lang) {
        if (textMap == null || textMap.size() < 1) try {textMap =  (!isEmpty(text)) ? (Map) JSONUtil.deserialize(text) : null;} catch (Exception ignored) {}
        return (textMap != null && textMap.containsKey(lang)) ? textMap.get(lang) : null;
    }

    public void setText(String lang, String value) {
        if (textMap==null) textMap = new HashMap<String,String>();
        textMap.put(lang, value);
        try {text = (textMap!=null && textMap.size()>0) ? JSONUtil.serialize(textMap) :null;} catch (JSONException ignored) {}
    }

    public String toString() {
        return new ToStringBuilder(this)
                .append("id", getId())
                .toString();
    }

    public boolean equals(Object other) {
        if ((this == other)) return true;
        if (!(other instanceof Codes)) return false;
        Codes castOther = (Codes) other;
        return new EqualsBuilder()
                .append(this.getId(), castOther.getId())
                .isEquals();
    }



}