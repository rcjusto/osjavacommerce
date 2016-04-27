package org.store.merchants.paypal;

import com.opensymphony.xwork2.util.logging.Logger;
import com.opensymphony.xwork2.util.logging.LoggerFactory;
import com.paypal.soap.api.AddressType;
import com.paypal.soap.api.PayerInfoType;
import org.apache.commons.lang.StringUtils;
import org.store.core.beans.State;
import org.store.core.beans.User;
import org.store.core.beans.UserAddress;
import org.store.core.front.FrontModuleAction;
import org.store.core.globals.SomeUtils;
import org.store.core.utils.events.DefaultEventServiceImpl;

import javax.servlet.ServletContext;
import java.util.Map;

public class PaypalExpressEventImpl extends DefaultEventServiceImpl {
    public static final String PAYPAL_EXPRESS_CHECKOUT = "Paypal Express Checkout";
    public static final String ACTION_RESPONSE = "paypal_express_response";
    public static final String ACTION_CANCEL = "paypal_express_cancel";
    private static final Logger LOG = LoggerFactory.getLogger(PaypalExpressEventImpl.class);


    public String getName() {
        return PAYPAL_EXPRESS_CHECKOUT;
    }

    public String getDescription(FrontModuleAction action) {
        return "Paypal Express Checkout Implementation";
    }

    public Boolean onExecuteEvent(ServletContext ctx, int eventType, FrontModuleAction action, Map<String,Object> map) {
        if (eventType==EVENT_CUSTOM_ACTION && map != null && map.containsKey("actionName")) {
            if (ACTION_RESPONSE.equalsIgnoreCase((String) map.get("actionName"))) {
                LOG.info("Paypal Response - Start");
                String token = action.getRequest().getParameter("token");
                if (StringUtils.isNotEmpty(token)) {
                    LOG.info("Paypal Response - TOKEN Found");
                    // Acceder a paypal para traer el resultado de la transaccion
                    try {
                        PaypalExpressServiceImpl service = new PaypalExpressServiceImpl();
                        service.loadProperties(action);
                        PayerInfoType payerInfoType = service.getExpressCheckoutDetail(token, action);
                        if (payerInfoType!=null) {
                            User user;
                            if (action.getFrontUser()!=null) {
                                user = action.getFrontUser();
                            } else {
                                user = action.getDao().getUserByEmail(payerInfoType.getPayer());
                                if (user==null) {
                                    user = new User();
                                    user.setAdmin(false);
                                    user.setEmail(payerInfoType.getPayer());
                                    user.setFirstname(payerInfoType.getPayerName().getFirstName());
                                    user.setLastname(payerInfoType.getPayerName().getLastName());
                                    user.setTitle(payerInfoType.getPayerName().getSalutation());
                                    user.setCompanyName(payerInfoType.getPayerBusiness());
                                    user.setUserId(payerInfoType.getPayer());
                                    user.setLanguage(action.getLocale().getLanguage());
                                    user.setLevel(action.getDefaultLevel());
                                    user.setCredit(0d);
                                    user.setVisits(1l);
                                    String password = User.generatePassword(8);
                                    user.setPassword(SomeUtils.encrypt3Des(password,action.getEncryptionKey()));
                                    action.getDao().save(user);
                                    action.getDao().flushSession();

                                    // generar welcome email
                                    action.sendWelcomeMail(user, password);
                                }
                                action.setFrontUser(user);
                            }

                            AddressType addressType = payerInfoType.getAddress();
                            UserAddress userAddress = user.findAddress(addressType.getStreet1(), addressType.getCityName(), addressType.getPostalCode());
                            if (userAddress==null) {
                                userAddress = new UserAddress();
                                userAddress.setAddress(addressType.getStreet1());
                                userAddress.setAddress2(addressType.getStreet2());
                                userAddress.setBilling(true);
                                userAddress.setCity(addressType.getCityName());
                                userAddress.setCode("");
                                userAddress.setPhone(addressType.getPhone());
                                userAddress.setFirstname(user.getFirstname());
                                userAddress.setLastname(user.getLastname());
                                if (addressType!=null && addressType.getCountry()!=null && addressType.getCountry().getValue()!=null)
                                    userAddress.setIdCountry(addressType.getCountry().getValue().toString());
                                else if (payerInfoType.getPayerCountry()!=null && payerInfoType.getPayerCountry().getValue()!=null) {
                                    userAddress.setIdCountry(payerInfoType.getPayerCountry().getValue().toString());
                                }
                                userAddress.setShipping(true);
                                State state = action.getDao().getStateByName(userAddress.getIdCountry(), addressType.getStateOrProvince());
                                if (state==null) {
                                    state = new State();
                                    state.setCountryCode(addressType.getCountry().getValue().toString().toUpperCase());
                                    state.setStateCode(addressType.getStateOrProvince().toUpperCase().substring(2));
                                    state.setStateName(addressType.getStateOrProvince());
                                    state.setInventaryCode(action.getStoreCode());
                                    action.getDao().save(state);
                                }
                                userAddress.setState(state);
                                userAddress.setZipCode(addressType.getPostalCode());
                                userAddress.setUser(user);
                                action.getDao().save(userAddress);
                                action.getDao().flushSession();
                            }

                            action.getUserSession().setShippingAddress(userAddress);
                            action.getUserSession().setBillingAddress(userAddress);
                            action.getUserSession().setShippingType("billing");

                            action.updateShoppingCartInSession();

                            map.put("result","address");
                        } else {
                            // Error paypal no devolvio nada
                            LOG.error("Paypal Response - Empty response from Paypal");
                        }
                    } catch (Exception e) {
                        LOG.error(e.getMessage(), e);
                    }
                } else {
                    // Error paypal no devolvio el token en la redireccion
                    LOG.error("Paypal Response - Empty token from Paypal");
                }
                LOG.info("Paypal Response - End");
            } else if (ACTION_CANCEL.equalsIgnoreCase((String) map.get("actionName"))) {
                LOG.info("Paypal Cancel Executed");
                map.put("result","shopcart");
            }
            return true;
        }
        return false;
    }

}
