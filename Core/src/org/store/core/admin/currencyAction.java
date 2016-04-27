package org.store.core.admin;

import com.opensymphony.xwork2.inject.Inject;
import org.store.core.beans.Currency;
import org.store.core.beans.utils.BreadCrumb;
import org.store.core.globals.CurrencyRatesUpdater;
import org.store.core.globals.StoreMessages;
import org.apache.commons.lang.StringUtils;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;

import java.io.Serializable;
import java.util.Calendar;
import java.util.HashMap;

@Namespace(value = "/admin")
@ParentPackage(value = "store-admin")
public class currencyAction extends AdminModuleAction implements StoreMessages {

    @Inject
    private CurrencyRatesUpdater ratesUpdater;

    @Override
    public void prepare() throws Exception {
        currency = (Currency) dao.get(Currency.class, idCurrency);
    }

    public String list() throws Exception {
        if (selecteds != null && selecteds.length > 0) {
            for (Long id : selecteds) {
                Currency bean = (Currency) dao.get(Currency.class, id);
                if (bean != null) {
                    String res = dao.isUsedCurrency(bean);
                    if (StringUtils.isNotEmpty(res)) {
                        addActionError(getText(CNT_ERROR_CANNOT_DELETE_CURRENCY, CNT_DEFAULT_ERROR_CANNOT_DELETE_CURRENCY, new String[]{bean.getCode(), res}));
                    } else {
                        dao.delete(bean);
                    }
                }
            }
            dao.flushSession();
        }
        getBreadCrumbs().add(new BreadCrumb(null, getText("admin.currency.list"), null, null));

        addToStack("currencies", dao.getCurrencies());
        return SUCCESS;
    }

    public String edit() throws Exception {
        getBreadCrumbs().add(new BreadCrumb(null, getText("admin.currency.list"), url("listcurrency", "/admin"), null));
        if (currency != null) getBreadCrumbs().add(new BreadCrumb(null, getText("admin.currency.modify"), null, null));
        else getBreadCrumbs().add(new BreadCrumb(null, getText("admin.currency.configure.new"), null, null));
        return SUCCESS;
    }

    public String save() throws Exception {
        if (currency != null) {
            currency.setInventaryCode(getStoreCode());
            if (currency.getRatio() == null || currency.getRatio() <= 0.0d) {
                addActionError(getText(CNT_ERROR_CURRENCY_INCORRECT_RATE, CNT_DEFAULT_ERROR_CURRENCY_INCORRECT_RATE));
                return INPUT;
            } else {
                currency.setLastUpdate(Calendar.getInstance().getTime());
                dao.clearSession();
                dao.save(currency);
            }
        }
        return SUCCESS;
    }

    @Action(value = "currencyrateupdate", results = @Result(type = "json", params = {"root", "jsonResp"}))
    public String rateUpdate() throws Exception {
        jsonResp = new HashMap<String, Serializable>();
        if (currency != null && !currency.equals(getDefaultCurrency())) {
            if (ratesUpdater != null) {
                CurrencyRatesUpdater.ResponseRateUpdate res = ratesUpdater.getRate(getDefaultCurrency().getCode(), currency.getCode());
                if (res != null) {
                    if (StringUtils.isEmpty(res.getError()) && res.getRateValue() != null && res.getRateValue() >= 0.0d) {
                        currency.setRatio(res.getRateValue());
                        currency.setReverseRatio(res.getReverseRateValue());
                        currency.setLastUpdate(res.getRateDate());
                        dao.save(currency);
                    } else {
                        addJsonError(getText(CNT_ERROR_RATE + res.getError().toLowerCase()));
                    }
                } else {
                    addJsonError(getText(CNT_ERROR_RATES_CONFIGURATION, CNT_DEFAULT_ERROR_RATES_CONFIGURATION));
                }
            } else {
                addJsonError(getText(CNT_ERROR_RATES_CONFIGURATION, CNT_DEFAULT_ERROR_RATES_CONFIGURATION));
            }
            jsonResp.put("currency", currency.getCode());
            jsonResp.put("ratio", currency.getRatio());
            jsonResp.put("reverseRatio", currency.getReverseRatio());
            jsonResp.put("lastUpdate", currency.getLastUpdate());
            if (jsonErrors != null && jsonErrors.size() > 0) jsonResp.put("errors", jsonErrors.toArray());
        } else if (currency.equals(getDefaultCurrency())) {
            currency.setRatio(1d);
            currency.setReverseRatio(1d);
        }
        return SUCCESS;
    }

    private Currency currency;
    private Long idCurrency;

    public CurrencyRatesUpdater getRatesUpdater() {
        return ratesUpdater;
    }

    public void setRatesUpdater(CurrencyRatesUpdater ratesUpdater) {
        this.ratesUpdater = ratesUpdater;
    }

    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    public Long getIdCurrency() {
        return idCurrency;
    }

    public void setIdCurrency(Long idCurrency) {
        this.idCurrency = idCurrency;
    }
}
