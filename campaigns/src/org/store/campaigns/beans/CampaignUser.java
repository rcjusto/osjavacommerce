package org.store.campaigns.beans;

import org.store.core.beans.BaseBean;
import org.apache.commons.lang.builder.ToStringBuilder;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@Entity
@Table(name = "t_campaign_user")
public class CampaignUser extends BaseBean {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long idCampaign;
    private Long idUser;
    private Date opened;
    private Date clicked;

    public CampaignUser() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getIdUser() {
        return idUser;
    }

    public void setIdUser(Long idUser) {
        this.idUser = idUser;
    }

    public Long getIdCampaign() {
        return idCampaign;
    }

    public void setIdCampaign(Long idCampaign) {
        this.idCampaign = idCampaign;
    }

    public Date getOpened() {
        return opened;
    }

    public void setOpened(Date opened) {
        this.opened = opened;
    }

    public Date getClicked() {
        return clicked;
    }

    public void setClicked(Date clicked) {
        this.clicked = clicked;
    }

    public String toString() {
        return new ToStringBuilder(this)
                .append("id", getId())
                .toString();
    }

}