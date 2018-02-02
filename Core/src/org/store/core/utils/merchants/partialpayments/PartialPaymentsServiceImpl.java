package org.store.core.utils.merchants.partialpayments;

import org.store.core.beans.Order;
import org.store.core.beans.OrderPayment;
import org.store.core.front.FrontModuleAction;
import org.store.core.globals.BaseAction;
import org.store.core.globals.SomeUtils;
import org.store.core.utils.merchants.MerchantService;
import org.store.core.utils.merchants.PaymentResult;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.struts2.json.JSONException;
import org.apache.struts2.json.JSONUtil;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class PartialPaymentsServiceImpl extends MerchantService {

    private List<PartialPaymentConfig> payments;
    private static final String PROPERTY_PAYMENTS = "PAYMENTS_CONFIGURATION";
    private static final String CONFIG_TEMPLATE = "/WEB-INF/views/org/store/core/utils/merchants/partialpayments/config.vm";
    private static final String SELECT_PAYMENT_TEMPLATE = "/WEB-INF/views/org/store/core/utils/merchants/partialpayments/payment.vm";

    public boolean validatePayment(Order order, BaseAction action) {
        return true;
    }

    public PaymentResult doPayment(Order order, BaseAction action) {
        loadProperties(action);
        PartialPaymentConfig payment = getPaymentByCode(action.getRequest().getParameter("partialPaymentOption"));
        if (payment != null) {
            Calendar cal = Calendar.getInstance();
            // generar primer pago
            if (payment.hasInitialPayment()) {
                OrderPayment op = new OrderPayment();
                op.setOrder(order);
                op.setStatus(OrderPayment.STATUS_PENDING);
                op.setExpectedDate(SomeUtils.dateIni(cal.getTime()));
                op.setAmount(payment.getFirstPayment(order.getTotal()));
                action.getDao().save(op);
            }
            // generar siguientes pagos
            if (payment.hasPartialPayment()) {
                for (int i = 0; i < payment.getPartialPayments(); i++) {
                    if (payment.getFrequencyDays() != null && payment.getFrequencyDays() > 0)
                        cal.add(Calendar.DATE, payment.getFrequencyDays());
                    OrderPayment op = new OrderPayment();
                    op.setOrder(order);
                    op.setStatus(OrderPayment.STATUS_PENDING);
                    op.setExpectedDate(SomeUtils.dateIni(cal.getTime()));
                    op.setAmount(payment.getPartialPayment(order.getTotal()));
                    action.getDao().save(op);
                }
            }
        }
        PaymentResult res = new PaymentResult();
        res.setTransactionResult(PaymentResult.RESULT_PENDING);
        return res;
    }

    private PartialPaymentConfig getPaymentByCode(String code) {
        if (StringUtils.isNotEmpty(code) && payments != null && !payments.isEmpty()) {
            for (PartialPaymentConfig c : payments)
                if (code.equalsIgnoreCase(c.toString())) return c;
        }
        return null;
    }

    public PaymentResult doRequestStatus(Order order, BaseAction action) {
        return null;
    }

    @Override
    public Double getInterestPercent(BaseAction action) {
        loadProperties(action);
        PartialPaymentConfig payment = getPaymentByCode(action.getRequest().getParameter("partialPaymentOption"));
        return (payment != null && payment.getInterestPercent() != null) ? payment.getInterestPercent() : null;
    }

    public Map preparePaymentRedirection(Order order, BaseAction action) {
        return null;
    }

    public String doPaymentRedirection(FrontModuleAction action) {
        return null;
    }

    public String getCode() {
        return "Partial Payments";
    }

    public String getLabel() {
        return "partial.payments";
    }

    public String getType() {
        return MerchantService.TYPE_OFFLINE;
    }

    public String getError() {
        return null;
    }

    public String getForm(BaseAction action) {
        loadProperties(action);
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("payments", payments);
        return action.proccessVelocityTemplate(SELECT_PAYMENT_TEMPLATE, map);
    }

    @Override
    public String getPropertiesForm(BaseAction action) {
        loadProperties(action);
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("active", isActive());
        map.put("payments", payments);
        return action.proccessVelocityTemplate(CONFIG_TEMPLATE, map);
    }

    @Override
    public void savePropertiesForm(BaseAction action) {
        payments = new ArrayList<PartialPaymentConfig>();
        String[] initialPercentArr = action.getRequest().getParameterValues("initialPercent");
        String[] partialPercentArr = action.getRequest().getParameterValues("partialPercent");
        String[] frequencyDaysArr = action.getRequest().getParameterValues("frequencyDays");
        String[] interestPercentArr = action.getRequest().getParameterValues("interestPercent");
        String[] partialPaymentsArr = action.getRequest().getParameterValues("partialPayments");
        String[] paymentNameArr = action.getRequest().getParameterValues("paymentOptionName");
        if (initialPercentArr != null && initialPercentArr.length > 0) {
            for (int i = 0; i < initialPercentArr.length; i++) {
                if (StringUtils.isNotEmpty(initialPercentArr[i]) || StringUtils.isNotEmpty(partialPercentArr[i])) {
                    Double initialPercent = NumberUtils.toDouble(initialPercentArr[i], 0d);
                    Double partialPercent = NumberUtils.toDouble(partialPercentArr[i], 0d);
                    Double interestPercent = NumberUtils.toDouble(interestPercentArr[i], 0d);
                    if (initialPercent > 0 || partialPercent > 0) {
                        Integer frequencyDays = NumberUtils.toInt(frequencyDaysArr[i], 0);
                        Integer partialPayments = NumberUtils.toInt(partialPaymentsArr[i], 0);
                        PartialPaymentConfig c = new PartialPaymentConfig();
                        c.setFrequencyDays(frequencyDays);
                        c.setInitialPercent(initialPercent);
                        c.setPartialPayments(partialPayments);
                        c.setPartialPercent(partialPercent);
                        c.setInterestPercent(interestPercent);
                        c.setName(paymentNameArr[i]);
                        payments.add(c);
                    }
                }
            }
        }

        properties.setProperty(PROPERTY_PAYMENTS, serializePayments());
        super.savePropertiesForm(action);
    }

    @Override
    public void loadProperties(BaseAction action) {
        super.loadProperties(action);
        deserializePayments(properties.getProperty(PROPERTY_PAYMENTS));
    }

    private String serializePayments() {
        String res;
        List<Map> list = new ArrayList<Map>();
        if (payments != null && !payments.isEmpty()) {
            for (PartialPaymentConfig c : payments) {
                try {
                    list.add(BeanUtils.describe(c));
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
        }
        try {
            res = JSONUtil.serialize(list);
        } catch (JSONException e) {
            log.error(e.getMessage(), e);
            res = "";
        }
        return res;
    }

    private void deserializePayments(String cad) {
        payments = new ArrayList<PartialPaymentConfig>();
        if (StringUtils.isNotEmpty(cad))
            try {
                List<Map> list = (List<Map>) JSONUtil.deserialize(cad);
                if (list != null && !list.isEmpty()) {
                    for (Map m : list) {
                        if (m != null) {
                            PartialPaymentConfig p = new PartialPaymentConfig();
                            BeanUtils.populate(p, m);
                            payments.add(p);
                        }
                    }
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
    }

}