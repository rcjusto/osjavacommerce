package org.store.core.beans.utils;

import org.apache.commons.lang.StringUtils;

public abstract class BaseFilter {

    protected String sorted;
    protected String sortedField;
    protected String sortedDirection;

    public abstract String getDefaultSortedField();
    public String getDefaultSortedDirection() { return "asc"; }

    public String getSortedField() {
        return (StringUtils.isNotEmpty(sortedField)) ? sortedField : getDefaultSortedField();
    }

    public void setSortedField(String sortedField) {
        this.sortedField = sortedField;
    }

    public String getSortedDirection() {
        return (StringUtils.isNotEmpty(sortedDirection)) ? sortedDirection : getDefaultSortedDirection();
    }

    public void setSortedDirection(String sortedDirection) {
        this.sortedDirection = sortedDirection;
    }

    public String getSorted() {
        return sorted;
    }

    public void setSorted(String sorted) {
        this.sorted = sorted;
        String[] arr = StringUtils.split(sorted, ":");
        if (arr!=null) {
            if (arr.length>0) sortedField = arr[0];
            if (arr.length>1) sortedDirection = arr[1];
        }
    }
}