package org.store.core.beans;

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
 * Jobs
 */
@Entity
@Table(name = "t_help")
public class Help extends BaseBean {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 250)
    private String page;

    @Column(length = 250)
    private String code;

    @Lob
    private String helpContent;
    @Transient
    private Map<String,String> langMap;

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

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getHelpContent() {
        return helpContent;
    }

    public void setHelpContent(String helpContent) {
        this.helpContent = helpContent;
    }

    public String getHelpContent(String key) {
        if (langMap == null || langMap.size() < 1) deserialize();
        return (langMap != null && langMap.containsKey(key)) ? langMap.get(key) : null;
    }

    public void setHelpContent(String key, String value) {
        if (langMap==null) langMap = new HashMap<String,String>();
        langMap.put(key, value);
        serialize();
    }

    public void serialize() {
        try {
            helpContent =  (langMap!=null && langMap.size()>0) ? JSONUtil.serialize(langMap) :null;
        } catch (JSONException e) {
            log.error(e.getMessage(), e);
        }
    }

    public void deserialize() {
        try {
            langMap =  (!isEmpty(helpContent)) ? (HashMap) JSONUtil.deserialize(helpContent) : null;
        } catch (JSONException e) {
            log.error(e.getMessage(), e);
        }
    }
    public String toString() {
        return new ToStringBuilder(this)
                .append("id", getId())
                .toString();
    }

    public boolean equals(Object other) {
        if ((this == other)) return true;
        if (!(other instanceof Help)) return false;
        Help castOther = (Help) other;
        return new EqualsBuilder()
                .append(this.getId(), castOther.getId())
                .isEquals();
    }

}