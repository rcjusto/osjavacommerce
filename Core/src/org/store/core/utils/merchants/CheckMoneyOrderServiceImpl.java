package org.store.core.utils.merchants;

import org.store.core.beans.Order;
import org.store.core.front.FrontModuleAction;
import org.store.core.globals.BaseAction;

import java.util.Map;


public class CheckMoneyOrderServiceImpl extends MerchantService {
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
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Map preparePaymentRedirection(Order order, BaseAction action) {
        return null;
    }

    public String doPaymentRedirection(FrontModuleAction action) {
        return null;
    }

    public String getCode() {
        return "Check / Money Order";
    }

    public String getLabel() {
        return "check.money.order";
    }

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
