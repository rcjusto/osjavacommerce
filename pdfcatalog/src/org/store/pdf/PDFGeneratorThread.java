package org.store.pdf;

import org.store.core.beans.Currency;
import org.store.core.beans.Product;
import org.store.core.beans.StoreProperty;
import org.store.core.beans.UserLevel;
import org.store.core.beans.utils.DataNavigator;
import org.store.core.beans.utils.ProductFilter;
import org.store.core.dao.HibernateDAO;
import org.store.core.globals.BaseAction;
import org.store.core.globals.ImageResolver;
import org.store.core.globals.ImageResolverImpl;
import org.store.core.globals.StoreThread;
import org.store.core.globals.config.Store20Database;
import org.store.core.hibernate.HibernateSessionFactory;
import org.store.core.utils.velocity.StoreVelocityGenerator;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.velocity.VelocityContext;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Rogelio Caballero
 * 15/12/11 23:28
 */
public class PDFGeneratorThread extends StoreThread {

    private String skin;
    private String storeCode;
    private Store20Database database;
    private ImageResolver imageResolver;
    private Long idLevel;
    private String mode;
    private String layoutView;
    private String pdfPath;
    private String appPath;
    private String filename;
    private Long idCategory;
    private List<Long> productIds;
    private Currency currency;


    public PDFGeneratorThread(BaseAction action, Long idLevel, String mode, String layout, String filename) {
        this.skin = action.getSkin();
        this.storeCode = action.getStoreCode();
        this.database = action.getDatabaseConfig();
        this.imageResolver = action.getImageResolver();
        this.idLevel = idLevel;
        this.mode = mode;
        this.appPath = action.getServletContext().getRealPath("/");
        this.pdfPath = action.getServletContext().getRealPath("/stores/" + action.getStoreCode() + "/pdf/");
        this.layoutView = layout;
        this.filename = filename;
        this.currency = action.getDefaultCurrency();
    }

    public void setIdCategory(Long idCategory) {
        this.idCategory = idCategory;
    }

    public void setProductIds(List<Long> productIds) {
        this.productIds = productIds;
    }

    @Override
    public void run() {
        try {
            Session session = HibernateSessionFactory.getSession(database);
            Transaction tx = session.beginTransaction();
            try {
                HibernateDAO dao = new HibernateDAO(session, storeCode);
                setExecutionMessage("Searching products...");
                setExecutionPercent(0d);

                String lang = dao.getDefaultLanguage();

                List<PDFProduct> productList = getProducts(dao, lang);
                if (productList != null && !productList.isEmpty()) {
                    String urlSite = dao.getStorePropertyValue(StoreProperty.PROP_SITE_HOST, StoreProperty.TYPE_GENERAL, "");
                    if (urlSite.endsWith("/")) urlSite = urlSite.substring(0, urlSite.length() - 1);

                    File htmlFile = generateHtml(productList, urlSite, lang, dao);
                    if (htmlFile != null) {
                        generatePDF(htmlFile);
                    }
                }

                setExecutionPercent(100d);
                setExecutionMessage("Complete");
                tx.commit();
            } catch (Exception e) {
                tx.rollback();
                log.error(e.getMessage(), e);
            } finally {
                if (session.isOpen()) session.close();
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    private void generatePDF(File htmlFile) {
        setExecutionPercent(95d);
        setExecutionMessage("Generating PDF...");
        try {
            String session = filename;
            if (StringUtils.isEmpty(session)) session = String.valueOf(Calendar.getInstance().getTimeInMillis());
            String path = FilenameUtils.getFullPath(htmlFile.getAbsolutePath()) + session + ".pdf";
            String htmlText = FileUtils.readFileToString(htmlFile, "UTF-8");
            OutputStream os = new FileOutputStream(path);
            ITextRenderer renderer = new ITextRenderer();
            renderer.setDocumentFromString(htmlText);
            renderer.layout();
            renderer.createPDF(os);
            os.close();
            htmlFile.delete();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    private File generateHtml(List<PDFProduct> productList, String urlSite, String lang, HibernateDAO dao) {
        setExecutionPercent(92d);
        setExecutionMessage("Generating structure...");

        File f = null;
        String session = String.valueOf(Calendar.getInstance().getTimeInMillis());
        try {
            FileUtils.forceMkdir(new File(pdfPath));
        } catch (IOException ignored) {
        }
        if (!pdfPath.endsWith(File.separator)) pdfPath += File.separator;

        // generar html
        if (productList != null && !productList.isEmpty()) {
            VelocityContext map = StoreVelocityGenerator.createContext();
            map.put("productList", productList);
            map.put("urlSite", urlSite);
            map.put("storeCode", storeCode);
            map.put("skin", skin);
            map.put("urlSite", urlSite);
            map.put("currency", currency);
            map.put("lang", lang);
            map.put("dao", dao);
            map.put("application-path", appPath);

            f = new File(pdfPath + session + ".html");
            if (f.exists()) f.delete();
            StoreVelocityGenerator.generateFile(map, layoutView, f);
        }
        return (f != null && f.exists()) ? f : null;
    }

    private List<PDFProduct> getProducts(HibernateDAO dao, String lang) {
        List<PDFProduct> productList = new ArrayList<PDFProduct>();

        UserLevel userLevel = (UserLevel) dao.get(UserLevel.class, idLevel);
        if (userLevel == null) userLevel = getDefaultLevel(dao);

        if ("all".equalsIgnoreCase(mode)) {
            ProductFilter pf = new ProductFilter();
            DataNavigator nav = new DataNavigator(null);
            nav.setPageRows(2024);
            List<Product> list = dao.listFrontProducts(nav, pf, null, lang, null);
            if (list != null && !list.isEmpty()) {
                int index = 1, total = list.size();
                for (Product product : list) {
                    setExecutionPercent(90d*index++/total);
                    setExecutionMessage("Reading product " + String.valueOf(index) + " of " + String.valueOf(total));
                    PDFProduct pdfP = new PDFProduct(product, userLevel, lang, dao);
                    pdfP.setImageSmall(imageResolver.getImageForProduct(product, ImageResolverImpl.PATH_LIST));
                    pdfP.setImageNormal(imageResolver.getImageForProduct(product, ""));
                    productList.add(pdfP);
                    dao.evict(product);
                }
            }
        } else if ("category".equalsIgnoreCase(mode)) {
            ProductFilter pf = new ProductFilter();
            if (idCategory != null) {
                pf.setFilterCategories(idCategory.toString());
            }
            DataNavigator nav = new DataNavigator(null);
            nav.setPageRows(2024);
            List<Product> list = dao.listFrontProducts(nav, pf, null, dao.getDefaultLanguage(), null);
            if (list != null && !list.isEmpty()) {
                int index = 1, total = list.size();
                for (Product product : list) {
                    setExecutionPercent(90d*index++/total);
                    setExecutionMessage("Reading product " + String.valueOf(index) + " of " + String.valueOf(total));
                    PDFProduct pdfP = new PDFProduct(product, userLevel, lang, dao);
                    pdfP.setImageSmall(imageResolver.getImageForProduct(product, ImageResolverImpl.PATH_LIST));
                    pdfP.setImageNormal(imageResolver.getImageForProduct(product, ""));
                    productList.add(pdfP);
                    dao.evict(product);
                }
            }
        } else if ("products".equalsIgnoreCase(mode)) {
            if (productIds != null && !productIds.isEmpty()) {
                int index = 1, total = productIds.size();
                for (Long id : productIds) {
                    Product product = (Product) dao.get(Product.class, id);
                    setExecutionPercent(90d*index++/total);
                    setExecutionMessage("Reading product " + String.valueOf(index) + " of " + String.valueOf(total));
                    if (product != null) {
                        PDFProduct pdfP = new PDFProduct(product, userLevel, lang, dao);
                        pdfP.setImageSmall(imageResolver.getImageForProduct(product, ImageResolverImpl.PATH_LIST));
                        pdfP.setImageNormal(imageResolver.getImageForProduct(product, ""));
                        productList.add(pdfP);
                        dao.evict(product);
                    }
                }
            }
        }
        return productList;
    }

    public UserLevel getDefaultLevel(HibernateDAO dao) {
        String defUsrLevelCode = dao.getStorePropertyValue(StoreProperty.PROP_DEFAULT_USER_LEVEL, StoreProperty.TYPE_GENERAL, null);
        UserLevel l = dao.getUserLevel(defUsrLevelCode);
        if (l == null) l = dao.getUserLevel(UserLevel.DEFAULT_LEVEL);
        if (l == null) {
            l = new UserLevel();
            l.setCode(UserLevel.DEFAULT_LEVEL);
            for (String lang : dao.getLanguages()) l.setName(lang, UserLevel.DEFAULT_LEVEL);
            l.setLevelOrder(1);
            dao.save(l);
        }
        return l;
    }


}
