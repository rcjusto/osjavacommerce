package org.store.core.front;

import com.octo.captcha.service.CaptchaServiceException;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.struts2.json.JSONUtil;
import org.apache.struts2.util.TokenHelper;
import org.store.core.beans.*;
import org.store.core.beans.mail.MFriend;
import org.store.core.beans.mail.MUser;
import org.store.core.beans.mail.MWishlist;
import org.store.core.beans.utils.BreadCrumb;
import org.store.core.beans.utils.DataNavigator;
import org.store.core.dto.StateDTO;
import org.store.core.globals.CaptchaServiceSingleton;
import org.store.core.globals.SomeUtils;
import org.store.core.globals.StoreMessages;
import org.store.core.globals.StoreSessionInterceptor;
import org.store.core.mail.MailSenderThreat;
import org.store.core.utils.carriers.CarrierService;
import org.store.core.utils.carriers.CarrierUtils;
import org.store.core.utils.events.EventService;
import org.store.core.utils.events.EventUtils;
import org.store.sslplugin.annotation.Unsecured;
import org.hibernate.Session;
import org.store.core.hibernate.HibernateSessionFactory;
import javax.servlet.http.Cookie;
import java.io.ByteArrayInputStream;
import java.util.*;


public class UserAction extends FrontModuleAction implements StoreMessages {

    @Override
    public void prepare() throws Exception {
        super.prepare();
        address = (UserAddress) dao.get(UserAddress.class, idAddress);
        rma = (Rma) dao.get(Rma.class, idRma);
    }

    public String gotoProfile() throws Exception {
        user = getFrontUser();
        if (user != null && !user.getAnonymousCheckout()) {
            EventUtils.executeEvent(getServletContext(), EventService.EVENT_VISIT_PROFILE, this);
            getBreadCrumbs().add(new BreadCrumb("profile", getText("profile.page.title", "My Profile"), null, null));
            return SUCCESS;
        }
        return "register";
    }

    public String profileSaveAddress() throws Exception {
        user = getFrontUser();
        if (user == null) return "register";
        if (address != null && (address.getUser() == null || user.equals(address.getUser()))) {
            address.setState((State) dao.get(State.class, addressState));
            address.setUser(user);
            dao.save(address);
        }
        return SUCCESS;
    }

    public String login() throws Exception {
        if (StringUtils.isNotEmpty(loginemail) && StringUtils.isNotEmpty(loginpassword)) {
            user = dao.getUserByUserId(loginemail);
            if (user != null) {
                String encPwdMD5 = DigestUtils.md5Hex(loginpassword);
                String encPwdDES = SomeUtils.encrypt3Des(loginpassword, getEncryptionKey());
                if (encPwdDES.equalsIgnoreCase(user.getPassword()) || encPwdMD5.equalsIgnoreCase(user.getPassword()) || loginpassword.equalsIgnoreCase(user.getPassword())) {
                    if (user.getBlocked()) {
                        addActionError(getText(CNT_ERROR_USER_BLOCKED, CNT_DEFAULT_ERROR_USER_BLOCKED));
                        return INPUT;
                    } else {
                        user.setLastIP(getRequest().getRemoteAddr());
                        user.setLastBrowser(getBrowser(getRequest()));
                        user.addVisit();
                        dao.save(user);
                        setFrontUser(user);
                        if (rememberMe != null && rememberMe) {
                            Cookie c = new Cookie(StoreSessionInterceptor.COOKIE_USER, SomeUtils.encrypt3Des(user.getUserId(), getEncryptionKey()));
                            c.setMaxAge(63271836);
                            getResponse().addCookie(c);
                        } else {
                            Cookie cu = new Cookie(StoreSessionInterceptor.COOKIE_USER, "");
                            cu.setMaxAge(0);
                            response.addCookie(cu);
                        }
                        EventUtils.executeEvent(getServletContext(), EventService.EVENT_LOGIN, this);
                        return (StringUtils.isNotEmpty(redirectUrl)) ? "redirect" : SUCCESS;
                    }
                }
            }
        }

        addActionError(getText(CNT_ERROR_LOGIN, CNT_DEFAULT_ERROR_LOGIN));
        return INPUT;
    }

    public String loginAjax() throws Exception {
        jsonResult = new HashMap<String, String>();
        String result = login();
        if (SUCCESS.equalsIgnoreCase(result)) {
            jsonResult.put("result","ok");
        } else {
            String error = "";
            for(String err : getActionErrors()) error = err; // mostrar solo el ultimo
            jsonResult.put("result","error");
            jsonResult.put("message",error);
        }
        return SUCCESS;
    }

    public String register() throws Exception {
        String canRegister = getStoreProperty(StoreProperty.PROP_ALLOW_REGISTRATIONS, StoreProperty.PROP_DEFAULT_ALLOW_REGISTRATIONS);
        if (!"Y".equalsIgnoreCase(canRegister)) {
            addActionError(getText(CNT_ERROR_REGISTRATIONS_NOT_ALLOWED, CNT_DEFAULT_ERROR_REGISTRATIONS_NOT_ALLOWED));
            return INPUT;
        }

        boolean useEmailAsLogin = "Y".equalsIgnoreCase(getStoreProperty(StoreProperty.PROP_USE_EMAIL_AS_LOGIN, StoreProperty.PROP_DEFAULT_USE_EMAIL_AS_LOGIN));

        if ("Y".equalsIgnoreCase(getStoreProperty("register.show.captcha", "Y"))) {
            String privateKey = getStoreProperty(StoreProperty.RECAPTCHA_PRIVATE, null);
            if (StringUtils.isNotEmpty(privateKey)) {
                String reCaptchaResponse = request.getParameter("g-recaptcha-response");
                if (!SomeUtils.reCaptcha2(privateKey, request.getRemoteAddr(), reCaptchaResponse)) {
                    addFieldError("captcha", getText(CNT_ERROR_CAPTCHA_INVALID, CNT_DEFAULT_ERROR_CAPTCHA_INVALID));
                }
            }
        }

        if (useEmailAsLogin) userId = email;
        user = dao.getUserByUserId(userId);
        State billState = (State) dao.get(State.class, billingState);
        State shipState = (State) dao.get(State.class, shippingState);
        if (user != null) {
            if (useEmailAsLogin) addFieldError("email", getText(CNT_ERROR_EMAIL_EXIST, CNT_DEFAULT_ERROR_EMAIL_EXIST));
            else addFieldError("userId", getText(CNT_ERROR_USER_ID_EXIST, CNT_DEFAULT_ERROR_USER_ID_EXIST));
        }
        if (StringUtils.isEmpty(email)) addFieldError("email", getText(CNT_ERROR_FIELD_EMPTY, CNT_DEFAULT_ERROR_FIELD_EMPTY));
        else {
            if (StringUtils.isEmpty(emailRepeat)) addFieldError("emailRepeat", getText(CNT_ERROR_FIELD_EMPTY, CNT_DEFAULT_ERROR_FIELD_EMPTY));
            if (!StringUtils.equals(email, emailRepeat)) addFieldError("emailRepeat", getText(CNT_ERROR_FIELD_INVALID_CONFIRM, CNT_DEFAULT_ERROR_FIELD_INVALID_CONFIRM));
        }
        if (StringUtils.isEmpty(password)) addFieldError("password", getText(CNT_ERROR_FIELD_EMPTY, CNT_DEFAULT_ERROR_FIELD_EMPTY));
        else {
            if (StringUtils.isEmpty(passwordRepeat)) addFieldError("passwordRepeat", getText(CNT_ERROR_FIELD_EMPTY, CNT_DEFAULT_ERROR_FIELD_EMPTY));
            if (!StringUtils.equals(password, passwordRepeat)) addFieldError("passwordRepeat", getText(CNT_ERROR_FIELD_INVALID_CONFIRM, CNT_DEFAULT_ERROR_FIELD_INVALID_CONFIRM));
        }
        if (hasErrors()) return INPUT;


        user = new User();
        user.setInventaryCode(getStoreCode());
        user.setLastBrowser(getBrowser(getRequest()));
        user.setLastIP(getRequest().getRemoteAddr());
        user.setAdmin(false);
        user.setBirthday(SomeUtils.strToDate(birthday, getLocale().getLanguage()));
        if (billingAdd != null) {
            user.setCompanyName(billingAdd.getCompany());
            user.setTitle(billingAdd.getTitle());
            user.setFirstname(billingAdd.getFirstname());
            user.setLastname(billingAdd.getLastname());
            user.setPhone(billingAdd.getPhone());
        }
        user.setCredit(0.0d);
        user.setEmail(email);
        user.setUserId(userId);
        user.setPassword(SomeUtils.encrypt3Des(password, getEncryptionKey()));
        user.setRegisterDate(SomeUtils.today());
        user.setSex(sex);
        user.setVisits(1l);
        user.setWebsite(website);

        user.setLevelRequested(levelRequested);
        // Verificar si el nivel necesita aprobacion
        UserLevel l = dao.getUserLevel(levelRequested);
        boolean generateRequestLevelEmail = false;
        if (l != null && !l.getNeedApproval()) {
            user.setLevel(l);
            user.setLevelStatus(STATUS_APPROVED);
        } else {
            user.setLevel(getDefaultLevel());
            user.setLevelStatus(STATUS_REQUESTED);
            generateRequestLevelEmail = true;
        }

        dao.save(user);

        // save billing address
        if (billingAdd != null) {
            billingAdd.setState(billState);
            billingAdd.setUser(user);
            billingAdd.setBilling(true);
            billingAdd.setShipping("y".equalsIgnoreCase(shippingSameBilling));
            dao.save(billingAdd);
        }

        // save shipping address
        if (shippingAdd != null && !"y".equalsIgnoreCase(shippingSameBilling)) {
            shippingAdd.setState(shipState);
            shippingAdd.setUser(user);
            shippingAdd.setBilling(false);
            shippingAdd.setShipping(true);
            dao.save(shippingAdd);
        }

        // preferences
        if (preferenceCode != null && preferenceCode.length > 0) {
            for (int i = 0; i < preferenceCode.length; i++) {
                Long idQ = SomeUtils.strToLong(preferenceCode[i]);
                Long idA = (preferenceValue != null && preferenceValue.length > i) ? SomeUtils.strToLong(preferenceValue[i]) : null;
                InquiryQuestion question = (InquiryQuestion) dao.get(InquiryQuestion.class, idQ);
                InquiryAnswer answer = (InquiryAnswer) dao.get(InquiryAnswer.class, idA);
                if (question != null) {
                    InquiryAnswerUser iau = dao.getUserQuestionAnswer(user, question);
                    if (answer == null && iau != null) {
                        dao.delete(iau);
                    } else if (answer != null) {
                        if (iau == null) {
                            iau = new InquiryAnswerUser();
                            iau.setUser(user);
                        }
                        iau.setAnswer(answer);
                        dao.save(iau);
                    }
                }
            }
        }

        if ("yes".equalsIgnoreCase(registerSubscription)) {
            UserPreference up = new UserPreference();
            up.setPreferenceCode("register.subscriptions");
            up.setPreferenceValue(registerSubscription);
            up.setUser(user);
            dao.save(up);
        }

        // referral
        String referralEmail = "";
        List<UserFriends> referrals = dao.getReferrals(user.getEmail());
        if (referrals != null && !referrals.isEmpty()) {
            boolean referredFound = false;
            for (UserFriends uf : referrals) {
                uf.setFriend(user);
                if (!referredFound && StringUtils.isNotEmpty(secretCode) && secretCode.equalsIgnoreCase(uf.getSecretCode())) {
                    uf.setReferred(true);
                    referralEmail = uf.getUser().getEmail();
                    referredFound = true;
                }
                dao.save(uf);

                UserFriends rf = new UserFriends();
                rf.setUser(user);
                rf.setFriend(uf.getUser());
                rf.setReferred(false);
                dao.save(rf);
            }
        }

        // put admin as friend and welcome message
        Map rewardsConfig = (Map) request.getAttribute("rewardsConfig");
        if (rewardsConfig != null && rewardsConfig.containsKey("contactUser")) {
            User referralAdmin = dao.getUserByUserId((String) rewardsConfig.get("contactUser"));
            if (referralAdmin != null && StringUtils.isNotEmpty(referralAdmin.getEmail())) {
                if (!referralAdmin.getEmail().equalsIgnoreCase(referralEmail)) {
                    UserFriends uf = new UserFriends();
                    uf.setUser(user);
                    uf.setFriend(referralAdmin);
                    dao.save(uf);
                }

                String text = rewardsConfig.containsKey("welcomeText_" + getLocale().getLanguage()) ? (String) rewardsConfig.get("welcomeText_" + getLocale().getLanguage()) : null;
                if (StringUtils.isNotEmpty(text)) {
                    UserMessages msg = new UserMessages();
                    msg.setFromUser(referralAdmin);
                    msg.setToUser(user);
                    msg.setCreated(Calendar.getInstance().getTime());
                    msg.setMessage(text);
                    dao.save(msg);
                }

            }
        }

        sendWelcomeMail(user, password);
        if (generateRequestLevelEmail) generateUserRequestLevelEmail(user);
        setFrontUser(user);
        EventUtils.executeEvent(getServletContext(), EventService.EVENT_REGISTER, this);
        return (StringUtils.isNotEmpty(redirectUrl)) ? "redirect" : SUCCESS;
    }

    public String registerForNewsletter() throws Exception {
        String canRegister = getStoreProperty(StoreProperty.PROP_ALLOW_NEWSLETTERS, StoreProperty.PROP_DEFAULT_ALLOW_NEWSLETTERS);
        if (!"Y".equalsIgnoreCase(canRegister)) {
            code = getText(CNT_ERROR_REGISTRATIONS_NOT_ALLOWED, CNT_DEFAULT_ERROR_REGISTRATIONS_NOT_ALLOWED);
        } else {
            if (StringUtils.isEmpty(email)) {
                code = getText(CNT_ERROR_EMAIL_EXIST, CNT_DEFAULT_ERROR_EMAIL_EXIST);
            } else {
                user = dao.getUserByUserId(email);
                if (user == null) {
                    user = new User();
                    user.setInventaryCode(getStoreCode());
                    user.setLastBrowser(getBrowser(getRequest()));
                    user.setLastIP(getRequest().getRemoteAddr());
                    user.setLevel(getDefaultLevel());
                    user.setAdmin(false);
                    user.setCredit(0.0d);
                    user.setEmail(email);
                    user.setUserId(email);
                    String aPassword = User.generatePassword(8);
                    user.setPassword(SomeUtils.encrypt3Des(aPassword, getEncryptionKey()));
                    user.setRegisterDate(SomeUtils.today());
                    user.setVisits(1l);
                    dao.save(user);

                    UserPreference pref = new UserPreference();
                    pref.setUser(user);
                    pref.setPreferenceCode("register.subscriptions");
                    pref.setPreferenceValue("yes");
                    dao.save(pref);

                    // Send welcome email
                    Map<String, Object> map = new HashMap<String, Object>();
                    map.put("user", new MUser(user, this));
                    map.put("password", aPassword);
                    String body = proccessVelocityTemplate(Mail.MAIL_TEMPLATE_USER_WELCOME_NEWSLETTER, map);
                    Mail mail = new Mail();
                    mail.setInventaryCode(getStoreCode());
                    mail.setBody(body);
                    mail.setSubject(getText(CNT_SUBJECT_WELCOME_NEWSLETTER, CNT_DEFAULT_SUBJECT_WELCOME_NEWSLETTER));
                    mail.setToAddress(user.getEmail());
                    mail.setPriority(Mail.PRIORITY_HIGH);
                    mail.setReference("WELCOME " + user.getIdUser());
                    Session hSession = HibernateSessionFactory.getSessionAutoCommit(getDatabaseConfig());
                    hSession.saveOrUpdate(mail);
                    hSession.close();
                    MailSenderThreat.asyncSendMail(mail, this);
                    code = "ok";
                    EventUtils.executeEvent(getServletContext(), EventService.EVENT_REGISTER, this);
                } else {
                    code = getText(CNT_ERROR_EMAIL_EXIST, CNT_DEFAULT_ERROR_EMAIL_EXIST);
                }
            }
        }
        return SUCCESS;
    }

    public String sendPassword() throws Exception {
        jsonResult = new HashMap<String, String>();
        if (StringUtils.isNotEmpty(email)) {
            jsonResult.put("email", email);
            user = dao.getUserByEmail(email);
            if (user != null) {
                String forgetAction = getStoreProperty(StoreProperty.PROP_FORGET_PASSWORD_ACTION, StoreProperty.PROP_DEFAULT_FORGET_PASSWORD_ACTION);
                if (StoreProperty.PROP_FORGET_ACTION_SEND.equals(forgetAction)) {
                    // Send email with password
                    String password;
                    try {
                        password = SomeUtils.decrypt3Des(user.getPassword(), getEncryptionKey());
                    } catch (Exception e) {
                        password = user.getPassword();
                    }
                    Map<String, Object> map = new HashMap<String, Object>();
                    map.put("user", new MUser(user, this));
                    map.put("password", password);
                    String body = proccessVelocityTemplate(Mail.MAIL_TEMPLATE_USER_CREDENTIALS, map);
                    Mail mail = new Mail();
                    mail.setInventaryCode(getStoreCode());
                    mail.setBody(body);
                    mail.setSubject(getText(CNT_SUBJECT_CREDENTIALS, CNT_DEFAULT_SUBJECT_CREDENTIALS));
                    mail.setToAddress(user.getEmail());
                    mail.setPriority(Mail.PRIORITY_HIGH);
                    mail.setReference("CREDENTIALS " + user.getIdUser());
                    Session hSession = HibernateSessionFactory.getSessionAutoCommit(getDatabaseConfig());
                    hSession.saveOrUpdate(mail);
                    hSession.close();
                    if (!MailSenderThreat.sendPendingMail(mail, this))
                        jsonResult.put("error", getText(CNT_ERROR_EMAIL_NOT_SENT, CNT_DEFAULT_ERROR_EMAIL_NOT_SENT));
                } else {
                    // send email with link to reset
                    Map<String, Object> map = new HashMap<String, Object>();
                    map.put("user", new MUser(user, this));
                    String body = proccessVelocityTemplate(Mail.MAIL_TEMPLATE_RESET_PASSWORD, map);
                    Mail mail = new Mail();
                    mail.setInventaryCode(getStoreCode());
                    mail.setBody(body);
                    mail.setSubject(getText(CNT_SUBJECT_RESET_PASSWORD, CNT_DEFAULT_SUBJECT_RESET_PASSWORD));
                    mail.setToAddress(user.getEmail());
                    mail.setPriority(Mail.PRIORITY_HIGH);
                    mail.setReference("RESET PASSWORD " + user.getIdUser());
                    Session hSession = HibernateSessionFactory.getSessionAutoCommit(getDatabaseConfig());
                    hSession.saveOrUpdate(mail);
                    hSession.close();
                    if (!MailSenderThreat.sendPendingMail(mail, this))
                        jsonResult.put("error", getText(CNT_ERROR_EMAIL_NOT_SENT, CNT_DEFAULT_ERROR_EMAIL_NOT_SENT));
                }

            } else {
                jsonResult.put("error", getText(CNT_ERROR_EMAIL_NOT_FOUND, CNT_DEFAULT_ERROR_EMAIL_NOT_FOUND));
            }
        }
        return SUCCESS;
    }

    public String resetPassword() throws Exception {
        if (StringUtils.isNotEmpty(code)) {
            String userId = SomeUtils.decrypt3Des(code, getEncryptionKey());
            User user = dao.getUserByUserId(userId);
            if (user != null) {
                if (user.getBlocked()) {
                    addActionError(getText(CNT_ERROR_USER_BLOCKED, CNT_DEFAULT_ERROR_USER_BLOCKED));
                } else {
                    user.setLastIP(getRequest().getRemoteAddr());
                    user.setLastBrowser(getBrowser(getRequest()));
                    user.addVisit();
                    dao.save(user);
                    setFrontUser(user);
                    EventUtils.executeEvent(getServletContext(), EventService.EVENT_LOGIN, this);
                    return SUCCESS;
                }
            } else {
                addActionError(getText(CNT_ERROR_USER_NOT_FOUND, CNT_DEFAULT_ERROR_USER_NOT_FOUND));
            }
        } else {
            addActionError(getText(CNT_ERROR_INCORRECT_PARAMETERS, CNT_DEFAULT_ERROR_INCORRECT_PARAMETERS));
        }
        return "home";
    }

    public String sendComment() throws Exception {
        // Si no hay usuario validar q puso el correo
        if (getFrontUser() == null) {
            if (StringUtils.isEmpty(mailFrom)) {
                addActionError(getText(GeneralAction.CNT_ERROR_MAILFROM_REQUIRED, GeneralAction.CNT_DEFAULT_ERROR_MAILFROM_REQUIRED));
                return INPUT;
            }
            if (StringUtils.isEmpty(mailFromName)) {
                addActionError(getText(GeneralAction.CNT_ERROR_FROMNAME_REQUIRED, GeneralAction.CNT_DEFAULT_ERROR_FROMNAME_REQUIRED));
                return INPUT;
            }
        } else {
            if (StringUtils.isEmpty(mailFrom)) mailFrom = getFrontUser().getEmail();
            if (StringUtils.isEmpty(mailFromName)) mailFromName = getFrontUser().getFullName();
        }
        if (StringUtils.isEmpty(mailComment)) {
            addActionError(getText(CNT_ERROR_EMPTY_COMMENT, CNT_DEFAULT_ERROR_EMPTY_COMMENT));
            return INPUT;
        }

        String privateKey = getStoreProperty(StoreProperty.RECAPTCHA_PRIVATE, null);
        if (StringUtils.isNotEmpty(privateKey)) {
            String reCaptchaResponse = request.getParameter("g-recaptcha-response");
            if (!SomeUtils.reCaptcha2(privateKey, request.getRemoteAddr(), reCaptchaResponse)) {
                addActionError(getText(CNT_ERROR_CAPTCHA_INVALID, CNT_DEFAULT_ERROR_CAPTCHA_INVALID));
                return INPUT;
            }
        }

        if (StringUtils.isEmpty(mailSubject)) mailSubject = getText(CNT_MAIL_SUBJECT_COMMENT, CNT_DEFAULT_MAIL_SUBJECT_COMMENT);

        if (StringUtils.isNotEmpty(getStoreProperty(StoreProperty.PROP_MAIL_CONTACT_US, null))) {
            StringBuilder buff = new StringBuilder();
            buff.append("<p>From: ").append(mailFromName).append(" (<a href='mailto:").append(mailFrom).append("'>").append(mailFrom).append("</a>)</p>");
            buff.append("<p>").append(mailComment).append("</p>");
            Mail mail = new Mail();
            mail.setInventaryCode(getStoreCode());
            mail.setBody(buff.toString());
            mail.setSubject(mailSubject);
            mail.setToAddress(getStoreProperty(StoreProperty.PROP_MAIL_CONTACT_US, ""));
            mail.setPriority(Mail.PRIORITY_MEDIUM);
            mail.setReference("CUSTOMER COMMENTS");
            Session hSession = HibernateSessionFactory.getSessionAutoCommit(getDatabaseConfig());
            hSession.saveOrUpdate(mail);
            hSession.close();
            MailSenderThreat.sendPendingMail(mail, this);
            addToStack("mailSent", "Y");
        }

        return SUCCESS;
    }

    /**
     * Save a customer comment
     *
     * @return
     * @throws Exception
     */
    public String comment() throws Exception {
        jsonResult = new HashMap<String, String>();
        if (userComment != null && StringUtils.isNotEmpty(userComment.getComment())) {
            if (getFrontUser() != null) userComment.setUser(getFrontUser());
            userComment.setCommentStatus(UserComment.STATUS_NEW);
            userComment.setCreated(Calendar.getInstance().getTime());
            dao.save(userComment);
            jsonResult.put("result", "ok");
        } else {
            jsonResult.put("result", "error");
            jsonResult.put("message", "error");
        }
        return SUCCESS;
    }

    public String comments() throws Exception {
        DataNavigator nav = new DataNavigator("comments");
        nav.setListado(dao.getComments(nav,code,UserComment.STATUS_ACTIVE,null,null,null));
        addToStack("comments", nav);
        return SUCCESS;
    }

    public String saveprofile() throws Exception {
        user = getFrontUser();
        if (user == null) return "register";
        // Salvar

        boolean useEmailAsLogin = "Y".equalsIgnoreCase(getStoreProperty(StoreProperty.PROP_USE_EMAIL_AS_LOGIN, StoreProperty.PROP_DEFAULT_USE_EMAIL_AS_LOGIN));
        if (StringUtils.isEmpty(email)) {
            addFieldError("email", getText(CNT_ERROR_FIELD_EMPTY, CNT_DEFAULT_ERROR_FIELD_EMPTY));
            return INPUT;
        } else if (useEmailAsLogin) {
            userId = email;
        }
        if (StringUtils.isEmpty(userId)) {
            if (useEmailAsLogin) addFieldError("email", getText(CNT_ERROR_FIELD_EMPTY, CNT_DEFAULT_ERROR_FIELD_EMPTY));
            else addFieldError("userId", getText(CNT_ERROR_FIELD_EMPTY, CNT_DEFAULT_ERROR_FIELD_EMPTY));
            return INPUT;
        }

        if (StringUtils.isNotEmpty(userId) && !userId.equalsIgnoreCase(user.getUserId())) {
            if (dao.getUserByUserId(userId) != null) {
                if (useEmailAsLogin) addFieldError("email", getText(CNT_ERROR_EMAIL_EXIST, CNT_DEFAULT_ERROR_EMAIL_EXIST));
                else addFieldError("userId", getText(CNT_ERROR_USER_ID_EXIST, CNT_DEFAULT_ERROR_USER_ID_EXIST));
                return INPUT;
            }
        }
        if (StringUtils.isNotEmpty(password) && password.equalsIgnoreCase(passwordRepeat)) {
            user.setPassword(SomeUtils.encrypt3Des(password, getEncryptionKey()));
        }
        user.setEmail(email);
        user.setUserId(userId);
        user.setLevelRequested(levelRequested);
        dao.save(user);

        if (preferenceCode != null && preferenceCode.length > 0) {
            for (int i = 0; i < preferenceCode.length; i++) {
                Long idQ = SomeUtils.strToLong(preferenceCode[i]);
                Long idA = (preferenceValue != null && preferenceValue.length > i) ? SomeUtils.strToLong(preferenceValue[i]) : null;
                InquiryQuestion question = (InquiryQuestion) dao.get(InquiryQuestion.class, idQ);
                InquiryAnswer answer = (InquiryAnswer) dao.get(InquiryAnswer.class, idA);
                if (question != null) {
                    InquiryAnswerUser iau = dao.getUserQuestionAnswer(user, question);
                    if (answer == null && iau != null) {
                        dao.delete(iau);
                    } else if (answer != null) {
                        if (iau == null) {
                            iau = new InquiryAnswerUser();
                            iau.setUser(user);
                        }
                        iau.setAnswer(answer);
                        dao.save(iau);
                    }
                }
            }
        }

        if ("Y".equalsIgnoreCase(getStoreProperty(StoreProperty.PROP_ALLOW_NEWSLETTERS, StoreProperty.PROP_DEFAULT_ALLOW_NEWSLETTERS))) {
            UserPreference up = dao.getUserPreference(user, "register.subscriptions");
            if ("yes".equalsIgnoreCase(registerSubscription) && up == null) {
                up = new UserPreference();
                up.setPreferenceCode("register.subscriptions");
                up.setPreferenceValue(registerSubscription);
                up.setUser(user);
                dao.save(up);
            } else if (!"yes".equalsIgnoreCase(registerSubscription) && up != null) {
                dao.delete(up);
            }
        }


        return SUCCESS;
    }

    public String payStepAnonymous() {
        State billState = (State) dao.get(State.class, billingState);
        State shipState = (State) dao.get(State.class, shippingState);
        if (StringUtils.isEmpty(email)) addFieldError("email", getText(CNT_ERROR_FIELD_EMPTY, CNT_DEFAULT_ERROR_FIELD_EMPTY));

        if (hasErrors()) return INPUT;

        user = new User();
        user.setInventaryCode(getStoreCode());
        user.setLastBrowser(getBrowser(getRequest()));
        user.setLastIP(getRequest().getRemoteAddr());
        user.setLevel(getAnonymousLevel());
        user.setAdmin(false);
        if (billingAdd != null) {
            user.setCompanyName(billingAdd.getCompany());
            user.setFirstname(billingAdd.getFirstname());
            user.setLastname(billingAdd.getLastname());
            user.setPhone(billingAdd.getPhone());
            user.setTitle(billingAdd.getTitle());
        }
        user.setCredit(0.0d);
        user.setEmail(email);
        user.setRegisterDate(SomeUtils.today());
        user.setVisits(1l);
        user.setAnonymousCheckout(true);
        dao.save(user);

        // save billing address
        if (billingAdd != null) {
            billingAdd.setState(billState);
            billingAdd.setUser(user);
            billingAdd.setBilling(true);
            billingAdd.setShipping("y".equalsIgnoreCase(shippingSameBilling));
            dao.save(billingAdd);
        }

        // save shipping address
        if (shippingAdd != null && !"y".equalsIgnoreCase(shippingSameBilling)) {
            shippingAdd.setState(shipState);
            shippingAdd.setUser(user);
            shippingAdd.setBilling(false);
            shippingAdd.setShipping(true);
            dao.save(shippingAdd);
        }
        setFrontUser(user);
        getUserSession().setBillingAddress(billingAdd);
        getUserSession().setShippingAddress(("y".equalsIgnoreCase(shippingSameBilling)) ? billingAdd : shippingAdd);
        EventUtils.executeEvent(getServletContext(), EventService.EVENT_REGISTER, this);
        return ("Y".equalsIgnoreCase(getStoreProperty(StoreProperty.PROP_PRODUCT_ONEPAGE_CHECKOUT, StoreProperty.PROP_DEFAULT_PRODUCT_ONEPAGE_CHECKOUT))) ? "onepage" : SUCCESS;
    }

    public String wishlist() throws Exception {
        Integer numRelatedProducts = SomeUtils.strToInteger(getStoreProperty(StoreProperty.PROP_SHOPCART_CROSS_SELLING_NUMBER, StoreProperty.PROP_DEFAULT_SHOPCART_CROSS_SELLING_NUMBER));
        user = getFrontUser();
        if (user == null) {
            setRedirectUrl(url("wishlist"));
            return "register";
        }

        // Acciones
        if (user.getWishList() != null) {
            if (wishListAction != null && "move_all".equalsIgnoreCase(wishListAction) && wishListProduct != null) {
                for (int i = 0; i < wishListProduct.length; i++) {
                    Product product = (Product) dao.get(Product.class, wishListProduct[i]);
                    if (product != null) {
                        UserWishList toMove = null;
                        for (UserWishList w : user.getWishList()) if (product.getIdProduct().equals(w.getIdProduct())) toMove = w;
                        Long quantity = (wishListQuantity != null && wishListQuantity.length > i) ? wishListQuantity[i] : 1;
                        if (addToCart(product, null, quantity.intValue())) {
                            user.getWishList().remove(toMove);
                            dao.delete(toMove);
                        }
                    }
                }
            } else if (wishListAction != null && "remove_all".equalsIgnoreCase(wishListAction)) {
                for (UserWishList w : user.getWishList()) dao.delete(w);
                user.setWishList(null);
            } else if (wishListAction != null && "move".equalsIgnoreCase(wishListAction) && wishListItem != null) {
                Long idProduct = (wishListProduct != null && wishListProduct.length > wishListItem) ? wishListProduct[wishListItem] : null;
                Long quantity = (wishListQuantity != null && wishListQuantity.length > wishListItem) ? wishListQuantity[wishListItem] : 1;
                Product product = (Product) dao.get(Product.class, idProduct);
                if (product != null) {
                    UserWishList toMove = null;
                    for (UserWishList w : user.getWishList()) if (product.getIdProduct().equals(w.getIdProduct())) toMove = w;
                    if (toMove != null) {
                        if (addToCart(product, null, quantity.intValue())) {
                            user.getWishList().remove(toMove);
                            dao.delete(toMove);
                        }
                    }
                } else {
                    addActionError("error.product.notfound");
                }
            } else if (wishListAction != null && "remove".equalsIgnoreCase(wishListAction) && wishListItem != null) {
                Long idProduct = (wishListProduct != null && wishListProduct.length > wishListItem) ? wishListProduct[wishListItem] : null;
                if (idProduct != null) {
                    UserWishList toMove = null;
                    for (UserWishList w : user.getWishList()) if (idProduct.equals(w.getIdProduct())) toMove = w;
                    if (toMove != null) {
                        user.getWishList().remove(toMove);
                        dao.delete(toMove);
                    }
                }
            }
        }

        List<Product> products = new ArrayList<Product>();
        List<Product> related = new ArrayList<Product>();
        if (user.getWishList() != null && !user.getWishList().isEmpty()) {
            for (UserWishList w : user.getWishList()) {
                Product product = (Product) dao.get(Product.class, w.getIdProduct());
                if (product != null) {
                    products.add(product);
                    List<ProductRelated> relatedProducts = getProductsRelated(product);
                    if (relatedProducts != null) {
                        for (ProductRelated r : relatedProducts) {
                            if (!productInWishList(r.getRelated().getIdProduct())) related.add(r.getRelated());
                            if (related.size() >= numRelatedProducts) break;
                        }
                    }
                }
            }
        }
        addToStack("wishlist", products);
        if (related.size() < numRelatedProducts) {
            List<Product> l = getRandomProducts("label:hot", 2 * numRelatedProducts);
            if (l != null)
                for (Product p : l) {
                    if (related.size() < numRelatedProducts && !related.contains(p)) related.add(p);
                    if (related.size() >= numRelatedProducts) break;
                }
        }

        addToStack("associatedProducts", related);
        EventUtils.executeEvent(getServletContext(), EventService.EVENT_VISIT_WISHLIST, this);
        getBreadCrumbs().add(new BreadCrumb("profile", getText("wishlist.page.title", "My Wish List"), null, null));
        return ("print".equalsIgnoreCase(output)) ? "print" : SUCCESS;
    }

    public String wishlistmail() throws Exception {
        user = getFrontUser();
        if (user == null) return "register";

        if (StringUtils.isEmpty(mailTo)) addActionError("error.mail.to.empty");
        if (getFrontUser() == null && StringUtils.isEmpty(mailFrom)) addActionError("error.mail.from.empty");
        if (getFrontUser() == null && StringUtils.isEmpty(mailFromName)) addActionError("error.mail.from.name.empty");

        if (hasErrors()) return SUCCESS;

        List<Product> products = new ArrayList<Product>();
        if (user.getWishList() != null && !user.getWishList().isEmpty()) {
            for (UserWishList w : user.getWishList()) {
                Product product = (Product) dao.get(Product.class, w.getIdProduct());
                if (product != null) {
                    products.add(product);
                }
            }
        }
        addToStack("wishlist", products);

        Map<String, Object> map = new HashMap<String, Object>();
        map.put("wishlist", new MWishlist(products, this));
        map.put("mailTo", mailTo);
        map.put("mailFrom", mailFrom);
        map.put("mailFromName", mailFromName);
        map.put("mailComment", mailComment);

        String body = proccessVelocityTemplate(Mail.MAIL_TEMPLATE_WISHLIST, map);
        Mail mail = new Mail();
        mail.setInventaryCode(getStoreCode());
        mail.setBody(body);
        mail.setSubject(getText(CNT_SUBJECT_WISHLIST, CNT_DEFAULT_SUBJECT_WISHLIST));
        mail.setFromAddress(mailFrom);
        mail.setToAddress(mailTo);
        mail.setPriority(Mail.PRIORITY_HIGH);
        mail.setReference("WISHLIST TO FRIEND ");
        dao.save(mail);
        MailSenderThreat.asyncSendMail(mail, this);
        addToStack("mailSent", 'Y');
        return SUCCESS;
    }

    public boolean addToCart(Product beanP1, ProductVariation beanV1, Integer quantity) {
        if (beanP1 != null && quantity != null) {
            long maxCanBuy = beanP1.getMaxToBuy(getFrontUser());
            if (maxCanBuy >= getUserSession().getNumProduct(beanP1.getIdProduct()) + quantity) {
                getUserSession().addItem(beanP1.getIdProduct(), (beanV1 != null) ? beanV1.getId() : null, quantity);
                updateShoppingCartInSession();
                EventUtils.executeEvent(getServletContext(), EventService.EVENT_ADD_TO_CART, this);
                return true;
            } else {
                if (maxCanBuy < 1) addActionError(getText(GeneralAction.CNT_ERROR_HASNO_STOCK, GeneralAction.CNT_DEFAULT_ERROR_HASNO_STOCK, new String[]{beanP1.getProductName(getLocale().getLanguage())}));
                else addActionError(getText(GeneralAction.CNT_ERROR_STOCK_LIMITED, GeneralAction.CNT_DEFAULT_ERROR_STOCK_LIMITED, new String[]{beanP1.getProductName(getLocale().getLanguage()), String.valueOf(maxCanBuy)}));
            }
        }
        return false;
    }

    public String countryStates() throws Exception {
        List<StateDTO> states = dao.getStatesDTOByCountry(country);
        String jsonStr = JSONUtil.serialize(states);
        setInputStream(new ByteArrayInputStream(jsonStr.getBytes()));
        setContentType("application/json");
        return SUCCESS;
    }

    public String countryStatesEx() throws Exception {
        List<StateDTO> states = dao.getStatesDTOByCountry(country);
        if (states.size()==1 && "*".equalsIgnoreCase(states.get(0).getStateName())) {
            addToStack("select", Boolean.FALSE);
            addToStack("idState", states.get(0).getIdState());
        } else {
            addToStack("select", Boolean.TRUE);
            addToStack("states", states);
        }
        if (stateName!=null) addToStack("stateName", stateName);
        if (addressState!=null) addToStack("addressState", addressState);
        if (fieldId!=null) addToStack("fieldId", fieldId);
        if (fieldName!=null) addToStack("fieldName", fieldName);
        if (fieldName!=null) addToStack("fieldNameEx", fieldNameEx);
        if (fieldClass!=null) addToStack("fieldClass", fieldClass);
        return SUCCESS;
    }

    public String logout() throws Exception {
        setFrontUser(null);
        Cookie c = new Cookie(StoreSessionInterceptor.COOKIE_USER, "");
        c.setMaxAge(0);
        getResponse().addCookie(c);

        return SUCCESS;
    }

    public String orders() throws Exception {
        if (getFrontUser() == null) {
            setRedirectUrl(url("orders"));
            return "register";
        }

        DataNavigator orders = new DataNavigator(getRequest(), "orders");
        orders.setListado(dao.getOrdersByUser(orders, getFrontUser()));
        addToStack("orders", orders);

        EventUtils.executeEvent(getServletContext(), EventService.EVENT_VISIT_ORDERS, this);
        getBreadCrumbs().add(new BreadCrumb("orders", getText("my.orders", "My Orders"), null, null));
        return SUCCESS;
    }

    public String order() throws Exception {
        if (getFrontUser() == null) return "register";

        if (idOrder != null) {
            Order order = (Order) dao.get(Order.class, idOrder);
            if (order != null && order.getUser() != null && order.getUser().equals(getFrontUser())) {
                addToStack("order", order);
                getBreadCrumbs().add(new BreadCrumb("orders", getText("my.orders", "My Orders"), url("orders"), null));
                getBreadCrumbs().add(new BreadCrumb("order", getText("order.data", "Order Information") + " #" + order.getIdOrder().toString(), null, null));
                EventUtils.executeEvent(getServletContext(), EventService.EVENT_VISIT_ORDER, this);
            } else {
                addActionError(getText(CNT_ERROR_ORDER_NOT_EXIST, CNT_DEFAULT_ERROR_ORDER_NOT_EXIST, new String[]{idOrder.toString()}));
            }
        }
        return ("print".equalsIgnoreCase(output)) ? "print" : SUCCESS;
    }

    public String tracking() throws Exception {
        if (getFrontUser() == null) return "register";
        OrderPacking pack = (OrderPacking) dao.get(OrderPacking.class, idPackageProduct);
        if (!getFrontUser().equals(pack.getOrder().getUser())) pack = null;

        if (pack != null) {
            addToStack("package", pack);
            if (StringUtils.isNotEmpty(pack.getCustomTracking())) {
                addToStack("tracking", pack.getCustomTracking());
            } else if (pack.getShippingMethod() != null && StringUtils.isNotEmpty(pack.getShippingMethod().getCarrierName())) {
                CarrierUtils cu = new CarrierUtils(getServletContext());
                CarrierService cs = cu.getCarrierService(pack.getShippingMethod().getCarrierName(), dao.getCarrierProperties(pack.getShippingMethod().getCarrierName()));
                String trackingText = cs.getHTMLTracking(pack.getTrackingNumber(), pack.getShippingMethod().getMethodCode());
                addToStack("tracking", trackingText);
            }
        } else {
            addActionError(getText(CNT_ERROR_PACKAGE_NOT_FOUND, CNT_DEFAULT_ERROR_PACKAGE_NOT_FOUND));
        }
        return SUCCESS;
    }

    public String addOrderToCart() throws Exception {
        if (getFrontUser() == null) return "register";

        if (idOrder != null) {
            Order order = (Order) dao.get(Order.class, idOrder);
            if (order != null && order.getUser() != null && order.getUser().equals(getFrontUser())) {
                if (order.getOrderDetails() != null && !order.getOrderDetails().isEmpty()) {
                    for (OrderDetail detail : order.getOrderDetails()) {
                        OrderDetailProduct p1 = (detail.getOrderDetailProducts() != null && detail.getOrderDetailProducts().size() > 0) ? detail.getOrderDetailProducts().get(0) : null;
                        OrderDetailProduct p2 = (detail.getOrderDetailProducts() != null && detail.getOrderDetailProducts().size() > 1) ? detail.getOrderDetailProducts().get(1) : null;

                        if (p1 != null && p1.getProduct() != null) {
                            Long idProduct1 = p1.getProduct().getIdProduct();
                            Long idProduct2 = (p2 != null && p2.getProduct() != null) ? p2.getProduct().getIdProduct() : null;
                            Long idVariation1 = p1.getIdVariation();
                            Long idVariation2 = (p2 != null) ? p2.getIdVariation() : null;
                            long maxCanBuy = p1.getProduct().getMaxToBuy(getFrontUser());
                            if (maxCanBuy >= getUserSession().getNumProduct(idProduct1) + detail.getQuantity()) {
                                getUserSession().addItem(idProduct1, idVariation1, idProduct2, idVariation2, detail.getQuantity(), detail.getSeldate(), detail.getSeltime(), null, null, false);
                                updateShoppingCartInSession();
                            } else {
                                if (maxCanBuy < 1) addSessionError(getText(GeneralAction.CNT_ERROR_HASNO_STOCK, GeneralAction.CNT_DEFAULT_ERROR_HASNO_STOCK, new String[]{p1.getProduct().getProductName(getLocale().getLanguage())}));
                                else addSessionError(getText(GeneralAction.CNT_ERROR_STOCK_LIMITED, GeneralAction.CNT_DEFAULT_ERROR_STOCK_LIMITED, new String[]{p1.getProduct().getProductName(getLocale().getLanguage()), String.valueOf(maxCanBuy)}));
                            }
                        }
                    }
                    getUserSession().initializeItems();
                }

                EventUtils.executeEvent(getServletContext(), EventService.EVENT_ADD_TO_CART, this);
            } else {
                addSessionError(getText(CNT_ERROR_ORDER_NOT_EXIST, CNT_DEFAULT_ERROR_ORDER_NOT_EXIST, new String[]{idOrder.toString()}));
            }
        }
        return SUCCESS;
    }

    public String rmaOrder() throws Exception {
        if (getFrontUser() == null) return "register";

        Order order = (Order) dao.get(Order.class, idOrder);
        if (order != null && getFrontUser().equals(order.getUser()) && order.getOrderDetails() != null) {
            List<RmaType> rmaTypes = dao.getRmaTypes();
            if (rmaTypes != null && !rmaTypes.isEmpty()) {
                List<OrderPackingProduct> listado = new ArrayList<OrderPackingProduct>();
                List<OrderPacking> packageList = dao.getOrderPackages(order);
                if (packageList != null) {
                    for (OrderPacking orderPackage : packageList) {
                        if (orderPackage.getDeliveryDate() != null && orderPackage.getDeliveryDate().before(SomeUtils.today()) && orderPackage.getPackingProductList() != null) {
                            for (OrderPackingProduct opp : orderPackage.getPackingProductList()) {
                                List<Rma> listaRmas = dao.getRmas(opp);
                                opp.addProperty("rmas", listaRmas);
                                // Verificar si se puede crear rmas
                                boolean canCreateRma = true;
                                if (listaRmas != null && !listaRmas.isEmpty()) for (Rma rma : listaRmas) if (rma.isOpen()) canCreateRma = false;
                                // Verificar si hay tipos de RMA disponibles
                                List<RmaType> availableTypes = new ArrayList<RmaType>();
                                if (canCreateRma) {
                                    for (RmaType type : rmaTypes) if (opp.canCreateRmaType(type)) availableTypes.add(type);
                                }
                                opp.addProperty("types", availableTypes);
                                opp.addProperty("can_create", canCreateRma && !availableTypes.isEmpty());
                                listado.add(opp);
                            }
                        }
                    }
                }
                addToStack("packageProducts", listado);
            }
            getBreadCrumbs().add(new BreadCrumb("orders", getText("my.orders", "My Orders"), url("orders"), null));
            Map<String, String> params = new HashMap<String, String>();
            params.put("idOrder", order.getIdOrder().toString());
            getBreadCrumbs().add(new BreadCrumb("order", getText("order.data", "Order Information") + " #" + order.getIdOrder().toString(), url("order", "", params), null));
            getBreadCrumbs().add(new BreadCrumb("rma", getText("order.rma", "RMAs"), null, null));
            addToStack("order", order);
        }

        return SUCCESS;
    }

    public String rmaDetail() throws Exception {
        if (getFrontUser() == null) return "register";

        // Si el rma no es de una orden del usuario conectado, no mostrarla
        if (rma != null && rma.getOrderProduct() != null &&
                rma.getOrderProduct().getPacking() != null &&
                rma.getOrderProduct().getPacking().getOrder() != null &&
                !getFrontUser().equals(rma.getOrderProduct().getPacking().getOrder().getUser())) {
            rma = null;
        }
        if (rma != null && rma.getOrderProduct() != null && rma.getOrderProduct().getOrder() != null) {
            addToStack("order", rma.getOrderProduct().getOrder());
            getBreadCrumbs().add(new BreadCrumb("orders", getText("my.orders", "My Orders"), url("orders"), null));
            Map<String, String> params = new HashMap<String, String>();
            params.put("idOrder", rma.getOrder().getIdOrder().toString());
            getBreadCrumbs().add(new BreadCrumb("order", getText("order.data", "Order Information") + " #" + rma.getOrder().getIdOrder().toString(), url("order", "", params), null));
            getBreadCrumbs().add(new BreadCrumb("rma", getText("order.rma", "RMAs"), url("rmaorder", "", params), null));
            getBreadCrumbs().add(new BreadCrumb("rma", getText("rma.detail", "RMA Detail"), null, null));
        }
        return SUCCESS;
    }

    public String rmaSave() throws Exception {
        if (getFrontUser() == null) return "register";

        if (rma != null) {
            dao.save(rma);

            if (StringUtils.isNotEmpty(rmaComment)) {
                RmaLog rmaLog = new RmaLog();
                rmaLog.setActionComments(rmaComment);
                rmaLog.setActionUser(getFrontUser());
                rmaLog.setRma(rma);
                dao.save(rmaLog);
            }

        }
        return SUCCESS;
    }

    public String rmaNew() throws Exception {
        if (getFrontUser() == null) return "register";

        OrderPackingProduct opp = (OrderPackingProduct) dao.get(OrderPackingProduct.class, idPackageProduct);
        RmaType rmaType = (RmaType) dao.get(RmaType.class, idRmaType);
        if (opp != null && opp.getOrder() != null && getFrontUser().equals(opp.getOrder().getUser()) && rmaType != null && opp.canCreateRmaType(rmaType)) {
            rma = new Rma();
            rma.setOrderProduct(opp);
            rma.setRmaType(rmaType);
            rma.setRmaStatus(Rma.STATUS_REQUESTED);
            rma.setRmaSerialNumber(rmaSerialNumber);
            dao.save(rma);

            RmaLog log = new RmaLog();
            log.setActionComments(rmaComment);
            log.setActionDate(SomeUtils.today());
            log.setActionUser(getFrontUser());
            log.setRma(rma);
            log.setRmaStatus(rma.getRmaStatus());
            dao.save(log);
        }
        return SUCCESS;
    }

    public String userAction() throws Exception {
        if (StringUtils.isNotEmpty(code)) {
            String cad = new String(Hex.decodeHex(code.toCharArray()));
            int pos = cad.indexOf(":");
            if (pos > -1) {
                String userId = cad.substring(0, pos);
                String pwd = cad.substring(pos + 1);
                user = dao.getUserByUserId(userId);
                if (user != null && pwd.equalsIgnoreCase(user.getPassword())) {

                    if ("cancelnewsletter".equalsIgnoreCase(act) && user.getPreferences() != null) {
                        for (UserPreference p : user.getPreferences()) {
                            if ("register.subscriptions".equalsIgnoreCase(p.getPreferenceCode())) {
                                p.setPreferenceValue("no");
                                dao.save(p);
                            }
                        }
                        addActionMessage(getText(CNT_MSG_SUBSCRIPTION_CANCELLED, CNT_DEFAULT_MSG_SUBSCRIPTION_CANCELLED));
                    }

                }
            }
        }
        return SUCCESS;
    }

    public String referToFriend() throws Exception {
        if (getFrontUser() == null) return "register";

        List<UserFriends> usersToSendEmail = new ArrayList<UserFriends>();
        if (newFriends != null && newFriends.length > 0) {
            List<String> usersAlreadyRegistered = new ArrayList<String>();
            List<String> usersAlreadyReferred = new ArrayList<String>();
            for (String m : newFriends)
                if (StringUtils.isNotEmpty(m)) {
                    User u = dao.getUserByEmail(m);
                    if (u != null) {
                        usersAlreadyRegistered.add(m);
                    } else if (dao.userHasReferred(m)) {
                        usersAlreadyReferred.add(m);
                    }
                    if (!dao.userHasFriend(getFrontUser(), m)) {
                        UserFriends userFriends = new UserFriends();
                        userFriends.setFriendEmail(m);
                        userFriends.setFriend(u);
                        userFriends.setSecretCode(User.generatePassword(8));
                        userFriends.setUser(getFrontUser());
                        dao.save(userFriends);
                        if (u == null) usersToSendEmail.add(userFriends);
                    }
                }
            if (!usersAlreadyRegistered.isEmpty()) addToStack("usersAlreadyRegistered", usersAlreadyRegistered);
            if (!usersAlreadyReferred.isEmpty()) addToStack("usersAlreadyReferred", usersAlreadyReferred);
        }

        if (!usersToSendEmail.isEmpty()) {
            String subject = getText(CNT_SUBJECT_REFER_FRIEND, CNT_DEFAULT_SUBJECT_REFER_FRIEND, new String[]{getFrontUser().getFullName(), getStoreProperty(StoreProperty.PROP_SITE_NAME, "this site")});
            for (UserFriends userFriend : usersToSendEmail) {
                Map<String, Object> map1 = new HashMap<String, Object>();
                map1.put("friend", new MFriend(userFriend, this));
                map1.put("mailFrom", mailFrom);
                map1.put("mailFromName", mailFromName);
                map1.put("mailComment", mailComment);
                String body = proccessVelocityTemplate(Mail.MAIL_TEMPLATE_REFER_TO_FRIEND, map1);

                Mail mail = new Mail();
                mail.setInventaryCode(getStoreCode());
                mail.setBody(body);
                mail.setSubject(subject);
                mail.setToAddress(userFriend.getFriendEmail());
                mail.setPriority(6);
                mail.setReference("REFER TO FRIEND");
                dao.save(mail);
                Map<String, Object> map = new HashMap<String, Object>();
                map.put("email", userFriend.getFriendEmail());
                EventUtils.executeEvent(getServletContext(), EventService.EVENT_REFER_FRIEND, this, map);
            }

            MailSenderThreat th = new MailSenderThreat(getStoreCode(), getDatabaseConfig());
            th.setPriority(6);
            th.start();

            addToStack("mailSent", 'Y');
        }

        return SUCCESS;
    }

    public String friendRegister() throws Exception {
        if (dao.getUserByEmail(email) != null) {
            if (StringUtils.isNotEmpty(email)) addToStack("loginemail", email);
        } else {
            if (user == null && StringUtils.isNotEmpty(email)) {
                user = new User();
                user.setEmail(email);
            }
        }
        if (StringUtils.isNotEmpty(secretCode)) addToStack("secretCode", secretCode);
        return "register";
    }

    public String friendData() throws Exception {
        if (getFrontUser() == null) return "register";

        user = (User) dao.get(User.class, idUser);
        if (user != null) {
            addToStack("friend", user);

            List<UserMessages> unread = dao.getFriendMessages(user, getFrontUser(), true);
            if (unread != null && !unread.isEmpty()) {
                addToStack("mensajes", unread);
                for (UserMessages m : unread) {
                    m.setReaded(true);
                    dao.save(m);
                }
            }

            List<UserMessages> allMessages = new ArrayList<UserMessages>();
            List<UserMessages> mensajes1 = dao.getFriendMessages(user, getFrontUser(), false);
            if (mensajes1 != null && !mensajes1.isEmpty()) allMessages.addAll(mensajes1);
            List<UserMessages> mensajes2 = dao.getFriendMessages(getFrontUser(), user, false);
            if (mensajes2 != null && !mensajes2.isEmpty()) allMessages.addAll(mensajes2);
            if (!allMessages.isEmpty()) {
                Collections.sort(allMessages, new Comparator<UserMessages>() {
                    public int compare(UserMessages o1, UserMessages o2) {
                        return o2.getCreated().compareTo(o1.getCreated());
                    }
                });
                addToStack("allMessages", allMessages.subList(0, Math.min(40, allMessages.size())));
            }

            List<UserRewardHistory> earns = dao.getEarnsFromFriend(getFrontUser(), user);
            if (earns != null && !earns.isEmpty()) addToStack("earns", earns);
        }
        return SUCCESS;
    }

    public String messageToUser() throws Exception {
        if (getFrontUser() == null) return "register";

        boolean validToken;
        synchronized (session) {
            validToken = TokenHelper.validToken();
        }
        user = (User) dao.get(User.class, idUser);
        if (validToken && user != null && StringUtils.isNotEmpty(customMessage)) {
            UserMessages message = new UserMessages();
            message.setFromUser(getFrontUser());
            message.setToUser(user);
            message.setMessage(customMessage);
            dao.save(message);
            addToStack("msgSent", "OK");
        }
        return friendData();
    }

    public String rewardHistory() throws Exception {
        if (getFrontUser() == null) return "register";

        DataNavigator nav = new DataNavigator(request, "rewards");
        nav.setListado(dao.getRewardsHistory(nav, getFrontUser()));
        addToStack("rewards", nav);

        return SUCCESS;
    }

    public String friendsSalesReport() throws Exception {
        if (getFrontUser() == null) return "register";
        List l = dao.getFriendSalesReport(getFrontUser(), 20);
        if (l != null) addToStack("products", l);
        return SUCCESS;
    }

    private Map<String, String> jsonResult;
    private Long idUser;
    private User user;
    private String userId;
    private String email;
    private String emailRepeat;
    private String password;
    private String passwordRepeat;
    private String birthday;
    private String website;
    private String sex;
    private String shippingSameBilling;
    private String levelRequested;
    private Long billingState;
    private Long shippingState;
    private Boolean rememberMe;
    private String registerSubscription;

    private UserAddress billingAdd;
    private UserAddress shippingAdd;

    private String country;

    private String loginemail;
    private String loginpassword;

    private String[] preferenceCode;
    private String[] preferenceValue;

    private Long idOrder;
    private String output;

    private UserAddress address;
    private Long idAddress;
    private Long addressState;

    private Long[] wishListProduct;
    private Long[] wishListQuantity;
    private String wishListAction;
    private Integer wishListItem;

    private Rma rma;
    private Long idRma;
    private Long idPackageProduct;
    private Long idRmaType;
    private String rmaComment;
    private String rmaSerialNumber;

    private String code;
    private String act;

    private String mailFromName;
    private String mailFrom;
    private String mailTo;
    private String mailComment;
    private String mailSubject;

    private String captcha;
    private String secretCode;
    private String customMessage;

    private String fieldId;
    private String fieldName;
    private String fieldNameEx;
    private String fieldClass;
    private String stateName;

    private String[] newFriends;

    private UserComment userComment;

    public UserComment getUserComment() {
        return userComment;
    }

    public void setUserComment(UserComment userComment) {
        this.userComment = userComment;
    }

    public Long getIdUser() {
        return idUser;
    }

    public void setIdUser(Long idUser) {
        this.idUser = idUser;
    }

    public String[] getNewFriends() {
        return newFriends;
    }

    public void setNewFriends(String[] newFriends) {
        this.newFriends = newFriends;
    }

    public String getSecretCode() {
        return secretCode;
    }

    public void setSecretCode(String secretCode) {
        this.secretCode = secretCode;
    }

    public String getCustomMessage() {
        return customMessage;
    }

    public void setCustomMessage(String customMessage) {
        this.customMessage = customMessage;
    }

    public Map<String, String> getJsonResult() {
        return jsonResult;
    }

    public void setJsonResult(Map<String, String> jsonResult) {
        this.jsonResult = jsonResult;
    }

    public String getCaptcha() {
        return captcha;
    }

    public void setCaptcha(String captcha) {
        this.captcha = captcha;
    }

    public String getRmaSerialNumber() {
        return rmaSerialNumber;
    }

    public void setRmaSerialNumber(String rmaSerialNumber) {
        this.rmaSerialNumber = rmaSerialNumber;
    }

    public String getMailFromName() {
        return mailFromName;
    }

    public void setMailFromName(String mailFromName) {
        this.mailFromName = mailFromName;
    }

    public String getMailFrom() {
        return mailFrom;
    }

    public void setMailFrom(String mailFrom) {
        this.mailFrom = mailFrom;
    }

    public String getMailSubject() {
        return mailSubject;
    }

    public void setMailSubject(String mailSubject) {
        this.mailSubject = mailSubject;
    }

    public String getMailTo() {
        return mailTo;
    }

    public void setMailTo(String mailTo) {
        this.mailTo = mailTo;
    }

    public String getMailComment() {
        return mailComment;
    }

    public void setMailComment(String mailComment) {
        this.mailComment = mailComment;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getAct() {
        return act;
    }

    public void setAct(String act) {
        this.act = act;
    }

    public Long getIdRma() {
        return idRma;
    }

    public void setIdRma(Long idRma) {
        this.idRma = idRma;
    }

    public Long getIdRmaType() {
        return idRmaType;
    }

    public void setIdRmaType(Long idRmaType) {
        this.idRmaType = idRmaType;
    }

    public String getRmaComment() {
        return rmaComment;
    }

    public void setRmaComment(String rmaComment) {
        this.rmaComment = rmaComment;
    }

    public Rma getRma() {
        return rma;
    }

    public void setRma(Rma rma) {
        this.rma = rma;
    }

    public Long getIdPackageProduct() {
        return idPackageProduct;
    }

    public void setIdPackageProduct(Long idPackageProduct) {
        this.idPackageProduct = idPackageProduct;
    }

    public Long[] getWishListProduct() {
        return wishListProduct;
    }

    public void setWishListProduct(Long[] wishListProduct) {
        this.wishListProduct = wishListProduct;
    }

    public Long[] getWishListQuantity() {
        return wishListQuantity;
    }

    public void setWishListQuantity(Long[] wishListQuantity) {
        this.wishListQuantity = wishListQuantity;
    }

    public String getWishListAction() {
        return wishListAction;
    }

    public void setWishListAction(String wishListAction) {
        this.wishListAction = wishListAction;
    }

    public Integer getWishListItem() {
        return wishListItem;
    }

    public void setWishListItem(Integer wishListItem) {
        this.wishListItem = wishListItem;
    }

    public UserAddress getAddress() {
        return address;
    }

    public void setAddress(UserAddress address) {
        this.address = address;
    }

    public Long getAddressState() {
        return addressState;
    }

    public void setAddressState(Long addressState) {
        this.addressState = addressState;
    }

    public Long getIdAddress() {
        return idAddress;
    }

    public void setIdAddress(Long idAddress) {
        this.idAddress = idAddress;
    }

    public String getOutput() {
        return output;
    }

    public void setOutput(String output) {
        this.output = output;
    }

    public Long getIdOrder() {
        return idOrder;
    }

    public void setIdOrder(Long idOrder) {
        this.idOrder = idOrder;
    }

    public String getLoginemail() {
        return loginemail;
    }

    public void setLoginemail(String loginemail) {
        this.loginemail = loginemail;
    }

    public String getLoginpassword() {
        return loginpassword;
    }

    public void setLoginpassword(String loginpassword) {
        this.loginpassword = loginpassword;
    }

    public Boolean getRememberMe() {
        return rememberMe;
    }

    public void setRememberMe(Boolean rememberMe) {
        this.rememberMe = rememberMe;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEmailRepeat() {
        return emailRepeat;
    }

    public void setEmailRepeat(String emailRepeat) {
        this.emailRepeat = emailRepeat;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPasswordRepeat() {
        return passwordRepeat;
    }

    public void setPasswordRepeat(String passwordRepeat) {
        this.passwordRepeat = passwordRepeat;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getLevelRequested() {
        return levelRequested;
    }

    public void setLevelRequested(String levelRequested) {
        this.levelRequested = levelRequested;
    }

    public String getShippingSameBilling() {
        return shippingSameBilling;
    }

    public void setShippingSameBilling(String shippingSameBilling) {
        this.shippingSameBilling = shippingSameBilling;
    }

    public UserAddress getBillingAdd() {
        return billingAdd;
    }

    public void setBillingAdd(UserAddress billingAdd) {
        this.billingAdd = billingAdd;
    }

    public UserAddress getShippingAdd() {
        return shippingAdd;
    }

    public void setShippingAdd(UserAddress shippingAdd) {
        this.shippingAdd = shippingAdd;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public Long getBillingState() {
        return billingState;
    }

    public void setBillingState(Long billingState) {
        this.billingState = billingState;
    }

    public Long getShippingState() {
        return shippingState;
    }

    public void setShippingState(Long shippingState) {
        this.shippingState = shippingState;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String[] getPreferenceCode() {
        return preferenceCode;
    }

    public void setPreferenceCode(String[] preferenceCode) {
        this.preferenceCode = preferenceCode;
    }

    public String[] getPreferenceValue() {
        return preferenceValue;
    }

    public void setPreferenceValue(String[] preferenceValue) {
        this.preferenceValue = preferenceValue;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getRegisterSubscription() {
        return registerSubscription;
    }

    public void setRegisterSubscription(String registerSubscription) {
        this.registerSubscription = registerSubscription;
    }

    public String getFieldId() {return fieldId;}

    public void setFieldId(String fieldId) {this.fieldId = fieldId;}

    public String getFieldName() {return fieldName;}

    public void setFieldName(String fieldName) {this.fieldName = fieldName;}

    public String getFieldClass() {return fieldClass;}

    public void setFieldClass(String fieldClass) {this.fieldClass = fieldClass;}

    public String getFieldNameEx() {return fieldNameEx;}

    public void setFieldNameEx(String fieldNameEx) {this.fieldNameEx = fieldNameEx;}

    public String getStateName() {return stateName;}

    public void setStateName(String stateName) {this.stateName = stateName;}
}
