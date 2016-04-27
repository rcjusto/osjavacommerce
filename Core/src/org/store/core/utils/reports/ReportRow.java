package org.store.core.utils.reports;

import java.util.ArrayList;
import java.util.List;

public class ReportRow {

    private List<ReportCell> cells;
    private String style;

    public List<ReportCell> getCells() {
        return cells;
    }

    public void setCells(List<ReportCell> cells) {
        this.cells = cells;
    }

    public String getStyle() {
        return style;
    }

    public ReportRow setStyle(String style) {
        this.style = style;
        return this;
    }

    public ReportRow addCell(ReportCell cell) {
        if(cells==null) cells = new ArrayList<ReportCell>();
        cells.add(cell);
        return this;
    }

    public int getNumCols() {
        int res = 0;
        if (cells!=null)
            for(ReportCell cell : cells)
                if (cell!=null)
                    res += cell.getColSpan();
        return res;
    }

}
