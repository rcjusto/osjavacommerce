package org.store.campaigns.beans;

import org.store.core.beans.BaseBean;
import org.store.core.beans.utils.StoreBean;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.util.Date;

/**
 * Campaign
 */
@Entity
@Table(name = "t_campaign")
public class Campaign extends BaseBean implements StoreBean {

    public static final String SEND_TO_ALL_CUSTOMERS = "all.customers";
    public static final String SEND_TO_NEWSLETTER_SUBSCRIBERS = "newsletter.subscribers";
    public static final String SEND_TO_GROUP = "customer.group";

    public static final String STATUS_CREATING = "creating";
    public static final String STATUS_READY = "ready";
    public static final String STATUS_SENT = "sent";

    public static final String EMAIL_REFERENCE_PREFIX = "CAMPAIGN ID ";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Tienda en la q esta configurado el fee
    @Column(length = 10)
    private String inventaryCode;

    private Date dateToSend;

    @Column(length = 50)
    private String sendTo;

    @ManyToOne
    private UserGroup userGroup;

    @Column(length = 512)
    private String subject;

    @Lob
    private String campaignMail;

    private String status;

    private Long mailNumber;
    private Long hits;

    public Campaign() {
        status = STATUS_CREATING;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getInventaryCode() {
        return inventaryCode;
    }

    public void setInventaryCode(String inventaryCode) {
        this.inventaryCode = inventaryCode;
    }

    public Date getDateToSend() {
        return dateToSend;
    }

    public void setDateToSend(Date dateToSend) {
        this.dateToSend = dateToSend;
    }

    public UserGroup getUserGroup() {
        return userGroup;
    }

    public void setUserGroup(UserGroup userGroup) {
        this.userGroup = userGroup;
    }

    public String getCampaignMail() {
        return campaignMail;
    }

    public void setCampaignMail(String campaignMail) {
        this.campaignMail = campaignMail;
    }

    public Long getHits() {
        return hits;
    }

    public void setHits(Long hits) {
        this.hits = hits;
    }

    public Long getMailNumber() {
        return mailNumber;
    }

    public void setMailNumber(Long mailNumber) {
        this.mailNumber = mailNumber;
    }

    public String getStatus() {
        return (StringUtils.isNotEmpty(status)) ? status : STATUS_CREATING;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getSendTo() {
        return (StringUtils.isNotEmpty(sendTo)) ? sendTo : SEND_TO_ALL_CUSTOMERS;
    }

    public void setSendTo(String sendTo) {
        this.sendTo = sendTo;
    }

    public Campaign addHit() {
        if (hits==null) hits = 0l;
        hits ++;
        return this;
    }

    public String toString() {
        return new ToStringBuilder(this)
                .append("id", getId())
                .toString();
    }

    public boolean equals(Object other) {
        if ((this == other)) return true;
        if (!(other instanceof Campaign)) return false;
        Campaign castOther = (Campaign) other;
        return new EqualsBuilder()
                .append(this.getId(), castOther.getId())
                .isEquals();
    }

}