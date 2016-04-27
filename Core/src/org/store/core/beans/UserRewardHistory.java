package org.store.core.beans;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.util.Calendar;
import java.util.Date;


@Entity
@Table(name="t_user_reward_history")
public class UserRewardHistory extends BaseBean {

    public static final String TYPE_REGISTERING = "registering";
    public static final String TYPE_PURCHASE_CANCELLATION = "purchase_cancellation";
    public static final String TYPE_PURCHASE = "purchase";
    public static final String TYPE_REFER_FRIEND = "refer_friend";
    public static final String TYPE_SUBMIT_REVIEW = "submit_review";
    public static final String TYPE_SUBMIT_POLL = "submit_poll";
    public static final String TYPE_FRIEND_LINK = "friend_link";
    public static final String TYPE_FRIEND_REGISTERING = "friend_registering";
    public static final String TYPE_FRIEND_PURCHASE = "friend_purchase";

    public static final String STATUS_PENDING = "pending";
    public static final String STATUS_COMPLETE = "complete";
    public static final String STATUS_CANCELLED = "cancelled";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 1024)
    private String message;

    @Column(length = 50)
    private String type;

    private Double amount;

    private Date created;

    private Long idOrder;

    private String status;

    @ManyToOne
    private User friend;

    @ManyToOne
    private User user;


    public UserRewardHistory() {
        created = Calendar.getInstance().getTime();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public Long getIdOrder() {
        return idOrder;
    }

    public void setIdOrder(Long idOrder) {
        this.idOrder = idOrder;
    }

    public User getFriend() {
        return friend;
    }

    public void setFriend(User friend) {
        this.friend = friend;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String toString() {
        return new ToStringBuilder(this)
            .append("id", getId())
            .toString();
    }

    public boolean equals(Object other) {
        if ((this == other)) return true;
        if (!(other instanceof UserRewardHistory)) return false;
        UserRewardHistory castOther = (UserRewardHistory) other;
        return new EqualsBuilder()
                .append(this.getId(), castOther.getId())
                .isEquals();
    }


}