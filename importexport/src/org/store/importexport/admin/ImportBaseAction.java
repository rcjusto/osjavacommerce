package org.store.importexport.admin;

import org.store.core.admin.AdminModuleAction;
import org.store.core.beans.Provider;
import org.store.core.globals.StoreThread;
import org.store.core.utils.quartz.ThreadUtilities;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Rogelio Caballero
 * 12/04/12 20:48
 */
public class ImportBaseAction extends AdminModuleAction {

    public static final String THREAD_IMPORT_NAME = "importing_";
    protected static final String ERROR_FILE_NOT_SELECTED = "file.not.selected";
    protected static final String ERROR_DEFAULT_FILE_NOT_SELECTED = "You must select a valid file";

    protected StoreThread getThread() {
        Thread th = ThreadUtilities.getThread(THREAD_IMPORT_NAME + getStoreCode());
        return (th != null && th instanceof StoreThread) ? (StoreThread) th : null;
    }

    public List<String> getProductFields() {
        List<String> res = new ArrayList<String>();
        res.addAll(Arrays.asList("partNumber", "mfgPartnumber", "lang.productName", "lang.description", "lang.information", "lang.features", "lang.searchKeywords", "lang.seoTitle", "lang.seoDescription", "lang.seoKeywords", "lang.urlCode", "lang.caract1", "lang.caract2", "lang.caract3", "active", "archived", "markupFactor", "erMarkupFactor", "stock", "oldPrice", "listPrice", "costPrice", "price", "eta", "deliveryTime", "sales", "extraSales", "stockMin", "productTemplate", "ratingBy", "mailVendor", "affiliatePercent", "warranty", "warrantyPrice", "warrantyPercent", "taxable", "needShipping", "dimentionWidth", "dimentionHeight", "dimentionLength", "weight", "dateSelection", "timeSelection", "manufacturer.manufacturerName", "categories"));
        List<Provider> l = dao.getProviders();
        if (l != null) for (Provider p : l) {
            if (StringUtils.isNotEmpty(p.getProviderName())) {
                res.add("supplier.sku." + p.getProviderName());
                res.add("supplier.stock." + p.getProviderName());
                res.add("supplier.active." + p.getProviderName());
                res.add("supplier.cost." + p.getProviderName());
            }
        }
        return res;
    }

    public List<String> getCustomerFields() {
        List<String> res = new ArrayList<String>();
        res.addAll(Arrays.asList("email", "userId", "title", "firstname", "lastname", "sex", "birthday", "registerDate", "visits", "rewardPoints", "phone", "lastIP", "lastBrowser", "language", "level.code"));
        return res;
    }

    public List<String> getCategoryFields() {
        List<String> res = new ArrayList<String>();
        res.addAll(Arrays.asList("idCategory", "idParent", "active", "categoryName", "description", "urlCode", "template", "productTemplate", "markupFactor", "erMarkupFactor", "caract1", "caract2", "caract3", "dimentionLength", "dimentionWidth", "dimentionHeight", "weight", "freeShipping", "needShipping", "timeSelection", "dateSelection", "deliveryTime", "stockMin"));
        return res;
    }

    protected File importFile;
    protected String fileName;
    protected List fields;

    public File getImportFile() {
        return importFile;
    }

    public void setImportFile(File importFile) {
        this.importFile = importFile;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public List getFields() {
        return fields;
    }

    public void setFields(List fields) {
        this.fields = fields;
    }

}
