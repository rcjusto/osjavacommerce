package org.store.sage;

import org.store.core.admin.AdminModuleAction;
import org.store.core.admin.CustomerAction;
import org.store.core.admin.OrderAction;
import org.store.core.beans.Job;
import org.store.core.beans.LocalizedText;
import org.store.core.beans.Order;
import org.store.core.beans.ShippingMethod;
import org.store.core.beans.StoreProperty;
import org.store.core.beans.User;
import org.store.core.dao.HibernateDAO;
import org.store.core.front.FrontModuleAction;
import org.store.core.globals.BaseAction;
import org.store.core.globals.SomeUtils;
import org.store.core.globals.config.Store20Config;
import org.store.core.utils.PluginAdminMenu;
import org.store.core.utils.events.DefaultEventServiceImpl;
import org.store.core.utils.events.EventService;
import org.store.core.utils.quartz.QuartzUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.log4j.Logger;
import org.hibernate.Session;

import javax.servlet.ServletContext;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class SageEventService extends DefaultEventServiceImpl {

    public static Logger log = Logger.getLogger(SageEventService.class);

    public static final String EVENT_SERVICE_NAME = "SAGE";

    public String getName() {
        return EVENT_SERVICE_NAME;
    }

    public String getDescription(FrontModuleAction action) {
        return EVENT_SERVICE_NAME;
    }

    public Boolean onExecuteEvent(ServletContext ctx, int eventType, FrontModuleAction action, Map<String, Object> map) {
        if (EventService.EVENT_APPROVE_ORDER == eventType && map!=null && map.containsKey("order")) {
            try {
                onOrderSaved(action, (Order) map.get("order"));
            } catch (Exception e) {
                log.error(e.getMessage(), e); 
            }
            return true;
        }
        return false;
    }

    public Boolean onExecuteAdminEvent(ServletContext ctx, int eventType, AdminModuleAction action, Map<String, Object> map) {
        try {

            // Exportar la orden cuando cambie el estado de la orden
            if (EventService.EVENT_ADMIN_CHANGE_ORDER_STATUS == eventType && action instanceof OrderAction) {
                onOrderSaved(action, ((OrderAction) action).getOrder());
                return true;
            }

            // Exportar los datos del usuario cuando se salve en el modulo de administracion
            if (EventService.EVENT_ADMIN_SAVE_USER == eventType && action instanceof CustomerAction) {
                onUserSaved(action, ((CustomerAction) action).getUser());
                return true;
            }


        } catch (Exception e) {
            log.error(e.getMessage(), e); 
        }
        return null;
    }

    private void onOrderSaved(BaseAction action, Order orderSaved) throws Exception {
        Job job = action.getDao().getJob(SageOrdersJob.JOB_NAME);
        if (job != null && job.getActive() && orderSaved != null) {
            String[] statuses = job.getJobProperty(SageOrdersJob.PROP_ORDER_STATUS_TO_EXPORT, SageOrdersJob.PROP_ORDER_STATUS_TO_EXPORT_DEFAULT).split(",");
            if (ArrayUtils.contains(statuses, orderSaved.getStatus().getStatusCode())) {
                SageOrdersTask ordersTask = new SageOrdersTask(action.getStoreCode(), job, action.getDatabaseConfig());
                ordersTask.setOnlyOrder(orderSaved.getIdOrder());
                ordersTask.start();
            }
        }
    }

    private void onUserSaved(AdminModuleAction action, User user) throws Exception {
        Job job = action.getDao().getJob(SageCustomersJob.JOB_NAME);
        if (job != null && job.getActive() && user != null) {
            if ("Y".equalsIgnoreCase(job.getJobProperty(SageCustomersJob.PROP_UPDATE_CLIENT_ADDRESSES, "Y"))
                    || "Y".equalsIgnoreCase(job.getJobProperty(SageCustomersJob.PROP_UPDATE_CLIENT_NAMES, "Y"))
                    || "Y".equalsIgnoreCase(job.getJobProperty(SageCustomersJob.PROP_UPDATE_CONTACT_INFO, "Y"))) {
                SageCustomersTask customersTask = new SageCustomersTask(action.getStoreCode(), job, action.getDatabaseConfig());
                customersTask.setOnlyExportUser(user.getIdUser());
                customersTask.start();
            }
        }
    }

    public String getConfigurationTemplate() {
        return "/WEB-INF/views/org/store/sage/config.vm";
    }

    public void loadConfigurationData(BaseAction action) {
        Map<Integer, String> sageCarriers = getSAGETransportList(action);
        if (sageCarriers!=null && !sageCarriers.isEmpty()) {
            action.addToStack("sageCarriers", sageCarriers);
            List<ShippingMethod> carriers = action.getDao().getShippingMethodList();
            if (carriers != null && !carriers.isEmpty()) action.addToStack("carriers", carriers);
            action.addToStack("pickInStore", action.getCanPickInStore());
        }
        Map config = getConfiguration(action);
        if (config != null) action.addToStack("config", config);
    }

    public void saveConfigurationData(BaseAction action) {
        QuartzUtils qu = new QuartzUtils(action.getServletContext(), action.getDao());

        String sageUrl = action.getRequest().getParameter("sageUrl");
        String sageUser = action.getRequest().getParameter("sageUser");
        String sagePassword = action.getRequest().getParameter("sagePassword");

        // Product
        Job jobProducts = action.getDao().getJob(SageProductsJob.JOB_NAME, true);
        jobProducts.setActive("Y".equalsIgnoreCase(action.getRequest().getParameter("jobProducts.active")));
        jobProducts.setBeginHour(action.getRequest().getParameter("jobProducts.startAt"));
        jobProducts.setIntervalNumber(SomeUtils.strToInteger(action.getRequest().getParameter("jobProducts.intervalNumber")));
        jobProducts.setIntervalUnit(action.getRequest().getParameter("jobProducts.intervalUnit"));

        jobProducts.setJobProperty("sage.url", sageUrl);
        jobProducts.setJobProperty("sage.user", sageUser);
        jobProducts.setJobProperty("sage.password", sagePassword);

        String[] arrPropsProduct = action.getRequest().getParameterValues("sageProductProperty");
        String[] arrValuesProduct = action.getRequest().getParameterValues("sageProductValue");
        if (arrPropsProduct != null && arrPropsProduct.length > 0 && ArrayUtils.isSameLength(arrPropsProduct, arrValuesProduct))
            for (int i = 0; i < arrPropsProduct.length; i++) jobProducts.setJobProperty(arrPropsProduct[i], arrValuesProduct[i]);
        action.getDao().save(jobProducts);
        try {
            qu.configureTask(action.getStoreCode(), jobProducts.getName(), action.getDatabaseConfig(), Store20Config.getInstance(action.getServletContext()));
        } catch (Exception e) {
            log.error(e.getMessage(), e); 
        }

        // Customers
        Job jobCustomers = action.getDao().getJob(SageCustomersJob.JOB_NAME, true);
        jobCustomers.setActive("Y".equalsIgnoreCase(action.getRequest().getParameter("jobCustomers.active")));
        jobCustomers.setBeginHour(action.getRequest().getParameter("jobCustomers.startAt"));
        jobCustomers.setIntervalNumber(SomeUtils.strToInteger(action.getRequest().getParameter("jobCustomers.intervalNumber")));
        jobCustomers.setIntervalUnit(action.getRequest().getParameter("jobCustomers.intervalUnit"));

        jobCustomers.setJobProperty("sage.url", sageUrl);
        jobCustomers.setJobProperty("sage.user", sageUser);
        jobCustomers.setJobProperty("sage.password", sagePassword);

        String[] arrPropsCustomer = action.getRequest().getParameterValues("sageCustomerProperty");
        String[] arrValuesCustomer = action.getRequest().getParameterValues("sageCustomerValue");
        if (arrPropsCustomer != null && arrPropsCustomer.length > 0 && ArrayUtils.isSameLength(arrPropsCustomer, arrValuesCustomer))
            for (int i = 0; i < arrPropsCustomer.length; i++) jobCustomers.setJobProperty(arrPropsCustomer[i], arrValuesCustomer[i]);
        action.getDao().save(jobCustomers);
        try {
            qu.configureTask(action.getStoreCode(), jobCustomers.getName(), action.getDatabaseConfig(), Store20Config.getInstance(action.getServletContext()));
        } catch (Exception e) {
            log.error(e.getMessage(), e); 
        }

        // Orders
        Job jobOrders = action.getDao().getJob(SageOrdersJob.JOB_NAME, true);
        jobOrders.setActive("Y".equalsIgnoreCase(action.getRequest().getParameter("jobOrders.active")));
        jobOrders.setBeginHour(action.getRequest().getParameter("jobOrders.startAt"));
        jobOrders.setIntervalNumber(SomeUtils.strToInteger(action.getRequest().getParameter("jobOrders.intervalNumber")));
        jobOrders.setIntervalUnit(action.getRequest().getParameter("jobOrders.intervalUnit"));

        jobOrders.setJobProperty("sage.url", sageUrl);
        jobOrders.setJobProperty("sage.user", sageUser);
        jobOrders.setJobProperty("sage.password", sagePassword);

        String[] arrStoreCarriers = action.getRequest().getParameterValues("storeCarriers");
        String[] arrSageCarriers = action.getRequest().getParameterValues("sageCarriers");
        if (arrStoreCarriers != null && arrStoreCarriers.length > 0 && ArrayUtils.isSameLength(arrStoreCarriers, arrSageCarriers))
            for (int i = 0; i < arrStoreCarriers.length; i++) jobOrders.setJobProperty("sage.order.carrier."+arrStoreCarriers[i], arrSageCarriers[i]);
        jobOrders.setJobProperty("sage.order.pick.in.store",action.getRequest().getParameter("sagePickInStore"));

        String[] arrPropsOrder = action.getRequest().getParameterValues("sageOrderProperty");
        String[] arrValuesOrder = action.getRequest().getParameterValues("sageOrderValue");
        if (arrPropsOrder != null && arrPropsOrder.length > 0 && ArrayUtils.isSameLength(arrPropsOrder, arrValuesOrder))
            for (int i = 0; i < arrPropsOrder.length; i++) jobOrders.setJobProperty(arrPropsOrder[i], arrValuesOrder[i]);

        action.getDao().save(jobOrders);
        try {
            qu.configureTask(action.getStoreCode(), jobOrders.getName(), action.getDatabaseConfig(), Store20Config.getInstance(action.getServletContext()));
        } catch (Exception e) {
            log.error(e.getMessage(), e); 
        }

        // Taxes
        Job jobTaxes = action.getDao().getJob(SageTaxesJob.JOB_NAME, true);
        jobTaxes.setActive("Y".equalsIgnoreCase(action.getRequest().getParameter("jobTaxes.active")));
        jobTaxes.setBeginHour(action.getRequest().getParameter("jobTaxes.startAt"));
        jobTaxes.setIntervalNumber(SomeUtils.strToInteger(action.getRequest().getParameter("jobTaxes.intervalNumber")));
        jobTaxes.setIntervalUnit(action.getRequest().getParameter("jobTaxes.intervalUnit"));

        jobTaxes.setJobProperty("sage.url", sageUrl);
        jobTaxes.setJobProperty("sage.user", sageUser);
        jobTaxes.setJobProperty("sage.password", sagePassword);

        String useTaxPerProduct = action.getRequest().getParameter(StoreProperty.PROP_USE_TAX_PER_PRODUCT);
        StoreProperty prop = action.getDao().getStoreProperty(StoreProperty.PROP_USE_TAX_PER_PRODUCT, StoreProperty.TYPE_GENERAL, true);
        prop.setValue(useTaxPerProduct);
        action.getDao().save(prop);

        String[] arrPropsTaxes = action.getRequest().getParameterValues("sageTaxesProperty");
        String[] arrValuesTaxes = action.getRequest().getParameterValues("sageTaxesValue");
        if (arrPropsTaxes != null && arrPropsTaxes.length > 0 && ArrayUtils.isSameLength(arrPropsTaxes, arrValuesTaxes))
            for (int i = 0; i < arrPropsTaxes.length; i++) jobTaxes.setJobProperty(arrPropsTaxes[i], arrValuesTaxes[i]);
        action.getDao().save(jobTaxes);
        try {
            qu.configureTask(action.getStoreCode(), jobTaxes.getName(), action.getDatabaseConfig(), Store20Config.getInstance(action.getServletContext()));
        } catch (Exception e) {
            log.error(e.getMessage(), e); 
        }

    }

    public void initialize(ServletContext ctx, Session databaseSession, String store) {
        // generate menu item
        PluginAdminMenu menu = new PluginAdminMenu();
        menu.setMenuParent(PluginAdminMenu.PARENT_CONFIGURATION);
        menu.setMenuLabel("menu.sage");
        menu.setMenuText("SAGE Configuration");
        menu.setMenuAction(PluginAdminMenu.EDIT_PROPERTIES_ACTION);
        menu.addActionParameter(PluginAdminMenu.EDIT_PROPERTIES_PARAM_NAME, getName());
        Store20Config.getInstance(ctx).addPluginAdminMenu(menu);

        HibernateDAO dao = new HibernateDAO(databaseSession, store);
        String[] languages = dao.getStorePropertyValue(StoreProperty.PROP_LANGUAGES, StoreProperty.TYPE_GENERAL, "en").split(",");
        LocalizedText label = initializeLabel(dao, "menu.sage", "SAGE Configuration", languages);
        label.addValue("es", "Configuracion de SAGE");
        dao.save(label);
        initializeLabel(dao, "admin.task.complete", "Tarea Completada", languages);
        initializeLabel(dao, "admin.click.here.to refresh.status", "Haga click aqui para actualizar el estado de la tarea", languages);
    }

    private Map<String, Object> getConfiguration(BaseAction action) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("products", action.getDao().getJob(SageProductsJob.JOB_NAME));
        map.put("customers", action.getDao().getJob(SageCustomersJob.JOB_NAME));
        map.put("orders", action.getDao().getJob(SageOrdersJob.JOB_NAME));
        map.put("taxes", action.getDao().getJob(SageTaxesJob.JOB_NAME));
        QuartzUtils qu = new QuartzUtils(action.getServletContext(), action.getDao());
        try {
            map.put("trigger_products", qu.getJobTrigger(action.getStoreCode(), SageProductsJob.JOB_NAME));
            map.put("trigger_customers", qu.getJobTrigger(action.getStoreCode(), SageCustomersJob.JOB_NAME));
            map.put("trigger_orders", qu.getJobTrigger(action.getStoreCode(), SageOrdersJob.JOB_NAME));
            map.put("trigger_taxes", qu.getJobTrigger(action.getStoreCode(), SageTaxesJob.JOB_NAME));
        } catch (Exception e) {
            log.error(e.getMessage(), e); 
        }

        return map;
    }

    private Map<Integer, String> getSAGETransportList(BaseAction action) {
        Map<Integer, String> result = new HashMap<Integer, String>();
        Job job = action.getDao().getJob(SageOrdersJob.JOB_NAME);
        if (job != null) {
            try {
                SageOrdersTask task = new SageOrdersTask(action.getStoreCode(), job, action.getDatabaseConfig());
                Connection conn = task.getConnection();
                if (conn != null)
                    try {
                        PreparedStatement stmt = conn.prepareStatement("select cbMarq, E_Intitule from P_EXPEDITION where E_Intitule is not null and E_Intitule<>''");
                        ResultSet rs = stmt.executeQuery();
                        while(rs.next()) {
                            result.put(rs.getInt("cbMarq"), rs.getString("E_Intitule"));
                        }
                        rs.close();
                    } catch (SQLException e) {
                        log.error(e.getMessage(), e); 
                    } finally {
                        conn.close();
                    }
            } catch (Exception e) {
                log.error(e.getMessage(), e); 
            }
        }
        return (!result.isEmpty()) ? result : null;
    }

}
