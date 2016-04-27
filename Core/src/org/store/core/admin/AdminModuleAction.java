package org.store.core.admin;

import org.store.core.beans.utils.BreadCrumb;
import org.store.core.globals.BaseAction;
import org.apache.commons.lang.StringUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AdminModuleAction extends BaseAction {

    protected Map<String, Serializable> jsonResp;
    protected List<String> jsonErrors;
    protected Long[] selecteds;
    protected Boolean modal;
    protected Map filters;

    public void addJsonError(String err) {
        if (jsonErrors == null) jsonErrors = new ArrayList<String>();
        jsonErrors.add(err);
    }

    public Long[] getSelecteds() {
        return selecteds;
    }

    public void setSelecteds(Long[] selecteds) {
        this.selecteds = selecteds;
    }

    public List<String> getJsonErrors() {
        return jsonErrors;
    }

    public void setJsonErrors(List<String> jsonErrors) {
        this.jsonErrors = jsonErrors;
    }

    public Map<String, Serializable> getJsonResp() {
        return jsonResp;
    }

    public void setJsonResp(Map<String, Serializable> jsonResp) {
        this.jsonResp = jsonResp;
    }

    public Boolean getModal() {
        return modal!=null && modal;
    }

    public void setModal(Boolean modal) {
        this.modal = modal;
    }

    public Map getFilters() {
        return filters;
    }

    public void setFilters(Map filters) {
        this.filters = filters;
    }

    protected void processFilters() {
        if (filters != null && !filters.isEmpty()) {
            for (Object key : filters.keySet()) {
                if (key instanceof String) filters.put(key, getFilterVal((String) key));
            }
        }
    }

    protected String getFilterVal(String cad) {
        if (filters != null && StringUtils.isNotEmpty(cad) && filters.containsKey(cad)) {
            Object o = filters.get(cad);
            if (o != null) {
                if (o instanceof String) return (String) o;
                else if (o instanceof String[] && ((String[]) o).length > 0) return ((String[]) o)[0];
                else return o.toString();
            }
        }
        return null;
    }

    protected List<BreadCrumb> breadCrumbs;

    public List<BreadCrumb> getBreadCrumbs() {
        if (breadCrumbs==null) breadCrumbs = new ArrayList<BreadCrumb>();
        return breadCrumbs;
    }

    public void setBreadCrumbs(List<BreadCrumb> breadCrumbs) {
        this.breadCrumbs = breadCrumbs;
    }


}
