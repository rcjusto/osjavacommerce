package org.store.core.beans.utils;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.hibernate.EmptyInterceptor;
import org.hibernate.type.Type;
import org.store.core.beans.Product;
import org.store.core.beans.ProductLang;

import java.io.Serializable;

/**
 * Rogelio Caballero
 * 18/05/12 12:08
 */
public class ProductInterceptor extends EmptyInterceptor {
    
    private static final String[] PRODUCT_LANG_FIELDS = {"productName","description","features","information"};

    @Override
    public boolean onFlushDirty(Object entity, Serializable id, Object[] currentState, Object[] previousState, String[] propertyNames, Type[] types) {
        if (entity instanceof Product) {
            if (productChanged(currentState,previousState,propertyNames)) markToIndex(currentState, propertyNames);
        } else if (entity instanceof ProductLang) {
            if (fieldsChanged(currentState,previousState,propertyNames, PRODUCT_LANG_FIELDS)) markToIndex(currentState, propertyNames);
        }
        return true;
    }

    @Override
    public boolean onSave(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types) {
        if (entity instanceof Product) {
            markToIndex(state, propertyNames);
        } else if (entity instanceof ProductLang) {
            markToIndex(state, propertyNames);
        }
        return true;
    }
    
    private void markToIndex(Object[] state, String[] propertyNames) {
        int pos = ArrayUtils.indexOf(propertyNames, "indexed");
        if (pos>-1 && pos<state.length) state[pos] = Boolean.FALSE;
    }

    private boolean fieldsChanged(Object[] currentState, Object[] previousState, String[] propertyNames, String... fields) {
        for(String field : fields) {
            int pos = ArrayUtils.indexOf(propertyNames, field);
            if (pos>-1 && pos<currentState.length) {
                EqualsBuilder eq = new EqualsBuilder().append(currentState[pos],previousState[pos]);
                if (!eq.isEquals()) return true;
            } 
        }
        return  false;
    }

    private boolean productChanged(Object[] currentState, Object[] previousState, String[] propertyNames) {
        int pos = ArrayUtils.indexOf(propertyNames, "stock");
        if (pos>-1 && pos<currentState.length) {
            Long oldStock = (Long) previousState[pos]; if (oldStock==null) oldStock = 0l;
            Long newStock = (Long) currentState[pos]; if (newStock==null) newStock = 0l;
            if ((oldStock<1 && newStock>0) || (oldStock>0 && newStock<1)) return true;
        } 

        pos = ArrayUtils.indexOf(propertyNames, "eta");
        if (pos>-1 && pos<currentState.length) {
            String oldEta = (String) previousState[pos]; if (oldEta==null) oldEta = "";
            String newEta = (String) currentState[pos]; if (newEta==null) newEta = "";
            if (("".equals(oldEta.trim()) && !"".equals(newEta.trim())) || (!"".equals(oldEta.trim()) && "".equals(newEta.trim()))) return true;
        }

        return fieldsChanged(currentState,previousState,propertyNames, "active", "archived", "productType", "partNumber", "mfgPartnumber", "searchKeywords");
    }

}
