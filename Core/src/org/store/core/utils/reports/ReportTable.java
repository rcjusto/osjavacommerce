package org.store.core.utils.reports;


import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReportTable {

    public static final int PAGE_LETTER = 1;
    public static final int PAGE_LETTER_H = 2;
    public static final int PAGE_A4 = 3;
    public static final int PAGE_A4_H = 4;

    private float[] colWidths;
    private List<ReportRow> rows;
    private String style;
    private Map<String,ReportStyle> styles;
    private String title;
    private Map<String, String> filters;
    private Map<String, Object> params;
    private int pageSize = PAGE_LETTER;
    private boolean autoCalculateWidth = true;

    public ReportTable(String title) {
        this.title = title;
    }

    public List<ReportRow> getRows() {
        return rows;
    }

    public void setRows(List<ReportRow> rows) {
        this.rows = rows;
    }

    public String getStyle() {
        return style;
    }

    public ReportTable setStyle(String style) {
        this.style = style;
        return this;
    }

    public int getPageSize() {
        return pageSize;
    }

    public ReportTable setPageSize(int pageSize) {
        this.pageSize = pageSize;
        return this;
    }

    public Map<String, ReportStyle> getStyles() {
        return styles;
    }

    public void setStyles(Map<String, ReportStyle> styles) {
        this.styles = styles;
    }

    public float[] getColWidths() {
        return colWidths;
    }

    public ReportTable setColWidths(float[] colWidths) {
        this.colWidths = colWidths;
        this.autoCalculateWidth = false;
        return this;
    }

    public ReportTable setColWidths(Collection colWidths) {
        if (colWidths!=null && !colWidths.isEmpty()) {
            this.colWidths = new float[colWidths.size()];
            int i = 0;
            for(Object o : colWidths) this.colWidths[i++] = (Float) o;
            this.autoCalculateWidth = false;
        }
        return this;
    }

    public String getTitle() {
        return title;
    }

    public ReportTable setTitle(String title) {
        this.title = title;
        return this;
    }

    public Map<String, String> getFilters() {
        return filters;
    }

    public void setFilters(Map<String, String> filters) {
        this.filters = filters;
    }

    public Map<String, Object> getParams() {
        return params;
    }

    public void setParams(Map<String, Object> params) {
        this.params = params;
    }

    public ReportTable addFilter(String key, String value) {
        if (filters ==null) filters = new HashMap<String,String>();
        filters.put(key,value);
        return this;
    }

    public ReportTable addParam(String key, Object value) {
        if (params ==null) params = new HashMap<String,Object>();
        params.put(key,value);
        return this;
    }

    public Object getParamValue(String key, Object defaultValue) {
        return (params!=null && params.containsKey(key) && params.get(key)!=null) ? params.get(key) : defaultValue;
    }

    public Number getParamNumber(String key, Number defaultValue) {
        return (params!=null && params.containsKey(key) && params.get(key)!=null) ? (Number) params.get(key) : defaultValue;
    }

    public ReportTable addRow(ReportRow row) {
        if (rows==null) rows = new ArrayList<ReportRow>();
        rows.add(row);
        if (autoCalculateWidth) updateColWidths(row);
        return this;
    }

    public ReportTable addStyle(String name, ReportStyle style) {
        if (styles==null) styles = new HashMap<String,ReportStyle>();
        styles.put(name, style);
        return this;
    }

    public int getNumCols() {
        int result = 0;
        if (rows!=null && !rows.isEmpty()) {
            for(ReportCell cell : rows.get(0).getCells())
                if (cell!=null) result += cell.getColSpan();
        }
        return result;
    }

    private void updateColWidths(ReportRow row) {
        if (row!=null && row.getCells()!=null) {
            if (colWidths==null || colWidths.length<1) colWidths = new float[row.getNumCols()];
            int index = 0;
            for(ReportCell cell : row.getCells()) {
                float numChars = cell.getValue().toString().length() / cell.getColSpan();
                if (colWidths[index]<numChars) colWidths[index] = numChars;
                index += cell.getColSpan();
            }
        }
    }
}
