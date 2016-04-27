package org.store.publications.publidisa;

import org.store.core.beans.OrderDetail;
import org.store.core.beans.OrderDetailProduct;
import org.store.core.beans.Product;
import org.store.core.beans.ProductProvider;
import org.store.core.beans.Provider;
import org.store.core.dao.HibernateDAO;
import org.store.core.globals.SomeUtils;
import org.store.publications.LibrariesEventServiceImpl;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.datacontract.schemas._2004._07.distripalWSDL_data.Publidisa_Credentials;
import org.datacontract.schemas._2004._07.distripalWSDL_data.Publidisa_Direction;
import org.datacontract.schemas._2004._07.distripalWSDL_data.Publidisa_Info;
import org.datacontract.schemas._2004._07.distripalWSDL_data.Publidisa_Items;
import org.datacontract.schemas._2004._07.distripalWSDL_data.Publidisa_Links;
import org.datacontract.schemas._2004._07.distripalWSDL_data.Publidisa_StatusOrder;
import org.publidisa.Publidisa_Descriptor;
import org.publidisa.Publidisa_DescriptorLocator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Rogelio Caballero
 * 18/09/11 3:00
 */
public class OrderProcessor {

    public static Logger log = Logger.getLogger(OrderProcessor.class);
    protected HibernateDAO dao;
    private Provider provider;
    private String userCode;
    private String login;
    private String password;

    public OrderProcessor(HibernateDAO dao, Map config) {
        this.userCode = (config != null && config.containsKey("publidisa_userCode")) ? (String) config.get("publidisa_userCode") : "";
        this.login = (config != null && config.containsKey("publidisa_login")) ? (String) config.get("publidisa_login") : "";
        this.password = (config != null && config.containsKey("publidisa_password")) ? (String) config.get("publidisa_password") : "";
        this.dao = dao;


        provider = dao.getProviderByAltName("Publidisa");
        if (provider == null) {
            provider = new Provider();
            provider.setAlternateNo("Publidisa");
            provider.setProviderName("Publidisa");
            dao.save(provider);
        }

    }

    public List<OrderDetailProduct> processOrder(Long idOrder) {
        List<OrderDetailProduct> newLinks = new ArrayList<OrderDetailProduct>();
        try {
            org.store.core.beans.Order order = (org.store.core.beans.Order) dao.get(org.store.core.beans.Order.class, idOrder);
            if (order != null) {
                Integer internalOrderId = SomeUtils.strToInteger(LibrariesEventServiceImpl.getInternalOrderId(order, LibrariesEventServiceImpl.FOLDER_PUBLIDISA));
                List<OrderDetailProduct> productList = getProducts(order, provider);
                if (productList != null && !productList.isEmpty()) {

                    Map<String, OrderDetailProduct> map = new HashMap<String, OrderDetailProduct>();
                    for (OrderDetailProduct odp : productList) {
                        ProductProvider pp = odp.getProduct().getProductProvider(provider);
                        if (pp != null && (Product.TYPE_DIGITAL.equalsIgnoreCase(odp.getProduct().getProductType()) && StringUtils.isEmpty(odp.getDownloadLink()) || Product.TYPE_STANDARD.equalsIgnoreCase(odp.getProduct().getProductType()))) {
                            map.put(pp.getSku(), odp);
                        }
                    }

                    if (!map.isEmpty()) {
                        Publidisa_Descriptor services = new Publidisa_DescriptorLocator();

                        Publidisa_Credentials credentials = new Publidisa_Credentials();
                        credentials.setUserCode(SomeUtils.strToInteger(userCode));
                        credentials.setLogin(login);
                        credentials.setPwd(password);

                        String errorMsg = null;
                        if (internalOrderId != null) {
                            // la orden ya esta registrada

                            Publidisa_StatusOrder response = services.getBasicHttpBinding_Publidisa_IDescriptor().statusOrderClient(order.getIdOrder().toString(), credentials);
                            if (response != null) {
                                if (response.getError() != null) errorMsg = response.getError().getMsgError();
                                if (response.getDestinations() != null) {
                                    for (Publidisa_Info publidisa_info : response.getDestinations()) {
                                        for (Publidisa_Links publidisa_link : publidisa_info.getLinks()) {
                                            OrderDetailProduct odp = map.get(publidisa_link.getPublicationID());
                                            if (odp != null && odp.getProduct() != null && Product.TYPE_DIGITAL.equalsIgnoreCase(odp.getProduct().getProductType())) {
                                                if (StringUtils.isNotEmpty(publidisa_link.getURL())) {
                                                    odp.setDownloadLink(publidisa_link.getURL());
                                                    odp.setTrackingStatus("Ready to download");
                                                    newLinks.add(odp);
                                                } else {
                                                    odp.setTrackingStatus("Error: download link not available");
                                                }
                                                dao.save(odp);
                                            }
                                        }
                                    }
                                }
                            }

                        } else {
                            // registrar orden

                            Publidisa_Direction direction = new Publidisa_Direction();
                            direction.setEmail(order.getUser().getEmail());
                            direction.setName(order.getUser().getFullName());

                            if (order.getDeliveryAddress() != null) {
                                direction.setAddress(order.getDeliveryAddress().getAddress());
                                direction.setCity(order.getDeliveryAddress().getCity());
                                direction.setCountry(order.getDeliveryAddress().getIdCountry());
                                direction.setPhone(order.getDeliveryAddress().getPhone());
                                direction.setPostcode(order.getDeliveryAddress().getZipCode());
                                direction.setState(order.getDeliveryAddress().getState().getStateName());
                            }

                            Publidisa_Items[] items = new Publidisa_Items[map.size()];

                            int i = 0;
                            for (String sku : map.keySet()) {
                                OrderDetailProduct odp = map.get(sku);
                                Publidisa_Items linea = new Publidisa_Items();
                                linea.setPublicationID(sku);
                                linea.setQuantity(odp.getOrderDetail().getQuantity());
                                items[i++] = linea;
                            }

                            Publidisa_StatusOrder response = services.getBasicHttpBinding_Publidisa_IDescriptor().purchaseOrderLink(items, direction, "description", order.getIdOrder().toString(), credentials);
                            if (response != null) {
                                if (response.getError() != null) errorMsg = response.getError().getMsgError();
                                if (response.getNumOrder() != null) {
                                    LibrariesEventServiceImpl.setInternalOrderId(order, LibrariesEventServiceImpl.FOLDER_PUBLIDISA, response.getNumOrder().toString());
                                    dao.save(order);
                                }
                                if (response.getDestinations() != null) {
                                    for (Publidisa_Info publidisa_info : response.getDestinations()) {
                                        for (Publidisa_Links publidisa_link : publidisa_info.getLinks()) {
                                            OrderDetailProduct odp = map.get(publidisa_link.getPublicationID());
                                            if (odp != null && odp.getProduct() != null && Product.TYPE_DIGITAL.equalsIgnoreCase(odp.getProduct().getProductType())) {
                                                if (StringUtils.isNotEmpty(publidisa_link.getURL())) {
                                                    odp.setDownloadLink(publidisa_link.getURL());
                                                    odp.setTrackingStatus("Ready to download");
                                                } else {
                                                    odp.setTrackingStatus("Error: download link not available");
                                                }
                                                dao.save(odp);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        for (OrderDetailProduct odp : map.values()) {
                            if (odp.getProduct() != null && Product.TYPE_DIGITAL.equalsIgnoreCase(odp.getProduct().getProductType()) && StringUtils.isEmpty(odp.getDownloadLink())) {
                                odp.setTrackingStatus("Error: " + ((StringUtils.isNotEmpty(errorMsg) ? errorMsg : "No se pudo bajar el enlace de Publidisa")));
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

    private List<OrderDetailProduct> getProducts(org.store.core.beans.Order order, Provider provider) {
        if (order != null && order.getOrderDetails() != null) {
            List<OrderDetailProduct> result = new ArrayList<OrderDetailProduct>();
            for (OrderDetail od : order.getOrderDetails()) {
                if (od.getOrderDetailProducts() != null) {
                    for (OrderDetailProduct odp : od.getOrderDetailProducts()) {
                        if (dao.productHasProvider(odp.getProduct(), provider)) {
                            result.add(odp);
                        }
                    }
                }
            }
            return result;
        }
        return null;
    }

}
