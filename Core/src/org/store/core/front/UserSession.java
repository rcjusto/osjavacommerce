package org.store.core.front;

import org.store.core.beans.*;
import org.store.core.globals.BaseAction;
import org.store.core.globals.SomeUtils;
import org.store.core.globals.StoreMessages;
import org.store.core.utils.carriers.Address;
import org.store.core.utils.carriers.BasePackage;
import org.store.core.utils.carriers.CarrierUtils;
import org.store.core.utils.carriers.RateService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.log4j.Logger;
import org.apache.struts2.ServletActionContext;
import org.apache.struts2.json.JSONException;
import org.apache.struts2.json.JSONUtil;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;


public class UserSession implements Serializable, StoreMessages {

    public static Logger log = Logger.getLogger(UserSession.class);
    private BaseAction action;
    private ShopCart shoppingCart;

    private String shippingType;
    private String shippingDate;
    private Double shippingValue;
    private Double shippingInsurance;
    private Boolean useRewards;
    private List<Map<String, Object>> shippingRates;


    public UserSession(BaseAction baseAction) {
        this.action = baseAction;
        this.shoppingCart = new ShopCart();
    }

    public UserSession(BaseAction baseAction, String jsonStr) {
        this.action = baseAction;
        try {
            deserialize(jsonStr);
        } catch (JSONException e) {
            log.error(e.getMessage(), e);
        }
    }


    public ShopCart getShoppingCart() {
        if (shoppingCart == null) shoppingCart = new ShopCart();
        return shoppingCart;
    }

    public void setShoppingCart(ShopCart shoppingCart) {
        this.shoppingCart = shoppingCart;
    }

    public List<Map<String, Object>> getShippingRates() {
        return shippingRates;
    }

    public void setShippingRates(List<Map<String, Object>> shippingRates) {
        this.shippingRates = shippingRates;
    }

    public Boolean getUseRewards() {
        return useRewards != null && useRewards;
    }

    public void setUseRewards(Boolean useRewards) {
        this.useRewards = useRewards;
    }

    public String getShippingType() {
        return (StringUtils.isNotEmpty(shippingType)) ? shippingType : "billing";
    }

    public void setShippingType(String shippingType) {
        if (!getShippingType().equalsIgnoreCase(shippingType)) resetShipping();
        this.shippingType = shippingType;
    }

    public UserAddress getBillingAddress() {
        return getShoppingCart().getBillingAddress();
    }

    public void setBillingAddress(UserAddress billingAddress) {
        getShoppingCart().setBillingAddress(billingAddress);
    }

    public UserAddress getShippingAddress() {
        return ("shipping".equalsIgnoreCase(getShippingType())) ? getShoppingCart().getDeliveryAddress() : getShoppingCart().getBillingAddress();
    }

    public void setShippingAddress(UserAddress shippingAddress) {
        getShoppingCart().setDeliveryAddress(shippingAddress);
    }

    public ShippingMethod getShippingMethod() {
        return getShoppingCart().getShippingMethod();
    }

    public LocationStore getShippingStore() {
        return getShoppingCart().getPickInStore();
    }

    public void setShippingStore(LocationStore shippingStore) {
        getShoppingCart().setPickInStore(shippingStore);
    }

    public String getShippingDate() {
        return shippingDate;
    }

    public Double getShippingValue() {
        return shippingValue;
    }

    public void setShippingMethod(ShippingMethod shippingMethod) {
        getShoppingCart().setShippingMethod(shippingMethod);
    }

    public void setShippingDate(String shippingDate) {
        this.shippingDate = shippingDate;
    }

    public void setShippingValue(Double shippingValue) {
        this.shippingValue = shippingValue;
    }

    public Double getShippingInsurance() {
        return shippingInsurance;
    }

    public void setShippingInsurance(Double shippingInsurance) {
        this.shippingInsurance = shippingInsurance;
    }

    /**
     * Devuelve la lista de elementos del carro de compras
     *
     * @return
     */
    public List<ShopCartItem> getItems() {
        return getShoppingCart().getItems();
    }

    /**
     * Adiciona un producto al carro de compras
     *
     * @param idProduct1
     * @param variation1
     * @param quantity
     */
    public void addItem(Long idProduct1, Long variation1, Integer quantity) {
        addItem(idProduct1, variation1, null, null, quantity, null, null, null, null, true);
    }

    /**
     * Adiciona un producto al carro de compras
     *
     * @param idProduct1
     * @param variation1
     * @param idProduct2
     * @param variation2
     * @param quantity
     * @param selDate
     * @param selTime
     * @param price
     * @param oriPrice
     * @param initialize
     */
    public void addItem(Long idProduct1, Long variation1, Long idProduct2, Long variation2, Integer quantity, String selDate, String selTime, Double price, Double oriPrice, boolean initialize) {
        ShopCartItem item = findItem(idProduct1, variation1, idProduct2, variation2, selDate, selTime);
        if (item == null) {
            item = new ShopCartItem(getShoppingCart());
            item.setProduct1(idProduct1);
            item.setVariation1(variation1);
            item.setProduct2(idProduct2);
            item.setVariation2(variation2);
            item.setQuantity(quantity);
            item.setSelDate(selDate);
            item.setSelTime(selTime);
            item.setPrice(price);
            item.setPriceOriginal(oriPrice);
            if (initialize) inicializeItem(item);
            getShoppingCart().addItem(item);
        } else {
            item.addQuantity(quantity);
        }
        resetShipping();
    }

    /**
     * Busca si existe un producto en el carro de compras
     *
     * @param idProduct1
     * @param variation1
     * @param idProduct2
     * @param variation2
     * @param selDate
     * @param selTime
     * @return
     */
    public ShopCartItem findItem(Long idProduct1, Long variation1, Long idProduct2, Long variation2, String selDate, String selTime) {
        if (!getShoppingCart().getItems().isEmpty()) {
            for (ShopCartItem it : getShoppingCart().getItems()) {
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

    /**
     * Inicializa los elementos del carro del compras.
     */
    public void initializeItems() {
        if (!getShoppingCart().getItems().isEmpty()) {
            for (ShopCartItem item : getShoppingCart().getItems()) {
                item.setShopCart(getShoppingCart());
                inicializeItem(item);
            }
        }
    }

    /**
     * Devuelve un elemento del carro de compras
     *
     * @param index Indice del elemento a devolver
     * @return
     */
    public ShopCartItem getItem(int index) {
        return (index < getShoppingCart().getItems().size()) ? getShoppingCart().getItems().get(index) : null;
    }

    /**
     * Devuelve la cantidad de un producto en el carro de compras
     *
     * @param id Identificador del producto
     * @return
     */
    public int getNumProduct(Long id) {
        return getShoppingCart().getNumProduct(id);
    }

    /**
     * Elimina un elemento del carro de compras
     *
     * @param index Indice del elemento a eliminar
     */
    public void delItem(int index) {
        if (index < getShoppingCart().getItems().size()) {
            getShoppingCart().getItems().remove(index);
            resetShipping();
        }
    }

    /**
     * Devuelve el total de carro de compras incluyendo las promociones seleccionadas
     *
     * @return
     */
    public double getTotal() {
        return getTotal(true);
    }

    /**
     * Devuelve el total del carro de compras
     *
     * @param includePromotions Incluir promociones en calculo del total
     * @return
     */
    public double getTotal(boolean includePromotions) {
        double res = 0.0d;
        if (!getShoppingCart().getItems().isEmpty()) {
            for (ShopCartItem it : getShoppingCart().getItems()) {
                Double subtotal = it.getSubtotal();
                if (subtotal != null) res += subtotal;
            }
        }
        double subtotalProd = res;
        if (includePromotions && getShoppingCart().getPromotionalCodes() != null && getShoppingCart().getPromotionalCodes().length > 0) {
            for (String code : getShoppingCart().getPromotionalCodes()) {
                PromotionalCode pc = action.getDao().getPromotionalCode(code);
                if (pc != null) {
                    if (pc.getDiscount() != null && pc.getDiscount() > 0) {
                        res -= couponUsed(pc);
                    } else if (pc.getDiscountPercent() != null && pc.getDiscountPercent() > 0) {
                        res -= BigDecimal.valueOf(pc.getDiscountPercent() * subtotalProd).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
                    }
                }
            }
        }
        return res;
    }

    /**
     * Reinicia el carro de compra y todas las direcciones
     */
    public void resetAll() {
        shoppingCart = null;
        useRewards = false;
        resetShipping();
    }

    /**
     * Reinicia datos relacionados con las direcciones
     */
    public void resetShipping() {
        getShoppingCart().setDeliveryAddress(null);
        getShoppingCart().setShippingMethod(null);
        getShoppingCart().setPickInStore(null);
        shippingDate = null;
        shippingValue = null;
        shippingRates = null;
        shippingType = null;
        shippingInsurance = null;
        FrontModuleAction action = (ServletActionContext.getContext().getActionInvocation().getAction() instanceof FrontModuleAction) ? (FrontModuleAction) ServletActionContext.getContext().getActionInvocation().getAction() : null;
        if (action != null) action.updateShoppingCartInSession();
    }

    /**
     * Devuelve un codigo promocional seleccionado
     *
     * @param index
     * @return
     */
    public String getPromotionalCode(int index) {
        return (getShoppingCart().getPromotionalCodes() != null && getShoppingCart().getPromotionalCodes().length > index) ? getShoppingCart().getPromotionalCodes()[index] : null;
    }

    /**
     * Serializa el carro de compras para salvarlo en la session
     *
     * @return
     * @throws JSONException
     */
    public String serialize() throws JSONException {
        Map<String, Object> data = new HashMap<String, Object>();

        // tratar de salvar el carro de compras para obtener el id
        if (shoppingCart != null && !shoppingCart.getItems().isEmpty()) {
            if (shoppingCart.getUser() == null && action.getFrontUser() != null) shoppingCart.setUser(action.getFrontUser());
            if (StringUtils.isEmpty(shoppingCart.getStatus()) && "Y".equalsIgnoreCase(action.getStoreProperty(StoreProperty.PROP_SHOPCART_AUTO_SAVE, ""))) shoppingCart.setStatus(ShopCart.STATUS_SAVED);
            action.getDao().save(shoppingCart);
            data.put("cart", shoppingCart.getId().toString());
        }

        data.put("rewards", getUseRewards() ? "yes" : "no");
        if (StringUtils.isNotEmpty(shippingType)) data.put("shippingType", shippingType);
        if (shippingDate != null) data.put("shippingDate", shippingDate);
        if (shippingValue != null) data.put("shippingValue", shippingValue.toString());
        if (shippingInsurance != null) data.put("shippingInsurance", shippingInsurance.toString());
        if (shippingRates != null && shippingRates.size() > 0) data.put("shippingRates", shippingRates);
        if (getShoppingCart().getPromotionalCodes() != null && getShoppingCart().getPromotionalCodes().length > 0) {
            List<String> codes = new ArrayList<String>();
            for (String code : getShoppingCart().getPromotionalCodes()) codes.add(code);
            data.put("promotionalCodes", codes);
        }
        return JSONUtil.serialize(data);
    }

    /**
     * Deserializa el carro de compras salvado en la session
     *
     * @param str
     * @throws JSONException
     */
    public void deserialize(String str) throws JSONException {
        Map data = (Map) JSONUtil.deserialize(str);

        if (data.containsKey("cart")) {
            shoppingCart = (ShopCart) action.getDao().get(ShopCart.class, SomeUtils.strToLong((String) data.get("cart")));
            initializeItems();
        }

        BaseAction action = (ServletActionContext.getContext().getActionInvocation().getAction() instanceof BaseAction) ? (BaseAction) ServletActionContext.getContext().getActionInvocation().getAction() : null;
        shippingType = data.containsKey("shippingType") ? (String) data.get("shippingType") : null;
        shippingDate = data.containsKey("shippingDate") ? (String) data.get("shippingDate") : null;
        shippingValue = data.containsKey("shippingValue") ? SomeUtils.strToDouble((String) data.get("shippingValue")) : null;
        shippingInsurance = data.containsKey("shippingInsurance") ? SomeUtils.strToDouble((String) data.get("shippingInsurance")) : null;
        shippingRates = (data.containsKey("shippingRates")) ? (List<Map<String, Object>>) data.get("shippingRates") : null;
        useRewards = (data.containsKey("rewards") && "yes".equalsIgnoreCase((String) data.get("rewards")));
        List<String> codes = (data.containsKey("promotionalCodes")) ? (List<String>) data.get("promotionalCodes") : null;
        if (codes != null && !codes.isEmpty()) {
            for (String code : codes) {
                PromotionalCode pCode = action.getDao().getPromotionalCode(code);
                if (pCode != null) {
                    if (pCode.isValid(this)) {
                        if (!getShoppingCart().hasPromotionalCode(code)) getShoppingCart().addPromotionalCode(code);
                    } else {
                        action.addFieldError("promotions", action.getText(GeneralAction.CNT_ERROR_CANNOT_APPLY_PROMOTION, GeneralAction.CNT_DEFAULT_ERROR_CANNOT_APPLY_PROMOTION, new String[]{code}));
                    }
                } else {
                    action.addFieldError("promotions", action.getText(GeneralAction.CNT_ERROR_INVALID_PROMOTIONAL_CODE, GeneralAction.CNT_DEFAULT_ERROR_INVALID_PROMOTIONAL_CODE, new String[]{code}));
                }
            }
        }
    }

    /**
     * Inicializa un elemento del carro de compras
     * Actualiza el bean de producto, nombre, precio, etc
     *
     * @param item
     */
    public void inicializeItem(ShopCartItem item) {
        BaseAction action = (ServletActionContext.getContext().getActionInvocation().getAction() instanceof BaseAction) ? (BaseAction) ServletActionContext.getContext().getActionInvocation().getAction() : null;
        if (item != null && action != null) {
            item.initializeItem(action);
        }
    }

    /**
     * Calcula todos los costos relacionados con el carro de compras
     * 1. Promotional codes
     * 2. Product fees
     * 3. Shipping
     * 4. Taxes
     * 5. Reward use
     *
     * @return Devuelve un mapa con todos los costos
     */
    public Map getPriceMap() {
        Map<String, Object> res = null;
        try {
            res = new HashMap<String, Object>();
            FrontModuleAction action = (ServletActionContext.getContext().getActionInvocation().getAction() instanceof FrontModuleAction) ? (FrontModuleAction) ServletActionContext.getContext().getActionInvocation().getAction() : null;
            Integer maxFlatRate = (action != null) ? SomeUtils.strToInteger(action.getDao().getStorePropertyValue(StoreProperty.PROP_FLAT_RATE_MAX, StoreProperty.TYPE_GENERAL, StoreProperty.PROP_DEFAULT_FLAT_RATE_MAX)) : 100;

            //Promotional Codes
            boolean freeShippingOrder = false;
            if (getShoppingCart().getPromotionalCodes() != null && getShoppingCart().getPromotionalCodes().length > 0) {
                for (String code : getShoppingCart().getPromotionalCodes()) {
                    PromotionalCode pc = action.getDao().getPromotionalCode(code);
                    if (pc != null && pc.isValid(this)) {
                        if (pc.getFreeShipping()) freeShippingOrder = true;
                    }
                }
            }
            if ("pickinstore".equalsIgnoreCase(getShippingType())) freeShippingOrder = true;

            // Calcular subtotal productos y asignar fees
            if (!getShoppingCart().getItems().isEmpty()) {
                double grandTotal = 0.0;
                double subtotalProd = 0.0;
                double shipping = 0.0;
                MapCounter productsWithOutShippingRate = new MapCounter();
                MapCounter productsWithFlatRate = new MapCounter();
                for (ShopCartItem it : getShoppingCart().getItems()) {
                    if (it.getVariation1()!=null) it.getBeanProd1().addProperty("variation",it.getVariation1());
                    if (it.getBeanProd2()!=null && it.getVariation2()!=null) it.getBeanProd2().addProperty("variation",it.getVariation2());
                    subtotalProd += it.getSubtotal();
                    if (getShippingAddress() != null && action != null) {
                        // Calcular Fees
                        it.getFees().clear();
                        List<CategoryFee> lista = new ArrayList<CategoryFee>();
                        List<CategoryFee> l1 = (it.getBeanProd1() != null) ? action.getDao().getParentCategoryFee(it.getBeanProd1().getCategory(), getShippingAddress().getState()) : null;
                        if (CollectionUtils.isEmpty(l1)) l1 = (it.getBeanProd1() != null) ? action.getDao().getParentCategoryFee(it.getBeanProd1().getCategory(), getShippingAddress().getState().getCountryCode()) : null;
                        if (CollectionUtils.isNotEmpty(l1)) lista.addAll(l1);
                        List<CategoryFee> l2 = (it.getBeanProd2() != null) ? action.getDao().getParentCategoryFee(it.getBeanProd2().getCategory(), getShippingAddress().getState()) : null;
                        if (CollectionUtils.isEmpty(l2)) l2 = (it.getBeanProd2() != null) ? action.getDao().getParentCategoryFee(it.getBeanProd2().getCategory(), getShippingAddress().getState().getCountryCode()) : null;
                        if (CollectionUtils.isNotEmpty(l2)) lista.addAll(l2);

                        if (lista.size() > 0) {
                            for (CategoryFee f : lista)
                                if (action.getFrontUser() == null || !action.getFrontUser().hasFeeExemption(f.getFee().getId())) {
                                    it.addFee(f);
                                    subtotalProd += f.getTotal(it) * it.getQuantity();
                                }
                        }
                        // Calcular Fixed Shipping
                        if (it.getBeanProd1() != null && action.getDao().productNeedShipping(it.getBeanProd1()) && !it.getBeanProd1().hasFreeShipping()) {
                            ShippingRate rate1 = action.getDao().getShippingRate(it.getBeanProd1(), getShippingAddress().getState());
                            if (rate1 != null) {
                                if (ShippingRate.TYPE_FIXED.equalsIgnoreCase(rate1.getShippingType())) {
                                    if (!freeShippingOrder) shipping += rate1.getValue() * it.getQuantity();
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
                        if (it.getBeanProd2() != null && action.getDao().productNeedShipping(it.getBeanProd2()) && !it.getBeanProd2().hasFreeShipping()) {
                            ShippingRate rate2 = action.getDao().getShippingRate(it.getBeanProd2(), getShippingAddress().getState());
                            if (rate2 != null) {
                                if (ShippingRate.TYPE_FIXED.equalsIgnoreCase(rate2.getShippingType())) {
                                    if (!freeShippingOrder) shipping += rate2.getValue() * it.getQuantity();
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
                }
                res.put("subtotal", subtotalProd);
                grandTotal += subtotalProd;

                // Promotions
                double subtotalProm = 0.0d;
                if (getShoppingCart().getPromotionalCodes() != null && getShoppingCart().getPromotionalCodes().length > 0) {
                    List<Map<String, Object>> promos = new ArrayList<Map<String, Object>>();
                    for (String code : getShoppingCart().getPromotionalCodes()) {
                        PromotionalCode pc = action.getDao().getPromotionalCode(code);
                        if (pc != null) {
                            Map<String, Object> map = new HashMap<String, Object>();
                            map.put("code", pc.getCode());
                            map.put("name", pc.getName(action.getLocale().getLanguage()));
                            double value = 0.0d;
                            if (pc.getFreeShipping()) {
                                map.put("type", "free-shipping");
                            } else if (pc.getFreeProduct() != null) {
                                map.put("type", "product");
                                map.put("data", pc.getFreeProduct().getIdProduct());
                            } else if (pc.getDiscount() != null && pc.getDiscount() > 0) {
                                map.put("type", "discount");
                                value -= couponUsed(pc);
                            } else if (pc.getDiscountPercent() != null && pc.getDiscountPercent() > 0) {
                                map.put("type", "discount-percent");
                                map.put("data", pc.getDiscountPercent());
                                value -= BigDecimal.valueOf(pc.getDiscountPercent() * subtotalProd).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
                            }
                            subtotalProm += value;
                            map.put("value", value);
                            promos.add(map);
                        }
                    }
                    if (!promos.isEmpty()) {
                        res.put("promotions", promos);
                    }
                    grandTotal += subtotalProm;
                }

                // Shipping
                if (getShoppingCart().getShippingMethod() == null || shippingValue == null) {
                    for (Object o : productsWithFlatRate.keySet()) {
                        ShippingRate sr = (ShippingRate) o;
                        if (!freeShippingOrder) shipping += sr.getValue();
                    }
                    if (!productsWithOutShippingRate.isEmpty() && getShippingAddress() != null && !freeShippingOrder) {
                        res.put("request_shipping", productsWithOutShippingRate);
                        if (shippingRates == null) {
                            Map<String, Object> requestMap = doShippingLiveRequest(productsWithOutShippingRate, getShippingAddress());
                            if (requestMap.containsKey("serviceList")) {
                                List<RateService> services = (List<RateService>) requestMap.get("serviceList");
                                if (services != null && !services.isEmpty()) {
                                    if (services.size() == 1) {
                                        RateService selectedService = services.get(0);
                                        shipping += selectedService.getValue();
                                        getShoppingCart().setShippingMethod(selectedService.getMethod());
                                        shippingValue = shipping;
                                        shippingDate = selectedService.getDaysToDelivery();
                                        res.put("shippingService", selectedService);
                                        action.updateShoppingCartInSession();
                                    } else {
                                        shippingRates = new ArrayList<Map<String, Object>>();
                                        for (RateService rs : services) {
                                            Map<String, Object> map = new HashMap<String, Object>();
                                            map.put("id", rs.getMethod().getId());
                                            map.put("carrier", rs.getCarrier());
                                            map.put("name", rs.getMethod().getMethodName(action.getLocale().getLanguage()));
                                            map.put("value", rs.getValue() + shipping);
                                            map.put("days", rs.getDaysToDelivery());
                                            shippingRates.add(map);
                                        }
                                        action.updateShoppingCartInSession();
                                    }
                                }
                            }
                            if (requestMap.containsKey("errors")) {
                                res.put("shippingErrors", requestMap.get("errors"));
                            }
                        }
                        if (shippingRates != null) res.put("services", shippingRates);
                    } else if (getShippingAddress() != null) {
                        // No se necesita hacer request. Mostrar el nombre del default shipping method
                        getShoppingCart().setShippingMethod(action.getDao().getDefaultShippingMethod());
                        shippingValue = shipping;
                        action.updateShoppingCartInSession();
                    }
                } else {
                    if (!freeShippingOrder) shipping = shippingValue;
                }

                // Si ya hay shipping seleccionado, entonces calcular lo demas
                res.put("shipping", shipping);
                grandTotal += shipping;

                // Shipping insurance
                if (shippingInsurance != null && shippingInsurance > 0) {
                    res.put("insurance", shippingInsurance);
                    grandTotal += shippingInsurance;
                }

                // Tax especifico por productos (SAGE)
                List<Map<String, Object>> taxList = new ArrayList<Map<String, Object>>();
                if ("Y".equalsIgnoreCase(action.getStoreProperty(StoreProperty.PROP_USE_TAX_PER_PRODUCT, StoreProperty.PROP_DEFAULT_USE_TAX_PER_PRODUCT)) && action.getFrontUser() != null && StringUtils.isNotEmpty(action.getFrontUser().getAltCategory())) {
                    Map<TaxPerFamily, Double> taxMap = new HashMap<TaxPerFamily, Double>();
                    for (ShopCartItem it : getShoppingCart().getItems()) {
                        if (it.getBeanProd1() != null && StringUtils.isNotEmpty(it.getBeanProd1().getAltCategory())) {
                            ProductUserTax puTax = action.getDao().getProductUserTaxes(it.getBeanProd1().getAltCategory(), action.getFrontUser().getAltCategory());
                            if (puTax != null) {
                                for (TaxPerFamily tpf : puTax.getTaxes()) {
                                    Double old = taxMap.containsKey(tpf) ? taxMap.get(tpf) : 0.0d;
                                    taxMap.put(tpf, old + it.getPrice() * it.getQuantity());
                                }
                            }
                        }
                        if (it.getBeanProd2() != null && StringUtils.isNotEmpty(it.getBeanProd2().getAltCategory())) {
                            ProductUserTax puTax = action.getDao().getProductUserTaxes(it.getBeanProd2().getAltCategory(), action.getFrontUser().getAltCategory());
                            if (puTax != null) {
                                for (TaxPerFamily tpf : puTax.getTaxes()) {
                                    Double old = taxMap.containsKey(tpf) ? taxMap.get(tpf) : 0.0d;
                                    taxMap.put(tpf, old + it.getPrice() * it.getQuantity());
                                }
                            }
                        }
                    }
                    if (!taxMap.isEmpty()) {
                        for (TaxPerFamily tpf : taxMap.keySet()) {
                            Double toTax = taxMap.get(tpf);
                            if (toTax != null && tpf.getValue() != null) {
                                double taxApplied = BigDecimal.valueOf(toTax * tpf.getValue()).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
                                grandTotal += taxApplied;
                                Map<String, Object> map = new HashMap<String, Object>();
                                map.put("name", tpf.getTaxName());
                                map.put("percent", tpf.getValue());
                                map.put("totax", toTax);
                                map.put("value", taxApplied);
                                taxList.add(map);
                            }
                        }
                    }
                }

                // Taxes
                List<Tax> taxes = (getShippingAddress() != null) ? action.getDao().getTaxes(getShippingAddress().getIdCountry(), getShippingAddress().getState()) : null;
                if (taxes != null && taxes.size() > 0) {
                    double previousTaxes = 0.00;
                    Double amountToTax = null;
                    Map<String, Double> taxMap = new HashMap<String, Double>();
                    for (Tax tax : taxes)
                        if (action.getFrontUser() == null || !action.getFrontUser().hasTaxExemption(tax.getId())) {
                            double toTax = subtotalProd + subtotalProm;
                            if (tax.getIncludeShippping()) toTax += shipping;
                            if (shippingInsurance != null && shippingInsurance > 0) toTax += shippingInsurance;
                            if (amountToTax == null) amountToTax = toTax;
                            if (tax.getIncludeTaxes()) toTax += previousTaxes;
                            double taxApplied = BigDecimal.valueOf(tax.getValue() * toTax).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
                            taxMap.put(tax.getTaxName(), taxApplied);
                            previousTaxes += taxApplied;
                            grandTotal += taxApplied;
                            Map<String, Object> map = new HashMap<String, Object>();
                            map.put("name", tax.getTaxName());
                            map.put("percent", tax.getValue());
                            map.put("totax", toTax);
                            map.put("value", taxApplied);
                            taxList.add(map);
                        }
                    res.put("taxes", taxMap);
                    res.put("toTax", amountToTax);
                }
                res.put("taxInfo", taxList);

                // rewards
                Boolean rewardsEnabled = (Boolean) action.getRequest().getAttribute("rewardsEnabled");
                if (rewardsEnabled != null && rewardsEnabled && getUseRewards() && action.getFrontUser() != null && action.getFrontUser().getRewardPoints() > 0) {
                    Number rewardsRate = (Number) action.getRequest().getAttribute("rewardsRate");
                    if (rewardsRate != null && rewardsRate.doubleValue() > 0) {
                        Double rewardsMoney = (rewardsRate != null) ? action.getFrontUser().getRewardPoints() * rewardsRate.doubleValue() : null;
                        if (rewardsMoney != null && rewardsMoney > 0) {
                            double rewardsToUse = Math.min(grandTotal, rewardsMoney);
                            int pointsToUse = (int) Math.min(Math.ceil(rewardsToUse / rewardsRate.doubleValue()), action.getFrontUser().getRewardPoints());
                            grandTotal -= rewardsToUse;
                            res.put("usedRewards", rewardsToUse);
                            res.put("usedRewardsPoints", pointsToUse);
                            res.put("restRewardsPoints", action.getFrontUser().getRewardPoints() - pointsToUse);
                        }
                    }
                }

                res.put("total", grandTotal);

            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        return res;
    }

    public double couponUsed(PromotionalCode pc) {
        double res = 0.0d;
        if (pc.getDiscount() != null && pc.getDiscount() > 0 && !getShoppingCart().getItems().isEmpty()) {
            for (ShopCartItem it : getShoppingCart().getItems()) {
                if (it.getBeanProd1() != null && pc.canApplyTo(it.getBeanProd1())) {
                    if (pc.getDiscount() > res) {
                        res += Math.min(pc.getDiscount() - res, it.getPrice());
                    }
                }
            }
        }
        return res;
    }

    public boolean needShipping() {
        BaseAction action = (ServletActionContext.getContext().getActionInvocation().getAction() instanceof BaseAction) ? (BaseAction) ServletActionContext.getContext().getActionInvocation().getAction() : null;
        if (!getShoppingCart().getItems().isEmpty() && action != null) {
            for (ShopCartItem it : getShoppingCart().getItems()) {
                if (it.getBeanProd1() != null && action.getDao().productNeedShipping(it.getBeanProd1())) return true;
                if (it.getBeanProd1() != null && action.getDao().productNeedShipping(it.getBeanProd2())) return true;
            }
        }
        return false;
    }

    public List<Map<String, Object>> getPromotionsByType(List<Map<String, Object>> l, String type) {
        if (l == null) return null;
        List<Map<String, Object>> res = new ArrayList<Map<String, Object>>();
        for (Map<String, Object> map : l) if (type.equalsIgnoreCase((String) map.get("type"))) res.add(map);
        return res;
    }

    public double calculateDeliveryCostForLocation(List<ShopCartItem> listItems, State state, String city, String zipCode) {
        BaseAction action = (ServletActionContext.getContext().getActionInvocation().getAction() instanceof BaseAction) ? (BaseAction) ServletActionContext.getContext().getActionInvocation().getAction() : null;
        Integer maxFlatRate = (action != null) ? SomeUtils.strToInteger(action.getDao().getStorePropertyValue(StoreProperty.PROP_FLAT_RATE_MAX, StoreProperty.TYPE_GENERAL, StoreProperty.PROP_DEFAULT_FLAT_RATE_MAX)) : 100;

        double shipping = 0.0;
        boolean freeShippingOrder = false;
        // Verificar si la orden tiene freeshipping
        if (action != null && getShoppingCart().getPromotionalCodes() != null)
            for (String code : getShoppingCart().getPromotionalCodes()) {
                PromotionalCode pc = action.getDao().getPromotionalCode(code);
                if (pc != null && pc.isValid(this) && pc.getFreeShipping()) return shipping;
            }

        if (listItems != null && listItems.size() > 0) {
            MapCounter productsWithOutShippingRate = new MapCounter();
            MapCounter productsWithFlatRate = new MapCounter();
            for (ShopCartItem it : listItems) {
                if (action != null) {

                    if (it.getVariation1()!=null) it.getBeanProd1().addProperty("variation",it.getVariation1());
                    if (it.getBeanProd2()!=null && it.getVariation2()!=null) it.getBeanProd2().addProperty("variation",it.getVariation2());

                    // Calcular Fixed Shipping
                    if (it.getBeanProd1() != null && action.getDao().productNeedShipping(it.getBeanProd1()) && !it.getBeanProd1().hasFreeShipping()) {
                        ShippingRate rate1 = action.getDao().getShippingRate(it.getBeanProd1(), state);
                        if (rate1 != null) {
                            if (ShippingRate.TYPE_FIXED.equalsIgnoreCase(rate1.getShippingType())) {
                                if (!freeShippingOrder) shipping += rate1.getValue() * it.getQuantity();
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
                    if (it.getBeanProd2() != null && action.getDao().productNeedShipping(it.getBeanProd2()) && !it.getBeanProd2().hasFreeShipping()) {
                        ShippingRate rate2 = action.getDao().getShippingRate(it.getBeanProd2(), state);
                        if (rate2 != null) {
                            if (ShippingRate.TYPE_FIXED.equalsIgnoreCase(rate2.getShippingType())) {
                                if (!freeShippingOrder) shipping += rate2.getValue() * it.getQuantity();
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
            }

            // Add Flat Rates
            for (Object o : productsWithFlatRate.keySet()) {
                ShippingRate sr = (ShippingRate) o;
                if (!freeShippingOrder) shipping += sr.getValue();
            }

            // Request Live Rates
            if (!productsWithOutShippingRate.isEmpty()) {
                UserAddress tmpAdd = new UserAddress();
                tmpAdd.setState(state);
                tmpAdd.setCity(city);
                tmpAdd.setIdCountry(state.getCountryCode());
                tmpAdd.setZipCode(zipCode);
                Map<String, Object> requestMap = doShippingLiveRequest(productsWithOutShippingRate, tmpAdd);
                if (requestMap.containsKey("serviceList")) {
                    List<RateService> services = (List<RateService>) requestMap.get("serviceList");
                    if (services != null && !services.isEmpty()) {
                        if (services.size() == 1) {
                            RateService selectedService = services.get(0);
                            shipping += selectedService.getValue();
                        } else {
                            // Lower value
                            Float lowerValue = null;
                            for (RateService rs : services) {
                                if (lowerValue == null) lowerValue = rs.getValue();
                                else if (lowerValue > rs.getValue()) lowerValue = rs.getValue();
                            }
                            if (lowerValue != null) shipping += lowerValue;
                        }
                    }
                }
            }
        }
        return shipping;
    }

    public Map<String, Object> doShippingLiveRequest(MapCounter productsWithOutShippingRate, UserAddress shippingAddress) {
        Map<String, Object> res = new HashMap<String, Object>();
        List<String> errors = new ArrayList<String>();
        if (productsWithOutShippingRate != null && !productsWithOutShippingRate.isEmpty()) {
            BaseAction action = (ServletActionContext.getContext().getActionInvocation().getAction() instanceof BaseAction) ? (BaseAction) ServletActionContext.getContext().getActionInvocation().getAction() : null;
            List<BasePackage> listaPaquetes = new ArrayList<BasePackage>();
            if (action != null) {
                for (Object o : productsWithOutShippingRate.keySet()) {
                    Integer cant = productsWithOutShippingRate.get(o);
                    Product prod = (Product) o;

                    ProductVariation pv = null;
                    if (prod.getProperty("variation")!=null) {
                        Long varId = (Long) prod.getProperty("variation");
                        pv = (ProductVariation) action.getDao().get(ProductVariation.class, varId);
                    }

                    Double price = prod.getFinalPrice(action.getFrontUserLevel(), 1);
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
                            if (price != null) pck.setInsuredValueMonetaryValue(price.floatValue());
                            pck.setDescription(prod.getPartNumber());
                            pck.setCurrencyCode(action.getDefaultCurrency().getCode());
                            pck.setDimensionUnit(action.getDimensionUnit());
                            pck.setWeightUnit(action.getWeightUnit());
                            listaPaquetes.add(pck);
                        }
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
                                // Hacer la consulta
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
                            } else {
                                errors.add(carrier + " " + action.getText(CNT_ERROR_NOT_AVAILABLE_FOR_ITEMS, CNT_ERROR_DEFAULT_NOT_AVAILABLE_FOR_ITEMS));
                            }
                        } else {
                            errors.add(carrier + " " + action.getText(CNT_ERROR_CARRIER_NOT_CONFIGURED, CNT_ERROR_DEFAULT_CARRIER_NOT_CONFIGURED));
                        }
                    }
                    res.put("serviceList", serviceList);
                } else {
                    errors.add(action.getText(CNT_ERROR_NOT_ACTIVE_CARRIERS, CNT_ERROR_DEFAULT_NOT_ACTIVE_CARRIERS));
                }
            }
        }
        if (!errors.isEmpty()) res.put("errors", errors);
        return res;
    }

    public String getReadyToPay() {
        BaseAction action = (ServletActionContext.getContext().getActionInvocation().getAction() instanceof BaseAction) ? (BaseAction) ServletActionContext.getContext().getActionInvocation().getAction() : null;
        if (getShoppingCart().getItems().isEmpty()) return action.getText(CNT_MSG_CART_EMPTY, CNT_DEFAULT_MSG_CART_EMPTY);
        if (getBillingAddress() == null) return action.getText(CNT_MSG_SELECT_BILLING_ADDRESS, CNT_DEFAULT_MSG_SELECT_BILLING_ADDRESS);
        if (needShipping()) {
            if ("pickinstore".equalsIgnoreCase(getShippingType()) && getShippingStore() == null) return action.getText(CNT_MSG_SELECT_PICKINSTORE, CNT_DEFAULT_MSG_SELECT_PICKINSTORE);
            if (!"pickinstore".equalsIgnoreCase(getShippingType()) && getShippingAddress() == null) return action.getText(CNT_MSG_SELECT_SHIPPING_ADDRESS, CNT_DEFAULT_MSG_SELECT_SHIPPING_ADDRESS);
            if (!"pickinstore".equalsIgnoreCase(getShippingType()) && getShippingMethod() == null) return action.getText(CNT_MSG_SELECT_SHIPPING_METHOD, CNT_DEFAULT_MSG_SELECT_SHIPPING_METHOD);
        }
        return null;
    }

    public boolean hasProduct(Product productInCart) {
        if (productInCart != null && !getShoppingCart().getItems().isEmpty()) {
            for (ShopCartItem item : getShoppingCart().getItems()) {
                if (item.getBeanProd1() != null && productInCart.equals(item.getBeanProd1())) return true;
                if (item.getBeanProd2() != null && productInCart.equals(item.getBeanProd2())) return true;
            }
        }
        return false;
    }

    public boolean hasCategory(Category categoryInCart) {
        if (categoryInCart != null && !getShoppingCart().getItems().isEmpty()) {
            for (ShopCartItem item : getShoppingCart().getItems()) {
                if (item.getBeanProd1() != null && (item.getBeanProd1().belongToCategory(categoryInCart))) return true;
                if (item.getBeanProd2() != null && (item.getBeanProd2().belongToCategory(categoryInCart))) return true;
            }
        }
        return false;
    }

    public boolean hasManufacter(Manufacturer manufacturerInCart) {
        if (manufacturerInCart != null && !getShoppingCart().getItems().isEmpty()) {
            for (ShopCartItem item : getShoppingCart().getItems()) {
                if (item.getBeanProd1() != null && item.getBeanProd1().getManufacturer() != null && manufacturerInCart.equals(item.getBeanProd1().getManufacturer()))
                    return true;
                if (item.getBeanProd2() != null && item.getBeanProd2().getManufacturer() != null && manufacturerInCart.equals(item.getBeanProd2().getManufacturer()))
                    return true;
            }
        }
        return false;
    }

    public boolean hasLabel(ProductLabel labelInCart) {
        if (labelInCart != null && !getShoppingCart().getItems().isEmpty()) {
            for (ShopCartItem item : getShoppingCart().getItems()) {
                if (item.getBeanProd1() != null && item.getBeanProd1().hasLabel(labelInCart.getCode()))
                    return true;
                if (item.getBeanProd2() != null && item.getBeanProd2().hasLabel(labelInCart.getCode()))
                    return true;
            }
        }
        return false;
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
