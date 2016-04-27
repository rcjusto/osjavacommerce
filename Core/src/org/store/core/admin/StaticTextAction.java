package org.store.core.admin;

import org.store.core.beans.StaticText;
import org.store.core.beans.StaticTextLang;
import org.store.core.beans.utils.BreadCrumb;
import org.store.core.beans.utils.DataNavigator;
import org.store.core.globals.SomeUtils;
import org.store.core.globals.StoreMessages;
import org.store.core.utils.feeds.Rss2;
import org.apache.commons.lang.StringUtils;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Actions;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletResponse;

@Namespace(value = "/admin")
@ParentPackage(value = "store-admin")
public class StaticTextAction extends AdminModuleAction implements StoreMessages {

    @Override
    public void prepare() throws Exception {
        staticText = (StaticText) dao.get(StaticText.class, staticTextId);
    }

    @Action(value = "importstaticnews", params = {"filterType","news"}, results = @Result(type = "redirectAction", location = "liststaticpage?filterType=${filterType}"))
    public String importNews() throws Exception {
        if (StringUtils.isNotEmpty(fileName)) {
            try {
                Rss2 rss = new Rss2(fileName);
                if (!rss.getItems().isEmpty()) {
                    for (Rss2.Item item : rss.getItems()) {
                        StaticText st = new StaticText();
                        st.setContentUrl(item.getLink());
                        st.setInventaryCode(getStoreCode());
                        st.setTextType(StaticText.TYPE_NEWS);
                        st.setContentDate(item.getParsedPubUpdate());
                        dao.save(st);
                        for (String lang : getLanguages()) {
                            StaticTextLang stl = new StaticTextLang();
                            stl.setStaticLang(lang);
                            stl.setStaticText(st);
                            stl.setTitle(item.getTitle());
                            stl.setValue(item.getDescription());
                            dao.save(stl);
                        }
                        dao.updateStaticTextUrlCode(st, null, getDefaultLanguage());
                    }
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }

        return SUCCESS;
    }

    @Actions({
            @Action(value = "liststaticnews", params = {"filterType","news"} ,results = {
                    @Result(type = "velocity", location = "/WEB-INF/views/admin/statictextlist.vm"),
                    @Result(type = "velocity", name="modal", location = "/WEB-INF/views/admin/statictextlistmodal.vm")
            }),
            @Action(value = "liststaticpage", params = {"filterType","page"} ,results = {
                    @Result(type = "velocity", location = "/WEB-INF/views/admin/statictextlist.vm"),
                    @Result(type = "velocity", name="modal", location = "/WEB-INF/views/admin/statictextlistmodal.vm")
            }),
            @Action(value = "liststaticblock", params = {"filterType","block"} ,results = {
                    @Result(type = "velocity", location = "/WEB-INF/views/admin/staticblocklist.vm"),
                    @Result(type = "velocity", name="modal", location = "/WEB-INF/views/admin/staticblocklist.vm")
            })
    })
    public String statictextlist() throws Exception {
        if (getModal()) {
            List<StaticText> l = dao.getStaticTexts(filterType);
            Map map = new HashMap();
            for (StaticText s : l) {
                String group = (StringUtils.isNotEmpty(s.getTextType())) ? s.getTextType() : getText(CNT_NO_NAME);
                List<StaticText> lista = null;
                if (map.containsKey(group)) lista = (List<StaticText>) map.get(group);
                if (lista == null) {
                    lista = new ArrayList<StaticText>();
                    map.put(group, lista);
                }
                lista.add(s);
            }
            addToStack("staticTextMap", map);
            return "modal";
        } else {

            if (selecteds != null && selecteds.length > 0) {
                for (Long id : selecteds) {
                    StaticText bean = (StaticText) dao.get(StaticText.class, id);
                    if (bean != null) {
                        dao.deleteStaticText(bean);
                    }
                }
                dao.flushSession();
            }

            DataNavigator staticTextList = new DataNavigator(getRequest(), "staticTextList");
            staticTextList.setListado(dao.getStaticTexts(staticTextList, filterType, filterName));
            addToStack("staticTextList", staticTextList);
            staticTextList.saveToSession(request);
        }
        getBreadCrumbs().add(new BreadCrumb(null, getText("admin.static."+filterType+".list"), null, null));
        return SUCCESS;
    }

    @Actions({
            @Action(value = "editstaticnews", params = {"filterType","news"}, results = @Result(type = "velocity", location = "/WEB-INF/views/admin/statictextedit.vm")),
            @Action(value = "editstaticpage", params = {"filterType","page"}, results = @Result(type = "velocity", location = "/WEB-INF/views/admin/statictextedit.vm")),
            @Action(value = "editstaticblock", params = {"filterType","block"}, results = @Result(type = "velocity", location = "/WEB-INF/views/admin/statictextedit.vm"))
    })
    public String statictextedit() throws Exception {
        if (staticText == null) {
            staticText = dao.getStaticText(staticId, filterType);
            if (staticText == null) {
                staticText = new StaticText();
                staticText.setTextType(filterType);
                staticText.setCode(staticId);
                staticText.setInventaryCode(getStoreCode());
            }
        }
        getBreadCrumbs().add(new BreadCrumb(null, getText("admin.static."+filterType+".list"), url("liststatic"+filterType,"/admin"), null));
        getBreadCrumbs().add(new BreadCrumb(null, getText(staticText!=null ? "admin.static."+filterType+".modify" : "admin.static."+filterType+".new"), null, null));
        return SUCCESS;
    }

    @Actions({
            @Action(value = "savestaticnews", params = {"filterType","news"}, results = {
                    @Result(type = "velocity", name="input", location = "/WEB-INF/views/admin/statictextedit.vm"),
                    @Result(type = "redirectAction", location = "liststaticpage?filterType=${filterType}")
            }),
            @Action(value = "savestaticpage", params = {"filterType","page"}, results = {
                    @Result(type = "velocity", name="input", location = "/WEB-INF/views/admin/statictextedit.vm"),
                    @Result(type = "redirectAction", location = "liststaticpage?filterType=${filterType}")
            }),
            @Action(value = "savestaticblock", params = {"filterType","block"}, results = {
                    @Result(type = "velocity", name="input", location = "/WEB-INF/views/admin/statictextedit.vm"),
                    @Result(type = "redirectAction", location = "liststaticblock?filterType=${filterType}")
            })
    })
    public String statictextsave() throws Exception {
        if (staticText != null) {
            if (dao.canSaveStaticText(staticText)) {
                staticText.setLastModified(Calendar.getInstance().getTime());
                if (StringUtils.isNotEmpty(filterType)) staticText.setTextType(filterType);
                Date date = SomeUtils.strToDate(contentDate, getDefaultLanguage());
                Date time = SomeUtils.strToTime(contentTime);
                if (date != null) {
                    if (time != null) {
                        date.setHours(time.getHours());
                        date.setMinutes(time.getMinutes());
                    }
                    staticText.setContentDate(date);
                }
                staticText.setInventaryCode(getStoreCode());
                dao.save(staticText);
                if (staticTextValue != null && staticTextValue.length > 0) {
                    for (int l = 0; l < getLanguages().length; l++) {
                        String lang = getLanguages()[l];
                        StaticTextLang sl = dao.getStaticTextLang(staticText, lang);
                        if (sl == null) {
                            sl = new StaticTextLang();
                            sl.setStaticText(staticText);
                            sl.setStaticLang(lang);
                        }
                        sl.setTitle((staticTextTitle != null && staticTextTitle.length > l) ? staticTextTitle[l] : null);
                        sl.setValue((staticTextValue != null && staticTextValue.length > l) ? staticTextValue[l] : null);
                        dao.save(sl);
                    }
                }
                dao.updateStaticTextUrlCode(staticText, null, getDefaultLanguage());
            } else {
                addActionError(getText(CNT_ERROR_STATICTEXT_CODE_EXIST, CNT_DEFAULT_ERROR_STATICTEXT_CODE_EXIST));
                return INPUT;
            }
        }
        return SUCCESS;
    }

    @Action("pagesupdateurl")
    public String updateCodeNames() throws Exception
    {
        int oks = 0;
        int errors = 0;
        List<StaticText> listado = this.dao.getStaticTexts("");
        for (StaticText bean : listado) {
            if (StringUtils.isEmpty(bean.getUrlCode())) {
                try
                {
                    if (this.dao.updateStaticTextUrlCode(bean, null, getDefaultLanguage())) {
                        oks++;
                    } else {
                        errors++;
                    }
                }
                catch (Exception e)
                {
                    log.error(e.getMessage(), e);
                }
            }
        }
        this.response.getWriter().println("Pages OK: " + String.valueOf(oks));
        this.response.getWriter().println("Pages Error: " + String.valueOf(errors));
        return null;
    }

    private StaticText staticText;
    private Long staticTextId;
    private String[] staticTextTitle;
    private String[] staticTextValue;
    private String fileName;
    private String filterType;
    private String filterName;
    private String staticId;
    private String contentDate;
    private String contentTime;

    public StaticText getStaticText() {
        return staticText;
    }

    public void setStaticText(StaticText staticText) {
        this.staticText = staticText;
    }

    public Long getStaticTextId() {
        return staticTextId;
    }

    public void setStaticTextId(Long staticTextId) {
        this.staticTextId = staticTextId;
    }

    public String[] getStaticTextTitle() {
        return staticTextTitle;
    }

    public void setStaticTextTitle(String[] staticTextTitle) {
        this.staticTextTitle = staticTextTitle;
    }

    public String[] getStaticTextValue() {
        return staticTextValue;
    }

    public void setStaticTextValue(String[] staticTextValue) {
        this.staticTextValue = staticTextValue;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFilterType() {
        return filterType;
    }

    public void setFilterType(String filterType) {
        this.filterType = filterType;
    }

    public String getFilterName() {
        return filterName;
    }

    public void setFilterName(String filterName) {
        this.filterName = filterName;
    }

    public String getStaticId() {
        return staticId;
    }

    public void setStaticId(String staticId) {
        this.staticId = staticId;
    }

    public String getContentDate() {
        return contentDate;
    }

    public void setContentDate(String contentDate) {
        this.contentDate = contentDate;
    }

    public String getContentTime() {
        return contentTime;
    }

    public void setContentTime(String contentTime) {
        this.contentTime = contentTime;
    }
}
