package org.store.core.globals;

import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.AbstractInterceptor;
import com.opensymphony.xwork2.util.logging.Logger;
import com.opensymphony.xwork2.util.logging.LoggerFactory;
import org.store.core.globals.config.Store20Commerce;
import org.store.core.globals.config.Store20Config;
import org.apache.struts2.ServletActionContext;

import java.util.Map;

/**
 * Detecta a que comercio esta dirigida la request
 * Rogelio Caballero
 * 15/06/11 23:42
 */
public class StoreDetectInterceptor extends AbstractInterceptor {

    private static final Logger LOG = LoggerFactory.getLogger(StoreDetectInterceptor.class);
    public static final String RESULT_NOT_STORE = "notStore";

    @Override
    public String intercept(ActionInvocation invocation) throws Exception {

        if (invocation.getAction() instanceof BaseAction) {
            BaseAction action = (BaseAction) invocation.getAction();
            StoreActionMapping mapping = (StoreActionMapping) invocation.getInvocationContext().get(ServletActionContext.ACTION_MAPPING);
            String storeCode = action.getStoreCodeByMapping(mapping.getDomain(), mapping.getStore());

            LOG.debug("ACTION: " + invocation.getInvocationContext().getName());
            // Verificar que es un store configurado
            Map stores = Store20Config.getInstance(action.getServletContext()).getStoreMap();
            if (stores != null && stores.keySet().contains(storeCode)) {
                LOG.debug("Store found: " + storeCode);
            } else {
                LOG.warn("Store not found: " + storeCode);
                if (stores != null && !stores.isEmpty()) action.addToStack("stores", stores);
                else LOG.error("Stores not configured");
                return RESULT_NOT_STORE;
            }

            action.setStoreCode(storeCode);
            action.getDao().setStoreCode(storeCode);

            Store20Commerce commerce = Store20Config.getInstance(action.getServletContext()).getStoreMap().get(storeCode);
            action.setCommerceConfig(commerce);
            action.setDatabaseConfig(Store20Config.getInstance(action.getServletContext()).getStoreDb().get(commerce.getDatabase()));

        }
        return invocation.invoke();
    }

}
