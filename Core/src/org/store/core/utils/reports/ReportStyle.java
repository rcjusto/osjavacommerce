package org.store.core.utils.reports;


public class ReportStyle {

    private String name;
    private String textColor;
    private Integer textSize;
    private String textAlign;
    private String bkgColor;
    private String textWeight;
    private String textStyle;
    private String format;
    private Boolean noWrap;
    private ReportStyleBorder borderTop;
    private ReportStyleBorder borderBottom;
    private ReportStyleBorder borderLeft;
    private ReportStyleBorder borderRight;

    public ReportStyle(String name) {
        this.name = name;
    }

    public String getFormat() {
        return format;
    }

    public ReportStyle setFormat(String format) {
        this.format = format;
        return this;
    }

    public String getName() {
        return name;
    }

    public ReportStyle setName(String name) {
        this.name = name;
        return this;
    }

    public String getTextColor() {
        return textColor;
    }

    public ReportStyle setTextColor(String textColor) {
        this.textColor = textColor;
        return this;
    }

    public Integer getTextSize() {
        return textSize;
    }

    public ReportStyle setTextSize(Integer textSize) {
        this.textSize = textSize;
        return this;
    }

    public String getTextAlign() {
        return textAlign;
    }

    public ReportStyle setTextAlign(String textAlign) {
        this.textAlign = textAlign;
        return this;
    }

    public String getBkgColor() {
        return bkgColor;
    }

    public ReportStyle setBkgColor(String bkgColor) {
        this.bkgColor = bkgColor;
        return this;
    }

    public String getTextWeight() {
        return textWeight;
    }

    public ReportStyle setTextWeight(String textWeight) {
        this.textWeight = textWeight;
        return this;
    }

    public String getTextStyle() {
        return textStyle;
    }

    public ReportStyle setTextStyle(String textStyle) {
        this.textStyle = textStyle;
        return this;
    }

    public Boolean getNoWrap() {
        return noWrap!=null && noWrap;
    }

    public ReportStyle setNoWrap(Boolean noWrap) {
        this.noWrap = noWrap;
        return this;
    }

    public ReportStyleBorder getBorderTop() {
        return borderTop;
    }

    public ReportStyle setBorderTop(ReportStyleBorder borderTop) {
        this.borderTop = borderTop;
        return this;
    }

    public ReportStyle setBorderTop(String color, String style, Integer size) {
        this.borderTop = new ReportStyleBorder(color, style, size);
        return this;
    }

    public ReportStyleBorder getBorderBottom() {
        return borderBottom;
    }

    public ReportStyle setBorderBottom(ReportStyleBorder borderBottom) {
        this.borderBottom = borderBottom;
        return this;
    }

    public ReportStyle setBorderBottom(String color, String style, Integer size) {
        this.borderBottom = new ReportStyleBorder(color, style, size);
        return this;
    }

    public ReportStyleBorder getBorderLeft() {
        return borderLeft;
    }

    public ReportStyle setBorderLeft(ReportStyleBorder borderLeft) {
        this.borderLeft = borderLeft;
        return this;
    }

    public ReportStyle setBorderLeft(String color, String style, Integer size) {
        this.borderLeft = new ReportStyleBorder(color, style, size);
        return this;
    }

    public ReportStyleBorder getBorderRight() {
        return borderRight;
    }

    public ReportStyle setBorderRight(ReportStyleBorder borderRight) {
        this.borderRight = borderRight;
        return this;
    }

    public ReportStyle setBorderRight(String color, String style, Integer size) {
         this.borderRight = new ReportStyleBorder(color, style, size);
         return this;
     }


    public class ReportStyleBorder {
        private String color;
        private String style;
        private Integer size;

        public ReportStyleBorder() {
        }

        public ReportStyleBorder(String color, String style, Integer size) {
            this.color = color;
            this.style = style;
            this.size = size;
        }

        public String getColor() {
            return color;
        }

        public ReportStyleBorder setColor(String color) {
            this.color = color;
            return this;
        }

        public String getStyle() {
            return style;
        }

        public ReportStyleBorder setStyle(String style) {
            this.style = style;
            return this;
        }

        public Integer getSize() {
            return size;
        }

        public ReportStyleBorder setSize(Integer size) {
            this.size = size;
            return this;
        }

    }

}
