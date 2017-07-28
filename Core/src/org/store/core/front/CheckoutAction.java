package org.store.core.front;

import org.store.core.beans.CategoryFee;
import org.store.core.beans.ComplementGroup;
import org.store.core.beans.Insurance;
import org.store.core.beans.Order;
import org.store.core.beans.OrderDetail;
import org.store.core.beans.OrderDetailProduct;
import org.store.core.beans.OrderHistory;
import org.store.core.beans.OrderStatus;
import org.store.core.beans.Product;
import org.store.core.beans.ProductUserTax;
import org.store.core.beans.ProductVariation;
import org.store.core.beans.PromotionalCode;
import org.store.core.beans.ShippingMethod;
import org.store.core.beans.ShopCart;
import org.store.core.beans.ShopCartItem;
import org.store.core.beans.State;
import org.store.core.beans.StaticText;
import org.store.core.beans.TaxPerFamily;
import org.store.core.beans.UserAddress;
import org.store.core.beans.UserRewardHistory;
import org.store.core.beans.utils.BreadCrumb;
import org.store.core.globals.ShopCartUtils;
import org.store.core.globals.SomeUtils;
import org.store.core.globals.StoreSessionInterceptor;
import org.store.core.utils.events.EventService;
import org.store.core.utils.events.EventUtils;
import org.store.core.utils.merchants.MerchantService;
import org.store.core.utils.merchants.MerchantUtils;
import org.store.core.utils.merchants.PaymentResult;
import org.store.sslplugin.annotation.Secured;
import org.apache.commons.lang.StringUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Rogelio Caballero
 * 5/08/11 3:38
 */
public class CheckoutAction extends FrontModuleAction {

    @Override
    public void prepare() throws Exception {
        super.prepare();
        shopCart = (ShopCart) dao.get(ShopCart.class, idShopCart);
    }

    @Secured
    public String checkout() throws Exception {
        // sino hay checkout usar el actual
        if (shopCart == null && getUserSession().getShoppingCart().getId() != null)
            shopCart = getUserSession().getShoppingCart();

        // validar q hay usuario
        if (getFrontUser() == null) {
            Map<String, String> params = new HashMap<String, String>();
            if (shopCart != null) params.put("idShopCart", shopCart.getId().toString());
            setRedirectUrl(url("checkout", "", params));
            return "register";
        }

        // validar q hay shopcart
        if (shopCart == null) {
            return "shopcart";
        }

        shopCart.initializeItems(this);

        // validar q hay productos
        if (shopCart.getItems() == null || shopCart.getItems().isEmpty() || !getFrontUser().equals(shopCart.getUser())) {
            return "shopcart";
        }

        // validar q no es vencida
        if (!shopCart.canCheckoutToday()) {
            return "shopcart";
        }

        // buscar wrapping options
        boolean canPickComplements = false;
        for (ShopCartItem item : shopCart.getItems()) {
            List<Product> complements = new ArrayList<Product>();
            Set<ComplementGroup> complementGroups = new HashSet<ComplementGroup>();
            Set<ComplementGroup> cg1 = dao.getParentCategoryComplement(item.getBeanProd1().getCategory());
            if (cg1 != null && !cg1.isEmpty()) complementGroups.addAll(cg1);
            Set<ComplementGroup> cg2 = item.getBeanProd1().getRelatedGroups();
            if (cg2 != null && !cg2.isEmpty()) complementGroups.addAll(cg2);
            for (ComplementGroup cg : complementGroups) {
                List<Product> cs = dao.getComplements(cg, true);
                if (cs != null && !cs.isEmpty()) complements.addAll(cs);
            }
            if (!complements.isEmpty()) {
                canPickComplements = true;
                item.addProperty("complements", complements);
            }
        }
        addToStack("canPickComplements", canPickComplements);

        // buscar ultima orden del usuario para obtener valores por defecto para la nueva orden
        Order lastOrder = dao.getUserLastOrder(frontUser);

        // direccion de billing por defecto
        if (shopCart.getBillingAddress() == null) {
            if (lastOrder != null) shopCart.setBillingAddress(lastOrder.getBillingAddress());
            else if (frontUser.getBillingAddress() != null) shopCart.setBillingAddress(frontUser.getBillingAddress());
        }

        // verificar metodo envio ultima orden
        if (shopCart.getDeliveryAddress() == null && lastOrder != null) shopCart.setDeliveryAddress(lastOrder.getDeliveryAddress());
        if (shopCart.getDeliveryAddress() == null && frontUser.getShippingAddress() != null) shopCart.setDeliveryAddress(frontUser.getShippingAddress());
        if (shopCart.getDeliveryAddress() == null && shopCart.getBillingAddress() != null) shopCart.setDeliveryAddress(shopCart.getBillingAddress());

        // Calcular costos y precios
        ShopCartUtils scu = getShopCartUtils(false);
        scu.removeSessionData(this);
        addToStack("scu", scu);

        getBreadCrumbs().add(new BreadCrumb("checkout", getText("checkout", "Checkout"), null, null));
        EventUtils.executeEvent(getServletContext(), EventService.EVENT_ONEPAGE_CHECKOUT, this);

        return SUCCESS;
    }

    @Secured
    public String checkoutGetComplements() throws Exception {
        checkout();
        return SUCCESS;
    }

    @Secured
    public String checkoutSetComplements() throws Exception {
        jsonResp = new HashMap<String, Serializable>();
        if (shopCart != null) {
            int components = 0;
            for (int i = 0; i < itemId.length; i++) {
                ShopCartItem item = shopCart.getItemById(itemId[i]);
                item.setComplement((complementId != null && complementId.length > i) ? complementId[i] : null);
                item.setMessage((complementMessage != null && complementMessage.length > i) ? complementMessage[i] : null);
                dao.save(item);
                if (complementId != null && complementId.length > i && complementId[i] != null) components += item.getQuantity();
            }
            jsonResp.put("data", components + " " + getText("complements.added", "complements.added"));
            jsonResp.put("result", "OK");
        } else {
            jsonResp.put("result", "ERROR");
        }
        return SUCCESS;
    }

    @Secured
    public String checkoutGetBilling() throws Exception {
        checkout();
        return SUCCESS;
    }

    @Secured
    public String checkoutSetBilling() throws Exception {
        jsonResp = new HashMap<String, Serializable>();
        if (shopCart != null) {
            if (idAddress != null) {
                shopCart.setBillingAddress(frontUser.getAddressById(idAddress));
            } else if (billingAdd != null) {
                billingAdd.setUser(frontUser);
                State billState = (State) dao.get(State.class, billingState);
                billingAdd.setState(billState);
                dao.save(billingAdd);
                shopCart.setBillingAddress(billingAdd);
            }
            dao.save(shopCart);

            if (shopCart.getBillingAddress() != null) {
                jsonResp.put("result", "OK");
                jsonResp.put("data", shopCart.getBillingAddress().getFullAddress(false));
            } else {
                checkout();
                jsonResp.put("result", "ERROR");
            }
        } else {
            jsonResp.put("result", "ERROR");
        }
        return SUCCESS;
    }

    @Secured
    public String checkoutGetShipping() throws Exception {
        checkout();
        return SUCCESS;
    }

    @Secured
    public String checkoutSetShipping() throws Exception {
        jsonResp = new HashMap<String, Serializable>();
        if (shopCart != null) {
            shopCart.setShippingMethod(null);
            if (idLocation != null) {
                shopCart.setPickInStore(getLocationStore(idLocation));
            } else if (idAddress != null) {
                shopCart.setDeliveryAddress(frontUser.getAddressById(idAddress));
                shopCart.setPickInStore(null);
            } else if (shippingAdd != null) {
                shippingAdd.setUser(frontUser);
                State shipState = (State) dao.get(State.class, shippingState);
                shippingAdd.setState(shipState);
                dao.save(shippingAdd);
                shopCart.setDeliveryAddress(shippingAdd);
                shopCart.setPickInStore(null);
            }
            dao.save(shopCart);

            if (shopCart.getPickInStore() != null) {
                jsonResp.put("result", "OK");
                jsonResp.put("type", "pickinstore");
                jsonResp.put("data", shopCart.getPickInStore().getFullAddress(false));
            } else if (shopCart.getDeliveryAddress() != null) {
                jsonResp.put("result", "OK");
                jsonResp.put("type", "delivery");
                jsonResp.put("data", shopCart.getDeliveryAddress().getFullAddress(false));
            } else {
                checkout();
                jsonResp.put("result", "ERROR");
            }
        } else {
            jsonResp.put("result", "ERROR");
        }
        return SUCCESS;
    }

    @Secured
    public String checkoutGetMethod() throws Exception {
        if (shopCart != null) {
            ShopCartUtils scu = getShopCartUtils(true);
            addToStack("scu", scu);
        }
        EventUtils.executeEvent(getServletContext(), EventService.EVENT_ONEPAGE_CHECKOUT, this);
        return SUCCESS;
    }

    @Secured
    public String checkoutSetMethod() throws Exception {
        jsonResp = new HashMap<String, Serializable>();
        if (shopCart != null) {
            ShippingMethod method = (ShippingMethod) dao.get(ShippingMethod.class, selectedMethod);
            shopCart.setShippingMethod(method);
            ShopCartUtils scu = getShopCartUtils(true);

            if (shopCart.getPickInStore() != null) {
                jsonResp.put("result", "OK");
                jsonResp.put("data", getText("pick.in.store", "Pick in store") + " " + formatActualCurrency(0));
                shopCart.setShippingMethod(null);
            } else if (scu.isFreeShipping()) {
                jsonResp.put("result", "OK");
                jsonResp.put("data", getText("free.shipping", "Free Shipping") + " " + formatActualCurrency(0));
                shopCart.setShippingMethod(null);
            } else if (scu.isShippingNeedSelection() && scu.getShippingServices() != null && !scu.getShippingServices().isEmpty() && selectedMethod != null) {
                boolean methodExists = false;
                for (Map m : scu.getShippingServices())
                    if (selectedMethod.equals(m.get("id"))) {
                        methodExists = true;
                        break;
                    }

                if (methodExists) {
                    shopCart.setShippingMethod(scu.getShippingMethod());
                    shopCart.setShippingMethod(method);
                    jsonResp.put("result", "OK");
                    jsonResp.put("data", shopCart.getShippingMethod().getMethodName(getLocale().getLanguage()) + " " + formatActualCurrency(scu.getSubtotalShipping()));
                } else {
                    jsonResp.put("result", "ERROR");
                    jsonResp.put("error", "Selected method is not available");
                }

            } else if (scu.getShippingMethod() != null) {
                shopCart.setShippingMethod(scu.getShippingMethod());
                jsonResp.put("result", "OK");
                jsonResp.put("data", shopCart.getShippingMethod().getMethodName(getLocale().getLanguage()) + " " + formatActualCurrency(scu.getSubtotalShipping()));
            } else {
                jsonResp.put("result", "ERROR");
                jsonResp.put("error", "There is not selected method for shipping");
            }

            // seguros
            shopCart.setInsurance(null);
            if (shopCart.getPickInStore() == null && idInsurance != null) {
                Insurance insurance = (Insurance) dao.get(Insurance.class, idInsurance);
                if (insurance != null) {
                    shopCart.setInsurance(idInsurance);
                    StringBuffer buffer = new StringBuffer();
                    if (jsonResp.containsKey("data")) buffer.append(jsonResp.get("data"));
                    if (StringUtils.isNotEmpty(buffer.toString())) buffer.append(" ,  ");
                    buffer.append(getText("subtotal.shipping.insurance")).append(": ").append(formatActualCurrency(insurance.getInsuranceValue()));
                    jsonResp.put("data", buffer.toString());
                }
            }
            dao.save(shopCart);
        } else {
            jsonResp.put("result", "ERROR");
        }
        return SUCCESS;
    }

    @Secured
    public String checkoutGetPayment() throws Exception {
        if (shopCart != null) {
            ShopCartUtils scu = getShopCartUtils(true);
            addToStack("scu", scu);
            addToStack("methods", getPaymentMethods(false, getFrontUserLevel()));
        }
        EventUtils.executeEvent(getServletContext(), EventService.EVENT_ONEPAGE_CHECKOUT, this);
        return SUCCESS;
    }

    @Secured
    public String checkoutSetPayment() throws Exception {
        jsonResp = new HashMap<String, Serializable>();
        if (shopCart != null) {
            if (StringUtils.isNotEmpty(paymentService)) {
                MerchantUtils mu = new MerchantUtils(getServletContext());
                MerchantService ms = mu.getService(paymentService, this);
                shopCart.setInterestPercent( ms.getInterestPercent(this) );
            }
            shopCart.setUseRewards("Y".equalsIgnoreCase(useRewardPoints));
            dao.save(shopCart);
            jsonResp.put("result", "OK");
            jsonResp.put("data", ("Y".equalsIgnoreCase(useRewardPoints)) ? getText("using.reward.points", "Using reward points") : "");
        } else {
            jsonResp.put("result", "ERROR");
        }
        return SUCCESS;
    }

    @Secured
    public String checkoutGetConfirm() throws Exception {
        EventUtils.executeEvent(getServletContext(), EventService.EVENT_ONEPAGE_CHECKOUT, this);
        if (shopCart != null) {
            ShopCartUtils scu = getShopCartUtils(true);
            addToStack("scu", scu);
        }
        return SUCCESS;
    }

    @Secured
    public String checkoutSaveOrder() throws Exception {
        EventUtils.executeEvent(getServletContext(), EventService.EVENT_PAYMENT_PRE_SAVE, this);
        if (shopCart != null && getFrontUser() != null) {
            ShopCartUtils scu = getShopCartUtils(true);
            String error = scu.getPendingForPay();
            if (StringUtils.isNotEmpty(error)) {
                addActionError(error);
                return ERROR;
            }

            // Save Order

            order = new Order();
            order.setUser(getFrontUser());
            order.setAffiliate(getAffiliateUser());
            order.setAffiliateCode(getAffiliateCode());
            order.setBillingAddress(shopCart.getBillingAddress());
            if (scu.getInsurance() != null) order.setTotalInsurance(scu.getInsurance().getInsuranceValue());
            order.setCodeMerchant(null);
            order.setCurrency(getActualCurrency());
            order.setDeliveryAddress(shopCart.getDeliveryAddress());
            if (shopCart.getPickInStore() != null) {
                order.setPickInStore(shopCart.getPickInStore());
            } else {
                order.setShippingMethod(shopCart.getShippingMethod());
                if (shopCart.getShippingMethod() != null) order.setCodeCarrier(shopCart.getShippingMethod().getCarrierName());
            }
            order.setPaymentMethod(null);
            order.setRemoteIp(getRequest().getRemoteAddr());
            order.setStatus(dao.getOrderStatus(OrderStatus.STATUS_DEFAULT, true));
            order.setCustomMessage(getCustomMessage());
            order.setCustomReference(getCustomReference());

            order.setTotalProducts(scu.getSubtotalProduct());
            order.setTotalFees(scu.getSubtotalFees());
            order.setTotalShipping(scu.getSubtotalShipping());

            order.setInterestPercent(scu.getPercentInterest());
            order.setTotalInterest(scu.getSubtotalInterest());

            // Taxes
            if (scu.getTaxes() != null && !scu.getTaxes().isEmpty()) {
                double totalTax = 0.0d;
                for (Map<String, Object> map : scu.getTaxes()) {
                    order.addTax(map);
                    Double taxValue = (Double) map.get("value");
                    if (taxValue != null) totalTax += taxValue;
                }
                order.setTotalTax(totalTax);
            }

            // Promotions
            if (scu.getPromotions() != null && !scu.getPromotions().isEmpty()) {
                for (Map<String, Object> map : scu.getPromotions()) order.addPromotion(map);
                order.setTotalDiscountPromotion(scu.getSubtotalPromotions());
            }

            // Rewards points
            if (scu.getRewardsUsedPoints() > 0) {
                order.setTotalRewards(scu.getRewardsToUse());
            }

            // Components
            order.setTotalComponents(shopCart.getTotalComplements());

            dao.save(order);

            // Productos
            order.setOrderDetails(new ArrayList<OrderDetail>());
            for (ShopCartItem item : shopCart.getItems()) {
                OrderDetail orderDetail = new OrderDetail();
                orderDetail.setOrder(order);
                orderDetail.setPrice(item.getPrice());
                orderDetail.setQuantity(item.getQuantity());
                orderDetail.setSeldate(item.getSelDate());
                orderDetail.setSeltime(item.getSelTime());

                if (item.getBeanComponent() != null) {
                    orderDetail.setComplementName(item.getBeanComponent().getProductName(getLocale().getLanguage()));
                    orderDetail.setComplementValue(item.getComplementPrice());
                }

                dao.save(orderDetail);

                orderDetail.setOrderDetailProducts(new ArrayList<OrderDetailProduct>());
                for (Product p : new Product[]{item.getBeanProd1(), item.getBeanProd2()}) {
                    if (p != null) {
                        OrderDetailProduct orderProd = new OrderDetailProduct();
                        ProductVariation var1 = (ProductVariation) dao.get(ProductVariation.class, item.getVariation1());
                        if (var1 != null) {
                            orderProd.setIdVariation(item.getVariation1());
                            orderProd.setCaractValue1(var1.getCaract1());
                            orderProd.setCaractValue2(var1.getCaract2());
                            orderProd.setCaractValue3(var1.getCaract3());
                            // Rebajar stock
                            var1.addStock(-item.getQuantity().longValue());
                            dao.save(var1);
                        }
                        if (Product.TYPE_DIGITAL.equalsIgnoreCase(p.getProductType()))
                            orderProd.setDownloads(dao.getMaxDownloads(p));
                        if (item.getFees() != null && item.getFees().size() > 0) {
                            CategoryFee fee = item.getFees().get(0);
                            orderProd.setFeeName(fee.getFee().getFeeName());
                            orderProd.setFeeValue(fee.getValue());
                        }
                        orderProd.setProduct(p);
                        orderProd.setCostPrice(p.getCostPrice());
                        orderProd.setOrderDetail(orderDetail);

                        //tax
                        double tax = 0.0;
                        ProductUserTax puTax = dao.getProductUserTaxes(p.getAltCategory(), getFrontUser().getAltCategory());
                        if (puTax != null && puTax.getTaxes() != null && !puTax.getTaxes().isEmpty()) {
                            for (TaxPerFamily tpf : puTax.getTaxes()) {
                                tax += tpf.getValue() * orderDetail.getPrice() * orderProd.getPercentOfPrice();
                                Map<String, Object> m = new HashMap<String, Object>();
                                m.put("value", tpf.getValue());
                                m.put("name", tpf.getTaxName());
                                m.put("code", tpf.getExternalCode());
                                orderProd.addTax(m);
                            }
                        }
                        orderProd.setTax(tax);

                        dao.save(orderProd);
                        orderDetail.getOrderDetailProducts().add(orderProd);
                        // Rebajar stock
                        p.addStock(-item.getQuantity());
                        p.addSales(item.getQuantity());
                        dao.save(p);
                    }
                }
                order.getOrderDetails().add(orderDetail);
            }

            // Free product
            for (Map<String, Object> map : order.getPromotions("product")) {
                Long idProd = (Long) map.get("data");
                Product p = (Product) dao.get(Product.class, idProd);
                if (p != null) {
                    OrderDetail orderDetail = new OrderDetail();
                    orderDetail.setOrder(order);
                    orderDetail.setPrice(0.0d);
                    orderDetail.setQuantity(1);
                    orderDetail.setPromotionCode((String) map.get("code"));
                    orderDetail.setPromotionName((String) map.get("name"));
                    dao.save(orderDetail);
                    OrderDetailProduct orderProd = new OrderDetailProduct();
                    if (Product.TYPE_DIGITAL.equalsIgnoreCase(p.getProductType()))
                        orderProd.setDownloads(dao.getMaxDownloads(p));
                    orderProd.setProduct(p);
                    orderProd.setCostPrice(p.getCostPrice());
                    orderProd.setOrderDetail(orderDetail);
                    orderProd.setTax(0.0);
                    dao.save(orderProd);
                    p.addStock(-1);
                    dao.save(p);
                }
            }

            OrderHistory history = new OrderHistory();
            history.setHistoryComment("");
            history.setHistoryDate(SomeUtils.today());
            history.setHistoryStatus(order.getStatus());
            history.setOrder(order);
            history.setUser(getFrontUser());
            dao.save(history);

            // descontar del usuario
            if (scu.getRewardsUsedPoints() > 0) {
                getFrontUser().setRewardPoints((long) scu.getRewardsRestPoints());
                dao.save(getFrontUser());
                addRewardHistory(getFrontUser(), -((Number)scu.getRewardsUsedPoints()).doubleValue(), order, null, UserRewardHistory.TYPE_PURCHASE, "");
            }

            //Salvar carro de compras
            shopCart.setStatus(ShopCart.STATUS_FINISHED);
            dao.save(shopCart);
            if (shopCart.equals(getUserSession().getShoppingCart())) getUserSession().resetAll();

            dao.flushSession();

            EventUtils.executeEvent(getServletContext(), EventService.EVENT_PAYMENT_POST_SAVE, this);

            // Pay Order if we select a payment service
            if (StringUtils.isEmpty(paymentService) && getStoreSessionObjects().containsKey(StoreSessionInterceptor.CNT_EXTERNAL_PAYMENT) && StringUtils.isNotEmpty((String) getStoreSessionObjects().get(StoreSessionInterceptor.CNT_EXTERNAL_PAYMENT))) {
                paymentService = (String) getStoreSessionObjects().get(StoreSessionInterceptor.CNT_EXTERNAL_PAYMENT);
            }
            if (StringUtils.isNotEmpty(paymentService)) {
                return checkoutPayOrder();
            }

        }
        return SUCCESS;
    }

    public String checkoutPayOrder() {
        OrderStatus defaultStatus = dao.getOrderStatus(OrderStatus.STATUS_DEFAULT, true);
        if (order != null && defaultStatus.equals(order.getStatus()) && StringUtils.isNotEmpty(paymentService)) {
            MerchantUtils mu = new MerchantUtils(getServletContext());
            MerchantService ms = mu.getService(paymentService, this);

            if (MerchantService.TYPE_HOSTED_PAGE.equalsIgnoreCase(ms.getType())) {
                Map result = ms.preparePaymentRedirection(order, this);
                if (result != null) {
                    if (result.containsKey("__error"))
                    {
                        addActionError(result.get("__error").toString());
                        return "error";
                    }
                    addToStack("dataMap", result);
                    return "hosted-page";
                }
            } else {
                if (!ms.validatePayment(order, this)) {
                    addActionError(getText("payment.method.not.available.for.order", "This payment methods is not available for this order"));
                    return "error";
                }

                PaymentResult result = ms.doPayment(order, this);
                Map<String, Object> map = new HashMap<String, Object>();
                map.put("order", order);
                if (result != null && result.isApproved()) {
                    order.setStatus(dao.getOrderStatus(OrderStatus.STATUS_APPROVED, true));
                    order.setPaymentMethod(paymentService);
                    order.setCodeMerchant(result.getTransactionId());
                    order.setPaymentCard(result.getCardType());
                    dao.save(order);
                    addOrderHistory(order, getFrontUser(), "");
                    if (order.getStatus().getSendEmail()) sendOrderStatusMail(order);
                    setBlockCode(StaticText.BLOCK_ORDER_APPROVED);
                    EventUtils.executeEvent(getServletContext(), EventService.EVENT_APPROVE_ORDER, this, map);
                } else if (result != null && result.isRejected()) {
                    order.setStatus(dao.getOrderStatus(OrderStatus.STATUS_REJECTED, true));
                    order.setPaymentMethod(paymentService);
                    order.setCodeMerchant(result.getTransactionId());
                    order.setPaymentCard(result.getCardType());
                    dao.save(order);
                    addOrderHistory(order, getFrontUser(), result.getTransactionError());
                    if (order.getStatus().getSendEmail()) sendOrderStatusMail(order);
                    // Recuperar stock reservado
                    recoverOrderStock(order);
                    recoverOrderReward(order);
                    setBlockCode(StaticText.BLOCK_ORDER_REJECTED);
                    EventUtils.executeEvent(getServletContext(), EventService.EVENT_DENY_ORDER, this, map);
                } else if (result != null && result.isPending()) {
                    order.setStatus(dao.getOrderStatus(OrderStatus.STATUS_PAYMENT_VALIDATION, true));
                    order.setPaymentMethod(paymentService);
                    order.setCodeMerchant(result.getTransactionId());
                    order.setPaymentCard(result.getCardType());
                    dao.save(order);
                    addOrderHistory(order, getFrontUser(), "Pending payment validation");
                    if (order.getStatus().getSendEmail()) sendOrderStatusMail(order);
                    setBlockCode(StaticText.BLOCK_ORDER_PENDING);
                } else {
                    order.setStatus(dao.getOrderStatus(OrderStatus.STATUS_DEFAULT, true));
                    order.setPaymentMethod(paymentService);
                    if (result != null) {
                        order.setCodeMerchant(result.getTransactionId());
                        order.setPaymentCard(result.getCardType());
                    }
                    dao.save(order);
                    addOrderHistory(order, getFrontUser(), "");
                    if (order.getStatus().getSendEmail()) sendOrderStatusMail(order);
                    setBlockCode(StaticText.BLOCK_ORDER_PENDING);
                    EventUtils.executeEvent(getServletContext(), EventService.EVENT_SAVE_ORDER, this, map);
                }
                getStoreSessionObjects().remove(StoreSessionInterceptor.CNT_EXTERNAL_PAYMENT);
                dao.flushSession();
            }
            return "payment-ok";
        } else {
            addActionError(getText(CNT_PAYMENT_INVALID_ORDER_STATUS, CNT_DEFAULT_PAYMENT_INVALID_ORDER_STATUS));
            return "error";
        }
    }


    /**
     * GLOBALES *
     */
    private ShopCartUtils getShopCartUtils(boolean getLiveMethods) {
        if (shopCart != null) {
            // Validar los codigos promocionales
            List<PromotionalCode> promotionalCodes = null;
            if (shopCart.getPromotionalCodes() != null && shopCart.getPromotionalCodes().length > 0) {
                promotionalCodes = new ArrayList<PromotionalCode>();
                for (String code : shopCart.getPromotionalCodes()) {
                    PromotionalCode promoCode = dao.getPromotionalCode(code);
                    if (promoCode != null) {
                        if (promoCode.isValid(getFrontUser()) && promoCode.isValid(shopCart)) {
                            promotionalCodes.add(promoCode);
                        } else {
                            addActionError(getText(CNT_ERROR_CANNOT_APPLY_PROMOTION, CNT_DEFAULT_ERROR_CANNOT_APPLY_PROMOTION, new String[]{code}));
                        }
                    } else {
                        addActionError(getText(CNT_ERROR_INVALID_PROMOTIONAL_CODE, CNT_DEFAULT_ERROR_INVALID_PROMOTIONAL_CODE, new String[]{code}));
                    }
                }
            }
            return new ShopCartUtils(this, shopCart, promotionalCodes, getLiveMethods);
        }
        return null;
    }


    /**
     * VARIABLES   *
     */

    protected Map<String, Serializable> jsonResp;

    private Order order;
    private ShopCart shopCart;
    private Long idShopCart;
    private Long idAddress;
    private Long idLocation;
    private Long billingState;
    private Long shippingState;
    private UserAddress billingAdd;
    private UserAddress shippingAdd;
    private Long selectedMethod;
    private Long idInsurance;
    private String customMessage;
    private String customReference;
    private String paymentService;
    private String useRewardPoints;
    private String blockCode;

    private Long[] itemId;
    private Long[] complementId;
    private String[] complementMessage;

    public Map<String, Serializable> getJsonResp() {
        return jsonResp;
    }

    public void setJsonResp(Map<String, Serializable> jsonResp) {
        this.jsonResp = jsonResp;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public ShopCart getShopCart() {
        return shopCart;
    }

    public void setShopCart(ShopCart shopCart) {
        this.shopCart = shopCart;
    }

    public Long getIdShopCart() {
        return idShopCart;
    }

    public void setIdShopCart(Long idShopCart) {
        this.idShopCart = idShopCart;
    }

    public Long getIdAddress() {
        return idAddress;
    }

    public void setIdAddress(Long idAddress) {
        this.idAddress = idAddress;
    }

    public Long getIdLocation() {
        return idLocation;
    }

    public void setIdLocation(Long idLocation) {
        this.idLocation = idLocation;
    }

    public Long getBillingState() {
        return billingState;
    }

    public void setBillingState(Long billingState) {
        this.billingState = billingState;
    }

    public Long getShippingState() {
        return shippingState;
    }

    public void setShippingState(Long shippingState) {
        this.shippingState = shippingState;
    }

    public UserAddress getBillingAdd() {
        return billingAdd;
    }

    public void setBillingAdd(UserAddress billingAdd) {
        this.billingAdd = billingAdd;
    }

    public UserAddress getShippingAdd() {
        return shippingAdd;
    }

    public void setShippingAdd(UserAddress shippingAdd) {
        this.shippingAdd = shippingAdd;
    }

    public Long getSelectedMethod() {
        return selectedMethod;
    }

    public void setSelectedMethod(Long selectedMethod) {
        this.selectedMethod = selectedMethod;
    }

    public Long getIdInsurance() {
        return idInsurance;
    }

    public void setIdInsurance(Long idInsurance) {
        this.idInsurance = idInsurance;
    }

    public String getCustomMessage() {
        return customMessage;
    }

    public void setCustomMessage(String customMessage) {
        this.customMessage = customMessage;
    }

    public String getCustomReference() {
        return customReference;
    }

    public void setCustomReference(String customReference) {
        this.customReference = customReference;
    }

    public String getPaymentService() {
        return paymentService;
    }

    public void setPaymentService(String paymentService) {
        this.paymentService = paymentService;
    }

    public String getUseRewardPoints() {
        return useRewardPoints;
    }

    public void setUseRewardPoints(String useRewardPoints) {
        this.useRewardPoints = useRewardPoints;
    }

    public String getBlockCode() {
        return blockCode;
    }

    public void setBlockCode(String blockCode) {
        this.blockCode = blockCode;
    }

    public Long[] getItemId() {
        return itemId;
    }

    public void setItemId(Long[] itemId) {
        this.itemId = itemId;
    }

    public Long[] getComplementId() {
        return complementId;
    }

    public void setComplementId(Long[] complementId) {
        this.complementId = complementId;
    }

    public String[] getComplementMessage() {
        return complementMessage;
    }

    public void setComplementMessage(String[] complementMessage) {
        this.complementMessage = complementMessage;
    }
}
