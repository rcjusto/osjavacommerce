package org.store.core.globals;

import org.store.core.beans.CategoryFee;
import org.store.core.beans.ShopCart;
import org.store.core.beans.ShopCartItem;
import org.store.core.beans.State;
import org.store.core.beans.User;
import org.store.core.dao.HibernateDAO;
import org.apache.commons.collections.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

public class FeesUtils {

    private Double total;

    public FeesUtils(ShopCart cart, State deliveryState, User user, BaseAction action) {
        calculate(cart, deliveryState, user, action.getDao());
    }

    public FeesUtils calculate(ShopCart cart, State deliveryState, User user, HibernateDAO dao) {
        total = 0.0d;
        if (cart != null && cart.getItems() != null) {
            for (ShopCartItem item : cart.getItems())
                total += calculateFees(item, deliveryState, user, dao);
        }
        return this;
    }

    public double calculateFees(ShopCartItem item, State deliveryState, User user, HibernateDAO dao) {
        double t = 0.0;
        item.getFees().clear();
        List<CategoryFee> lista = new ArrayList<CategoryFee>();

        List<CategoryFee> l1 = (item.getBeanProd1() != null) ? dao.getParentCategoryFee(item.getBeanProd1().getCategory(), deliveryState) : null;
        if (CollectionUtils.isEmpty(l1)) l1 = (item.getBeanProd1() != null && deliveryState!=null) ? dao.getParentCategoryFee(item.getBeanProd1().getCategory(), deliveryState.getCountryCode()) : null;
        if (CollectionUtils.isNotEmpty(l1)) lista.addAll(l1);

        List<CategoryFee> l2 = (item.getBeanProd2() != null) ? dao.getParentCategoryFee(item.getBeanProd2().getCategory(), deliveryState) : null;
        if (CollectionUtils.isEmpty(l2)) l2 = (item.getBeanProd2() != null && deliveryState!=null) ? dao.getParentCategoryFee(item.getBeanProd2().getCategory(), deliveryState.getCountryCode()) : null;
        if (CollectionUtils.isNotEmpty(l2)) lista.addAll(l2);

        if (lista.size() > 0) {
            for (CategoryFee f : lista)
                if (user == null || !user.hasFeeExemption(f.getFee().getId())) {
                    item.addFee(f);
                    t += f.getTotal(item) * item.getQuantity();
                }
        }
        return t;
    }

    public double getTotal() {
        return total!=null ? total : 0.0;
    }
}
