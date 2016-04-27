package org.store.core.admin;

import org.store.core.beans.Insurance;
import org.store.core.beans.utils.BreadCrumb;
import org.store.core.globals.StoreMessages;
import org.apache.commons.lang.ArrayUtils;

public class insuranceAction extends AdminModuleAction implements StoreMessages {

    @Override
    public void prepare() throws Exception {
        insurance = (Insurance) dao.get(Insurance.class, idInsurance);
    }

     public String list() throws Exception {
        if (selecteds != null && selecteds.length > 0) {
            for (Long id : selecteds) {
                Insurance bean = (Insurance) dao.get(Insurance.class, id);
                if (bean != null) {
                    dao.delete(bean);
                }
            }
            dao.flushSession();
        }
         getBreadCrumbs().add(new BreadCrumb(null, getText("admin.insurance.list"), null, null));
        return SUCCESS;
    }

    public String edit() throws Exception {
        getBreadCrumbs().add(new BreadCrumb(null, getText("admin.insurance.list"), url("listinsurance","/admin"), null));
        getBreadCrumbs().add(new BreadCrumb(null, getText(insurance!=null ? "admin.insurance.modify" : "admin.insurance.new"), null, null));
        return SUCCESS;
    }

    public String del() throws Exception {
        if (insurance != null) dao.delete(insurance);
        return SUCCESS;
    }

    public String save() throws Exception {
        if (insurance != null) {
            insurance.setInventaryCode(getStoreCode());
            if (ArrayUtils.isSameLength(insuranceText, getLanguages())) {
                for (int i = 0; i < getLanguages().length; i++) insurance.setText(getLanguages()[i], insuranceText[i]);
            }
            dao.save(insurance);
        }
        return SUCCESS;
    }

    private Insurance insurance;
    private Long idInsurance;
    private String[] insuranceText;

    public Insurance getInsurance() {
        return insurance;
    }

    public void setInsurance(Insurance insurance) {
        this.insurance = insurance;
    }

    public Long getIdInsurance() {
        return idInsurance;
    }

    public void setIdInsurance(Long idInsurance) {
        this.idInsurance = idInsurance;
    }

    public String[] getInsuranceText() {
        return insuranceText;
    }

    public void setInsuranceText(String[] insuranceText) {
        this.insuranceText = insuranceText;
    }
    
}
