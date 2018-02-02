package org.store.core.utils.carriers.velocity;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * User: Rogelio Caballero Justo
 * Date: 27-dic-2006
 * Time: 18:06:52
 */
public class CarrierVelocityTool {

    private Locale locale;
    private DecimalFormatSymbols symbols;


    public DecimalFormatSymbols getSymbols() {
        return symbols;
    }

    public void setSymbols(DecimalFormatSymbols symbols) {
        this.symbols = symbols;
    }

    public Locale getLocale() {
        return locale;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    public CarrierVelocityTool() {
        locale = new Locale("en");
        symbols = new DecimalFormatSymbols(locale);
        symbols.setDecimalSeparator('.');
    }

    public String format(double d, String format) {
        DecimalFormat df = new DecimalFormat(format, symbols);
        return df.format(d);
    }
    public String format(float d, String format) {
        DecimalFormat df = new DecimalFormat(format, symbols);
        return df.format(d);
    }
    public String format(Double d, String format) {
        if (d==null) return "";
        DecimalFormat df = new DecimalFormat(format, symbols);
        return df.format(d.doubleValue());
    }
    public String format(Float d, String format) {
        if (d==null) return "";
        DecimalFormat df = new DecimalFormat(format, symbols);
        return df.format(d.floatValue());
    }

    public String roundToInt(Double d) {
        if(d!=null) return roundToInt(d.doubleValue());
        else return "";
    }

    public String roundToInt(double d) {
        long ri = Math.round(d);
        if (ri<1) ri=1;
        return String.valueOf(ri);
    }

    public String roundToInt(float d) {
        long ri = Math.round(d);
        if (ri<1) ri=1;
        return String.valueOf(ri);
    }

    public String roundToInt(Float d) {
        if(d!=null) return roundToInt(d.floatValue());
        else return "";
    }

    public String escapeXml(String cad) {
        if (cad == null)
        {
            return null;
        }
        return StringEscapeUtils.escapeXml(String.valueOf(cad));
    }

    public String replaceStr(String source, String c1, String c2) {
        return StringUtils.replace(source,c1,c2);
    }

}
