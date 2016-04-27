package org.store.core.admin;

import org.store.core.beans.ProductReview;
import org.store.core.beans.utils.BreadCrumb;
import org.store.core.beans.utils.DataNavigator;
import org.store.core.globals.SomeUtils;
import org.store.core.globals.StoreMessages;

public class reviewsAction extends AdminModuleAction implements StoreMessages {

    public String list() throws Exception {

        // primero salvar
        if (idReview != null && idReview.length > 0) {
            for (int i=0; i<idReview.length; i++) {
                ProductReview r = (ProductReview) dao.get(ProductReview.class, idReview[i]);
                if (r != null) {
                    if (remove!=null && remove.length>i && "Y".equalsIgnoreCase(remove[i])) {
                        dao.delete(r);
                    } else {
                        if (visible!=null && visible.length>i) r.setVisible( "Y".equalsIgnoreCase(visible[i]));
                        dao.save(r);
                    }
                }
            }
        }

        DataNavigator nav = new DataNavigator(getRequest(), "reviews");
        nav.setListado(dao.getReviews(nav, status, SomeUtils.strToDate(dateFrom, getDefaultLanguage()), SomeUtils.strToDate(dateTo, getDefaultLanguage()), code));
        addToStack("reviews", nav);

        getBreadCrumbs().add(new BreadCrumb(null, getText("admin.reviews.list"), null, null));
        return SUCCESS;
    }


    private Long[] idReview;
    private String[] remove;
    private String[] visible;
    private String[] stateCode;
    private String code;
    private String status;
    private String dateFrom;
    private String dateTo;


    public Long[] getIdReview() {
        return idReview;
    }

    public void setIdReview(Long[] idReview) {
        this.idReview = idReview;
    }

    public String[] getRemove() {
        return remove;
    }

    public void setRemove(String[] remove) {
        this.remove = remove;
    }

    public String[] getVisible() {
        return visible;
    }

    public void setVisible(String[] visible) {
        this.visible = visible;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDateFrom() {
        return dateFrom;
    }

    public void setDateFrom(String dateFrom) {
        this.dateFrom = dateFrom;
    }

    public String getDateTo() {
        return dateTo;
    }

    public void setDateTo(String dateTo) {
        this.dateTo = dateTo;
    }

    public String[] getStateCode() {
        return stateCode;
    }

    public void setStateCode(String[] stateCode) {
        this.stateCode = stateCode;
    }

}
