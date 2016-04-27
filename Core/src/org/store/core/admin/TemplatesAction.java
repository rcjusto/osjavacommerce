package org.store.core.admin;

import org.store.core.beans.StoreProperty;
import org.store.core.beans.utils.BreadCrumb;
import org.store.core.beans.utils.DataNavigator;
import org.store.core.globals.StoreMessages;
import org.store.core.utils.templates.TemplateConfig;
import org.store.core.utils.templates.TemplateUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;

import java.util.List;

/**
 * Rogelio Caballero
 * 20/02/12 13:37
 */
@Namespace(value = "/admin")
@ParentPackage(value = "store-admin")
public class TemplatesAction extends AdminModuleAction implements StoreMessages {

    @Action(value = "showtemplates", results = @Result(type = "velocity", location = "/WEB-INF/views/admin/show_templates.vm"))
    public String showTemplates() throws Exception {
        List<TemplateConfig> list = TemplateUtils.getTemplates(getServletContext(), getStoreCode());
        if (list != null && !list.isEmpty()) {
            DataNavigator nav = new DataNavigator(getRequest(), "templates");
            nav.setTotalRows(list.size());
            nav.setPageRows(list.size());
            //nav.setListado(list.subList(nav.getFirstRow() - 1, nav.getLastRow()));
            nav.setListado(list);
            addToStack("templates", nav);
        }

        // active template
        String activeTemplate = getStoreProperty(StoreProperty.PROP_TEMPLATE, StoreProperty.PROP_DEFAULT_TEMPLATE);
        addToStack("currTemplate", TemplateUtils.getTemplateConfig(getServletContext(), activeTemplate));
        addToStack("skins", TemplateUtils.getSkinsForTemplate(getServletContext(), activeTemplate));

        String testingTemplate = getStoreSessionObjects().containsKey(StoreProperty.PROP_TEMPLATE) ? (String) getStoreSessionObjects().get(StoreProperty.PROP_TEMPLATE) : null;
        if (StringUtils.isNotEmpty(testingTemplate)) {
            addToStack("testTemplate", TemplateUtils.getTemplateConfig(getServletContext(), testingTemplate));
            addToStack("testSkins", TemplateUtils.getSkinsForTemplate(getServletContext(), testingTemplate));
        }
        getBreadCrumbs().add(new BreadCrumb(null, getText("admin.menu.templates"), null, null));
        return SUCCESS;
    }

    @Action(value = "changetemplate", results = @Result(type = "redirectAction", location = "showtemplates"))
    public String changeTemplate() throws Exception {
        if (StringUtils.isNotEmpty(newTemplate)) {
            String[] newSkins = TemplateUtils.getSkinsForTemplate(getServletContext(), newTemplate);
            if (newSkins != null && newSkins.length > 0) {
                StoreProperty sp = dao.getStoreProperty(StoreProperty.PROP_TEMPLATE, StoreProperty.TYPE_GENERAL, true);
                sp.setValue(newTemplate);
                dao.save(sp);
                if (!ArrayUtils.contains(newSkins, getSkin())) {
                    StoreProperty sp1 = dao.getStoreProperty(StoreProperty.PROP_SKIN, StoreProperty.TYPE_GENERAL, true);
                    sp1.setValue(newSkins[0]);
                    dao.save(sp1);
                }
            }
        }
        if ("Y".equalsIgnoreCase(removeTest)) {
            unTestTemplate();
        }
        return SUCCESS;
    }

    @Action(value = "testtemplate", results = @Result(type = "redirectAction", location = "showtemplates"))
    public String testTemplate() throws Exception {
        if (StringUtils.isNotEmpty(newTemplate)) {
            String[] newSkins = TemplateUtils.getSkinsForTemplate(getServletContext(), newTemplate);
            if (newSkins != null && newSkins.length > 0) {
                getStoreSessionObjects().put(StoreProperty.PROP_TEMPLATE, newTemplate);
                if (!ArrayUtils.contains(newSkins, getSkin())) {
                    getStoreSessionObjects().put(StoreProperty.PROP_SKIN, newSkins[0]);
                }
            }
        }
        return SUCCESS;
    }

    @Action(value = "untesttemplate", results = @Result(type = "redirectAction", location = "showtemplates"))
    public String unTestTemplate() throws Exception {
        if (getStoreSessionObjects().containsKey(StoreProperty.PROP_TEMPLATE)) getStoreSessionObjects().remove(StoreProperty.PROP_TEMPLATE);
        if (getStoreSessionObjects().containsKey(StoreProperty.PROP_SKIN)) getStoreSessionObjects().remove(StoreProperty.PROP_SKIN);
        return SUCCESS;
    }

    @Action(value = "testskin", results = @Result(type = "redirectAction", location = "showtemplates"))
    public String testSkin() throws Exception {
        if (StringUtils.isNotEmpty(newSkin)) {
            String[] newSkins = TemplateUtils.getSkinsForTemplate(getServletContext(), getTemplate());
            if (newSkins != null && ArrayUtils.contains(newSkins, newSkin)) {
                getStoreSessionObjects().put(StoreProperty.PROP_SKIN, newSkin);
            }
        }
        return SUCCESS;
    }

    @Action(value = "untestskin", results = @Result(type = "redirectAction", location = "showtemplates"))
    public String unTestSkin() throws Exception {
        if (getStoreSessionObjects().containsKey(StoreProperty.PROP_SKIN)) getStoreSessionObjects().remove(StoreProperty.PROP_SKIN);
        return SUCCESS;
    }

    @Action(value = "changeskin", results = @Result(type = "redirectAction", location = "showtemplates"))
    public String changeSkin() throws Exception {
        String[] skins = TemplateUtils.getSkinsForTemplate(getServletContext(), getTemplate());
        if (StringUtils.isNotEmpty(newSkin) && ArrayUtils.contains(skins, newSkin)) {
            StoreProperty sp = dao.getStoreProperty(StoreProperty.PROP_SKIN, StoreProperty.TYPE_GENERAL, true);
            sp.setValue(newSkin);
            dao.save(sp);
        }
        return SUCCESS;
    }


    private String newTemplate;
    private String newSkin;
    private String removeTest;

    public String getNewTemplate() {
        return newTemplate;
    }

    public void setNewTemplate(String newTemplate) {
        this.newTemplate = newTemplate;
    }

    public String getNewSkin() {
        return newSkin;
    }

    public void setNewSkin(String newSkin) {
        this.newSkin = newSkin;
    }

    public String getRemoveTest() {
        return removeTest;
    }

    public void setRemoveTest(String removeTest) {
        this.removeTest = removeTest;
    }
}

