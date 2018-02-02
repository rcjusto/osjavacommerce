package org.store.core.beans;

import org.store.core.beans.utils.StoreBean;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import java.util.Calendar;
import java.util.Date;

/**
 * Correos Electronicos
 */
@Entity
@Table(name = "t_mail")
public class Mail extends BaseBean implements StoreBean {

    public static final String STATUS_PENDING = "pending";
    public static final String STATUS_SENT = "sent";
    public static final String STATUS_ERROR = "error";
    public static final String STATUS_FATAL = "fatal";

    public static final int PRIORITY_HIGH = 10;
    public static final int PRIORITY_MEDIUM = 5;
    public static final int PRIORITY_LOW = 1;

    public static final String MAIL_TEMPLATE_RESET_PASSWORD = "/WEB-INF/views/mails/resetpassword.vm";
    public static final String MAIL_TEMPLATE_USER_CREDENTIALS = "/WEB-INF/views/mails/credentials.vm";
    public static final String MAIL_TEMPLATE_USER_WELCOME = "/WEB-INF/views/mails/welcome.vm";
    public static final String MAIL_TEMPLATE_USER_WELCOME_NEWSLETTER = "/WEB-INF/views/mails/welcome_newsletter.vm";
    public static final String MAIL_TEMPLATE_ADMIN_STOCK_ALERT = "/WEB-INF/views/mails/stock_alert_admin.vm";
    public static final String MAIL_TEMPLATE_USER_STOCK_ALERT = "/WEB-INF/views/mails/stock_alert_users.vm";
    public static final String MAIL_TEMPLATE_ORDER_STATUS = "/WEB-INF/views/mails/order_status.vm";
    public static final String MAIL_TEMPLATE_PRODUCT = "/WEB-INF/views/mails/product.vm";
    public static final String MAIL_TEMPLATE_WISHLIST = "/WEB-INF/views/mails/wishlist.vm";
    public static final String MAIL_TEMPLATE_INVOICE = "/WEB-INF/views/mails/invoice.vm";
    public static final String MAIL_TEMPLATE_PACKINGSLIP = "/WEB-INF/views/mails/packing_slip.vm";
    public static final String MAIL_TEMPLATE_PACKINGSLIP_PDF = "/WEB-INF/views/mails/packing_slip_pdf.vm";
    public static final String MAIL_TEMPLATE_ADMIN_REQUEST_QUOTE = "/WEB-INF/views/mails/requestquote.vm";
    public static final String MAIL_TEMPLATE_ADMIN_RESPONSE_QUOTE = "/WEB-INF/views/mails/responsequote.vm";
    public static final String MAIL_TEMPLATE_PURCHASE = "/WEB-INF/views/mails/purchase.vm";
    public static final String MAIL_TEMPLATE_REFER_TO_FRIEND = "/WEB-INF/views/mails/referfriend.vm";
    public static final String MAIL_TEMPLATE_FRIEND_REGISTERED = "/WEB-INF/views/mails/friend_registered.vm";
    public static final String MAIL_TEMPLATE_FRIEND_PURCHASE = "/WEB-INF/views/mails/friend_purchase.vm";
    public static final String MAIL_TEMPLATE_AVAILABLE_LINKS = "/WEB-INF/views/mails/available_links.vm";
    public static final String MAIL_TEMPLATE_ORDER_READY_TO_PAY = "/WEB-INF/views/mails/order_pay_ready.vm";
    public static final String MAIL_TEMPLATE_PRODUCT_REVIEW = "/WEB-INF/views/mails/product_review.vm";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idMail;
    private Date sentDate;
    @Column(length = 512)
    private String fromAddress;
    @Column(length = 1024)
    private String ccAddress;
    @Column(length = 1024)
    private String toAddress;
    @Column(length = 512)
    private String subject;
    @Column(length=1024)
    private String attachment;
    @Lob
    private String body;
    private Integer priority;
    private Integer status;
    @Lob
    private String errors;

    // Tienda a la que pertenece el mail
    @Column(length = 10)
    private String inventaryCode;


    @Column(length = 1024)
    private String reference;

    public Mail() {
        priority = PRIORITY_MEDIUM;
        status = 0;
        sentDate = Calendar.getInstance().getTime();
    }

    public Long getIdMail() {
        return idMail;
    }

    public void setIdMail(Long idMail) {
        this.idMail = idMail;
    }

    public String getInventaryCode() {
        return inventaryCode;
    }

    public void setInventaryCode(String inventaryCode) {
        this.inventaryCode = inventaryCode;
    }

    public Date getSentDate() {
        return sentDate;
    }

    public void setSentDate(Date sentDate) {
        this.sentDate = sentDate;
    }

    public String getFromAddress() {
        return fromAddress;
    }

    public void setFromAddress(String fromAddress) {
        this.fromAddress = fromAddress;
    }

    public String getCcAddress() {
        return ccAddress;
    }

    public void setCcAddress(String ccAddress) {
        this.ccAddress = ccAddress;
    }

    public String getToAddress() {
        return toAddress;
    }

    public void setToAddress(String toAddress) {
        this.toAddress = toAddress;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getErrors() {
        return errors;
    }

    public void setErrors(String errors) {
        this.errors = errors;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getAttachment()
    {
        return this.attachment;
    }

    public void setAttachment(String attachment)
    {
        this.attachment = attachment;
    }

    public void addError(String err) {
        err = "<p>" + err + "</p>";
        errors = (StringUtils.isNotEmpty(errors)) ?  errors + err : err;
    }

    public String getSentStatus() {
        if (status != null)
            switch (status) {
                case 0:
                    return STATUS_PENDING;
                case 10:
                    return STATUS_SENT;
                case -1:
                    return STATUS_FATAL;
                default:
                    return STATUS_ERROR;
            }
        return null;
    }

    public String toString() {
        return new ToStringBuilder(this)
                .append("id", getIdMail())
                .toString();
    }

}