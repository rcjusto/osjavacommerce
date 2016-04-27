package org.store.core.globals;

import org.store.core.beans.Insurance;
import org.store.core.beans.PromotionalCode;
import org.store.core.beans.ShippingMethod;
import org.store.core.beans.ShopCart;
import org.store.core.beans.State;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ShopCartUtils {

    public static Logger log = Logger.getLogger(ShopCartUtils.class);
    private double subtotalProductCost;
    private double subtotalProduct;
    private double subtotalComponent;
    private double subtotalFees;
    private double subtotalPromotions;
    private double subtotalShipping;
    private double subtotalInterest;
    private double total;
    private double totalWithoutInterest;
    private double percentInterest;
    private List<Map<String, Object>> promotions;
    List<Map<String, Object>> shippingServices;
    private Insurance insurance;
    private boolean freeShipping;
    private boolean shippingNeeded;
    private boolean shippingNeedSelection;
    private ShippingMethod shippingMethod;
    private List<String> shippingErrors;
    private List<Map<String, Object>> taxes;
    private double rewardsToUse;
    private int rewardsUsedPoints;
    private int rewardsRestPoints;
    private String pendingForPay;

    public ShopCartUtils(BaseAction action, ShopCart cart, List<PromotionalCode> promotionalCodes, boolean getLiveMethods) {
        if (cart!=null) cart.initializeItems(action);
        calculatePrices(action, cart, promotionalCodes, getLiveMethods);
    }

    private void calculatePrices(BaseAction action, ShopCart cart, List<PromotionalCode> promotionalCodes, boolean getLiveMethods) {
        try {
            State deliveryState = (cart != null && cart.getDeliveryAddress() != null) ? cart.getDeliveryAddress().getState() : null;

            total = cart.getTotal();

            total += subtotalComponent = cart.getTotalComplements();
            subtotalProduct = total;

            // interest
            totalWithoutInterest = total;
            percentInterest = subtotalInterest = 0;
            if (cart.getInterestPercent()!=null && cart.getInterestPercent()>0) {
                percentInterest = cart.getInterestPercent();
                subtotalInterest = total * cart.getInterestPercent() / 100;
                total += subtotalInterest;
            }

            // Fees
            FeesUtils feeUtils = new FeesUtils(cart, deliveryState, cart.getUser(), action);
            total += subtotalFees = feeUtils.getTotal();

            PromotionUtils promUtils = new PromotionUtils(promotionalCodes, cart, action, total);
            subtotalPromotions = promUtils.getTotal();
            total += subtotalPromotions;
            promotions = promUtils.getData();
            freeShipping = promUtils.isFreeShipping();

            subtotalProductCost = cart.getTotalCost() + promUtils.getFreeProductCost();

            List<Insurance> insurances = action.getDao().getInsurancesFor(total);
            double totalInsurance = 0.0;
            insurance = (Insurance) action.getDao().get(Insurance.class, cart.getInsurance());
            if (insurance != null && insurances.contains(insurance)) {
                totalInsurance = insurance.getInsuranceValue();
                total += totalInsurance;
            }

            ShippingUtils shipUtils = new ShippingUtils(cart, deliveryState, action, cart.getShippingMethod(), freeShipping, getLiveMethods);
            shippingNeeded = shipUtils.getOrderNeedShipping();
            shippingNeedSelection = shipUtils.getNeedToSelectShipping();
            if (shipUtils.getSelected() != null) {
                subtotalShipping = shipUtils.getTotal();
                shippingMethod = shipUtils.getSelected();
                total += subtotalShipping;
            }
            shippingServices = shipUtils.getShippingRates();
            if (shipUtils.getErrors() != null && !shipUtils.getErrors().isEmpty())
                shippingErrors = shipUtils.getErrors();

            TaxesUtils taxUtils = new TaxesUtils(cart, deliveryState, cart.getUser(), action, cart.getTotal() + feeUtils.getTotal() + promUtils.getTotal() + totalInsurance, shipUtils.getTotal());
            total += taxUtils.getTotal();
            taxes = taxUtils.getData();

            // rewards
            Boolean rewardsEnabled = (Boolean) action.getRequest().getAttribute("rewardsEnabled");
            if (rewardsEnabled != null && rewardsEnabled && cart.getUseRewards() && cart.getUser() != null && cart.getUser().getRewardPoints() > 0) {
                Number rewardsRate = (Number) action.getRequest().getAttribute("rewardsRate");
                if (rewardsRate != null && rewardsRate.doubleValue() > 0) {
                    Double rewardsMoney = (rewardsRate != null) ? cart.getUser().getRewardPoints() * rewardsRate.doubleValue() : null;
                    if (rewardsMoney != null && rewardsMoney > 0) {
                        rewardsToUse = Math.min(total, rewardsMoney);
                        rewardsUsedPoints = (int) Math.min(Math.ceil(rewardsToUse / rewardsRate.doubleValue()), cart.getUser().getRewardPoints());
                        total -= rewardsToUse;
                        rewardsRestPoints = (int) (cart.getUser().getRewardPoints() - rewardsUsedPoints);
                    }
                }
            }

            // verify it is ready to pay
            pendingForPay = "";
            if (cart.getItems()==null || cart.getItems().isEmpty()) pendingForPay = action.getText(StoreMessages.CNT_MSG_SELECT_BILLING_ADDRESS, StoreMessages.CNT_DEFAULT_MSG_SELECT_BILLING_ADDRESS);
            if (StringUtils.isEmpty(pendingForPay) && cart.getBillingAddress()==null) pendingForPay = action.getText(StoreMessages.CNT_MSG_SELECT_BILLING_ADDRESS, StoreMessages.CNT_DEFAULT_MSG_SELECT_BILLING_ADDRESS);
            if (StringUtils.isEmpty(pendingForPay) && shippingNeeded && (cart.getDeliveryAddress()==null && cart.getPickInStore()==null)) pendingForPay = action.getText(StoreMessages.CNT_MSG_SELECT_SHIPPING_ADDRESS, StoreMessages.CNT_DEFAULT_MSG_SELECT_SHIPPING_ADDRESS);
            if (StringUtils.isEmpty(pendingForPay) && shippingNeeded && (cart.getShippingMethod()==null && cart.getPickInStore()==null)) pendingForPay = action.getText(StoreMessages.CNT_MSG_SELECT_SHIPPING_METHOD, StoreMessages.CNT_DEFAULT_MSG_SELECT_SHIPPING_METHOD);

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

    }

    public void removeSessionData(BaseAction action) {
        if (action.getStoreSessionObjects().containsKey(ShippingUtils.SESSION_RATE_SERVICES))
            action.getStoreSessionObjects().remove(ShippingUtils.SESSION_RATE_SERVICES);
    }

    public double getSubtotalProduct() {
        return subtotalProduct;
    }

    public double getSubtotalPromotions() {
        return subtotalPromotions;
    }

    public double getSubtotalShipping() {
        return subtotalShipping;
    }

    public double getTotal() {
        return total;
    }

    public double getSubtotalProductCost() {
        return subtotalProductCost;
    }

    public void setSubtotalProductCost(double subtotalProductCost) {
        this.subtotalProductCost = subtotalProductCost;
    }

    public double getSubtotalComponent() {
        return subtotalComponent;
    }

    public double getSubtotalFees() {
        return subtotalFees;
    }

    public double getSubtotalInterest() {
        return subtotalInterest;
    }

    public double getTotalWithoutInterest() {
        return totalWithoutInterest;
    }

    public double getPercentInterest() {
        return percentInterest;
    }

    public List<Map<String, Object>> getPromotions() {
        return promotions;
    }

    public List<Map<String, Object>> getShippingServices() {
        return shippingServices;
    }

    public Insurance getInsurance() {
        return insurance;
    }

    public boolean isShippingNeeded() {
        return shippingNeeded;
    }

    public boolean isShippingNeedSelection() {
        return shippingNeedSelection;
    }

    public ShippingMethod getShippingMethod() {
        return shippingMethod;
    }

    public List<String> getShippingErrors() {
        return shippingErrors;
    }

    public List<Map<String, Object>> getTaxes() {
        return taxes;
    }

    public double getRewardsToUse() {
        return rewardsToUse;
    }

    public int getRewardsUsedPoints() {
        return rewardsUsedPoints;
    }

    public int getRewardsRestPoints() {
        return rewardsRestPoints;
    }

    public boolean isFreeShipping() {
        return freeShipping;
    }

    public void setFreeShipping(boolean freeShipping) {
        this.freeShipping = freeShipping;
    }

    public String getPendingForPay() {
        return pendingForPay;
    }

    public void setPendingForPay(String pendingForPay) {
        this.pendingForPay = pendingForPay;
    }

    public List<Map<String, Object>> getPromotionsByType(String type) {
        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
        if (promotions!=null && !promotions.isEmpty()) {
            for(Map<String, Object> map : promotions) {
                if (type.equals(map.get("type"))) result.add(map);
            }
        }
        return result;
    }

    public Map<String, Object> getFirstPromotionByType(String type) {
        List<Map<String, Object>> list = getPromotionsByType(type);
        return (list!=null && !list.isEmpty()) ? list.get(0) : null;
    }

}
