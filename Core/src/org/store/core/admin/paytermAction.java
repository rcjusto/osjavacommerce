package org.store.core.admin;

import org.store.core.beans.Payterms;
import org.store.core.beans.utils.BreadCrumb;
import org.store.core.globals.StoreMessages;
import org.apache.commons.lang.StringUtils;

public class paytermAction extends AdminModuleAction implements StoreMessages {

    @Override
    public void prepare() throws Exception {
        payterm = (Payterms) dao.get(Payterms.class, idPayterm);
    }

    public String list() throws Exception {
        if (selecteds != null && selecteds.length > 0) {
            for (Long id : selecteds) {
                Payterms bean = (Payterms) dao.get(Payterms.class, id);
                if (bean != null) {
                    String res = dao.isUsedPayterm(bean);
                    if (StringUtils.isNotEmpty(res)) {
                        addActionError(getText(CNT_ERROR_CANNOT_DELETE_PAYTERM, CNT_DEFAULT_ERROR_CANNOT_DELETE_PAYTERM, new String[]{bean.getDescription(), res}));
                    } else {

                    }
                }
            }
            dao.flushSession();
        }
        getBreadCrumbs().add(new BreadCrumb(null, getText("admin.payterm.list"), null, null));
        addToStack("payterms", dao.getPayterms());
        return SUCCESS;
    }

    public String edit() throws Exception {
        getBreadCrumbs().add(new BreadCrumb(null, getText("admin.payterm.list"), url("listpayterm","/admin"), null));
        getBreadCrumbs().add(new BreadCrumb(null, getText(payterm!=null ? "admin.payterm.modify" : "admin.payterm.new"), null, null));
      return SUCCESS;
    }

    public String save() throws Exception {
        if (payterm != null) {
            payterm.setInventaryCode(getStoreCode());
            dao.save(payterm);
        }
        return SUCCESS;
    }

    private Payterms payterm;
    private Long idPayterm;

    public Payterms getPayterm() {
        return payterm;
    }

    public void setPayterm(Payterms payterm) {
        this.payterm = payterm;
    }

    public Long getIdPayterm() {
        return idPayterm;
    }

    public void setIdPayterm(Long idPayterm) {
        this.idPayterm = idPayterm;
    }
}
