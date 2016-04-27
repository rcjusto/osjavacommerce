package org.store.campaigns.beans;

import org.store.core.beans.BaseBean;
import org.apache.commons.lang.builder.ToStringBuilder;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;


@Entity
@Table(name = "t_campaign_condition")
public class CampaignCondition extends BaseBean {

    /**
     * identifier field
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * clase q valida la condicion
     */
    @Column(length = 250)
    private String conditionClass;

    /**
     * parametros
     */
    @Lob
    private String conditionParams;

    // Usuarios del grupo
    @ManyToOne
    private Campaign campaign;

    public CampaignCondition() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getConditionClass() {
        return conditionClass;
    }

    public void setConditionClass(String conditionClass) {
        this.conditionClass = conditionClass;
    }

    public String getConditionParams() {
        return conditionParams;
    }

    public void setConditionParams(String conditionParams) {
        this.conditionParams = conditionParams;
    }

    public Campaign getCampaign() {
        return campaign;
    }

    public void setCampaign(Campaign campaign) {
        this.campaign = campaign;
    }

    public String toString() {
        return new ToStringBuilder(this)
                .append("id", getId())
                .toString();
    }

}