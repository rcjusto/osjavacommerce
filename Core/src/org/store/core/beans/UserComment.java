package org.store.core.beans;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.hibernate.event.EventSource;
import org.store.core.beans.utils.StoreBean;

import javax.persistence.*;
import java.util.Calendar;
import java.util.Date;

/**
 * Comentario del usuario
 */
@Entity
@Table(name="t_user_comment")
public class UserComment extends BaseBean implements StoreBean {

    public static final String STATUS_NEW = "N";
    public static final String STATUS_ACTIVE = "A";
    public static final String STATUS_INACTIVE = "I";

    /** identifier field */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idComment;

    /** Titulo del comentario */
    @Column(length = 512)
    private String title;

    /** Comentario del usuario */
    @Lob
    private String comment;

    /** email del usuario */
    @Column(length = 250)
    private String userEmail;

    /** nombre del usuario */
    @Column(length = 250)
    private String userName;

    @Column(length = 50)
    private String commentType;

    @Column(length = 1)
    private String commentStatus;

    /** Feche de la nota */
    private Date created;

    // Tienda en la q se hizo el comentario
    @Column(length = 10)
    private String inventaryCode;

    /** Usuario sobre el q se escribe la nota */
    @ManyToOne
    private User user;


    public Long getIdComment() {
        return idComment;
    }

    public void setIdComment(Long idComment) {
        this.idComment = idComment;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getCommentType() {
        return commentType;
    }

    public void setCommentType(String commentType) {
        this.commentType = commentType;
    }

    public String getCommentStatus() {
        return commentStatus;
    }

    public void setCommentStatus(String commentStatus) {
        this.commentStatus = commentStatus;
    }

    public Date getCreated() {
        return created;
    }

    public String getInventaryCode() {
        return inventaryCode;
    }

    public void setInventaryCode(String inventaryCode) {
        this.inventaryCode = inventaryCode;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String toString() {
        return new ToStringBuilder(this)
            .append("id", getIdComment())
            .toString();
    }

    @Override
    public boolean handlePreUpdate(EventSource session, boolean isNew) {
        if (this.created==null) this.created = Calendar.getInstance().getTime();
        return super.handlePreUpdate(session, isNew);
    }
}