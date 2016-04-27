package org.store.core.utils.merchants.partialpayments;

import org.store.core.globals.BaseAction;
import org.apache.commons.lang.StringUtils;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * Rogelio Caballero
 */
public class PartialPaymentConfig {

    private String name;
    private Double initialPercent;
    private Double partialPercent;
    private Double interestPercent;
    private Integer frequencyDays;
    private Integer partialPayments;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getInitialPercent() {
        return initialPercent;
    }

    public void setInitialPercent(Double initialPercent) {
        this.initialPercent = initialPercent;
    }

    public Double getPartialPercent() {
        return partialPercent;
    }

    public void setPartialPercent(Double partialPercent) {
        this.partialPercent = partialPercent;
    }

    public Integer getFrequencyDays() {
        return frequencyDays;
    }

    public void setFrequencyDays(Integer frequencyDays) {
        this.frequencyDays = frequencyDays;
    }

    public Integer getPartialPayments() {
        return partialPayments;
    }

    public void setPartialPayments(Integer partialPayments) {
        this.partialPayments = partialPayments;
    }

    public Double getInterestPercent() {
        return interestPercent;
    }

    public void setInterestPercent(Double interestPercent) {
        this.interestPercent = interestPercent;
    }

    public Integer getTotalDays() {
        return (partialPayments != null && frequencyDays != null) ? partialPayments * frequencyDays : null;
    }

    @Override
    public String toString() {
        StringBuffer buff = new StringBuffer();
        if (initialPercent != null) buff.append(initialPercent);
        buff.append("_");
        if (partialPercent != null) buff.append(partialPercent);
        buff.append("_");
        if (frequencyDays != null) buff.append(frequencyDays);
        buff.append("_");
        if (partialPayments != null) buff.append(partialPayments);
        return StringUtils.replace(buff.toString(),".","-");
    }

    public Double getFirstPayment(Double total) {
        return (initialPercent!=null) ? initialPercent * total / 100 : 0d;
    }

    public Double getPartialPayment(Double total) {
        return (partialPercent!=null) ? partialPercent * total / 100 : 0d;
    }

    public boolean hasInitialPayment() {
        return initialPercent!=null && initialPercent>0;
    }

    public boolean hasPartialPayment() {
        return partialPercent!=null && partialPercent>0 && partialPayments!=null && partialPayments>0;
    }

    public String getDescription(BaseAction action, Double cant) {
        DecimalFormatSymbols dfs = new DecimalFormatSymbols(new Locale("en"));
        DecimalFormat df = new DecimalFormat("0.00", dfs);
        StringBuffer buff = new StringBuffer();
        if (cant != null) {
            if (initialPercent != null) {
                buff.append(action.getText("partial.payments.description.initial", "1 initial payment of {0}", new String[]{df.format(initialPercent * cant / 100)}));
            }
            if (partialPercent!=null && partialPayments!=null && partialPayments>0 && partialPercent>0) {
                if (StringUtils.isNotEmpty(buff.toString())) buff.append(action.getText("partial.payments.description.separator","<br/>"));
                buff.append(action.getText("partial.payments.description.partial","{1} payments of {0} every {2} days", new String[]{df.format(partialPercent * cant / 100), partialPayments.toString(), frequencyDays!=null ? frequencyDays.toString() : ""}));
            }
        }
        return buff.toString();
    }
}
