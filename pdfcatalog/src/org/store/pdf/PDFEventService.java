package org.store.pdf;

import org.store.core.beans.StoreProperty;
import org.store.core.dao.HibernateDAO;
import org.store.core.front.FrontModuleAction;
import org.store.core.globals.config.Store20Config;
import org.store.core.utils.PluginAdminMenu;
import org.store.core.utils.events.DefaultEventServiceImpl;
import org.hibernate.Session;

import javax.servlet.ServletContext;

/**
 * Rogelio Caballero
 * 14/12/11 10:41
 */
public class PDFEventService extends DefaultEventServiceImpl {

    private static final String PLUGIN_NAME = "pdfgenerator";
    private static final String PROP_HEADER_TEXT = "store.plugin.pdfgenerator.text";
    private static final String PROP_HEADER_TEXT_DEFAULT = "Tool to generate PDF Documents from Catalog.";
    private static final String MAIN_CONFIG_ACTION = "pdf_main_config";

    public String getName() {
        return PLUGIN_NAME;
    }

    public String getDescription(FrontModuleAction action) {
        return action.getText(PROP_HEADER_TEXT, PROP_HEADER_TEXT_DEFAULT);
    }

    public void initialize(ServletContext ctx, Session databaseSession, String store) {
        // generate menu item
        PluginAdminMenu menu = new PluginAdminMenu();
        menu.setMenuParent(PluginAdminMenu.PARENT_CATALOG);
        menu.setMenuLabel("menu.pdf.generator");
        menu.setMenuText("PDF Catalog Generator");
        menu.setMenuAction(MAIN_CONFIG_ACTION);
        Store20Config.getInstance(ctx).addPluginAdminMenu(menu);

        // generate default labels
        HibernateDAO dao = new HibernateDAO(databaseSession, store);
        String[] languages = dao.getStorePropertyValue(StoreProperty.PROP_LANGUAGES, StoreProperty.TYPE_GENERAL, "en").split(",");
        initializeLabel(dao, "custom.url", "Custom URL", languages);
        initializeLabel(dao, "admin.selected.products", "Selected Products", languages);
        initializeLabel(dao, "admin.generate.pdf", "Generate PDF", languages);
        initializeLabel(dao, "full.catalog", "Full Catalog", languages);
        initializeLabel(dao, "include.in.pdf", "Include in PDF", languages);
        initializeLabel(dao, "plugin.pdf.generator", "PDF Generator Plugin", languages);
        initializeLabel(dao, "pdf.name", "PDF filename", languages);
        initializeLabel(dao, "generate.new.pdf", "Generate New PDF", languages);
        initializeLabel(dao, "generated.pdfs", "Generated PDFs", languages);
        initializeLabel(dao, "pdf.layout", "Select PDF layout", languages);
        initializeLabel(dao, "pdf.layouts.not.found", "PDF layouts not found.", languages);
        initializeLabel(dao, "pdf.title", "Online Catalog", languages);
        initializeLabel(dao, "pdf.product.prices", "Calculate price for", languages);
        initializeLabel(dao, "pdf.select.products.to.pdf", "Select products to include in PDF", languages);
        initializeLabel(dao, "click.to.select.products", "Click here to select products", languages);
    }

}
