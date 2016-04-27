package org.store.core.globals;

import org.store.core.beans.*;
import org.store.core.dao.HibernateDAO;
import org.store.core.utils.carriers.Address;
import org.store.core.utils.carriers.BasePackage;
import org.store.core.utils.carriers.CarrierUtils;
import org.store.core.utils.carriers.RateService;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class ShippingUtils implements StoreMessages {

    public static Logger log = Logger.getLogger(ShippingUtils.class);
    public static final String SESSION_RATE_SERVICES = "SESSION_RATE_SERVICES";

    // ouput
    private double total = 0.0d;
    private ShippingMethod selected;
    private List<Map<String, Object>> shippingRates;
    private List<String> errors;
    private Boolean orderNeedShipping = false;
    private Boolean needToSelectShipping = false;

    // private
    private MapCounter productsWithOutShippingRate = new MapCounter();
    private MapCounter productsWithFlatRate = new MapCounter();

    public ShippingUtils(ShopCart cart, State deliveryState, BaseAction action, ShippingMethod selectedMethod, boolean freeShipping, boolean getLiveMethods) {
        this.selected = selectedMethod;
        verifyShippableProducts(cart, action);
        if (cart.getPickInStore()!=null) {
            total = 0d;
            selected = null;
        } else calculate(cart, deliveryState, action, freeShipping, getLiveMethods);
    }

    public void verifyShippableProducts(ShopCart cart, BaseAction action) {
        orderNeedShipping = false;
        HibernateDAO dao = action.getDao();
        for (ShopCartItem it : cart.getItems()) {
            if (it.getBeanProd1() != null && dao.productNeedShipping(it.getBeanProd1())) orderNeedShipping = true;
            if (it.getBeanProd2() != null && dao.productNeedShipping(it.getBeanProd2())) orderNeedShipping = true;
        }
    }

    public void calculate(ShopCart cart, State deliveryState, BaseAction action, boolean freeShipping, boolean getLiveMethods) {
        HibernateDAO dao = action.getDao();
        Integer maxFlatRate = SomeUtils.strToInteger(dao.getStorePropertyValue(StoreProperty.PROP_FLAT_RATE_MAX, StoreProperty.TYPE_GENERAL, StoreProperty.PROP_DEFAULT_FLAT_RATE_MAX));

        double shippingCost = 0.0d;

        for (ShopCartItem it : cart.getItems()) {

            if (it.getVariation1()!=null) it.getBeanProd1().addProperty("variation",it.getVariation1());
            if (it.getBeanProd2()!=null && it.getVariation2()!=null) it.getBeanProd2().addProperty("variation",it.getVariation2());

            // Calcular Fixed Shipping
            if (deliveryState != null && it.getBeanProd1() != null && dao.productNeedShipping(it.getBeanProd1()) && !it.getBeanProd1().hasFreeShipping()) {
                ShippingRate rate1 = dao.getShippingRate(it.getBeanProd1(), deliveryState);
                if (rate1 != null) {
                    if (ShippingRate.TYPE_FIXED.equalsIgnoreCase(rate1.getShippingType())) {
                        if (!freeShipping) shippingCost += rate1.getValue() * it.getQuantity();
                    } else if (ShippingRate.TYPE_FLAT.equalsIgnoreCase(rate1.getShippingType())) {
                        Integer oldC = (productsWithFlatRate.containsKey(rate1)) ? productsWithFlatRate.get(rate1) : 0;
                        int withFlat = Math.max(0, Math.min(maxFlatRate - oldC, it.getQuantity()));
                        int withOutFlat = Math.max(0, Math.min(oldC + it.getQuantity() - maxFlatRate, it.getQuantity()));
                        if (withFlat > 0) productsWithFlatRate.add(rate1, withFlat);
                        if (withOutFlat > 0)
                            productsWithOutShippingRate.add(it.getBeanProd1(), withOutFlat);
                    } else if (ShippingRate.TYPE_LIVE.equalsIgnoreCase(rate1.getShippingType())) {
                        productsWithOutShippingRate.add(it.getBeanProd1(), it.getQuantity());
                    }
                } else {
                    productsWithOutShippingRate.add(it.getBeanProd1(), it.getQuantity());
                }
            }
            if (deliveryState != null && it.getBeanProd2() != null && dao.productNeedShipping(it.getBeanProd2()) && !it.getBeanProd2().hasFreeShipping()) {
                ShippingRate rate2 = dao.getShippingRate(it.getBeanProd2(), deliveryState);
                if (rate2 != null) {
                    if (ShippingRate.TYPE_FIXED.equalsIgnoreCase(rate2.getShippingType())) {
                        if (!freeShipping) shippingCost += rate2.getValue() * it.getQuantity();
                    } else if (ShippingRate.TYPE_FLAT.equalsIgnoreCase(rate2.getShippingType())) {
                        Integer oldC = (productsWithFlatRate.containsKey(rate2)) ? productsWithFlatRate.get(rate2) : 0;
                        int withFlat = Math.max(0, Math.min(maxFlatRate - oldC, it.getQuantity()));
                        int withOutFlat = Math.max(0, Math.min(oldC + it.getQuantity() - maxFlatRate, it.getQuantity()));
                        if (withFlat > 0) productsWithFlatRate.add(rate2, withFlat);
                        if (withOutFlat > 0)
                            productsWithOutShippingRate.add(it.getBeanProd2(), withOutFlat);
                    } else if (ShippingRate.TYPE_LIVE.equalsIgnoreCase(rate2.getShippingType())) {
                        productsWithOutShippingRate.add(it.getBeanProd2(), it.getQuantity());
                    }
                } else {
                    productsWithOutShippingRate.add(it.getBeanProd2(), it.getQuantity());
                }
            }

        }

        if (orderNeedShipping) {
            for (Object o : productsWithFlatRate.keySet()) {
                ShippingRate sr = (ShippingRate) o;
                if (!freeShipping) shippingCost += sr.getValue();
            }
            if (!productsWithOutShippingRate.isEmpty() && cart.getDeliveryAddress() != null && !freeShipping) {
                needToSelectShipping = true;
                if (shippingRates == null && getLiveMethods) {
                    List<RateService> services = doShippingLiveRequest(productsWithOutShippingRate, cart.getDeliveryAddress(), cart.getTotal() + cart.getTotalFees(), action, true);
                    if (services != null && !services.isEmpty()) {
                        if (services.size() == 1) { // Si hay una sola opcion ponerla como shipping seleccionado
                            RateService rs = services.get(0);
                            selected = rs.getMethod();
                            total = shippingCost + rs.getValue();
                        } else { // Si hay mas de una opcion devolver lista de rates
                            shippingRates = new ArrayList<Map<String, Object>>();
                            Double lowerValue = null;
                            ShippingMethod lowerService = null;
                            boolean existeSelected = false; // determina si el metodo seleccionado esta disponible
                            for (RateService rs : services) {
                                // si ya vino uno seleccionado y esta en la lista, actualizar el costo
                                if (selected != null && selected.equals(rs.getMethod())) {
                                    total = shippingCost + rs.getValue();
                                    existeSelected = true;
                                }

                                Map<String, Object> map = new HashMap<String, Object>();
                                map.put("id", rs.getMethod().getId());
                                map.put("carrier", rs.getCarrier());
                                map.put("name", rs.getMethod().getMethodName(action.getLocale().getLanguage()));
                                map.put("value", rs.getValue() + shippingCost);
                                map.put("days", rs.getDaysToDelivery());
                                shippingRates.add(map);

                                // ver cual es el metodo de menor costo
                                if (lowerValue == null || lowerValue > (rs.getValue() + shippingCost)) {
                                    lowerService = rs.getMethod();
                                    lowerValue = rs.getValue() + shippingCost;
                                }
                            }
                            // si el metodo seleccionado no esta entre lso disponibles, quitarlo
                            if (selected != null && !existeSelected) selected = null;
                            // Si no hay metodo seleccionado, seleccionar el de menor costo
                            if (selected == null) {
                                selected = lowerService;
                                total = lowerValue;
                            }
                        }
                    }
                }
            } else {
                // No se necesita hacer request. Mostrar el nombre del default shipping method
                needToSelectShipping = false;
                selected = dao.getDefaultShippingMethod();
                total = shippingCost;
            }
        }
        // sobreescribir con el valor de personalisado
        if (cart.getOverrideShipping() != null) total = cart.getOverrideShipping();

    }

    public List<RateService> doShippingLiveRequest(MapCounter productsWithOutShippingRate, UserAddress shippingAddress, Double insuranceValue, BaseAction action, boolean findInSessionFirst) {
        if (findInSessionFirst && action.getStoreSessionObjects().containsKey(SESSION_RATE_SERVICES)) {
            return (List<RateService>) action.getStoreSessionObjects().get(SESSION_RATE_SERVICES);
        }
        errors = new ArrayList<String>();
        if (productsWithOutShippingRate != null && !productsWithOutShippingRate.isEmpty()) {
            List<BasePackage> listaPaquetes = new ArrayList<BasePackage>();
            for (Object o : productsWithOutShippingRate.keySet()) {
                Integer cant = productsWithOutShippingRate.get(o);
                Product prod = (Product) o;
                Double price = Double.valueOf(prod.getFinalPrice(null, 1));
                ProductVariation pv = null;
                if (prod.getProperty("variation")!=null) {
                    Long varId = (Long) prod.getProperty("variation");
                    pv = (ProductVariation) action.getDao().get(ProductVariation.class, varId);
                }
                if (cant != null)
                    for (int i = 0; i < cant; i++) {
                        BasePackage pck = new BasePackage();
                        Double height = SomeUtils.strToDouble(action.getParentProperty(prod, "dimentionHeight"));
                        if (pv!=null && pv.getDimentionHeight()!=null) height = pv.getDimentionHeight();
                        Double width = SomeUtils.strToDouble(action.getParentProperty(prod, "dimentionWidth"));
                        if (pv!=null && pv.getDimentionWidth()!=null) width = pv.getDimentionWidth();
                        Double length = SomeUtils.strToDouble(action.getParentProperty(prod, "dimentionLength"));
                        if (pv!=null && pv.getDimentionLength()!=null) length = pv.getDimentionLength();
                        Double weight = SomeUtils.strToDouble(action.getParentProperty(prod, "weight"));
                        if (pv!=null && pv.getWeight()!=null) weight = pv.getWeight();
                        if (height != null) pck.setDimensionsHeight(height.floatValue());
                        if (length != null) pck.setDimensionsLength(length.floatValue());
                        if (width != null) pck.setDimensionsWidth(width.floatValue());
                        if (weight != null) pck.setPackageWeight(weight.floatValue());
                        if (price != null) {
                            pck.setInsuredValueMonetaryValue(price.floatValue());
                        }
                        pck.setDescription(prod.getPartNumber());
                        pck.setCurrencyCode(action != null ? action.getDefaultCurrency().getCode() : "USD");
                        pck.setDimensionUnit(action != null ? action.getDimensionUnit() : "");
                        pck.setWeightUnit(action != null ? action.getWeightUnit() : "");
                        listaPaquetes.add(pck);
                    }
            }
            if (action == null) {
                errors.add(CNT_ERROR_FATAL);
            } else if (listaPaquetes.isEmpty()) {
                errors.add(action.getText(CNT_ERROR_NOT_ITEMS, CNT_ERROR_DEFAULT_NOT_ITEMS));
            } else if (shippingAddress == null) {
                errors.add(action.getText(CNT_ERROR_NOT_ADDRESS, CNT_ERROR_DEFAULT_NOT_ADDRESS));
            } else {
                Address address = new Address(shippingAddress.getAddress(), shippingAddress.getAddress2(), shippingAddress.getCity(), shippingAddress.getState().getStateCode(), shippingAddress.getZipCode(), shippingAddress.getIdCountry());
                org.store.core.utils.carriers.StructuredPhoneNumber phone = (shippingAddress.getPhone() != null) ? new org.store.core.utils.carriers.StructuredPhoneNumber(shippingAddress.getPhone(), "-") : null;
                org.store.core.utils.carriers.ShipTo shipTo = new org.store.core.utils.carriers.ShipTo(shippingAddress.getCompany(), shippingAddress.getFullName(), address, phone);
                List<String> carriers = action.getDao().getCarriers(true);
                boolean useDefaultOnly = false;
                if (carriers.isEmpty()) {
                    ShippingMethod defaultMethod = action.getDao().getDefaultShippingMethod();
                    if (defaultMethod != null) {
                        carriers.add(defaultMethod.getCarrierName());
                        useDefaultOnly = true;
                    }
                }

                if (!carriers.isEmpty()) {
                    CarrierUtils carrierUtils = new CarrierUtils(action.getServletContext());
                    List<RateService> serviceList = new ArrayList<RateService>();
                    Map<String, ShippingMethod> mapShippingMethods = action.getDao().getMapShippingMethods();
                    for (String carrier : carriers) {
                        Properties carrierProps = action.getCarrierProperties(carrier);
                        if (carrierProps != null) {
                            org.store.core.utils.carriers.CarrierService cs = carrierUtils.getCarrierService(carrier, carrierProps);
                            if (cs.available(shipTo, listaPaquetes)) {
                                try {
                                    // Hacer la consulta, la hacemos dentro de un try catch, por si una falla no afectar las otras
                                    org.store.core.utils.carriers.RateServiceResponse ratesResponse = cs.getRateServices(shipTo, listaPaquetes);

                                    // Leer respuesta
                                    for (RateService rateService : ratesResponse.getRateServices()) {
                                        ShippingMethod shippingMethod = mapShippingMethods.get(carrier.toUpperCase() + "-" + rateService.getCode());
                                        if (shippingMethod != null && (shippingMethod.getActive() || (useDefaultOnly && shippingMethod.getDefaultMethod()))) {
                                            rateService.setMethod(shippingMethod);
                                            serviceList.add(rateService);
                                        }
                                    }

                                    // Verificar si hay error
                                    if (StringUtils.isNotEmpty(ratesResponse.getErrors())) {
                                        errors.add(carrier.toUpperCase() + ": " + ratesResponse.getErrors());
                                    }
                                } catch (Exception e) {
                                    log.error(e.getMessage(), e);
                                    errors.add(carrier + " " + e.getMessage());
                                }
                            } else {
                                errors.add(carrier + " " + action.getText(CNT_ERROR_NOT_AVAILABLE_FOR_ITEMS, CNT_ERROR_DEFAULT_NOT_AVAILABLE_FOR_ITEMS));
                            }
                        } else {
                            errors.add(carrier + " " + action.getText(CNT_ERROR_CARRIER_NOT_CONFIGURED, CNT_ERROR_DEFAULT_CARRIER_NOT_CONFIGURED));
                        }
                    }
                    action.getStoreSessionObjects().put(SESSION_RATE_SERVICES, serviceList);
                    return serviceList;
                } else {
                    errors.add(action.getText(CNT_ERROR_NOT_ACTIVE_CARRIERS, CNT_ERROR_DEFAULT_NOT_ACTIVE_CARRIERS));
                }
            }
        }
        return null;
    }

    public void removeSessionData(BaseAction action) {
        if (action.getStoreSessionObjects().containsKey(SESSION_RATE_SERVICES))
            action.getStoreSessionObjects().remove(SESSION_RATE_SERVICES);
    }

    public double getTotal() {
        return total;
    }

    public ShippingMethod getSelected() {
        return selected;
    }

    public Boolean getOrderNeedShipping() {
        return orderNeedShipping != null && orderNeedShipping;
    }

    public Boolean getNeedToSelectShipping() {
        return needToSelectShipping != null && needToSelectShipping;
    }

    public List<Map<String, Object>> getShippingRates() {
        return shippingRates;
    }

    public List<String> getErrors() {
        return errors;
    }

    public class MapCounter extends HashMap<Object, Integer> {
        public void add(Object o, Integer c) {
            if (o != null && c != null) {
                if (containsKey(o)) {
                    Integer oldC = get(o);
                    put(o, (oldC != null) ? oldC + c : c);
                } else {
                    put(o, c);
                }
            }
        }

    }

}
