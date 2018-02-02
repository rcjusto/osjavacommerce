package org.store.importexport.admin;

import org.store.core.beans.LocalizedText;
import org.store.core.beans.StoreProperty;
import org.store.importexport.utils.BeanSerializer;
import org.store.core.beans.utils.ProductFilter;
import org.apache.commons.lang.StringUtils;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.json.JSONException;
import org.apache.struts2.json.JSONUtil;

import java.io.File;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Rogelio Caballero
 * 12/04/12 15:07
 */
@Namespace(value = "/admin")
@ParentPackage(value = "store-admin")
public class ExportProductsAction extends ImportBaseAction {

    public static final String TYPE_PRODUCT = "products";
    private static final String CNT_ERROR_NO_PRODUCTS_TO_EXPORT = "error.no.products.to.export";
    private static final String CNT_DEFAULT_ERROR_NO_PRODUCTS_TO_EXPORT = "There are no products to export. Change filters.";

    @Action(value = "export_products", results = {@Result(type = "velocity", location = "/WEB-INF/views/org/store/importexport/views/export_products.vm")})
    public String exportProducts() throws Exception {
        File[] files = BeanSerializer.getExportedFiles(getServletContext(), getStoreCode(), TYPE_PRODUCT);
        if (files!=null && files.length>0) addToStack("files", files);
        Map<String,String> profiles = getExportedProductProfiles();
        if (profiles!=null) addToStack("profiles",profiles);
        return SUCCESS;
    }

    @Action(value = "export_products_do", results = {
            @Result(type = "velocity", location = "/WEB-INF/views/org/store/importexport/views/export_products_do.vm")
    })
    public String exportProductsDo() throws Exception {
        if (exportField != null && exportField.length > 0) {
            BeanSerializer ser = BeanSerializer.getInstance(getStoreCode(), TYPE_PRODUCT, ".csv");
            List l = dao.listProducts(null, productFilter, filterSupplier);
            if (l!=null && !l.isEmpty()) {
                String exportFile = "/stores/" + getStoreCode() + BeanSerializer.PATH_EXPORT + "/" + TYPE_PRODUCT + "/" + String.valueOf(Calendar.getInstance().getTimeInMillis()) + ".csv";
                ser.exportBeanList(l, exportField, getDefaultLanguage(), getFieldLabelsToExport(), new File(getServletContext().getRealPath(exportFile)));
                addToStack("exportFile", exportFile);
                return SUCCESS;
            } else {
                addActionError(getText(CNT_ERROR_NO_PRODUCTS_TO_EXPORT, CNT_DEFAULT_ERROR_NO_PRODUCTS_TO_EXPORT));
                return INPUT;
            }
        } else {
            addActionError(getText(CNT_ERROR_SELECT_FIELDS_TO_EXPORT, CNT_DEFAULT_ERROR_SELECT_FIELDS_TO_EXPORT));
            return INPUT;
        }
    }

    @Action(value = "export_products_del", results = @Result(type = "redirectAction", location = "export_products"))
    public String exportProductsDelete() throws Exception {
        if (forDelete != null)
            for (String fn : forDelete) {
                File f = new File(getServletContext().getRealPath("/stores/" + getStoreCode() + BeanSerializer.PATH_EXPORT + "/" + TYPE_PRODUCT + "/" + fn));
                if (f.exists()) f.delete();
            }
        return SUCCESS;
    }

    @Action(value = "export_products_profile_save", results = @Result(type = "velocity", location = "/WEB-INF/views/org/store/importexport/views/export_products_profiles.vm"))
    public String saveExportProfile() {
        if (StringUtils.isNotEmpty(profileName) && StringUtils.isNotEmpty(profileField)) {
            Map<String,String> profiles = getExportedProductProfiles();
            if (profiles==null) profiles = new HashMap<String,String>();
            profiles.put(profileName, profileField);
            try {
                String cad = JSONUtil.serialize(profiles);
                StoreProperty bean = dao.getStoreProperty(StoreProperty.PROP_EXPORT_PRODUCT_PROFILES, StoreProperty.TYPE_GENERAL,true);
                bean.setValue(cad);
                dao.save(bean);
            } catch (JSONException e) {
                log.error(e.getMessage(), e);
            }
            if (!profiles.isEmpty()) {
                addToStack("profiles", profiles);
            }
        }
        return SUCCESS;
    }

    @Action(value = "export_products_profile_del", results = @Result(type = "velocity", location = "/WEB-INF/views/org/store/importexport/views/export_products_profiles.vm"))
    public String delExportProfile() {
        if (StringUtils.isNotEmpty(profileName)) {
            Map<String,String> profiles = getExportedProductProfiles();
            if (profiles!=null && profiles.containsKey(profileName)) {
                profiles.remove(profileName);
                try {
                    String cad = (profiles.isEmpty()) ? "" : JSONUtil.serialize(profiles);
                    StoreProperty bean = dao.getStoreProperty(StoreProperty.PROP_EXPORT_PRODUCT_PROFILES, StoreProperty.TYPE_GENERAL,true);
                    bean.setValue(cad);
                    dao.save(bean);
                } catch (JSONException e) {
                    log.error(e.getMessage(), e);
                }
            }
            if (profiles!=null && !profiles.isEmpty()) {
                addToStack("profiles", profiles);
            }
        }
        return SUCCESS;
    }

    private Map<String,String> getExportedProductProfiles() {
        String cad = dao.getStorePropertyValue(StoreProperty.PROP_EXPORT_PRODUCT_PROFILES, StoreProperty.TYPE_GENERAL, null);
        if (StringUtils.isNotEmpty(cad)) {
            try {
                Object o = JSONUtil.deserialize(cad);
                if (o instanceof Map) {
                    return (Map<String, String>) o;
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
        return null;
    }

    @Action(value = "export_products_profiles", results = @Result(type = "velocity", location = "/WEB-INF/views/org/store/importexport/views/export_products_profiles.vm"))
    public String getExportProfiles() {
        Map<String,String> profiles = getExportedProductProfiles();
        if (profiles!=null && !profiles.isEmpty()) {
            addToStack("profiles", profiles);
        }
        return SUCCESS;
    }


    public Map<String, String> getFieldLabelsToExport() {
        if (!requestCache.containsKey("FieldLabelsToExport")) {
            Map<String, String> result = new HashMap<String, String>();
            for (String fn : getProductFields()) {
                LocalizedText st = dao.getLocalizedtext("export." + fn);
                if (st != null) {
                    String fnLabel = st.getValueLang(getDefaultLanguage());
                    dao.evict(st);
                    if (StringUtils.isNotEmpty(fnLabel)) result.put(fn, fnLabel);
                    else result.put(fn, fn);
                } else result.put(fn, fn);
            }
            requestCache.put("FieldLabelsToExport", result);
        }
        return (Map<String, String>) requestCache.get("FieldLabelsToExport");
    }

    private String profileName;
    private String profileField;
    private String[] exportField;
    private String[] forDelete;
    private ProductFilter productFilter;
    private Long filterSupplier;

    public String getProfileName() {
        return profileName;
    }

    public void setProfileName(String profileName) {
        this.profileName = profileName;
    }

    public String getProfileField() {
        return profileField;
    }

    public void setProfileField(String profileField) {
        this.profileField = profileField;
    }

    public String[] getExportField() {
        return exportField;
    }

    public void setExportField(String[] exportField) {
        this.exportField = exportField;
    }

    public String[] getForDelete() {
        return forDelete;
    }

    public void setForDelete(String[] forDelete) {
        this.forDelete = forDelete;
    }

    public ProductFilter getProductFilter() {
        return productFilter;
    }

    public void setProductFilter(ProductFilter productFilter) {
        this.productFilter = productFilter;
    }

    public Long getFilterSupplier() {
        return filterSupplier;
    }

    public void setFilterSupplier(Long filterSupplier) {
        this.filterSupplier = filterSupplier;
    }
}
