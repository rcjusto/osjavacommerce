package org.store.core.beans;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.util.Date;
import java.util.Calendar;


/**
 * Producto en el wishlist del usuario.
 * Fijarse q es un OrderDetail, listo para agregar al Shopcart 
 */
@Entity
@Table(name="t_user_wishlist")
public class UserWishList extends BaseBean {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** producto */
    private Long idProduct;

    private Long idVariation;

    private Date created;

    /** usuario */
    @ManyToOne
    private User user;

    public UserWishList() {
        created = Calendar.getInstance().getTime();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getIdProduct() {
        return idProduct;
    }

    public void setIdProduct(Long idProduct) {
        this.idProduct = idProduct;
    }

    public Long getIdVariation() {
        return idVariation;
    }

    public void setIdVariation(Long idVariation) {
        this.idVariation = idVariation;
    }

    public Date getCreated() {
        return created;
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
}