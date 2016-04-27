package org.store.core.admin;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.store.core.beans.*;
import org.store.core.beans.mail.MOrder;
import org.store.core.beans.mail.MUser;
import org.store.core.beans.utils.BreadCrumb;
import org.store.core.beans.utils.DataNavigator;
import org.store.core.globals.FeesUtils;
import org.store.core.globals.ShopCartUtils;
import org.store.core.globals.SomeUtils;
import org.store.core.globals.StoreMessages;
import org.store.core.mail.MailSenderThreat;
import org.store.core.utils.merchants.MerchantService;
import org.store.core.utils.merchants.MerchantUtils;
import org.store.core.utils.merchants.PaymentResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Namespace(value = "/admin")
@ParentPackage(value = "store-admin")
public class SalesAction extends AdminModuleAction implements StoreMessages {

    private static final String SALE_ORDER = "SALE_ORDER";
    private static final String ACTION_SET_USER = "set_user";
    private static final String ACTION_SET_ADDRESS = "set_address";
    private static final String ACTION_ADD_PRODUCT = "add_product";
    private static final String ACTION_DEL_PRODUCT = "del_product";
    private static final String ACTION_GENERATE_ORDER = "generate_order";
    private static final String ACTION_GENERATE_ORDER_FOR_USER = "generate_order_for_user";

    @Override
    public void prepare() throws Exception {
        order = (Order) dao.get(Order.class, idOrder);
        shopCart = (ShopCart) dao.get(ShopCart.class, idCart);
        user = (User) dao.get(User.class, idUser);
        billing = (UserAddress) dao.get(UserAddress.class, idBilling);
        shipping = (UserAddress) dao.get(UserAddress.class, idShipping);
    }

    @Action(value = "salescarts", results = @Result(type = "velocity", location = "/WEB-INF/views/admin/sales_cartlist.vm"))
    public String shopCartList() throws Exception {
        addToStack("carts", dao.getShopCartsPreparing(getAdminUser().getIdUser()));
        getBreadCrumbs().add(new BreadCrumb(null, getText("admin.preparing.order.list"), null, null));
        return SUCCESS;
    }

    @Action(value = "salescartedit", results = @Result(type = "velocity", location = "/WEB-INF/views/admin/sales_cartedit.vm"))
    public String shopCartEdit() throws Exception {
        boolean canOrder = true;
        validateShopCart();
        initializeItems();
        if (shopCart != null && shopCart.getUser() != null) {
            List<PromotionalCode> promotionalCodes = getPromotionalCodes(shopCart);
            addToStack("promotionalCodes", promotionalCodes);
            if (shopCart.getBillingAddress() == null) shopCart.setBillingAddress(shopCart.getUser().getBillingAddress());
            if (shopCart.getDeliveryAddress() == null) shopCart.setDeliveryAddress(shopCart.getUser().getShippingAddress());
            ShopCartUtils priceMap = new ShopCartUtils(this, shopCart, promotionalCodes, true);
            addToStack("priceMap", priceMap);
            addToStack("insurances", dao.getInsurancesFor(shopCart.getTotal() + shopCart.getTotalFees()));

            // validar si se puede generar la orden
            if (shopCart.getBillingAddress() == null) {
                addActionMessage(getText("admin.order.select.billing.address", "Select a billing address"));
                canOrder = false;
            } else if (priceMap.isShippingNeeded() && shopCart.getDeliveryAddress() == null) {
                addActionMessage(getText("admin.order.select.shipping.address", "Select a shipping address"));
                canOrder = false;
            } else if (shopCart.getItems() == null || shopCart.getItems().isEmpty()) {
                addActionMessage(getText("admin.order.add.products", "Add some products to the order"));
                canOrder = false;
            } else if (priceMap.isShippingNeedSelection() && priceMap.getShippingMethod() == null) {
                addActionMessage(getText("admin.order.select.shipping.method", "Select a shipping method"));
                canOrder = false;
            }

        } else canOrder = false;

        // metodos de pago
        List<Map<String, Object>> metodosPago = new ArrayList<Map<String, Object>>();
        MerchantUtils mu = new MerchantUtils(getServletContext());
        Map<String, Class> map = mu.getMerchants();
        if (map != null && !map.isEmpty()) {
            int index = 1;
            for (String serviceName : map.keySet()) {
                MerchantService service = mu.getService(serviceName, this);
                if (service != null && service.isActive() && (getAdminUser().getLevel() == null || getAdminUser().getLevel().canPayWith(serviceName))) {
                    if (!MerchantService.TYPE_EXTERNAL.equalsIgnoreCase(service.getType()) && !MerchantService.TYPE_HOSTED_PAGE.equalsIgnoreCase(service.getType())) {
                        Map<String, Object> m = new HashMap<String, Object>();
                        m.put("id", index++);
                        m.put("name", service.getCode());
                        m.put("label", service.getLabel());
                        String formData = service.getForm(this);
                        if (StringUtils.isNotEmpty(formData)) m.put("form", formData);
                        metodosPago.add(m);
                    }
                }
            }
        }
        addToStack("metodosPago", metodosPago);
        addToStack("canOrder", canOrder);
        getBreadCrumbs().add(new BreadCrumb(null, getText("admin.preparing.order.list"), url("salesorders", "/admin"), null));
        getBreadCrumbs().add(new BreadCrumb(null, getText("admin.preparing.order"), null, null));
        return SUCCESS;
    }

    @Action(value = "salescartdel", results = @Result(type = "redirectAction", location = "salesorders"))
    public String shopCartDel() throws Exception {
        validateShopCart();
        if (shopCart != null) {
            dao.delete(shopCart);
        }
        return SUCCESS;
    }

    @Action(value = "salescartsave", results = {
            @Result(type = "redirectAction", location = "salescartedit?idCart=${shopCart.id}"),
            @Result(type = "redirectAction", name = "orderedit", location = "orderdata?idOrder=${order.idOrder}"),
            @Result(type = "velocity", name = "input", location = "/WEB-INF/views/admin/sales_cartedit.vm")
    })
    public String shopCartSave() throws Exception {
        validateShopCart();
        if (ACTION_SET_USER.equalsIgnoreCase(action) && user != null)
            cartAddUser();
        else if (ACTION_SET_ADDRESS.equalsIgnoreCase(action) && shopCart != null && shopCart.getUser() != null)
            cartSetAddress();
        else if (ACTION_ADD_PRODUCT.equalsIgnoreCase(action) && idProduct != null)
            cartAddProduct();
        else if (ACTION_DEL_PRODUCT.equalsIgnoreCase(action) && idProduct != null)
            cartDelProduct();
        else if (shopCart != null) {
            cartSave();
            if (ACTION_GENERATE_ORDER.equalsIgnoreCase(action) || ACTION_GENERATE_ORDER_FOR_USER.equalsIgnoreCase(action)) {
                return cartGenerateOrder();
            }
        }
        return SUCCESS;
    }

    private String cartGenerateOrder() {
        // validar si se puede generar la orden
        boolean canOrder = true;
        initializeItems();
        ShopCartUtils priceMap = new ShopCartUtils(this, shopCart, getPromotionalCodes(shopCart), true);
        if (shopCart.getBillingAddress() == null) {
            addActionMessage(getText("admin.order.select.billing.address", "Select a billing address"));
            canOrder = false;
        } else if (priceMap.isShippingNeeded() && shopCart.getDeliveryAddress() == null) {
            addActionMessage(getText("admin.order.select.shipping.address", "Select a shipping address"));
            canOrder = false;
        } else if (shopCart.getItems() == null || shopCart.getItems().isEmpty()) {
            addActionMessage(getText("admin.order.add.products", "Add some products to the order"));
            canOrder = false;
        } else if (priceMap.isShippingNeedSelection() && priceMap.getShippingMethod() == null) {
            addActionMessage(getText("admin.order.select.shipping.method", "Select a shipping method"));
            canOrder = false;
        }
        if (canOrder) {

            order = new Order();
            order.setUser(shopCart.getUser());
            order.setAffiliate(getAffiliateUser());
            order.setAffiliateCode(getAffiliateCode());
            order.setBillingAddress(shopCart.getBillingAddress());
            order.setCodeMerchant(null);
            order.setCurrency(getActualCurrency());
            order.setDeliveryAddress(shopCart.getDeliveryAddress());
            order.setPickInStore(shopCart.getPickInStore());
            order.setShippingMethod(priceMap.getShippingMethod());
            order.setCodeCarrier(priceMap.getShippingMethod() != null ? priceMap.getShippingMethod().getCarrierName() : null);
            order.setPaymentMethod(null);
            order.setRemoteIp(getRequest().getRemoteAddr());
            order.setStatus(dao.getOrderStatus(OrderStatus.STATUS_DEFAULT, true));
            order.setIdAdminUser(shopCart.getAdminUser());

            order.setTotalProducts(priceMap.getSubtotalProduct());
            order.setTotalFees(priceMap.getSubtotalFees());
            order.setTotalShipping(priceMap.getSubtotalShipping());
            order.setTotal(priceMap.getTotal());

            // Insurance
            Insurance insurance = (Insurance) dao.get(Insurance.class, shopCart.getInsurance());
            order.setTotalInsurance(insurance != null ? insurance.getInsuranceValue() : null);

            // Taxes
            if (priceMap.getTaxes() != null && !priceMap.getTaxes().isEmpty()) {
                double totalTax = 0.0d;
                for (Map<String, Object> map : priceMap.getTaxes()) {
                    order.addTax(map);
                    Double taxValue = (Double) map.get("value");
                    if (taxValue != null) totalTax += taxValue;
                }
                order.setTotalTax(totalTax);
            }

            // Promotions
            if (priceMap.getPromotions() != null && !priceMap.getPromotions().isEmpty()) {
                double totalPromoDiscount = 0.0d;
                for (Map<String, Object> map : priceMap.getPromotions()) {
                    order.addPromotion(map);
                    if ("discount".equals(map.get("type")) || "discount-percent".equals(map.get("type"))) {
                        Double disc = (Double) map.get("value");
                        totalPromoDiscount += disc;
                    }
                }
                order.setTotalDiscountPromotion(totalPromoDiscount);
            }

            // Rewards points
            order.setTotalRewards(priceMap.getRewardsToUse());
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
                orderDetail.setOrderDetailProducts(new ArrayList<OrderDetailProduct>());
                dao.save(orderDetail);

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
                        ProductUserTax puTax = dao.getProductUserTaxes(p.getAltCategory(), shopCart.getUser().getAltCategory());
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

            addOrderHistory(order, shopCart.getUser(), "");

            // descontar del usuario
            if (priceMap.getRewardsUsedPoints() > 0) {
                shopCart.getUser().setRewardPoints((long) priceMap.getRewardsRestPoints());
                dao.save(shopCart.getUser());
            }

            shopCart.setStatus(ShopCart.STATUS_FINISHED);
            dao.save(shopCart);

            if (ACTION_GENERATE_ORDER.equalsIgnoreCase(action) && StringUtils.isNotEmpty(paymentService)) {

                MerchantUtils mu = new MerchantUtils(getServletContext());
                MerchantService ms = mu.getService(paymentService, this);

                if (!ms.validatePayment(order, this)) {
                    return SUCCESS;
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
                } else if (result != null && result.isRejected()) {
                    order.setStatus(dao.getOrderStatus(OrderStatus.STATUS_REJECTED, true));
                    order.setPaymentMethod(paymentService);
                    order.setCodeMerchant(result.getTransactionId());
                    order.setPaymentCard(result.getCardType());
                    dao.save(order);
                    addOrderHistory(order, getFrontUser(), "");
                    if (order.getStatus().getSendEmail()) sendOrderStatusMail(order);
                    // Recuperar stock reservado
                    recoverOrderStock(order);
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
                }

            } else if (ACTION_GENERATE_ORDER_FOR_USER.equalsIgnoreCase(action)) {
                // send to user
                if (StringUtils.isNotEmpty(order.getUser().getEmail())) {
                    try {
                        Map data = new HashMap();
                        data.put("order", new MOrder(order, this));
                        data.put("user", new MUser(order.getUser(), this));
                        String body = proccessVelocityTemplate(Mail.MAIL_TEMPLATE_ORDER_READY_TO_PAY, data);
                        Mail mail = new Mail();
                        mail.setInventaryCode(getStoreCode());
                        mail.setBody(body);
                        mail.setSubject(getText(CNT_SUBJECT_ORDER_READY_TO_PAY, CNT_DEFAULT_SUBJECT_ORDER_READY_TO_PAY));
                        mail.setToAddress(order.getUser().getEmail());
                        mail.setPriority(10);
                        mail.setReference("ORDER READY TO PAY " + order.getIdOrder());
                        dao.save(mail);
                        MailSenderThreat.asyncSendMail(mail, this);
                    } catch (Exception e) {
                        log.error(e.getMessage());
                    }
                }
            }


            dao.flushSession();

            return "orderedit";
        }
        return SUCCESS;
    }

    private void cartAddUser() {
        if (shopCart == null) {
            shopCart = new ShopCart();
            shopCart.setStatus(ShopCart.STATUS_PREPARING);
            shopCart.setAdminUser(getAdminUser().getIdUser());
        }
        if (!user.equals(shopCart.getUser())) {
            shopCart.setUser(user);
            shopCart.setBillingAddress(user.getBillingAddress());
            shopCart.setDeliveryAddress(user.getShippingAddress());
        }
        dao.save(shopCart);
    }

    private void cartSetAddress() {
        if (address != null) {
            address.setUser(shopCart.getUser());
            State state = (idState != null) ? (State) dao.get(State.class, idState) : null;
            if (state == null && !StringUtils.isEmpty(newState)) {
                state = new State();
                state.setCountryCode(address.getIdCountry());
                state.setStateCode(StringUtils.left(newState, 5));
                state.setStateName(newState);
                dao.save(state);
            }
            address.setState(state);
            address.setStateName(newStateName);
            dao.save(address);
            if ("billing".equalsIgnoreCase(addressType)) shopCart.setBillingAddress(address);
            else if ("shipping".equalsIgnoreCase(addressType)) shopCart.setDeliveryAddress(address);
            dao.save(shopCart);
        }
    }

    private void cartAddProduct() {
        Product p = (Product) dao.get(Product.class, idProduct);
        if (p != null) {
            ShopCartItem item = findItem(idProduct, null, null, null, null, null);
            if (item == null) {
                item = new ShopCartItem(shopCart);
                shopCart.addItem(item);
            }
            item.setProduct1(p.getIdProduct());
            item.setVariation1(null);
            item.setProduct2(null);
            item.setVariation2(null);
            item.setQuantity(quantity);
            item.setShopCart(shopCart);
            inicializeItem(item);
            dao.save(item);
        }
    }

    private void cartDelProduct() {
        ShopCartItem item = shopCart.getItemById(idProduct);
        if (item != null && shopCart.equals(item.getShopCart())) {
            shopCart.getItems().remove(item);
            dao.delete(item);
        }
    }

    private void cartSave() {
        if (billing != null && billing.getUser() != null && billing.getUser().equals(shopCart.getUser())) shopCart.setBillingAddress(billing);
        if (shipping != null && shipping.getUser() != null && shipping.getUser().equals(shopCart.getUser())) shopCart.setDeliveryAddress(shipping);

        // actualizar items
        if (idItem != null && idItem.length > 0) {
            for (int i = 0; i < idItem.length; i++) {
                ShopCartItem item = shopCart.getItemById(idItem[i]);
                if (item != null) {
                    Double p = (price != null && price.length > i) ? SomeUtils.strToDouble(price[i]) : null;
                    if (p != null) item.setPrice(p);
                    Integer s = (toBuy != null && toBuy.length > i) ? SomeUtils.strToInteger(toBuy[i]) : null;
                    if (s != null) item.setQuantity(s);
                    dao.save(item);
                }
            }
        }

        // actualizar promotional codes
        if (StringUtils.isNotEmpty(addPromoCode)) {
            PromotionalCode promo = dao.getPromotionalCode(addPromoCode);
            if (promo != null) {
                if (promo.isValid(shopCart) && promo.isValid(shopCart.getUser())) {
                    shopCart.addPromotionalCode(addPromoCode);
                } else {
                    addSessionFieldError("promotions", getText(CNT_ERROR_CANNOT_APPLY_PROMOTION, CNT_DEFAULT_ERROR_CANNOT_APPLY_PROMOTION, new String[]{addPromoCode}));
                }
            } else {
                addSessionFieldError("promotions", getText(CNT_ERROR_INVALID_PROMOTIONAL_CODE, CNT_DEFAULT_ERROR_INVALID_PROMOTIONAL_CODE, new String[]{addPromoCode}));
            }
        }

        // shipping
        ShippingMethod method = (ShippingMethod) dao.get(ShippingMethod.class, shippingMethod);
        shopCart.setShippingMethod(method);
        shopCart.setOverrideShipping(SomeUtils.strToDouble(overrideShipping));

        // insurances
        shopCart.setInsurance(getShippingInsurance());

        // rewards
        shopCart.setUseRewards(getUseRewards());

        if (StringUtils.isNotEmpty(delPromoCode)) {
            shopCart.delPromotionalCode(delPromoCode);
        }
        dao.save(shopCart);
    }

    @Action(value = "salescartsaveproducts", results = @Result(type = "velocity", location = "/WEB-INF/views/admin/sales_cartedit_products.vm"))
    public String shopCartSaveProducts() throws Exception {
        validateShopCart();
        if (shopCart != null && idProduct != null) {
            if (ACTION_ADD_PRODUCT.equalsIgnoreCase(action)) {
                Product p = (Product) dao.get(Product.class, idProduct);
                if (p != null) {
                    ShopCartItem item = findItem(idProduct, null, null, null, null, null);
                    if (item == null) {
                        item = new ShopCartItem(shopCart);
                        shopCart.addItem(item);
                    }
                    item.setProduct1(p.getIdProduct());
                    item.setVariation1(null);
                    item.setProduct2(null);
                    item.setVariation2(null);
                    item.setQuantity(quantity);
                    item.setShopCart(shopCart);
                    inicializeItem(item);
                    dao.save(item);
                }
            } else if (ACTION_DEL_PRODUCT.equalsIgnoreCase(action)) {
                ShopCartItem item = shopCart.getItemById(idProduct);
                if (item != null && shopCart.equals(item.getShopCart())) {
                    shopCart.getItems().remove(item);
                    dao.delete(item);
                }
            }
        }
        if (shopCart != null) {
            initializeItems();
            State deliveryState = (shopCart.getDeliveryAddress() != null) ? shopCart.getDeliveryAddress().getState() : null;
            new FeesUtils(shopCart, deliveryState, shopCart.getUser(), this);
        }
        return SUCCESS;
    }

    @Action(value = "salesorders", results = @Result(type = "velocity", location = "/WEB-INF/views/admin/sales_orderlist.vm"))
    public String orderList() throws Exception {
        addToStack("carts", dao.getShopCartsPreparing(getAdminUser().getIdUser()));

        DataNavigator orders = new DataNavigator(getRequest(), "orders");
        List<OrderStatus> arrStatus = null;
        if (filterStatus != null && filterStatus.length > 0) {
            arrStatus = new ArrayList<OrderStatus>();
            for (Long idStatus : filterStatus) {
                OrderStatus status = (OrderStatus) dao.get(OrderStatus.class, idStatus);
                if (status != null) arrStatus.add(status);
            }
        }
        orders.setListado(dao.getOrders(
                orders,
                SomeUtils.strToLong(filterCode),
                SomeUtils.strToDate(filterDateFrom, getDefaultLanguage()),
                SomeUtils.strToDate(filterDateTo, getDefaultLanguage()),
                arrStatus,
                filterUser,
                getAdminUser().getIdUser(),
                null,
                null)
        );
        addToStack("orders", orders);
        return SUCCESS;
    }

    public String orderEdit() throws Exception {

        return SUCCESS;
    }

    public String orderSave() throws Exception {


        return SUCCESS;
    }

    private void validateShopCart() {
        if (shopCart != null && getAdminUser() != null) {
            if (!getAdminUser().getIdUser().equals(shopCart.getAdminUser())) shopCart = null;
            else if (!ShopCart.STATUS_PREPARING.equalsIgnoreCase(shopCart.getStatus())) shopCart = null;
        }
    }

    private void initializeItems() {
        if (shopCart != null && shopCart.getItems() != null) {
            for (ShopCartItem it : shopCart.getItems())
                inicializeItem(it);
        }
    }

    private void inicializeItem(ShopCartItem item) {
        if (item != null) {
            boolean calculatePrice = (item.getPriceOriginal() == null);
            boolean set_price = (item.getPrice() == null);
            if (item.getProduct1() != null) {
                Product p1 = (Product) dao.get(Product.class, item.getProduct1());
                if (p1 != null) {
                    item.setCode1(p1.getPartNumber());
                    StringBuffer name1 = new StringBuffer();
                    name1.append(p1.getProductName(getLocale().getLanguage()));
                    ProductVariation v1 = (ProductVariation) dao.get(ProductVariation.class, item.getVariation1());
                    if (v1 != null) name1.append(" - ").append(v1.getFullOption());
                    item.setName1(name1.toString());
                    if (calculatePrice) {
                        item.setPriceOriginal(p1.getFinalPrice(shopCart.getUser().getLevel(), getNumProduct(item.getProduct1()), v1, dao));
                        if (set_price) item.setPrice(item.getPriceOriginal());
                    }
                    item.setStock(p1.getStock().doubleValue());
                }
                item.setBeanProd1(p1);
            }
            if (item.getProduct2() != null) {
                Product p2 = (Product) dao.get(Product.class, item.getProduct2());
                if (p2 != null) {
                    item.setCode2(p2.getPartNumber());
                    ProductLang l2 = p2.getLanguage(getLocale().getLanguage(), getDefaultLanguage());
                    StringBuffer name2 = new StringBuffer();
                    name2.append((l2 != null) ? l2.getProductName() : p2.getPartNumber());
                    ProductVariation v2 = (ProductVariation) dao.get(ProductVariation.class, item.getVariation2());
                    if (v2 != null) name2.append(" - ").append(v2.getFullOption());
                    item.setName2(name2.toString());
                }
                item.setBeanProd2(p2);
            }
            if (calculatePrice && item.getBeanProd1() != null && item.getBeanProd2() != null) {
                ProductRelated pr = dao.getProductRelated(item.getBeanProd1(), item.getBeanProd2());
                if (pr != null && pr.getCombinedPrice() != null && pr.getCombinedPrice() > 0) {
                    item.setPriceOriginal(pr.getCombinedPrice());
                    if (set_price) item.setPrice(item.getPriceOriginal());
                }
            }
        }
    }

    public int getNumProduct(Long id) {
        int res = 0;
        if (id != null && shopCart != null)
            for (ShopCartItem item : shopCart.getItems())
                if (id.equals(item.getProduct1())) res += item.getQuantity();
        return res;
    }

    public ShopCartItem findItem(Long idProduct1, Long variation1, Long idProduct2, Long variation2, String selDate, String selTime) {
        if (shopCart != null && !shopCart.getItems().isEmpty()) {
            for (ShopCartItem it : shopCart.getItems()) {
                boolean equals = new EqualsBuilder()
                        .append(idProduct1, it.getProduct1())
                        .append(idProduct2, it.getProduct2())
                        .append(variation1, it.getVariation1())
                        .append(variation2, it.getVariation2())
                        .append(selDate, it.getSelDate())
                        .append(selTime, it.getSelTime())
                        .isEquals();
                if (equals) return it;
            }
        }
        return null;
    }

    private List<PromotionalCode> getPromotionalCodes(ShopCart cart) {
        List<PromotionalCode> res = new ArrayList<PromotionalCode>();
        if (cart != null && cart.getPromotionalCodes() != null) {
            for (String code : cart.getPromotionalCodes()) {
                PromotionalCode p = dao.getPromotionalCode(code);
                if (p != null) {
                    if (p.isValid(cart) && p.isValid(cart.getUser())) res.add(p);
                    else {
                        cart.delPromotionalCode(code);
                        addFieldError("promotions", getText(CNT_ERROR_CANNOT_APPLY_PROMOTION, CNT_DEFAULT_ERROR_CANNOT_APPLY_PROMOTION, new String[]{code}));
                        dao.save(cart);
                    }
                } else {
                    cart.delPromotionalCode(code);
                    addFieldError("promotions", getText(CNT_ERROR_INVALID_PROMOTIONAL_CODE, CNT_DEFAULT_ERROR_INVALID_PROMOTIONAL_CODE, new String[]{code}));
                    dao.save(cart);
                }
            }
        }
        return res;
    }

    private Long idOrder;
    private Order order;
    private Long idCart;
    private ShopCart shopCart;
    private Long idUser;
    private User user;
    private Long idBilling;
    private Long idShipping;
    private UserAddress billing;
    private UserAddress shipping;
    private String filterCode;
    private Long[] filterStatus;
    private String filterDateFrom;
    private String filterDateTo;
    private String filterUser;
    private Long filterType;
    private String[] filterRmaStatus;
    private String action;
    private Long idProduct;
    private Integer quantity;

    private Long[] idItem;
    private String[] price;
    private String[] toBuy;

    private String addPromoCode;
    private String delPromoCode;

    private UserAddress address;
    private Long idState;
    private String newState;
    private String newStateName;
    private String addressType;
    private Long shippingInsurance;
    private Long shippingMethod;
    private Boolean useRewards;
    private String paymentService;
    private String overrideShipping;

    public String getOverrideShipping() {
        return overrideShipping;
    }

    public void setOverrideShipping(String overrideShipping) {
        this.overrideShipping = overrideShipping;
    }

    public String getPaymentService() {
        return paymentService;
    }

    public void setPaymentService(String paymentService) {
        this.paymentService = paymentService;
    }

    public Long getShippingMethod() {
        return shippingMethod;
    }

    public void setShippingMethod(Long shippingMethod) {
        this.shippingMethod = shippingMethod;
    }

    public String getAddPromoCode() {
        return addPromoCode;
    }

    public void setAddPromoCode(String addPromoCode) {
        this.addPromoCode = addPromoCode;
    }

    public String getDelPromoCode() {
        return delPromoCode;
    }

    public void setDelPromoCode(String delPromoCode) {
        this.delPromoCode = delPromoCode;
    }

    public Boolean getUseRewards() {
        return useRewards != null && useRewards;
    }

    public void setUseRewards(Boolean useRewards) {
        this.useRewards = useRewards;
    }

    public Long getShippingInsurance() {
        return shippingInsurance;
    }

    public void setShippingInsurance(Long shippingInsurance) {
        this.shippingInsurance = shippingInsurance;
    }

    public Long[] getIdItem() {
        return idItem;
    }

    public void setIdItem(Long[] idItem) {
        this.idItem = idItem;
    }

    public String[] getPrice() {
        return price;
    }

    public void setPrice(String[] price) {
        this.price = price;
    }

    public String[] getToBuy() {
        return toBuy;
    }

    public void setToBuy(String[] toBuy) {
        this.toBuy = toBuy;
    }

    public Long getIdProduct() {
        return idProduct;
    }

    public void setIdProduct(Long idProduct) {
        this.idProduct = idProduct;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public String getAddressType() {
        return addressType;
    }

    public void setAddressType(String addressType) {
        this.addressType = addressType;
    }

    public UserAddress getAddress() {
        return address;
    }

    public void setAddress(UserAddress address) {
        this.address = address;
    }

    public Long getIdState() {
        return idState;
    }

    public void setIdState(Long idState) {
        this.idState = idState;
    }

    public String getNewState() {return newState;}

    public void setNewState(String newState) {
        this.newState = newState;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public Long getIdOrder() {
        return idOrder;
    }

    public void setIdOrder(Long idOrder) {
        this.idOrder = idOrder;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public Long getIdCart() {
        return idCart;
    }

    public void setIdCart(Long idCart) {
        this.idCart = idCart;
    }

    public ShopCart getShopCart() {
        return shopCart;
    }

    public void setShopCart(ShopCart shopCart) {
        this.shopCart = shopCart;
    }

    public Long getIdUser() {
        return idUser;
    }

    public void setIdUser(Long idUser) {
        this.idUser = idUser;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Long getIdBilling() {
        return idBilling;
    }

    public void setIdBilling(Long idBilling) {
        this.idBilling = idBilling;
    }

    public Long getIdShipping() {
        return idShipping;
    }

    public void setIdShipping(Long idShipping) {
        this.idShipping = idShipping;
    }

    public UserAddress getBilling() {
        return billing;
    }

    public void setBilling(UserAddress billing) {
        this.billing = billing;
    }

    public UserAddress getShipping() {
        return shipping;
    }

    public void setShipping(UserAddress shipping) {
        this.shipping = shipping;
    }

    public String getFilterCode() {
        return filterCode;
    }

    public void setFilterCode(String filterCode) {
        this.filterCode = filterCode;
    }

    public Long[] getFilterStatus() {
        return filterStatus;
    }

    public void setFilterStatus(Long[] filterStatus) {
        this.filterStatus = filterStatus;
    }

    public String getFilterDateFrom() {
        return filterDateFrom;
    }

    public void setFilterDateFrom(String filterDateFrom) {
        this.filterDateFrom = filterDateFrom;
    }

    public String getFilterDateTo() {
        return filterDateTo;
    }

    public void setFilterDateTo(String filterDateTo) {
        this.filterDateTo = filterDateTo;
    }

    public String getFilterUser() {
        return filterUser;
    }

    public void setFilterUser(String filterUser) {
        this.filterUser = filterUser;
    }

    public Long getFilterType() {
        return filterType;
    }

    public void setFilterType(Long filterType) {
        this.filterType = filterType;
    }

    public String[] getFilterRmaStatus() {
        return filterRmaStatus;
    }

    public void setFilterRmaStatus(String[] filterRmaStatus) {
        this.filterRmaStatus = filterRmaStatus;
    }

    public String getNewStateName() {return newStateName;}

    public void setNewStateName(String newStateName) {this.newStateName = newStateName;}
}
