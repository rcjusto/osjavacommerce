package org.store.core.globals;

import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.AbstractInterceptor;
import com.opensymphony.xwork2.util.LocalizedTextUtil;
import com.opensymphony.xwork2.util.logging.Logger;
import com.opensymphony.xwork2.util.logging.LoggerFactory;
import org.store.core.admin.SecurityInterceptor;
import org.store.core.beans.Currency;
import org.store.core.beans.StoreProperty;
import org.store.core.beans.User;
import org.store.core.beans.UserVisit;
import org.store.core.front.FrontModuleAction;
import org.store.core.front.UserSession;
import org.store.core.hibernate.HibernateSessionFactory;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.struts2.ServletActionContext;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.StatelessSession;
import org.hibernate.criterion.Restrictions;

import javax.servlet.http.Cookie;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class StoreSessionInterceptor extends AbstractInterceptor {

    private static final Logger LOG = LoggerFactory.getLogger(StoreSessionInterceptor.class);

    public static final String CNT_ADMIN_USER = "ADMIN_USER";
    public static final String CNT_FRONT_USER = "FRONT_USER";
    public static final String CNT_FRONT_USER_MESSAGES = "FRONT_USER_MESSAGES";
    public static final String CNT_AFFILIATE_USER = "AFFILIATE_USER";
    public static final String CNT_AFFILIATE_CODE = "AFFILIATE_CODE";
    public static final String CNT_SHOPPING_CART = "SHOPP_CART";
    public static final String CNT_CURRENCY = "CURRENCY";
    public static final String CNT_LANGUAGE = "LANGUAGE";
    public static final String CNT_MESSAGES = "MESSAGES";
    public static final String CNT_ATTRIBUTES = "ATTRIBUTES";
    public static final String CNT_ERRORS = "ERRORS";
    public static final String CNT_ERRORS_FIELD = "ERRORS_FIELD";
    public static final String CNT_LAST_CATEGORY = "LAST_CATEGORY";
    public static final String CNT_EXTERNAL_PAYMENT = "EXTERNAL_PAYMENT";
    public static final String COOKIE_USER = "USR_432423SE90";


    @Override
    public String intercept(ActionInvocation invocation) throws Exception {
        if (invocation.getAction() instanceof BaseAction) {
            BaseAction action = (BaseAction) invocation.getAction();
            Map<String, Object> session = invocation.getInvocationContext().getSession();
            Map<String, Object> params = invocation.getInvocationContext().getParameters();

            StoreActionMapping mapping = (StoreActionMapping) invocation.getInvocationContext().get(ServletActionContext.ACTION_MAPPING);
            StatelessSession hSession = HibernateSessionFactory.getSessionFactory(action.getDatabaseConfig()).openStatelessSession();

            try {
                // Get store session map
                Map<String, Object> storeSessionMap = null;
                if (session.containsKey(action.getStoreCode()))
                    storeSessionMap = (Map<String, Object>) session.get(action.getStoreCode());
                if (storeSessionMap == null) {
                    LOG.debug("Store session map not found. Creating...");
                    storeSessionMap = new HashMap<String, Object>();
                    session.put(action.getStoreCode(), storeSessionMap);
                }
                action.setStoreSessionObjects(storeSessionMap);

                // Set messages and errors
                if (storeSessionMap.containsKey(CNT_ATTRIBUTES)) {
                    Object lAtts = storeSessionMap.get(CNT_ATTRIBUTES);
                    if (lAtts != null && lAtts instanceof Map) {
                        for (Object key : ((Map) lAtts).keySet()) {
                            try {
                                BeanUtils.setProperty(action, key.toString(), ((Map) lAtts).get(key));
                            } catch (Exception e) {
                                action.getRequest().setAttribute(key.toString(), ((Map) lAtts).get(key));
                            }
                        }
                    }
                    storeSessionMap.remove(CNT_ATTRIBUTES);
                }
                if (storeSessionMap.containsKey(CNT_MESSAGES)) {
                    Object lMsg = storeSessionMap.get(CNT_MESSAGES);
                    if (lMsg != null && lMsg instanceof List) {
                        for (Object msg : (List) lMsg) if (msg != null) action.addActionMessage(msg.toString());
                    }
                    storeSessionMap.remove(CNT_MESSAGES);
                }
                if (storeSessionMap.containsKey(CNT_ERRORS)) {
                    Object lMsg = storeSessionMap.get(CNT_ERRORS);
                    if (lMsg != null && lMsg instanceof List) {
                        for (Object msg : (List) lMsg) if (msg != null) action.addActionError(msg.toString());
                    }
                    storeSessionMap.remove(CNT_ERRORS);
                }
                if (storeSessionMap.containsKey(CNT_ERRORS_FIELD)) {
                    Object lMsg = storeSessionMap.get(CNT_ERRORS_FIELD);
                    if (lMsg != null && lMsg instanceof Map) {
                        Map<String, List<String>> map = (Map<String, List<String>>) lMsg;
                        for (String fld : map.keySet()) {
                            List<String> l = map.get(fld);
                            for (String msg : l) {
                                if (msg != null) action.addFieldError(fld, msg);
                            }
                        }
                    }
                    storeSessionMap.remove(CNT_ERRORS_FIELD);
                }

                // usuarios
                // Buscar usuario en la session
                User user;
                if (action instanceof FrontModuleAction) {
                    Long idUser = (storeSessionMap.containsKey(CNT_FRONT_USER)) ? (Long) storeSessionMap.get(CNT_FRONT_USER) : null;
                    user = (idUser != null) ? (User) action.getDao().get(User.class, idUser) : null;
                    // Buscar usuario en las cookies
                    if (user == null && action.getRequest().getCookies() != null && action.getRequest().getCookies().length > 0) {
                        for (Cookie c : action.getRequest().getCookies()) {
                            if (COOKIE_USER.equalsIgnoreCase(c.getName()) && StringUtils.isNotEmpty(c.getValue())) {
                                String userId = SomeUtils.decrypt3Des(c.getValue(), action.getEncryptionKey());
                                user = (StringUtils.isNotEmpty(userId)) ? action.getDao().getUserByUserId(userId) : null;
                                if (user != null) {
                                    ((FrontModuleAction)action).setFrontUser(user);
                                    break;
                                }
                            }
                        }
                    }
                    if (user != null) {
                        Hibernate.initialize(user.getAddressList());
                        Hibernate.initialize(user.getPreferences());
                        Hibernate.initialize(user.getWishList());
                    }
                    ((FrontModuleAction)action).setFrontUser(user);

                    UserVisit uv = action.getDao().getUserVisit(action.getRequest());
                    if (uv == null) {
                        uv = new UserVisit();
                        uv.setInventaryCode(action.getStoreCode());
                        uv.setIpAddress(action.getRequest().getRemoteAddr());
                        uv.setSessionId(action.getRequest().getSession().getId());
                        uv.setVisitDate(Calendar.getInstance().getTime());
                    }
                    if (user != null && !user.equals(uv.getUser())) uv.setUser(user);
                    action.getDao().save(uv);
                    
                }

                Long idAdminUser = (storeSessionMap.containsKey(CNT_ADMIN_USER)) ? (Long) storeSessionMap.get(CNT_ADMIN_USER) : null;
                User adminUser = (idAdminUser != null) ? (User) action.getDao().get(User.class, idAdminUser) : null;
                if (adminUser == null && action.getRequest().getCookies() != null) {
                    for (Cookie c : action.getRequest().getCookies()) {
                        if (SecurityInterceptor.CNT_ADMIN_USER_COOKIE.equalsIgnoreCase(c.getName()) && StringUtils.isNotEmpty(c.getValue())) {
                            adminUser = findUserByCookie(action, c.getValue());
                        }
                    }
                }
                if (adminUser != null) {
                    Hibernate.initialize(adminUser.getRoles());
                }
                action.setAdminUser(adminUser);

                Long idAffiliateUser = (storeSessionMap.containsKey(CNT_AFFILIATE_USER)) ? (Long) storeSessionMap.get(CNT_AFFILIATE_USER) : null;
                User affiliateUser = (idAffiliateUser != null) ? (User) action.getDao().get(User.class, idAffiliateUser) : null;
                action.setAffiliateUser(affiliateUser);
                action.setAffiliateCode((storeSessionMap.containsKey(CNT_AFFILIATE_CODE)) ? (String) storeSessionMap.get(CNT_AFFILIATE_CODE) : null);

                // update language
                if (mapping!=null && mapping.getNamespace()!=null && mapping.getNamespace().endsWith("admin")) {
                    String admLang = action.getStoreProperty(StoreProperty.PROP_ADMIN_LANGUAGE, action.getDefaultLanguage());
                    Locale admLocale = LocalizedTextUtil.localeFromString(admLang, null);
                    if (admLocale!=null) saveLocale(invocation, admLocale);
                } else {
                    Object requestedLanguage = params.remove("set_lang");
                    if (requestedLanguage != null && requestedLanguage.getClass().isArray() && ((Object[]) requestedLanguage).length == 1) requestedLanguage = ((Object[]) requestedLanguage)[0];
                    if (requestedLanguage != null && validLanguage(action, requestedLanguage.toString())) storeSessionMap.put(CNT_LANGUAGE, requestedLanguage.toString());
                    String actualLanguage = (storeSessionMap.containsKey(CNT_LANGUAGE)) ? (String) storeSessionMap.get(CNT_LANGUAGE) : null;
                    if (!validLanguage(action, actualLanguage)) {
                        actualLanguage = action.getDefaultLanguage();
                        storeSessionMap.put(CNT_LANGUAGE, actualLanguage);
                    }
                    Locale locale = LocalizedTextUtil.localeFromString(actualLanguage, null);
                    if (locale != null) saveLocale(invocation, locale);
                }

                // Currency
                Object requestedCurrency = params.remove("set_curr");
                if (requestedCurrency != null && requestedCurrency.getClass().isArray() && ((Object[]) requestedCurrency).length == 1) requestedCurrency = ((Object[]) requestedCurrency)[0];
                if (requestedCurrency != null && validCurrency(hSession, requestedCurrency.toString())) storeSessionMap.put(CNT_CURRENCY, requestedCurrency.toString());
                String actualCurrency = (storeSessionMap.containsKey(CNT_CURRENCY)) ? (String) storeSessionMap.get(CNT_CURRENCY) : null;
                if (!validCurrency(hSession, actualCurrency)) {
                    actualCurrency = action.getDefaultCurrency().getCode();
                    storeSessionMap.put(CNT_CURRENCY, actualCurrency);
                }
                action.setActualCurrency(action.getDao().getCurrency(actualCurrency));

                // Shopping cart
                if (action instanceof FrontModuleAction) {
                    UserSession sc;
                    Object obj = (storeSessionMap.containsKey(CNT_SHOPPING_CART)) ? storeSessionMap.get(CNT_SHOPPING_CART) : null;
                    if (obj == null || !(obj instanceof String)) {
                        sc = new UserSession(action);
                    } else {
                        sc = new UserSession(action, (String) obj);
                    }
                    ((FrontModuleAction) action).setShoppingCart(sc);
                }

            } catch (Exception e) {
                LOG.error(e.getMessage(), e);
            } finally {
                if (hSession != null)
                    try {
                        hSession.close();
                    } catch (Exception e) {
                        LOG.error(e.getMessage(), e);
                    }
            }
        }
        return invocation.invoke();
    }

    private boolean validLanguage(BaseAction action, String lang) {
        return ArrayUtils.contains(action.getLanguages(), lang);
    }

    private boolean validCurrency(StatelessSession hSession, String code) {
        List l = hSession.createCriteria(Currency.class).add(Restrictions.eq("code", code)).add(Restrictions.eq("active", Boolean.TRUE)).list();
        return l != null && l.size() > 0;
    }

    private User findUserByCookie(BaseAction action, String encryptedUser) {
        try {
            String userId = SomeUtils.decrypt3Des(encryptedUser, action.getEncryptionKey());
            Session s = HibernateSessionFactory.getSession(action.getDatabaseConfig());
            List<User> l = s.createCriteria(User.class).add(Restrictions.eq("inventaryCode", action.getStoreCode())).add(Restrictions.eq("admin", Boolean.TRUE)).add(Restrictions.eq("userId", userId)).list();
            if (l!=null && !l.isEmpty()) return l.get(0);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
        return null;
    }


    protected void saveLocale(ActionInvocation invocation, Locale locale) {
        invocation.getInvocationContext().setLocale(locale);
    }

}
