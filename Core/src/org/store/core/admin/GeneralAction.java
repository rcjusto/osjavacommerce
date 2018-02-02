package org.store.core.admin;

import org.store.core.beans.*;
import org.store.core.globals.StoreMessages;
import org.store.core.utils.merchants.MerchantService;
import org.store.core.utils.merchants.MerchantUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Namespace(value = "/admin")
@ParentPackage(value = "store-admin")
public class GeneralAction extends AdminModuleAction implements StoreMessages {

    @Action(value = "home", results = @Result(type = "velocity", location = "/WEB-INF/views/admin/home.vm"))
    public String home() throws Exception {
        String ips = getStoreProperty(StoreProperty.PROP_STATISTICS_EXCLUDED_IPS, null);
        String[] arrIp = (StringUtils.isNotEmpty(ips)) ? ips.split(",") : null;

        Map<String, Date> mapDates = dao.getReportDates();
        addToStack("dates", mapDates);
        addToStack("hits", dao.getHits(mapDates, false, arrIp));
        addToStack("logins", dao.getHits(mapDates, true, arrIp));
        addToStack("registrations", dao.getRegistrations(mapDates, true));
        addToStack("orders", dao.getOrderStats(mapDates, null));

        List<OrderStatus> statusList = getOrderStatusList();
        Map<OrderStatus, Object> mapOrders = new HashMap<OrderStatus, Object>();
        for (OrderStatus status : statusList) {
            mapOrders.put(status, dao.getOrderStats(mapDates, status));
        }
        addToStack("ordersDetail", mapOrders);
        addToStack("homeStats", dao.getHomeStats());
        addToStack("reviews", dao.getPendingReviews());
        addToStack("stockAlerts", dao.getStockAlertsStats());
        addToStack("cartAlerts", dao.getCartAlerts());

        return SUCCESS;
    }

    @Action(value = "adminWarnings", results = @Result(type = "velocity", location = "/WEB-INF/views/admin/warnings.vm"))
    public String showGlobalWarnings() throws Exception {

        // validate payment methods
        boolean hayMetodoPago = false;
        MerchantUtils mu = new MerchantUtils(getServletContext());
        Map<String, Class> map = mu.getMerchants();
        if (map != null && !map.isEmpty()) {
            for (String serviceName : map.keySet()) {
                MerchantService service = mu.getService(serviceName, this);
                if (service != null && service.isActive()) {
                    hayMetodoPago = true;
                    break;
                }
            }
        }
        if (!hayMetodoPago) addActionError("payment");

        // validate shipping methods
        boolean hayShipping = false;
        List<String> carriers = dao.getCarriers(true);
        if (carriers.isEmpty()) {
            ShippingMethod defaultMethod = dao.getDefaultShippingMethod();
            if (defaultMethod != null) hayShipping = true;
        } else hayShipping = true;
        if (!hayShipping) addActionError("shipping");

        // validate RMA Types
        if ("Y".equalsIgnoreCase(getStoreProperty(StoreProperty.PROP_CAN_RMA, StoreProperty.PROP_DEFAULT_CAN_RMA))) {
            List rmaTypes = dao.getRmaTypes();
            if (rmaTypes == null || rmaTypes.isEmpty()) addActionError("rmatypes");
        }

        return SUCCESS;
    }

    @Action(value = "countrystates", results = @Result(type = "velocity", location = "/WEB-INF/views/admin/countryStates.vm"))
    public String countryStates() throws Exception {
        addToStack("states", dao.getStatesByCountry(countryCode));
        return SUCCESS;
    }

    /**
     * Determina si un archivo existe.
     * Se usa al crear reportes o exportaciones en background.
     * Se accede repetidamente con ajax, hasta que el archivo es generado.
     *
     * @return
     * @throws Exception
     */
    @Action(value = "generatedfileexist", results = @Result(type = "json"))
    public String generatedfileexist() throws Exception {
        if (StringUtils.isNotEmpty(fileName)) {
            fileExist = new File(getServletContext().getRealPath(fileName)).exists();
        } else addJsonError("File name is empty.");
        return SUCCESS;
    }

    @Action(value = "autoCompleteAttributes", results = @Result(type = "json", params = {"root", "autocompleteList"}))
    public String autoCompleteAttributes() {
        autocompleteList = new ArrayList<Map<String, String>>();
        if (StringUtils.isNotEmpty(term)) {
            List<AttributeProd> list = dao.findAttributes(term);
            if (list != null && !list.isEmpty()) {
                for (AttributeProd ap : list) {
                    Map<String, String> m = new HashMap<String, String>();
                    m.put("value", ap.getId().toString());
                    m.put("label", ap.getAttributeName(getDefaultLanguage()));
                    autocompleteList.add(m);
                    dao.evict(ap);
                }
            }
        }
        return SUCCESS;
    }

    @Action(value = "autocompleteManufacturers", results = @Result(type = "json", params = {"root", "autocompleteList"}))
    public String autoCompleteManufacturers() {
        autocompleteList = new ArrayList<Map<String, String>>();
        if (StringUtils.isNotEmpty(term)) {
            List<Manufacturer> list = dao.findManufacturers(term);
            if (list != null && !list.isEmpty()) {
                for (Manufacturer ap : list) {
                    Map<String, String> m = new HashMap<String, String>();
                    m.put("value", ap.getIdManufacturer().toString());
                    m.put("label", ap.getManufacturerName());
                    autocompleteList.add(m);
                    dao.evict(ap);
                }
            }
        }
        return SUCCESS;
    }

    @Action(value = "autocompleteCategories", results = @Result(type = "json", params = {"root", "autocompleteList"}))
    public String autoCompleteCategories() {
        autocompleteList = new ArrayList<Map<String, String>>();
        if (StringUtils.isNotEmpty(term)) {
            List<Category> list = dao.findCategories(term);
            if (list != null && !list.isEmpty()) {
                for (Category ap : list) {
                    Map<String, String> m = new HashMap<String, String>();
                    m.put("value", ap.getIdCategory().toString());
                    m.put("label", ap.getCategoryName(getDefaultLanguage()));
                    autocompleteList.add(m);
                    dao.evict(ap);
                }
            }
        }
        return SUCCESS;
    }

    @Action(value = "session_copy")
    public String sessionCopy() {
        if (StringUtils.isNotEmpty(term)) {
            getSession().put("CLIPBOARD_" + term, toCopy);
        }
        return null;
    }

    @Action(value = "session_paste", results = @Result(type = "stream", params = {"allowCaching", "false", "inputName", "inputStream", "contentType", "${contentType}"}))
    public String sessionPaste() {
        try {
            if (StringUtils.isNotEmpty(term)) {
                toCopy = (String) getSession().get("CLIPBOARD_" + term);
                setInputStream(new ByteArrayInputStream(toCopy.getBytes("utf-8")));
                setContentType("text/html");
            }
        } catch (UnsupportedEncodingException e) {
        }
        return SUCCESS;
    }

    private String countryCode;
    private String fileName;
    private String includeEmpty;
    private Boolean fileExist;
    private String term;
    private String toCopy;
    private List<Map<String, String>> autocompleteList;

    public String getTerm() {
        return term;
    }

    public void setTerm(String term) {
        this.term = term;
    }

    public String getToCopy() {
        return toCopy;
    }

    public void setToCopy(String toCopy) {
        this.toCopy = toCopy;
    }

    public List<Map<String, String>> getAutocompleteList() {
        return autocompleteList;
    }

    public void setAutocompleteList(List<Map<String, String>> autocompleteList) {
        this.autocompleteList = autocompleteList;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Boolean getFileExist() {
        return fileExist;
    }

    public void setFileExist(Boolean fileExist) {
        this.fileExist = fileExist;
    }

    public String getIncludeEmpty() {
        return includeEmpty;
    }

    public void setIncludeEmpty(String includeEmpty) {
        this.includeEmpty = includeEmpty;
    }
}
