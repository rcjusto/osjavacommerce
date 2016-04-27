package org.store.core.beans;

import org.store.core.beans.utils.StoreBean;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.text.DecimalFormat;
import java.util.Date;

/**
 * Monedas
 */
@Entity
@Table(name = "t_currency")
public class Currency extends BaseBean implements StoreBean {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Codigo de la moneda
    @Column(length = 5)
    private String code;

    // Nombre de la modeda
    @Column(length = 50)
    private String symbol;

    // Tienda en que esta configurado
    @Column(length = 10)
    private String inventaryCode;

    // factor porque se multiplica
    private Double ratio;
    private Double reverseRatio;

    // fecha de la ultima actualizacion
    private Date lastUpdate;

    // activa
    private Boolean active;

    @Column(length = 50)
    private String format;
    private static final String DEFAULT_DECIMAL_FORMAT = "0.00";

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public Double getRatio() {
        return ratio!=null ? ratio : 1d;
    }

    public void setRatio(Double ratio) {
        this.ratio = ratio;
    }

    public Double getReverseRatio() {
        return reverseRatio!=null ? reverseRatio : 1;
    }

    public void setReverseRatio(Double reverseRatio) {
        this.reverseRatio = reverseRatio;
    }

    public Date getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(Date lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public String getInventaryCode() {
        return inventaryCode;
    }

    public void setInventaryCode(String inventaryCode) {
        this.inventaryCode = inventaryCode;
    }

    public Boolean getActive() {
        return (active != null) && active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String formatValue(Double val) {
        StringBuffer buff = new StringBuffer();
        if (val<0) buff.append("-");
        if (StringUtils.isNotEmpty(symbol)) buff.append(symbol);
        String formato = StringUtils.isNotEmpty(format) ? format : DEFAULT_DECIMAL_FORMAT;
        DecimalFormat df = new DecimalFormat(formato);
        buff.append(df.format(Math.abs(val)));
        return buff.toString();
    }

    public String toString() {
        return new ToStringBuilder(this)
                .append("code", getCode())
                .toString();
    }

    public boolean equals(Object other) {
        if ((this == other)) return true;
        if (!(other instanceof Currency)) return false;
        Currency castOther = (Currency) other;
        return new EqualsBuilder()
                .append(this.getCode(), castOther.getCode())
                .isEquals();
    }
}