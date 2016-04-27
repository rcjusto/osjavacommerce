package org.store.core.admin;

import org.store.core.beans.StoreProperty;
import org.store.core.beans.utils.BreadCrumb;
import org.store.core.globals.StoreMessages;
import org.apache.commons.lang.StringUtils;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Namespace(value = "/admin")
@ParentPackage(value = "store-admin")
public class StorePropertiesAction extends AdminModuleAction implements StoreMessages {

    @Action(value = "storeproperties", results = @Result(type = "velocity", location = "/WEB-INF/views/admin/storeproperties.vm"))
    public String storeproperties() throws Exception {
        List<StoreProperty> l = dao.getStoreProperties(StoreProperty.TYPE_GENERAL);
        Map<String, StoreProperty> storeProperties = new HashMap<String, StoreProperty>();
        for (StoreProperty bean : l) storeProperties.put(bean.getCode(), bean);
        addToStack("storeProperties", storeProperties);

        File f1 = new File(getServletContext().getRealPath("/stores/" + getStoreCode()+"/sitemap.xml"));
        if (f1.exists()) addToStack("xml_sitemap", f1);
        List<File> catalogs = new ArrayList<File>();
        int index = 0;
        File f2 = new File(getServletContext().getRealPath("/stores/" + getStoreCode()+"/fullcatalog_" + index + ".xml"));
        while (f2.exists()) {
            catalogs.add(f2);
            index++;
            f2 = new File(getServletContext().getRealPath("/stores/" + getStoreCode()+"/fullcatalog_" + index + ".xml"));
        }
        if (!catalogs.isEmpty()) addToStack("xml_catalog", catalogs);

        getBreadCrumbs().add(new BreadCrumb(null, getText("admin.menu.store.properties"), null, null));
        return SUCCESS;
    }

    @Action(value = "storepropertiessave", results = @Result(type = "redirectAction", location = "storeproperties"))
    public String storepropertiessave() throws Exception {
        if (storeProperties_id != null && storeProperties_id.length > 0) {
            for (int i = 0; i < storeProperties_id.length; i++) {
                if (!StringUtils.isEmpty(storeProperties_id[i]) && storeProperties_value.length > i) {
                    StoreProperty bean = dao.getStoreProperty(storeProperties_id[i], storeProperties_type);
                    if (bean == null) {
                        bean = new StoreProperty();
                        bean.setCode(storeProperties_id[i]);
                    }
                    bean.setType(storeProperties_type);
                    bean.setValue(storeProperties_value[i]);
                    bean.setInventaryCode(getStoreCode());
                    dao.save(bean);
                }
            }
        }

        // Actualizar propiedades que dependen del idioma
        StoreProperty beanSeoTitle = dao.getStoreProperty("seo.title", StoreProperty.TYPE_GENERAL, true);
        StoreProperty beanSeoDescr = dao.getStoreProperty("seo.description", StoreProperty.TYPE_GENERAL, true);
        StoreProperty beanSeoKeywo = dao.getStoreProperty("seo.keywords", StoreProperty.TYPE_GENERAL, true);
        StoreProperty beanSeoAbstr = dao.getStoreProperty("seo.abstract", StoreProperty.TYPE_GENERAL, true);
        for (int l = 0; l < getLanguages().length; l++) {
            String lang = getLanguages()[l];
            if (metaTitle != null && metaTitle.length > l && StringUtils.isNotEmpty(metaTitle[l])) beanSeoTitle.addValue(lang, metaTitle[l]);
            if (metaDescription != null && metaDescription.length > l && StringUtils.isNotEmpty(metaDescription[l])) beanSeoDescr.addValue(lang, metaDescription[l]);
            if (metaKeywords != null && metaKeywords.length > l && StringUtils.isNotEmpty(metaKeywords[l])) beanSeoKeywo.addValue(lang, metaKeywords[l]);
            if (metaAbstract != null && metaAbstract.length > l && StringUtils.isNotEmpty(metaAbstract[l])) beanSeoAbstr.addValue(lang, metaAbstract[l]);
        }
        beanSeoTitle.setInventaryCode(getStoreCode());
        dao.save(beanSeoTitle);
        beanSeoDescr.setInventaryCode(getStoreCode());
        dao.save(beanSeoDescr);
        beanSeoKeywo.setInventaryCode(getStoreCode());
        dao.save(beanSeoKeywo);
        beanSeoAbstr.setInventaryCode(getStoreCode());
        dao.save(beanSeoAbstr);
        return SUCCESS;
    }

    private String propertyType;
    private String[] storeProperties_id;
    private String storeProperties_type;
    private String[] storeProperties_value;
    private String[] metaTitle;
    private String[] metaDescription;
    private String[] metaKeywords;
    private String[] metaAbstract;

    public String getPropertyType() {
        return propertyType;
    }

    public void setPropertyType(String propertyType) {
        this.propertyType = propertyType;
    }

    public String[] getStoreProperties_id() {
        return storeProperties_id;
    }

    public void setStoreProperties_id(String[] storeProperties_id) {
        this.storeProperties_id = storeProperties_id;
    }

    public String getStoreProperties_type() {
        return storeProperties_type;
    }

    public void setStoreProperties_type(String storeProperties_type) {
        this.storeProperties_type = storeProperties_type;
    }

    public String[] getStoreProperties_value() {
        return storeProperties_value;
    }

    public void setStoreProperties_value(String[] storeProperties_value) {
        this.storeProperties_value = storeProperties_value;
    }

    public String[] getMetaTitle() {
        return metaTitle;
    }

    public void setMetaTitle(String[] metaTitle) {
        this.metaTitle = metaTitle;
    }

    public String[] getMetaDescription() {
        return metaDescription;
    }

    public void setMetaDescription(String[] metaDescription) {
        this.metaDescription = metaDescription;
    }

    public String[] getMetaKeywords() {
        return metaKeywords;
    }

    public void setMetaKeywords(String[] metaKeywords) {
        this.metaKeywords = metaKeywords;
    }

    public String[] getMetaAbstract() {
        return metaAbstract;
    }

    public void setMetaAbstract(String[] metaAbstract) {
        this.metaAbstract = metaAbstract;
    }
}
