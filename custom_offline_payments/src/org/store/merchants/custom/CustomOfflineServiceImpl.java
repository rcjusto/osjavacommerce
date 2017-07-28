package org.store.merchants.custom;

import org.store.core.beans.Order;
import org.store.core.front.FrontModuleAction;
import org.store.core.globals.BaseAction;
import org.store.core.utils.merchants.MerchantService;
import org.store.core.utils.merchants.PaymentResult;

import java.util.Map;


public abstract class CustomOfflineServiceImpl extends MerchantService {
    public boolean validatePayment(Order order, BaseAction action) {
        return true;
    }

    public PaymentResult doPayment(Order order, BaseAction action) {
        PaymentResult res = new PaymentResult();
        res.setTransactionResult(PaymentResult.RESULT_PENDING);
        return res;
    }

    public PaymentResult doRequestStatus(Order order, BaseAction action) {
        return null;
    }

    @Override
    public Double getInterestPercent(BaseAction action) {
        return null;
    }

    public Map preparePaymentRedirection(Order order, BaseAction action) {
        return null;
    }

    public String doPaymentRedirection(FrontModuleAction action) {
        return null;
    }

    @Override
    public String getCode() {
        return "Custom Offline Payment " + getIndex();
    }

    @Override
    public String getLabel() {
        return "custom.offline.payment." + getIndex();
    }

    public abstract String getIndex();

    public String getType() {
        return MerchantService.TYPE_OFFLINE;
    }

    public String getError() {
        return null;
    }

    public String getForm(BaseAction action) {
        return null;
    }

    @Override
    public String getPropertiesForm(BaseAction action) {
        return null;
    }


}