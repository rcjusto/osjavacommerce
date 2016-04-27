package org.store.core.admin;

import org.store.core.beans.Category;
import org.store.core.beans.CategoryFee;
import org.store.core.beans.Fee;
import org.store.core.beans.State;
import org.store.core.beans.utils.BreadCrumb;
import org.store.core.globals.SomeUtils;
import org.store.core.globals.StoreMessages;
import org.apache.commons.lang.StringUtils;

import javax.swing.tree.DefaultMutableTreeNode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class feeAction extends AdminModuleAction implements StoreMessages {

    @Override
    public void prepare() throws Exception {
        fee = (Fee) dao.get(Fee.class, idFee);
    }

    public String list() throws Exception {
        if (selecteds != null && selecteds.length > 0) {
            for (Long id : selecteds) {
                Fee bean = (Fee) dao.get(Fee.class, id);
                if (bean != null) {
                    dao.deleteFee(bean);
                }
            }
            dao.flushSession();
        }
        getBreadCrumbs().add(new BreadCrumb(null, getText("admin.fee.list"), null, null));
        return SUCCESS;
    }

    public String edit() throws Exception {
        DefaultMutableTreeNode categoryTree = new DefaultMutableTreeNode();
        dao.fillCategoryNodeChilds(categoryTree);
        addToStack("categoryTree", categoryTree);
        List<CategoryFee> feeCategoryList = dao.getFeeCategories(fee);
        Map<Long, Double> mapValue = new HashMap<Long, Double>();
        Map<Long, Double> mapPercent = new HashMap<Long, Double>();
        if (feeCategoryList != null) {
            for (CategoryFee cf : feeCategoryList) {
                mapValue.put(cf.getCategory().getIdCategory(), cf.getValue());
                mapPercent.put(cf.getCategory().getIdCategory(), cf.getPercent());
            }
        }
        getRequest().setAttribute("feeValues", mapValue);
        getRequest().setAttribute("feePercents", mapPercent);
        getBreadCrumbs().add(new BreadCrumb(null, getText("admin.fee.list"), url("listfee","/admin"), null));
        getBreadCrumbs().add(new BreadCrumb(null, getText(fee!=null ? "admin.fee.modify" : "admin.fee.new"), null, null));
        return SUCCESS;
    }

    public String save() throws Exception {
        if (fee != null) {
            State state = (idState != null) ? (State) dao.get(State.class, idState) : null;
            if (state == null && !StringUtils.isEmpty(newState)) {
                state = new State();
                state.setCountryCode(fee.getCountry());
                state.setStateCode(StringUtils.left(newState, 5));
                state.setStateName(newState);
                dao.save(state);
            }
            fee.setState(state);
            fee.setInventaryCode(getStoreCode());
            dao.save(fee);

            if (categoryId != null && categoryId.length > 0) {
                for (int i = 0; i < categoryId.length; i++) {
                    Category cat = dao.getCategory(categoryId[i]);
                    if (cat != null) {
                        CategoryFee cf = dao.getCategoryFee(fee, cat);
                        Double fv = SomeUtils.strToDouble(feeValue[i]);
                        Double fp = SomeUtils.strToDouble(feePercent[i]);
                        if ((fv!=null && fv>0) || (fp!=null && fp>0)) {
                            if (cf == null) {
                                cf = new CategoryFee();
                                cf.setFee(fee);
                                cf.setCategory(cat);
                            }
                            cf.setValue(fv);
                            cf.setPercent(fp);
                            dao.save(cf);
                        } else if (cf != null) {
                            dao.delete(cf);
                        }
                    }
                }
            }
        }
        return SUCCESS;
    }

    private Fee fee;
    private Long idFee;
    private Long[] categoryId;
    private Long[] feeId;
    private String[] feeValue;
    private String[] feePercent;
    private Long idState;
    private String newState;

    public Fee getFee() {
        return fee;
    }

    public void setFee(Fee fee) {
        this.fee = fee;
    }

    public Long getIdFee() {
        return idFee;
    }

    public void setIdFee(Long idFee) {
        this.idFee = idFee;
    }

    public Long[] getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long[] categoryId) {
        this.categoryId = categoryId;
    }

    public Long[] getFeeId() {
        return feeId;
    }

    public void setFeeId(Long[] feeId) {
        this.feeId = feeId;
    }

    public String[] getFeeValue() {
        return feeValue;
    }

    public void setFeeValue(String[] feeValue) {
        this.feeValue = feeValue;
    }

    public String[] getFeePercent() {
        return feePercent;
    }

    public void setFeePercent(String[] feePercent) {
        this.feePercent = feePercent;
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
