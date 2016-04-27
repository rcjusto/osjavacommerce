package org.store.core.beans;

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
import java.util.Calendar;
import java.util.Date;

/**
 * RMA LOgs
 */
@Entity
@Table(name = "t_rma_log")
public class RmaLog extends BaseBean {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Rma rma;

    @Column(length = 50)
    private String rmaStatus;

    private Date actionDate;

    @ManyToOne
    private User actionUser;

    @Lob
    private String actionComments;

    public RmaLog() {
        actionDate = Calendar.getInstance().getTime();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Rma getRma() {
        return rma;
    }

    public void setRma(Rma rma) {
        this.rma = rma;
    }

    public String getRmaStatus() {
        return rmaStatus;
    }

    public void setRmaStatus(String rmaStatus) {
        this.rmaStatus = rmaStatus;
    }

    public Date getActionDate() {
        return actionDate;
    }

    public void setActionDate(Date actionDate) {
        this.actionDate = actionDate;
    }

    public User getActionUser() {
        return actionUser;
    }

    public void setActionUser(User actionUser) {
        this.actionUser = actionUser;
    }

    public String getActionComments() {
        return actionComments;
    }

    public void setActionComments(String actionComments) {
        this.actionComments = actionComments;
    }

    public String toString() {
        return new ToStringBuilder(this)
                .append("id", getId())
                .toString();
    }

    public boolean equals(Object other) {
        if ((this == other)) return true;
        if (!(other instanceof RmaLog)) return false;
        RmaLog castOther = (RmaLog) other;
        return new EqualsBuilder()
                .append(this.getId(), castOther.getId())
                .isEquals();
    }


}