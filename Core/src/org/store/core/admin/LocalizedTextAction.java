package org.store.core.admin;

import org.store.core.beans.LocalizedText;
import org.store.core.beans.utils.BreadCrumb;
import org.store.core.beans.utils.DataNavigator;
import org.store.core.globals.StoreMessages;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;

@Namespace(value = "/admin")
@ParentPackage(value = "store-admin")
public class LocalizedTextAction extends AdminModuleAction implements StoreMessages {

    @Override
    public void prepare() throws Exception {
        localizedText = dao.getLocalizedtext(staticId);
    }

    @Action(value = "staticlabels", params = {"staticTextType", "LABEL"}, results = @Result(type = "velocity", location = "/WEB-INF/views/admin/staticlabels.vm"))
    public String list() throws Exception {
        if (selecteds != null && selecteds.length > 0) {
            for (Long id : selecteds) {
                LocalizedText lt = (LocalizedText) dao.get(LocalizedText.class, id);
                if (lt != null) dao.delete(lt);
            }
            dao.flushSession();
        }
        DataNavigator nav = new DataNavigator(request, "localizedTexts");
        nav.setListado(dao.getLocalizedTexts(nav, filterText));
        addToStack("localizedTexts", nav);
        getBreadCrumbs().add(new BreadCrumb(null, getText("admin.label.list"), null, null));
        return SUCCESS;
    }

    @Action(value = "staticlabeledit", results = {
            @Result(type = "velocity", location = "/WEB-INF/views/admin/staticlabelsedit.vm"),
            @Result(type = "velocity", name = "modal", location = "/WEB-INF/views/admin/staticlabelseditmodal.vm")
    })
    public String edit() throws Exception {
        getBreadCrumbs().add(new BreadCrumb(null, getText("admin.label.list"), url("staticlabels", "/admin"), null));
        getBreadCrumbs().add(new BreadCrumb(null, getText(localizedText != null ? "admin.label.modify" : "admin.label.new"), null, null));
        return (modal != null && modal) ? "modal" : SUCCESS;
    }

    @Action(value = "staticlabelsave", results = @Result(type = "redirectAction", location = "staticlabels"))
    public String save() throws Exception {
        if (localizedText != null) {
            String[] lang = getLanguages();
            for (int i = 0; i < localizedText_value.length; i++) {
                if (i < lang.length) localizedText.addValue(lang[i], localizedText_value[i]);
            }
            localizedText.serialize();
            localizedText.setInventaryCode(getStoreCode());
            dao.save(localizedText);
        }
        return SUCCESS;
    }

    private String filterText;
    private String staticId;
    private String staticTextType;
    private LocalizedText localizedText;
    private String[] localizedText_value;

    public String getFilterText() {
        return filterText;
    }

    public void setFilterText(String filterText) {
        this.filterText = filterText;
    }

    public String getStaticId() {
        return staticId;
    }

    public void setStaticId(String staticId) {
        this.staticId = staticId;
    }

    public String getStaticTextType() {
        return staticTextType;
    }

    public void setStaticTextType(String staticTextType) {
        this.staticTextType = staticTextType;
    }

    public LocalizedText getLocalizedText() {
        return localizedText;
    }

    public void setLocalizedText(LocalizedText localizedText) {
        this.localizedText = localizedText;
    }

    public String[] getLocalizedText_value() {
        return localizedText_value;
    }

    public void setLocalizedText_value(String[] localizedText_value) {
        this.localizedText_value = localizedText_value;
    }
}
