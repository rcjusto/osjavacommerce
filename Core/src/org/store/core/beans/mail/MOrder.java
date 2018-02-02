package org.store.core.beans.mail;

import org.apache.commons.lang.StringUtils;
import org.store.core.beans.Currency;
import org.store.core.beans.Order;
import org.store.core.beans.StoreProperty;
import org.store.core.globals.BaseAction;
import org.store.core.globals.CountryFactory;
import org.store.core.globals.SomeUtils;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Rogelio Caballero
 * 19/07/12 18:21
 */
public class MOrder {

    private Order order;
    private BaseAction action;
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    private static final String MAIL_TEMPLATE_INVOICE_ITEMS = "/WEB-INF/views/mails/invoice_items.vm";
    private static final String MAIL_TEMPLATE_ORDER_ITEMS = "/WEB-INF/views/mails/order_items.vm";

    public MOrder(Order order, BaseAction action) {
        this.order = order;
        this.action = action;
    }

    public Order getData() {
        return order;
    }

    public String getCode() {
        return order.getIdOrder().toString();
    }

    public boolean hasInvoiceNo() {
        return StringUtils.isNotEmpty(getInvoiceNo());
    }

    public String getInvoiceNo() {
        if ("Y".equalsIgnoreCase(action.getStoreProperty(StoreProperty.PROP_INVOICE_NUMBER_AUTOMATIC, "N"))) {
            String format = action.getStoreProperty(StoreProperty.PROP_INVOICE_NUMBER_PREFIX, "#");
            NumberFormat nf = new DecimalFormat(format);
            return (order.getInvoiceConsecutive() != null) ? nf.format(order.getInvoiceConsecutive()) : "";
        } else {
            return order.getInvoiceNo();
        }
    }

    public boolean hasInvoiceDate() {
        return StringUtils.isNotEmpty(getInvoiceDate());
    }

    public String getInvoiceDate() {
        return (order.getInvoiceDate() != null) ? SomeUtils.formatDate(order.getInvoiceDate(), action.getDefaultLanguage()) : "";
    }

    public String getBillingCompany() {
        return (order.getBillingAddress() != null) ? order.getBillingAddress().getCompany() : "";
    }

    public String getBillingFullName() {
        return (order.getBillingAddress() != null) ? order.getBillingAddress().getFullName() : "";
    }

    public String getBillingAddressLine1() {
        return (order.getBillingAddress() != null) ? order.getBillingAddress().getAddress() : "";
    }

    public String getBillingAddressLine2() {
        return (order.getBillingAddress() != null) ? order.getBillingAddress().getAddress2() : "";
    }

    public String getBillingPostalCode() {
        return (order.getBillingAddress() != null) ? order.getBillingAddress().getZipCode() : "";
    }

    public String getBillingCity() {
        return (order.getBillingAddress() != null) ? order.getBillingAddress().getCity() : "";
    }

    public String getBillingState() {
        return (order.getBillingAddress() != null && order.getBillingAddress().getState() != null) ? order.getBillingAddress().getState().getStateName() : "";
    }

    public String getBillingCountry() {
        return (order.getBillingAddress() != null && order.getBillingAddress().getIdCountry() != null) ? CountryFactory.getCountryName(order.getBillingAddress().getIdCountry(), action.getLocale()) : "";
    }

    public String getBillingFax() {
        return (order.getBillingAddress() != null) ? order.getBillingAddress().getFax() : "";
    }

    public String getBillingPhone() {
        return (order.getBillingAddress() != null) ? order.getBillingAddress().getPhone() : "";
    }

    public String getShippingCompany() {
        return (order.getDeliveryAddress() != null) ? order.getDeliveryAddress().getCompany() : "";
    }

    public String getShippingFullName() {
        return (order.getDeliveryAddress() != null) ? order.getDeliveryAddress().getFullName() : "";
    }

    public String getShippingAddressLine1() {
        return (order.getDeliveryAddress() != null) ? order.getDeliveryAddress().getAddress() : "";
    }

    public String getShippingAddressLine2() {
        return (order.getDeliveryAddress() != null) ? order.getDeliveryAddress().getAddress2() : "";
    }

    public String getShippingCity() {
        return (order.getDeliveryAddress() != null) ? order.getDeliveryAddress().getCity() : "";
    }

    public String getShippingState() {
        return (order.getDeliveryAddress() != null && order.getDeliveryAddress().getState() != null) ? order.getDeliveryAddress().getState().getStateName() : "";
    }

    public String getShippingCountry() {
        return (order.getDeliveryAddress() != null && order.getDeliveryAddress().getIdCountry() != null) ? CountryFactory.getCountryName(order.getDeliveryAddress().getIdCountry(), action.getLocale()) : "";
    }

    public String getShippingFax() {
        return (order.getDeliveryAddress() != null) ? order.getDeliveryAddress().getFax() : "";
    }

    public String getShippingPhone() {
        return (order.getDeliveryAddress() != null) ? order.getDeliveryAddress().getPhone() : "";
    }

    public String getShippingPostalCode() {
        return (order.getDeliveryAddress() != null) ? order.getDeliveryAddress().getZipCode() : "";
    }

    public boolean hasPurchaseOrder() {
        return StringUtils.isNotEmpty(order.getPurchaseOrder());
    }

    public String getPurchaseOrder() {
        return order.getPurchaseOrder();
    }

    public String getPaymentMethod() {
        if (StringUtils.isNotEmpty(order.getPaymentCard())) {
            return order.getPaymentCard();
        } else if (StringUtils.isNotEmpty(order.getPaymentMethod())) {
            return action.getPaymentMethodName(order.getPaymentMethod());
        }
        return "";
    }

    public String getShippingMethod() {
        if (order.getShippingMethod() != null) {
            return order.getShippingMethod().getMethodName(action.getDefaultLanguage());
        } else if (order.getPickInStore() != null) {
            return action.getText("shipping.type.pickinstore");
        }
        return "";
    }

    public String getCreatedDate() {
        return (order.getCreatedDate() != null) ? SomeUtils.formatDate(order.getCreatedDate(), action.getDefaultLanguage()) : "";
    }

    public String getSentDate() {
        return (order.getSentDate() != null) ? SomeUtils.formatDate(order.getSentDate(), action.getDefaultLanguage()) : "";
    }

    public String getInvoiceItems() {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("order", order);
        return action.proccessVelocityTemplate(MAIL_TEMPLATE_INVOICE_ITEMS, map);
    }

    public String getOrderItems() {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("order", this);
        return action.proccessVelocityTemplate(MAIL_TEMPLATE_ORDER_ITEMS, map);
    }

    public boolean hasCustomReference() {
        return StringUtils.isNotEmpty(order.getCustomReference());
    }

    public String getCustomReference() {
        return order.getCustomReference();
    }

    public String getStatus() {
        return (order.getStatus() != null) ? order.getStatus().getStatusName(action.getDefaultLanguage()) : "";
    }

    public boolean hasCodeMerchant() {
        return StringUtils.isNotEmpty(order.getCodeMerchant());
    }

    public String getCodeMerchant() {
        return order.getCodeMerchant();
    }

    public Currency getCurrency() {
        return order.getCurrency();
    }

    public String getSubtotalProducts() {
        return action.formatActualCurrency(order.getTotalProducts(), order.getCurrency());
    }

    public boolean hasFees() {
        return order.getTotalFees() != null && order.getTotalFees() > 0;
    }

    public String getSubtotalFees() {
        return action.formatActualCurrency(order.getTotalFees(), order.getCurrency());
    }

    public boolean hasInterest() {
        return order.getInterestPercent() != null && order.getInterestPercent() > 0;
    }

    public String getPercentInterest() {
        if (order.getInterestPercent() != null) {
            DecimalFormat df = new DecimalFormat("0.##");
            df.format(order.getInterestPercent());
        }
        return "";
    }

    public String getSubtotalInterest() {
        return action.formatActualCurrency(order.getTotalInterest(), order.getCurrency());
    }

    public boolean hasInsurance() {
        return order.getTotalInsurance() != null && order.getTotalInsurance() > 0;
    }

    public String getSubtotalInsurance() {
        return action.formatActualCurrency(order.getTotalInsurance(), order.getCurrency());
    }

    public String getSubtotalDiscount() {
        return action.formatActualCurrency(order.getTotalDiscountPromotion(), order.getCurrency());
    }

    public String getSubtotalShipping() {
        return action.formatActualCurrency(order.getTotalShipping(), order.getCurrency());
    }

    public boolean hasRewards() {
        return order.getTotalRewards() != null && order.getTotalRewards() > 0;
    }

    public String getSubtotalRewards() {
        return action.formatActualCurrency(-order.getTotalRewards(), order.getCurrency());
    }

    public String getTotal() {
        return action.formatActualCurrency(order.getTotal(), order.getCurrency());
    }

    public String getShippingLabel() {
        List<Map<String, Object>> freeShipPromList = order.getPromotions("free-shipping");
        if (freeShipPromList != null && !freeShipPromList.isEmpty()) {
            Map<String, Object> map = freeShipPromList.get(0);
            return "PROMO: " + map.get("code") + " - " + map.get("name");
        } else {
            return action.getText("subtotal.shipping");
        }
    }

    public String getTaxesRows() {
        if (order.getTaxes() != null && !order.getTaxes().isEmpty()) {
            DecimalFormat df = new DecimalFormat("0.##");
            StringBuilder b = new StringBuilder();
            for (Map<String, Object> map : order.getTaxes()) {
                b.append("<tr>\n")
                        .append("<th>")
                        .append(action.getText("subtotal.tax"))
                        .append(" ")
                        .append(map.get("name"));
                if (map.containsKey("totax")) {
                    b.append("<span class=\"tax-detail\">")
                            .append(df.format(map.get("percent")))
                            .append(" ").append(action.getText("of")).append(" ")
                            .append(action.formatActualCurrency((Number) map.get("totax"), order.getCurrency()))
                            .append("</span>");
                }
                b.append("</th>\n")
                        .append("<td>")
                        .append(action.formatActualCurrency((Number) map.get("value"), order.getCurrency()))
                        .append("</td>\n")
                        .append("</tr>\n");
            }
            return b.toString();
        }
        return "";
    }

    public String getPickInStoreName() {
        return (order.getPickInStore() != null) ? order.getPickInStore().getStoreName() : "";
    }

    public String getPickInStoreAddressLine1() {
        return (order.getPickInStore() != null) ? order.getPickInStore().getAddress() : "";
    }

    public String getPickInStoreAddressLine2() {
        return (order.getPickInStore() != null) ? order.getPickInStore().getAddress2() : "";
    }

    public String getPickInStoreCity() {
        return (order.getPickInStore() != null) ? order.getPickInStore().getCity() : "";
    }

    public String getPickInStorePhone() {
        return (order.getPickInStore() != null) ? order.getPickInStore().getPhone() : "";
    }

    public String getPickInStorePostalCode() {
        return (order.getPickInStore() != null) ? order.getPickInStore().getZipCode() : "";
    }

    public String getPickInStoreFax() {
        return (order.getPickInStore() != null) ? order.getPickInStore().getFax() : "";
    }

    public String getPickInStoreEmail() {
        return (order.getPickInStore() != null) ? order.getPickInStore().getEmail() : "";
    }

    public String getPickInStoreState() {
        return (order.getPickInStore() != null && order.getPickInStore().getState() != null) ? order.getPickInStore().getState().getStateName() : "";
    }

    public String getPickInStoreCountry() {
        return (order.getPickInStore() != null && StringUtils.isNotEmpty(order.getPickInStore().getIdCountry())) ? CountryFactory.getCountryName(order.getPickInStore().getIdCountry(), action.getLocale()) : "";
    }

    public String getPayUrl() {
        Map params = new HashMap();
        params.put("idOrder", order.getIdOrder());
        try {
            return action.urlEnc(action.url("payorder", "", params));
        } catch (Exception ignored) {
        }
        return "";
    }

}
