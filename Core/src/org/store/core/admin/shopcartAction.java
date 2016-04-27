package org.store.core.admin;

import org.store.core.beans.Mail;
import org.store.core.beans.ShopCart;
import org.store.core.beans.ShopCartItem;
import org.store.core.beans.utils.BreadCrumb;
import org.store.core.beans.utils.DataNavigator;
import org.store.core.globals.SomeUtils;
import org.store.core.globals.StoreMessages;
import org.store.core.mail.MailSenderThreat;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

import java.util.Date;

public class shopcartAction extends AdminModuleAction implements StoreMessages {

    @Override
    public void prepare() throws Exception {
        shopcart = (ShopCart) dao.get(ShopCart.class, idCart);
    }

    public String list() throws Exception {
        if (selecteds != null && selecteds.length > 0) {
            for (Long id : selecteds) {
                ShopCart bean = (ShopCart) dao.get(ShopCart.class, id);
                if (bean != null) dao.delete(bean);
            }
            dao.flushSession();
        }

        processFilters();
        DataNavigator nav = new DataNavigator(getRequest(), "shopcarts");
        Date dateFrom = SomeUtils.strToDate(getFilterVal("minDate"), getDefaultLanguage());
        Date dateTo = SomeUtils.strToDate(getFilterVal("maxDate"), getDefaultLanguage());

        nav.setListado(dao.getShopCarts(nav, getFilterVal("status"), dateFrom, dateTo));
        addToStack("shopcarts", nav);
        return SUCCESS;
    }

    public String edit() throws Exception {
        getBreadCrumbs().add(new BreadCrumb(null, getText("admin.shopcart.list"), url("listshopcart","/admin"), null));
        getBreadCrumbs().add(new BreadCrumb(null, getText(shopcart!=null ? "admin.shopcart.details" : "admin.shopcart.details"), null, null));
        return SUCCESS;
    }

    public String show() throws Exception {
        getBreadCrumbs().add(new BreadCrumb(null, getText("admin.shopcart.list"), url("listshopcart","/admin"), null));
        getBreadCrumbs().add(new BreadCrumb(null, getText(shopcart!=null ? "admin.shopcart.details" : "admin.shopcart.details"), null, null));
        return SUCCESS;
    }

    public String del() throws Exception {
        if (shopcart != null) dao.delete(shopcart);
        return SUCCESS;
    }

    public String save() throws Exception {
        if (shopcart != null && (ShopCart.STATUS_PENDING.equals(shopcart.getStatus()) || ShopCart.STATUS_APPROVED.equals(shopcart.getStatus()))) {
            if (ArrayUtils.isSameLength(newPrice, itemId)) {
                for (int i = 0; i < itemId.length; i++) {
                    Double price = SomeUtils.strToDouble(newPrice[i]);
                    ShopCartItem item = shopcart.getItemById(itemId[i]);
                    if (item != null && price != null) item.setPrice(price);
                }
                shopcart.setStatus(ShopCart.STATUS_APPROVED);
                Date valid = (StringUtils.isNotEmpty(validUntil)) ? SomeUtils.strToDate(validUntil, getDefaultLanguage()) : null;
                shopcart.setValidUntil(valid);
                shopcart.setQuoteMessage(message);
                dao.save(shopcart);

                if (StringUtils.isNotEmpty(shopcart.getUser().getEmail())) {
                    String body = proccessVelocityTemplate(Mail.MAIL_TEMPLATE_ADMIN_RESPONSE_QUOTE);
                    Mail mail = new Mail();
                    mail.setInventaryCode(getStoreCode());
                    mail.setBody(body);
                    mail.setSubject(getText(CNT_SUBJECT_ADMIN_RESPONSE_QUOTE, CNT_SUBJECT_ADMIN_RESPONSE_QUOTE_DEFAULT));
                    mail.setToAddress(shopcart.getUser().getEmail());
                    mail.setPriority(Mail.PRIORITY_HIGH);
                    mail.setReference("CART RESPONSE QUOTE ID:" + shopcart.getId());
                    dao.save(mail);
                    MailSenderThreat.asyncSendMail(mail, this);
                }

                setActionResult("showshopcart?idCart=" + shopcart.getId().toString());
                return "action";
            }
        }
        return SUCCESS;
    }

    private ShopCart shopcart;
    private Long idCart;
    private Long[] itemId;
    private String[] newPrice;
    private String validUntil;
    private String message;
    private String actionResult;


    public ShopCart getShopcart() {
        return shopcart;
    }

    public void setShopcart(ShopCart shopcart) {
        this.shopcart = shopcart;
    }

    public Long getIdCart() {
        return idCart;
    }

    public void setIdCart(Long idCart) {
        this.idCart = idCart;
    }

    public Long[] getItemId() {
        return itemId;
    }

    public void setItemId(Long[] itemId) {
        this.itemId = itemId;
    }

    public String[] getNewPrice() {
        return newPrice;
    }

    public void setNewPrice(String[] newPrice) {
        this.newPrice = newPrice;
    }

    public String getValidUntil() {
        return validUntil;
    }

    public void setValidUntil(String validUntil) {
        this.validUntil = validUntil;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getActionResult() {
        return actionResult;
    }

    public void setActionResult(String actionResult) {
        this.actionResult = actionResult;
    }
}
