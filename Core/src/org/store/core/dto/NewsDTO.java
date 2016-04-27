package org.store.core.dto;

import org.store.core.beans.StaticText;
import org.store.core.beans.StaticTextLang;
import org.store.core.globals.BaseAction;
import org.apache.commons.lang.StringUtils;
import org.apache.struts2.ServletActionContext;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class NewsDTO {

    private Long id;
    private String title;
    private String resume;
    private String link;
    private Date date;

    public NewsDTO(StaticText bean) {
        BaseAction action = (ServletActionContext.getContext().getActionInvocation().getAction() instanceof BaseAction) ? (BaseAction) ServletActionContext.getContext().getActionInvocation().getAction() : null;
        this.id = bean.getId();
        this.date = bean.getContentDate();
        if (action != null) {
            StaticTextLang beanLang = bean.getLanguage(action.getLocale().getLanguage(), action.getDefaultLanguage());
            if (beanLang!=null) {
                this.title = beanLang.getTitle();
                if (StringUtils.isNotEmpty(bean.getContentUrl())) {
                    this.resume = action.extractText(beanLang.getValue());
                    this.link = bean.getContentUrl();
                } else {
                    if (StringUtils.isNotEmpty(beanLang.getValue())) {
                        this.resume = action.extractText(beanLang.getValue());
                    }
                    Map<String,String> params = new HashMap<String,String>();
                    if (StringUtils.isNotEmpty(bean.getUrlCode())) params.put("code",bean.getUrlCode());
                    else params.put("idStaticText", bean.getId().toString());
                    this.link = action.url("page","",params, true);
                }
            }
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getResume() {
        return resume;
    }

    public void setResume(String resume) {
        this.resume = resume;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}