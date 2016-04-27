package org.store.merchants.paypal.front;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.json.JSONException;
import org.apache.struts2.json.JSONUtil;
import org.store.core.beans.Order;
import org.store.core.beans.OrderStatus;
import org.store.core.dao.HibernateDAO;
import org.store.core.front.FrontModuleAction;
import org.store.core.utils.events.EventUtils;

@Namespace("/")
@ParentPackage("store-front")
public class PayflowHPPAction extends FrontModuleAction {
    private static final String BLOCK_APPROVED_TEXT = "payflow-hpp.pay.result.approved";
    private static final String BLOCK_REJECTED_TEXT = "payflow-hpp.pay.result.rejected";

    @Action("payflow_response")
    public String payFlowResponse()
            throws Exception {
        onResponseAction();
        return null;
    }

    @Action("payflow_return")
    public String payFlowReturn()
            throws Exception {
        boolean approved = onResponseAction();
        Long idOrder = getSecureTokenOrderID(getRequest().getParameter("SECURETOKENID"));

        Map<String, String> params = new HashMap();
        params.put("idOrder", idOrder.toString());
        params.put("blockCode", approved ? "payflow-hpp.pay.result.approved" : "payflow-hpp.pay.result.rejected");
        setRedirectUrl(getActionUrl("paystepResult", params));
        return "redirectUrl";
    }

    public boolean onResponseAction() {
        boolean res = false;

        Map<String, Serializable> reqMap = new HashMap();
        reqMap.put("transaction.type", getRequest().getParameter("SECURETOKEN"));
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date d = null;
        try {
            String dStr = getRequest().getParameter("TRANSTIME");
            if ((dStr != null) && (!"".equals(dStr))) {
                d = sdf.parse(dStr);
            }
        } catch (ParseException ignored) {
        }
        if (d == null) {
            d = Calendar.getInstance().getTime();
        }
        reqMap.put("transaction.date", d);
        reqMap.put("autorization.code", getRequest().getParameter("PPREF"));
        addToStack("transactionInfo", reqMap);

        Long idOrder = getSecureTokenOrderID(getRequest().getParameter("SECURETOKENID"));
        Order order = (Order) getDao().get(Order.class, idOrder);
        if ((order != null) && ("pending".equalsIgnoreCase(order.getStatus().getStatusCode()))) {
            try {
                String cad = JSONUtil.serialize(getRequest().getParameterMap());
                order.setMerchantData(cad);
            } catch (JSONException ignored) {
            }
            Map<String, Object> map1 = new HashMap();
            map1.put("order", order);
            order.setPaymentMethod("Payflow Hosted Page");
            order.setCodeMerchant(getRequest().getParameter("PPREF"));
            if ("0".equalsIgnoreCase(getRequest().getParameter("RESULT"))) {
                res = true;
                order.setPaymentCard(getCardName(getRequest().getParameter("TENDER"), getRequest().getParameter("CARDTYPE")));
                order.setStatus(getDao().getOrderStatus("approved", true));
                getDao().save(order);
                addOrderHistory(order, getFrontUser(), "");
                if (order.getStatus().getSendEmail().booleanValue()) {
                    sendOrderStatusMail(order);
                }
                addToStackSession("blockCode", "order.approved.text");
                EventUtils.executeEvent(getServletContext(), 12, this, map1);
            } else {
                order.setStatus(getDao().getOrderStatus("rejected", true));
                getDao().save(order);
                addOrderHistory(order, getFrontUser(), "");
                if (order.getStatus().getSendEmail().booleanValue()) {
                    sendOrderStatusMail(order);
                }
                recoverOrderStock(order);
                addToStackSession("blockCode", "order.rejected.text");
                EventUtils.executeEvent(getServletContext(), 13, this, map1);
            }
            addToStackSession("idOrder", order.getIdOrder());
        } else {
            if (order != null) {
                return !getDao().getOrderStatus("rejected", true).equals(order.getStatus());
            }
            addActionError(getText("order.not.found", "Order not found"));
        }
        return res;
    }

    private String getCardName(String method, String cardType) {
        if (method != null) {
            if ("C".equalsIgnoreCase(method)) {
                if (cardType != null) {
                    if ("0".equals(cardType)) {
                        return "Visa";
                    }
                    if ("1".equals(cardType)) {
                        return "MasterCard";
                    }
                    if ("2".equals(cardType)) {
                        return "Discover";
                    }
                    if ("3".equals(cardType)) {
                        return "American Express";
                    }
                    if ("4".equals(cardType)) {
                        return "Diner’s Club";
                    }
                    if ("5".equals(cardType)) {
                        return "JCB";
                    }
                }
                return "";
            }
            if ("P".equalsIgnoreCase(method)) {
                return "Paypal";
            }
            if ("D".equalsIgnoreCase(method)) {
                return "Pinless debit";
            }
            if ("K".equalsIgnoreCase(method)) {
                return "Telecheck";
            }
        }
        return "";
    }

    public static Long getSecureTokenOrderID(String secureToken) {
        return secureToken != null ? Long.valueOf(Long.parseLong(secureToken.substring(26))) : null;
    }
}
