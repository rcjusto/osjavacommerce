package org.store.registration.front;

import org.hibernate.Session;
import org.store.core.dao.HibernateDAO;
import org.store.core.globals.config.Store20Config;
import org.store.core.utils.PluginAdminMenu;
import org.store.core.utils.events.DefaultEventServiceImpl;
import org.store.registration.beans.ProductRegistration;

import javax.servlet.ServletContext;

public class ProductRegistrationEventService extends DefaultEventServiceImpl {

    private static final String PLUGIN_NAME = "product_registration";
    @Override
    public String getName() {
        return PLUGIN_NAME;
    }

    @Override
    public void initialize(ServletContext ctx, Session databaseSession, String store) {
        Store20Config config = Store20Config.getInstance(ctx);
        PluginAdminMenu menu0 = new PluginAdminMenu();
        menu0.setMenuParent(PluginAdminMenu.PARENT_CUSTOMERS);
        menu0.setMenuLabel("admin.menu.product.registration");
        menu0.setMenuText("Product Registration");
        menu0.setMenuAction("product_registrations");
        config.addPluginAdminMenu(menu0);

        HibernateDAO dao = new HibernateDAO(databaseSession, store);
        String[] langArr = dao.getLanguages();
        initializeLabel(dao, "admin.menu.product.registration", "Product Registration", langArr);
        initializeLabel(dao, "admin.purchase.place", "Purchase Place", langArr);
        initializeLabel(dao, "admin.model.number", "Model Number", langArr);
        initializeLabel(dao, "admin.invoice.number", "Invoice Number", langArr);
        initializeLabel(dao, "registration.purchaseCountry", "Country", langArr);
        initializeLabel(dao, "registration.purchaseCity", "City", langArr);
        initializeLabel(dao, "registration.purchasePlace", "Purchase Place", langArr);
        initializeLabel(dao, "registration.purchaseDate", "Purchase Date", langArr);
        initializeLabel(dao, "registration.invoiceNumber", "Invoice Number", langArr);
        initializeLabel(dao, "registration.modelNumber", "Model Number", langArr);
        initializeLabel(dao, "registration.details", "Details", langArr);
        initializeLabel(dao, "product.registration", "Product Registration", langArr);
        initializeLabel(dao, "product.registration.enter.data", "Please fill the following form.", langArr);
        initializeLabel(dao, "product.registration.new", "New Product Registration", langArr);
        initializeLabel(dao, "my.product.registrations", "My Registered Products", langArr);
        initializeLabel(dao, "registration.extra.modelNumber", "Model Number", langArr);
    }

    @Override
    public void initializePlugin(ServletContext ctx) {
        Store20Config config = Store20Config.getInstance(ctx);
        config.registerBean(ProductRegistration.class);

    }
}
