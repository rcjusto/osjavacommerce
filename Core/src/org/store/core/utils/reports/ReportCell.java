package org.store.core.utils.reports;

public class ReportCell {

    private Object value;
    private String style;
    private int colSpan = 1;

    public ReportCell(Object value) {
        this.value = value;
    }

    public ReportCell(Object value, String style) {
        this.value = value;
        this.style = style;
    }

    public ReportCell(Object value, String style, int colSpan) {
        this.value = value;
        this.style = style;
        this.colSpan = colSpan;
    }

    public Object getValue() {
        return (value!=null) ? value : "";
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public String getStyle() {
        return style;
    }

    public void setStyle(String style) {
        this.style = style;
    }

    public int getColSpan() {
        return colSpan;
    }

    public void setColSpan(int colSpan) {
        this.colSpan = colSpan;
    }
}
