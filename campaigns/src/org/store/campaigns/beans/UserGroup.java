package org.store.campaigns.beans;

import org.store.core.beans.BaseBean;
import org.store.core.beans.utils.StoreBean;
import org.apache.commons.lang.builder.ToStringBuilder;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Calendar;
import java.util.Date;


@Entity
@Table(name = "t_user_group")
public class UserGroup extends BaseBean implements StoreBean {

    /**
     * identifier field
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idGroup;

    /**
     * nombre del grupo
     */
    @Column(length = 250)
    private String groupName;

    // Tienda en la q esta configurado el grupo
    @Column(length = 10)
    private String inventaryCode;

    // Fecha de creacion
    private Date created;

    public UserGroup() {
        created = Calendar.getInstance().getTime();
    }

    public Long getIdGroup() {
        return idGroup;
    }

    public void setIdGroup(Long idGroup) {
        this.idGroup = idGroup;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getInventaryCode() {
        return inventaryCode;
    }

    public void setInventaryCode(String inventaryCode) {
        this.inventaryCode = inventaryCode;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public String toString() {
        return new ToStringBuilder(this)
                .append("id", getIdGroup())
                .toString();
    }

}