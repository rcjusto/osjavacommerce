package org.store.publications.digitalbooks;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.struts2.json.JSONException;
import org.apache.struts2.json.JSONUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.store.core.beans.*;
import org.store.core.beans.Currency;
import org.store.core.beans.utils.PageMeta;
import org.store.core.dao.HibernateDAO;
import org.store.core.globals.ImageResolver;
import org.store.core.globals.ImageResolverImpl;
import org.store.core.globals.SomeUtils;
import org.store.core.globals.StoreMessages;
import org.store.core.globals.config.Store20Database;
import org.store.core.hibernate.HibernateSessionFactory;
import org.store.publications.LibrariesEventServiceImpl;
import org.store.publications.OnixProduct;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.*;

/**
 * Rogelio Caballero
 * 29/09/11 19:54
 */
public class ONIXUtils {

    public static Logger log = Logger.getLogger(ONIXUtils.class);
    private static final Integer NOTIFICATION_TYPE_DELETE = 5;

    public static String updateProduct(OnixProduct onixProduct, Store20Database databaseConfig, String storeCode, Provider provider, String basePath, String onixPath) {
        if (!basePath.endsWith(File.separator)) basePath += File.separator;
        String message = null;
        try {
            Session session = HibernateSessionFactory.getNewSession(databaseConfig);
            Transaction tx = session.beginTransaction();
            try {
                HibernateDAO dao = new HibernateDAO(session, storeCode);
                boolean generateSeo = "Y".equalsIgnoreCase(dao.getStorePropertyValue(StoreProperty.PROP_AUTOGENERATE_METAS, StoreProperty.TYPE_GENERAL, StoreProperty.PROP_DEFAULT_AUTOGENERATE_METAS));
                String propZoom = dao.getStorePropertyValue(StoreProperty.PROP_PRODUCT_IMAGES_ZOOM, StoreProperty.TYPE_GENERAL, StoreProperty.PROP_DEFAULT_PRODUCT_IMAGES_ZOOM);
                String propDetail = dao.getStorePropertyValue(StoreProperty.PROP_PRODUCT_IMAGES_DETAIL, StoreProperty.TYPE_GENERAL, StoreProperty.PROP_DEFAULT_PRODUCT_IMAGES_DETAIL);
                String propList = dao.getStorePropertyValue(StoreProperty.PROP_PRODUCT_IMAGES_LIST, StoreProperty.TYPE_GENERAL, StoreProperty.PROP_DEFAULT_PRODUCT_IMAGES_LIST);
                Integer propMaxWidth = SomeUtils.strToInteger(dao.getStorePropertyValue(StoreProperty.PROP_PRODUCT_IMAGES_MAX_WIDTH, StoreProperty.TYPE_GENERAL, StoreProperty.PROP_DEFAULT_PRODUCT_IMAGES_MAX_WIDTH));
                Integer propMaxHeight = SomeUtils.strToInteger(dao.getStorePropertyValue(StoreProperty.PROP_PRODUCT_IMAGES_MAX_HEIGHT, StoreProperty.TYPE_GENERAL, StoreProperty.PROP_DEFAULT_PRODUCT_IMAGES_MAX_HEIGHT));
                ImageResolver ir = new ImageResolverImpl(basePath, storeCode, propZoom, propDetail, propList, propMaxWidth, propMaxHeight);

                Category parentCatDigital = null;
                Category parentCatPaper = null;
                StoreProperty bean = dao.getStoreProperty(LibrariesEventServiceImpl.PROP_CONFIGURATION_PROPERTY, StoreProperty.TYPE_GENERAL);
                if (bean != null && StringUtils.isNotEmpty(bean.getValue())) {
                    try {
                        Object o = JSONUtil.deserialize(bean.getValue());
                        if (o != null && o instanceof Map) {
                            Map config = (Map) o;
                            Long idCatD = (config.containsKey("category_digital")) ? SomeUtils.strToLong((String) config.get("category_digital")) : null;
                            parentCatDigital = dao.getCategory(idCatD);
                            Long idCatP = (config.containsKey("category_paper")) ? SomeUtils.strToLong((String) config.get("category_paper")) : null;
                            parentCatPaper = dao.getCategory(idCatP);
                        }
                    } catch (JSONException e) {
                        log.error(e.getMessage(), e);
                    }
                }


                String[] languages = dao.getLanguages();
                String partNumber = onixProduct.getCode();
                Integer notificationType = (onixProduct.getNotificationType() != null) ? SomeUtils.strToInteger(onixProduct.getNotificationType()) : null;
                Product product = null;
                if (StringUtils.isNotEmpty(partNumber)) {
                    product = dao.getProductOfProvider(provider, partNumber);
                }
                if (notificationType != null && NOTIFICATION_TYPE_DELETE.equals(notificationType)) {  // producto eliminado del inventario
                    if (product != null) {
                        product.setArchived(true);
                        product.setActive(false);
                        dao.save(product);
                    }
                } else if (StringUtils.isNotEmpty(partNumber)) {

                    if (product == null) {
                        product = new Product();
                        product.setPartNumber(partNumber);
                        product.setMfgPartnumber(partNumber);
                        if (OnixProduct.TYPE_DIGITAL.equalsIgnoreCase(onixProduct.getType())) product.setProductType(Product.TYPE_DIGITAL);
                        else product.setProductType(Product.TYPE_STANDARD);
                        product.setActive(true);

                        // set new label
                        ProductLabel labelNew = dao.getProductLabelByCode(ProductLabel.LABEL_NEW);
                        if (labelNew != null) {
                            if (product.getLabels() == null) product.setLabels(new HashSet<ProductLabel>());
                            product.getLabels().add(labelNew);
                        }
                    }
                    if (StringUtils.isEmpty(product.getPartNumber())) product.setPartNumber(partNumber);

                    // titulos
                    StringBuilder seoKeywords = new StringBuilder();
                    String title = onixProduct.getName();
                    String description = onixProduct.getDescription();
                    String autor = onixProduct.getAuthor();
                    if (StringUtils.isNotEmpty(title)) seoKeywords.append(title);
                    if (StringUtils.isNotEmpty(autor)) {
                        Manufacturer manufacturer = getManufacturer(autor, dao, storeCode);
                        if (manufacturer != null) product.setManufacturer(manufacturer);
                        seoKeywords.append(", ").append(autor);
                    }

                    // categorias
                    List<String> categoryNames = onixProduct.getCategories();
                    if (categoryNames != null && !categoryNames.isEmpty()) {
                        for (String catCode : categoryNames) {
                            List<Codes> codes = OnixProduct.getBICCategoryName(catCode, dao);
                            Category category = getCategory(codes, dao, Product.TYPE_DIGITAL.equalsIgnoreCase(product.getProductType()) ? parentCatDigital : parentCatPaper);
                            if (product.getCategory() == null) product.setCategory(category);
                            if (category != null) {
                                if (product.getProductCategories() == null) product.setProductCategories(new HashSet<Category>());
                                product.getProductCategories().add(category);
                            }
                        }
                    }

                    Double costPrice = null;
                    Currency costCurrency = null;
                    String defaultCurrency = dao.getDefaultCurrency().getCode();
                    Map<String, Double> productPrice = onixProduct.getPrice();
                    if (productPrice != null && productPrice.containsKey(defaultCurrency)) {
                        costPrice = productPrice.get(defaultCurrency);
                        if (costPrice != null) product.setCostPrice(costPrice);
                    } else if (productPrice != null && !productPrice.isEmpty()) {
                        for (String currCode : productPrice.keySet()) {
                            costPrice = productPrice.get(currCode);
                            Currency curr = dao.getCurrency(currCode);
                            if (curr != null && costPrice != null) {
                                costCurrency = curr;
                                product.setCostPrice(costPrice);
                                break;
                            }
                        }
                    }

                    Long stock = onixProduct.getStock();
                    if (stock != null) product.setStock(stock);

                    dao.save(product);
                    dao.auditStock(product, "Updated by Digital Books", null);

                    // imagenes
                    String pathUpload = basePath + "stores" + File.separator + storeCode + File.separator + "uploads" + File.separator;
                    int unprocessedImages = 0;
                    List<Map<String, String>> images = onixProduct.getImages();
                    if (StringUtils.isEmpty(ir.getImageForProduct(product, "list/")) && images != null) {
                        for (Map<String, String> map : images) {
                            if ("included".equalsIgnoreCase(map.get("mode"))) {
                                // buscar archivo en la carpeta del onix
                                if (onixPath != null) {
                                    File imgFile = new File(onixPath + File.separator + map.get("file"));
                                    if (imgFile.exists()) {
                                        try {
                                            ir.deleteImages(product);
                                            if (!ir.processImage(product, imgFile, map.get("type"))) {
                                                // copiar a uploads
                                                File targetFile = new File(pathUpload + partNumber + "." + map.get("type"));
                                                if (!targetFile.exists()) FileUtils.copyFile(imgFile, targetFile);
                                                unprocessedImages++;
                                            }
                                        } catch (Exception ignored) {
                                        }
                                    }
                                }
                            } else {
                                // hay q bajarlo
                                BufferedInputStream in = null;
                                FileOutputStream fout = null;
                                FileUtils.forceMkdir(new File(pathUpload));
                                String filePath = pathUpload + partNumber + "." + map.get("type");
                                try {
                                    in = new BufferedInputStream(new URL(map.get("file")).openStream());
                                    fout = new FileOutputStream(filePath);
                                    byte data[] = new byte[1024];
                                    int count;
                                    while ((count = in.read(data, 0, 1024)) != -1) fout.write(data, 0, count);
                                } catch (Exception ignored) {

                                } finally {
                                    if (in != null) try {
                                        in.close();
                                    } catch (IOException ignored) {
                                    }
                                    if (fout != null) try {
                                        fout.close();
                                    } catch (IOException ignored) {
                                    }
                                }

                                File imgFile = new File(filePath);
                                if (imgFile.exists()) {
                                    ir.deleteImages(product);
                                    if (ir.processImage(product, imgFile, map.get("type"))) imgFile.delete();
                                    else unprocessedImages++;
                                }
                            }
                        }
                    }
                    if (unprocessedImages > 0) message = String.valueOf(unprocessedImages) + " unprocessed images. Click <a href=\"productproccessimages.jsp\">here</a>.";

                    // preview
                    List<Map<String, String>> previews = onixProduct.getPreview();
                    if (product.getResource(Resource.TYPE_PREVIEW) == null && previews != null) {
                        for (Map<String, String> map : previews) {
                            if ("included".equalsIgnoreCase(map.get("mode"))) {
                                // buscar archivo en la carpeta del onix
                                if (onixPath != null) {
                                    File previewFile = new File(onixPath + File.separator + map.get("file"));
                                    if (previewFile.exists()) {
                                        if (updateProductPreviewFile(product, previewFile, dao, basePath)) {
                                            dao.save(product);
                                        }
                                    }
                                }
                            } else {
                                // hay q bajarlo
                                BufferedInputStream in = null;
                                FileOutputStream fout = null;
                                FileUtils.forceMkdir(new File(pathUpload));
                                String filePath = pathUpload + map.get("file");
                                File previewFile = new File(filePath);
                                if (previewFile.exists()) previewFile.delete();
                                try {
                                    in = new BufferedInputStream(new URL(map.get("file")).openStream());
                                    fout = new FileOutputStream(filePath);
                                    byte data[] = new byte[1024];
                                    int count;
                                    while ((count = in.read(data, 0, 1024)) != -1) {
                                        fout.write(data, 0, count);
                                    }
                                } catch (Exception ignored) {
                                } finally {
                                    if (in != null) try {
                                        in.close();
                                    } catch (IOException ignored) {
                                    }
                                    if (fout != null) try {
                                        fout.close();
                                    } catch (IOException ignored) {
                                    }
                                }

                                if (previewFile.exists()) {
                                    if (updateProductPreviewFile(product, previewFile, dao, basePath)) dao.save(product);
                                }

                            }
                        }
                    }

                    if (costPrice != null && costPrice > 0 && costCurrency != null) {
                        ProductProvider pp = dao.getProductProvider(product, provider);
                        if (pp == null) {
                            pp = new ProductProvider();
                            pp.setActive(false);
                            pp.setProduct(product);
                            pp.setProvider(provider);
                            pp.setSku(partNumber);
                        }
                        pp.setCostPrice(costPrice);
                        pp.setCostCurrency(costCurrency);
                        pp.setStock(product.getStock());
                        pp.setLastUpdate(Calendar.getInstance().getTime());
                        dao.save(pp);
                    }

                    if (languages != null) {
                        for (String lang : languages) {
                            ProductLang productLang = dao.getProductLang(product, lang);
                            if (productLang == null) {
                                productLang = new ProductLang();
                                productLang.setProduct(product);
                                productLang.setProductLang(lang);
                            }
                            if (StringUtils.isEmpty(productLang.getProductName())) productLang.setProductName(title);
                            if (StringUtils.isEmpty(productLang.getDescription())) productLang.setDescription(description);
                            if (generateSeo && !"".equalsIgnoreCase(seoKeywords.toString())) {
                                // String descriptionText = SomeUtils.extractText(productLang.getDescription());
                                if (StringUtils.isEmpty(productLang.getMetaValue(PageMeta.META_TITLE))) productLang.addMeta(PageMeta.META_TITLE, seoKeywords.toString(), false);
                                if (StringUtils.isEmpty(productLang.getMetaValue(PageMeta.META_DESCRIPTION))) productLang.addMeta(PageMeta.META_DESCRIPTION,  seoKeywords.toString(), false);
                                if (StringUtils.isEmpty(productLang.getMetaValue(PageMeta.META_KEYWORDS))) productLang.addMeta(PageMeta.META_KEYWORDS,  seoKeywords.toString(), false);
                            }
                            dao.save(productLang);
                        }
                    }

                    if (product.getCostPrice() == null || product.getCostPrice() < 0.01) product.setActive(false);
                    if (StringUtils.isEmpty(product.getSearchKeywords())) product.setSearchKeywords( seoKeywords.toString() );
                    dao.updateProductUrlCode(product, null, dao.getDefaultLanguage());
                    dao.indexProduct(product, false);

                }

                tx.commit();
            } catch (Exception e) {
                tx.rollback();
                throw e;
            } finally {
                if (session.isOpen()) session.close();
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            message = e.getMessage();
        }
        return message;
    }

    private static boolean updateProductPreviewFile(Product product, File previewFile, HibernateDAO dao, String path) {
        if (previewFile != null) {
            String filename = FilenameUtils.getName(previewFile.getName());
            String fileExt = FilenameUtils.getExtension(previewFile.getName());
            if (StringUtils.isNotEmpty(filename)) {
                // validate if resource exist
                Resource resource = null;
                Set<Resource> list = product.getProductResources();
                if (list != null)
                    for (Resource r : list)
                        if (filename.equalsIgnoreCase(r.getFileName()))
                            resource = r;

                // create if not exist
                if (resource == null) {
                    while (dao.resourceExist(filename)) {
                        filename = "_" + filename;
                    }
                    resource = new Resource();
                    resource.setFileName(filename);
                    for (String lang : dao.getLanguages()) {
                        resource.setResourceName(lang, dao.getLocalizedtextValue("digital.preview.for", lang) + " " + product.getProductName(lang));
                    }
                    resource.setResourceType(Resource.TYPE_PREVIEW);
                    dao.save(resource);
                    if (product.getProductResources() == null) product.setProductResources(new HashSet<Resource>());
                    product.getProductResources().add(resource);

                    File target = new File(path + "stores" + File.separator + dao.getStoreCode() + StoreMessages.PATH_RESOURCES + File.separator + resource.getId() + "." + fileExt);
                    return previewFile.renameTo(target);
                } else {
                    File target = new File(path + "stores" + File.separator + dao.getStoreCode() + StoreMessages.PATH_RESOURCES + File.separator + resource.getId() + "." + fileExt);
                    if (!target.exists()) {
                        return previewFile.renameTo(target);
                    }
                }
            }
        }
        return false;
    }

    private static Manufacturer getManufacturer(String manufacturerName, HibernateDAO dao, String storeCode) {
        if (StringUtils.isNotEmpty(manufacturerName)) {
            Manufacturer manufacturer = dao.getManufacturerByName(manufacturerName);
            if (manufacturer == null) {
                manufacturer = new Manufacturer();
                manufacturer.setManufacturerName(manufacturerName);
                manufacturer.setInventaryCode(storeCode);
                dao.updateManufacturerUrlCode(manufacturer, manufacturerName);
                dao.save(manufacturer);
            }
            return manufacturer;
        }
        return null;
    }

    private static Category getCategory(List<Codes> codes, HibernateDAO dao, Category parent) {
        Category category = null;
        Long idParent = (parent != null) ? parent.getIdCategory() : dao.getRootCategory().getIdCategory();
        if (codes != null && !codes.isEmpty()) {
            for (Codes code : codes) {
                category = dao.getCategoryByExternalCodeAncestorOf(code.getCode(), parent);
                if (category == null) {
                    category = new Category();
                    category.setExternalCode(code.getCode());
                    category.setIdParent(idParent);
                    category.setVisible(true);
                    dao.save(category);
                    if (idParent != null) {
                        CategoryTree tree = new CategoryTree();
                        tree.setChild(category);
                        tree.setParent(dao.getCategory(idParent));
                        dao.save(tree);
                    }
                    String[] languages = dao.getLanguages();
                    if (languages != null) {
                        for (String l : languages) {
                            CategoryLang catLang = dao.getCategoryLang(category, l);
                            if (catLang == null) {
                                catLang = new CategoryLang();
                                catLang.setCategory(category);
                                catLang.setCategoryLang(l);
                            }
                            catLang.setCategoryName(code.getText(l));
                            dao.save(catLang);
                        }
                    }
                    dao.updateCategoryUrlCode(category, null, dao.getDefaultLanguage());
                }
                idParent = category.getIdCategory();
            }
        }
        return category;
    }

}
