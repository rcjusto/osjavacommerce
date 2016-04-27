package org.store.core.utils.reports;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

public class ReportExportHtml extends IReportExport {

    private boolean useInlineStyles = true;

    public void exportToFile(ReportTable table, File file) {
        try {
            FileUtils.writeStringToFile(file, exportToString(table, "<html><body>", "</body></html>"));
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }

    @Override
    public byte[] exportToBytes(ReportTable table) {
        String cad = exportToString(table, "<html><body>", "</body></html>");
        return cad.getBytes();
    }

    @Override
    public String getContentType() {
        return "text/html";
    }

    @Override
    public String getContentDisposition() {
        return null;
    }

    public String exportToString(ReportTable table, String prefix, String suffix) {
        // generar mapa de estilos
        Map<String, String> mapStyles = new HashMap<String, String>();
        Map<String, Format> mapFormat = new HashMap<String, Format>();
        if (table.getStyles() != null && !table.getStyles().isEmpty()) {
            for (String styleName : table.getStyles().keySet()) {
                ReportStyle repStyle = table.getStyles().get(styleName);
                if (StringUtils.isNotEmpty(repStyle.getFormat())) {
                    mapFormat.put(styleName, getFormater(repStyle.getFormat()));
                }

                String htmlStyle = getHtmlStyle(repStyle);
                if (StringUtils.isNotEmpty(htmlStyle)) mapStyles.put(styleName, htmlStyle);
            }
        }

        StringBuffer buffer = new StringBuffer();
        if (StringUtils.isNotEmpty(prefix)) buffer.append(prefix);
        buffer.append("<table");
        if (StringUtils.isNotEmpty(table.getStyle())) {
            buffer.append(" class=\"").append(table.getStyle()).append("\"");
            if (useInlineStyles && mapStyles.containsKey(table.getStyle())) buffer.append("style=\"").append(mapStyles.get(table.getStyle())).append("\"");
        }
        buffer.append(">");
        for (ReportRow row : table.getRows()) {
            buffer.append("<tr");
            if (StringUtils.isNotEmpty(row.getStyle())) {
                buffer.append(" class=\"").append(row.getStyle()).append("\"");
                if (useInlineStyles && mapStyles.containsKey(row.getStyle())) buffer.append("style=\"").append(mapStyles.get(row.getStyle())).append("\"");
            }
            buffer.append(">\n");
            for (ReportCell cell : row.getCells()) {
                Object value = cell.getValue();
                buffer.append("<td");
                if (cell.getColSpan()>1) buffer.append(" colspan=\"").append(cell.getColSpan()).append("\"");
                if (StringUtils.isNotEmpty(cell.getStyle())) {
                    buffer.append(" class=\"").append(cell.getStyle()).append("\"");
                    if (useInlineStyles && mapStyles.containsKey(cell.getStyle())) buffer.append("style=\"").append(mapStyles.get(cell.getStyle())).append("\"");
                    if (mapFormat.containsKey(cell.getStyle())) {
                        try {
                            value = mapFormat.get(cell.getStyle()).format(value);
                        } catch (Exception ignored) {}
                    }
                }
                buffer.append(">\n");
                buffer.append(value.toString());
                buffer.append("</td>");
            }
            buffer.append("</tr>");
        }
        buffer.append("<table>\n");
        if (StringUtils.isNotEmpty(suffix)) buffer.append(suffix);
        return buffer.toString();
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

    public String getHtmlStyle(ReportStyle style) {
        StringBuffer buffer = new StringBuffer();
        if (StringUtils.isNotEmpty(style.getTextColor())) buffer.append("color:#").append(style.getTextColor()).append(";");
        if (style.getTextSize()!=null) buffer.append("font-size:").append(style.getTextSize()).append("pt;");
        if (StringUtils.isNotEmpty(style.getTextAlign())) buffer.append("text-align:").append(style.getTextAlign()).append(";");
        if (StringUtils.isNotEmpty(style.getBkgColor())) buffer.append("background-color:#").append(style.getBkgColor()).append(";");
        if (StringUtils.isNotEmpty(style.getTextWeight())) buffer.append("font-weight:").append(style.getTextWeight()).append(";");
        if (StringUtils.isNotEmpty(style.getTextStyle())) buffer.append("font-style:").append(style.getTextStyle()).append(";");
        if (style.getBorderTop()!=null) buffer.append("border-top:").append(getHtmlStyleBorder(style.getBorderTop())).append(";");
        if (style.getBorderRight()!=null) buffer.append("border-right:").append(getHtmlStyleBorder(style.getBorderRight())).append(";");
        if (style.getBorderBottom()!=null) buffer.append("border-bottom:").append(getHtmlStyleBorder(style.getBorderBottom())).append(";");
        if (style.getBorderLeft()!=null) buffer.append("border-left:").append(getHtmlStyleBorder(style.getBorderLeft())).append(";");
        return buffer.toString();
    }

    public String getHtmlStyleBorder(ReportStyle.ReportStyleBorder styleBorder) {
        StringBuffer buff = new StringBuffer();
        if (styleBorder.getSize()!=null) buff.append(styleBorder.getSize()).append("px"); else buff.append("1px");
        if (StringUtils.isNotEmpty(styleBorder.getStyle())) buff.append(" ").append(styleBorder.getStyle()); else buff.append(" solid");
        if (StringUtils.isNotEmpty(styleBorder.getColor())) buff.append(" ").append("#").append(styleBorder.getColor());
        return buff.toString();
    }

    public boolean isUseInlineStyles() {
        return useInlineStyles;
    }

    public void setUseInlineStyles(boolean useInlineStyles) {
        this.useInlineStyles = useInlineStyles;
    }
}
