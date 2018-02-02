package org.store.core.admin;

import org.store.core.beans.LocalizedText;
import org.store.core.beans.Rma;
import org.store.core.beans.StoreProperty;
import org.store.core.beans.utils.BreadCrumb;
import org.store.core.globals.SomeUtils;
import org.store.core.globals.StoreMessages;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

import java.util.List;

public class rmastatusAction extends AdminModuleAction implements StoreMessages {

    public String list() throws Exception {
        List<String> statuses = dao.getRmaStatus(true);
        if (statusName != null && statusName.length > 0) {
            boolean modified = false;
            for (String st : statusName) {
                if (StringUtils.isNotEmpty(st) && statuses.contains(st)) {
                    if (st.equalsIgnoreCase(Rma.STATUS_ACCEPTED) || st.equalsIgnoreCase(Rma.STATUS_REQUESTED) || st.equalsIgnoreCase(Rma.STATUS_REJECTED) || st.equalsIgnoreCase(Rma.STATUS_CLOSED)) {
                        addActionError(getText(CNT_ERROR_CANNOT_DELETE_RMASTATUS, CNT_DEFAULT_ERROR_CANNOT_DELETE_RMASTATUS, new String[]{st}));
                    } else {
                        statuses.remove(st);
                        LocalizedText lt = dao.getLocalizedtext("rma.status." + statusCode);
                        if (lt != null) dao.delete(lt);
                        modified = true;
                    }
                }
            }
            if (modified) {
                StoreProperty bean = dao.getStoreProperty(StoreProperty.PROP_RMA_STATUS, StoreProperty.TYPE_GENERAL);
                if (bean != null) {
                    bean.setValue(SomeUtils.join(statuses, ","));
                    dao.save(bean);
                    dao.flushSession();
                }
            }
        }
        addToStack("rmastatuslist", statuses);
        getBreadCrumbs().add(new BreadCrumb(null, getText("admin.rma.status.list"), null, null));
        return SUCCESS;
    }

    public String edit() throws Exception {
        if (StringUtils.isNotEmpty(statusCode)) {
            addToStack("names", dao.getLocalizedtext("rma.status." + statusCode));
            addToStack("texts", dao.getLocalizedtext("rma.texts." + statusCode));
        }
        getBreadCrumbs().add(new BreadCrumb(null, getText("admin.rma.status.list"), url("listrmastatus","/admin"), null));
        getBreadCrumbs().add(new BreadCrumb(null, getText(statusCode!=null ? "admin.rma.status.modify" : "admin.rma.status.new"), null, null));
        return SUCCESS;
    }

    public String save() throws Exception {
        if (StringUtils.isNotEmpty(statusCode)) {
            if (statusName != null && ArrayUtils.isSameLength(statusName, getLanguages())) {
                LocalizedText lt = dao.getLocalizedtext("rma.status." + statusCode);
                if (lt == null) {
                    lt = new LocalizedText();
                    lt.setInventaryCode(getStoreCode());
                    lt.setCode("rma.status." + statusCode);
                }
                for (int i = 0; i < getLanguages().length; i++) {
                    lt.addValue(getLanguages()[i], StringUtils.isNotEmpty(statusName[i]) ? statusName[i] : "");
                }
                dao.save(lt);
            }
            if (statusText != null && ArrayUtils.isSameLength(statusText, getLanguages())) {
                LocalizedText lt = dao.getLocalizedtext("rma.text." + statusCode);
                if (lt == null) {
                    lt = new LocalizedText();
                    lt.setInventaryCode(getStoreCode());
                    lt.setCode("rma.text." + statusCode);
                }
                for (int i = 0; i < getLanguages().length; i++) {
                    lt.addValue(getLanguages()[i], StringUtils.isNotEmpty(statusText[i]) ? statusText[i] : "");
                }
                dao.save(lt);
            }
            List<String> statuses = dao.getRmaStatus(true);
            if (!statuses.contains(statusCode)) {
                StoreProperty bean = dao.getStoreProperty(StoreProperty.PROP_RMA_STATUS, StoreProperty.TYPE_GENERAL);
                statuses.add(statusCode);
                bean.setValue(SomeUtils.join(statuses, ","));
                dao.save(bean);
            }
        }
        return SUCCESS;
    }

    private String[] statusName;
    private String[] statusText;
    private String statusCode;

    public String[] getStatusName() {
        return statusName;
    }

    public void setStatusName(String[] statusName) {
        this.statusName = statusName;
    }

    public String[] getStatusText() {
        return statusText;
    }

    public void setStatusText(String[] statusText) {
        this.statusText = statusText;
    }

    public String getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(String statusCode) {
        this.statusCode = statusCode;
    }
}
