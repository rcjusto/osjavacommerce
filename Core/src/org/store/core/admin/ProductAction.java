package org.store.core.admin;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.json.JSONException;
import org.apache.struts2.json.JSONUtil;
import org.store.core.beans.*;
import org.store.core.beans.Currency;
import org.store.core.beans.utils.*;
import org.store.core.globals.ImageResolver;
import org.store.core.globals.SomeUtils;
import org.store.core.globals.TableParser;
import org.store.core.globals.config.Store20Config;
import org.store.core.utils.quartz.JobStoreThread;
import org.store.core.utils.quartz.ThreadUtilities;
import org.store.core.utils.suppliers.SupplierUtils;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.swing.tree.DefaultMutableTreeNode;
import java.io.File;
import java.io.FilenameFilter;
import java.io.Reader;
import java.io.StringReader;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: Administrador
 * Date: Mar 22, 2010
 */
@Namespace(value = "/admin")
@ParentPackage(value = "store-admin")
public class ProductAction extends AdminModuleAction {
    private static final String PATH_MASSIVE_UPLOAD = "uploads";
    private static final String CNT_ERROR_IMAGE_CANNOT_BE_PROCESSED = "error.image.cannot.be.processed";
    private static final String CNT_DEFAULT_ERROR_IMAGE_CANNOT_BE_PROCESSED = "Image {0} cannot be processed. {1}";
    private static final String CNT_ERROR_CANNOT_DETELE_PRODUCT = "error.cannot.delete.product";
    private static final String CNT_DEFAULT_ERROR_CANNOT_DETELE_PRODUCT = "Can not delete product {0}. {1}";
    private static final String CNT_ERROR_CANNOT_DETELE_COMPLEMENT = "error.cannot.delete.complement";
    private static final String CNT_DEFAULT_ERROR_CANNOT_DETELE_COMPLEMENT = "Can not delete complement {0}. {1}";
    private static final String CNT_ERROR_CANNOT_DETELE_COMPLEMENTGROUP = "error.cannot.delete.complementgroups";
    private static final String CNT_DEFAULT_ERROR_CANNOT_DETELE_COMPLEMENTGROUP = "Can not delete complement group {0}. {1}";
    private static final String CNT_COOKIE_FILTERS = "product_filters";
    private static final String CNT_COOKIE_COLUMNS = "product_columns";
    private static final int MAX_MANUFACTURERS = 150;
    private static final int MAX_CATEGORIES = 50;

    @Override
    public void prepare() throws Exception {
        product = (Product) dao.get(Product.class, idProduct);
        category = (Category) dao.get(Category.class, idCategory);
        manufacturer = (Manufacturer) dao.get(Manufacturer.class, idManufacturer);
        review = (ProductReview) dao.get(ProductReview.class, idReview);
        group = (ComplementGroup) dao.get(ComplementGroup.class, idGroup);
    }

    @Action(value = "productlist", results = {
            @Result(type = "velocity", location = "/WEB-INF/views/admin/productlist.vm"),
            @Result(type = "velocity", name = "selector", location = "/WEB-INF/views/admin/productlist_selector.vm"),
            @Result(type = "velocity", name = "modal", location = "/WEB-INF/views/admin/productlist_modal.vm")
    })
    public String productlist() throws Exception {
        if ("Y".equalsIgnoreCase(productUpdate)) {
            productlistSave();
            dao.flushSession();
        }

        if (selecteds != null && selecteds.length > 0) {
            for (Long id : selecteds) {
                Product prod = (Product) dao.get(Product.class, id);
                if (prod != null) {
                    String res = dao.isUsedProduct(prod);
                    if (StringUtils.isNotEmpty(res)) {
                        addActionError(getText(CNT_ERROR_CANNOT_DETELE_PRODUCT, CNT_DEFAULT_ERROR_CANNOT_DETELE_PRODUCT, new String[]{prod.getProductName(getDefaultLanguage()), res}));
                    } else {
                        dao.deleteProduct(prod);
                    }
                }
            }
            dao.flushSession();
        }

        List<ProductTableField> listaCampos = null;
        if ("Y".equalsIgnoreCase(updateFilters)) {
            // Salvar cookies
            if (productFilter != null) {
                String cookieValue = JSONUtil.serialize(productFilter);
                response.addCookie(getCookie(CNT_COOKIE_FILTERS, cookieValue));
            }
            if (fieldNames != null) {
                Map<String, String[]> map = new HashMap<String, String[]>();
                map.put("fieldNames", fieldNames);
                map.put("showField", showField);
                map.put("editField", editField);
                String cookieValue = JSONUtil.serialize(map);
                response.addCookie(getCookie(CNT_COOKIE_COLUMNS, cookieValue));
            }
        } else {
            // llenar a partir de las cookies
            String cookieValue = getCookie(CNT_COOKIE_FILTERS);
            if (StringUtils.isNotEmpty(cookieValue)) {
                try {
                    Object o = JSONUtil.deserialize(cookieValue);
                    if (o instanceof Map) productFilter = new ProductFilter((Map) o);
                } catch (JSONException e) {
                    log.error(e.getMessage(), e);
                }
            }
            String cookieValue1 = getCookie(CNT_COOKIE_COLUMNS);
            if (StringUtils.isNotEmpty(cookieValue1)) {
                try {
                    Object o = JSONUtil.deserialize(cookieValue1);
                    if (o instanceof Map) {
                        Map map = (Map) o;
                        if (map.containsKey("fieldNames")) {
                            List<String> l = (List<String>) map.get("fieldNames");
                            fieldNames = l.toArray(new String[]{});
                        }
                        if (map.containsKey("showField")) {
                            List<String> l = (List<String>) map.get("showField");
                            showField = l.toArray(new String[]{});
                        }
                        if (map.containsKey("editField")) {
                            List<String> l = (List<String>) map.get("editField");
                            editField = l.toArray(new String[]{});
                        }
                    }
                } catch (JSONException e) {
                    log.error(e.getMessage(), e);
                }
            }
        }
        // si no viene en la request o cookies, crearlo a mano para usar los filtros por default
        if (productFilter == null) productFilter = new ProductFilter();

        // seting default sort
        if (StringUtils.isEmpty(productFilter.getSortedField())) {
            productFilter.setSorted("idProduct:desc");
        }

        if (fieldNames != null && fieldNames.length > 0) {
            listaCampos = new ArrayList<ProductTableField>();
            for (int i = 0; i < fieldNames.length; i++) {
                if (StringUtils.isNotEmpty(fieldNames[i])) {
                    ProductTableField field = new ProductTableField(fieldNames[i], (showField != null && showField.length > i && "Y".equalsIgnoreCase(showField[i])), (editField != null && editField.length > i && "Y".equalsIgnoreCase(editField[i])));
                    listaCampos.add(field);
                }
            }
        } else {
            listaCampos = inicializeFields();
        }
        addToStack("fieldList", listaCampos);
        addToStack("manufacturerFilter", dao.getManufacturersCount() < MAX_MANUFACTURERS);
        addToStack("categoryFilter", dao.getCategoriesCount() < MAX_CATEGORIES);

        addToStack("can_export", actionExist("export_products", "/admin"));

        DataNavigator productlist = new org.store.core.beans.utils.DataNavigator(getRequest(), "productlist");
        if ("selector".equalsIgnoreCase(output)) productlist.setPageRows(10);
        productlist.setListado(dao.listProducts(productlist, productFilter, filterSupplier));
        addToStack("productlist", productlist);
        if (!"selector".equalsIgnoreCase(output)) getResponse().addCookie(productlist.getPageRowCookie());
        categoryTree = new DefaultMutableTreeNode();
        dao.fillCategoryNodeChilds(categoryTree);
        getBreadCrumbs().add(new BreadCrumb(null, getText("admin.product.list"), null, null));
        if ("modal".equalsIgnoreCase(output)) return "modal";
        else if ("selector".equalsIgnoreCase(output)) return "selector";
        else return SUCCESS;
    }

    private List<ProductTableField> inicializeFields() {
        List<ProductTableField> lista = new ArrayList<ProductTableField>();
        lista.add(new ProductTableField("partNumber", true, false));
        lista.add(new ProductTableField("productName", true, false));
        lista.add(new ProductTableField("manufacter", false, false));
        lista.add(new ProductTableField("mfgPartnumber", true, false));
        lista.add(new ProductTableField("category", false, false));
        lista.add(new ProductTableField("basicPrice", false, false));
        lista.add(new ProductTableField("price", true, false));
        lista.add(new ProductTableField("costPrice", false, false));
        lista.add(new ProductTableField("stock", true, false));
        lista.add(new ProductTableField("hits", true, false));
        lista.add(new ProductTableField("active", true, false));
        lista.add(new ProductTableField("archived", false, false));
        lista.add(new ProductTableField("public", false, false));
        List<ProductLabel> listLabel = getLabelList();
        if (listLabel != null) {
            for (ProductLabel label : listLabel) {
                lista.add(new ProductTableField("label." + label.getCode(), false, false));
            }
        }
        return lista;
    }

    public void productlistSave() throws Exception {
        if (productId != null && productId.length > 0) {
            for (int i = 0; i < productId.length; i++) {
                Product bean = (Product) dao.get(Product.class, productId[i]);
                if (bean != null) {
                    if (productActive != null && productActive.length > i) bean.setActive("Y".equalsIgnoreCase(productActive[i]));
                    if (productArchived != null && productArchived.length > i) bean.setArchived("Y".equalsIgnoreCase(productArchived[i]));
                    if (productPartNumber != null && productPartNumber.length > i) bean.setPartNumber(productPartNumber[i]);
                    if (productMfgPartNumber != null && productMfgPartNumber.length > i) bean.setMfgPartnumber(productMfgPartNumber[i]);
                    if (productName != null && productName.length > i) {
                        ProductLang beanL = bean.getLanguage(getDefaultLanguage());
                        if (beanL != null) beanL.setProductName(productName[i]);
                    }
                    if (productStock != null && productStock.length > i) bean.setStock(productStock[i]);
                    if (productPrice != null && productPrice.length > i) bean.setPrice(productPrice[i]);
                    if (productCostPrice != null && productCostPrice.length > i) bean.setCostPrice(productCostPrice[i]);
                    if (productManufacturer != null && productManufacturer.length > i) bean.setManufacturer((Manufacturer) dao.get(Manufacturer.class, productManufacturer[i]));
                    if (productCategory != null && productCategory.length > i) {
                        Category c = (Category) dao.get(Category.class, productCategory[i]);
                        bean.setCategory(c);
                        if (bean.getProductCategories() == null) bean.setProductCategories(new HashSet<Category>());
                        bean.getProductCategories().add(c);
                    }

                    List<ProductLabel> listLabel = getLabelList();
                    if (listLabel != null) {
                        for (ProductLabel label : listLabel) {
                            String[] labelValues = getRequest().getParameterValues("productLabel" + label.getCode());
                            if (labelValues != null && labelValues.length > i) {
                                if ("Y".equalsIgnoreCase(labelValues[i])) bean.addLabel(label);
                                else bean.delLabel(label.getCode());
                            }
                        }
                    }
                    bean.updateCalculatedPrice(dao);
                    dao.save(bean);
                    dao.indexProduct(bean, false);

                    // Audit stock
                    if (productStock != null && productStock.length > i) auditProductStock(bean);
                }
            }

        }
    }

    @Action(value = "productedit", results = @Result(type = "velocity", location = "/WEB-INF/views/admin/productedit.vm"))
    public String productedit() throws Exception {
        addToStack("categoryFilter", dao.getCategoriesCount() < MAX_CATEGORIES);
        addToStack("manufacturerFilter", dao.getManufacturersCount() < MAX_MANUFACTURERS);
        productImages = (product != null) ? getImageResolver().getImagesForProduct(product, "list/") : null;
        getBreadCrumbs().add(new BreadCrumb(null, getText("admin.product.list"), url("productlist", "/admin"), null));
        getBreadCrumbs().add(new BreadCrumb(null, getText(product != null ? product.getProductName(getDefaultLanguage()) : "admin.product.new"), null, null));
        return SUCCESS;
    }

    @Action(value = "productdel", results = @Result(type = "json", params = {"root", "output"}))
    public String productdel() throws Exception {
        if (product != null) {
            String res = dao.isUsedProduct(product);
            if (StringUtils.isNotEmpty(res)) {
                setOutput(getText(CNT_ERROR_CANNOT_DETELE_PRODUCT, CNT_DEFAULT_ERROR_CANNOT_DETELE_PRODUCT, new String[]{product.getProductName(getDefaultLanguage()), res}));
            } else {
                dao.deleteProduct(product);
                dao.indexProduct(product, false);
            }
        }
        return SUCCESS;
    }

    @Action(value = "productsave", results = {
            @Result(type = "redirectAction", location = "productlist"),
            @Result(type = "velocity", name = "input", location = "/WEB-INF/views/admin/productedit.vm"),
            @Result(type = "redirectAction", name = "reedit", location = "productedit?idProduct=${product.idProduct}&opentab=${openTab}")
    })
    public String productsave() throws Exception {
        if (product != null) {
            if (manufacturer != null) product.setManufacturer(manufacturer);
            else product.setManufacturer(null);
            if (category != null) product.setCategory(category);
            product.setInventaryCode(getStoreCode());
            if ((product.getIdProduct() == null || product.getIdProduct() < 1) && category != null) {
                if (product.getProductCategories() == null) product.setProductCategories(new HashSet<Category>());
                product.getProductCategories().add(category);
            }
            boolean newProduct = (product.getIdProduct() == null);
            dao.save(product);
            product.updateCalculatedPrice(dao);
            dao.save(product);
            if (newProduct && "Y".equalsIgnoreCase(getStoreProperty(StoreProperty.PROP_PRODUCT_NEW_LABEL_AUTO, StoreProperty.PROP_DEFAULT_PRODUCT_NEW_LABEL_AUTO))) {
                product.addLabel(dao.getProductLabelByCode(ProductLabel.LABEL_NEW, true));
            }
            if (productName != null && productName.length > 0) {
                for (int l = 0; l < getLanguages().length; l++) {
                    String lang = getLanguages()[l];
                    ProductLang prodLang = dao.getProductLang(product, lang);
                    if (prodLang == null) {
                        prodLang = new ProductLang();
                        prodLang.setProduct(product);
                        prodLang.setProductLang(lang);
                    }
                    prodLang.setProductName(productName[l]);
                    prodLang.setDescription(description[l]);
                    prodLang.setFeatures(features[l]);
                    prodLang.setInformation(information[l]);
                    prodLang.setCaract1(caract1[l]);
                    prodLang.setCaract2(caract2[l]);
                    prodLang.setCaract3(caract3[l]);
                    prodLang.resetMetas();
                    if (metaTitle != null && metaTitle.length > l && StringUtils.isNotEmpty(metaTitle[l])) prodLang.addMeta(PageMeta.META_TITLE, metaTitle[l], metaTitleAppend);
                    if (metaDescription != null && metaDescription.length > l && StringUtils.isNotEmpty(metaDescription[l])) prodLang.addMeta(PageMeta.META_DESCRIPTION, metaDescription[l], metaDescriptionAppend);
                    if (metaKeywords != null && metaKeywords.length > l && StringUtils.isNotEmpty(metaKeywords[l])) prodLang.addMeta(PageMeta.META_KEYWORDS, metaKeywords[l], metaKeywordsAppend);
                    if (metaAbstract != null && metaAbstract.length > l && StringUtils.isNotEmpty(metaAbstract[l])) prodLang.addMeta(PageMeta.META_ABSTRACT, metaAbstract[l], metaAbstractAppend);
                    if (metasName != null && metasName.length > 0 && metasValue != null && metasValue.length == metasName.length) {
                        for (int i = 0; i < metasName.length; i++) {
                            if (StringUtils.isNotEmpty(metasName[i]) && StringUtils.isNotEmpty(metasValue[i])) {
                                prodLang.addMeta(metasName[i], metasValue[i], (metasAppend != null && metasAppend.length > i) ? metasAppend[i] : false);
                            }
                        }
                    }
                    dao.save(prodLang);

                    // Verificar si existen los METAS
                    if (prodLang != null && "Y".equalsIgnoreCase(getStoreProperty(StoreProperty.PROP_AUTOGENERATE_METAS, StoreProperty.PROP_DEFAULT_AUTOGENERATE_METAS))) {
                        String descriptionText = extractText(prodLang.getDescription());
                        if (StringUtils.isEmpty(prodLang.getMetaValue(PageMeta.META_TITLE))) prodLang.addMeta(PageMeta.META_TITLE, prodLang.getProductName(), false);
                        if (StringUtils.isEmpty(prodLang.getMetaValue(PageMeta.META_DESCRIPTION))) prodLang.addMeta(PageMeta.META_DESCRIPTION, descriptionText, false);
                        if (StringUtils.isEmpty(prodLang.getMetaValue(PageMeta.META_KEYWORDS))) prodLang.addMeta(PageMeta.META_KEYWORDS, extractKeywords(prodLang.getProductName() + " " + descriptionText), false);
//                if (StringUtils.isEmpty(prodLang.getMetaValue(PageMeta.META_ABSTRACT))) prodLang.addMeta(PageMeta.META_ABSTRACT,descText,false);
                    }
                }
            }
            dao.updateProductUrlCode(product, seoUrl, getDefaultLanguage());
            dao.indexProduct(product, false);
            // Audit stock
            auditProductStock(product);
        }
        return ("Y".equalsIgnoreCase(reedit)) ? "reedit" : SUCCESS;
    }


    public String productPrice() throws Exception {
        return SUCCESS;
    }

    @Action(value = "productsaveprice", results = {
            @Result(type = "velocity", name = "input", location = "/WEB-INF/views/admin/productedit_price.vm"),
            @Result(type = "velocity", location = "/WEB-INF/views/admin/productedit_price.vm")
    })
    public String productSavePrice() throws Exception {
        if (product != null) {
            dao.save(product);
            dao.indexProduct(product, false);

            // Delete Volume
            for (ProductVolume bean : getProductVolume()) {
                if (ArrayUtils.indexOf(volumen, bean.getVolume()) < 0) dao.delete(bean);
            }
            // Add Volume
            for (int i = 0; i < volumen.length; i++) {
                if (volumen[i] != null && volumenPercent[i] != null) {
                    ProductVolume bean = dao.getProductVolume(product, volumen[i]);
                    if (bean == null) {
                        bean = new ProductVolume();
                        bean.setProduct(product);
                        bean.setVolume(volumen[i]);
                    }
                    bean.setPercentDiscount(volumenPercent[i]);
                    bean.setDescription(volumenDescription[i]);
                    dao.save(bean);
                }
            }
            requestCache.remove("productVolume");

            // Save Customer Level Discount
            if (levelId != null) {
                for (int i = 0; i < levelId.length; i++) {
                    if (levelId[i] != null) {
                        UserLevel uLevel = (UserLevel) dao.get(UserLevel.class, levelId[i]);
                        if (uLevel != null) {
                            ProductUserLevel bean = dao.getProductUserLevel(product, uLevel);
                            if (levelPercent[i] != null) {
                                if (bean == null) {
                                    bean = new ProductUserLevel();
                                    bean.setProduct(product);
                                    bean.setLevel(uLevel);
                                }
                                bean.setPercentDiscount(levelPercent[i]);
                                dao.save(bean);
                            } else if (bean != null) {
                                dao.delete(bean);
                            }
                        }
                    }
                }
            }

            // Save configured providers
            if (providerId != null) {
                for (int i = 0; i < providerId.length; i++) {
                    Provider prov = (Provider) dao.get(Provider.class, providerId[i]);
                    if (prov != null) {
                        ProductProvider bean = dao.getProductProvider(product, prov);
                        if (bean == null) {
                            bean = new ProductProvider();
                            bean.setProduct(product);
                            bean.setProvider(prov);
                            bean.setActive(true);
                        }
                        bean.setActive("Y".equalsIgnoreCase(providerActive[i]));
                        bean.setSku(providerSku[i]);
                        bean.setCostPrice(providerCost[i]);
                        Currency sc = (providerCurrency != null && providerCurrency.length > i && StringUtils.isNotEmpty(providerCurrency[i])) ? dao.getCurrency(providerCurrency[i]) : getDefaultCurrency();
                        bean.setCostCurrency(sc);
                        bean.setStock(providerStock[i]);
                        bean.setEta(SomeUtils.strToDate(providerEta[i], getDefaultLanguage()));
                        dao.save(bean);
                    }
                }
            }

            // Save Promotions
            if (promotionId != null) {
                for (int i = 0; i < promotionId.length; i++) {
                    ProductOffer bean = (promotionId[i] != null) ? (ProductOffer) dao.get(ProductOffer.class, promotionId[i]) : null;
                    if (promotionPercent[i] != null || promotionValue[i] != null) {
                        if (bean == null) bean = new ProductOffer();
                        bean.setRuleObject(ProductOffer.RULE_PRODUCT);
                        bean.setRuleOperation(ProductOffer.OPER_EQUAL);
                        bean.setRuleValue(product.getIdProduct().toString());
                        bean.setDateFrom(SomeUtils.strToDate(promotionFrom[i], getDefaultLanguage()));
                        bean.setDateTo(SomeUtils.strToDate(promotionTo[i], getDefaultLanguage()));
                        bean.setDiscount(promotionValue[i]);
                        bean.setPercent(promotionPercent[i]);
                        bean.setInventaryCode(getStoreCode());
                        dao.save(bean);
                    } else {
                        if (bean != null && bean.isForProduct(product)) dao.delete(bean);
                    }
                }
            }

            // Shipping rates
            if (shippingState != null && shippingState.length > 0) {
                for (int i = 0; i < shippingState.length; i++) {
                    Long sState = shippingState[i];
                    State state = (State) dao.get(State.class, sState);
                    String sType = (shippingType != null && shippingType.length > i) ? shippingType[i] : null;
                    Double sValue = (shippingValue != null && shippingValue.length > i) ? shippingValue[i] : null;
                    if (state != null) {
                        ShippingRate bean = dao.getProductShipping(product, state);
                        if (sValue != null) {
                            if (bean == null) bean = new ShippingRate();
                            bean.setProduct(product);
                            bean.setCategory(null);
                            bean.setState(state);
                            bean.setValue(sValue);
                            bean.setShippingType(sType);
                            bean.setInventaryCode(getStoreCode());
                            dao.save(bean);
                        } else if (bean != null) {
                            dao.delete(bean);
                        }
                    }
                }
            }

        }
        return productPrice();
    }

    @Action(value = "productdelprovider", results = {
            @Result(type = "velocity", name = "input", location = "/WEB-INF/views/admin/productedit_price.vm"),
            @Result(type = "velocity", location = "/WEB-INF/views/admin/productedit_price.vm")
    })
    public String productDelProductProvider() throws Exception {
        if (product != null && providerId != null) {
            for (Long idProv : providerId) {
                Provider prov = (Provider) dao.get(Provider.class, idProv);
                if (prov != null) {
                    ProductProvider bean = dao.getProductProvider(product, prov);
                    if (bean != null) dao.delete(bean);
                }
            }
        }
        return productPrice();
    }

    @Action(value = "productproccessproviders", results = {
            @Result(type = "velocity", name = "input", location = "/WEB-INF/views/admin/productedit_price.vm"),
            @Result(type = "json", params = {"root", "jsonResult"})
    })
    public String productProcessProviders() throws Exception {
        jsonResult = new HashMap<String, Object>();
        if (product != null) {
            Map<String, Class> map;
            synchronized (getServletContext()) {
                map = Store20Config.getInstance(getServletContext()).getMapSuplier();
            }
            SupplierUtils su = new SupplierUtils(map, dao.gethSession(), getStoreCode(), getDatabaseConfig());
            su.setName("supplier_task_" + String.valueOf(Calendar.getInstance().getTimeInMillis()));
            try {
                su.setOnlyProduct(product.getIdProduct());
                su.start();
                jsonResult.put("name", su.getName());
            } catch (Exception e) {
                LOG.error(e.getMessage());
                jsonResult.put("error", e.getMessage());
                log.error(e.getMessage(), e);
            }
        } else {
            jsonResult.put("error", "Product not found");
        }
        return SUCCESS;

    }

    @Action(value = "productproccessproviderstate", results = @Result(type = "json", params = {"root", "jsonResult"}))
    public String getProcessProviderState() throws Exception {
        jsonResult = new HashMap<String, Object>();
        if (StringUtils.isNotEmpty(taskName)) {
            Thread th = ThreadUtilities.getThread(taskName);
            if (th != null && th instanceof JobStoreThread) {
                JobStoreThread sth = (JobStoreThread) th;
                jsonResult.put("msg", sth.getExecutionMessage());
                jsonResult.put("percent", sth.getExecutionPercent());
            } else {
                jsonResult.put("msg", "FINISHED");
                jsonResult.put("percent", 100);
            }
        } else {
            jsonResult.put("error", "Task not found");
        }
        return SUCCESS;
    }

    @Action(value = "productsaveimages", results = {
            @Result(type = "velocity", name = "input", location = "/WEB-INF/views/admin/productedit_images.vm"),
            @Result(type = "velocity", location = "/WEB-INF/views/admin/productedit_images.vm")
    })
    public String productSaveImages() throws Exception {
        if (product != null && file != null) {
            ImageResolver ir = getImageResolver();
            for (int i = 0; i < file.length; i++) {
                if (file[i] != null && StringUtils.isNotEmpty(fileFileName[i])) {
                    if (ir.validExtension(fileFileName[i])) {
                        if (!ir.processImage(product, file[i], FilenameUtils.getExtension(fileFileName[i]))) {
                            addFlash("images_errors", getText(CNT_ERROR_IMAGE_CANNOT_BE_PROCESSED, CNT_DEFAULT_ERROR_IMAGE_CANNOT_BE_PROCESSED, new String[]{fileFileName[i], ir.getLastError()}));
                        }
                    } else {
                        addFlash("images_errors", getText(CNT_ERROR_IMAGE_CANNOT_BE_PROCESSED, CNT_DEFAULT_ERROR_IMAGE_CANNOT_BE_PROCESSED, new String[]{fileFileName[i], "Invalid file extension"}));
                    }
                }
            }
        }
        productImages = (product != null) ? getImageResolver().getImagesForProduct(product, "list/") : null;
        return SUCCESS;
    }

    @Action(value = "complementsaveimages", results = {
            @Result(type = "velocity", name = "input", location = "/WEB-INF/views/admin/complementedit_images.vm"),
            @Result(type = "velocity", location = "/WEB-INF/views/admin/complementedit_images.vm")
    })
    public String complementSaveImages() throws Exception {
        return productSaveImages();
    }

    @Action(value = "productsaveimagesex", results = {
            @Result(type = "redirectAction", name = "input", location = "productedit?idProduct=${product.idProduct}&openTab=3"),
            @Result(type = "redirectAction", location = "productedit?idProduct=${product.idProduct}&openTab=3")
    })
    public String productSaveImagesEx() throws Exception {
        return productSaveImages();
    }

    @Action(value = "productdelimages", results = {
            @Result(type = "velocity", name = "input", location = "/WEB-INF/views/admin/productedit_images.vm"),
            @Result(type = "velocity", location = "/WEB-INF/views/admin/productedit_images.vm")
    })
    public String productDelImages() throws Exception {
        if (product != null && StringUtils.isNotEmpty(deleteImg)) {
            ImageResolver ir = getImageResolver();
            ir.deleteImages(product, deleteImg);
        }
        productImages = (product != null) ? getImageResolver().getImagesForProduct(product, "list/") : null;
        return SUCCESS;
    }

    @Action(value = "complementdelimages", results = {
            @Result(type = "velocity", name = "input", location = "/WEB-INF/views/admin/complementedit_images.vm"),
            @Result(type = "velocity", location = "/WEB-INF/views/admin/complementedit_images.vm")
    })
    public String complementDelImages() throws Exception {
        return productDelImages();
    }

    @Action(value = "productmainimage", results = @Result(type = "json", params = {"root", "mainImage"}))
    public String productMainImage() throws Exception {
        if (product != null && StringUtils.isNotEmpty(mainImage)) {
            product.setMainImage(mainImage);
            dao.save(product);
        }
        return SUCCESS;
    }


    @Action(value = "complementmainimage", results = @Result(type = "json", params = {"root", "mainImage"}))
    public String complementMainImage() throws Exception {
        return productMainImage();
    }

    @Action(value = "productaddlabels", results = {
            @Result(type = "velocity", name = "input", location = "/WEB-INF/views/admin/productedit_labels.vm"),
            @Result(type = "velocity", location = "/WEB-INF/views/admin/productedit_labels.vm")
    })
    public String productAddLabels() throws Exception {
        if (product != null && labels != null) {
            for (String l : labels) {
                ProductLabel bean = dao.getProductLabelByCode(l);
                if (product.getLabels() == null) product.setLabels(new HashSet<ProductLabel>());
                if (bean != null && !product.getLabels().contains(bean)) {
                    product.getLabels().add(bean);
                    dao.save(product);
                }
            }
        }
        return SUCCESS;
    }

    @Action(value = "productdellabels", results = {
            @Result(type = "velocity", name = "input", location = "/WEB-INF/views/admin/productedit_labels.vm"),
            @Result(type = "velocity", location = "/WEB-INF/views/admin/productedit_labels.vm")
    })
    public String productDelLabels() throws Exception {
        if (product != null && labels != null) {
            for (String l : labels) {
                ProductLabel bean = dao.getProductLabelByCode(l);
                if (product.getLabels() == null) product.setLabels(new HashSet<ProductLabel>());
                if (bean != null && product.getLabels().contains(bean)) {
                    product.getLabels().remove(bean);
                    dao.save(product);
                }
            }
        }
        return SUCCESS;
    }

    @Action(value = "productaddcategory", results = {
            @Result(type = "velocity", name = "input", location = "/WEB-INF/views/admin/productedit_labels.vm"),
            @Result(type = "velocity", location = "/WEB-INF/views/admin/productedit_labels.vm")
    })
    public String productAddCategory() throws Exception {
        if (product != null && category != null) {
            if (product.getProductCategories() == null) product.setProductCategories(new HashSet<Category>());
            if (!product.getProductCategories().contains(category)) {
                product.getProductCategories().add(category);
                dao.save(product);
            }
        }
        return SUCCESS;
    }

    @Action(value = "productdelcategory", results = {
            @Result(type = "velocity", name = "input", location = "/WEB-INF/views/admin/productedit_labels.vm"),
            @Result(type = "velocity", location = "/WEB-INF/views/admin/productedit_labels.vm")
    })
    public String productDelCategory() throws Exception {
        if (product != null && product.getProductCategories() != null && category != null) {
            if (product.getProductCategories().contains(category)) {
                product.getProductCategories().remove(category);
                dao.save(product);
            }
        }
        return SUCCESS;
    }

    @Action(value = "productaddrelated", results = {
            @Result(type = "velocity", name = "input", location = "/WEB-INF/views/admin/productedit_related.vm"),
            @Result(type = "velocity", location = "/WEB-INF/views/admin/productedit_related.vm")
    })
    public String productAddRelated() throws Exception {
        if (product != null && related != null) {
            for (Long l : related) {
                Product beanRelated = (Product) dao.get(Product.class, l);
                if (beanRelated != null && !beanRelated.equals(product)) {
                    ProductRelated bean = dao.getProductRelated(product, beanRelated);
                    if (bean == null) {
                        bean = new ProductRelated();
                        bean.setProduct(product);
                        bean.setRelated(beanRelated);
                        dao.save(bean);
                    }
                }
            }
        }
        return SUCCESS;
    }

    @Action(value = "productdelrelated", results = {
            @Result(type = "velocity", name = "input", location = "/WEB-INF/views/admin/productedit_related.vm"),
            @Result(type = "velocity", location = "/WEB-INF/views/admin/productedit_related.vm")
    })
    public String productDelRelated() throws Exception {
        if (product != null && related != null) {
            for (Long l : related) {
                Product beanRelated = (Product) dao.get(Product.class, l);
                ProductRelated bean = (ProductRelated) dao.getProductRelated(product, beanRelated);
                if (bean != null) dao.delete(bean);
            }
        }
        return SUCCESS;
    }

    @Action(value = "productsaverelated", results = {
            @Result(type = "velocity", name = "input", location = "/WEB-INF/views/admin/productedit_related.vm"),
            @Result(type = "velocity", location = "/WEB-INF/views/admin/productedit_related.vm")
    })
    public String productSaveRelated() throws Exception {
        if (product != null && related != null) {
            for (int i = 0; i < related.length; i++) {
                Product beanRelated = (Product) dao.get(Product.class, related[i]);
                ProductRelated bean = (ProductRelated) dao.getProductRelated(product, beanRelated);
                if (bean != null) {
                    bean.setCombinedPrice((productPrice != null && productPrice.length > i) ? productPrice[i] : null);
                    dao.save(bean);
                }
            }
        }
        return SUCCESS;
    }

    @Action(value = "productaddreview", results = {
            @Result(type = "velocity", name = "input", location = "/WEB-INF/views/admin/productedit_review.vm"),
            @Result(type = "velocity", location = "/WEB-INF/views/admin/productedit_review.vm")
    })
    public String productAddReview() throws Exception {
        if (product != null && review != null) {
            review.setProduct(product);
            if (review.getCreated() == null) review.setCreated(Calendar.getInstance().getTime());
            dao.save(review);
        }
        return SUCCESS;
    }

    @Action(value = "productsavereviews", results = {
            @Result(type = "velocity", name = "input", location = "/WEB-INF/views/admin/productedit_review.vm"),
            @Result(type = "velocity", location = "/WEB-INF/views/admin/productedit_review.vm")
    })
    public String productSaveReviews() throws Exception {
        if (product != null && reviewId != null && reviewId.length > 0) {
            for (int i = 0; i < reviewId.length; i++) {
                ProductReview bean = (ProductReview) dao.get(ProductReview.class, reviewId[i]);
                if (bean != null && reviewVisible.length > i) {
                    bean.setVisible("Y".equalsIgnoreCase(reviewVisible[i]));
                    dao.save(bean);
                }
            }
            dao.updateAverageScore(product);
            dao.save(product);
        }
        return SUCCESS;
    }

    @Action(value = "productresourceadd", results = {
            @Result(type = "velocity", name = "input", location = "/WEB-INF/views/admin/productedit_statictexts.vm"),
            @Result(type = "velocity", location = "/WEB-INF/views/admin/productedit_statictexts.vm")
    })
    public String productResourceAdd() throws Exception {
        if (product != null) {
            Set<Resource> set = new HashSet<Resource>();
            if (selectedResource != null && selectedResource.length > 0) {
                for (Long idRes : selectedResource) {
                    Resource res = (Resource) dao.get(Resource.class, idRes);
                    if (res != null) set.add(res);
                }
            }
            product.setProductResources(set);
        }
        return SUCCESS;
    }

    @Action(value = "productstatictextsadd", results = {
            @Result(type = "velocity", name = "input", location = "/WEB-INF/views/admin/productedit_statictexts.vm"),
            @Result(type = "velocity", location = "/WEB-INF/views/admin/productedit_statictexts.vm")
    })
    public String productStaticTextAdd() throws Exception {
        if (product != null) {
            // Eliminar los q no estan
            List<ProductStaticText> productStaticTexts = dao.getProductStaticText(product);
            if (productStaticTexts != null) {
                for (ProductStaticText bean : productStaticTexts) {
                    if (ArrayUtils.indexOf(selectedStaticText, bean.getStaticText().getId()) < 0)
                        dao.delete(bean);
                }
            }
            // Adicionar
            if (selectedStaticText != null) {
                for (Long idST : selectedStaticText) {
                    StaticText beanST = (StaticText) dao.get(StaticText.class, idST);
                    if (beanST != null) {
                        ProductStaticText bean = dao.getProductStaticText(product, beanST);
                        if (bean == null) {
                            bean = new ProductStaticText();
                            bean.setProduct(product);
                            bean.setStaticText(beanST);
                            dao.save(bean);
                        }
                    }
                }
            }
            requestCache.remove("PRODUCT_STATIC_TEXT");
        }
        return SUCCESS;
    }

    @Action(value = "productstatictextssave", results = {
            @Result(type = "velocity", name = "input", location = "/WEB-INF/views/admin/productedit_statictexts.vm"),
            @Result(type = "velocity", location = "/WEB-INF/views/admin/productedit_statictexts.vm")
    })
    public String productStaticTextSave() throws Exception {
        if (product != null && contentStaticTextId != null) {
            for (int i = 0; i < contentStaticTextId.length; i++) {
                ProductStaticText bean = (contentStaticTextId[i] != null) ? (ProductStaticText) dao.get(ProductStaticText.class, contentStaticTextId[i]) : null;
                if (bean != null) {
                    bean.setProduct(product);
                    bean.setContentOrder(contentOrder[i]);
                    bean.setContentPlace(contentPlace[i]);
                    dao.save(bean);
                }
            }
            requestCache.remove("PRODUCT_STATIC_TEXT");
        }
        return SUCCESS;
    }

    @Action(value = "productdelreview", results = {
            @Result(type = "velocity", name = "input", location = "/WEB-INF/views/admin/productedit_review.vm"),
            @Result(type = "velocity", location = "/WEB-INF/views/admin/productedit_review.vm")
    })
    public String productDelReview() throws Exception {
        if (product != null && review != null) {
            dao.delete(review);
        }
        return SUCCESS;
    }

    @Action(value = "productsaveproperties", results = {
            @Result(type = "velocity", name = "input", location = "/WEB-INF/views/admin/productedit_properties.vm"),
            @Result(type = "velocity", location = "/WEB-INF/views/admin/productedit_properties.vm")
    })
    public String productSaveProperties() throws Exception {
        if (product != null) {
            // Save properties
            if (attributeId != null && attributeId.length > 0) {
                for (int i = 0; i < attributeId.length; i++) {
                    AttributeProd attribute = (AttributeProd) dao.get(AttributeProd.class, attributeId[i]);
                    ProductProperty beanPP = dao.getProductProperty(product, attribute);
                    if (StringUtils.isNotEmpty(propertyValue[i])) {
                        if (beanPP == null && attribute != null) {
                            beanPP = new ProductProperty();
                            beanPP.setProduct(product);
                            beanPP.setAttribute(attribute);
                        }
                        if (beanPP != null) {
                            beanPP.setPropertyValue(propertyValue[i]);
                            dao.save(beanPP);
                        }
                    } else if (beanPP != null)
                        dao.delete(beanPP);
                }
            }

            // Add new property
            if (attributeNew != null) {
                AttributeProd attribute = (AttributeProd) dao.get(AttributeProd.class, attributeNew);
                ProductProperty beanPP = dao.getProductProperty(product, attribute);
                if (beanPP == null && attribute != null) {
                    beanPP = new ProductProperty();
                    beanPP.setProduct(product);
                    beanPP.setAttribute(attribute);
                }
                if (beanPP != null) {
                    beanPP.setPropertyValue(attributeValue);
                    dao.save(beanPP);
                }

            }

            // Options
            if (variationId != null) {
                for (int i = 0; i < variationId.length; i++) {
                    ProductVariation bean = (ProductVariation) dao.get(ProductVariation.class, variationId[i]);
                    String vc1 = (variationCaract1 != null && variationCaract1.length > i) ? variationCaract1[i] : null;
                    String vc2 = (variationCaract2 != null && variationCaract2.length > i) ? variationCaract2[i] : null;
                    String vc3 = (variationCaract3 != null && variationCaract3.length > i) ? variationCaract3[i] : null;
                    if (StringUtils.isNotEmpty(vc1) || StringUtils.isNotEmpty(vc2) || StringUtils.isNotEmpty(vc3)) {
                        if (bean == null) bean = new ProductVariation();
                        bean.setCaract1(vc1);
                        bean.setCaract2(vc2);
                        bean.setCaract3(vc3);
                        bean.setStock(variationStock[i]);
                        bean.setPriceInc(variationPrice[i]);
                        bean.setCostPriceInc(variationCost[i]);
                        bean.setDimentionWidth(variationWidth[i]);
                        bean.setDimentionHeight(variationHeight[i]);
                        bean.setDimentionLength(variationLength[i]);
                        bean.setWeight(variationWeight[i]);
                        bean.setProduct(product);
                        dao.save(bean);
                    } else if (bean != null) {
                        dao.delete(bean);
                    }
                }
            }
        }
        return SUCCESS;
    }

    public List<Map<String, Object>> getProductAttributes() {
        if (product == null) return null;

        List<Map<String, Object>> productAttributes = new ArrayList<Map<String, Object>>();
        List<AttributeProd> included = new ArrayList<AttributeProd>();
        // Listar los que ya tienen valor en el producto
        if (getProductProperties() != null)
            for (ProductProperty pp : getProductProperties()) {
                Map<String, Object> m = new HashMap<String, Object>();
                m.put("PP_ID", pp.getId());
                m.put("PP_VALUE", pp.getPropertyValue());
                m.put("ATTR", pp.getAttribute());
                productAttributes.add(m);
                included.add(pp.getAttribute());
            }
        // Adicionar los definidos en la categoria
        List<AttributeProd> filteredInCategory = new ArrayList<AttributeProd>();
        if (getParentCategoryProperties() != null)
            for (CategoryProperty cp : getParentCategoryProperties()) {
                if (cp.getCanfilter()) filteredInCategory.add(cp.getAttribute());
                if (!included.contains(cp.getAttribute())) {
                    Map<String, Object> m = new HashMap<String, Object>();
                    m.put("ATTR", cp.getAttribute());
                    productAttributes.add(m);
                }
            }
        // Marcar los q se filtran en la categoria
        if (!filteredInCategory.isEmpty())
            for (Map<String, Object> m : productAttributes) {
                AttributeProd ap = (AttributeProd) m.get("ATTR");
                if (ap != null && filteredInCategory.contains(ap)) m.put("FILTERED", "Y");
            }
        // Ordenar por nombre
        Collections.sort(productAttributes, new Comparator<Map<String, Object>>() {
            public int compare(Map<String, Object> m1, Map<String, Object> m2) {
                AttributeProd o1 = (AttributeProd) m1.get("ATTR");
                AttributeProd o2 = (AttributeProd) m2.get("ATTR");
                String l1 = (o1 != null && o1.getAttributeName(getDefaultLanguage()) != null) ? o1.getAttributeName(getDefaultLanguage()) : "";
                String l2 = (o2 != null && o2.getAttributeName(getDefaultLanguage()) != null) ? o2.getAttributeName(getDefaultLanguage()) : "";
                return l1.compareTo(l2);
            }
        });
        requestCache.remove("PRODUCT_PROPERTIES");
        requestCache.remove("parentCategoryProperties");
        return productAttributes;
    }

    @Action(value = "productupdateurl")
    public String updateCodeNames() throws Exception {
        int oks = 0;
        int errors = 0;
        List<Product> listado = dao.getProducts();

        for (Product bean : listado) {
            try {
                if (dao.updateProductUrlCode(bean, null, getDefaultLanguage())) oks++;
                else errors++;
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
        response.getWriter().println("Products OK: " + String.valueOf(oks));
        response.getWriter().println("Products Error: " + String.valueOf(errors));
        return null;
    }

    /* Procesar el html y sacar los datos de la table */

    @Action(value = "importattributes1", results = @Result(type = "velocity", location = "/WEB-INF/views/admin/importattributes1.vm"))
    public String importAttributesStep1() throws Exception {
        if (StringUtils.isNotEmpty(htmlContent)) {
            TableParser parser = new TableParser();
            Reader reader = new StringReader(htmlContent);
            parser.parse(reader);
            int maxCol = 0;
            for (Object ot : parser) {
                TableParser.HTMLTable t = (TableParser.HTMLTable) ot;
                for (Object or : t) {
                    TableParser.HTMLRow r = (TableParser.HTMLRow) or;
                    if (r.size() > maxCol) maxCol = r.size();
                }
            }
            if (maxCol > 1) {
                addToStack("tableToImport", parser);
                addToStack("tableColumns", maxCol);
            }
        }
        return SUCCESS;
    }

    @Action(value = "importattributes2", results = @Result(type = "velocity", location = "/WEB-INF/views/admin/importattributes2.vm"))
    public String importAttributesStep2() throws Exception {
        if (fieldAction != null && fieldAction.length > 1) {
            String[] names = null;
            String[] values = null;
            for (int col = 0; col < fieldAction.length; col++) {
                if ("name".equalsIgnoreCase(fieldAction[col])) {
                    names = getRequest().getParameterValues("column" + String.valueOf(col));
                } else if ("value".equalsIgnoreCase(fieldAction[col])) {
                    values = getRequest().getParameterValues("column" + String.valueOf(col));
                }
            }
            if (names != null && values != null && names.length > 0 && names.length == values.length) {
                List<Map<String, String>> attList = new ArrayList<Map<String, String>>();
                for (int row = 0; row < names.length; row++)
                    if (StringUtils.isNotEmpty(names[row])) {
                        AttributeProd att = dao.getAttributeByName(getDefaultLanguage(), names[row]);
                        Map<String, String> map = new HashMap<String, String>();
                        map.put("name", names[row]);
                        map.put("value", values[row]);
                        map.put("att", (att != null) ? "Y" : "N");
                        attList.add(map);
                    }
                addToStack("attributes", attList);
            }
        }
        return SUCCESS;
    }

    @Action(value = "importattributes3", results = @Result(type = "redirectAction", location = "productedit?idProduct=${product.idProduct}&openTab=${openTab}"))
    public String importAttributesStep3() throws Exception {
        if (product != null && !ArrayUtils.isEmpty(attImport) && !ArrayUtils.isEmpty(attImportName) && !ArrayUtils.isEmpty(attImportValue) && ArrayUtils.isSameLength(attImport, attImportName) && ArrayUtils.isSameLength(attImport, attImportValue)) {
            for (int i = 0; i < attImport.length; i++) {
                if ("Y".equalsIgnoreCase(attImport[i]) && StringUtils.isNotEmpty(attImportName[i])) {
                    AttributeProd att = dao.getAttributeByName(getDefaultLanguage(), attImportName[i]);
                    if (att == null) {
                        att = new AttributeProd();
                        att.setAttributeGroup("");
                        att.setInventaryCode(getStoreCode());
                        for (String lang : getLanguages()) att.setAttributeName(lang, attImportName[i]);
                        dao.save(att);
                    }

                    ProductProperty pp = dao.getProductProperty(product, att);
                    if (pp == null) {
                        pp = new ProductProperty();
                        pp.setAttribute(att);
                        pp.setProduct(product);
                    }
                    pp.setPropertyValue(attImportValue[i]);
                    dao.save(pp);

                    if (category != null) {
                        CategoryProperty cp = dao.getCategoryProperty(category, att);
                        if (cp == null) {
                            cp = new CategoryProperty();
                            cp.setAttribute(att);
                            cp.setCategory(category);
                            cp.setCanfilter(false);
                            dao.save(cp);
                        }
                    }
                }
            }
        }
        return SUCCESS;
    }

    @Action(value = "complementgrouplist", results = @Result(type = "velocity", location = "/WEB-INF/views/admin/complementgrouplist.vm"))
    public String complementGroupList() throws Exception {
        if (selecteds != null && selecteds.length > 0) {
            for (Long id : selecteds) {
                ComplementGroup bean = (ComplementGroup) dao.get(ComplementGroup.class, id);
                if (bean != null) {
                    String res = dao.isUsedComplementGroup(bean);
                    if (StringUtils.isNotEmpty(res)) {
                        addActionError(getText(CNT_ERROR_CANNOT_DETELE_COMPLEMENTGROUP, CNT_DEFAULT_ERROR_CANNOT_DETELE_COMPLEMENTGROUP, new String[]{bean.getGroupName(getDefaultLanguage()), res}));
                    } else {
                        dao.delete(bean);
                    }
                }
            }
        }
        getBreadCrumbs().add(new BreadCrumb(null, getText("admin.complement.group.list"), null, null));
        return SUCCESS;
    }

    @Action(value = "complementgroupedit", results = @Result(type = "velocity", location = "/WEB-INF/views/admin/complementgroupedit.vm"))
    public String complementGroupEdit() throws Exception {
        getBreadCrumbs().add(new BreadCrumb(null, getText("admin.complement.group.list"), url("complementgrouplist", "/admin"), null));
        getBreadCrumbs().add(new BreadCrumb(null, getText(group != null ? "admin.complement.group.modify" : "admin.complement.group.new"), null, null));
        return SUCCESS;
    }

    @Action(value = "complementgroupsave", results = @Result(type = "redirectAction", location = "complementgrouplist"))
    public String complementGroupSave() throws Exception {
        if (group != null) {
            if (groupName != null && ArrayUtils.isSameLength(getLanguages(), groupName)) {
                for (int i = 0; i < groupName.length; i++)
                    group.setGroupName(getLanguages()[i], groupName[i]);
            }
            group.setInventaryCode(getStoreCode());
            dao.save(group);
        }
        return SUCCESS;
    }

    @Action(value = "complementlist", results = @Result(type = "velocity", location = "/WEB-INF/views/admin/complementlist.vm"))
    public String complementList() throws Exception {
        if (group != null) {
            if (selecteds != null && selecteds.length > 0) {
                for (Long id : selecteds) {
                    Product bean = (Product) dao.get(Product.class, id);
                    if (bean != null) {
                        String res = dao.isUsedProduct(bean);
                        if (StringUtils.isNotEmpty(res)) {
                            addActionError(getText(CNT_ERROR_CANNOT_DETELE_COMPLEMENT, CNT_DEFAULT_ERROR_CANNOT_DETELE_COMPLEMENT, new String[]{bean.getProductName(getDefaultLanguage()), res}));
                        } else {
                            dao.deleteProduct(bean);
                        }
                    }
                }
                dao.flushSession();
            }
            getBreadCrumbs().add(new BreadCrumb(null, getText("admin.complement.group.list"), url("complementgrouplist", "/admin"), null));
            addToStack("products", dao.getComplements(group, false));
        }
        getBreadCrumbs().add(new BreadCrumb(null, getText("admin.complement.list"), null, null));
        return SUCCESS;
    }

    @Action(value = "complementedit", results = @Result(type = "velocity", location = "/WEB-INF/views/admin/complementedit.vm"))
    public String complementEdit() throws Exception {
        if (product != null && !Product.TYPE_COMPLEMENT.equalsIgnoreCase(product.getProductType())) product = null;
        productImages = (product != null) ? getImageResolver().getImagesForProduct(product, "list/") : null;
        getBreadCrumbs().add(new BreadCrumb(null, getText("admin.complement.group.list"), url("complementgrouplist", "/admin"), null));
        Map<String, String> params = new HashMap<String, String>();
        if (idGroup != null) params.put("idGroup", idGroup.toString());
        getBreadCrumbs().add(new BreadCrumb(null, getText("admin.complement.list"), url("complementlist", "/admin", params), null));
        getBreadCrumbs().add(new BreadCrumb(null, getText(product != null ? "admin.complement.modify" : "admin.complement.new"), null, null));
        return SUCCESS;
    }

    @Action(value = "complementsave", results = {
            @Result(type = "velocity", name = "input", location = "/WEB-INF/views/admin/complementedit.vm"),
            @Result(type = "redirectAction", location = "complementlist?idGroup=${group.idGroup}")
    })
    public String complementSave() throws Exception {
        if (product != null && group != null) {
            product.setProductType(Product.TYPE_COMPLEMENT);
            product.setComplementGroup(group);
            if (manufacturer != null) product.setManufacturer(manufacturer);
            if (category != null) product.setCategory(category);
            product.setInventaryCode(getStoreCode());
            dao.save(product);
            if (productName != null && productName.length > 0) {
                for (int l = 0; l < getLanguages().length; l++) {
                    String lang = getLanguages()[l];
                    ProductLang prodLang = dao.getProductLang(product, lang);
                    if (prodLang == null) {
                        prodLang = new ProductLang();
                        prodLang.setProduct(product);
                        prodLang.setProductLang(lang);
                    }
                    prodLang.setProductName(productName[l]);
                    prodLang.setDescription(description[l]);
                    dao.save(prodLang);
                }
            }
            dao.updateProductUrlCode(product, seoUrl, getDefaultLanguage());

            // Audit stock
            auditProductStock(product);
        }
        return SUCCESS;
    }

    @Action(value = "productcomplementadd", results = @Result(type = "velocity", location = "/WEB-INF/views/admin/productedit_complements.vm"))
    public String productComplementAdd() throws Exception {
        if (product != null && groupId != null && groupId.length > 0) {
            if (product.getRelatedGroups() == null) product.setRelatedGroups(new HashSet<ComplementGroup>());
            for (Long id : groupId) {
                ComplementGroup group = (ComplementGroup) dao.get(ComplementGroup.class, id);
                if (group != null && !product.getRelatedGroups().contains(group)) {
                    product.getRelatedGroups().add(group);
                }
            }
            dao.save(product);
        }
        if (product != null && levelId != null && levelId.length > 0) {
            if (product.getForUsers() == null) product.setForUsers(new HashSet<Long>());
            for (Long id : levelId) {
                UserLevel level = (UserLevel) dao.get(UserLevel.class, id);
                if (level != null && !product.getForUsers().contains(id)) {
                    product.getForUsers().add(level.getId());
                }
            }
            dao.save(product);
        }
        return SUCCESS;
    }

    @Action(value = "productcomplementdel", results = @Result(type = "velocity", location = "/WEB-INF/views/admin/productedit_complements.vm"))
    public String productComplementDel() throws Exception {
        if (product != null && product.getRelatedGroups() != null && groupId != null && groupId.length > 0) {
            for (Long id : groupId) {
                ComplementGroup group = (ComplementGroup) dao.get(ComplementGroup.class, id);
                if (group != null && product.getRelatedGroups().contains(group)) {
                    product.getRelatedGroups().remove(group);
                }
            }
            dao.save(product);
        }
        if (product != null && product.getForUsers() != null && levelId != null && levelId.length > 0) {
            for (Long id : levelId) {
                UserLevel level = (UserLevel) dao.get(UserLevel.class, id);
                if (level != null && product.getForUsers().contains(id)) {
                    product.getForUsers().remove(id);
                }
            }
            dao.save(product);
        }
        return SUCCESS;
    }

    @Action(value = "productproccessimages", results = {
            @Result(type = "velocity", name = "input", location = "/WEB-INF/views/admin/productproccessimages.vm"),
            @Result(type = "velocity", location = "/WEB-INF/views/admin/productproccessimages.vm")
    })
    public String productProccessImages() throws Exception {
        File dir = new File(getServletContext().getRealPath("/stores/" + getStoreCode() + "/" + PATH_MASSIVE_UPLOAD));
        if (dir.exists() && dir.isDirectory()) {
            final ImageResolver ir = getImageResolver();

            // Procesar imagenes
            if (productPartNumber != null && productImage != null && ArrayUtils.isSameLength(productPartNumber, productImage)) {
                int processed = 0;
                for (int i = 0; i < productPartNumber.length; i++) {
                    Product p = dao.getProductByPartNumber(productPartNumber[i]);
                    if (p == null) p = dao.getProductByMfgPartNumber(productPartNumber[i]);
                    File image = new File(getServletContext().getRealPath("/stores/" + getStoreCode() + "/" + PATH_MASSIVE_UPLOAD + "/" + productImage[i]));
                    if (p != null && image.exists() && image.isFile()) {
                        if (ir.processImage(p, image, FilenameUtils.getExtension(productImage[i]))) {
                            if (image.exists()) FileUtils.forceDelete(image);
                            processed++;
                        } else {
                            addActionError(getText(CNT_ERROR_IMAGE_CANNOT_BE_PROCESSED, CNT_DEFAULT_ERROR_IMAGE_CANNOT_BE_PROCESSED, new String[]{productImage[i], ir.getLastError()}));
                        }
                    }
                }
                addToStack("processed", processed);
            }


            // Verificar imagenes nuevas
            String[] listFiles = dir.list(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return ir.validExtension(name);
                }
            });
            if (listFiles != null) {
                Arrays.sort(listFiles);
                List<Map<String, Object>> listOk = new ArrayList<Map<String, Object>>();
                List<String> listErr = new ArrayList<String>();
                for (String fn : listFiles) {
                    String cad = StringUtils.substringBeforeLast(fn, ".");
                    String cad1 = StringUtils.substringBeforeLast(cad, "_");
                    String cad2 = StringUtils.substringBeforeLast(cad, "-");
                    Product p = dao.getProductByPartNumber(cad);
                    if (p == null) p = dao.getProductByMfgPartNumber(cad);
                    if (p == null && StringUtils.isNotEmpty(cad1)) p = dao.getProductByPartNumber(cad1);
                    if (p == null && StringUtils.isNotEmpty(cad1)) p = dao.getProductByMfgPartNumber(cad1);
                    if (p == null && StringUtils.isNotEmpty(cad2)) p = dao.getProductByPartNumber(cad2);
                    if (p == null && StringUtils.isNotEmpty(cad2)) p = dao.getProductByMfgPartNumber(cad2);
                    if (p != null) {
                        Map<String, Object> map = new HashMap<String, Object>();
                        map.put("file", fn);
                        map.put("product", p);
                        listOk.add(map);
                    } else {
                        listErr.add(fn);
                    }
                }
                addToStack("listOk", listOk);
                addToStack("listErr", listErr);
            }
        }
        getBreadCrumbs().add(new BreadCrumb(null, getText("admin.product.massive.image.upload"), null, null));
        return SUCCESS;
    }

    @Action("productmassiveupload")
    public String productmassiveupload() throws Exception
    {
        String folder = getServletContext().getRealPath(File.separator + "stores" + File.separator + getStoreCode() + File.separator + "uploads");
        FileUtils.forceMkdir(new File(folder));
        if (this.file != null) {
            for (int i = 0; i < this.file.length; i++) {
                if ((this.file[i] != null) && (StringUtils.isNotEmpty(this.fileFileName[i])))
                {
                    File dest = new File(folder + File.separator + this.fileFileName[i]);
                    if (this.file[i].renameTo(dest))
                    {
                        this.response.getWriter().write("{\"jsonrpc\" : \"2.0\", \"result\" : null, \"id\" : \"id\"}");
                        return null;
                    }
                }
            }
        }
        this.response.getWriter().write("{\"jsonrpc\" : \"2.0\", \"error\" : {\"code\": 101, \"message\": \"Failed to open input stream.\"}, \"id\" : \"id\"}");
        return null;
    }

    @Action(value="productmassivedelete", results={@org.apache.struts2.convention.annotation.Result(type="redirectAction", location="productproccessimages")})
    public String productmassivedelete() throws Exception
    {
        String folder = getServletContext().getRealPath(File.separator + "stores" + File.separator + getStoreCode() + File.separator + "uploads");
        File dir = new File(folder);
        if ((dir.exists()) && (dir.isDirectory()))
        {
            final ImageResolver ir = getImageResolver();
            String[] listFiles = dir.list(new FilenameFilter()
            {
                public boolean accept(File dir, String name)
                {
                    return ir.validExtension(name);
                }
            });
            if (listFiles != null) {
                for (String fn : listFiles) {
                    FileUtils.forceDelete(new File(folder + File.separator + fn));
                }
            }
        }
        return "success";
    }

    @Action(value="productwithoutimage", results={@org.apache.struts2.convention.annotation.Result(type="stream", params={"inputName", "inputStream", "contentType", "application/vnd.ms-excel", "contentDisposition", "attachment; filename=products_without_image.xls"})})
    public String productsWithoutImage() throws Exception
    {
        ImageResolver ir = getImageResolver();
        try
        {
            HSSFWorkbook wb = new HSSFWorkbook();

            HSSFSheet sheet = wb.createSheet("Sheet 1");
            int rowIndex = 0;
            HSSFRow excelRow = sheet.createRow(rowIndex++);
            excelRow.createCell(0).setCellValue("Part Number");
            excelRow.createCell(1).setCellValue("Product name");
            excelRow.createCell(2).setCellValue("Status");
            for (Product p : this.dao.getProducts()) {
                if (StringUtils.isEmpty(ir.getImageForProduct(p, "")))
                {
                    excelRow = sheet.createRow(rowIndex++);
                    excelRow.createCell(0).setCellValue(p.getPartNumber());
                    excelRow.createCell(1).setCellValue(p.getProductName(getDefaultLanguage()));
                    excelRow.createCell(2).setCellValue(p.getActive().booleanValue() ? "Active" : "Inactive");
                }
            }
            ByteArrayOutputStream fileOut = new ByteArrayOutputStream();
            wb.write(fileOut);
            setInputStream(new ByteArrayInputStream(fileOut.toByteArray()));
        }
        catch (IOException e)
        {
            log.error(e.getMessage(), e);
        }
        return "success";
    }

    @Action(value = "productResetIndex")
    public String resetLuceneIndex() throws Exception {
        int cant = dao.gethSession().createSQLQuery("UPDATE t_product SET indexed=FALSE WHERE inventaryCode=:store").setString("store", getStoreCode()).executeUpdate();
        response.getWriter().print("Index Cleared: " + String.valueOf(cant));
        return null;
    }

    //  private DataNavigator products;
    private Product product;
    private Long idProduct;
    private ProductFilter productFilter;
    private DefaultMutableTreeNode categoryTree;
    private Integer openTab;
    private String reedit;
    private String output;
    private String multiple;

    private String[] fieldNames;
    private String[] showField;
    private String[] editField;

    private String[] exportField;
    private String exportFile;
    private File[] files;
    private String[] forDelete;

    private Manufacturer manufacturer;
    private Long idManufacturer;

    private Category category;
    private Long idCategory;

    private Currency currency;
    private String currencyCode;

    private String[] productName;
    private String[] description;
    private String[] features;
    private String[] information;
    private String[] caract1;
    private String[] caract2;
    private String[] caract3;

    private String[] seoTitle;
    private String[] seoKeywords;
    private String[] seoDescription;
    private String seoUrl;

    private Long[] volumen;
    private Double[] volumenPercent;
    private String[] volumenDescription;

    private Double[] levelPercent;

    private Long[] providerId;
    private Long[] providerStock;
    private Double[] providerCost;
    private String[] providerCurrency;
    private String[] providerEta;
    private String[] providerSku;
    private String[] providerActive;

    private String[] productImages;
    private File[] file;
    private String[] fileFileName;
    private String deleteImg;
    private String mainImage;

    private String[] labels;

    private Long[] related;

    private Long[] promotionId;
    private String[] promotionFrom;
    private String[] promotionTo;
    private Double[] promotionPercent;
    private Double[] promotionValue;

    private Long[] reviewId;
    private String[] reviewVisible;
    private Long idReview;
    private ProductReview review;

    private File importFile;
    private TableFile tableFile;
    private Integer[] importNew;
    private Integer[] importMod;

    private Long[] productId;
    private Long[] selecteds;
    private String[] productActive;
    private String[] productArchived;
    private String[] productPartNumber;
    private String[] productMfgPartNumber;
    private Long[] productManufacturer;
    private Long[] productCategory;
    private Double[] productPrice;
    private Double[] productCostPrice;
    private Long[] productStock;
    private String[] productLabelCode;
    private String[] productImage;


    private String productUpdate;

    private Long[] selectedResource;
    private Long[] selectedStaticText;
    private Long[] contentStaticTextId;
    private String[] contentPlace;
    private Integer[] contentOrder;

    private String htmlContent;
    private String[] fieldAction;

    private String[] attImportName;
    private String[] attImportValue;
    private String[] attImport;

    private Long idGroup;
    private ComplementGroup group;
    private String[] groupName;
    private Long[] groupId;
    private Long[] levelId;

    private Long taskId;
    private String taskName;

    private String updateFilters;

    private Long filterSupplier;

    public String[] getProviderCurrency() {
        return providerCurrency;
    }

    public void setProviderCurrency(String[] providerCurrency) {
        this.providerCurrency = providerCurrency;
    }

    public Long getFilterSupplier() {
        return filterSupplier;
    }

    public void setFilterSupplier(Long filterSupplier) {
        this.filterSupplier = filterSupplier;
    }

    public String getUpdateFilters() {
        return updateFilters;
    }

    public void setUpdateFilters(String updateFilters) {
        this.updateFilters = updateFilters;
    }

    public Long[] getSelecteds() {
        return selecteds;
    }

    public void setSelecteds(Long[] selecteds) {
        this.selecteds = selecteds;
    }

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public String[] getProviderActive() {
        return providerActive;
    }

    public void setProviderActive(String[] providerActive) {
        this.providerActive = providerActive;
    }

    public String[] getProviderSku() {
        return providerSku;
    }

    public void setProviderSku(String[] providerSku) {
        this.providerSku = providerSku;
    }

    public TableFile getTableFile() {
        return tableFile;
    }

    public void setTableFile(TableFile tableFile) {
        this.tableFile = tableFile;
    }

    public Long[] getGroupId() {
        return groupId;
    }

    public void setGroupId(Long[] groupId) {
        this.groupId = groupId;
    }

    public ComplementGroup getGroup() {
        return group;
    }

    public void setGroup(ComplementGroup group) {
        this.group = group;
    }

    public Long getIdGroup() {
        return idGroup;
    }

    public void setIdGroup(Long idGroup) {
        this.idGroup = idGroup;
    }

    public String[] getGroupName() {
        return groupName;
    }

    public void setGroupName(String[] groupName) {
        this.groupName = groupName;
    }

    public String[] getProductImage() {
        return productImage;
    }

    public void setProductImage(String[] productImage) {
        this.productImage = productImage;
    }

    public String[] getAttImportName() {
        return attImportName;
    }

    public void setAttImportName(String[] attImportName) {
        this.attImportName = attImportName;
    }

    public String[] getAttImportValue() {
        return attImportValue;
    }

    public void setAttImportValue(String[] attImportValue) {
        this.attImportValue = attImportValue;
    }

    public String[] getAttImport() {
        return attImport;
    }

    public void setAttImport(String[] attImport) {
        this.attImport = attImport;
    }

    public String[] getFieldAction() {
        return fieldAction;
    }

    public void setFieldAction(String[] fieldAction) {
        this.fieldAction = fieldAction;
    }

    public String getHtmlContent() {
        return htmlContent;
    }

    public void setHtmlContent(String htmlContent) {
        this.htmlContent = htmlContent;
    }

    public String[] getProductPartNumber() {
        return productPartNumber;
    }

    public void setProductPartNumber(String[] productPartNumber) {
        this.productPartNumber = productPartNumber;
    }

    public String[] getProductMfgPartNumber() {
        return productMfgPartNumber;
    }

    public void setProductMfgPartNumber(String[] productMfgPartNumber) {
        this.productMfgPartNumber = productMfgPartNumber;
    }

    public Long[] getProductManufacturer() {
        return productManufacturer;
    }

    public void setProductManufacturer(Long[] productManufacturer) {
        this.productManufacturer = productManufacturer;
    }

    public Long[] getProductCategory() {
        return productCategory;
    }

    public void setProductCategory(Long[] productCategory) {
        this.productCategory = productCategory;
    }

    public Double[] getProductPrice() {
        return productPrice;
    }

    public void setProductPrice(Double[] productPrice) {
        this.productPrice = productPrice;
    }

    public Double[] getProductCostPrice() {
        return productCostPrice;
    }

    public void setProductCostPrice(Double[] productCostPrice) {
        this.productCostPrice = productCostPrice;
    }

    public Long[] getProductStock() {
        return productStock;
    }

    public void setProductStock(Long[] productStock) {
        this.productStock = productStock;
    }

    public String[] getProductLabelCode() {
        return productLabelCode;
    }

    public void setProductLabelCode(String[] productLabelCode) {
        this.productLabelCode = productLabelCode;
    }

    public String[] getFieldNames() {
        return fieldNames;
    }

    public void setFieldNames(String[] fieldNames) {
        this.fieldNames = fieldNames;
    }

    public String[] getShowField() {
        return showField;
    }

    public void setShowField(String[] showField) {
        this.showField = showField;
    }

    public String[] getEditField() {
        return editField;
    }

    public void setEditField(String[] editField) {
        this.editField = editField;
    }

    public Long[] getSelectedResource() {
        return selectedResource;
    }

    public void setSelectedResource(Long[] selectedResource) {
        this.selectedResource = selectedResource;
    }

    public Long[] getSelectedStaticText() {
        return selectedStaticText;
    }

    public void setSelectedStaticText(Long[] selectedStaticText) {
        this.selectedStaticText = selectedStaticText;
    }

    public Long[] getContentStaticTextId() {
        return contentStaticTextId;
    }

    public void setContentStaticTextId(Long[] contentStaticTextId) {
        this.contentStaticTextId = contentStaticTextId;
    }

    public String[] getContentPlace() {
        return contentPlace;
    }

    public void setContentPlace(String[] contentPlace) {
        this.contentPlace = contentPlace;
    }

    public Integer[] getContentOrder() {
        return contentOrder;
    }

    public void setContentOrder(Integer[] contentOrder) {
        this.contentOrder = contentOrder;
    }

    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public Manufacturer getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(Manufacturer manufacturer) {
        this.manufacturer = manufacturer;
    }

    public Integer[] getImportNew() {
        return importNew;
    }

    public void setImportNew(Integer[] importNew) {
        this.importNew = importNew;
    }

    public Integer[] getImportMod() {
        return importMod;
    }

    public void setImportMod(Integer[] importMod) {
        this.importMod = importMod;
    }

    public String[] getProductArchived() {
        return productArchived;
    }

    public void setProductArchived(String[] productArchived) {
        this.productArchived = productArchived;
    }

    public List<ProductStaticText> getProductStaticText() {
        if (!requestCache.containsKey("PRODUCT_STATIC_TEXT")) requestCache.put("PRODUCT_STATIC_TEXT", dao.getProductStaticText(product));
        return (List<ProductStaticText>) requestCache.get("PRODUCT_STATIC_TEXT");
    }

    public List<CategoryStaticText> getParentCategoryStaticTexts() {
        if (product.getCategory() != null) {
            if (!requestCache.containsKey("parentCategoryStaticText")) requestCache.put("parentCategoryStaticText", dao.getParentCategoryStaticTexts(product.getCategory()));
            return (List<CategoryStaticText>) requestCache.get("parentCategoryStaticText");
        }
        return null;
    }


    public List<ProductProperty> getProductProperties() {
        if (!requestCache.containsKey("PRODUCT_PROPERTIES")) requestCache.put("PRODUCT_PROPERTIES", dao.getProductProperties(product));
        return (List<ProductProperty>) requestCache.get("PRODUCT_PROPERTIES");
    }

    public List<CategoryProperty> getParentCategoryProperties() {
        if (product.getCategory() != null) {
            if (!requestCache.containsKey("parentCategoryProperties")) requestCache.put("parentCategoryProperties", dao.getParentCategoryProperties(product.getCategory(), true));
            return (List<CategoryProperty>) requestCache.get("parentCategoryProperties");
        }
        return null;
    }

    public List<CategoryVolume> getProductVolumeParent() {
        if (CollectionUtils.isEmpty(getProductVolume()) && product.getCategory() != null) {
            if (!requestCache.containsKey("ProductVolumeParent")) requestCache.put("ProductVolumeParent", dao.getParentCategoryVolume(product.getCategory()));
            return (List<CategoryVolume>) requestCache.get("ProductVolumeParent");
        }
        return null;
    }

    public List<ProductVolume> getProductVolume() {
        if (!requestCache.containsKey("productVolume")) requestCache.put("productVolume", dao.getProductVolume(product));
        return (List<ProductVolume>) requestCache.get("productVolume");
    }

    public List<ProductProvider> getProductProviders() {
        if (!requestCache.containsKey("ProductProvider")) requestCache.put("ProductProvider", dao.getProductProviders(product));
        return (List<ProductProvider>) requestCache.get("ProductProvider");
    }

    public List<ProductOffer> getProductOffers() {
        if (!requestCache.containsKey("productOffers")) requestCache.put("productOffers", dao.getProductOffers(product));
        return (List<ProductOffer>) requestCache.get("productOffers");
    }

    public List<ProductOffer> getProductOffersParent() {
        if (product.getCategory() != null) {
            if (!requestCache.containsKey("productOffersParent")) requestCache.put("productOffersParent", dao.getParentCategoryOffers(product.getCategory()));
            return (List<ProductOffer>) requestCache.get("productOffersParent");
        }
        return null;
    }

    public List<ProductReview> getReviews() {
        if (!requestCache.containsKey("reviews")) requestCache.put("reviews", dao.getProductReviews(product, false));
        return (List<ProductReview>) requestCache.get("reviews");
    }

    public List<ProductVariation> getProductVariations() {
        if (!requestCache.containsKey("productVariations")) requestCache.put("productVariations", dao.getProductVariations(product));
        return (List<ProductVariation>) requestCache.get("productVariations");
    }

    public Map<State, ShippingRate> getProductShipping() {
        if (!requestCache.containsKey("productShipping")) requestCache.put("productShipping", dao.getProductShipping(product));
        return (Map<State, ShippingRate>) requestCache.get("productShipping");
    }

    public Map<State, ShippingRate> getCategoryShippingParent() {
        if (product != null && product.getCategory() != null) {
            if (!requestCache.containsKey("parentProductShipping")) requestCache.put("parentProductShipping", dao.getParentCategoryShipping(product.getCategory()));
            return (Map<State, ShippingRate>) requestCache.get("parentProductShipping");
        }
        return null;
    }

    public Set<ComplementGroup> getProductComplementParent() {
        if (product != null && product.getCategory() != null) {
            if (!requestCache.containsKey("parentProductComplement")) requestCache.put("parentProductComplement", dao.getParentCategoryComplement(product.getCategory()));
            return (Set<ComplementGroup>) requestCache.get("parentProductComplement");
        }
        return null;
    }

    public Set<ProductLabel> getProductLabelParent() {
        if (product != null && product.getCategory() != null) {
            if (!requestCache.containsKey("parentProductLabels")) requestCache.put("parentProductLabels", dao.getParentCategoryLabels(product.getCategory()));
            return (Set<ProductLabel>) requestCache.get("parentProductLabels");
        }
        return null;
    }


    private Long[] attributeId;
    private Long[] propertyId;
    private String[] propertyValue;

    private Long attributeNew;
    private String attributeValue;

    private Long[] variationId;
    private String[] variationCaract1;
    private String[] variationCaract2;
    private String[] variationCaract3;
    private Long[] variationStock;
    private Double[] variationPrice;
    private Double[] variationCost;
    private Double[] variationWidth;
    private Double[] variationHeight;
    private Double[] variationLength;
    private Double[] variationWeight;

    private String[] metaTitle;
    private Boolean metaTitleAppend;
    private String[] metaDescription;
    private Boolean metaDescriptionAppend;
    private String[] metaKeywords;
    private Boolean metaKeywordsAppend;
    private String[] metaAbstract;
    private Boolean metaAbstractAppend;

    private String[] metasName;
    private String[] metasValue;
    private Boolean[] metasAppend;

    private String[] shippingType;
    private Long[] shippingState;
    private Double[] shippingValue;

    private String profileName;
    private String profileField;

    private Map<String, Object> jsonResult;

    public Map<String, Object> getJsonResult() {
        return jsonResult;
    }

    public void setJsonResult(Map<String, Object> jsonResult) {
        this.jsonResult = jsonResult;
    }

    public String getProfileName() {
        return profileName;
    }

    public void setProfileName(String profileName) {
        this.profileName = profileName;
    }

    public String getProfileField() {
        return profileField;
    }

    public void setProfileField(String profileField) {
        this.profileField = profileField;
    }

    public String[] getShippingType() {
        return shippingType;
    }

    public void setShippingType(String[] shippingType) {
        this.shippingType = shippingType;
    }

    public Long[] getShippingState() {
        return shippingState;
    }

    public void setShippingState(Long[] shippingState) {
        this.shippingState = shippingState;
    }

    public Double[] getShippingValue() {
        return shippingValue;
    }

    public void setShippingValue(Double[] shippingValue) {
        this.shippingValue = shippingValue;
    }

    public String[] getMetaTitle() {
        return metaTitle;
    }

    public void setMetaTitle(String[] metaTitle) {
        this.metaTitle = metaTitle;
    }

    public Boolean getMetaTitleAppend() {
        return metaTitleAppend;
    }

    public void setMetaTitleAppend(Boolean metaTitleAppend) {
        this.metaTitleAppend = metaTitleAppend;
    }

    public String[] getMetaDescription() {
        return metaDescription;
    }

    public void setMetaDescription(String[] metaDescription) {
        this.metaDescription = metaDescription;
    }

    public Boolean getMetaDescriptionAppend() {
        return metaDescriptionAppend;
    }

    public void setMetaDescriptionAppend(Boolean metaDescriptionAppend) {
        this.metaDescriptionAppend = metaDescriptionAppend;
    }

    public String[] getMetaKeywords() {
        return metaKeywords;
    }

    public void setMetaKeywords(String[] metaKeywords) {
        this.metaKeywords = metaKeywords;
    }

    public Boolean getMetaKeywordsAppend() {
        return metaKeywordsAppend;
    }

    public void setMetaKeywordsAppend(Boolean metaKeywordsAppend) {
        this.metaKeywordsAppend = metaKeywordsAppend;
    }

    public String[] getMetaAbstract() {
        return metaAbstract;
    }

    public void setMetaAbstract(String[] metaAbstract) {
        this.metaAbstract = metaAbstract;
    }

    public Boolean getMetaAbstractAppend() {
        return metaAbstractAppend;
    }

    public void setMetaAbstractAppend(Boolean metaAbstractAppend) {
        this.metaAbstractAppend = metaAbstractAppend;
    }

    public String[] getMetasName() {
        return metasName;
    }

    public void setMetasName(String[] metasName) {
        this.metasName = metasName;
    }

    public String[] getMetasValue() {
        return metasValue;
    }

    public void setMetasValue(String[] metasValue) {
        this.metasValue = metasValue;
    }

    public Boolean[] getMetasAppend() {
        return metasAppend;
    }

    public void setMetasAppend(Boolean[] metasAppend) {
        this.metasAppend = metasAppend;
    }

    public File[] getFiles() {
        return files;
    }

    public void setFiles(File[] files) {
        this.files = files;
    }

    public String[] getForDelete() {
        return forDelete;
    }

    public void setForDelete(String[] forDelete) {
        this.forDelete = forDelete;
    }

    public String[] getExportField() {
        return exportField;
    }

    public void setExportField(String[] exportField) {
        this.exportField = exportField;
    }

    public String getExportFile() {
        return exportFile;
    }

    public void setExportFile(String exportFile) {
        this.exportFile = exportFile;
    }

    public Long[] getVariationId() {
        return variationId;
    }

    public void setVariationId(Long[] variationId) {
        this.variationId = variationId;
    }

    public Long[] getVariationStock() {
        return variationStock;
    }

    public void setVariationStock(Long[] variationStock) {
        this.variationStock = variationStock;
    }

    public Double[] getVariationPrice() {
        return variationPrice;
    }

    public void setVariationPrice(Double[] variationPrice) {
        this.variationPrice = variationPrice;
    }

    public Double[] getVariationCost() {
        return variationCost;
    }

    public void setVariationCost(Double[] variationCost) {
        this.variationCost = variationCost;
    }

    public String[] getVariationCaract1() {
        return variationCaract1;
    }

    public void setVariationCaract1(String[] variationCaract1) {
        this.variationCaract1 = variationCaract1;
    }

    public String[] getVariationCaract2() {
        return variationCaract2;
    }

    public void setVariationCaract2(String[] variationCaract2) {
        this.variationCaract2 = variationCaract2;
    }

    public String[] getVariationCaract3() {
        return variationCaract3;
    }

    public void setVariationCaract3(String[] variationCaract3) {
        this.variationCaract3 = variationCaract3;
    }

    public Double[] getVariationWidth() {
        return variationWidth;
    }

    public void setVariationWidth(Double[] variationWidth) {
        this.variationWidth = variationWidth;
    }

    public Double[] getVariationHeight() {
        return variationHeight;
    }

    public void setVariationHeight(Double[] variationHeight) {
        this.variationHeight = variationHeight;
    }

    public Double[] getVariationLength() {
        return variationLength;
    }

    public void setVariationLength(Double[] variationLength) {
        this.variationLength = variationLength;
    }

    public Double[] getVariationWeight() {
        return variationWeight;
    }

    public void setVariationWeight(Double[] variationWeight) {
        this.variationWeight = variationWeight;
    }

    public Long[] getAttributeId() {
        return attributeId;
    }

    public void setAttributeId(Long[] attributeId) {
        this.attributeId = attributeId;
    }

    public Long[] getPropertyId() {
        return propertyId;
    }

    public void setPropertyId(Long[] propertyId) {
        this.propertyId = propertyId;
    }

    public String[] getPropertyValue() {
        return propertyValue;
    }

    public void setPropertyValue(String[] propertyValue) {
        this.propertyValue = propertyValue;
    }

    public Long getAttributeNew() {
        return attributeNew;
    }

    public void setAttributeNew(Long attributeNew) {
        this.attributeNew = attributeNew;
    }

    public String getAttributeValue() {
        return attributeValue;
    }

    public void setAttributeValue(String attributeValue) {
        this.attributeValue = attributeValue;
    }

    /*
    public DataNavigator getProducts() {
        return products;
    }
    public void setProducts(org.store.core.beans.utils.DataNavigator products) {
        this.products = products;
    }
    */

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public Long getIdProduct() {
        return idProduct;
    }

    public void setIdProduct(Long idProduct) {
        this.idProduct = idProduct;
    }

    public ProductFilter getProductFilter() {
        return productFilter;
    }

    public void setProductFilter(ProductFilter productFilter) {
        this.productFilter = productFilter;
    }

    public DefaultMutableTreeNode getCategoryTree() {
        return categoryTree;
    }

    public void setCategoryTree(DefaultMutableTreeNode categoryTree) {
        this.categoryTree = categoryTree;
    }

    public Integer getOpenTab() {
        return openTab;
    }

    public void setOpenTab(Integer openTab) {
        this.openTab = openTab;
    }

    public String getReedit() {
        return reedit;
    }

    public void setReedit(String reedit) {
        this.reedit = reedit;
    }

    public Long getIdManufacturer() {
        return idManufacturer;
    }

    public void setIdManufacturer(Long idManufacturer) {
        this.idManufacturer = idManufacturer;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public Long getIdCategory() {
        return idCategory;
    }

    public void setIdCategory(Long idCategory) {
        this.idCategory = idCategory;
    }

    public String[] getProductName() {
        return productName;
    }

    public void setProductName(String[] productName) {
        this.productName = productName;
    }

    public String[] getDescription() {
        return description;
    }

    public void setDescription(String[] description) {
        this.description = description;
    }

    public String[] getFeatures() {
        return features;
    }

    public void setFeatures(String[] features) {
        this.features = features;
    }

    public String[] getInformation() {
        return information;
    }

    public void setInformation(String[] information) {
        this.information = information;
    }

    public String[] getCaract1() {
        return caract1;
    }

    public void setCaract1(String[] caract1) {
        this.caract1 = caract1;
    }

    public String[] getCaract2() {
        return caract2;
    }

    public void setCaract2(String[] caract2) {
        this.caract2 = caract2;
    }

    public String[] getCaract3() {
        return caract3;
    }

    public void setCaract3(String[] caract3) {
        this.caract3 = caract3;
    }

    public String[] getSeoTitle() {
        return seoTitle;
    }

    public void setSeoTitle(String[] seoTitle) {
        this.seoTitle = seoTitle;
    }

    public String[] getSeoKeywords() {
        return seoKeywords;
    }

    public void setSeoKeywords(String[] seoKeywords) {
        this.seoKeywords = seoKeywords;
    }

    public String[] getSeoDescription() {
        return seoDescription;
    }

    public void setSeoDescription(String[] seoDescription) {
        this.seoDescription = seoDescription;
    }

    public String getSeoUrl() {
        return seoUrl;
    }

    public void setSeoUrl(String seoUrl) {
        this.seoUrl = seoUrl;
    }

    public Long[] getVolumen() {
        return volumen;
    }

    public void setVolumen(Long[] volumen) {
        this.volumen = volumen;
    }

    public Double[] getVolumenPercent() {
        return volumenPercent;
    }

    public void setVolumenPercent(Double[] volumenPercent) {
        this.volumenPercent = volumenPercent;
    }

    public String[] getVolumenDescription() {
        return volumenDescription;
    }

    public void setVolumenDescription(String[] volumenDescription) {
        this.volumenDescription = volumenDescription;
    }

    public Long[] getLevelId() {
        return levelId;
    }

    public void setLevelId(Long[] levelId) {
        this.levelId = levelId;
    }

    public Double[] getLevelPercent() {
        return levelPercent;
    }

    public void setLevelPercent(Double[] levelPercent) {
        this.levelPercent = levelPercent;
    }

    public Long[] getProviderId() {
        return providerId;
    }

    public void setProviderId(Long[] providerId) {
        this.providerId = providerId;
    }

    public Long[] getProviderStock() {
        return providerStock;
    }

    public void setProviderStock(Long[] providerStock) {
        this.providerStock = providerStock;
    }

    public Double[] getProviderCost() {
        return providerCost;
    }

    public void setProviderCost(Double[] providerCost) {
        this.providerCost = providerCost;
    }

    public String[] getProviderEta() {
        return providerEta;
    }

    public void setProviderEta(String[] providerEta) {
        this.providerEta = providerEta;
    }

    public String[] getProductImages() {
        return productImages;
    }

    public void setProductImages(String[] productImages) {
        this.productImages = productImages;
    }

    public Double getProductUserLevelPercent(Product product, UserLevel level) {
        return dao.getProductUserLevelPercent(product, level);
    }

    public Double getParentProductUserLevelPercent(Product product, UserLevel level) {
        return dao.getParentCategoryUserLevelPercent(product.getCategory(), level);
    }

    public File[] getFile() {
        return file;
    }

    public void setFile(File[] file) {
        this.file = file;
    }

    public String[] getFileFileName() {
        return fileFileName;
    }

    public void setFileFileName(String[] fileFileName) {
        this.fileFileName = fileFileName;
    }

    public String getDeleteImg() {
        return deleteImg;
    }

    public void setDeleteImg(String deleteImg) {
        this.deleteImg = deleteImg;
    }

    public String[] getLabels() {
        return labels;
    }

    public void setLabels(String[] labels) {
        this.labels = labels;
    }

    public String getOutput() {
        return output;
    }

    public void setOutput(String output) {
        this.output = output;
    }

    public Long[] getRelated() {
        return related;
    }

    public void setRelated(Long[] related) {
        this.related = related;
    }

    public Long[] getPromotionId() {
        return promotionId;
    }

    public void setPromotionId(Long[] promotionId) {
        this.promotionId = promotionId;
    }

    public String[] getPromotionFrom() {
        return promotionFrom;
    }

    public void setPromotionFrom(String[] promotionFrom) {
        this.promotionFrom = promotionFrom;
    }

    public String[] getPromotionTo() {
        return promotionTo;
    }

    public void setPromotionTo(String[] promotionTo) {
        this.promotionTo = promotionTo;
    }

    public Double[] getPromotionPercent() {
        return promotionPercent;
    }

    public void setPromotionPercent(Double[] promotionPercent) {
        this.promotionPercent = promotionPercent;
    }

    public Double[] getPromotionValue() {
        return promotionValue;
    }

    public void setPromotionValue(Double[] promotionValue) {
        this.promotionValue = promotionValue;
    }

    public Long[] getReviewId() {
        return reviewId;
    }

    public void setReviewId(Long[] reviewId) {
        this.reviewId = reviewId;
    }

    public String[] getReviewVisible() {
        return reviewVisible;
    }

    public void setReviewVisible(String[] reviewVisible) {
        this.reviewVisible = reviewVisible;
    }

    public Long getIdReview() {
        return idReview;
    }

    public void setIdReview(Long idReview) {
        this.idReview = idReview;
    }

    public ProductReview getReview() {
        return review;
    }

    public void setReview(ProductReview review) {
        this.review = review;
    }

    public File getImportFile() {
        return importFile;
    }

    public void setImportFile(File importFile) {
        this.importFile = importFile;
    }

    public Long[] getProductId() {
        return productId;
    }

    public void setProductId(Long[] productId) {
        this.productId = productId;
    }

    public String[] getProductActive() {
        return productActive;
    }

    public void setProductActive(String[] productActive) {
        this.productActive = productActive;
    }

    public String getProductUpdate() {
        return productUpdate;
    }

    public void setProductUpdate(String productUpdate) {
        this.productUpdate = productUpdate;
    }

    public String getMainImage() {
        return mainImage;
    }

    public void setMainImage(String mainImage) {
        this.mainImage = mainImage;
    }

    public String getMultiple() {
        return multiple;
    }

    public void setMultiple(String multiple) {
        this.multiple = multiple;
    }

    public class ProductTableField {
        private String name;
        private boolean show;
        private boolean edit;

        public ProductTableField(String name, boolean show, boolean edit) {
            this.name = name;
            this.show = show;
            this.edit = edit;
        }

        public boolean getCanSort() {
            String[] arrSorted = new String[]{"partNumber", "productName", "stock", "price", "costPrice", "mfgPartNumber"};
            return (StringUtils.isNotEmpty(name) && ArrayUtils.contains(arrSorted, name));
        }

        public String getSortName() {
            if (StringUtils.isNotEmpty(name) && "basePrice".equalsIgnoreCase(name)) return "price";
            else return name;
        }

        public boolean getCanEdit() {
            String[] arrCanNotEdit = new String[]{"basePrice"};
            return (StringUtils.isNotEmpty(name) && !ArrayUtils.contains(arrCanNotEdit, name));
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public boolean getShow() {
            return show;
        }

        public void setShow(boolean show) {
            this.show = show;
        }

        public boolean getEdit() {
            return edit;
        }

        public void setEdit(boolean edit) {
            this.edit = edit;
        }

    }

}