package org.store.core.beans;

import org.apache.commons.lang.builder.ToStringBuilder;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.util.Date;

/**
 * Historia de los estados de la orden
 */
@Entity
@Table(name="t_order_history")
public class OrderHistory extends BaseBean {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idHistory;

    // Estado en q se encuentra la orden
    @ManyToOne
    private OrderStatus historyStatus;

    // Fecha del cambio
    private Date historyDate;

    // Comentarios sobre el cambio de estado
    @Lob
    private String historyComment;
    // Administrador q cambio el estado
    @ManyToOne
    private User user;
    // Orden
    @ManyToOne
    private Order order;


    public OrderHistory() {
    }

    public Long getIdHistory() {
        return idHistory;
    }

    public void setIdHistory(Long idHistory) {
        this.idHistory = idHistory;
    }

    public OrderStatus getHistoryStatus() {
        return historyStatus;
    }

    public void setHistoryStatus(OrderStatus historyStatus) {
        this.historyStatus = historyStatus;
    }

    public Date getHistoryDate() {
        return historyDate;
    }

    public void setHistoryDate(Date historyDate) {
        this.historyDate = historyDate;
    }

    public String getHistoryComment() {
        return historyComment;
    }

    public void setHistoryComment(String historyComment) {
        this.historyComment = historyComment;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public String toString() {
        return new ToStringBuilder(this)
            .append("id", getIdHistory())
            .toString();
    }

}