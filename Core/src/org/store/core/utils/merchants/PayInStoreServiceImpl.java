package org.store.core.utils.merchants;

import org.store.core.beans.Order;
import org.store.core.beans.StaticText;
import org.store.core.front.FrontModuleAction;
import org.store.core.globals.BaseAction;

import java.util.Map;


public class PayInStoreServiceImpl extends MerchantService {

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
        return "Pay In Store";
    }

    public String getLabel() {
        return "pay.in.store";
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
        StaticText st = action.getStaticText("message." + getLabel(), StaticText.TYPE_BLOCK);
        return (st!=null) ? st.getContentValue(action.getLocale().getLanguage()) : null;
    }



}