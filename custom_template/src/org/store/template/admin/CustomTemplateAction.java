package org.store.template.admin;

import org.store.core.admin.AdminModuleAction;
import org.store.core.beans.StoreProperty;
import org.store.core.globals.StoreMessages;
import org.store.core.utils.templates.TemplateBannerZone;
import org.store.core.utils.templates.TemplateBlock;
import org.store.core.utils.templates.TemplateConfig;
import org.store.core.utils.templates.TemplateUtils;
import org.store.template.TemplateCssParser;
import org.store.template.TemplateDecorator;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.json.JSONObject;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Rogelio Caballero
 * 14/01/12 21:44
 */
@Namespace(value = "/admin")
@ParentPackage(value = "store-admin")
public class CustomTemplateAction extends AdminModuleAction implements StoreMessages {
    private static final String PROP_CSS_MAP = "current_css_map";

    @Action(value = "custom_template_create_template", results = {@Result(type = "velocity", location = "/WEB-INF/views/org/store/template/views/template_editor.vm")})
    public String createTemplate() throws Exception {
        templateFrom = "custom";
        if (StringUtils.isEmpty(templateCode)) templateCode = templateFrom;
        if (!templateCode.startsWith("custom_")) templateCode = "custom_" + templateCode;
        TemplateConfig config = TemplateUtils.duplicateTemplate(getServletContext(), templateFrom, templateCode);
        if (config != null) {
            log.debug("Template source edit: " + (StringUtils.isNotEmpty(config.getEdit()) ? config.getEdit() : "no es editable"));
            config.setStore(getStoreCode());
            config.setName(templateName);
            config.setEdit("custom_template_edit_template");
            TemplateUtils.saveTemplateConfig(getServletContext(), config);
            templateCode = config.getCode();
            getStoreSessionObjects().put(StoreProperty.PROP_TEMPLATE, templateCode);
        } else {
            log.error("Could not duplicate custom template");
        }
        return editTemplatePage();
    }

    @Action(value = "custom_template_edit_template", results = {@Result(type = "velocity", location = "/WEB-INF/views/org/store/template/views/template_editor.vm")})
    public String editTemplatePage() throws Exception {
        if (StringUtils.isEmpty(templateCode)) templateCode = getTemplate();
        if (StringUtils.isEmpty(pageName)) pageName = "home";

        TemplateDecorator decorator = new TemplateDecorator(this);
        addToStack("decorator", decorator);

        JSONObject obj = new JSONObject();
        List<String> banners = new ArrayList<String>();

        if (getTemplateConfig().getBanners()!=null) for (TemplateBannerZone tbz : getTemplateConfig().getBanners()) banners.add(tbz.getCode());
        obj.put("bannerZone", banners);
        List<String> blocks = new ArrayList<String>();
        if (getTemplateConfig().getBlocks()!=null) for (TemplateBlock tb : getTemplateConfig().getBlocks()) blocks.add(tb.getCode());
        obj.put("blockName", blocks);
        addToStack("ieOptions", obj.toString());

        return SUCCESS;
    }

    @Action(value = "custom_template_save_template", results = {@Result(type = "json", params = {"root", "jsonResp"})})
    public String saveTemplatePage() throws Exception {
        jsonResp = new HashMap<String, Serializable>();

        Map<String, List<Map<String, String>>> map = new HashMap<String, List<Map<String, String>>>();
        if (zones != null && zones.length > 0) {
            for (int i = 0; i < zones.length; i++) {
                String zone = zones[i];
                String name = names != null && names.length > i ? names[i] : "";
                String attr = attrs != null && attrs.length > i ? attrs[i] : "";
                if (StringUtils.isNotEmpty(zone)) {
                    Map<String, String> m = new HashMap<String, String>();
                    m.put("n", name);
                    m.put("a", attr);
                    if (!map.containsKey(zone)) map.put(zone, new ArrayList<Map<String, String>>());
                    map.get(zone).add(m);
                }
            }
        }

        if (!map.isEmpty()) {
            for (Map.Entry<String, List<Map<String, String>>> entry : map.entrySet()) {
                saveTemplateZone(entry.getKey(), entry.getValue());
            }
        }

        jsonResp.put("result", "ok");
        return SUCCESS;
    }

    private void saveTemplateZone(String zone, List<Map<String, String>> value) {
        if (StringUtils.isNotEmpty(templateCode)) {
            StringBuilder builder = new StringBuilder();
            for (Map<String, String> map : value) {
                String name = map.get("n");
                if (StringUtils.isNotEmpty(name)) {
                    builder.append("#zone('" + name + "' '" + map.get("a") + "')");
                }
            }
            File f = new File(getServletContext().getRealPath("/templates/" + templateCode + "/zones/" + zone + ".vm"));
            try {
                FileUtils.writeStringToFile(f, builder.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Action(value = "custom_template_edit_skin", results = {@Result(type = "velocity", location = "/WEB-INF/views/org/store/template/views/skin_editor.vm")})
    public String editSkin() throws Exception {
        List<String> skins = getSkins();

        if (StringUtils.isEmpty(skinName)) skinName = getSkin();
        if (!skins.contains(skinName)) skinName = skins.get(0);

        File f = new File(getServletContext().getRealPath("/templates/" + templateCode + "/skins/" + skinName + "/css/site.css"));
        if (f.exists()) {
            Map map = TemplateCssParser.parseCss(f, getStoreProperty(StoreProperty.PROP_SITE_HOST, "http://dummy.com"));
            getStoreSessionObjects().put(PROP_CSS_MAP, map);
            JSONObject json = new JSONObject(map);
            addToStack("skinCss", json.toString());
        }

        // load skins
        addToStack("skins", skins);
        addToStack("skinCssPath", "/templates/" + templateCode + "/skins/" + skinName + "/css/site.css");

        getStoreSessionObjects().put(StoreProperty.PROP_SKIN, skinName);

        return SUCCESS;
    }

    @Action(value = "custom_template_create_skin", results = {@Result(type = "redirectAction", location = "custom_template_edit_skin?templateCode=${templateCode}&skinName=${newSkin}")})
    public String createSkin() throws Exception {
        if (StringUtils.isEmpty(templateCode)) templateCode = getTemplate();
        List<String> skins = getSkins();
        if (StringUtils.isEmpty(newSkin)) {
            addSessionError(getText("new.skin.name.required"));
            return SUCCESS;
        }
        if (skins.contains(newSkin)) {
            addSessionError(getText("new.skin.name.already.exists"));
            return SUCCESS;
        }

        if (StringUtils.isEmpty(skinName) || !skins.contains(skinName)) skinName = getSkin();

        File newFolder = new File(getServletContext().getRealPath("/templates/" + templateCode + "/skins/" + newSkin));
        if (newFolder.exists()) {
            try {
                FileUtils.deleteDirectory(newFolder);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        File oldFolder = new File(getServletContext().getRealPath("/templates/" + templateCode + "/skins/" + skinName));
        if (!oldFolder.exists()) {
            addSessionError(getText("skin.not.found", "skin.not.found", skinName));
            return SUCCESS;
        }

        try {
            FileUtils.copyDirectory(oldFolder, newFolder);
            getStoreSessionObjects().put(StoreProperty.PROP_SKIN, newSkin);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return SUCCESS;
    }

    @Action(value = "custom_template_save_skin", results = @Result(type = "json", params = {"root", "jsonResp"}))
    public String saveSkin() throws Exception {
        if (StringUtils.isEmpty(templateCode)) templateCode = getTemplate();
        jsonResp = new HashMap<String, Serializable>();
        jsonResp.put("result", "error");
        if (StringUtils.isNotEmpty(skinName) && !"default".equalsIgnoreCase(skinName)) {
            Map map = getStoreSessionObjects().containsKey(PROP_CSS_MAP) ? (Map) getStoreSessionObjects().get(PROP_CSS_MAP) : null;
            if (map != null && StringUtils.isNotEmpty(selector) && StringUtils.isNotEmpty(property)) {
                Map m = map.containsKey(selector) ? (Map) map.get(selector) : null;
                if (m == null) {
                    m = new HashMap();
                    map.put(selector, m);
                }
                if (StringUtils.isNotEmpty(value)) m.put(property, value);
                else if (m.containsKey(selector)) m.remove(selector);

                File f = new File(getServletContext().getRealPath("/templates/" + templateCode + "/skins/" + skinName + "/css/site.css"));
                try {
                    FileUtils.writeStringToFile(f, TemplateCssParser.mapToCss(map), "UTF-8");
                    jsonResp.put("result", "OK");
                    Map mapTemp = new HashMap();
                    mapTemp.put(selector, m);
                    jsonResp.put("css", TemplateCssParser.mapToCss(mapTemp));
                } catch (IOException e) {
                    jsonResp.put("error", e.getMessage());
                }
            } else {
                jsonResp.put("error", "Faltaron datos");
            }
        } else {
            jsonResp.put("error", "El skin por defecto no puede modificarse");
        }
        return SUCCESS;
    }

    private List<String> getSkins() {
        List<String> result = new ArrayList<String>();
        File skinsFolder = new File(getServletContext().getRealPath("/templates/" + templateCode + "/skins"));
        File[] arrSkins = skinsFolder.listFiles(new FileFilter() {
            public boolean accept(File file) {
                return file.isDirectory();
            }
        });
        if (arrSkins != null) {
            for (File file : arrSkins)
                result.add(file.getName());
        }
        return result;
    }

    public TemplateConfig getTemplateConfig() {
        return (StringUtils.isNotEmpty(templateCode)) ? TemplateUtils.getTemplateConfig(getServletContext(), templateCode) : null;
    }

    private String templateCode;
    private String templateFrom;
    private String templateName;

    private String skinName;
    private String newSkin;
    private String selector;
    private String property;
    private String value;

    private String[] zones;
    private String[] names;
    private String[] attrs;

    public String getTemplateFrom() {
        return templateFrom;
    }

    public void setTemplateFrom(String templateFrom) {
        this.templateFrom = templateFrom;
    }

    public String getTemplateCode() {
        return templateCode;
    }

    public void setTemplateCode(String templateCode) {
        this.templateCode = templateCode;
    }

    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    public String[] getZones() {
        return zones;
    }

    public void setZones(String[] zones) {
        this.zones = zones;
    }

    public String[] getNames() {
        return names;
    }

    public void setNames(String[] names) {
        this.names = names;
    }

    public String[] getAttrs() {
        return attrs;
    }

    public void setAttrs(String[] attrs) {
        this.attrs = attrs;
    }

    private String pageName;

    public String getPageName() {
        return pageName;
    }

    public void setPageName(String pageName) {
        this.pageName = pageName;
    }

    public String getSkinName() {
        return skinName;
    }

    public void setSkinName(String skinName) {
        this.skinName = skinName;
    }

    public String getNewSkin() {
        return newSkin;
    }

    public void setNewSkin(String newSkin) {
        this.newSkin = newSkin;
    }

    public String getSelector() {
        return selector;
    }

    public void setSelector(String selector) {
        this.selector = selector;
    }

    public String getProperty() {
        return property;
    }

    public void setProperty(String property) {
        this.property = property;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
