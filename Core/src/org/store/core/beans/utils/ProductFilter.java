package org.store.core.beans.utils;

import org.store.core.globals.SomeUtils;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: Rogelio
 * Date: 22-may-2010
 * Time: 16:45:00
 * To change this template use File | Settings | File Templates.
 */
public class ProductFilter extends BaseFilter {
    public static Logger log = Logger.getLogger(ProductFilter.class);

    private String filterId;
    private String filterLabel;
    private String filterHasDiscount;
    private String filterEnabled;
    private String filterCategories;
    private String filterManufacturer;
    private String filterMinStock;
    private String filterMaxStock;
    private String filterMinPrice;
    private String filterMaxPrice;
    private String filterCode;
    private String filterName;
    private String filterUserLevel;
    private String filterDayWithoutStock;
    private String filterQuery;
    private String showArchived;
    private String[] filterAttributes;

    public ProductFilter() {
        showArchived = "N";
    }

    public ProductFilter(Map map) {
        loadFromMap(map);
    }

    public ProductFilter(String filters) {
        if (StringUtils.isNotEmpty(filters)) {
            String[] arr = StringUtils.split(filters, "||");
            for (String f : arr) {
                String[] arr1 = StringUtils.split(f, ":", 2);
                if (arr1.length == 2) {
                    if ("label".equalsIgnoreCase(arr1[0])) filterLabel = arr1[1];
                    else if ("discount".equalsIgnoreCase(arr1[0])) filterHasDiscount = arr1[1];
                    else if ("enabled".equalsIgnoreCase(arr1[0])) filterEnabled = arr1[1];
                    else if ("category".equalsIgnoreCase(arr1[0])) filterCategories = arr1[1];
                    else if ("manufacturer".equalsIgnoreCase(arr1[0])) filterManufacturer = arr1[1];
                    else if ("minStock".equalsIgnoreCase(arr1[0])) filterMinStock = arr1[1];
                    else if ("maxStock".equalsIgnoreCase(arr1[0])) filterMaxStock = arr1[1];
                    else if ("minPrice".equalsIgnoreCase(arr1[0])) filterMinPrice = arr1[1];
                    else if ("maxPrice".equalsIgnoreCase(arr1[0])) filterMaxPrice = arr1[1];
                    else if ("code".equalsIgnoreCase(arr1[0])) filterCode = arr1[1];
                    else if ("name".equalsIgnoreCase(arr1[0])) filterName = arr1[1];
                    else if ("id".equalsIgnoreCase(arr1[0])) filterId = arr1[1];
                }
            }
        }
    }

    public String getFilterLabel() {
        return filterLabel;
    }

    public void setFilterLabel(String filterLabel) {
        this.filterLabel = filterLabel;
    }

    public String getFilterHasDiscount() {
        return filterHasDiscount;
    }

    public void setFilterHasDiscount(String filterHasDiscount) {
        this.filterHasDiscount = filterHasDiscount;
    }

    public String getFilterEnabled() {
        return filterEnabled;
    }

    public void setFilterEnabled(String filterEnabled) {
        this.filterEnabled = filterEnabled;
    }

    public String getFilterCategories() {
        return filterCategories;
    }

    public void setFilterCategories(String filterCategories) {
        this.filterCategories = filterCategories;
    }


    public String getFilterManufacturer() {
        return filterManufacturer;
    }

    public void setFilterManufacturer(String filterManufacturer) {
        this.filterManufacturer = filterManufacturer;
    }

    public String getFilterMinStock() {
        return filterMinStock;
    }

    public void setFilterMinStock(String filterMinStock) {
        this.filterMinStock = filterMinStock;
    }

    public String getFilterMaxStock() {
        return filterMaxStock;
    }

    public void setFilterMaxStock(String filterMaxStock) {
        this.filterMaxStock = filterMaxStock;
    }

    public String getFilterMinPrice() {
        return filterMinPrice;
    }

    public void setFilterMinPrice(String filterMinPrice) {
        this.filterMinPrice = filterMinPrice;
    }

    public String getFilterMaxPrice() {
        return filterMaxPrice;
    }

    public void setFilterMaxPrice(String filterMaxPrice) {
        this.filterMaxPrice = filterMaxPrice;
    }

    public String getFilterCode() {
        return filterCode;
    }

    public void setFilterCode(String filterCode) {
        this.filterCode = filterCode;
    }

    public String getFilterName() {
        return filterName;
    }

    public void setFilterName(String filterName) {
        this.filterName = filterName;
    }

    public String getFilterId() {
        return filterId;
    }

    public void setFilterId(String filterId) {
        this.filterId = filterId;
    }

    public String getShowArchived() {
        return showArchived;
    }

    public void setShowArchived(String showArchived) {
        this.showArchived = showArchived;
    }

    public String getFilterDayWithoutStock() {
        return filterDayWithoutStock;
    }

    public void setFilterDayWithoutStock(String filterDayWithoutStock) {
        this.filterDayWithoutStock = filterDayWithoutStock;
    }

    public String getFilterUserLevel() {
        return filterUserLevel;
    }

    public void setFilterUserLevel(String filterUserLevel) {
        this.filterUserLevel = filterUserLevel;
    }

    public String[] getFilterAttributes() {
        return filterAttributes;
    }

    public void setFilterAttributes(String[] filterAttributes) {
        this.filterAttributes = filterAttributes;
    }

    public String getFilterQuery() {
        return filterQuery;
    }

    public void setFilterQuery(String filterQuery) {
        this.filterQuery = filterQuery;
    }

    public Map<Long, String> getFilterAttributesMap() {
        Map<Long,String> res = new HashMap<Long,String>();
        if (filterAttributes!=null && filterAttributes.length>0) {
            for(String cad : filterAttributes) {
                if (StringUtils.isNotEmpty(cad)) {
                    int i = StringUtils.indexOf(cad, "=");
                    if (i>0 && i<cad.length()-1) {
                        Long id = SomeUtils.strToLong(cad.substring(0, i));
                        String value = cad.substring(i+1);
                        if (StringUtils.isNotEmpty(value) && id!=null) {
                            res.put(id,value);
                        }
                    }
                }
            }
        }
        return res;
    }

    @Override
    public String getDefaultSortedField() {
        return "idProduct";
    }

    @Override
    public String getDefaultSortedDirection() {
        return "desc";
    }

    public boolean hasPriceFilter() {
        return StringUtils.isNotEmpty(filterMinPrice) || StringUtils.isNotEmpty(filterMaxPrice);
    }

    public void loadFromMap(Map map) {
        if (map != null)
            try {
                BeanUtils.populate(this, map);
            } catch (IllegalAccessException e) {
                log.error(e.getMessage(), e);
            } catch (InvocationTargetException e) {
                log.error(e.getMessage(), e);
            }
    }
}
