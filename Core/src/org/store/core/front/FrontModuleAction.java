package org.store.core.front;

import org.apache.commons.lang.StringUtils;
import org.apache.struts2.ServletActionContext;
import org.apache.struts2.json.JSONException;
import org.store.core.beans.*;
import org.store.core.beans.utils.BreadCrumb;
import org.store.core.globals.BaseAction;
import org.store.core.globals.IP2CountryService;
import org.store.core.globals.SomeUtils;
import org.store.core.globals.StoreSessionInterceptor;
import org.store.core.utils.merchants.MerchantService;
import org.store.core.utils.merchants.MerchantUtils;

import java.util.*;


public class FrontModuleAction extends BaseAction {

    @Override
    public void prepare() throws Exception {
        addToStack("pageTitle", getStoreMeta("title", getLocale().getLanguage()));
        addToStack("metaDescription", getStoreMeta("description", getLocale().getLanguage()));
        addToStack("metaKeywords", getStoreMeta("keywords", getLocale().getLanguage()));
        addToStack("metaAbstract", getStoreMeta("abstract", getLocale().getLanguage()));
    }


    private UserSession userSession;

    public UserSession getUserSession() {
        return (userSession!=null) ? userSession : new UserSession(this);
    }

    public ShopCart getShoppingCart() {
        return (userSession!=null) ? userSession.getShoppingCart() : null;
    }

    public void updateShoppingCartInSession() {
        if (userSession != null)
            try {
                String cad = userSession.serialize();
                if (StringUtils.isNotEmpty(cad)) getStoreSessionObjects().put(StoreSessionInterceptor.CNT_SHOPPING_CART, cad);
                else getStoreSessionObjects().remove(StoreSessionInterceptor.CNT_SHOPPING_CART);
            } catch (JSONException e) {
                log.error(e.getMessage(), e);
            }
    }

    public void setShoppingCart(UserSession userSession) {
        this.userSession = userSession;
    }

    public List<Insurance> getInsuranceList() {
        return dao.getInsurancesFor(getUserSession().getTotal());
    }

    public void setFrontUser(User user) {
        this.frontUser = user;
        if (user != null) {
            if (!storeSessionObjects.containsKey(StoreSessionInterceptor.CNT_FRONT_USER) || !user.getIdUser().equals(storeSessionObjects.get(StoreSessionInterceptor.CNT_FRONT_USER))) {
                storeSessionObjects.put(StoreSessionInterceptor.CNT_FRONT_USER, user.getIdUser());

                getUserSession().getShoppingCart().setUser(user);
                updateShoppingCartInSession();

            /** aqui podemos hacer cualquier tarea de inicializacion del usuario **/
                List<String> messages = new ArrayList<String>();
                // buscar si tiene shopcarts salvados
                ShopCart lastCart = dao.getLastSaveCart(user);
                if (lastCart!=null) {
                    String date = SomeUtils.formatDate(lastCart.getCreatedDate(),getLocale().getLanguage());
                    String link = url("reloadCart","")+"?id="+lastCart.getId().toString();
                    messages.add(getText("message.reload.saved.cart", "Click <a href={2}>here</a> to reload your last saved cart from {0} with {1} products", new String[]{date, String.valueOf(lastCart.getItems().size()), link}));
                }

                // buscar si tiene shopcart aprobado
                List<ShopCart> approvedCarts = dao.getApprovedCarts(user);
                if (approvedCarts!=null && !approvedCarts.isEmpty()) {
                    for(ShopCart sc : approvedCarts) {
                        String date = SomeUtils.formatDate(sc.getCreatedDate(),getLocale().getLanguage());
                        String link = url("loadApprovedCart","")+"?id="+sc.getId().toString();
                        messages.add(getText("message.load.approved.cart", "You request quote for a Cart from {0} with {1} products, has been approved. Click <a href={2}>here</a> to load", new String[]{date, String.valueOf(sc.getItems().size()), link}));
                    }
                }

                if (!messages.isEmpty()) storeSessionObjects.put(StoreSessionInterceptor.CNT_FRONT_USER_MESSAGES, messages);
            }
        }
        else storeSessionObjects.remove(StoreSessionInterceptor.CNT_FRONT_USER);
    }

    public List<String> getFrontUserMessages() {
        return storeSessionObjects.containsKey(StoreSessionInterceptor.CNT_FRONT_USER_MESSAGES) ? (List<String>) storeSessionObjects.get(StoreSessionInterceptor.CNT_FRONT_USER_MESSAGES) : null;
    }

    protected List<BreadCrumb> breadCrumbs;

    public List<BreadCrumb> getBreadCrumbs() {
        if (breadCrumbs==null) breadCrumbs = new ArrayList<BreadCrumb>();
        return breadCrumbs;
    }

    public void setBreadCrumbs(List<BreadCrumb> breadCrumbs) {
        this.breadCrumbs = breadCrumbs;
    }


    protected List<Map<String, Object>> getPaymentMethods(boolean external, UserLevel level) {
        // Obtener la lista de metodos de pago
        List<Map<String, Object>> metodosPago = new ArrayList<Map<String, Object>>();
        MerchantUtils mu = new MerchantUtils(getServletContext());
        Map<String, Class> map = mu.getMerchants();
        if (map != null && !map.isEmpty()) {
            int index = 1;
            for (String serviceName : map.keySet()) {
                MerchantService service = mu.getService(serviceName, this);
                if (service != null && service.isActive() && (level == null || level.canPayWith(serviceName))) {
                    if (external == MerchantService.TYPE_EXTERNAL.equalsIgnoreCase(service.getType())) {
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
        return metodosPago;
    }

    public boolean getCanCheckout() {
        List<Map<String, Object>> metodosCheckout = getPaymentMethods(false, getFrontUserLevel());
        return metodosCheckout != null && !metodosCheckout.isEmpty();
    }

    public Integer getMaxPromoCodes() {
        Integer maxPromoCodes = SomeUtils.strToInteger(getStoreProperty(StoreProperty.PROP_MAX_PROMOTIONAL_CODES, StoreProperty.PROP_DEFAULT_MAX_PROMOTIONAL_CODES));
        return (maxPromoCodes != null) ? maxPromoCodes : 0;
    }

    public String getUseCvd() {
        return getStoreProperty(StoreProperty.PROP_PAYMENT_USE_CVD, StoreProperty.PROP_DEFAULT_PAYMENT_USE_CVD);
    }

    public String getUseAvs() {
        return getStoreProperty(StoreProperty.PROP_PAYMENT_USE_AVS, StoreProperty.PROP_DEFAULT_PAYMENT_USE_AVS);
    }

    private List<Map<String, Object>> getPaymentMethods(boolean external) {
        return getPaymentMethods(external, null);
    }

    public boolean getCanDeliver() {
        List<String> carriers = dao.getCarriers(true);
        if (carriers.isEmpty()) {
            ShippingMethod defaultMethod = dao.getDefaultShippingMethod();
            if (defaultMethod != null) return true;
        } else return true;
        return false;
    }

    public String getCountryConnected() {
        if (!storeSessionObjects.containsKey("country_conected")) {
            IP2CountryService ip2CountryService = getIP2CountryService();
            String cc = null;
            if (ip2CountryService != null) {
                cc = ip2CountryService.getCountryCode(request.getRemoteAddr());
            }
            storeSessionObjects.put("country_conected", (StringUtils.isNotEmpty(cc) ? cc : ""));
        }
        return (String) storeSessionObjects.get("country_conected");
    }

    public List<Product> getComparedProducts() {
        List<Long> list;
        if (getStoreSessionObjects().containsKey(CompareAction.COMPARE_PRODUCTS)) list = (List<Long>) getStoreSessionObjects().get(CompareAction.COMPARE_PRODUCTS);
        else {
            list = new ArrayList<Long>();
            getStoreSessionObjects().put(CompareAction.COMPARE_PRODUCTS, list);
        }
        if (!list.isEmpty()) {
            List<Product> result = new ArrayList<Product>();
            for(Long id : list) {
                Product product = (Product) dao.get(Product.class, id);
                if (product!=null && product.canShow()) {
                    result.add(product);
                }
            }
            if (!result.isEmpty()) return result;
        }
        return null;
    }

    public Set<ProductLabel> getProductLabels(Product product) {
        return dao.getProductLabels(product);
    }

    public boolean isMenuActive(Menu m) {
        String actionName = ServletActionContext.getActionMapping().getName();
        if ("category".equalsIgnoreCase(m.getMenuType())) {
            if (("category".equalsIgnoreCase(actionName) || "product".equalsIgnoreCase(actionName)) && this instanceof GeneralAction) {
                GeneralAction action = (GeneralAction) this;
                if (action.getCategory()!=null && m.getLinkCategory()!=null) {
                    return dao.getCategoryHierarchy(action.getCategory()).contains(m.getLinkCategory());
                }
            }
        } else if ("manufacturer".equalsIgnoreCase(m.getMenuType())) {
            if ("category".equalsIgnoreCase(actionName) && this instanceof GeneralAction) {
                GeneralAction action = (GeneralAction) this;
                return (action.getManufacturer()!=null && m.getLinkManufacturer()!=null && m.getLinkManufacturer().getIdManufacturer()!=null && action.getManufacturer().equals(m.getLinkManufacturer().getIdManufacturer().toString()));
            }
        } else if ("label".equalsIgnoreCase(m.getMenuType())) {
            if ("category".equalsIgnoreCase(actionName) && this instanceof GeneralAction) {
                GeneralAction action = (GeneralAction) this;
                return (action.getLabel()!=null && m.getLinkLabel()!=null && action.getLabel().equalsIgnoreCase(m.getLinkLabel().getCode()));
            }
        } else if ("statictext".equalsIgnoreCase(m.getMenuType())) {
            if ("page".equalsIgnoreCase(actionName) && this instanceof GeneralAction) {
                GeneralAction action = (GeneralAction) this;
                return (action.getCode()!=null && m.getLinkStaticText()!=null && action.getCode().equalsIgnoreCase(m.getLinkStaticText().getCode()));
            }
        } else if ("action".equalsIgnoreCase(m.getMenuType())) {
            return actionName.equalsIgnoreCase(m.getLinkAction());
        }
        return false;
    }

}
