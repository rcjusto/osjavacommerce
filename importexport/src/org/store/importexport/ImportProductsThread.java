package org.store.importexport;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.store.core.beans.*;
import org.store.core.beans.utils.TableFile;
import org.store.core.dao.HibernateDAO;
import org.store.core.globals.BaseAction;
import org.store.core.hibernate.HibernateSessionFactory;
import org.store.importexport.admin.ImportProductsAction;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * Rogelio Caballero
 * 12/04/12 15:55
 */
public class ImportProductsThread extends ImportFileThread {

    public ImportProductsThread(BaseAction action) {
        super(action);
    }

    @Override
    public void run() {
        TableFile tableFile = getTableFile(ImportProductsAction.TYPE_PRODUCT);
        Map map;
        if (tableFile.getRows() != null && !tableFile.getRows().isEmpty()) {
            long index = 0, total = tableFile.getRows().size(), inserted = 0, modified = 0;
            try {
                Session session = HibernateSessionFactory.getNewSession(databaseConfig);
                Transaction tx = session.beginTransaction();
                HibernateDAO dao = new HibernateDAO(session, storeCode);
                try {
                    addOutputMessage("Start time: " + Calendar.getInstance().getTime().toString());

                    for (TableFile.TableFileRow row : tableFile.getRows()) {
                        setExecutionMessage("Importing row " + (index++) + " of " + total);
                        setExecutionPercent(index * 100d / total);
                        map = getRow(row, getFields());
                        if (importProduct(map)) inserted++;
                        else modified++;
                    }

                    addOutputMessage("End time: " + Calendar.getInstance().getTime().toString());
                    addOutputMessage("New products: " + String.valueOf(inserted));
                    addOutputMessage("Updated products: " + String.valueOf(modified));

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    tx.rollback();
                    addOutputMessage("ERROR: " + e.getMessage());
                } finally {
                    if (session.isOpen()) session.close();
                }

                saveLogFile();
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    private boolean importProduct(Map<String, String> map) throws Exception {


        Session session = HibernateSessionFactory.getNewSession(databaseConfig);
        Transaction tx = session.beginTransaction();
        try {
            HibernateDAO dao = new HibernateDAO(session, storeCode);
            String defLang = dao.getDefaultLanguage();

            boolean inserted = false;
            String code = (map.containsKey("partNumber")) ? map.get("partNumber") : null;
            Product p = dao.getProductByPartNumber(code);
            if (p == null) {
                p = new Product();
                p.setInventaryCode(dao.getStoreCode());
                inserted = true;
                if ("Y".equalsIgnoreCase(dao.getStorePropertyValue(StoreProperty.PROP_PRODUCT_NEW_LABEL_AUTO, StoreProperty.TYPE_GENERAL, StoreProperty.PROP_DEFAULT_PRODUCT_NEW_LABEL_AUTO))) {
                    p.addLabel(dao.getProductLabelByCode(ProductLabel.LABEL_NEW, true));
                }
            }
            if (p.getForUsers() != null) Hibernate.initialize(p.getForUsers());

            Map mapProduct = new HashMap<String, Object>();
            Map<String, Object> mapLang = new HashMap<String, Object>();
            Category cat;
            Manufacturer man;
            for (String key : map.keySet()) {
                if (key.startsWith("lang.")) mapLang.put(key.substring("lang.".length()), map.get(key));
                else if ("categories".equalsIgnoreCase(key) && StringUtils.isNotEmpty(map.get(key))) {
                    cat = null;
                    String[] arr = map.get(key).split("[-][>]");
                    if (arr != null && arr.length > 0) {
                        for (String catName : arr) {
                            Category parentCat = cat;
                            cat = dao.getCategoryByName(catName.trim(), defLang);
                            if (cat == null) {
                                cat = new Category();
                                cat.setInventaryCode(dao.getStoreCode());
                                cat.setVisible(false);
                                cat.setIdParent(parentCat != null ? parentCat.getIdCategory() : dao.getRootCategory().getIdCategory());
                                dao.save(cat);
                                if (parentCat != null) {
                                    CategoryTree tree = new CategoryTree();
                                    tree.setParent(parentCat);
                                    tree.setChild(cat);
                                    dao.save(tree);
                                }
                                CategoryLang cl = new CategoryLang();
                                cl.setCategory(cat);
                                cl.setCategoryLang(defLang);
                                cl.setCategoryName(catName.trim());
                                dao.save(cl);
                                dao.updateCategoryUrlCode(cat, cat.getUrlCode(), defLang);
                            }
                        }
                    }
                    p.setCategory(cat);
                } else if (key.startsWith("category.")) {
                    if ("category.categoryName".equals(key)) {
                        cat = dao.getCategoryByName(map.get(key), defLang);
                        if (cat == null) {
                            cat = new Category();
                            cat.setInventaryCode(dao.getStoreCode());
                            cat.setVisible(false);
                            cat.setIdParent(dao.getRootCategory().getIdCategory());
                            dao.save(cat);
                            CategoryLang cl = new CategoryLang();
                            cl.setCategory(cat);
                            cl.setCategoryLang(defLang);
                            cl.setCategoryName(map.get(key));
                            dao.save(cl);
                            dao.updateCategoryUrlCode(cat, cat.getUrlCode(), defLang);
                        }
                        p.setCategory(cat);
                    }
                } else if (key.startsWith("manufacturer.")) {
                    if ("manufacturer.manufacturerName".equals(key)) {
                        man = dao.getManufacturerByName(map.get(key));
                        if (man == null) {
                            man = new Manufacturer();
                            man.setInventaryCode(dao.getStoreCode());
                            man.setManufacturerName(map.get(key));
                            dao.updateManufacturerUrlCode(man, null);
                            dao.save(man);
                        }
                        p.setManufacturer(man);
                    }
                } else mapProduct.put(key, map.get(key));
            }
            p.fromMap(mapProduct);
            dao.save(p);

            for (String key : map.keySet()) {
                if (key.startsWith("supplier.")) {
                    int ind1 = key.indexOf(".");
                    int ind2 = key.indexOf(".", ind1 + 1);
                    String field = (ind1 > 0 && ind2 > 0) ? key.substring(ind1 + 1, ind2) : null;
                    String supplierName = (ind1 > 0 && ind2 > 0) ? key.substring(ind2 + 1) : null;
                    if (StringUtils.isNotEmpty(field) && StringUtils.isNotEmpty(supplierName)) {
                        Provider provider = dao.getProviderByName(supplierName);
                        if (provider != null) {
                            ProductProvider pp = dao.getProductProvider(p, provider);
                            if (pp == null) {
                                pp = new ProductProvider();
                                pp.setProduct(p);
                                pp.setProvider(provider);
                            }
                            if ("stock".equalsIgnoreCase(field)) pp.setStock(NumberUtils.toLong(map.get(key)));
                            else if ("sku".equalsIgnoreCase(field)) pp.setSku(map.get(key));
                            else if ("cost".equalsIgnoreCase(field)) pp.setCostPrice(NumberUtils.toDouble(map.get(key)));
                            else if ("currency".equalsIgnoreCase(field)) pp.setCostCurrency(dao.getCurrency(map.get(key)));
                            else if ("active".equalsIgnoreCase(field)) pp.setActive("true".equalsIgnoreCase(map.get(key)));
                            dao.save(pp);
                        }
                    }
                }
            }

            if (p.getProductCategories() == null) p.setProductCategories(new HashSet<Category>());
            if (p.getCategory() != null && !p.getProductCategories().contains(p.getCategory())) {
                p.getProductCategories().add(p.getCategory());
                dao.save(p);
            }

            if (!mapLang.isEmpty()) {
                try {
                    ProductLang pl = dao.getProductLang(p, defLang);
                    if (pl == null) {
                        pl = new ProductLang();
                        pl.setProduct(p);
                        pl.setProductLang(defLang);
                    }
                    BeanUtils.populate(pl, mapLang);
                    dao.save(pl);
                } catch (IllegalAccessException e) {
                    log.error(e.getMessage(), e);
                } catch (InvocationTargetException e) {
                    log.error(e.getMessage(), e);
                }
            }

            if (inserted || map.keySet().contains("stock")) {
                dao.auditStock(p, "Updated in admin module", null);
            }
            if (inserted || map.keySet().contains("lang.productName")) {
                Hibernate.initialize(p.getProductLangs());
                dao.updateProductUrlCode(p, p.getUrlCode(), defLang);
            }
            dao.indexProduct(p, inserted);
            tx.commit();
            return inserted;

        } catch (Exception e) {
            tx.rollback();
        } finally {
            if (session.isOpen()) session.close();
        }

        return false;
    }

    private Map getRow(TableFile.TableFileRow row, List<String> fields) {
        Map result = new HashMap();
        for (int col = 0; col < row.values.size(); col++) {
            if (fields.size() > col && StringUtils.isNotEmpty(fields.get(col))) {
                if (row.values.get(col) != null) result.put(fields.get(col), row.values.get(col));
            }
        }
        return result;
    }

}
