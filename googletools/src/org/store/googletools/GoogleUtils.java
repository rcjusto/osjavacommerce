package org.store.googletools;

import org.store.core.beans.*;
import org.store.core.dao.HibernateDAO;
import org.store.core.globals.StoreThread;
import org.store.core.globals.config.Store20Database;
import org.store.core.hibernate.HibernateSessionFactory;
import org.store.core.utils.quartz.JobStoreThread;
import org.store.core.utils.velocity.StoreVelocityGenerator;
import org.apache.commons.lang.StringUtils;
import org.apache.velocity.VelocityContext;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.*;

public class GoogleUtils extends JobStoreThread {

    private static final String TEMPLATE_SITEMAP_XML = "org/store/googletools/resources/sitemap_xml.vm";
    public static final String FILE_SITEMAP_XML = "sitemap.xml";
    private static final String TEMPLATE_MERCHANT_RSS = "org/store/googletools/resources/merchant_rss.vm";
    public static final String FILE_MERCHANT_RSS = "fullcatalog";

    private String basePath;
    private static final int MAX_PRODUCTS_PER_FILE = 20000;

    public GoogleUtils(String storeCode, Store20Database db) {
        this.storeCode = storeCode;
        this.databaseConfig = db;
        setName("google_task_" + String.valueOf(Calendar.getInstance().getTimeInMillis()));
    }

    @Override
    public void run() {
        try {
            Session session = HibernateSessionFactory.getNewSession(databaseConfig);
            Transaction tx = session.beginTransaction();
            try {
                HibernateDAO dao = new HibernateDAO(session, storeCode);
                this.basePath = dao.getStorePropertyValue(StoreProperty.PROP_SITE_PATH, StoreProperty.TYPE_GENERAL, "");
                setExecutionMessage("Generating Google sitemap.xml");
                setExecutionPercent(10d);
                generateGoogleSiteMap(session);
                setExecutionMessage("Generating Google fullcatalog.xml");
                generateGooleMerchantRss(session);
                setExecutionMessage("Complete");
                setExecutionPercent(100d);
                tx.commit();
            } catch (Exception e) {
                tx.rollback();
                StoreThread.log.error(e.getMessage(), e);
            } finally {
                if (session.isOpen()) session.close();
            }
        } catch (Exception e) {
            StoreThread.log.error(e.getMessage(), e);
        }
    }

    public void generateGoogleSiteMap(Session hSession) {
        try {
            HibernateDAO dao = new HibernateDAO(hSession, storeCode);
            Date d = Calendar.getInstance().getTime();
            String cad = new SimpleDateFormat("yyyy-MM-dd").format(d) + "T" + new SimpleDateFormat("HH:mm:ss").format(d) + "+00:00";
            VelocityContext ctx = StoreVelocityGenerator.createContext();
            ctx.put("fecha", cad);
            String friendlyUrl = getStoreProperty(StoreProperty.PROP_USE_FRIENDLY_URLS, dao);
            if (StringUtils.isEmpty(friendlyUrl)) friendlyUrl = StoreProperty.PROP_DEFAULT_USE_FRIENDLY_URLS;
            boolean useFriendlyUrl = "Y".equalsIgnoreCase(friendlyUrl);
            String urlSite = getStoreProperty(StoreProperty.PROP_SITE_URL, dao);
            if (urlSite.endsWith("/")) urlSite = urlSite.substring(0, urlSite.length() - 1);
            ctx.put("urlsite", urlSite);

            List<String> staticPages = new ArrayList<String>();
            for (StaticText p : getStaticPages(dao)) staticPages.add(urlPage(p, urlSite, useFriendlyUrl));
            ctx.put("staticPages", staticPages);

            List<String> categories = new ArrayList<String>();
            for (Category c : getCategories(dao)) categories.add(urlCategory(c, urlSite, useFriendlyUrl));
            ctx.put("categories", categories);

            String contentXml = StoreVelocityGenerator.getGeneratedString(ctx, TEMPLATE_SITEMAP_XML);
            FileWriter fwXml = new FileWriter(basePath + File.separator + FILE_SITEMAP_XML);
            fwXml.write(contentXml);
            fwXml.close();
        } catch (IOException e) {
            StoreThread.log.error(e.getMessage(), e);
        }
    }

    public void generateGooleMerchantRss(Session hSession) {
        try {
            HibernateDAO dao = new HibernateDAO(hSession, storeCode);
            DecimalFormatSymbols dfs = new DecimalFormatSymbols(new Locale("en"));
            dfs.setDecimalSeparator('.');
            DecimalFormat df = new DecimalFormat("0.00", dfs);
            DecimalFormat df1 = new DecimalFormat("#", dfs);
            Date d = Calendar.getInstance().getTime();
            String updated = new SimpleDateFormat("yyyy-MM-dd").format(d) + "T" + new SimpleDateFormat("HH:mm:ss").format(d) + "+00:00";
            String today = new SimpleDateFormat("yyyy-MM-dd").format(d);
            VelocityContext ctx = StoreVelocityGenerator.createContext();
            ctx.put("updated", updated);
            ctx.put("today", today);
            ctx.put("siteName", getStoreProperty(StoreProperty.PROP_SITE_NAME, dao));
            ctx.put("basePath", basePath);

            String urlSite = getStoreProperty(StoreProperty.PROP_SITE_URL, dao);
            if (urlSite.endsWith("/")) urlSite = urlSite.substring(0, urlSite.length() - 1);
            ctx.put("siteUrl", urlSite);
            String defaultLanguage = getStoreProperty(StoreProperty.PROP_DEFAULT_LANGUAGE, dao);
            String weightUnit = getStoreProperty(StoreProperty.PROP_WEIGHT_UNIT, dao);
            String dimensionUnit = getStoreProperty(StoreProperty.PROP_DIMENSION_UNIT, dao);
            if (StringUtils.isEmpty(defaultLanguage)) defaultLanguage = "en";

            Criteria criP = hSession.createCriteria(Product.class);
            criP.add(Restrictions.eq("inventaryCode", storeCode));
            criP.add(Restrictions.eq("active", Boolean.TRUE));
            criP.setProjection(Projections.count("idProduct"));
            Number numProds = (Number) criP.uniqueResult();
            int rest = numProds.intValue() / MAX_PRODUCTS_PER_FILE;
            criP.setProjection(null);
            criP.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
            int index = 0;
            for (int page = 0; page <= rest; page++) {
                setExecutionMessage("Generating Google fullcatalog.xml (" + String.valueOf(page+1) + "/" + String.valueOf(rest+1)+")");
                hSession.clear();
                criP.setMaxResults(MAX_PRODUCTS_PER_FILE);
                criP.setFirstResult(page * MAX_PRODUCTS_PER_FILE);
                List<Product> listaProd = criP.list();
                if (listaProd != null && listaProd.size() > 0) {
                    List<Map<String, String>> lista = new ArrayList<Map<String, String>>();
                    for (Product p : listaProd) {
                        setExecutionPercent((index++) * 90d / numProds.intValue() + 10d);
                        String img = findProductImg(p, basePath);
                        ProductLang pl = p.getLanguage(defaultLanguage);
                        if (pl != null) {
                            Map<String, String> map = new HashMap<String, String>();
                            map.put("name", pl.getProductName());
                            map.put("desc", pl.getDescription());
                            if (p.getManufacturer() != null) map.put("brand", p.getManufacturer().getManufacturerName());
                            map.put("id", p.getPartNumber());
                            if (StringUtils.isNotEmpty(img)) map.put("imglink", urlSite + "/images/products/" + img);
                            map.put("link", urlSite + "/product/" + p.getUrlCode());
                            map.put("mpn", p.getMfgPartnumber());
                            map.put("price", df.format(p.getBasePrice(null, dao)));
                            map.put("stock", p.getStock().toString());
                            map.put("categories", getProductCategories(p, defaultLanguage, hSession));
                            if (p.getWeight() != null) map.put("weight", df1.format(p.getWeight()) + " " + weightUnit);
                            if (p.getDimentionHeight() != null) map.put("height", df1.format(p.getDimentionHeight()) + " " + dimensionUnit);
                            if (p.getDimentionLength() != null) map.put("length", df1.format(p.getDimentionLength()) + " " + dimensionUnit);
                            if (p.getDimentionWidth() != null) map.put("width", df1.format(p.getDimentionWidth()) + " " + dimensionUnit);
                            lista.add(map);
                            hSession.evict(pl);
                        }
                        hSession.evict(p);
                        ctx.put("products", lista);
                    }
                    String contentXml = StoreVelocityGenerator.getGeneratedString(ctx, TEMPLATE_MERCHANT_RSS);
                    FileWriter fwXml = new FileWriter(basePath + File.separator + FILE_MERCHANT_RSS + "_" + page + ".xml");
                    fwXml.write(contentXml);
                    fwXml.close();
                }
            }

        } catch (IOException e) {
            StoreThread.log.error(e.getMessage(), e);
        }
    }

    public String urlCategory(Category c, String urlSite, boolean friendyUrl) {
        StringBuffer result = new StringBuffer(urlSite);
        if (friendyUrl && StringUtils.isNotEmpty(c.getUrlCode())) {
            result.append("/category/").append(c.getUrlCode());
        } else {
            result.append("/category.jsp?");
            if (StringUtils.isNotEmpty(c.getUrlCode())) result.append("code=").append(c.getUrlCode());
            else result.append("idCategory=").append(c.getIdCategory().toString());
        }
        return result.toString();
    }

    public String urlPage(StaticText c, String urlSite, boolean friendyUrl) {
        StringBuffer result = new StringBuffer(urlSite);
        if (friendyUrl && StringUtils.isNotEmpty(c.getUrlCode())) {
            result.append("/page/").append(c.getUrlCode());
        } else {
            result.append("/page.jsp?");
            if (StringUtils.isNotEmpty(c.getUrlCode())) result.append("code=").append(c.getUrlCode());
            else result.append("idStaticText=").append(c.getId().toString());
        }
        return result.toString();
    }

    private String getStoreProperty(String prop, HibernateDAO dao) {
        return dao.getStorePropertyValue(prop, StoreProperty.TYPE_GENERAL, "");
    }

    private List<StaticText> getStaticPages(HibernateDAO dao) {
        return dao.getStaticTexts(StaticText.TYPE_PAGE);
    }

    private List<Category> getCategories(HibernateDAO dao) {
        return dao.getCategories(true);
    }

    private String getProductCategories(Product p, String lang, Session hSession) {
        List<Category> l = new ArrayList<Category>();
        if (p != null && p.getCategory() != null) {
            Category bean = p.getCategory();
            l.add(bean);
            while (bean != null && bean.getIdParent() != null) {
                bean = (Category) hSession.get(Category.class, bean.getIdParent());
                if (bean != null) l.add(bean);
            }
        }
        if (l.size() > 1) Collections.reverse(l);
        StringBuffer buff = new StringBuffer();
        for (Category c : l) {
            if (StringUtils.isNotEmpty(buff.toString())) buff.append(" > ");
            buff.append(c.getCategoryName(lang));
            hSession.evict(c);
        }
        return buff.toString();
    }

    private String findProductImg(final Product p, String basePath) {
        File folder = new File(basePath + File.separator + "images" + File.separator + "products");
        if (folder.exists()) {
            File[] arrFiles = folder.listFiles(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    name = name.toLowerCase();
                    return name.startsWith(p.getPartNumber().toLowerCase()) && (name.endsWith(".png") || name.endsWith(".gif") || name.endsWith(".jpg"));
                }
            });
            if (arrFiles != null && arrFiles.length > 0) return arrFiles[0].getName();
        }
        return null;
    }


}
