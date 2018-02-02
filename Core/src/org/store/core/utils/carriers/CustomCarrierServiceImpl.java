package org.store.core.utils.carriers;

import org.store.core.beans.CustomShippingMethod;
import org.store.core.beans.CustomShippingMethodRule;
import org.store.core.beans.State;
import org.store.core.globals.BaseAction;
import org.apache.struts2.ServletActionContext;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;


public class CustomCarrierServiceImpl implements CarrierService {

    private BaseAction action;

    public CustomCarrierServiceImpl() {
        if (ServletActionContext.getContext() != null && ServletActionContext.getContext().getActionInvocation() != null && ServletActionContext.getContext().getActionInvocation().getAction() != null)
            action = (ServletActionContext.getContext().getActionInvocation().getAction() instanceof BaseAction) ? (BaseAction) ServletActionContext.getContext().getActionInvocation().getAction() : null;
    }

    public RateServiceResponse getRateServices(ShipTo shipTo, List<BasePackage> packages) {
        if (action==null) return null;
        DecimalFormatSymbols dfs = new DecimalFormatSymbols(new Locale("en"));
        DecimalFormat df = new DecimalFormat("0.00", dfs);
        RateServiceResponse rss = new RateServiceResponse();
        double totalCost = 0;
        double totalWeight = 0;
        String country = shipTo.getAddress().getCountryCode();
        State state = action.getDao().getStateByCode(country, shipTo.getAddress().getStateProvinceCode());
        if (packages != null && packages.size() > 0) {
            for (BasePackage p : packages) {
                totalWeight += p.getPackageWeight();
                totalCost += p.getInsuredValueMonetaryValue();
            }
        }
        String currencyCode = action.getDefaultCurrency().getCode();

        List<CustomShippingMethod> l = action.getDao().getCustomShippingMethods();
        if (l != null && l.size() > 0) {
            for (CustomShippingMethod m : l) {
                Map<String, Object> map = getRate(m, country, state, totalCost, totalWeight);
                if (map != null) {
                    Double rate = (Double) map.get("rate");
                    Integer days = (Integer) map.get("days");
                    if (rate != null) {
                        RateService rs = new RateService(m.getCode(), currencyCode, df.format(rate), (days != null) ? days.toString() : null);
                        rss.addRateService(rs);
                    }
                }
            }
        }
        return rss;
    }

    private Map<String, Object> getRate(CustomShippingMethod m, String country, State state, double totalCost, double totalWeight) {
        if (m != null) {
            // Find rule
            List<CustomShippingMethodRule> rules = m.getRules();
            if (rules != null && rules.size() > 0) {
                for (CustomShippingMethodRule rule : rules) {
                    if (rule.canApply(country, state, totalCost, totalWeight)) {
                        if (rule.getRulePrice() != null) {
                            Map<String, Object> map = new HashMap<String, Object>();
                            map.put("rate", (rule.getPerWeightUnit()) ? totalWeight * rule.getRulePrice() : rule.getRulePrice());
                            map.put("days", (rule.getDays() != null) ? rule.getDays() : m.getDays());
                            return map;
                        } else if (rule.getRulePercent() != null) {
                            Map<String, Object> map = new HashMap<String, Object>();
                            map.put("rate", (rule.getPerWeightUnit()) ? totalWeight * rule.getRulePercent() * totalCost : rule.getRulePercent() * totalCost);
                            map.put("days", (rule.getDays() != null) ? rule.getDays() : m.getDays());
                            return map;
                        }
                    }
                }
            }
        }
        return null;
    }

    public RateServiceResponse getRateServices(ShipTo shipTo, Shipper shipFrom, List<BasePackage> packages) {
        return getRateServices(shipTo, packages);
    }

    public ShippingResult generateShipping(ShipTo shipTo, List<BasePackage> packages, String shippingMethod, String imgPath) {
        return null;
    }

    public ShippingResult generateShipping(ShipTo shipTo, Shipper shipFrom, List<BasePackage> packages, String shippingMethod, String imgPath, Date shipDate) {
        return null;
    }

    public VoidResponse makeVoid(String trackingNumber, String shippingMethod) {
        return null;
    }

    public String getHTMLTracking(String trackingNumber, String shipMethod) {
        return null;
    }

    public boolean available(ShipTo shipTo, List<BasePackage> packages) {
        return true;
    }

    public List<CarrierPropertyGroup> getPropertyNames() {
        return null;
    }

    public List<CarrierMethod> getShippingMethods() {
        if (action==null) return null;
        List<CustomShippingMethod> l = action.getDao().getCustomShippingMethods();
        if (l != null && l.size() > 0) {
            List<CarrierMethod> result = new ArrayList<CarrierMethod>();
            for (CustomShippingMethod m : l)
                result.add(new CarrierMethod(m.getCode(), m.getName(action.getDefaultLanguage())));
            return result;
        }

        return null;
    }

    public void setProperties(Properties p) {

    }

    public String getName() {
        return "CUSTOM";
    }
}
