package org.store.core.utils.reports;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import org.apache.commons.lang.StringUtils;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.text.DecimalFormat;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

public class ReportExportPdf extends IReportExport {

    public static final String MARGIN_LEFT = "marginLeft";
    public static final String MARGIN_RIGHT = "marginRight";
    public static final String MARGIN_TOP = "marginTop";
    public static final String MARGIN_BOTTOM = "marginBottom";
    public static final String PAGE_WIDTH = "pageWidth";
    public static final String PAGE_HEIGHT = "pageHeight";
    public static final String CELL_PADDING = "cellPadding";
    public static final String FONT_SIZE = "fontSize";
    public static final String LOGO_PATH = "logoPath";

    public void exportToFile(ReportTable table, File file) {

        try {
            Rectangle defPage = PageSize.LETTER;
            Document document = new Document(new Rectangle(
                    table.getParamNumber(PAGE_WIDTH, defPage.getWidth()).floatValue(),
                    table.getParamNumber(PAGE_HEIGHT, defPage.getHeight()).floatValue()));
            document.setMargins(
                    table.getParamNumber(MARGIN_LEFT, 20).floatValue(),
                    table.getParamNumber(MARGIN_RIGHT, 20).floatValue(),
                    table.getParamNumber(MARGIN_TOP, 20).floatValue(),
                    table.getParamNumber(MARGIN_BOTTOM, 20).floatValue());
            PdfWriter.getInstance(document, new FileOutputStream(file));
            document.open();
            generateDocument(document, table);
            document.close();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

    }


    public byte[] exportToBytes(ReportTable table) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            Rectangle defPage = PageSize.LETTER;
            if (ReportTable.PAGE_LETTER_H==table.getPageSize()) defPage = PageSize.LETTER.rotate();
            if (ReportTable.PAGE_A4==table.getPageSize()) defPage = PageSize.A4;
            if (ReportTable.PAGE_A4_H==table.getPageSize()) defPage = PageSize.A4.rotate();
            Document document = new Document(new Rectangle(
                    table.getParamNumber(PAGE_WIDTH, defPage.getWidth()).floatValue(),
                    table.getParamNumber(PAGE_HEIGHT, defPage.getHeight()).floatValue()));
            document.setMargins(
                    table.getParamNumber(MARGIN_LEFT, 20).floatValue(),
                    table.getParamNumber(MARGIN_RIGHT, 20).floatValue(),
                    table.getParamNumber(MARGIN_TOP, 20).floatValue(),
                    table.getParamNumber(MARGIN_BOTTOM, 20).floatValue());
            PdfWriter.getInstance(document, baos);
            document.open();
            generateDocument(document, table);
            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    public void generateDocument(Document document, ReportTable table) throws Exception {
        Font defaultFont = createFont(table);

        PdfPTable pdfTitle = new PdfPTable(new float[]{7f,3f});
        pdfTitle.setWidthPercentage(100);
        PdfPCell cell = new PdfPCell();
        cell.setBorder(0);

        // titulo
        if (StringUtils.isNotEmpty(table.getTitle())) {
            Font fTitle = new Font();
            fTitle.setSize(20);
            fTitle.setStyle(Font.BOLD);
            Paragraph p = new Paragraph(table.getTitle(), fTitle);
            p.setSpacingAfter(10);
            cell.addElement(p);
        }

        // Parameters
        if (table.getFilters() != null && !table.getFilters().isEmpty()) {
            Font fParam = createFont(table);
            fParam.setSize(8);
            fParam.setStyle(Font.BOLD);
            for (Map.Entry<String, String> entry : table.getFilters().entrySet()) {
                Paragraph p = new Paragraph();
                p.setLeading(10);
                p.add(new Phrase(entry.getKey() + ": ", fParam));
                p.add(new Phrase(entry.getValue(), defaultFont));
                cell.addElement(p);
            }
        }

        pdfTitle.addCell(cell);
        cell = new PdfPCell();
        cell.setBorder(0);
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        String logoPath = table.getParamValue(LOGO_PATH, "").toString();
        if (StringUtils.isNotEmpty(logoPath)) {
            Image logo = Image.getInstance(logoPath);
            logo.scaleToFit(200,50);
            logo.setAlignment(Image.ALIGN_RIGHT);
            cell.addElement(logo);
        }
        pdfTitle.addCell(cell);
        document.add(pdfTitle);

        float cellPadding = table.getParamNumber(CELL_PADDING, 4f).floatValue();

        Map<String, Font> mapFont = createFontMap(table);

        int numCols = table.getNumCols();
        float[] colWidths = new float[table.getNumCols()];

        PdfPTable pdfTable = new PdfPTable(table.getNumCols());
        pdfTable.setSpacingBefore(20);
        pdfTable.setWidthPercentage(100);
        if (table.getColWidths()!=null && table.getColWidths().length>0) pdfTable.setWidths(table.getColWidths());
        int colIndex = 0;
        for (ReportRow reportRow : table.getRows()) {
            for (ReportCell reportCell : reportRow.getCells()) {
                ReportStyle style = (table.getStyles() != null && table.getStyles().containsKey(reportCell.getStyle())) ? table.getStyles().get(reportCell.getStyle()) : null;
                Object value = reportCell.getValue();
                if (style != null && StringUtils.isNotEmpty(style.getFormat())) {
                    try {
                        value = getFormater(style.getFormat()).format(value);
                    } catch (Exception ignored) {
                    }
                }
                Font f = (style != null && mapFont.containsKey(reportCell.getStyle())) ? mapFont.get(reportCell.getStyle()) : defaultFont;
                cell = new PdfPCell(new Phrase(value.toString(), f));
                cell.setColspan(reportCell.getColSpan());
                cell.setNoWrap((style!=null && style.getNoWrap()));
                cell.setPadding(cellPadding);
                cell.setBorder(0);
                if (style != null) setCellStyle(cell, style);
                pdfTable.addCell(cell);

                if (colWidths[colIndex] < value.toString().length()) colWidths[colIndex] = value.toString().length();
                colIndex++;
                if (colIndex >= numCols) colIndex = 0;
            }
        }
        pdfTable.setHeaderRows(1);
        document.add(pdfTable);
    }

    @Override
    public String getContentType() {
        return "application/pdf";
    }

    @Override
    public String getContentDisposition() {
        return "attachment; filename=reporte.pdf";
    }


    protected Map<String, Font> createFontMap(ReportTable table) {
        Map<String, Font> mapFonts = new HashMap<String, Font>();
        if (table.getStyles() != null && !table.getStyles().isEmpty()) {
            for (String styleName : table.getStyles().keySet()) {
                Font font = null;
                ReportStyle style = table.getStyles().get(styleName);

                // color
                if (StringUtils.isNotEmpty(style.getTextColor())) {
                    font = createFont(table);
                    font.setColor(baseColor(style.getTextColor()));
                }

                // font size
                if (style.getTextSize() != null) {
                    if (font == null) font = createFont(table);
                    font.setSize(style.getTextSize().shortValue());
                }

                // font weight
                if ("bold".equalsIgnoreCase(style.getTextWeight()) || "bolder".equalsIgnoreCase(style.getTextWeight())) {
                    if (font == null) font = createFont(table);
                    if ("italic".equalsIgnoreCase(style.getTextStyle())) font.setStyle(Font.BOLDITALIC);
                    else font.setStyle(Font.BOLD);
                } else if ("italic".equalsIgnoreCase(style.getTextStyle())) {
                    if (font == null) font = createFont(table);
                    font.setStyle(Font.ITALIC);
                }

                if (font != null) mapFonts.put(styleName, font);
            }
        }
        return mapFonts;
    }

    private Font createFont(ReportTable table) {
        Font f = new Font();
        f.setSize(table.getParamNumber(FONT_SIZE, 10f).floatValue());
        return f;
    }

    private void setCellStyle(PdfPCell cell, ReportStyle style) {

        // backcolor
        if (StringUtils.isNotEmpty(style.getBkgColor())) {
            cell.setBackgroundColor(baseColor(style.getBkgColor()));
        }

        // alignment
        if ("right".equalsIgnoreCase(style.getTextAlign())) cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        else if ("center".equalsIgnoreCase(style.getTextAlign())) cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        else cell.setHorizontalAlignment(Element.ALIGN_LEFT);

        // borders
        if (style.getBorderTop() != null) setCellTopBorder(cell, style.getBorderTop());
        if (style.getBorderRight() != null) setCellRightBorder(cell, style.getBorderRight());
        if (style.getBorderBottom() != null) setCellBottomBorder(cell, style.getBorderBottom());
        if (style.getBorderLeft() != null) setCellLeftBorder(cell, style.getBorderLeft());

    }

    private void setCellLeftBorder(PdfPCell cell, ReportStyle.ReportStyleBorder border) {
        cell.setBorderWidthLeft(border.getSize() * 0.1f);
        cell.setBorderColorLeft(baseColor(border.getColor()));
    }

    private void setCellBottomBorder(PdfPCell cell, ReportStyle.ReportStyleBorder border) {
        cell.setBorderWidthBottom(border.getSize() * 0.1f);
        cell.setBorderColorBottom(baseColor(border.getColor()));
    }

    private void setCellRightBorder(PdfPCell cell, ReportStyle.ReportStyleBorder border) {
        cell.setBorderWidthRight(border.getSize() * 0.1f);
        cell.setBorderColorRight(baseColor(border.getColor()));
    }

    private void setCellTopBorder(PdfPCell cell, ReportStyle.ReportStyleBorder border) {
        cell.setBorderWidthTop(border.getSize() * 0.1f);
        cell.setBorderColorTop(baseColor(border.getColor()));
    }

    private Format getFormater(String format) {
        if ("date".equalsIgnoreCase(format)) return new SimpleDateFormat("yyyy-MM-dd");
        else if ("time".equalsIgnoreCase(format)) return new SimpleDateFormat("HH:mm");
        else if ("datetime".equalsIgnoreCase(format)) return new SimpleDateFormat("yyyy-MM-dd HH:mm");
        else if ("decimal".equalsIgnoreCase(format)) return new DecimalFormat("0.00");
        else if ("decimal2".equalsIgnoreCase(format)) return new DecimalFormat("0.00");
        else if ("decimal4".equalsIgnoreCase(format)) return new DecimalFormat("0.0000");
        else if ("integer".equalsIgnoreCase(format)) return new DecimalFormat("#");
        return null;
    }

    protected BaseColor baseColor(String cad) {
        if (!cad.startsWith("#")) cad = "#" + cad;
        Color c = Color.decode(cad);
        return (c != null) ? new BaseColor(c) : null;
    }


}
