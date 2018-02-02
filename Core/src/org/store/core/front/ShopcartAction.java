package org.store.core.front;

import org.store.core.beans.Mail;
import org.store.core.beans.Product;
import org.store.core.beans.ProductRelated;
import org.store.core.beans.ProductVariation;
import org.store.core.beans.PromotionalCode;
import org.store.core.beans.ShopCart;
import org.store.core.beans.ShopCartItem;
import org.store.core.beans.StoreProperty;
import org.store.core.beans.utils.BreadCrumb;
import org.store.core.globals.IP2CountryService;
import org.store.core.globals.SomeUtils;
import org.store.core.mail.MailSenderThreat;
import org.store.core.utils.events.EventService;
import org.store.core.utils.events.EventUtils;
import org.store.sslplugin.annotation.Unsecured;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Rogelio Caballero
 * 9/08/11 7:07
 */
public class ShopcartAction extends FrontModuleAction {


    @Unsecured
    public String shopcart() throws Exception {
        Integer numRelatedProducts = SomeUtils.strToInteger(getStoreProperty(StoreProperty.PROP_SHOPCART_CROSS_SELLING_NUMBER, StoreProperty.PROP_DEFAULT_SHOPCART_CROSS_SELLING_NUMBER));
        Set<Product> associated = new HashSet<Product>();
        if (getShoppingCart() != null && CollectionUtils.isNotEmpty(getShoppingCart().getItems())) {
            if (quantities != null && quantities.length == getShoppingCart().getItems().size()) {
                for (int i = 0; i < quantities.length; i++) {
                    ShopCartItem item = getUserSession().getItem(i);
                    Integer quant = SomeUtils.strToInteger(quantities[i]);
                    if (quant != null && quant > 0 && !item.getQuantity().equals(quant)) {
                        // Validar si se puede agregar
                        long maxCanBuy = item.getBeanProd1().getMaxToBuy(getFrontUser());
                        if (maxCanBuy >= getUserSession().getNumProduct(item.getProduct1()) - item.getQuantity() + quant) {
                            item.setQuantity(quant);
                            getUserSession().resetShipping();
                            getShoppingCart().setBillingAddress(null);
                            updateShoppingCartInSession();
                        } else {
                            if (maxCanBuy < 1) addActionError(getText(CNT_ERROR_HASNO_STOCK, CNT_DEFAULT_ERROR_HASNO_STOCK, new String[]{item.getName1()}));
                            else addActionError(getText(CNT_ERROR_STOCK_LIMITED, CNT_DEFAULT_ERROR_STOCK_LIMITED, new String[]{item.getName1(), String.valueOf(maxCanBuy)}));
                        }
                    }
                }
            }

            if (removeFromCart != null && removeFromCart >= 0 && removeFromCart < getShoppingCart().getItems().size()) {
                getUserSession().delItem(removeFromCart);
                updateShoppingCartInSession();
            }

            // buscar productos asociados a productos en el carro
            for (ShopCartItem item : getUserSession().getShoppingCart().getItems()) {
                List<ProductRelated> relatedProducts = getProductsRelated(item.getBeanProd1());
                if (item.getBeanProd1() != null && relatedProducts != null && !relatedProducts.isEmpty()) {
                    for (ProductRelated r : relatedProducts) {
                        if (associated.size() < numRelatedProducts && !associated.contains(r.getRelated())) associated.add(r.getRelated());
                        if (associated.size() >= numRelatedProducts) break;
                    }
                }
                if (associated.size() >= numRelatedProducts) break;
            }
        }

        // completar lista de productos asociados
        if (associated.size() < numRelatedProducts) {
            List<Product> l = getRandomProducts("label:hot", 2 * numRelatedProducts);
            if (l != null && !l.isEmpty())
                for (Product p : l) {
                    if (associated.size() < numRelatedProducts && !associated.contains(p)) associated.add(p);
                    if (associated.size() >= numRelatedProducts) break;
                }
        }
        addToStack("associatedProducts", associated);

        getUserSession().initializeItems();

        // validar q los promotion sigan cumpliendo las condiciones
        if (getUserSession().getShoppingCart().getPromotionalCodes()!=null && getUserSession().getShoppingCart().getPromotionalCodes().length>0) {
            for(String code : getUserSession().getShoppingCart().getPromotionalCodes()) {
                PromotionalCode bean = dao.getPromotionalCode(code);
                if (bean != null) {
                    if (!bean.isValid(getUserSession()) || !bean.isValid(getFrontUser())) {
                        addFieldError("promotions", getText(CNT_ERROR_CANNOT_APPLY_PROMOTION, CNT_DEFAULT_ERROR_CANNOT_APPLY_PROMOTION, new String[]{code}));
                        getUserSession().getShoppingCart().delPromotionalCode(code);
                    }
                }
            }
        }

        // Obtener la lista de metodos de pago
        List<Map<String, Object>> metodosPago = getPaymentMethods(true, getFrontUserLevel());
        addToStack("metodos", metodosPago);
        // Resetear elementos de shipping
        getUserSession().resetShipping();
        getUserSession().setBillingAddress(null);

        if (frontUser != null) {
            List cartAlerts = dao.getUserCartAlerts(frontUser);
            if (cartAlerts != null && !cartAlerts.isEmpty()) {
                Map map = new HashMap();
                for (Object o : cartAlerts) {
                    Object[] arr = (Object[]) o;
                    if (StringUtils.isNotEmpty((String) arr[0]) && ((Number) arr[1]).intValue() > 0) map.put(arr[0], arr[1]);
                }
                addToStack("cartAlerts", map);
            }
            ShopCart lastCart = dao.getLastSaveCart(frontUser);
            if (lastCart != null) addToStack("lastCart", lastCart);
        }

        getBreadCrumbs().add(new BreadCrumb("shopcart", getText("my.shopping.cart", "My Shopping Cart"), null, null));
        addToStack("_notAjax", Boolean.TRUE);
        EventUtils.executeEvent(getServletContext(), EventService.EVENT_VISIT_SHOPPCART, this);
        return SUCCESS;
    }

    public String addToCart() throws Exception {
        Product beanP1 = (Product) dao.get(Product.class, idProduct);
        Product beanP2 = (Product) dao.get(Product.class, idAdd);

        List<String> errors = new ArrayList<String>();
        List<String> messages = new ArrayList<String>();

        String method = getRequest().getMethod();
        if ("post".equalsIgnoreCase(method)) {   // permitir solo post

            // validate add to cart for ip address
            IP2CountryService ip2CountryService = getIP2CountryService();
            if (ip2CountryService != null) {
                String currentCountry = null;
                if (beanP1 != null && beanP1.getCategory() != null) {
                    String countriesStr = dao.getCountriesCanBuyCategory(beanP1.getCategory());
                    if (StringUtils.isNotEmpty(countriesStr)) {
                        String[] countries = StringUtils.split(countriesStr, ",");
                        if (!ArrayUtils.isEmpty(countries)) {
                            currentCountry = ip2CountryService.getCountryCode(getRequest().getRemoteAddr());
                            if (!ArrayUtils.contains(countries, currentCountry)) {
                                errors.add(getText(CNT_ERROR_COUNTRY_LIMITED, CNT_DEFAULT_ERROR_COUNTRY_LIMITED, new String[]{beanP1.getProductName(getLocale().getLanguage())}));
                            }
                        }
                    }
                }
                if (beanP2 != null && beanP2.getCategory() != null) {
                    String countriesStr = dao.getCountriesCanBuyCategory(beanP2.getCategory());
                    if (StringUtils.isNotEmpty(countriesStr)) {
                        String[] countries = StringUtils.split(countriesStr, ",");
                        if (!ArrayUtils.isEmpty(countries)) {
                            if (StringUtils.isNotEmpty(currentCountry))
                                currentCountry = ip2CountryService.getCountryCode(getRequest().getRemoteAddr());
                            if (!ArrayUtils.contains(countries, currentCountry)) {
                                errors.add(getText(CNT_ERROR_COUNTRY_LIMITED, CNT_DEFAULT_ERROR_COUNTRY_LIMITED, new String[]{beanP2.getProductName(getLocale().getLanguage())}));
                            }
                        }
                    }
                }
            }

            if (errors.isEmpty()) {
                ProductVariation beanV1 = (ProductVariation) dao.get(ProductVariation.class, variation);
                ProductVariation beanV2 = (ProductVariation) dao.get(ProductVariation.class, variationAdd);
                if (beanP1 != null) {
                    if (quantity == null || quantity < 1) quantity = 1;
                    if (variation != null) {
                        if (beanV1 == null) variation = null;
                        else if (!beanV1.getProduct().equals(beanP1)) variation = null;
                    }
                    if (beanP2 == null && idAdd != null) idAdd = null;
                    if (variationAdd != null) {
                        if (beanV2 == null) variationAdd = null;
                        else if (!beanV2.getProduct().equals(beanP2)) variationAdd = null;
                    }
                    long maxCanBuy = beanP1.getMaxToBuy(getFrontUser());
                    if (maxCanBuy >= getUserSession().getNumProduct(idProduct) + quantity) {
                        getUserSession().addItem(beanP1.getIdProduct(), variation, idAdd, variationAdd, quantity, selDate, selTime, null, null, true);
                        updateShoppingCartInSession();
                        messages.add(getText(CNT_MESSAGE_PRODUCT_ADDED_TO_CART, CNT_DEFAULT_MESSAGE_PRODUCT_ADDED_TO_CART, new String[]{beanP1.getProductName(getLocale().getLanguage())}));
                    } else {
                        if (maxCanBuy < 1) errors.add(getText(CNT_ERROR_HASNO_STOCK, CNT_DEFAULT_ERROR_HASNO_STOCK, new String[]{beanP1.getProductName(getLocale().getLanguage())}));
                        else errors.add(getText(CNT_ERROR_STOCK_LIMITED, CNT_DEFAULT_ERROR_STOCK_LIMITED, new String[]{beanP1.getProductName(getLocale().getLanguage()), String.valueOf(maxCanBuy)}));
                    }
                }
                EventUtils.executeEvent(getServletContext(), EventService.EVENT_ADD_TO_CART, this);
            }
        }

        if ("ajax-cart".equalsIgnoreCase(output)) {
            addToStack("messages", messages);
            addToStack("errors", errors);
            return "ajax-cart";
        } else {
            for (String e : errors) addSessionError(e);
            for (String m : messages) addSessionMessage(m);
            return SUCCESS;
        }
    }

    public String shopCartPromotion() throws Exception {
        if (StringUtils.isNotEmpty(code) && StringUtils.isNotEmpty(output)) {
            if ("add".equalsIgnoreCase(output)) {
                PromotionalCode bean = dao.getPromotionalCode(code);
                if (bean != null) {
                    if (bean.isValid(getUserSession()) && bean.isValid(getFrontUser())) {
                        if (!getUserSession().getShoppingCart().hasPromotionalCode(bean.getCode())) getUserSession().getShoppingCart().addPromotionalCode(bean.getCode());
                    } else {
                        addSessionFieldError("promotions", getText(CNT_ERROR_CANNOT_APPLY_PROMOTION, CNT_DEFAULT_ERROR_CANNOT_APPLY_PROMOTION, new String[]{code}));
                    }
                } else {
                    addSessionFieldError("promotions", getText(CNT_ERROR_INVALID_PROMOTIONAL_CODE, CNT_DEFAULT_ERROR_INVALID_PROMOTIONAL_CODE, new String[]{code}));
                }
            } else if ("del".equalsIgnoreCase(output)) {
                if (StringUtils.isNotEmpty(code)) {
                    getUserSession().getShoppingCart().delPromotionalCode(code);
                }
            }
            updateShoppingCartInSession();
        }
        return "shopcart";
    }

    public String shopCartList() throws Exception {
        if (getFrontUser() == null) {
            Map<String, String> map = new HashMap<String, String>();
            if (StringUtils.isNotBlank(output)) map.put("output", output);
            setRedirectUrl(url("shopcartlist", "", map));
            return "register";
        }
        addToStack("quotedCarts", dao.getQuotedCarts(frontUser));
        addToStack("savedCarts", dao.getSavedCarts(frontUser));
        return SUCCESS;
    }

    public String shopCartData() throws Exception {
        if (getFrontUser() != null) {
            ShopCart cart = (ShopCart) dao.get(ShopCart.class, idCart);
            if (cart != null && frontUser.equals(cart.getUser())) {
                cart.initializeItems(this);
                addToStack("cart", cart);
            } else addActionError(getText("shopcart.not.found", "Shopping Cart Not Found"));
        } else addActionError(getText("shopcart.not.found", "Shopping Cart Not Found"));
        return ("print".equalsIgnoreCase(output)) ? "print" : SUCCESS;
    }

    public String shopCartDel() throws Exception {
        if (getFrontUser() != null) {
            ShopCart cart = (ShopCart) dao.get(ShopCart.class, idCart);
            if (cart != null && frontUser.equals(cart.getUser())) {
                if (cart.equals(getUserSession().getShoppingCart())) getUserSession().resetAll();
                if (ShopCart.STATUS_PENDING.equalsIgnoreCase(cart.getStatus())) {
                    // enviar correo avisando de la cancelacion del request
                }
                cart.setStatus(ShopCart.STATUS_CANCELLED);
                dao.save(cart);
            } else addSessionError(getText("shopcart.not.found", "Shopping Cart Not Found"));
        } else addSessionError(getText("shopcart.not.found", "Shopping Cart Not Found"));
        return SUCCESS;
    }


    public String shopCartToCart() throws Exception {
        if (getFrontUser() == null) {
            Map<String, String> map = new HashMap<String, String>();
            if (StringUtils.isNotBlank(output)) map.put("output", output);
            setRedirectUrl(url("shopcartlist", "", map));
            return "register";
        }

        ShopCart cart = (ShopCart) dao.get(ShopCart.class, idCart);
        if (cart != null && getFrontUser().equals(cart.getUser())) {
            cart.initializeItems(this);
            for (ShopCartItem item : cart.getItems()) {
                Long idProduct1 = item.getProduct1();
                Long idProduct2 = item.getProduct2();
                Long idVariation1 = item.getVariation1();
                Long idVariation2 = item.getVariation2();
                long maxCanBuy = item.getBeanProd1().getMaxToBuy(getFrontUser());
                if (maxCanBuy >= getUserSession().getNumProduct(idProduct1) + item.getQuantity()) {
                    getUserSession().addItem(idProduct1, idVariation1, idProduct2, idVariation2, item.getQuantity(), item.getSelDate(), item.getSelTime(), null, null, false);
                    updateShoppingCartInSession();
                } else {
                    if (maxCanBuy < 1) addSessionError(getText(GeneralAction.CNT_ERROR_HASNO_STOCK, GeneralAction.CNT_DEFAULT_ERROR_HASNO_STOCK, new String[]{item.getBeanProd1().getProductName(getLocale().getLanguage())}));
                    else addSessionError(getText(GeneralAction.CNT_ERROR_STOCK_LIMITED, GeneralAction.CNT_DEFAULT_ERROR_STOCK_LIMITED, new String[]{item.getBeanProd1().getProductName(getLocale().getLanguage()), String.valueOf(maxCanBuy)}));
                }
            }
            getUserSession().initializeItems();
        }
        return SUCCESS;
    }

    public String shopCartLoad() throws Exception {
        if (getFrontUser() == null) {
            Map<String, String> map = new HashMap<String, String>();
            if (StringUtils.isNotBlank(output)) map.put("output", output);
            setRedirectUrl(url("shopcartload", "", map));
            return "register";
        }

        ShopCart cart = (ShopCart) dao.get(ShopCart.class, idCart);
        if (cart != null && getFrontUser().equals(cart.getUser())) {
            cart.initializeItems(this);
            getUserSession().setShoppingCart(cart);
            // valida stock
            for (ShopCartItem item : cart.getItems()) {
                // Validar si se puede agregar
                long maxCanBuy = item.getBeanProd1().getMaxToBuy(getFrontUser());
                if (maxCanBuy < getUserSession().getNumProduct(item.getProduct1())) {
                    if (maxCanBuy < 1) {
                        addToStack("cartMessage", getText(CNT_ERROR_HASNO_STOCK, CNT_DEFAULT_ERROR_HASNO_STOCK, new String[]{item.getName1()}));
                        cart.getItems().remove(item);
                    } else {
                        item.setQuantity((int) maxCanBuy);
                        addSessionError(getText(CNT_ERROR_STOCK_LIMITED, CNT_DEFAULT_ERROR_STOCK_LIMITED, new String[]{item.getName1(), String.valueOf(maxCanBuy)}));
                    }
                }
            }

            updateShoppingCartInSession();
        }
        return SUCCESS;
    }

    public String shopCartSave() throws Exception {

        if (getFrontUser() == null) {
            Map<String, String> map = new HashMap<String, String>();
            if (StringUtils.isNotBlank(output)) map.put("output", output);
            setRedirectUrl(url("shopcart", "", map));
            return "register";
        }

        if (!getShoppingCart().getItems().isEmpty()) {
            if ("quote".equalsIgnoreCase(getOutput())) {
                getShoppingCart().setStatus(ShopCart.STATUS_PENDING);
                String email = getStoreProperty(StoreProperty.PROP_MAIL_REQUEST_QUOTE, null);
                if (StringUtils.isNotEmpty(email)) {
                    String body = proccessVelocityTemplate(Mail.MAIL_TEMPLATE_ADMIN_REQUEST_QUOTE);
                    Mail mail = new Mail();
                    mail.setInventaryCode(getStoreCode());
                    mail.setBody(body);
                    mail.setSubject(getText(CNT_SUBJECT_ADMIN_REQUEST_QUOTE, CNT_SUBJECT_ADMIN_REQUEST_QUOTE_DEFAULT));
                    mail.setToAddress(email);
                    mail.setPriority(Mail.PRIORITY_HIGH);
                    mail.setReference("CART REQUEST QUOTE ID:" + getShoppingCart().getId());
                    dao.save(mail);
                    MailSenderThreat.asyncSendMail(mail, this);
                }
                updateShoppingCartInSession();
                addSessionMessage(getText("shopcart.quote.requested", "Shopping cart quote requested"));
                getUserSession().resetAll();
            } else if ("save".equalsIgnoreCase(getOutput())) {
                getShoppingCart().setStatus(ShopCart.STATUS_SAVED);
                updateShoppingCartInSession();
            } else if ("Y".equalsIgnoreCase(getStoreProperty(StoreProperty.PROP_SHOPCART_AUTO_SAVE, null))) {
                getShoppingCart().setStatus(ShopCart.STATUS_SAVED);
                updateShoppingCartInSession();
            } else {
                getShoppingCart().setStatus(null);
                updateShoppingCartInSession();
            }
        }

        return SUCCESS;
    }


    private Long idCart;
    private String[] quantities;
    private Integer removeFromCart;
    private Integer quantity;
    private Long idProduct;
    private Long idAdd;
    private Long variation;
    private Long variationAdd;
    private String selDate;
    private String selTime;
    private String code;
    private String output;

    public Long getIdCart() {
        return idCart;
    }

    public void setIdCart(Long idCart) {
        this.idCart = idCart;
    }

    public String[] getQuantities() {
        return quantities;
    }

    public void setQuantities(String[] quantities) {
        this.quantities = quantities;
    }

    public Integer getRemoveFromCart() {
        return removeFromCart;
    }

    public void setRemoveFromCart(Integer removeFromCart) {
        this.removeFromCart = removeFromCart;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Long getIdProduct() {
        return idProduct;
    }

    public void setIdProduct(Long idProduct) {
        this.idProduct = idProduct;
    }

    public Long getIdAdd() {
        return idAdd;
    }

    public void setIdAdd(Long idAdd) {
        this.idAdd = idAdd;
    }

    public Long getVariation() {
        return variation;
    }

    public void setVariation(Long variation) {
        this.variation = variation;
    }

    public Long getVariationAdd() {
        return variationAdd;
    }

    public void setVariationAdd(Long variationAdd) {
        this.variationAdd = variationAdd;
    }

    public String getSelDate() {
        return selDate;
    }

    public void setSelDate(String selDate) {
        this.selDate = selDate;
    }

    public String getSelTime() {
        return selTime;
    }

    public void setSelTime(String selTime) {
        this.selTime = selTime;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getOutput() {
        return output;
    }

    public void setOutput(String output) {
        this.output = output;
    }
}
