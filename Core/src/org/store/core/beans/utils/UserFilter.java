package org.store.core.beans.utils;

/**
 * Created by IntelliJ IDEA.
 * User: Rogelio
 * Date: 22-may-2010
 * Time: 16:45:00
 * To change this template use File | Settings | File Templates.
 */
public class UserFilter extends BaseFilter {

    private String filterCategories;
    private Long filterLevel;
    private String filterName;
    private String filterMinOrder;
    private String filterMaxOrder;

    public String getFilterCategories() {
        return filterCategories;
    }

    public void setFilterCategories(String filterCategories) {
        this.filterCategories = filterCategories;
    }

    public Long getFilterLevel() {
        return filterLevel;
    }

    public void setFilterLevel(Long filterLevel) {
        this.filterLevel = filterLevel;
    }

    public String getFilterName() {
        return filterName;
    }

    public void setFilterName(String filterName) {
        this.filterName = filterName;
    }

    public String getFilterMinOrder() {
        return filterMinOrder;
    }

    public void setFilterMinOrder(String filterMinOrder) {
        this.filterMinOrder = filterMinOrder;
    }

    public String getFilterMaxOrder() {
        return filterMaxOrder;
    }

    public void setFilterMaxOrder(String filterMaxOrder) {
        this.filterMaxOrder = filterMaxOrder;
    }

    @Override
    public String getDefaultSortedField() {
        return "idUser";
    }


}