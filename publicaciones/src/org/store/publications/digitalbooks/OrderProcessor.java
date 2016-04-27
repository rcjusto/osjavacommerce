package org.store.publications.digitalbooks;

import org.store.core.beans.Currency;
import org.store.core.beans.OrderDetail;
import org.store.core.beans.OrderDetailProduct;
import org.store.core.beans.ProductProvider;
import org.store.core.beans.Provider;
import org.store.core.dao.HibernateDAO;
import org.store.publications.digitalbooks.axis.DBSOAPMethods_BindingStub;
import org.store.publications.digitalbooks.axis.DBSOAPMethods_ServiceLocator;
import org.store.publications.digitalbooks.axis.LineaPedido;
import org.store.publications.digitalbooks.axis.LineaPedidoRespuesta;
import org.apache.axis.AxisFault;
import org.apache.axis.client.Call;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.rmi.RemoteException;
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
    private Currency currency;
    private Provider provider;
    private String username;
    private String password;
    private String retailerId;
    private String country;

    public OrderProcessor(HibernateDAO dao, Map config) {
        this.dao = dao;

        this.username = (config != null) ? (String) config.get("digitalbooks_user") : "";
        this.password = (config != null) ? (String) config.get("digitalbooks_password") : "";
        this.retailerId = (config != null) ? (String) config.get("digitalbooks_elibrary") : "";
        this.country = (config != null) ? (String) config.get("digitalbooks_country") : "";

        provider = dao.getProviderByAltName("DigitalBooks");
        if (provider == null) {
            provider = new Provider();
            provider.setAlternateNo("DigitalBooks");
            provider.setProviderName("Digital Books");
            dao.save(provider);
        }

        this.currency = dao.getDefaultCurrency();

    }

    public List<OrderDetailProduct> processOrder(Long idOrder) {
        List<OrderDetailProduct> newLinks = new ArrayList<OrderDetailProduct>();
        try {
            org.store.core.beans.Order order = (org.store.core.beans.Order) dao.get(org.store.core.beans.Order.class, idOrder);
            if (order != null) {
                List<OrderDetailProduct> productList = getProducts(order, provider);
                if (productList != null && !productList.isEmpty()) {

                    DBSOAPMethods_BindingStub binding;

                    Map<String, OrderDetailProduct> map = new HashMap<String, OrderDetailProduct>();
                    for (OrderDetailProduct odp : productList) {
                        if (StringUtils.isEmpty(odp.getDownloadLink())) {
                            map.put(odp.getProduct().getMfgPartnumber(), odp);
                        }
                    }

                    if (!map.isEmpty()) {

                        binding = (DBSOAPMethods_BindingStub) new DBSOAPMethods_ServiceLocator().getDBSOAPMethods();
                        binding._setProperty(Call.USERNAME_PROPERTY, username);
                        binding._setProperty(Call.PASSWORD_PROPERTY, password);

                        LineaPedido lines[] = new LineaPedido[map.size()];
                        int i = 0;
                        for (String ean13 : map.keySet()) {
                            OrderDetailProduct odp = map.get(ean13);
                            LineaPedido linea = new LineaPedido();
                            linea.setEan_13(ean13);
                            linea.setItem_pedido(String.valueOf(i + 1));
                            ProductProvider pp = odp.getProduct().getProductProvider(provider);
                            linea.setPrecio((pp != null && pp.getCostPrice() != null) ? pp.getCostPrice().floatValue() : odp.getCostPrice().floatValue());
                            linea.setDivisa((pp != null && pp.getCostCurrency() != null) ? pp.getCostCurrency().getCode() : currency.getCode());
                            lines[i++] = linea;
                        }

                        try {
                            LineaPedidoRespuesta[] response = binding.librosVendidos(retailerId, idOrder.toString(), lines, country);
                            if (response != null && response.length>0) {
                                for (LineaPedidoRespuesta ol : response) {
                                    OrderDetailProduct odp = map.get(ol.getEan_13());
                                    if (odp != null) {
                                        if (StringUtils.isNotEmpty(ol.getLink())) {
                                            odp.setDownloadLink(ol.getLink());
                                            odp.setTrackingStatus("Ready to download");
                                            newLinks.add(odp);
                                        } else {
                                            odp.setTrackingStatus("Error: download link not available");
                                        }
                                        dao.save(odp);
                                    }
                                }
                            }

                        } catch (RemoteException e) {
                            log.error(e.getMessage(), e);
                            if (e instanceof AxisFault && StringUtils.isNotEmpty(e.getMessage())) {
                                for (String ean13 : map.keySet()) {
                                    OrderDetailProduct odp = map.get(ean13);
                                    odp.setTrackingStatus("Error: " + getWSError(e.getMessage()));
                                    dao.save(odp);
                                }
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
    /*
    public void processOrder(Long idOrder) {
        try {
            org.store.core.beans.Order order = (org.store.core.beans.Order) dao.get(org.store.core.beans.Order.class, idOrder);
            if (order != null) {
                List<OrderDetailProduct> productList = getProducts(order, provider);
                if (productList != null && !productList.isEmpty()) {

                    DBSOAPMethodsStub stub = new DBSOAPMethodsStub();

                    HttpTransportProperties.Authenticator basicAuth = new HttpTransportProperties.Authenticator();
                    basicAuth.setUsername(username);
                    basicAuth.setPassword(password);
                    basicAuth.setPreemptiveAuthentication(true);
                    final Options clientOptions = stub._getServiceClient().getOptions();
                    clientOptions.setProperty(HTTPConstants.AUTHENTICATE, basicAuth);

                    Map<String, OrderDetailProduct> map = new HashMap<String, OrderDetailProduct>();
                    for (OrderDetailProduct odp : productList) {
                        if (StringUtils.isEmpty(odp.getDownloadLink())) {
                            map.put(odp.getProduct().getMfgPartnumber(), odp);
                        }
                    }

                    if (!map.isEmpty()) {
                        LibrosVendidos librosVendidos = new LibrosVendidos();
                        librosVendidos.setRetailer_id(retailerId);
                        librosVendidos.setId_pais_facturacion(country);
                        librosVendidos.setId_pedido(order.getIdOrder().toString());

                        LineaPedidoArray lines = new LineaPedidoArray();

                        int i = 0;
                        for (String ean13 : map.keySet()) {
                            OrderDetailProduct odp = map.get(ean13);
                            LineaPedido linea = new LineaPedido();
                            linea.setEan_13(ean13);
                            linea.setItem_pedido(String.valueOf(i + 1));
                            ProductProvider pp = odp.getProduct().getProductProvider(provider);
                            linea.setPrecio((pp != null && pp.getCostPrice() != null) ? pp.getCostPrice().floatValue() : odp.getCostPrice().floatValue());
                            linea.setDivisa((pp != null && pp.getCostCurrency() != null) ? pp.getCostCurrency().getCode() : currency.getCode());
                            lines.addLineaPedido(linea);
                        }

                        librosVendidos.setArray_pedidos(lines);

                        LibrosVendidosE request = new LibrosVendidosE();
                        request.setLibrosVendidos(librosVendidos);
                        try {
                            LibrosVendidosResponseE response = stub.librosVendidos(request);
                            if (response != null && response.getLibrosVendidosResponse() != null && response.getLibrosVendidosResponse().getLibrosVendidosResult() != null) {
                                for (LineaPedidoRespuesta ol : response.getLibrosVendidosResponse().getLibrosVendidosResult().getLineaPedidoRespuesta()) {
                                    OrderDetailProduct odp = map.get(ol.getEan_13());
                                    if (odp != null) {
                                        if (StringUtils.isNotEmpty(ol.getLink())) {
                                            odp.setDownloadLink(ol.getLink());
                                            odp.setTrackingStatus("Ready to download");
                                        } else {
                                            odp.setTrackingStatus("Error: download link not available");
                                        }
                                        dao.save(odp);
                                    }
                                }
                            }
                        } catch (RemoteException e) {
                            log.error(e.getMessage(), e);
                            if (e instanceof AxisFault && StringUtils.isNotEmpty(e.getMessage())) {
                                for (String ean13 : map.keySet()) {
                                    OrderDetailProduct odp = map.get(ean13);
                                    odp.setTrackingStatus("Error: " + getWSError(e.getMessage()));
                                    dao.save(odp);
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
    */
    private String getWSError(String message) {
        if ("00".equalsIgnoreCase(message)) return "Error en los parámetros de entrada";
        if ("01".equalsIgnoreCase(message)) return "La librería indicada es incorrecta. No existe";
        if ("02".equalsIgnoreCase(message)) return "La librería indicada es incorrecta. Usuario no autorizado";
        if ("03".equalsIgnoreCase(message)) return "El libro no se puede vender en la librería";
        if ("04".equalsIgnoreCase(message)) return "El precio indicado no coincide con el precio del libro";
        if ("05".equalsIgnoreCase(message)) return "No existe el código CS4 del libro. Imposible generar el enlace.";
        if ("06".equalsIgnoreCase(message)) return "Pedido duplicado";
        if ("07".equalsIgnoreCase(message)) return "Error interno al añadir la venta.";
        if ("08".equalsIgnoreCase(message)) return "No se encuentra el enlace asociado a los parámetros introducidos";
        if ("09".equalsIgnoreCase(message)) return "No se encuentran los enlaces asociados al pedido indicado";
        if ("10".equalsIgnoreCase(message)) return "No han pasado 24h desde la ultima petición";
        if ("11".equalsIgnoreCase(message)) return "No se ha especificado el valor de la divisa. Es obligatorio al ser librería multidivisa.";
        if ("12".equalsIgnoreCase(message)) return "La divisa indicada no esta entre las divisas con las que trabaja la librería.";
        if ("13".equalsIgnoreCase(message)) return "No se ha especificado el país de facturación. Es obligatorio al ser librería multidivisa";
        if ("14".equalsIgnoreCase(message)) return "Código de país de facturación desconocido";
        if ("15".equalsIgnoreCase(message)) return "No se ha indicado el item_pedido en una venta con mas de un libro repetido";
        if ("16".equalsIgnoreCase(message)) return "Uno de los libros del pedido no se puede vender en entorno de pruebas";
        if ("17".equalsIgnoreCase(message)) return "Se ha superado el número máximo de compras en entorno de pruebas";
        return message;
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
