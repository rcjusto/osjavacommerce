package org.store.merchants.moneris;

import org.store.core.beans.*;
import org.store.core.dao.HibernateDAO;
import org.store.core.front.FrontModuleAction;
import org.store.core.front.OtherAction;
import org.store.core.globals.config.Store20Config;
import org.store.core.utils.events.DefaultEventServiceImpl;
import org.store.core.utils.events.EventService;
import org.store.core.utils.events.EventUtils;
import org.store.core.utils.templates.TemplateBlock;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.struts2.json.JSONException;
import org.apache.struts2.json.JSONUtil;
import org.hibernate.Session;

import javax.servlet.ServletContext;
import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class MonerisHPPEventImpl extends DefaultEventServiceImpl {

    private static final String RESPONSE_ACTION_NAME = "moneris_response";
    private static final String APPROVED_ACTION_NAME = "moneris_approved";
    private static final String REJECTED_ACTION_NAME = "moneris_rejected";
    private static final String CNT_ERROR_ORDER_NOT_FOUND = "error.order.not.found";
    private static final String CNT_DEFAULT_ERROR_ORDER_NOT_FOUND = "Order not found";
    public static final String PAYMENT_SERVICE_NAME = "Moneris Hosted Page";

    private static final String BLOCK_APPROVED_TEXT = "moneris-hpp.pay.result.approved";
    private static final String BLOCK_REJECTED_TEXT = "moneris-hpp.pay.result.rejected";

    public String getName() {
        return PAYMENT_SERVICE_NAME;
    }

    public String getDescription(FrontModuleAction action) {
        return "Process Moneris Hosted Page Response";
    }

    public Boolean onExecuteEvent(ServletContext ctx, int eventType, FrontModuleAction action, Map<String, Object> map) {
        if (EventService.EVENT_CUSTOM_ACTION == eventType && map != null && map.containsKey("actionName") && RESPONSE_ACTION_NAME.equalsIgnoreCase((String) map.get("actionName"))) {
            onResponseAction(ctx, action);
            return true;
        } else if (EventService.EVENT_CUSTOM_ACTION == eventType && map != null && map.containsKey("actionName") && APPROVED_ACTION_NAME.equalsIgnoreCase((String) map.get("actionName"))) {
            onResponseAction(ctx, action);
            String idOrder = action.getRequest().getParameter("response_order_id");
            if (map.containsKey("result")) map.remove("result");
            Map<String, String> params = new HashMap<String, String>();
            params.put("idOrder", idOrder);
            params.put("blockCode", BLOCK_APPROVED_TEXT);
            map.put("redirectUrl", action.getActionUrl("paystepResult", params));
            return true;
        } else if (EventService.EVENT_CUSTOM_ACTION == eventType && map != null && map.containsKey("actionName") && REJECTED_ACTION_NAME.equalsIgnoreCase((String) map.get("actionName"))) {
            onResponseAction(ctx, action);
            String idOrder = action.getRequest().getParameter("response_order_id");
            if (map.containsKey("result")) map.remove("result");
            Map<String, String> params = new HashMap<String, String>();
            params.put("idOrder", idOrder);
            params.put("blockCode", BLOCK_REJECTED_TEXT);
            map.put("redirectUrl", action.getActionUrl("paystepResult", params));
            return true;
        }
        return false;
    }

    public void onResponseAction(ServletContext ctx, FrontModuleAction action) {
        OtherAction gAction = (OtherAction) action;

        Map<String, Serializable> reqMap = new HashMap<String, Serializable>();
        reqMap.put("transaction.type", action.getRequest().getParameter("trans_name"));
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-mm-dd");
        Date d = null;
        try {
            d = sdf.parse(action.getRequest().getParameter("date_stamp") + " " + action.getRequest().getParameter("time_stamp"));
        } catch (ParseException ignored) {
        }
        if (d == null) d = Calendar.getInstance().getTime();
        reqMap.put("transaction.date", d);
        reqMap.put("autorization.code", action.getRequest().getParameter("bank_approval_code"));
        reqMap.put("response.code", action.getRequest().getParameter("response_code"));
        reqMap.put("iso.code", action.getRequest().getParameter("iso_code"));
        reqMap.put("response.message", action.getRequest().getParameter("message"));
        reqMap.put("reference.number", action.getRequest().getParameter("bank_transaction_id"));
        reqMap.put("cardholder.name", action.getRequest().getParameter("cardholder"));
        action.addToStack("transactionInfo", reqMap);

        // Processing response
        Long idOrder = NumberUtils.toLong(action.getRequest().getParameter("response_order_id"));
        Order order = (Order) action.getDao().get(Order.class, idOrder);
        if (order != null && OrderStatus.STATUS_DEFAULT.equalsIgnoreCase(order.getStatus().getStatusCode())) {
            try {
                String cad = JSONUtil.serialize(action.getRequest().getParameterMap());
                order.setMerchantData(cad);
            } catch (JSONException ignored) {
            }

            Map<String, Object> map1 = new HashMap<String, Object>();
            map1.put("order", order);
            order.setPaymentMethod(PAYMENT_SERVICE_NAME);
            order.setCodeMerchant(gAction.getRequest().getParameter("bank_transaction_id"));
            order.setPaymentCard(MonerisServiceImpl.getCardName(gAction.getRequest().getParameter("card")));
            Integer status = NumberUtils.toInt(action.getRequest().getParameter("response_code"));
            if (status != null && status < 50) {
                order.setStatus(gAction.getDao().getOrderStatus(OrderStatus.STATUS_APPROVED, true));
                gAction.getDao().save(order);
                gAction.addOrderHistory(order, gAction.getFrontUser(), "");
                if (order.getStatus().getSendEmail()) gAction.sendOrderStatusMail(order);
                gAction.addToStackSession("blockCode", StaticText.BLOCK_ORDER_APPROVED);
                EventUtils.executeEvent(ctx, EventService.EVENT_APPROVE_ORDER, gAction, map1);
            } else {
                order.setStatus(gAction.getDao().getOrderStatus(OrderStatus.STATUS_REJECTED, true));
                gAction.getDao().save(order);
                gAction.addOrderHistory(order, gAction.getFrontUser(), "");
                if (order.getStatus().getSendEmail()) gAction.sendOrderStatusMail(order);
                // Recuperar stock reservado
                gAction.recoverOrderStock(order);
                gAction.addToStackSession("blockCode", StaticText.BLOCK_ORDER_REJECTED);
                EventUtils.executeEvent(ctx, EventService.EVENT_DENY_ORDER, gAction, map1);
            }
            gAction.addToStackSession("idOrder", order.getIdOrder());
        } else {
            action.addActionError(action.getText(CNT_ERROR_ORDER_NOT_FOUND, CNT_DEFAULT_ERROR_ORDER_NOT_FOUND));
        }
    }

    public void initialize(ServletContext ctx, Session databaseSession, String store) {
        HibernateDAO dao = new HibernateDAO(databaseSession, store);
        StoreProperty bean = dao.getStoreProperty(StoreProperty.PROP_LANGUAGES, StoreProperty.TYPE_GENERAL);
        String[] languages = (bean != null && !StringUtils.isEmpty(bean.getValue())) ? bean.getValue().split(",") : null;

        // Crear etiquetas que usa la aplicacion
        if (languages != null) {
            createBlock(dao, BLOCK_APPROVED_TEXT, "<h1>Your order was approved</h1>", store, languages);
            createBlock(dao, BLOCK_REJECTED_TEXT, "<h1>Your order was rejected</h1>", store, languages);
        }

        // Agregar los bloques a los templates
        Store20Config storeConfig = Store20Config.getInstance(ctx);
        TemplateBlock block1 = new TemplateBlock(BLOCK_APPROVED_TEXT);
        if (languages!=null) for(String lang : languages) block1.setName(lang, "Moneris HPP<br/>Approved Message");
        storeConfig.addBlock(block1);
        TemplateBlock block2 = new TemplateBlock(BLOCK_REJECTED_TEXT);
        if (languages!=null) for(String lang : languages) block2.setName(lang, "Moneris HPP<br/>Rejected Message");
        storeConfig.addBlock(block2);

    }

    private void createBlock(HibernateDAO dao, String code, String text, String store, String[] languages) {
        StaticText st = dao.getStaticText(code, StaticText.TYPE_BLOCK);
        if (st == null) {
            st = new StaticText();
            st.setCode(code);
            st.setInventaryCode(store);
            st.setTextType(StaticText.TYPE_BLOCK);
            dao.save(st);
            for (String lang : languages) {
                StaticTextLang stl = new StaticTextLang();
                stl.setStaticLang(lang);
                stl.setStaticText(st);
                stl.setValue(text);
                dao.save(stl);
            }
        }
    }


}
