package org.store.merchants.paypal;

import com.opensymphony.xwork2.util.logging.Logger;
import com.opensymphony.xwork2.util.logging.LoggerFactory;
import org.store.core.beans.*;
import org.store.core.dao.HibernateDAO;
import org.store.core.front.FrontModuleAction;
import org.store.core.globals.SomeUtils;
import org.store.core.globals.config.Store20Config;
import org.store.core.utils.events.DefaultEventServiceImpl;
import org.store.core.utils.events.EventService;
import org.store.core.utils.templates.TemplateBlock;
import org.apache.commons.lang.StringUtils;
import org.apache.struts2.json.JSONException;
import org.apache.struts2.json.JSONUtil;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import javax.servlet.ServletContext;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PaypalIntegralEventImpl extends DefaultEventServiceImpl {
    public static final String PAYPAL_HOSTED_PAGE = "Paypal Hosted Page";
    public static final String ACTION_RETURN = "paypal_hps_return";
    public static final String ACTION_NOTIFY = "paypal_hps_notify";
    private static final Logger LOG = LoggerFactory.getLogger(PaypalIntegralEventImpl.class);
    private static final String CNT_ERROR_ORDER_NOT_FOUND = "error.order.not.found";
    private static final String CNT_DEFAULT_ERROR_ORDER_NOT_FOUND = "Order not found";
    private static final String CNT_ERROR_TRANSACTION_NOT_FOUND = "error.paypal.transaction.not.found";
    private static final String CNT_DEFAULT_ERROR_TRANSACTION_NOT_FOUND = "Paypal transaction not found.";

    private static final String BLOCK_APPROVED_TEXT = "paypal.hpp.payment.result.approved";
    private static final String BLOCK_REJECTED_TEXT = "paypal.hpp.payment.result.rejected";
    private static final String BLOCK_PENDING_TEXT = "paypal.hpp.payment.result.pending";

    private static final String PAYPAL_IPN_LIVE = "https://securepayments.paypal.com/cgi-bin/webscr";
    private static final String PAYPAL_IPN_SANDBOX = "https://securepayments.sandbox.paypal.com/webscr";

    public String getName() {
        return PAYPAL_HOSTED_PAGE;
    }

    public String getDescription(FrontModuleAction action) {
        return "Paypal Hosted Page Implementation";
    }

    // Procesar la respuesta de paypal
    public Boolean onExecuteEvent(ServletContext ctx, int eventType, FrontModuleAction action, Map<String, Object> map) {
        if (EventService.EVENT_CUSTOM_ACTION == eventType && map != null && map.containsKey("actionName")) {
            if (ACTION_RETURN.equalsIgnoreCase((String) map.get("actionName"))) {
                PaypalIntegralServiceImpl service = new PaypalIntegralServiceImpl();
                service.loadProperties(action);
                String transactionId = action.getRequest().getParameter("tx");
                if (StringUtils.isNotEmpty(transactionId)) {
                    Map paymentResult = service.getTransactionDetailsNVP(transactionId);
                    if (paymentResult != null) {
                        String paymentStatus = (String) paymentResult.get("PAYMENTSTATUS");
                        Long idOrder = SomeUtils.strToLong((String) paymentResult.get("INVNUM"));
                        Order order = (Order) action.getDao().get(Order.class, idOrder);
                        if (order != null) {

                            // datos completos de la transaccion
                            try {
                                String cad = JSONUtil.serialize(action.getRequest().getParameterMap());
                                order.setMerchantData(cad);
                            } catch (JSONException ignored) {
                            }

                            order.setPaymentMethod(PAYPAL_HOSTED_PAGE);
                            order.setCodeMerchant(transactionId);

                            action.addToStackSession("idOrder", order.getIdOrder());

                            if (map.containsKey("result")) map.remove("result");
                            Map<String, String> params = new HashMap<String, String>();
                            params.put("idOrder", idOrder.toString());

                            if ("Pending".equalsIgnoreCase(paymentStatus)) {
                                // Pendiente de confirmacion
                                order.setStatus(action.getDao().getOrderStatus(OrderStatus.STATUS_PAYMENT_VALIDATION, true));
                                action.getDao().save(order);
                                action.addOrderHistory(order, action.getFrontUser(), "Pending for payment validation");
                                if (order.getStatus().getSendEmail()) action.sendOrderStatusMail(order);
                                params.put("blockCode", BLOCK_PENDING_TEXT);
                            } else if ("Completed".equalsIgnoreCase(paymentStatus) || "Processed".equalsIgnoreCase(paymentStatus)) {
                                // Aceptar
                                service.approveOrder(ctx, order, action);
                                params.put("blockCode", BLOCK_APPROVED_TEXT);
                            } else {
                                // Rechazar
                                service.rejectOrder(ctx, order, action);
                                params.put("blockCode", BLOCK_REJECTED_TEXT);
                            }
                            map.put("redirectUrl", action.getActionUrl("paystepResult", params));
                        } else {
                            action.addActionError(action.getText(CNT_ERROR_ORDER_NOT_FOUND, CNT_DEFAULT_ERROR_ORDER_NOT_FOUND));
                            map.put("redirectUrl", action.url("home", ""));
                        }
                    } else {
                        action.addActionError(action.getText(CNT_ERROR_TRANSACTION_NOT_FOUND, CNT_DEFAULT_ERROR_TRANSACTION_NOT_FOUND));
                        map.put("redirectUrl", action.url("home", ""));
                        LOG.error("Paypal Transaction not found, ID: " + transactionId);
                    }
                } else {

                    Long idOrder = SomeUtils.strToLong(action.getRequest().getParameter("invoice"));
                    Order order = (Order) action.getDao().get(Order.class, idOrder);
                    String paymentStatus = action.getRequest().getParameter("payment_status");
                    transactionId = action.getRequest().getParameter("txn_id");
                    if (order != null) {
                        order.setPaymentMethod(PAYPAL_HOSTED_PAGE);
                        order.setCodeMerchant(transactionId);
                        action.addToStackSession("idOrder", order.getIdOrder());

                        if (map.containsKey("result")) map.remove("result");

                        Map<String, String> params = new HashMap<String, String>();
                        params.put("idOrder", idOrder.toString());

                        if ("Pending".equalsIgnoreCase(paymentStatus)) {
                            // Pendiente de confirmacion
                            if (OrderStatus.STATUS_DEFAULT.equalsIgnoreCase(order.getStatus().getStatusCode())) {
                                order.setStatus(action.getDao().getOrderStatus(OrderStatus.STATUS_PAYMENT_VALIDATION, true));
                                action.getDao().save(order);
                                action.addOrderHistory(order, action.getFrontUser(), "Pending for payment validation");
                                if (order.getStatus().getSendEmail()) action.sendOrderStatusMail(order);
                            }
                            params.put("blockCode", BLOCK_PENDING_TEXT);
                        } else if ("Completed".equalsIgnoreCase(paymentStatus) || "Processed".equalsIgnoreCase(paymentStatus)) {
                            // Aceptar
                            if (OrderStatus.STATUS_DEFAULT.equalsIgnoreCase(order.getStatus().getStatusCode()) || OrderStatus.STATUS_PAYMENT_VALIDATION.equalsIgnoreCase(order.getStatus().getStatusCode()))
                                service.approveOrder(ctx, order, action);
                            params.put("blockCode", BLOCK_APPROVED_TEXT);
                        } else {
                            // Rechazar
                            if (OrderStatus.STATUS_DEFAULT.equalsIgnoreCase(order.getStatus().getStatusCode()) || OrderStatus.STATUS_PAYMENT_VALIDATION.equalsIgnoreCase(order.getStatus().getStatusCode()))
                                service.rejectOrder(ctx, order, action);
                            params.put("blockCode", BLOCK_REJECTED_TEXT);
                        }
                        map.put("redirectUrl", action.getActionUrl("paystepResult", params));
                    } else {
                        action.addActionError(action.getText(CNT_ERROR_TRANSACTION_NOT_FOUND, CNT_DEFAULT_ERROR_TRANSACTION_NOT_FOUND));
                        map.put("redirectUrl", action.url("home", ""));
                        LOG.error("TransactionID from paypal is null");
                    }
                }
                return true;
            } else if (ACTION_NOTIFY.equalsIgnoreCase((String) map.get("actionName"))) {
                StringBuilder buff = new StringBuilder();
                buff.append("cmd=_notify-validate");
                Enumeration<String> enumParams = action.getRequest().getParameterNames();
                while (enumParams.hasMoreElements()) {
                    String paramName = enumParams.nextElement();
                    String paramValue = action.getRequest().getParameter(paramName);
                    buff.append("&").append(paramName).append("=").append((paramValue != null) ? paramValue : "");
                }
                PaypalIntegralServiceImpl service = new PaypalIntegralServiceImpl();
                service.loadProperties(action);
                // testing if valid post
                boolean isValid = false;
                try {
                    URL url = new URL("live".equalsIgnoreCase(service.getProperty("paypal.enviroment", "sandbox")) ? PAYPAL_IPN_LIVE : PAYPAL_IPN_SANDBOX);
                    URLConnection conn = url.openConnection();
                    conn.setDoInput(true);
                    conn.setDoOutput(true);
                    conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                    conn.setRequestProperty("Content-length", String.valueOf(buff.length()));
                    OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());
                    writer.write(buff.toString());
                    writer.flush();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    String line = reader.readLine();
                    isValid = "VERIFIED".equalsIgnoreCase(line);
                    writer.close();
                    reader.close();
                } catch (Exception ex) {
                    LOG.error(ex.getMessage(), ex);
                }

                // process data
                Long idOrder = SomeUtils.strToLong(action.getRequest().getParameter("invoice"));
                Order order = (Order) action.getDao().get(Order.class, idOrder);
                String paymentStatus = action.getRequest().getParameter("payment_status");
                String transactionId = action.getRequest().getParameter("txn_id");
                if (order != null) {
                    order.setPaymentMethod(PAYPAL_HOSTED_PAGE);
                    order.setCodeMerchant(transactionId);
                    action.addToStackSession("idOrder", order.getIdOrder());

                    if (map.containsKey("result")) map.remove("result");

                    if ("Pending".equalsIgnoreCase(paymentStatus)) {
                        // Pendiente de confirmacion
                        if (OrderStatus.STATUS_DEFAULT.equalsIgnoreCase(order.getStatus().getStatusCode())) {
                            order.setStatus(action.getDao().getOrderStatus(OrderStatus.STATUS_PAYMENT_VALIDATION, true));
                            action.getDao().save(order);
                            action.addOrderHistory(order, action.getFrontUser(), "Pending for payment validation");
                            if (order.getStatus().getSendEmail()) action.sendOrderStatusMail(order);
                        }
                    } else if ("Completed".equalsIgnoreCase(paymentStatus) || "Processed".equalsIgnoreCase(paymentStatus)) {
                        // Aceptar
                        if (OrderStatus.STATUS_DEFAULT.equalsIgnoreCase(order.getStatus().getStatusCode()) || OrderStatus.STATUS_PAYMENT_VALIDATION.equalsIgnoreCase(order.getStatus().getStatusCode()))
                            service.approveOrder(ctx, order, action);
                    } else {
                        // Rechazar
                        if (OrderStatus.STATUS_DEFAULT.equalsIgnoreCase(order.getStatus().getStatusCode()) || OrderStatus.STATUS_PAYMENT_VALIDATION.equalsIgnoreCase(order.getStatus().getStatusCode()))
                            service.rejectOrder(ctx, order, action);
                    }
                }
                return true;
            }
        }
        return false;
    }

    public void initialize(ServletContext ctx, Session databaseSession, String store) {
        HibernateDAO dao = new HibernateDAO(databaseSession, store);
        StoreProperty bean = dao.getStoreProperty(StoreProperty.PROP_LANGUAGES, StoreProperty.TYPE_GENERAL);
        String[] languages = (bean != null && !StringUtils.isEmpty(bean.getValue())) ? bean.getValue().split(",") : null;

        // Crear etiquetas que usa la aplicacion
        if (languages != null) {
            createBlock(dao, BLOCK_APPROVED_TEXT, "<h1>Your order was approved</h1>", store, languages);
            createBlock(dao, BLOCK_REJECTED_TEXT, "<h1>Your order was rejected</h1>", store, languages);
            createBlock(dao, BLOCK_PENDING_TEXT, "<h1>Your order is pending for payment validation</h1>", store, languages);
        }

        // Agregar los bloques a los templates
        // Agregar los bloques a los templates
        Store20Config storeConfig = Store20Config.getInstance(ctx);
        TemplateBlock block1 = new TemplateBlock(BLOCK_APPROVED_TEXT);
        if (languages!=null) for(String lang : languages) block1.setName(lang, "Paypal Hosted Page<br/>Approved Message");
        storeConfig.addBlock(block1);
        TemplateBlock block2 = new TemplateBlock(BLOCK_REJECTED_TEXT);
        if (languages!=null) for(String lang : languages) block2.setName(lang, "Paypal Hosted Page<br/>Rejected Message");
        storeConfig.addBlock(block2);
        TemplateBlock block3 = new TemplateBlock(BLOCK_PENDING_TEXT);
        if (languages!=null) for(String lang : languages) block3.setName(lang, "Paypal Hosted Page<br/>Payment Validation Message");
        storeConfig.addBlock(block3);

        // crear order status
        List l = dao.createCriteriaForStore(OrderStatus.class).add(Restrictions.eq("statusCode", OrderStatus.STATUS_PAYMENT_VALIDATION)).list();
        OrderStatus orderStatus = (l != null && l.size() > 0) ? (OrderStatus) l.get(0) : null;
        if (orderStatus == null) {
            orderStatus = new OrderStatus();
            orderStatus.setStatusCode(OrderStatus.STATUS_PAYMENT_VALIDATION);
            orderStatus.setStatusType(OrderStatus.STATUS_DEFAULT);
            if (languages != null) for (String lang : languages) orderStatus.setStatusName(lang, "Payment Validation");
            orderStatus.setSendEmail(true);
            if (dao.hasStore()) orderStatus.setInventaryCode(dao.getStoreCode());
            dao.save(orderStatus);
        }

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
