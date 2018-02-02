package org.store.core.admin;

import org.store.core.beans.State;
import org.store.core.beans.Tax;
import org.store.core.beans.utils.BreadCrumb;
import org.store.core.globals.StoreMessages;
import org.apache.commons.lang.StringUtils;

public class taxAction extends AdminModuleAction implements StoreMessages {

    @Override
    public void prepare() throws Exception {
        tax = (Tax) dao.get(Tax.class, idTax);
    }

      public String list() throws Exception {
        if (selecteds != null && selecteds.length > 0) {
            for (Long id : selecteds) {
                Tax bean = (Tax) dao.get(Tax.class, id);
                if (bean != null) {
                    dao.delete(bean);
                }
            }
            dao.flushSession();
        }
        getBreadCrumbs().add(new BreadCrumb(null, getText("admin.tax.list"), null, null));
        return SUCCESS;
    }

    public String edit() throws Exception {
        getBreadCrumbs().add(new BreadCrumb(null, getText("admin.tax.list"), url("listtax","/admin"), null));
        getBreadCrumbs().add(new BreadCrumb(null, getText(tax!=null ? "admin.tax.modify" : "admin.tax.new"), null, null));
        return SUCCESS;
    }

    public String del() throws Exception {
        if (tax != null) dao.delete(tax);
        return SUCCESS;
    }

    public String save() throws Exception {
        if (tax != null) {
            State state = (idState != null) ? (State) dao.get(State.class, idState) : null;
            if (state == null && !StringUtils.isEmpty(newState)) {
                state = new State();
                state.setCountryCode(tax.getCountry());
                state.setStateCode(StringUtils.left(newState, 5));
                state.setStateName(newState);
                dao.save(state);
            }
            tax.setState(state);
            tax.setInventaryCode(getStoreCode());
            dao.save(tax);
        }
        return SUCCESS;
    }

    private Tax tax;
    private Long idTax;
    private Long idState;
    private String newState;

    public Tax getTax() {
        return tax;
    }

    public void setTax(Tax tax) {
        this.tax = tax;
    }

    public Long getIdTax() {
        return idTax;
    }

    public void setIdTax(Long idTax) {
        this.idTax = idTax;
    }

    public Long getIdState() {
        return idState;
    }

    public void setIdState(Long idState) {
        this.idState = idState;
    }

    public String getNewState() {
        return newState;
    }

    public void setNewState(String newState) {
        this.newState = newState;
    }
}
