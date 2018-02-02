package org.store.core.admin;

import org.store.core.beans.CustomShippingMethod;
import org.store.core.beans.CustomShippingMethodRule;
import org.store.core.beans.utils.BreadCrumb;
import org.store.core.globals.CountryFactory;
import org.store.core.globals.SomeUtils;
import org.store.core.globals.StoreMessages;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class customshipAction extends AdminModuleAction implements StoreMessages {

    @Override
    public void prepare() throws Exception {
        customShipping = (CustomShippingMethod) dao.get(CustomShippingMethod.class, idCustomShipping);
    }

    public String list() throws Exception {
        if (selecteds != null && selecteds.length > 0) {
            for (Long id : selecteds) {
                CustomShippingMethod bean = (CustomShippingMethod) dao.get(CustomShippingMethod.class, id);
                if (bean != null) {
                    String res = dao.isUsedCustomShippingMethod(bean);
                    if (StringUtils.isNotEmpty(res)) {
                        addActionError(getText(CNT_ERROR_CANNOT_DELETE_CUSTOM_METHOD, CNT_DEFAULT_ERROR_CANNOT_DELETE_CUSTOM_METHOD, new String[]{bean.getCode(), res}));
                    } else {
                        dao.delete(bean);
                    }
                }
            }
            dao.flushSession();
        }

        getBreadCrumbs().add(new BreadCrumb(null, getText("admin.custom.shipping.list"), null, null));
        addToStack("customShippingMethods", dao.getCustomShippingMethods());
        return SUCCESS;
    }

    public String edit() throws Exception {
        setCountries(CountryFactory.getCountries(getLocale()));
        getBreadCrumbs().add(new BreadCrumb(null, getText("admin.custom.shipping.list"), url("listcustomship","/admin"), null));
        getBreadCrumbs().add(new BreadCrumb(null, getText(customShipping!=null ? "admin.custom.shipping.modify" : "admin.custom.shipping.new"), null, null));
     return SUCCESS;
    }

    public String del() throws Exception {
        if (customShipping != null) {
            dao.delete(customShipping);
        }
        return SUCCESS;
    }

    public String save() throws Exception {
        if (customShipping != null) {
            if (methodName != null && ArrayUtils.isSameLength(methodName, getLanguages())) {
                for (int i = 0; i < getLanguages().length; i++)
                    customShipping.setName(getLanguages()[i], methodName[i]);
            }
            customShipping.setInventaryCode(getStoreCode());
            dao.save(customShipping);

            // ELiminar todos los hijos
            if (customShipping.getRules() != null) {
                for (CustomShippingMethodRule r : customShipping.getRules())
                    dao.delete(r);
            }
            customShipping.setRules(new ArrayList<CustomShippingMethodRule>());


            //Save Childs
            if (ruleOrder != null && ruleOrder.length > 0) {
                for (int i = 0; i < ruleOrder.length; i++) {
                    Integer order = (ruleOrder.length > i) ? ruleOrder[i] : null;
                    Double price = rulePrice != null && rulePrice.length > i ? SomeUtils.strToDouble(rulePrice[i]) : null;
                    Double percent = rulePercent != null && rulePercent.length > i ? SomeUtils.strToDouble(rulePercent[i]) : null;
                    if (order != null && (price != null || percent != null)) {
                        CustomShippingMethodRule rule = new CustomShippingMethodRule();
                        rule.setMethod(customShipping);
                        rule.setRuleOrder(order);
                        rule.setDays(ruleDays != null && ruleDays.length > i ? ruleDays[i] : null);

                        rule.setIdCountry(null);
                        rule.setState(null);
                        String cad = ruleState != null && ruleState.length > i ? ruleState[i] : null;
                        if (StringUtils.isNotEmpty(cad)) {
                            String[] arr = cad.split("[|][|]");
                            if (arr.length == 1 && StringUtils.isNotEmpty(arr[0])) {
                                rule.setIdCountry(arr[0]);
                            } else if (arr.length == 2 && StringUtils.isNotEmpty(arr[0])) {
                                rule.setIdCountry(arr[0]);
                                rule.setState(dao.getStateByCode(arr[0], arr[1]));
                            }
                        }

                        rule.setMinTotal(ruleMinTotal != null && ruleMinTotal.length > i ? SomeUtils.strToDouble(ruleMinTotal[i]) : null);
                        rule.setMaxTotal(ruleMaxTotal != null && ruleMaxTotal.length > i ? SomeUtils.strToDouble(ruleMaxTotal[i]) : null);
                        rule.setMinWeight(ruleMinWeight != null && ruleMinWeight.length > i ? SomeUtils.strToDouble(ruleMinWeight[i]) : null);
                        rule.setMaxWeight(ruleMaxWeight != null && ruleMaxWeight.length > i ? SomeUtils.strToDouble(ruleMaxWeight[i]) : null);
                        rule.setPerWeightUnit(rulePerWeight != null && rulePerWeight.length > i && "Y".equalsIgnoreCase(rulePerWeight[i]));
                        rule.setRulePercent(percent);
                        rule.setRulePrice(price);
                        dao.save(rule);
                        customShipping.getRules().add(rule);
                    }
                }
            }
        }
        return SUCCESS;
    }

    private CustomShippingMethod customShipping;
    private Long idCustomShipping;
    private String[] methodName;
    private Integer[] ruleOrder;
    private Integer[] ruleDays;
    private String[] ruleState;
    private String[] ruleMinTotal;
    private String[] ruleMaxTotal;
    private String[] ruleMinWeight;
    private String[] ruleMaxWeight;
    private String[] rulePrice;
    private String[] rulePercent;
    private String[] rulePerWeight;
    private List countries;

    public CustomShippingMethod getCustomShipping() {
        return customShipping;
    }

    public void setCustomShipping(CustomShippingMethod customShipping) {
        this.customShipping = customShipping;
    }

    public Long getIdCustomShipping() {
        return idCustomShipping;
    }

    public void setIdCustomShipping(Long idCustomShipping) {
        this.idCustomShipping = idCustomShipping;
    }

    public String[] getMethodName() {
        return methodName;
    }

    public void setMethodName(String[] methodName) {
        this.methodName = methodName;
    }

    public Integer[] getRuleOrder() {
        return ruleOrder;
    }

    public void setRuleOrder(Integer[] ruleOrder) {
        this.ruleOrder = ruleOrder;
    }

    public Integer[] getRuleDays() {
        return ruleDays;
    }

    public void setRuleDays(Integer[] ruleDays) {
        this.ruleDays = ruleDays;
    }

    public String[] getRuleState() {
        return ruleState;
    }

    public void setRuleState(String[] ruleState) {
        this.ruleState = ruleState;
    }

    public String[] getRuleMinTotal() {
        return ruleMinTotal;
    }

    public void setRuleMinTotal(String[] ruleMinTotal) {
        this.ruleMinTotal = ruleMinTotal;
    }

    public String[] getRuleMaxTotal() {
        return ruleMaxTotal;
    }

    public void setRuleMaxTotal(String[] ruleMaxTotal) {
        this.ruleMaxTotal = ruleMaxTotal;
    }

    public String[] getRuleMinWeight() {
        return ruleMinWeight;
    }

    public void setRuleMinWeight(String[] ruleMinWeight) {
        this.ruleMinWeight = ruleMinWeight;
    }

    public String[] getRuleMaxWeight() {
        return ruleMaxWeight;
    }

    public void setRuleMaxWeight(String[] ruleMaxWeight) {
        this.ruleMaxWeight = ruleMaxWeight;
    }

    public String[] getRulePrice() {
        return rulePrice;
    }

    public void setRulePrice(String[] rulePrice) {
        this.rulePrice = rulePrice;
    }

    public String[] getRulePercent() {
        return rulePercent;
    }

    public void setRulePercent(String[] rulePercent) {
        this.rulePercent = rulePercent;
    }

    public String[] getRulePerWeight() {
        return rulePerWeight;
    }

    public void setRulePerWeight(String[] rulePerWeight) {
        this.rulePerWeight = rulePerWeight;
    }

    public List getCountries() {
        return countries;
    }

    public void setCountries(List countries) {
        this.countries = countries;
    }
}
