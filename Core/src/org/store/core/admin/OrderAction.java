package org.store.core.admin;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.store.core.beans.*;
import org.store.core.beans.mail.MAvailableLinks;
import org.store.core.beans.mail.MOrder;
import org.store.core.beans.mail.MUser;
import org.store.core.beans.utils.BreadCrumb;
import org.store.core.beans.utils.DataNavigator;
import org.store.core.globals.SomeUtils;
import org.store.core.globals.StoreMessages;
import org.store.core.mail.MailSenderThreat;
import org.store.core.utils.carriers.*;
import org.store.core.utils.events.EventService;
import org.store.core.utils.events.EventUtils;
import org.store.core.utils.merchants.MerchantService;
import org.store.core.utils.merchants.MerchantUtils;
import org.store.core.utils.merchants.PaymentResult;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.*;
import java.util.*;


@Namespace(value = "/admin")
@ParentPackage(value = "store-admin")
public class OrderAction extends AdminModuleAction implements StoreMessages {
    private static final String PATH_CARRIER_IMAGES = "/images/shipping/";
    private static final String PATH_INVOICE = "/invoices";
    private static final String PATH_PACK_SLIP = "/packing_slip";

    @Override
    public void prepare() throws Exception {
        order = (Order) dao.get(Order.class, idOrder);
        orderStatus = (OrderStatus) dao.get(OrderStatus.class, idStatus);
        orderPacking = (OrderPacking) dao.get(OrderPacking.class, idPacking);
        orderPayment = (OrderPayment) dao.get(OrderPayment.class, idPayment);
        rma = (Rma) dao.get(Rma.class, idRma);
    }

    @Action(value = "orderlist", results = @Result(type = "velocity", location = "/WEB-INF/views/admin/orderlist.vm"))
    public String orderList() throws Exception {
        DataNavigator orders = new DataNavigator(getRequest(), "orders");
        List<OrderStatus> arrStatus = null;
        if (filterStatus != null && filterStatus.length > 0) {
            arrStatus = new ArrayList<OrderStatus>();
            for (Long idStatus : filterStatus) {
                OrderStatus status = (OrderStatus) dao.get(OrderStatus.class, idStatus);
                if (status != null) arrStatus.add(status);
            }
        }
        orders.setListado(dao.getOrders(orders, SomeUtils.strToLong(filterCode), SomeUtils.strToDate(filterDateFrom, getDefaultLanguage()), SomeUtils.strToDate(filterDateTo, getDefaultLanguage()), arrStatus, filterUser, filterAdmin, null, null));
        addToStack("orders", orders);
        getBreadCrumbs().add(new BreadCrumb(null, getText("admin.order.list"), null, null));
        return SUCCESS;
    }

    @Action(value = "orderdata", results = {
            @Result(type = "velocity", location = "/WEB-INF/views/admin/orderdata.vm"),
            @Result(type = "velocity", name = "print", location = "/WEB-INF/views/admin/orderdata_print.vm")
    })
    public String orderData() throws Exception {
        if (order != null && order.getOrderDetails() != null) {
            // buscar pagos
            addToStack("payments", dao.getOrderPayments(order));

            // Generar mapa para shipping
            int totalPending = 0;
            List<Map<String, Object>> listToShip = new ArrayList<Map<String, Object>>();
            List<OrderDetailProduct> digitalProducts = new ArrayList<OrderDetailProduct>();
            for (OrderDetail detail : order.getOrderDetails()) {
                for (OrderDetailProduct odp : detail.getOrderDetailProducts()) {
                    if (odp.getProduct() != null && Product.TYPE_DIGITAL.equalsIgnoreCase(odp.getProduct().getProductType())) {
                        digitalProducts.add(odp);
                    }

                    if (odp.getProduct() != null && dao.productNeedShipping(odp.getProduct())) {
                        Map<String, Object> map = new HashMap<String, Object>();
                        int cant = dao.getPackedOrderDetailProducts(odp);
                        int pending = Math.max(0, detail.getQuantity() - cant);
                        int possibles = Math.min(pending, odp.getProduct().getStock().intValue());
                        map.put("delivered", cant);
                        map.put("total", detail.getQuantity());
                        map.put("pending", pending);
                        map.put("possibles", possibles);
                        map.put("stock", detail.getQuantity());
                        map.put("code", odp.getProduct().getPartNumber());
                        map.put("name", odp.getProduct().getProductName(getDefaultLanguage()));
                        if (StringUtils.isNotEmpty(odp.getCaractValue1())) {
                            map.put("caract1", odp.getCaractValue1());
                        }
                        if (StringUtils.isNotEmpty(odp.getCaractValue2())) {
                            map.put("caract2", odp.getCaractValue2());
                        }
                        if (StringUtils.isNotEmpty(odp.getCaractValue3())) {
                            map.put("caract3", odp.getCaractValue3());
                        }

                        // find weight if variant
                        Double weight = odp.getProduct().getWeight();
                        if (odp.getIdVariation()!=null) {
                            ProductVariation pv = (ProductVariation) dao.get(ProductVariation.class, odp.getIdVariation());
                            if (pv!=null && pv.getWeight()!=null) weight = pv.getWeight();
                        }
                        map.put("weight", weight);
                        map.put("id", odp.getId());
                        listToShip.add(map);
                        totalPending += pending;
                    }
                }
            }
            addToStack("productsToShipping", listToShip);
            addToStack("productsPending", totalPending);
            addToStack("digitalProducts", digitalProducts);

            //Generar mapa para purchase
            if (OrderStatus.STATUS_DEFAULT.equalsIgnoreCase(order.getStatus().getStatusType()) || OrderStatus.STATUS_APPROVED.equalsIgnoreCase(order.getStatus().getStatusType())) {
                Map<Long, Long> pendingStock = dao.getPurchaseForReceiveResume();
                Map<Provider, List<OrderDetailProduct>> purchases = new HashMap<Provider, List<OrderDetailProduct>>();
                for (OrderDetail detail : order.getOrderDetails()) {
                    for (OrderDetailProduct odp : detail.getOrderDetailProducts()) {
                        if (odp.getProduct() != null && odp.getProduct().getStock() < 0) {
                            Long requestedPending = (pendingStock != null && pendingStock.containsKey(odp.getProduct().getIdProduct())) ? pendingStock.get(odp.getProduct().getIdProduct()) : 0;
                            odp.addProperty("requestedPending", requestedPending);
                            if (odp.getProduct().getStock() < 0) {
                                odp.addProperty("toRequest", Math.max(0, -odp.getProduct().getStock() - requestedPending));
                            } else {
                                odp.addProperty("toRequest", 0);
                            }

                            List<ProductProvider> listPP = dao.getProductProviders(odp.getProduct());
                            for (ProductProvider pp : listPP) {
                                if (purchases.containsKey(pp.getProvider())) {
                                    List<OrderDetailProduct> l = purchases.get(pp.getProvider());
                                    l.add(odp);
                                } else {
                                    List<OrderDetailProduct> l = new ArrayList<OrderDetailProduct>();
                                    l.add(odp);
                                    purchases.put(pp.getProvider(), l);
                                }
                            }
                        }
                    }
                }
                addToStack("purchases", purchases);
            }
            addToStack("packages", dao.getOrderPackages(order));

        }
        getBreadCrumbs().add(new BreadCrumb(null, getText("admin.order.list"), url("orderlist", "/admin"), null));
        getBreadCrumbs().add(new BreadCrumb(null, getText("admin.order.data"), null, null));

        return ("print".equalsIgnoreCase(output)) ? "print" : SUCCESS;
    }

    @Action(value = "orderrequeststate", results = @Result(type = "redirectAction", location = "orderdata?idOrder=${order.idOrder}"))
    public String orderRequestState() throws Exception {
        if (getAdminUser() != null && order != null && StringUtils.isNotEmpty(order.getPaymentMethod()) && StringUtils.isNotEmpty(order.getCodeMerchant())) {

            MerchantUtils mu = new MerchantUtils(getServletContext());
            MerchantService service = mu.getService(order.getPaymentMethod(), this);
            if (service != null) {
                boolean updateOrder = OrderStatus.STATUS_DEFAULT.equalsIgnoreCase(order.getStatus().getStatusType());
                PaymentResult result = service.doRequestStatus(order, this);
                if (result != null && StringUtils.isNotEmpty(result.getTransactionResult())) {
                    if (result.isApproved() && updateOrder) {
                        order.setStatus(getDao().getOrderStatus(OrderStatus.STATUS_APPROVED, true));
                        getDao().save(order);
                        addOrderHistory(order, getFrontUser(), "");
                        if (order.getStatus().getSendEmail()) sendOrderStatusMail(order);
                        EventUtils.executeAdminEvent(getServletContext(), EventService.EVENT_ADMIN_CHANGE_ORDER_STATUS, this);
                    } else if (result.isRejected() && updateOrder) {
                        order.setStatus(getDao().getOrderStatus(OrderStatus.STATUS_REJECTED, true));
                        getDao().save(order);
                        addOrderHistory(order, getFrontUser(), "");
                        if (order.getStatus().getSendEmail()) sendOrderStatusMail(order);
                        // Recuperar stock reservado
                        recoverOrderStock(order);
                        if (order.getStatus().getSendEmail()) sendOrderStatusMail(order);
                        EventUtils.executeAdminEvent(getServletContext(), EventService.EVENT_ADMIN_CHANGE_ORDER_STATUS, this);
                    }
                    addSessionMessage(getText("order.request.status.result", "The order status is {0}", result.getTransactionResult()));
                } else {
                    addSessionMessage(getText("order.error.requesting.status", "Error requesting order status, try again later."));
                }
            } else {
                addSessionMessage(getText("order.error.requesting.status", "Error requesting order status, try again later."));
            }
        } else {
            addSessionMessage(getText("order.error.requesting.status", "Error requesting order status, try again later."));
        }
        return SUCCESS;
    }

    @Action(value = "orderupdatestate", results = @Result(type = "redirectAction", location = "orderdata?idOrder=${order.idOrder}"))
    public String orderUpdateState() throws Exception {
        if (getAdminUser() != null && order != null && orderStatus != null && !orderStatus.equals(order.getStatus())) {
            boolean isApproved = !OrderStatus.STATUS_APPROVED.equalsIgnoreCase(order.getStatus().getStatusType()) && OrderStatus.STATUS_APPROVED.equalsIgnoreCase(orderStatus.getStatusType());
            boolean isRejected = !OrderStatus.STATUS_REJECTED.equalsIgnoreCase(order.getStatus().getStatusType()) && OrderStatus.STATUS_REJECTED.equalsIgnoreCase(orderStatus.getStatusType());
            order.setStatus(orderStatus);
            addOrderHistory(order, getAdminUser(), statusComment);
            dao.save(order);
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("order", order);
            if (isApproved) map.put("new_status", OrderStatus.STATUS_APPROVED);
            else if (isRejected) map.put("new_status", OrderStatus.STATUS_REJECTED);
            EventUtils.executeAdminEvent(getServletContext(), EventService.EVENT_ADMIN_CHANGE_ORDER_STATUS, this, map);
            if (isRejected) {
                recoverOrderStock(order);
                recoverOrderReward(order);
            }
            if (orderStatus.getSendEmail()) sendOrderStatusMail(order);
        }
        return SUCCESS;
    }

    @Action(value = "orderupdateinvoice", results = @Result(type = "redirectAction", location = "orderdata?idOrder=${order.idOrder}"))
    public String orderUpdateInvoice() throws Exception {
        if (getAdminUser() != null && order != null) {
            if (StringUtils.isEmpty(order.getInvoiceNo()) && order.getInvoiceConsecutive() == null) {
                if ("Y".equalsIgnoreCase(getStoreProperty(StoreProperty.PROP_INVOICE_NUMBER_AUTOMATIC, "N"))) {
                    Long nextInvoice = dao.getLastInvoice() + 1;
                    Long startFrom = SomeUtils.strToLong(getStoreProperty(StoreProperty.PROP_INVOICE_START_FROM, null));
                    if (startFrom != null && startFrom > nextInvoice) nextInvoice = startFrom;
                    order.setInvoiceConsecutive(nextInvoice);
                    order.setInvoiceNo(null);
                } else {
                    order.setInvoiceNo(invoiceNo);
                    order.setInvoiceConsecutive(null);
                }
            }
            if (order.getInvoiceDate() == null && StringUtils.isEmpty(invoiceDate)) {
                order.setInvoiceDate(Calendar.getInstance().getTime());
            } else if (StringUtils.isNotEmpty(invoiceDate)) {
                order.setInvoiceDate(SomeUtils.strToDate(invoiceDate, getDefaultLanguage()));
            }
            order.setPurchaseOrder(purchaseOrder);
            dao.save(order);
        }
        return SUCCESS;
    }

    @Action(value = "orderaddpackage", results = @Result(type = "redirectAction", location = "orderdata?idOrder=${order.idOrder}&openTab=1"))
    public String orderAddPackage() throws Exception {

        if (order != null && packageProductDetail != null && packageProductDetail.length > 0 && packageHeight != null && packageHeight > 0 && packageLength != null && packageLength > 0 && packageWidth != null && packageWidth > 0 && packageWeight != null && packageWeight > 0) {
            boolean productsToAdd = false;
            for (int i = 0; i < packageProductDetail.length; i++) {
                Integer quantityToPackage = (packageProductQuantity != null && packageProductQuantity.length > i) ? packageProductQuantity[i] : null;
                if (quantityToPackage != null && quantityToPackage > 0) {
                    OrderDetailProduct odp = (OrderDetailProduct) dao.get(OrderDetailProduct.class, packageProductDetail[i]);
                    if (odp != null && order.equals(odp.getOrderDetail().getOrder())) {
                        productsToAdd = true;
                        break;
                    }
                }
            }

            if (productsToAdd) {
                OrderPacking op = new OrderPacking();
                op.setDimentionHeight(packageHeight);
                op.setDimentionWidth(packageWidth);
                op.setDimentionLength(packageLength);
                op.setWeight(packageWeight);
                op.setOrder(order);
                dao.save(op);

                for (int i = 0; i < packageProductDetail.length; i++) {
                    Integer quantityToPackage = (packageProductQuantity != null && packageProductQuantity.length > i) ? packageProductQuantity[i] : null;
                    if (quantityToPackage != null && quantityToPackage > 0) {
                        OrderDetailProduct odp = (OrderDetailProduct) dao.get(OrderDetailProduct.class, packageProductDetail[i]);
                        if (odp != null && order.equals(odp.getOrderDetail().getOrder())) {
                            for (int index = 0; index < quantityToPackage; index++) {
                                OrderPackingProduct opp = new OrderPackingProduct();
                                opp.setOrderDetailProduct(odp);
                                opp.setPacking(op);
                                dao.save(opp);
                            }
                        }
                    }
                }
            }

        }
        return SUCCESS;
    }

    @Action(value = "orderdelpackage", results = @Result(type = "redirectAction", location = "orderdata?idOrder=${order.idOrder}&openTab=1"))
    public String orderDelPackage() throws Exception {
        if (orderPacking != null && order != null && order.equals(orderPacking.getOrder())) {
            dao.delete(orderPacking);
        }
        return SUCCESS;
    }

    @Action(value = "ordersavepackage", results = @Result(type = "redirectAction", location = "orderdata?idOrder=${order.idOrder}&openTab=1"))
    public String orderSavePackage() throws Exception {
        if (orderPacking != null && order != null && order.equals(orderPacking.getOrder()) && packageCost != null) {
            ShippingMethod method = (ShippingMethod) dao.get(ShippingMethod.class, packageShippingMethod);
            Date date = SomeUtils.strToDate(packageDate, getDefaultLanguage());
            if (method != null && date != null && StringUtils.isNotEmpty(packageTracking)) {
                orderPacking.setDeliveryCost(packageCost);
                orderPacking.setDeliveryCurrency(getDefaultCurrency());
                orderPacking.setDeliveryDate(date);
                orderPacking.setShippingMethod(method);
                orderPacking.setTrackingNumber(packageTracking);
                orderPacking.setCustomTracking(packageTrackingText);
                orderPacking.setManualShipping(true);
                dao.save(orderPacking);
            }
        }
        return SUCCESS;
    }

    @Action(value = "orderdeliverypackages", results = @Result(type = "redirectAction", location = "orderdata?idOrder=${order.idOrder}&openTab=2"))
    public String orderDeliveryPackages() throws Exception {
        ShippingMethod shippingMethod = (ShippingMethod) dao.get(ShippingMethod.class, deliveryShippingMethod);
        if (order != null && shippingMethod != null) {
            List<OrderPacking> list = dao.getPendingOrderPackages(order);
            if (list != null && !list.isEmpty()) {
                List<BasePackage> listaPaquetes = new ArrayList<BasePackage>();
                for (OrderPacking p : list) {
                    BasePackage pck = new org.store.core.utils.carriers.BasePackage();
                    Double height = SomeUtils.strToDouble(getParentProperty(p, "dimentionHeight"));
                    Double width = SomeUtils.strToDouble(getParentProperty(p, "dimentionWidth"));
                    Double length = SomeUtils.strToDouble(getParentProperty(p, "dimentionLength"));
                    Double weight = SomeUtils.strToDouble(getParentProperty(p, "weight"));
                    if (height != null) pck.setDimensionsHeight(height.floatValue());
                    if (length != null) pck.setDimensionsLength(length.floatValue());
                    if (width != null) pck.setDimensionsWidth(width.floatValue());
                    if (weight != null) pck.setPackageWeight(weight.floatValue());
                    pck.setInsuredValueMonetaryValue(p.getMonetaryValue().floatValue());
                    pck.setProductId(p.getId());
                    pck.setCurrencyCode(getDefaultCurrency().getCode());
                    pck.setDimensionUnit(getDimensionUnit());
                    pck.setWeightUnit(getWeightUnit());
                    listaPaquetes.add(pck);
                }

                org.store.core.utils.carriers.Address address = new org.store.core.utils.carriers.Address(order.getDeliveryAddress().getAddress(), order.getDeliveryAddress().getAddress2(), order.getDeliveryAddress().getCity(), order.getDeliveryAddress().getState().getStateCode(), order.getDeliveryAddress().getZipCode(), order.getDeliveryAddress().getIdCountry());
                StructuredPhoneNumber phone = (order.getDeliveryAddress().getPhone() != null) ? new StructuredPhoneNumber(order.getDeliveryAddress().getPhone(), "-") : null;
                org.store.core.utils.carriers.ShipTo shipTo = new org.store.core.utils.carriers.ShipTo(order.getDeliveryAddress().getCompany(), order.getDeliveryAddress().getFullName(), address, phone);

                Properties carrierProps = dao.getCarrierProperties(shippingMethod.getCarrierName());
                if (carrierProps != null) {
                    CarrierUtils carrierUtils = new CarrierUtils(getServletContext());
                    CarrierService cs = carrierUtils.getCarrierService(shippingMethod.getCarrierName(), carrierProps);
                    if (cs != null && cs.available(shipTo, listaPaquetes)) {
                        ShippingResult shippingResult = cs.generateShipping(shipTo, listaPaquetes, shippingMethod.getMethodCode(), getServletContext().getRealPath("/stores/" + getStoreCode() + PATH_CARRIER_IMAGES));
                        if (!shippingResult.hasErrors()) {
                            order.setCodeCarrier(shippingResult.getShippingNumber());
                            if (shippingResult.getTrackingNumbers().size() > 0) {
                                Date now = Calendar.getInstance().getTime();
                                double costByPack = shippingResult.getShipmentCharge() / shippingResult.getTrackingNumbers().size();
                                for (Object obj : shippingResult.getTrackingNumbers().keySet()) {
                                    Long id = (Long) obj;
                                    String trackNumber = shippingResult.getTrackingNumbers().get(id);
                                    OrderPacking packing = (OrderPacking) dao.get(OrderPacking.class, id);
                                    if (packing != null) {
                                        packing.setDeliveryDate(now);
                                        packing.setTrackingNumber(trackNumber);
                                        packing.setShippingMethod(shippingMethod);
                                        if (StringUtils.isNotEmpty(shippingResult.getShipmentChargeCurr())) packing.setDeliveryCurrency(dao.getCurrency(shippingResult.getShipmentChargeCurr()));
                                        else packing.setDeliveryCurrency(getDefaultCurrency());
                                        packing.setDeliveryCost(costByPack);
                                        dao.save(packing);
                                        //todo: Email to user, with package information
                                    }
                                }
                            }
                        } else {
                            for (Map<String, String> errMap : shippingResult.getErrors()) {
                                StringBuffer buff = new StringBuffer();
                                if (errMap.containsKey("CARRIER") && StringUtils.isNotEmpty(errMap.get("CARRIER"))) buff.append(errMap.get("CARRIER")).append(": ");
                                if (errMap.containsKey("CODE") && StringUtils.isNotEmpty(errMap.get("CODE"))) buff.append("(").append(errMap.get("CODE")).append(") ");
                                if (errMap.containsKey("ERROR") && StringUtils.isNotEmpty(errMap.get("ERROR"))) buff.append(errMap.get("ERROR"));
                                if (StringUtils.isNotEmpty(buff.toString())) addSessionFieldError("delivery", buff.toString());
                            }
                        }
                    } else {
                        addSessionFieldError("delivery", shippingMethod.getCarrierName() + " is not available for the selected items or shipping address.");
                    }
                } else {
                    addSessionFieldError("delivery", shippingMethod.getCarrierName() + " has not connection properties defined.");
                }
            } else {
                addSessionFieldError("delivery", "There is no pending packages to delivery.");
            }
        }
        return SUCCESS;
    }

    @Action(value = "ordercosts", results = @Result(type = "redirectAction", location = "orderdata?idOrder=${order.idOrder}&openTab=3"))
    public String orderCosts() throws Exception {
        if (order != null) {
            // product cost
            if (productId != null && productCost != null && productId.length > 0) {
                for (int i = 0; i < productId.length; i++) {
                    OrderDetailProduct odp = (OrderDetailProduct) dao.get(OrderDetailProduct.class, productId[i]);
                    if (odp != null && odp.getOrder().equals(order) && productCost.length > i) {
                        Double newCost = SomeUtils.forceStrToDouble(productCost[i]);
                        if (newCost != null) {
                            odp.setCostPrice(newCost);
                            dao.save(odp);
                        }
                    }
                }
            }
            // packages cost
            if (packageId != null && packagesCost != null && packageId.length > 0) {
                for (int i = 0; i < packageId.length; i++) {
                    OrderPacking op = (OrderPacking) dao.get(OrderPacking.class, packageId[i]);
                    if (op != null && op.getOrder().equals(order) && packagesCost.length > i) {
                        Double newCost = SomeUtils.forceStrToDouble(packagesCost[i]);
                        if (newCost != null) {
                            op.setDeliveryCost(newCost);
                            dao.save(op);
                        }
                    }
                }
            }

            Double hc = SomeUtils.forceStrToDouble(handlingCost);
            if (hc != null) {
                order.setHandlingCost(hc);
                dao.save(order);
            }
        }
        return SUCCESS;
    }

    @Action(value = "orderpendingrates", results = {
            @Result(type = "velocity", name = "input", location = "/WEB-INF/views/admin/orderpendingrates.vm"),
            @Result(type = "velocity", location = "/WEB-INF/views/admin/orderpendingrates.vm")
    })
    public String orderPendingRates() throws Exception {
        List<OrderPacking> list = dao.getPendingOrderPackages(order);
        if (list != null && !list.isEmpty()) {
            List<BasePackage> listaPaquetes = new ArrayList<BasePackage>();
            for (OrderPacking p : list) {
                BasePackage pck = new BasePackage();
                Double height = SomeUtils.strToDouble(getParentProperty(p, "dimentionHeight"));
                Double width = SomeUtils.strToDouble(getParentProperty(p, "dimentionWidth"));
                Double length = SomeUtils.strToDouble(getParentProperty(p, "dimentionLength"));
                Double weight = SomeUtils.strToDouble(getParentProperty(p, "weight"));
                if (height != null) pck.setDimensionsHeight(height.floatValue());
                if (length != null) pck.setDimensionsLength(length.floatValue());
                if (width != null) pck.setDimensionsWidth(width.floatValue());
                if (weight != null) pck.setPackageWeight(weight.floatValue());
                pck.setInsuredValueMonetaryValue(p.getMonetaryValue().floatValue());
                pck.setCurrencyCode(getDefaultCurrency().getCode());
                pck.setDimensionUnit(getDimensionUnit());
                pck.setWeightUnit(getWeightUnit());
                listaPaquetes.add(pck);
            }

            org.store.core.utils.carriers.Address address = new org.store.core.utils.carriers.Address(order.getDeliveryAddress().getAddress(), order.getDeliveryAddress().getAddress2(), order.getDeliveryAddress().getCity(), order.getDeliveryAddress().getState().getStateCode(), order.getDeliveryAddress().getZipCode(), order.getDeliveryAddress().getIdCountry());
            org.store.core.utils.carriers.StructuredPhoneNumber phone = (order.getDeliveryAddress().getPhone() != null) ? new StructuredPhoneNumber(order.getDeliveryAddress().getPhone(), "-") : null;
            org.store.core.utils.carriers.ShipTo shipTo = new ShipTo(order.getDeliveryAddress().getCompany(), order.getDeliveryAddress().getFullName(), address, phone);
            List<String> carriers = dao.getCarriers(false);
            if (!carriers.isEmpty()) {
                CarrierUtils carrierUtils = new CarrierUtils(getServletContext());
                List<org.store.core.utils.carriers.RateService> serviceList = new ArrayList<org.store.core.utils.carriers.RateService>();
                Map<String, ShippingMethod> mapShippingMethods = dao.getMapShippingMethods();
                for (String carrier : carriers) {
                    Properties carrierProps = dao.getCarrierProperties(carrier);
                    if (carrierProps != null) {
                        CarrierService cs = carrierUtils.getCarrierService(carrier, carrierProps);
                        if (cs.available(shipTo, listaPaquetes)) {
                            // Hacer la consulta
                            org.store.core.utils.carriers.RateServiceResponse ratesResponse = cs.getRateServices(shipTo, listaPaquetes);

                            // Leer respuesta
                            for (org.store.core.utils.carriers.RateService rateService : ratesResponse.getRateServices()) {
                                ShippingMethod shippingMethod = mapShippingMethods.get(carrier.toUpperCase() + "-" + rateService.getCode());
                                if (shippingMethod != null) {
                                    rateService.setMethod(shippingMethod);
                                    serviceList.add(rateService);
                                }
                            }

                            // Verificar si hay error
                            if (StringUtils.isNotEmpty(ratesResponse.getErrors())) {
                                addActionError(carrier.toUpperCase() + ": " + ratesResponse.getErrors());
                            }
                        } else {
                            addActionError(carrier + " is not available for the selected items or shipping address.");
                        }
                    } else {
                        addActionError(carrier + " has not connection properties defined.");
                    }
                }
                addToStack("serviceList", serviceList);
            } else {
                addActionError("There is no active carriers and not default method defined.");
            }
        }
        return SUCCESS;
    }

    @Action(value = "orderbarcode", results = @Result(type = "json", params = {"root", "barCode"}))
    public String orderBarCode() throws Exception {
        OrderDetailProduct odp = (OrderDetailProduct) dao.get(OrderDetailProduct.class, idODP);
        if (odp != null) {
            odp.setBarCode(barCode);
            dao.save(odp);
        }
        return SUCCESS;
    }

    @Action(value = "ordergenerateinvoice", results = @Result(type = "json", params = {"root", "jsonResp"}))
    public String generateInvoice() throws Exception {
        jsonResp = new HashMap<String, Serializable>();
        jsonResp.put("result", "error");
        if (order != null) {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("order", new MOrder(order, this));
            map.put("user", new MUser(order.getUser(), this));
            String htmlTextForMail = proccessVelocityTemplate(Mail.MAIL_TEMPLATE_INVOICE, map);
            map.put("output", "pdf");
            String htmlTextForPDF = cleanHtml(proccessVelocityTemplate(Mail.MAIL_TEMPLATE_INVOICE, map));
            try {

                String filename = getServletContext().getRealPath("/stores/" + getStoreCode() + PATH_INVOICE);
                FileUtils.forceMkdir(new File(filename));
                OutputStream os = new FileOutputStream(filename + File.separator + order.getIdOrder().toString() + ".pdf");
                ITextRenderer renderer = new ITextRenderer();
                renderer.setDocumentFromString(htmlTextForPDF);
                renderer.layout();
                renderer.createPDF(os);
                os.close();

                jsonResp.put("result", "ok");
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                jsonResp.put("error", e.getMessage());
            }

            if (StringUtils.isEmpty(noMail) && StringUtils.isNotEmpty(order.getUser().getEmail())) {
                Mail mail = new Mail();
                mail.setInventaryCode(getStoreCode());
                mail.setBody(htmlTextForMail);
                mail.setSubject(getText(CNT_SUBJECT_INVOICE, CNT_DEFAULT_SUBJECT_INVOICE, order.getIdOrder().toString()));
                mail.setToAddress(order.getUser().getEmail());
                mail.setPriority(Mail.PRIORITY_HIGH);
                mail.setReference("INVOICE FOR ORDER " + order.getIdOrder());
                mailSaveAndSend(mail);
            }
        }
        return SUCCESS;
    }

    @Action(value="orderpackingslip_old", results={@org.apache.struts2.convention.annotation.Result(type="json", params={"root", "jsonResp"})})
    public String packingSlip()
            throws Exception
    {
        this.jsonResp = new HashMap();
        this.jsonResp.put("result", "error");
        if (this.order != null)
        {
            Map<String, Object> map = new HashMap();
            map.put("order", new MOrder(this.order, this));
            map.put("user", new MUser(this.order.getUser(), this));
            String htmlTextForMail = proccessVelocityTemplate("/WEB-INF/views/mails/packing_slip.vm", map);
            if (StringUtils.isNotEmpty(this.noMail))
            {
                Mail mail = new Mail();
                mail.setInventaryCode(getStoreCode());
                mail.setBody(htmlTextForMail);
                mail.setSubject(getText("subject.packing.slip", "Packing Slip. Order {0}", this.order.getIdOrder().toString()));
                mail.setToAddress(this.noMail);
                mail.setPriority(Integer.valueOf(10));
                mail.setReference("PACKING SLIP " + this.order.getIdOrder());
                mailSaveAndSend(mail);
                this.jsonResp.put("result", "ok");
            }
            else
            {
                this.jsonResp.put("error", "Email address is required");
            }
        }
        return "success";
    }

    @Action(value="orderpackingslip", results={@org.apache.struts2.convention.annotation.Result(type="json", params={"root", "jsonResp"})})
    public String packingSlipPdf()
            throws Exception
    {
        this.jsonResp = new HashMap();
        this.jsonResp.put("result", "error");
        if (this.order != null)
        {
            Map<String, Object> map = new HashMap();
            map.put("order", new MOrder(this.order, this));
            map.put("user", new MUser(this.order.getUser(), this));
            String htmlTextForMail = proccessVelocityTemplate("/WEB-INF/views/mails/packing_slip.vm", map);

            map.put("output", "pdf");
            String pdfFileName = "";
            String htmlTextForPDF = cleanHtml(proccessVelocityTemplate("/WEB-INF/views/mails/packing_slip_pdf.vm", map));
            try
            {
                String filename = getServletContext().getRealPath("/stores/" + getStoreCode() + "/packing_slip");
                FileUtils.forceMkdir(new File(filename));
                pdfFileName = filename + File.separator + this.order.getIdOrder().toString() + ".pdf";
                OutputStream os = new FileOutputStream(pdfFileName);
                ITextRenderer renderer = new ITextRenderer();
                renderer.setDocumentFromString(htmlTextForPDF);
                renderer.layout();
                renderer.createPDF(os);
                os.close();
            }
            catch (Exception e)
            {
                log.error(e.getMessage(), e);
            }
            if (StringUtils.isNotEmpty(this.noMail))
            {
                Mail mail = new Mail();
                mail.setInventaryCode(getStoreCode());
                mail.setBody(htmlTextForMail);
                mail.setSubject(getText("subject.packing.slip", "Packing Slip. Order {0}", this.order.getIdOrder().toString()));
                mail.setToAddress(this.noMail);
                mail.setPriority(Integer.valueOf(10));
                mail.setReference("PACKING SLIP " + this.order.getIdOrder());
                mail.setAttachment(pdfFileName);
                mailSaveAndSend(mail);
                this.jsonResp.put("result", "ok");
            }
            else
            {
                this.jsonResp.put("error", "Email address is required");
            }
        }
        return "success";
    }

    private String cleanHtml(String s) {
        try {
            SAXBuilder saxBuilder = new SAXBuilder("org.ccil.cowan.tagsoup.Parser");
            org.jdom.Document document = saxBuilder.build(new StringReader(s));
            XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
            return outputter.outputString(document);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    @Action(value = "rmalist", results = @Result(type = "velocity", location = "/WEB-INF/views/admin/rmalist.vm"))
    public String rmaList() throws Exception {
        RmaType rmaType = (RmaType) dao.get(RmaType.class, filterType);
        DataNavigator rmas = new DataNavigator(getRequest(), "rmas");
        rmas.setListado(dao.getRmas(rmas, filterCode, SomeUtils.strToDate(filterDateFrom, getDefaultLanguage()), SomeUtils.strToDate(filterDateTo, getDefaultLanguage()), filterRmaStatus, rmaType));
        addToStack("rmas", rmas);
        getBreadCrumbs().add(new BreadCrumb(null, getText("admin.rma.list"), null, null));
        return SUCCESS;
    }

    @Action(value = "rmadata", results = @Result(type = "velocity", location = "/WEB-INF/views/admin/rmadata.vm"))
    public String rmaData() throws Exception {
        getBreadCrumbs().add(new BreadCrumb(null, getText("admin.rma.list"), url("rmalist", "/admin"), null));
        getBreadCrumbs().add(new BreadCrumb(null, getText("admin.rma.data"), null, null));
        return SUCCESS;
    }

    @Action(value = "rmasave", results = @Result(type = "redirectAction", location = "rmalist"))
    public String rmaSave() throws Exception {
        if (rma != null) {
            boolean newStatus = (StringUtils.isNotEmpty(rmaStatus) && !rmaStatus.equalsIgnoreCase(rma.getRmaStatus()));
            rma.setRmaStatus(rmaStatus);
            rma.setRmaNumber(rmaNumber);
            dao.save(rma);
            if (StringUtils.isNotEmpty(rmaComment) || newStatus) {
                RmaLog rmaLog = new RmaLog();
                rmaLog.setActionUser(getAdminUser());
                rmaLog.setRma(rma);
                if (newStatus) rmaLog.setRmaStatus(rmaStatus);
                rmaLog.setActionComments(rmaComment);
                dao.save(rmaLog);
            }
        }
        return SUCCESS;
    }

    public List<RmaType> getRmaTypes() {
        return dao.getRmaTypes();
    }

    @Action(value = "orderxml", results = @Result(type = "stream", params = {"allowCaching", "false", "inputName", "inputStream", "contentType", "${contentType}"}))
    public String orderXml() throws Exception {
        if (order != null) {
            String cad = proccessVelocityTemplate("/WEB-INF/views/admin/order_xml.vm");
            setInputStream(new ByteArrayInputStream(cad.getBytes()));
            setContentType("text/xml");
        }
        return SUCCESS;
    }

    @Action(value = "orderpaymentedit", results = @Result(type = "velocity", location = "/WEB-INF/views/admin/orderpaymentedit.vm"))
    public String orderPaymentEdit() throws Exception {
        if (order != null && orderPayment != null && !order.equals(orderPayment.getOrder()))
            orderPayment = null;
        getBreadCrumbs().add(new BreadCrumb(null, getText("admin.order.payment.list"), url("orderpaymentlist", "/admin"), null));
        getBreadCrumbs().add(new BreadCrumb(null, getText("admin.order.payment.edit"), null, null));
        return SUCCESS;
    }

    @Action(value = "orderpaymentsave", results = @Result(type = "redirectAction", location = "orderdata?idOrder=${order.idOrder}&openTab=1"))
    public String orderPaymentSave() throws Exception {
        if (order != null && orderPayment != null && orderPayment.getId() != null && order.equals(orderPayment.getOrder())) {
            orderPayment.setPaymentDate(SomeUtils.strToDate(paymentDate, getDefaultLanguage()));
            orderPayment.setUser(getAdminUser());
            dao.save(orderPayment);
        }
        return SUCCESS;
    }

    @Action(value = "orderpaymentlist", results = @Result(type = "velocity", location = "/WEB-INF/views/admin/orderpaymentlist.vm"))
    public String orderPaymentList() throws Exception {
        processFilters();
        DataNavigator payments = new DataNavigator(getRequest(), "payments");
        payments.setListado(dao.getPayments(payments, filters, getDefaultLanguage()));
        addToStack("payments", payments);
        getBreadCrumbs().add(new BreadCrumb(null, getText("admin.order.payment.list"), null, null));
        return SUCCESS;
    }

    @Action(value = "requestLinks", results = @Result(type = "redirectAction", location = "orderdata?idOrder=${order.idOrder}"))
    public String requestLinks() throws Exception {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("order", order);
        EventUtils.executeAdminEvent(getServletContext(), EventService.EVENT_ADMIN_CHANGE_ORDER_STATUS, this, map);
        return SUCCESS;
    }

    @Action(value = "orderManualLink", results = @Result(type = "redirectAction", location = "orderdata?idOrder=${order.idOrder}"))
    public String setManualLink() throws Exception {
        OrderDetailProduct odp = (OrderDetailProduct) dao.get(OrderDetailProduct.class, idODP);
        if (order != null && odp != null && order.equals(odp.getOrder()) && barCode != null) {
            odp.setDownloadLink(barCode);
            dao.save(odp);
            List<OrderDetailProduct> newLinks = new ArrayList<OrderDetailProduct>();
            newLinks.add(odp);
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("order", new MOrder(order, this));
            map.put("user", new MUser(order.getUser(), this));
            map.put("links", new MAvailableLinks(newLinks, this));
            String body = proccessVelocityTemplate(Mail.MAIL_TEMPLATE_AVAILABLE_LINKS, map);
            Mail mail = new Mail();
            mail.setInventaryCode(getStoreCode());
            mail.setBody(body);
            mail.setSubject(getText(CNT_SUBJECT_LINKS_AVAILABLE, CNT_DEFAULT_SUBJECT_LINKS_AVAILABLE));
            mail.setToAddress(order.getUser().getEmail());
            mail.setPriority(Mail.PRIORITY_HIGH);
            mail.setReference("NEW_LINKS " + order.getIdOrder());
            mailSaveAndSend(mail);
        }
        return SUCCESS;
    }


    private String filterCode;
    private Long[] filterStatus;
    private String filterDateFrom;
    private String filterDateTo;
    private String filterUser;
    private Long filterType;
    private Long filterAdmin;
    private String[] filterRmaStatus;

    private Order order;
    private Long idOrder;
    private OrderStatus orderStatus;
    private Long idStatus;
    private String statusComment;
    private OrderPacking orderPacking;
    private Long idPacking;
    private String invoiceNo;
    private String invoiceDate;
    private String purchaseOrder;
    private String noMail;

    private Long[] packageProductDetail;
    private Integer[] packageProductQuantity;
    private Double packageWidth;
    private Double packageHeight;
    private Double packageLength;
    private Double packageWeight;

    private Long packageShippingMethod;
    private String packageDate;
    private Double packageCost;
    private String packageTracking;
    private String packageTrackingText;
    private Long deliveryShippingMethod;

    private Rma rma;
    private Long idRma;
    private String rmaStatus;
    private String rmaComment;

    private Long idODP;
    private String barCode;
    private String output;
    private String rmaNumber;

    private String openTab;

    private Long idPayment;
    private OrderPayment orderPayment;
    private String paymentDate;

    private Long[] productId;
    private Long[] packageId;
    private String[] productCost;
    private String[] packagesCost;
    private String handlingCost;

    public Long[] getProductId() {
        return productId;
    }

    public void setProductId(Long[] productId) {
        this.productId = productId;
    }

    public Long[] getPackageId() {
        return packageId;
    }

    public void setPackageId(Long[] packageId) {
        this.packageId = packageId;
    }

    public String[] getPackagesCost() {
        return packagesCost;
    }

    public void setPackagesCost(String[] packagesCost) {
        this.packagesCost = packagesCost;
    }

    public String[] getProductCost() {
        return productCost;
    }

    public void setProductCost(String[] productCost) {
        this.productCost = productCost;
    }

    public String getHandlingCost() {
        return handlingCost;
    }

    public void setHandlingCost(String handlingCost) {
        this.handlingCost = handlingCost;
    }

    public String getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(String paymentDate) {
        this.paymentDate = paymentDate;
    }

    public Long getIdPayment() {
        return idPayment;
    }

    public void setIdPayment(Long idPayment) {
        this.idPayment = idPayment;
    }

    public OrderPayment getOrderPayment() {
        return orderPayment;
    }

    public void setOrderPayment(OrderPayment orderPayment) {
        this.orderPayment = orderPayment;
    }

    public String getOpenTab() {
        return openTab;
    }

    public void setOpenTab(String openTab) {
        this.openTab = openTab;
    }

    public String getRmaStatus() {
        return rmaStatus;
    }

    public void setRmaStatus(String rmaStatus) {
        this.rmaStatus = rmaStatus;
    }

    public String getRmaComment() {
        return rmaComment;
    }

    public void setRmaComment(String rmaComment) {
        this.rmaComment = rmaComment;
    }

    public Long getIdODP() {
        return idODP;
    }

    public void setIdODP(Long idODP) {
        this.idODP = idODP;
    }

    public String getBarCode() {
        return barCode;
    }

    public void setBarCode(String barCode) {
        this.barCode = barCode;
    }

    public Long getDeliveryShippingMethod() {
        return deliveryShippingMethod;
    }

    public void setDeliveryShippingMethod(Long deliveryShippingMethod) {
        this.deliveryShippingMethod = deliveryShippingMethod;
    }

    public Long getPackageShippingMethod() {
        return packageShippingMethod;
    }

    public void setPackageShippingMethod(Long packageShippingMethod) {
        this.packageShippingMethod = packageShippingMethod;
    }

    public String getPackageDate() {
        return packageDate;
    }

    public void setPackageDate(String packageDate) {
        this.packageDate = packageDate;
    }

    public Double getPackageCost() {
        return packageCost;
    }

    public void setPackageCost(Double packageCost) {
        this.packageCost = packageCost;
    }

    public String getPackageTracking() {
        return packageTracking;
    }

    public void setPackageTracking(String packageTracking) {
        this.packageTracking = packageTracking;
    }

    public String getPackageTrackingText() {
        return packageTrackingText;
    }

    public void setPackageTrackingText(String packageTrackingText) {
        this.packageTrackingText = packageTrackingText;
    }

    public Long[] getPackageProductDetail() {
        return packageProductDetail;
    }

    public void setPackageProductDetail(Long[] packageProductDetail) {
        this.packageProductDetail = packageProductDetail;
    }

    public Integer[] getPackageProductQuantity() {
        return packageProductQuantity;
    }

    public void setPackageProductQuantity(Integer[] packageProductQuantity) {
        this.packageProductQuantity = packageProductQuantity;
    }

    public Double getPackageWidth() {
        return packageWidth;
    }

    public void setPackageWidth(Double packageWidth) {
        this.packageWidth = packageWidth;
    }

    public Double getPackageHeight() {
        return packageHeight;
    }

    public void setPackageHeight(Double packageHeight) {
        this.packageHeight = packageHeight;
    }

    public Double getPackageLength() {
        return packageLength;
    }

    public void setPackageLength(Double packageLength) {
        this.packageLength = packageLength;
    }

    public Double getPackageWeight() {
        return packageWeight;
    }

    public void setPackageWeight(Double packageWeight) {
        this.packageWeight = packageWeight;
    }

    public OrderPacking getOrderPacking() {
        return orderPacking;
    }

    public void setOrderPacking(OrderPacking orderPacking) {
        this.orderPacking = orderPacking;
    }

    public Long getIdPacking() {
        return idPacking;
    }

    public void setIdPacking(Long idPacking) {
        this.idPacking = idPacking;
    }

    public OrderStatus getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(OrderStatus orderStatus) {
        this.orderStatus = orderStatus;
    }

    public String getStatusComment() {
        return statusComment;
    }

    public void setStatusComment(String statusComment) {
        this.statusComment = statusComment;
    }

    public Long getIdStatus() {
        return idStatus;
    }

    public void setIdStatus(Long idStatus) {
        this.idStatus = idStatus;
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

    public String getFilterCode() {
        return filterCode;
    }

    public void setFilterCode(String filterCode) {
        this.filterCode = filterCode;
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

    public Long[] getFilterStatus() {
        return filterStatus;
    }

    public void setFilterStatus(Long[] filterStatus) {
        this.filterStatus = filterStatus;
    }

    public Long getFilterType() {
        return filterType;
    }

    public void setFilterType(Long filterType) {
        this.filterType = filterType;
    }

    public Long getFilterAdmin() {
        return filterAdmin;
    }

    public void setFilterAdmin(Long filterAdmin) {
        this.filterAdmin = filterAdmin;
    }

    public String[] getFilterRmaStatus() {
        return filterRmaStatus;
    }

    public void setFilterRmaStatus(String[] filterRmaStatus) {
        this.filterRmaStatus = filterRmaStatus;
    }

    public Rma getRma() {
        return rma;
    }

    public void setRma(Rma rma) {
        this.rma = rma;
    }

    public Long getIdRma() {
        return idRma;
    }

    public void setIdRma(Long idRma) {
        this.idRma = idRma;
    }

    public String getRmaNumber() {
        return rmaNumber;
    }

    public void setRmaNumber(String rmaNumber) {
        this.rmaNumber = rmaNumber;
    }

    public String getOutput() {
        return output;
    }

    public void setOutput(String output) {
        this.output = output;
    }

    public String getNoMail() {
        return noMail;
    }

    public void setNoMail(String noMail) {
        this.noMail = noMail;
    }

    public String getInvoiceNo() {
        return invoiceNo;
    }

    public void setInvoiceNo(String invoiceNo) {
        this.invoiceNo = invoiceNo;
    }

    public String getInvoiceDate() {
        return invoiceDate;
    }

    public void setInvoiceDate(String invoiceDate) {
        this.invoiceDate = invoiceDate;
    }

    public String getPurchaseOrder() {
        return purchaseOrder;
    }

    public void setPurchaseOrder(String purchaseOrder) {
        this.purchaseOrder = purchaseOrder;
    }

}
