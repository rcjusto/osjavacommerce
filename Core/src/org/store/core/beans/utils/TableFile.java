package org.store.core.beans.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TableFile {

    public List<String> fields;
    public List<TableFileRow> rows;

    public TableFile() {
    }

    public void addRow(String[] lineValues) {
        if (rows == null) rows = new ArrayList<TableFileRow>();
        if (lineValues != null) rows.add(new TableFileRow(lineValues));
    }

    public void addRow(List<String> lineValues) {
        if (rows == null) rows = new ArrayList<TableFileRow>();
        if (lineValues != null) rows.add(new TableFileRow(lineValues));
    }

    public List<String> getHeaders() {
        return fields;
    }

    public List<TableFileRow> getRows() {
        return rows;
    }


    public class TableFileRow {
        public List<String> values;

        public TableFileRow(String[] arrValues) {
            this.values = Arrays.asList(arrValues);
        }

        public TableFileRow(List<String> arrValues) {
            this.values = arrValues;
        }

        public String getCol(int col) {
            return (values != null && values.size() > col) ? values.get(col) : "";
        }

    }
}
