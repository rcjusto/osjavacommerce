package org.store.publications.libranda;

import com.sejer.dwh.CreateAndConfirmOrder;
import com.sejer.dwh.CreateAndConfirmOrderDocument;
import com.sejer.dwh.CreateAndConfirmOrderResponseDocument;
import com.sejer.dwh.Customer;
import com.sejer.dwh.GetOrderStatus;
import com.sejer.dwh.GetOrderStatusDocument;
import com.sejer.dwh.GetOrderStatusResponseDocument;
import com.sejer.dwh.OrderLine;
import com.sejer.dwh.OrderLineReq;
import com.sejer.dwh.OrderLineStatus;
import com.sejer.dwh.OrderReq;
import com.sejer.dwh.Payment;
import org.store.core.beans.OrderDetail;
import org.store.core.beans.OrderDetailProduct;
import org.store.core.beans.Provider;
import org.store.core.dao.HibernateDAO;
import org.store.publications.LibrariesEventServiceImpl;
import com.store.publications.libranda.axis2.OrderServiceImpl2ServiceStub;
import com.store.publications.libranda.axis2.OrderServiceImplServiceStub;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Rogelio Caballero
 * 18/09/11 3:00
 */
public class OrderProcessor extends LibrandaProcesor {

    public static Logger log = Logger.getLogger(OrderProcessor.class);
    private Provider provider;

    public OrderProcessor(HibernateDAO dao, Map config) {
        super(dao, config);

        provider = dao.getProviderByAltName("Libranda");
        if (provider == null) {
            provider = new Provider();
            provider.setAlternateNo("Libranda");
            provider.setProviderName("Libranda");
            dao.save(provider);
        }
    }

    public void getOrderStatus(Long idOrder) {
        try {
            org.store.core.beans.Order order = (org.store.core.beans.Order) dao.get(org.store.core.beans.Order.class, idOrder);
            if (order != null && StringUtils.isNotEmpty(order.getSupplierId())) {

                OrderServiceImplServiceStub stub = new OrderServiceImplServiceStub();

                List<OrderDetailProduct> librandaProducts = getLibrandaProducts(order, provider);
                if (librandaProducts != null && !librandaProducts.isEmpty()) {

                    Map<String, OrderDetailProduct> map = new HashMap<String, OrderDetailProduct>();
                    for (OrderDetailProduct odp : librandaProducts) {
                        if (StringUtils.isEmpty(odp.getDownloadLink())) {
                            map.put(odp.getProduct().getMfgPartnumber(), odp);
                        }
                    }

                    if (!map.isEmpty()) {
                        GetOrderStatus st = GetOrderStatus.Factory.newInstance();
                        st.setGencode(getGenCode());
                        st.setOrderId(order.getSupplierId());
                        st.setOutletName(getOutletName());
                        st.setPassword(getPassword());

                        GetOrderStatusDocument request = GetOrderStatusDocument.Factory.newInstance();
                        request.setGetOrderStatus(st);
                        GetOrderStatusResponseDocument response = stub.getOrderStatus(request);

                        if (response != null && response.getGetOrderStatusResponse() != null && response.getGetOrderStatusResponse().getReturn() != null && response.getGetOrderStatusResponse().getReturn().getStatus() != null && response.getGetOrderStatusResponse().getReturn().getStatus().getOrderLinesArray() != null) {
                            for (OrderLineStatus ol : response.getGetOrderStatusResponse().getReturn().getStatus().getOrderLinesArray()) {
                                if (StringUtils.isNotEmpty(ol.getEan13())) {
                                    OrderDetailProduct odp = map.get(ol.getEan13());
                                    if (odp != null) {
                                        odp.setTrackingStatus(ol.getStatus());
                                        dao.save(odp);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    public List<OrderDetailProduct> processOrder(Long idOrder) {
        List<OrderDetailProduct> newLinks = new ArrayList<OrderDetailProduct>();
        try {
            org.store.core.beans.Order order = (org.store.core.beans.Order) dao.get(org.store.core.beans.Order.class, idOrder);
            if (order != null) {
                OrderServiceImpl2ServiceStub stub = new OrderServiceImpl2ServiceStub();

                List<OrderDetailProduct> librandaProducts = getLibrandaProducts(order, provider);
                if (librandaProducts != null && !librandaProducts.isEmpty()) {

                    Map<String, OrderDetailProduct> map = new HashMap<String, OrderDetailProduct>();
                    for (OrderDetailProduct odp : librandaProducts) {
                        if (StringUtils.isEmpty(odp.getDownloadLink())) {
                            map.put(odp.getProduct().getMfgPartnumber(), odp);
                        }
                    }

                    if (!map.isEmpty()) {

                        Customer customer = Customer.Factory.newInstance();
                        customer.setCustomerEmail(order.getUser().getEmail());
                        customer.setCustomerFirstName(order.getUser().getFirstname());
                        customer.setCustomerName(order.getUser().getLastname());

                        Payment payment = Payment.Factory.newInstance();
                        payment.setAuthorizationNumber(order.getCodeMerchant());

                        OrderReq orderReq = OrderReq.Factory.newInstance();
                        orderReq.setCustomer(customer);
                        orderReq.setOrderId(order.getIdOrder().toString());
                        orderReq.setOutletName(getOutletName());
                        orderReq.setPayment(payment);
                        orderReq.setValidityDate(Calendar.getInstance());

                        OrderLineReq[] lines = new OrderLineReq[map.size()];
                        int i = 0;
                        for (String ean13 : map.keySet()) {
                            OrderLineReq line = OrderLineReq.Factory.newInstance();
                            line.setEan13(ean13);
                            line.setQuantity(1);
                            line.setProductPrice(0);
                            lines[i++] = line;
                        }
                        orderReq.setOrderLinesArray(lines);

                        CreateAndConfirmOrder req = CreateAndConfirmOrder.Factory.newInstance();
                        req.setOrder(orderReq);
                        req.setGencode(getGenCode());
                        req.setPassword(getPassword());

                        CreateAndConfirmOrderDocument request = CreateAndConfirmOrderDocument.Factory.newInstance();
                        request.setCreateAndConfirmOrder(req);
                        CreateAndConfirmOrderResponseDocument response = stub.createAndConfirmOrder(request);

                        String errorMsg = null;
                        if (response != null && response.getCreateAndConfirmOrderResponse() != null && response.getCreateAndConfirmOrderResponse().getReturn() != null) {
                            LibrariesEventServiceImpl.setInternalOrderId(order, LibrariesEventServiceImpl.FOLDER_LIBRANDA, response.getCreateAndConfirmOrderResponse().getReturn().getOrderId());
                            dao.save(order);

                            if (response.getCreateAndConfirmOrderResponse().getReturn().getOrderLinesArray() != null) {
                                for (OrderLine ol : response.getCreateAndConfirmOrderResponse().getReturn().getOrderLinesArray()) {
                                    if (StringUtils.isNotEmpty(ol.getDownloadUrl())) {
                                        OrderDetailProduct odp = map.get(ol.getEan13());
                                        if (odp != null) {
                                            odp.setDownloadLink(ol.getDownloadUrl());
                                            odp.setTrackingStatus(ol.getStatus());
                                            dao.save(odp);
                                            newLinks.add(odp);
                                        }
                                    }
                                }
                            }

                            errorMsg = response.getCreateAndConfirmOrderResponse().getReturn().getError();
                        }

                        // poner error sino se bajo el link
                        for (OrderDetailProduct odp : map.values()) {
                            if (StringUtils.isEmpty(odp.getDownloadLink())) {
                                odp.setTrackingStatus("Error: " + ((StringUtils.isNotEmpty(errorMsg)) ? getWSError(errorMsg.trim()) : "No se pudo bajar el enlace de Libranda"));
                                dao.save(odp);
                            }
                        }

                    }
                }

            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return newLinks;
    }

    private List<OrderDetailProduct> getLibrandaProducts(org.store.core.beans.Order order, Provider librandaProv) {
        if (order != null && order.getOrderDetails() != null) {
            List<OrderDetailProduct> result = new ArrayList<OrderDetailProduct>();
            for (OrderDetail od : order.getOrderDetails()) {
                if (od.getOrderDetailProducts() != null) {
                    for (OrderDetailProduct odp : od.getOrderDetailProducts()) {
                        if (dao.productHasProvider(odp.getProduct(), librandaProv)) {
                            result.add(odp);
                        }
                    }
                }
            }
            return result;
        }
        return null;
    }


    private String getWSError(String message) {
        if ("ERROR_INTERNAL".equalsIgnoreCase(message)) return "Error interno. Contacte con su soporte técnico.";
        if ("ERROR_NO_GENCODE".equalsIgnoreCase(message)) return "El Gencode no ha sido citado.";
        if ("ERROR_NO_PASSWORD".equalsIgnoreCase(message)) return "El password no ha sido citado.";
        if ("ERROR_AUTHENTICATION_FAILED".equalsIgnoreCase(message)) return "El Gencode o el Password no son validos.";
        if ("ERROR_INVALID_GENCODE".equalsIgnoreCase(message)) return "El gencode es invalido. Indica que el gencode ha sido comunicado.";
        if ("ERROR_INVALID_OUTLET".equalsIgnoreCase(message)) return "La tienda especificada no es reconozida en el sistema.";
        if ("ERROR_INVALID_ORDER_ID".equalsIgnoreCase(message)) return "El numero de pedido es obligatorio.";
        if ("ERROR_NO_DRM_FOR_OUTLET".equalsIgnoreCase(message)) return "No se ha definido ningún DRM para el punto de venta.";
        if ("ERROR_INTERNAL_CREATE_PARTY".equalsIgnoreCase(message)) return "Error interno. Contacte con su soporte técnico.";
        if ("ERROR_INTERNAL_CREATE_PARTY_OI".equalsIgnoreCase(message)) return "Error interno. Contacte con su soporte técnico.";
        if ("ERROR_INTERNAL_CREATE_PARTY_P".equalsIgnoreCase(message)) return "Error interno. Contacte con su soporte técnico.";
        if ("ERROR_INTERNAL_CREATE_PARTY_OR".equalsIgnoreCase(message)) return "Error interno. Contacte con su soporte técnico.";
        if ("ERROR_NO_DRM_FOR_OUTLET".equalsIgnoreCase(message)) return "No se ha definido ningún DRM para el punto de venta.";
        if ("ERROR_INTERNAL_CREATE_PARTY".equalsIgnoreCase(message)) return "Error interno. Contacte con su soporte técnico.";
        if ("ERROR_INTERNAL_CREATE_PARTY_OI".equalsIgnoreCase(message)) return "Error interno. Contacte con su soporte técnico.";
        if ("ERROR_INTERNAL_CREATE_PARTY_P".equalsIgnoreCase(message)) return "Error interno. Contacte con su soporte técnico.";
        if ("ERROR_INTERNAL_CREATE_PARTY_OR".equalsIgnoreCase(message)) return "Error interno. Contacte con su soporte técnico.";
        return message;
    }

}
