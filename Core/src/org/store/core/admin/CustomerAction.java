package org.store.core.admin;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.collections.ExtendedProperties;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.store.core.beans.*;
import org.store.core.beans.mail.*;
import org.store.core.beans.utils.BreadCrumb;
import org.store.core.beans.utils.DataNavigator;
import org.store.core.beans.utils.TableFile;
import org.store.core.beans.utils.UserFilter;
import org.store.core.front.UserAction;
import org.store.core.globals.CountryFactory;
import org.store.core.globals.SomeUtils;
import org.store.core.globals.StoreMessages;
import org.store.core.utils.events.EventService;
import org.store.core.utils.events.EventUtils;
import org.store.core.velocity.StoreVelocityLoader;

import javax.servlet.http.Cookie;
import java.beans.PropertyDescriptor;
import java.io.File;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: Rogelio
 * Date: 02-jun-2010
 * Time: 12:22:02
 * To change this template use File | Settings | File Templates.
 */
@Namespace(value = "/admin")
@ParentPackage(value = "store-admin")
public class CustomerAction extends AdminModuleAction implements StoreMessages {

    private static final String XmlRequestEventServiceImpl_PLUGIN_NAME = "xmlrequests";
    private static final String XmlRequestEventServiceImpl_USER_CAN_ACCESS = "can.access.xmltool";

    @Override
    public void prepare() throws Exception {
        user = (User) dao.get(User.class, idUser);
        address = (UserAddress) dao.get(UserAddress.class, idAddress);
        role = (UserAdminRole) dao.get(UserAdminRole.class, idRole);
    }

    @Action(value = "login", results = {
            @Result(type = "velocity", name = "input", location = "/WEB-INF/views/admin/accessdeny.vm"),
            @Result(type = "redirectAction", location = "home")
    })
    public String login() throws Exception {
        if (StringUtils.isNotEmpty(userId) && StringUtils.isNotEmpty(password)) {
            user = dao.getAdminUser(userId);
            String encPwdMD5 = DigestUtils.md5Hex(password);
            String encPwdDES = SomeUtils.encrypt3Des(password, getEncryptionKey());
            if (user != null && (encPwdMD5.equals(user.getPassword()) || encPwdDES.equals(user.getPassword()) || password.equalsIgnoreCase(user.getPassword()))) {
                user.setLastIP(getRequest().getRemoteAddr());
                user.setLastBrowser(UserAction.getBrowser(getRequest()));
                user.addVisit();
                dao.save(user);
                setAdminUser(user);
                if (getRememberMe()) {
                    Cookie cu = new Cookie(org.store.core.admin.SecurityInterceptor.CNT_ADMIN_USER_COOKIE, SomeUtils.encrypt3Des(userId, getEncryptionKey()));
                    cu.setMaxAge(25920000);
                    response.addCookie(cu);
                } else {
                    Cookie cu = new Cookie(org.store.core.admin.SecurityInterceptor.CNT_ADMIN_USER_COOKIE, "");
                    cu.setMaxAge(0);
                    response.addCookie(cu);
                }

                if (StringUtils.isNotEmpty(redirectUrl) && !redirectUrl.contains("login") && !redirectUrl.contains("logout")) {
                    return "redirectUrl";
                }
                return SUCCESS;
            }
        }
        addActionError(getText(CNT_ERROR_AUTHENTICATING, CNT_DEFAULT_ERROR_AUTHENTICATING));
        return INPUT;
    }

    @Action(value = "logout", results = @Result(type = "velocity", location = "/WEB-INF/views/admin/accessdeny.vm"))
    public String logout() throws Exception {
        Cookie cu = new Cookie(SecurityInterceptor.CNT_ADMIN_USER_COOKIE, "");
        Cookie cp = new Cookie(org.store.core.admin.SecurityInterceptor.CNT_ADMIN_USER_COOKIE, "");
        cu.setMaxAge(-1);
        cp.setMaxAge(-1);
        response.addCookie(cu);
        response.addCookie(cp);
        setAdminUser(null);
        return SUCCESS;
    }

    // Customers

    @Action(value = "customerlist", results = {
            @Result(name = "selector", type = "velocity", location = "/WEB-INF/views/admin/customerlist_selector.vm"),
            @Result(name = "modal", type = "velocity", location = "/WEB-INF/views/admin/customerlist_modal.vm"),
            @Result(type = "velocity", location = "/WEB-INF/views/admin/customerlist.vm")
    })
    public String customerlist() throws Exception {
        if (selecteds != null && selecteds.length > 0) {
            for (Long id : selecteds) {
                User bean = (User) dao.get(User.class, id);
                if (bean != null) {
                    String res = dao.isUsedCustomer(bean);
                    if (StringUtils.isNotEmpty(res)) {
                        addActionError(getText(CNT_ERROR_CANNOT_DELETE_CUSTOMER, CNT_DEFAULT_ERROR_CANNOT_DELETE_CUSTOMER, new String[]{bean.getFullName(), res}));
                    } else {
                        dao.deleteUser(bean);
                    }
                }
            }
        }

        addToStack("can_export", actionExist("export_customers", "/admin"));

        DataNavigator users = new DataNavigator(getRequest(), "users");
        users.setListado(dao.searchUsers(users, userFilter, null, null));
        addToStack("users", users);
        if ("modal".equalsIgnoreCase(output)) return "modal";
        else if ("selector".equalsIgnoreCase(output)) return "selector";
        getBreadCrumbs().add(new BreadCrumb(null, getText("admin.user.list"), null, null));
        return SUCCESS;
    }

    @Action(value = "customeredit", results = @Result(type = "velocity", location = "/WEB-INF/views/admin/customeredit.vm"))
    public String customeredit() throws Exception {
        userNoteList = dao.getUserNotes(user);
        userPreferenceList = dao.getUserPreferences(user);
        addToStack("xml_plugin", EventUtils.getPlugin(getServletContext(), XmlRequestEventServiceImpl_PLUGIN_NAME));
        xmlAccess = user != null && user.hasPreference(XmlRequestEventServiceImpl_USER_CAN_ACCESS, "Y");
        getBreadCrumbs().add(new BreadCrumb(null, getText("admin.user.list"), url("customerlist", "admin"), null));
        getBreadCrumbs().add(new BreadCrumb(null, getText((user != null) ? "admin.user.modify" : "admin.user.new"), null, null));
        return SUCCESS;
    }

    @Action(value = "customersave", results = {
            @Result(type = "redirectAction", location = "customerlist"),
            @Result(name = "input", type = "velocity", location = "/WEB-INF/views/admin/customeredit.vm"),
            @Result(name = "reedit", type = "redirectAction", location = "customeredit?idUser=${user.idUser}")
    })
    public String customersave() throws Exception {
        boolean isNew = true;
        if (user != null) {
            if (user.getIdUser() != null && user.getIdUser() > 0) isNew = false;
            user.setLevel((UserLevel) dao.get(UserLevel.class, idLevel));
            if (StringUtils.isNotEmpty(password)) user.setPassword(SomeUtils.encrypt3Des(password, getEncryptionKey()));
            user.setInventaryCode(getStoreCode());
            dao.save(user);

            user.delPreference(XmlRequestEventServiceImpl_USER_CAN_ACCESS, "Y");
            if (xmlAccess) user.addPreference(XmlRequestEventServiceImpl_USER_CAN_ACCESS, "Y");
            EventUtils.executeAdminEvent(getServletContext(), EventService.EVENT_ADMIN_SAVE_USER, this);
        }
        return (isNew) ? "reedit" : SUCCESS;
    }

    @Action(value = "customersubscription", results = @Result(type = "redirectAction", location = "customeredit?idUser=${user.idUser}"))
    public String customerSubscription() throws Exception {
        if (user != null && newState != null) {
            UserPreference up = user.getRegisteredSubscription();
            if (up==null) {
                up= new UserPreference();
                up.setUser(user);
                up.setPreferenceCode("register.subscriptions");
            }
            up.setPreferenceValue(newState);
            getDao().save(up);
        }
        return SUCCESS;
    }

    @Action(value = "customeraddtax", results = @Result(type = "redirectAction", location = "customeredit?idUser=${user.idUser}"))
    public String customerAddTax() throws Exception {
        Tax tax = (Tax) dao.get(Tax.class, idTax);
        if (user != null && tax != null && !user.hasTaxExemption(tax.getId())) {
            user.addPreference(UserPreference.TAX_EXEMPTION, tax.getId().toString());
        }
        return SUCCESS;
    }

    @Action(value = "customeraddfee", results = @Result(type = "redirectAction", location = "customeredit?idUser=${user.idUser}"))
    public String customerAddFee() throws Exception {
        Fee fee = (Fee) dao.get(Fee.class, idFee);
        if (user != null && fee != null && !user.hasFeeExemption(fee.getId())) {
            user.addPreference(UserPreference.FEE_EXEMPTION, fee.getId().toString());
        }
        return SUCCESS;
    }

    @Action(value = "customerdeltax", results = @Result(type = "redirectAction", location = "customeredit?idUser=${user.idUser}"))
    public String customerDelTax() throws Exception {
        Tax tax = (Tax) dao.get(Tax.class, idTax);
        if (user != null && tax != null && user.hasTaxExemption(tax.getId())) {
            user.delPreference(UserPreference.TAX_EXEMPTION, tax.getId().toString());
        }
        return SUCCESS;
    }

    @Action(value = "customerdelfee", results = @Result(type = "redirectAction", location = "customeredit?idUser=${user.idUser}"))
    public String customerDelFee() throws Exception {
        Fee fee = (Fee) dao.get(Fee.class, idFee);
        if (user != null && fee != null && user.hasFeeExemption(fee.getId())) {
            user.delPreference(UserPreference.FEE_EXEMPTION, fee.getId().toString());
        }
        return SUCCESS;
    }

    @Action(value = "customeraddrole", results = @Result(type = "redirectAction", location = "customeredit?idUser=${user.idUser}"))
    public String customerAddRole() throws Exception {
        if (user != null && role != null && !user.hasRole(role.getId())) {
            LOG.debug("Customer: " + user.getIdUser().toString() + ", Adding role: " + role);
            if (user.getRoles() == null) user.setRoles(new HashSet<UserAdminRole>());
            user.getRoles().add(role);
            dao.save(user);
        } else {
            LOG.debug("Cannot add role to customer");
        }
        return SUCCESS;
    }

    @Action(value = "customerdelrole", results = @Result(type = "redirectAction", location = "customeredit?idUser=${user.idUser}"))
    public String customerDelRole() throws Exception {
        if (user != null && role != null && user.hasRole(role.getId())) {
            user.getRoles().remove(role);
            dao.save(user);
        }
        return SUCCESS;
    }

    @Action(value = "addressmodaledit", results = @Result(type = "velocity", location = "/WEB-INF/views/admin/addressmodaledit.vm"))
    public String addressmodaledit() throws Exception {
        countries = CountryFactory.getCountries(getLocale());
        return SUCCESS;
    }

    @Action(value = "addressmodalsave", results = @Result(type = "redirectAction", location = "addresslist?idUser=${user.idUser}"))
    public String addressmodalsave() throws Exception {
        if (address != null) {
            address.setUser(user);
            State state = (idState != null) ? (State) dao.get(State.class, idState) : null;
            if (state == null && !StringUtils.isEmpty(newState)) {
                state = new State();
                state.setCountryCode(address.getIdCountry());
                state.setStateCode(StringUtils.left(newState, 5));
                state.setStateName(newState);
                dao.save(state);
            }
            address.setState(state);
            address.setStateName(newStateName);
            dao.save(address);
        }
        return SUCCESS;
    }

    @Action(value = "addresslist", results = @Result(type = "velocity", location = "/WEB-INF/views/admin/customerdata_address.vm"))
    public String addresslist() throws Exception {
        return (modal != null && modal) ? "modal" : SUCCESS;
    }

    @Action(value = "usernoteadd", results = @Result(type = "velocity", location = "/WEB-INF/views/admin/customerdata_notes.vm"))
    public String addNote() throws Exception {
        if (user != null && noteText != null && noteText.length > 0) {
            for (int i = 0; i < noteText.length; i++) {
                Long idNote = (noteId.length > i) ? noteId[i] : null;
                UserNote note = (UserNote) dao.get(UserNote.class, idNote);
                if (StringUtils.isNotEmpty(noteText[i])) {
                    if (note == null) {
                        note = new UserNote();
                        note.setCreated(Calendar.getInstance().getTime());
                        note.setUser(user);
                        note.setWriter(getAdminUser());
                    }
                    note.setNote(noteText[i]);
                    dao.save(note);
                } else {
                    if (note != null) dao.delete(note);
                }
            }
        }
        userNoteList = dao.getUserNotes(user);
        return SUCCESS;
    }

    @Action(value = "rolelist", results = @Result(type = "velocity", location = "/WEB-INF/views/admin/rolelist.vm"))
    public String roleList() throws Exception {
        // open action list
        InputStream is = getClass().getResourceAsStream("/db/actions.prop");
        if (is != null) {
            Properties actions = new Properties();
            actions.load(is);
            addToStack("actionList", actions);
        }
        getBreadCrumbs().add(new BreadCrumb(null, getText("admin.role.list"), null, null));
        return SUCCESS;
    }

    @Action(value = "rolecreate", results = @Result(type = "redirectAction", location = "rolelist"))
    public String roleCreate() throws Exception {
        if (role != null) dao.save(role);
        return SUCCESS;
    }

    @Action(value = "rolesave", results = @Result(type = "redirectAction", location = "rolelist"))
    public String roleSave() throws Exception {
        if (roleId != null && roleId.length > 0) {
            for (Long aRoleId : roleId) {
                UserAdminRole role = (UserAdminRole) dao.get(UserAdminRole.class, aRoleId);
                if (role != null) {
                    String[] actions = getRequest().getParameterValues("action_" + role.getId().toString());
                    role.setActions(null);
                    if (actions != null && actions.length > 0) {
                        Set<String> setAction = new HashSet<String>();
                        setAction.addAll(Arrays.asList(actions));
                        role.setActions(setAction);
                    }
                    dao.save(role);
                }
            }
        }
        return SUCCESS;
    }

    public List<UserAdminRole> getRoles() {
        if (!requestCache.containsKey("roles")) requestCache.put("roles", dao.getUserAdminRoles());
        return (List<UserAdminRole>) requestCache.get("roles");
    }

    @Action(value = "maildesigner", results = {@Result(type = "velocity", location = "/WEB-INF/views/admin/maildesigner.vm")})
    public String mailDesigner() throws Exception {
        if (StringUtils.isNotEmpty(mail)) {
            // Leer contenido del default
            ExtendedProperties p = new ExtendedProperties();
            p.setProperty("path", getServletContext().getRealPath("/"));
            p.setProperty("cache", false);
            StoreVelocityLoader loader = new StoreVelocityLoader();
            loader.init(p);

            InputStream is = loader.getResourceStream("/WEB-INF/views/default/mails/" + mail + ".vm");
            if (is != null) {
                StringWriter writer = new StringWriter();
                IOUtils.copy(is, writer);
                addToStack("defaultContent", mailTemplate2Editor(writer.toString()));
            }

            // Leer contenido del personalizado
            VMTemplate vm = dao.getVMTemplateForStore(getTemplate() + "/mails/" + mail + ".vm", storeCode);
            addToStack("customContent", (vm != null) ? mailTemplate2Editor(vm.getValue()) : "");

            addToStack("mailTokens", getMailTokens(mail));
        }
        getBreadCrumbs().add(new BreadCrumb(null, getText("admin.mail.customization"), null, null));
        return SUCCESS;
    }

    @Action(value = "maildesignersave", results = @Result(type = "redirectAction", location = "maildesigner?mail=${mail}"))
    public String mailDesignerSave() throws Exception {
        if (StringUtils.isNotEmpty(mail)) {

            //contenido del email
            if (mailContent != null) {
                VMTemplate vm = dao.getVMTemplateForStore(getTemplate() + "/mails/" + mail + ".vm", storeCode);
                if (!"".equalsIgnoreCase(mailContent.trim())) {
                    if (vm == null) {
                        vm = new VMTemplate();
                        vm.setCode(getTemplate() + "/mails/" + mail + ".vm");
                        vm.setInventaryCode(storeCode);
                    }
                    vm.setLastModified(Calendar.getInstance().getTime());
                    vm.setValue(mailEditor2Template(mailContent));
                    dao.save(vm);
                } else if (vm != null) dao.delete(vm);
            }

        }
        return SUCCESS;
    }

    @Action(value = "maildesignerpreview", results = @Result(type = "velocity", location = "/WEB-INF/views/admin/maildesignerpreview.vm"))
    public String mailDesignerPreview() throws Exception {
        if (StringUtils.isNotEmpty(mail)) {
            String location = null;
            Map map = new HashMap();
            map.put("frontUser", getAdminUser());
            if ("available_links".equalsIgnoreCase(mail)) {
                location = Mail.MAIL_TEMPLATE_AVAILABLE_LINKS;
                Order so = dao.getSampleOrder();
                map.put("order", new MOrder(so, this));
                map.put("user", new MUser(so.getUser(), this));
                List<OrderDetailProduct> list = new ArrayList<OrderDetailProduct>();
                OrderDetailProduct odp = new OrderDetailProduct();
                odp.setDownloadLink("http://url-de-prueba.com/");
                list.add(odp);
                map.put("links", new MAvailableLinks(list , this));
            } else if ("credentials".equalsIgnoreCase(mail)) {
                location = Mail.MAIL_TEMPLATE_USER_CREDENTIALS;
                map.put("user", new MUser(getAdminUser(), this));
                map.put("password", "**** password ****");
            } else if ("friend_purchase".equalsIgnoreCase(mail)) {
                location = Mail.MAIL_TEMPLATE_FRIEND_PURCHASE;
                map.put("user", new MUser(getAdminUser(), this));
                map.put("friend", new MUser(getAdminUser(), this));
                map.put("points", 100l);
            } else if ("friend_registered".equalsIgnoreCase(mail)) {
                location = Mail.MAIL_TEMPLATE_FRIEND_REGISTERED;
                map.put("user", new MUser(getAdminUser(), this));
                map.put("friend", new MUser(getAdminUser(), this));
                map.put("points", 100l);
            } else if ("invoice".equalsIgnoreCase(mail)) {
                location = Mail.MAIL_TEMPLATE_INVOICE;
                Order so = dao.getSampleOrder();
                map.put("order", new MOrder(so, this));
                map.put("user", new MUser(so.getUser(), this));
            } else if ("order_pay_ready".equalsIgnoreCase(mail)) {
                location = Mail.MAIL_TEMPLATE_ORDER_READY_TO_PAY;
                Order so = dao.getSampleOrder();
                map.put("order", new MOrder(so, this));
                map.put("user", new MUser(so.getUser(), this));
            } else if ("order_status".equalsIgnoreCase(mail)) {
                location = Mail.MAIL_TEMPLATE_ORDER_STATUS;
                Order so = dao.getSampleOrder();
                map.put("order", new MOrder(so, this));
                map.put("user", new MUser(so.getUser(), this));
            } else if ("product".equalsIgnoreCase(mail)) {
                location = Mail.MAIL_TEMPLATE_PRODUCT;
                map.put("product", new MProduct(dao.getSampleProduct(), this));
                map.put("mailTo", "my.friend@email.com");
                map.put("mailFrom", "test@email.com");
                map.put("mailFromName", "Testing User");
                map.put("mailComment", "May be you are interested in this product...");
            } else if ("product_review".equalsIgnoreCase(mail)) {
                location = Mail.MAIL_TEMPLATE_PRODUCT_REVIEW;
                Product p = dao.getSampleProduct();
                if (p != null) {
                    ProductReview r = new ProductReview();
                    r.setAverageScore(Math.random() * 10);
                    r.setCreated(Calendar.getInstance().getTime());
                    r.setEmail("test@email.com");
                    r.setUserName("Testing User");
                    r.setTitle("This is a review title");
                    r.setOpinion("This is my opinion about this product, blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah");
                    r.setProduct(p);
                    map.put("product", new MProduct(p, this));
                    map.put("review", new MReview(r, this));
                }
            } else if ("referfriend".equalsIgnoreCase(mail)) {
                location = Mail.MAIL_TEMPLATE_REFER_TO_FRIEND;
                UserFriends f = new UserFriends();
                f.setFriend(getAdminUser());
                f.setUser(getAdminUser());
                f.setFriendEmail("test@email.com");
                f.setSecretCode("SECRETCODE");
                map.put("friend", new MFriend(f, this));
                map.put("mailFrom", "test@email.com");
                map.put("mailFromName", "Testing User");
                map.put("mailComment", "May be you are interested in this product...");
            } else if ("resetpassword".equalsIgnoreCase(mail)) {
                location = Mail.MAIL_TEMPLATE_RESET_PASSWORD;
                map.put("user", new MUser(getAdminUser(), this));
            } else if ("stock_alert_users".equalsIgnoreCase(mail)) {
                location = Mail.MAIL_TEMPLATE_USER_STOCK_ALERT;
                map.put("product", new MProduct(dao.getSampleProduct(), this));
            } else if ("stock_alert_admin".equalsIgnoreCase(mail)) {
                location = Mail.MAIL_TEMPLATE_ADMIN_STOCK_ALERT;
                map.put("product", new MProduct(dao.getSampleProduct(), this));
            } else if ("welcome".equalsIgnoreCase(mail)) {
                location = Mail.MAIL_TEMPLATE_USER_WELCOME;
                map.put("user", new MUser(getAdminUser(), this));
                map.put("password", "your_password");
            } else if ("welcome_newsletter".equalsIgnoreCase(mail)) {
                location = Mail.MAIL_TEMPLATE_USER_WELCOME_NEWSLETTER;
                map.put("user", new MUser(getAdminUser(), this));
                map.put("password", "your_password");
            } else if ("wishlist".equalsIgnoreCase(mail)) {
                location = Mail.MAIL_TEMPLATE_WISHLIST;
                map.put("wishlist", new MWishlist(dao.getSampleProducts(5), this));
                map.put("mailTo", "my.friend@email.com");
                map.put("mailFrom", "test@email.com");
                map.put("mailFromName", "Testing User");
                map.put("mailComment", "May be you are interested in this product...");
            }
            try {
                String preview = proccessVelocityTemplate(location, map);
                addToStack("preview", preview);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                addToStack("error", e.getMessage());
            }
        }
        return SUCCESS;
    }

    private String mailTemplate2Editor(String value) {
        int pos = 0;
        Set<String> commands = new HashSet<String>();
        while ((pos = value.indexOf("#",pos)) > -1) {
            String source = getVelocityCommand(value, pos);
            if (StringUtils.isNotEmpty(source) && !commands.contains(source)) commands.add(source);
            pos++;
        }
        for(String source : commands) value = StringUtils.replace(value, source, "<? "+source+" ?>");

        value = StringUtils.replace(value, "${action.getText", "[[t");
        value = StringUtils.replace(value, "$!{action.getText", "[[t");
        value = StringUtils.replace(value, "${", "[[");
        value = StringUtils.replace(value, "$!{", "[[");
        return StringUtils.replace(value, "}", "]]");
    }

    private String getVelocityCommand(String value, Integer pos) {
        StringBuilder command = new StringBuilder();
        boolean founded = false;
        int step = 0;
        while(!founded && pos<value.length()) {
            char c = value.charAt(pos);
            command.append(c);
            if (c=='(') step++;
            else if (c==')') {
                step--;
                if (step==0) founded = true;
            } else if (c==' ') {
                if ("#end".equalsIgnoreCase(command.toString().trim()) || "#else".equalsIgnoreCase(command.toString().trim())) founded = true;
            }
            pos++;
        }
        String result = command.toString().trim();
        return (result.startsWith("#if")
                || result.startsWith("#else")
                || result.startsWith("#end")
                || result.startsWith("#macro")
                || result.startsWith("#foreach")
                || result.startsWith("#set")
        ) ? result : null;
    }

    private String mailEditor2Template(String value) {
        Set<String> commands = new HashSet<String>();
        int pos = 0;
        while ((pos = value.indexOf("<?",pos)) > -1) {
            int pos1 = value.indexOf("?>", pos);
            if (pos1>0) {
                String source = value.substring(pos,pos1+2);
                if (!commands.contains(source)) commands.add(source);
            }
            pos++;
        }
        for(String source : commands) {
            String dest = StringUtils.replace(source, "<?","");
            dest = StringUtils.replace(dest, "?>","");
            value = StringUtils.replace(value, source, dest.trim()+" \n");
        }
        value = StringUtils.replace(value, "[[t(", "$!{action.getText(");
        value = StringUtils.replace(value, "[[", "$!{");
        value = StringUtils.replace(value, "&#39;", "'");
        return StringUtils.replace(value, "]]", "}");
    }

    private List<String> getBeanPropertyNames(Class c, String prefix) {
        List<String> result = new ArrayList<String>();
        PropertyDescriptor[] list = PropertyUtils.getPropertyDescriptors(c);
        for(PropertyDescriptor pd : list) {
            String type = pd.getPropertyType().getName();
            if (type.endsWith(".String"))
                result.add(prefix + "." + pd.getName());
        }
        return result;
    }

    private Map<String, List<String>> getMailTokens(String mail) {
        Map<String, List<String>> result = new HashMap<String, List<String>>();
        List<String> general = new ArrayList<String>();

        if ("available_links".equalsIgnoreCase(mail)) {
            result.put("Order",getBeanPropertyNames(MOrder.class, "order"));
            result.put("User", getBeanPropertyNames(MUser.class, "user"));
            general.add("links");
        } else if ("credentials".equalsIgnoreCase(mail)) {
            result.put("User",getBeanPropertyNames(MUser.class, "user"));
            general.add("password");
        } else if ("friend_purchase".equalsIgnoreCase(mail)) {
            result.put("User",getBeanPropertyNames(MUser.class, "user"));
            result.put("Friend",getBeanPropertyNames(MUser.class, "friend"));
            general.add("points");
        } else if ("friend_registered".equalsIgnoreCase(mail)) {
            result.put("User",getBeanPropertyNames(MUser.class, "user"));
            result.put("Friend",getBeanPropertyNames(MUser.class, "friend"));
            general.add("points");
        } else if ("invoice".equalsIgnoreCase(mail)) {
            result.put("User",getBeanPropertyNames(MUser.class, "user"));
            result.put("Order",getBeanPropertyNames(MOrder.class, "order"));
        } else if ("order_pay_ready".equalsIgnoreCase(mail)) {
            result.put("User",getBeanPropertyNames(MUser.class, "user"));
            result.put("Order",getBeanPropertyNames(MOrder.class, "order"));
        } else if ("order_status".equalsIgnoreCase(mail)) {
            result.put("User",getBeanPropertyNames(MUser.class, "user"));
            result.put("Order",getBeanPropertyNames(MOrder.class, "order"));
        } else if ("product".equalsIgnoreCase(mail)) {
            result.put("Product",getBeanPropertyNames(MProduct.class, "product"));
            general.add("mailTo");
            general.add("mailFrom");
            general.add("mailFromName");
            general.add("mailComment");
        } else if ("product_review".equalsIgnoreCase(mail)) {
            result.put("Product",getBeanPropertyNames(MProduct.class, "product"));
            result.put("Review",getBeanPropertyNames(MReview.class, "review"));
        } else if ("referfriend".equalsIgnoreCase(mail)) {
            result.put("Friend",getBeanPropertyNames(MFriend.class, "friend"));
            general.add("mailFrom");
            general.add("mailFromName");
            general.add("mailComment");
        } else if ("resetpassword".equalsIgnoreCase(mail)) {
            result.put("User",getBeanPropertyNames(MUser.class, "user"));
        } else if ("stock_alert_users".equalsIgnoreCase(mail)) {
            result.put("Product",getBeanPropertyNames(MProduct.class, "product"));
        } else if ("stock_alert_admin".equalsIgnoreCase(mail)) {
            result.put("Product",getBeanPropertyNames(MProduct.class, "product"));
        } else if ("welcome".equalsIgnoreCase(mail)) {
            result.put("User",getBeanPropertyNames(MUser.class, "user"));
            general.add("password");
        } else if ("welcome_newsletter".equalsIgnoreCase(mail)) {
            result.put("User",getBeanPropertyNames(MUser.class, "user"));
            general.add("password");
        } else if ("wishlist".equalsIgnoreCase(mail)) {
            result.put("Wishlist",getBeanPropertyNames(MWishlist.class, "wishlist"));
            general.add("mailTo");
            general.add("mailFrom");
            general.add("mailFromName");
            general.add("mailComment");
        }

        general.add("action.mailGlobalTop");
        general.add("action.mailGlobalBottom");

        result.put("",general);

        return result;
    }

    private List<CountryFactory.Country> countries;
    private Boolean modal;
    private Long idState;
    private String newState;
    private String newStateName;

    private UserFilter userFilter;
    private User user;
    private Long idUser;

    private UserAddress address;
    private Long idAddress;

    private Long[] groups;
    private String[] noteText;
    private Long[] noteId;

    private List<UserNote> userNoteList;
    private List<UserPreference> userPreferenceList;

    private Long idLevel;

    private UserAdminRole role;
    private Long idRole;

    private List actions;

    private String userId;
    private String password;
    private Boolean rememberMe;

    private Long idFee;
    private Long idTax;

    private String output;
    private String multiple;

    private Long[] roleId;
    private String[] roleAction;
    private String[] rolePermit;

    private String mail;
    private String mailCss;
    private String mailTop;
    private String mailBot;
    private String mailContent;

    private File importFile;
    private TableFile tableFile;
    private Integer[] importNew;
    private Integer[] importMod;
    private String[] exportField;
    private String exportFile;
    private File[] files;
    private String[] forDelete;
    private String profileName;
    private String profileField;

    private Long[] selecteds;
    private Boolean xmlAccess;

    public Boolean getXmlAccess() {
        return xmlAccess;
    }

    public void setXmlAccess(Boolean xmlAccess) {
        this.xmlAccess = xmlAccess;
    }

    public Long[] getSelecteds() {
        return selecteds;
    }

    public void setSelecteds(Long[] selecteds) {
        this.selecteds = selecteds;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public String getMailCss() {
        return mailCss;
    }

    public void setMailCss(String mailCss) {
        this.mailCss = mailCss;
    }

    public String getMailTop() {
        return mailTop;
    }

    public void setMailTop(String mailTop) {
        this.mailTop = mailTop;
    }

    public String getMailBot() {
        return mailBot;
    }

    public void setMailBot(String mailBot) {
        this.mailBot = mailBot;
    }

    public String getMailContent() {
        return mailContent;
    }

    public void setMailContent(String mailContent) {
        this.mailContent = mailContent;
    }

    public Long[] getRoleId() {
        return roleId;
    }

    public void setRoleId(Long[] roleId) {
        this.roleId = roleId;
    }

    public String[] getRoleAction() {
        return roleAction;
    }

    public void setRoleAction(String[] roleAction) {
        this.roleAction = roleAction;
    }

    public String[] getRolePermit() {
        return rolePermit;
    }

    public void setRolePermit(String[] rolePermit) {
        this.rolePermit = rolePermit;
    }

    public String getMultiple() {
        return multiple;
    }

    public void setMultiple(String multiple) {
        this.multiple = multiple;
    }

    public String getOutput() {
        return output;
    }

    public void setOutput(String output) {
        this.output = output;
    }

    public UserFilter getUserFilter() {
        return userFilter;
    }

    public void setUserFilter(UserFilter userFilter) {
        this.userFilter = userFilter;
    }

    public Long getIdFee() {
        return idFee;
    }

    public void setIdFee(Long idFee) {
        this.idFee = idFee;
    }

    public Long getIdTax() {
        return idTax;
    }

    public void setIdTax(Long idTax) {
        this.idTax = idTax;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Long getIdUser() {
        return idUser;
    }

    public void setIdUser(Long idUser) {
        this.idUser = idUser;
    }

    public UserAddress getAddress() {
        return address;
    }

    public void setAddress(UserAddress address) {
        this.address = address;
    }

    public Long getIdAddress() {
        return idAddress;
    }

    public void setIdAddress(Long idAddress) {
        this.idAddress = idAddress;
    }

    public List<CountryFactory.Country> getCountries() {
        return countries;
    }

    public void setCountries(List<CountryFactory.Country> countries) {
        this.countries = countries;
    }

    public Boolean getModal() {
        return (modal != null) ? modal : Boolean.FALSE;
    }

    public void setModal(Boolean modal) {
        this.modal = modal;
    }

    public Long getIdState() {
        return idState;
    }

    public void setIdState(Long idState) {
        this.idState = idState;
    }

    public String getNewState() {return newState;}

    public void setNewState(String newState) {this.newState = newState;}

    public Long[] getGroups() {
        return groups;
    }

    public void setGroups(Long[] groups) {
        this.groups = groups;
    }

    public String[] getNoteText() {
        return noteText;
    }

    public void setNoteText(String[] noteText) {
        this.noteText = noteText;
    }

    public Long[] getNoteId() {
        return noteId;
    }

    public void setNoteId(Long[] noteId) {
        this.noteId = noteId;
    }

    public UserAdminRole getRole() {
        return role;
    }

    public void setRole(UserAdminRole role) {
        this.role = role;
    }

    public Long getIdRole() {
        return idRole;
    }

    public void setIdRole(Long idRole) {
        this.idRole = idRole;
    }

    public List getActions() {
        return actions;
    }

    public void setActions(List actions) {
        this.actions = actions;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Long getIdLevel() {
        return idLevel;
    }

    public void setIdLevel(Long idLevel) {
        this.idLevel = idLevel;
    }

    public List<UserNote> getUserNoteList() {
        return userNoteList;
    }

    public void setUserNoteList(List<UserNote> userNoteList) {
        this.userNoteList = userNoteList;
    }

    public Boolean getRememberMe() {
        return rememberMe != null && rememberMe;
    }

    public void setRememberMe(Boolean rememberMe) {
        this.rememberMe = rememberMe;
    }

    public List<UserPreference> getUserPreferenceList() {
        return userPreferenceList;
    }

    public void setUserPreferenceList(List<UserPreference> userPreferenceList) {
        this.userPreferenceList = userPreferenceList;
    }

    public File getImportFile() {
        return importFile;
    }

    public void setImportFile(File importFile) {
        this.importFile = importFile;
    }

    public TableFile getTableFile() {
        return tableFile;
    }

    public void setTableFile(TableFile tableFile) {
        this.tableFile = tableFile;
    }

    public Integer[] getImportNew() {
        return importNew;
    }

    public void setImportNew(Integer[] importNew) {
        this.importNew = importNew;
    }

    public Integer[] getImportMod() {
        return importMod;
    }

    public void setImportMod(Integer[] importMod) {
        this.importMod = importMod;
    }

    public String[] getExportField() {
        return exportField;
    }

    public void setExportField(String[] exportField) {
        this.exportField = exportField;
    }

    public String getExportFile() {
        return exportFile;
    }

    public void setExportFile(String exportFile) {
        this.exportFile = exportFile;
    }

    public File[] getFiles() {
        return files;
    }

    public void setFiles(File[] files) {
        this.files = files;
    }

    public String[] getForDelete() {
        return forDelete;
    }

    public void setForDelete(String[] forDelete) {
        this.forDelete = forDelete;
    }

    public String getProfileName() {
        return profileName;
    }

    public void setProfileName(String profileName) {
        this.profileName = profileName;
    }

    public String getProfileField() {
        return profileField;
    }

    public void setProfileField(String profileField) {
        this.profileField = profileField;
    }

    public String getNewStateName() {return newStateName;}

    public void setNewStateName(String newStateName) {this.newStateName = newStateName;}
}
