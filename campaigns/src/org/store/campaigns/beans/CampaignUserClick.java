package org.store.campaigns.beans;

import org.store.core.beans.BaseBean;
import org.apache.commons.lang.builder.ToStringBuilder;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Calendar;
import java.util.Date;

@Entity
@Table(name = "t_campaign_user_click")
public class CampaignUserClick extends BaseBean {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long idCampaign;
    private Long idUser;
    private String url;
    private Date clicked;

    public CampaignUserClick() {
        this.setClicked(Calendar.getInstance().getTime());
    }

    public CampaignUserClick(CampaignUser cu) {
        this.setIdCampaign(cu.getIdCampaign());
        this.setIdUser(cu.getIdUser());
        this.setClicked(Calendar.getInstance().getTime());
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

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
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