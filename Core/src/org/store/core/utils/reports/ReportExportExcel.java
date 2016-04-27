package org.store.core.utils.reports;

import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFPalette;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.util.CellRangeAddress;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ReportExportExcel extends IReportExport {

    private Map<String, Short> mapColor = new HashMap<String,Short>();
    private short currentColor = 0;
    private short[] colors = {
            HSSFColor.AQUA.index,
            HSSFColor.BLUE_GREY.index,
            HSSFColor.BRIGHT_GREEN.index,
            HSSFColor.BROWN.index,
            HSSFColor.CORAL.index,
            HSSFColor.CORNFLOWER_BLUE.index,
            HSSFColor.DARK_BLUE.index,
            HSSFColor.DARK_GREEN.index,
            HSSFColor.DARK_RED.index,
            HSSFColor.DARK_TEAL.index,
            HSSFColor.DARK_YELLOW.index,
            HSSFColor.GOLD.index,
            HSSFColor.INDIGO.index,
            HSSFColor.LAVENDER.index,
            HSSFColor.LEMON_CHIFFON.index,
            HSSFColor.LIGHT_BLUE.index,
            HSSFColor.LIGHT_GREEN.index,
            HSSFColor.LIGHT_ORANGE.index,
            HSSFColor.LIGHT_YELLOW.index,
            HSSFColor.LIGHT_TURQUOISE.index,
            HSSFColor.LIGHT_CORNFLOWER_BLUE.index,
            HSSFColor.LIME.index,
            HSSFColor.OLIVE_GREEN.index,
            HSSFColor.ORCHID.index,
            HSSFColor.PALE_BLUE.index,
            HSSFColor.PLUM.index,
            HSSFColor.ROSE.index,
            HSSFColor.ROYAL_BLUE.index,
            HSSFColor.SKY_BLUE.index,
            HSSFColor.SEA_GREEN.index,
            HSSFColor.TAN.index,
            HSSFColor.TEAL.index,
            HSSFColor.TURQUOISE.index,
            HSSFColor.VIOLET.index
    };

    public void exportToFile(ReportTable table, File file) {

        try {
            HSSFWorkbook wb = new HSSFWorkbook();

            // generar mapa de estilos
            Map<String, HSSFCellStyle> mapStyles = createStylesMap(table, wb);

            HSSFSheet sheet = wb.createSheet("Hoja 1");
            int rowIndex = 0;
            for (ReportRow reportRow : table.getRows()) {
                HSSFRow excelRow = sheet.createRow(rowIndex++);
                int colIndex = 0;
                for (ReportCell reportCell : reportRow.getCells()) {
                    HSSFCellStyle style = (StringUtils.isNotEmpty(reportCell.getStyle()) && mapStyles.containsKey(reportCell.getStyle())) ? mapStyles.get(reportCell.getStyle()) : null;
                    setCellValue(excelRow.createCell(colIndex++), (Double) reportCell.getValue(), style);
                }
            }
            FileOutputStream fileOut = new FileOutputStream(file);
            wb.write(fileOut);
            fileOut.close();
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }

    }

    public byte[] exportToBytes(ReportTable table) {

        try {
            HSSFWorkbook wb = new HSSFWorkbook();

            // generar mapa de estilos
            Map<String, HSSFCellStyle> mapStyles = createStylesMap(table, wb);

            HSSFSheet sheet = wb.createSheet("Hoja 1");
            int rowIndex = 0;
            for (ReportRow reportRow : table.getRows()) {
                HSSFRow excelRow = sheet.createRow(rowIndex++);
                int colIndex = 0;
                for (ReportCell reportCell : reportRow.getCells()) {
                    HSSFCellStyle style = (StringUtils.isNotEmpty(reportCell.getStyle()) && mapStyles.containsKey(reportCell.getStyle())) ? mapStyles.get(reportCell.getStyle()) : null;
                    HSSFCell excelCell = excelRow.createCell(colIndex);
                    setCellValue(excelCell, reportCell.getValue(), style);
                    if (reportCell.getColSpan()>1) {
                        sheet.addMergedRegion(new CellRangeAddress(excelRow.getRowNum(),excelRow.getRowNum(),excelCell.getColumnIndex(),excelCell.getColumnIndex()+reportCell.getColSpan()-1));
                        colIndex += reportCell.getColSpan();
                    } else {
                        colIndex++;
                    }
                }
            }
            for(int col = 0; col<table.getNumCols(); col++)
                sheet.autoSizeColumn(col);
            ByteArrayOutputStream fileOut = new ByteArrayOutputStream();
            wb.write(fileOut);
            return fileOut.toByteArray();
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    @Override
    public String getContentType() {
        return "application/vnd.ms-excel";
    }

    @Override
    public String getContentDisposition() {
        return "attachment; filename=reporte.xls";
    }


    protected Map<String, HSSFCellStyle> createStylesMap(ReportTable table, HSSFWorkbook wb) {
        Map<String, HSSFCellStyle> mapStyles = new HashMap<String, HSSFCellStyle>();
        HSSFPalette palette = wb.getCustomPalette();
        if (table.getStyles() != null && !table.getStyles().isEmpty()) {
            for (String styleName : table.getStyles().keySet()) {
                ReportStyle style = table.getStyles().get(styleName);
                HSSFCellStyle cellStyle = wb.createCellStyle();
                HSSFFont cellFont = wb.createFont();

                // backcolor
                if (StringUtils.isNotEmpty(style.getBkgColor())) {
                    cellStyle.setFillForegroundColor(getColorIndex(palette, style.getBkgColor()));
                    cellStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
                }

                // color
                if (StringUtils.isNotEmpty(style.getTextColor()))
                    cellFont.setColor(getColorIndex(palette, style.getTextColor()));

                // font size
                if (style.getTextSize() != null)
                    cellFont.setFontHeightInPoints(style.getTextSize().shortValue());

                // font weight
                if ("bold".equalsIgnoreCase(style.getTextWeight())) cellFont.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
                else if ("bolder".equalsIgnoreCase(style.getTextWeight())) cellFont.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
                else if ("normal".equalsIgnoreCase(style.getTextWeight())) cellFont.setBoldweight(HSSFFont.BOLDWEIGHT_NORMAL);
                else if ("ligther".equalsIgnoreCase(style.getTextWeight())) cellFont.setBoldweight(HSSFFont.BOLDWEIGHT_NORMAL);

                // font style
                if ("italic".equalsIgnoreCase(style.getTextStyle())) cellFont.setItalic(true);

                // alignment
                if ("right".equalsIgnoreCase(style.getTextAlign())) cellStyle.setAlignment(HSSFCellStyle.ALIGN_RIGHT);
                else if ("center".equalsIgnoreCase(style.getTextAlign())) cellStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
                else cellStyle.setAlignment(HSSFCellStyle.ALIGN_LEFT);

                // borders
                if (style.getBorderTop() != null) setCellTopBorder(cellStyle, style.getBorderTop(), palette);
                if (style.getBorderRight() != null) setCellRightBorder(cellStyle, style.getBorderRight(), palette);
                if (style.getBorderBottom() != null) setCellBottomBorder(cellStyle, style.getBorderBottom(), palette);
                if (style.getBorderLeft() != null) setCellLeftBorder(cellStyle, style.getBorderLeft(), palette);

                //formats
                if (StringUtils.isNotEmpty(style.getFormat())) {
                    String format = getExcelFormat(style.getFormat());
                    if (format!=null) cellStyle.setDataFormat(wb.getCreationHelper().createDataFormat().getFormat(format));
                }

                cellStyle.setFont(cellFont);

                mapStyles.put(styleName, cellStyle);
            }
        }
        return mapStyles;
    }

    private String getExcelFormat(String format) {
        if ("date".equalsIgnoreCase(format)) return "yyyy-mm-dd";
        else if ("time".equalsIgnoreCase(format)) return "hh:mm";
        else if ("datetime".equalsIgnoreCase(format)) return "yyyy-mm-dd hh:mm";
        else if ("decimal".equalsIgnoreCase(format)) return "0.00";
        else if ("decimal2".equalsIgnoreCase(format)) return "0.00";
        else if ("decimal4".equalsIgnoreCase(format)) return "0.0000";
        else if ("integer".equalsIgnoreCase(format)) return "############";
        return null;
    }

    private void setCellTopBorder(HSSFCellStyle cellStyle, ReportStyle.ReportStyleBorder border, HSSFPalette palette) {
        short borderStyle = HSSFCellStyle.BORDER_THIN;
        if (border.getSize() != null && border.getSize() > 1) borderStyle = HSSFCellStyle.BORDER_MEDIUM;
        if ("dotted".equalsIgnoreCase(border.getStyle())) borderStyle = HSSFCellStyle.BORDER_DOTTED;
        else if ("dashed".equalsIgnoreCase(border.getStyle())) borderStyle = HSSFCellStyle.BORDER_DASHED;
        else if ("double".equalsIgnoreCase(border.getStyle())) borderStyle = HSSFCellStyle.BORDER_DOUBLE;
        cellStyle.setBorderTop(borderStyle);
        if (StringUtils.isNotEmpty(border.getColor())) {
            cellStyle.setTopBorderColor(getColorIndex(palette, border.getColor()));
        }
    }

    private void setCellBottomBorder(HSSFCellStyle cellStyle, ReportStyle.ReportStyleBorder border, HSSFPalette palette) {
        short borderStyle = HSSFCellStyle.BORDER_THIN;
        if (border.getSize() != null && border.getSize() > 1) borderStyle = HSSFCellStyle.BORDER_MEDIUM;
        if ("dotted".equalsIgnoreCase(border.getStyle())) borderStyle = HSSFCellStyle.BORDER_DOTTED;
        else if ("dashed".equalsIgnoreCase(border.getStyle())) borderStyle = HSSFCellStyle.BORDER_DASHED;
        else if ("double".equalsIgnoreCase(border.getStyle())) borderStyle = HSSFCellStyle.BORDER_DOUBLE;
        cellStyle.setBorderBottom(borderStyle);
        if (StringUtils.isNotEmpty(border.getColor())) {
            cellStyle.setBottomBorderColor(getColorIndex(palette, border.getColor()));
        }
    }

    private void setCellLeftBorder(HSSFCellStyle cellStyle, ReportStyle.ReportStyleBorder border, HSSFPalette palette) {
        short borderStyle = HSSFCellStyle.BORDER_THIN;
        if (border.getSize() != null && border.getSize() > 1) borderStyle = HSSFCellStyle.BORDER_MEDIUM;
        if ("dotted".equalsIgnoreCase(border.getStyle())) borderStyle = HSSFCellStyle.BORDER_DOTTED;
        else if ("dashed".equalsIgnoreCase(border.getStyle())) borderStyle = HSSFCellStyle.BORDER_DASHED;
        else if ("double".equalsIgnoreCase(border.getStyle())) borderStyle = HSSFCellStyle.BORDER_DOUBLE;
        cellStyle.setBorderLeft(borderStyle);
        if (StringUtils.isNotEmpty(border.getColor())) {
            cellStyle.setLeftBorderColor(getColorIndex(palette, border.getColor()));
        }
    }

    private void setCellRightBorder(HSSFCellStyle cellStyle, ReportStyle.ReportStyleBorder border, HSSFPalette palette) {
        short borderStyle = HSSFCellStyle.BORDER_THIN;
        if (border.getSize() != null && border.getSize() > 1) borderStyle = HSSFCellStyle.BORDER_MEDIUM;
        if ("dotted".equalsIgnoreCase(border.getStyle())) borderStyle = HSSFCellStyle.BORDER_DOTTED;
        else if ("dashed".equalsIgnoreCase(border.getStyle())) borderStyle = HSSFCellStyle.BORDER_DASHED;
        else if ("double".equalsIgnoreCase(border.getStyle())) borderStyle = HSSFCellStyle.BORDER_DOUBLE;
        cellStyle.setBorderRight(borderStyle);
        if (StringUtils.isNotEmpty(border.getColor())) {
            cellStyle.setRightBorderColor(getColorIndex(palette, border.getColor()));
        }
    }

    protected void setCellValue(HSSFCell cell, Object val, HSSFCellStyle style) {
        if (cell != null) {
            if (val == null) setCellValue(cell, "", style);
            else if (val instanceof Date) setCellValue(cell, (Date) val, style);
            else if (val instanceof String) setCellValue(cell, (String) val, style);
            else if (val instanceof Integer) setCellValue(cell, (Integer) val, style);
            else if (val instanceof Long) setCellValue(cell, (Long) val, style);
            else if (val instanceof Number) setCellValue(cell, (Number) val, style);
        }
    }

    protected void setCellValue(HSSFCell cell, Number val, HSSFCellStyle style) {
        if (cell != null) {
            if (style != null) cell.setCellStyle(style);
            if (val != null) cell.setCellValue(val.doubleValue());
        }
    }

    protected void setCellValue(HSSFCell cell, String val, HSSFCellStyle style) {
        if (cell != null) {
            if (style != null) cell.setCellStyle(style);
            if (val != null) cell.setCellValue(val);
        }
    }

    protected void setCellValue(HSSFCell cell, Integer val, HSSFCellStyle style) {
        if (cell != null) {
            if (style != null) cell.setCellStyle(style);
            if (val != null) cell.setCellValue(val);
        }
    }

    protected void setCellValue(HSSFCell cell, Long val, HSSFCellStyle style) {
        if (cell != null) {
            if (style != null) cell.setCellStyle(style);
            if (val != null) cell.setCellValue(val);
        }
    }

    protected void setCellValue(HSSFCell cell, Date val, HSSFCellStyle style) {
        if (cell != null) {
            if (style != null) cell.setCellStyle(style);
            if (val != null) cell.setCellValue(val);
        }
    }

    protected byte[] colorBytes(String cad) {
        if (!cad.startsWith("#")) cad = "#" + cad;
        Color c = Color.decode(cad);
        return new byte[]{(byte) c.getRed(), (byte) c.getGreen(), (byte) c.getBlue()};
    }

    protected short getColorIndex(HSSFPalette palette, String color) {
        if ("ffffff".equalsIgnoreCase(color)) return HSSFColor.WHITE.index;
        else if ("000000".equalsIgnoreCase(color)) return HSSFColor.BLACK.index;
        else if (mapColor.containsKey(color)) return mapColor.get(color);
        else {
            byte[] colorBytes = colorBytes(color);
            short index = colors[currentColor++];
            palette.setColorAtIndex(index, colorBytes[0], colorBytes[1], colorBytes[2]);
            mapColor.put(color, index);
            return index;
        }

    }

}
