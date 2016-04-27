package org.store.core.admin;

import org.store.core.beans.OrderStatus;
import org.store.core.beans.utils.BreadCrumb;
import org.store.core.globals.StoreMessages;
import org.apache.commons.lang.StringUtils;

public class orderstatusAction extends AdminModuleAction implements StoreMessages {

    @Override
    public void prepare() throws Exception {
        orderStatus = (OrderStatus) dao.get(OrderStatus.class, idStatus);
    }


    public String list() throws Exception {
        if (selecteds != null && selecteds.length > 0) {
            for (Long id : selecteds) {
                OrderStatus bean = (OrderStatus) dao.get(OrderStatus.class, id);
                if (bean != null) {
                    String res = dao.isUsedStatus(bean);
                    if (StringUtils.isNotEmpty(res)) {
                        addActionError(getText(CNT_ERROR_CANNOT_DELETE_STATUS, CNT_DEFAULT_ERROR_CANNOT_DELETE_STATUS, new String[]{bean.getStatusCode(), res}));
                    } else {
                        dao.delete(bean);
                    }
                }
            }
            dao.flushSession();
        }
        getBreadCrumbs().add(new BreadCrumb(null, getText("admin.order.status.list"), null, null));
        addToStack("statuslist", dao.getOrderStatuses());
        return SUCCESS;
    }

    public String edit() throws Exception {
        getBreadCrumbs().add(new BreadCrumb(null, getText("admin.order.status.list"), url("listorderstatus", "/admin"), null));
        getBreadCrumbs().add(new BreadCrumb(null, getText(orderStatus != null ? "admin.order.status.modify" : "admin.order.status.new"), null, null));
        return SUCCESS;
    }

    public String save() throws Exception {
        if (orderStatus != null) {
            if (statusName != null && statusName.length == getLanguages().length) {
                for (int i = 0; i < getLanguages().length; i++) {
                    orderStatus.setStatusName(getLanguages()[i], StringUtils.isNotEmpty(statusName[i]) ? statusName[i] : "");
                }
            }
            orderStatus.setInventaryCode(getStoreCode());
            dao.save(orderStatus);
        }
        return SUCCESS;
    }

    private OrderStatus orderStatus;
    private Long idStatus;
    private String[] statusName;

    public OrderStatus getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(OrderStatus orderStatus) {
        this.orderStatus = orderStatus;
    }

    public Long getIdStatus() {
        return idStatus;
    }

    public void setIdStatus(Long idStatus) {
        this.idStatus = idStatus;
    }

    public String[] getStatusName() {
        return statusName;
    }

    public void setStatusName(String[] statusName) {
        this.statusName = statusName;
    }
}
