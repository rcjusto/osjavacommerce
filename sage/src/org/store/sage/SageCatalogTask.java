package org.store.sage;

import org.apache.commons.lang.StringUtils;
import org.store.core.beans.*;
import org.store.core.beans.utils.PageMeta;
import org.store.core.globals.SomeUtils;
import org.store.core.globals.config.Store20Database;
import org.store.sage.bean.SAGECategory;
import org.store.sage.bean.SAGEProduct;
import org.store.sage.bean.SAGEUserLevel;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;

public class SageCatalogTask extends SageTask {

    private List<SAGEUserLevel> userLevelsToImport;

    public SageCatalogTask(String storeCode, Job job, Store20Database databaseConfig) throws Exception {
        super(storeCode, job, databaseConfig);
    }

    @Override
    protected void execute() {
        setExecutionPercent(0d);
        // importar categorias
        if ("Y".equalsIgnoreCase(getProperty("import.all.categories", "N"))) {
            setExecutionMessage("Sincronizando categorias...");
            List<SAGECategory> categoriesToImport = getCategoriesToImport();
            if (categoriesToImport != null && !categoriesToImport.isEmpty()) {
                int numcat = 0;
                for (SAGECategory sageCategory : categoriesToImport) if (importCategory(sageCategory) != null) numcat++;
                addOutputMessage(numcat + " categorias importadas");
                dao.flushSession();
                dao.clearSession();
            } else {
                addOutputMessage("No se encontraron categorias para importar");
            }
        }

        // importar categorias tarifarias
        userLevelsToImport = getUserLevelsToImport();
        if ("Y".equalsIgnoreCase(getProperty("import.all.userlevels", "N"))) {
            setExecutionMessage("Sincronizando categorias tarifarias...");
            if (userLevelsToImport != null && !userLevelsToImport.isEmpty()) {
                int numlev = 0;
                for (SAGEUserLevel sageUserLevel : userLevelsToImport) if (importUserLevel(sageUserLevel) != null) numlev++;
                addOutputMessage(numlev + " categorias tarifarias importadas");
                dao.flushSession();
                dao.clearSession();
            } else {
                addOutputMessage("No se encontraron categorias tarifarias para importar");
            }
        }

        // Importar productos
        int newProd = 0, updProd = 0;
        setExecutionMessage("Buscando productos para sincronizar...");
        List<SAGEProduct> productsToImport = getProductsToImport();
        int index = 0, totalProds = productsToImport.size();
        for (SAGEProduct sageProduct : productsToImport) {
            setExecutionPercent(100d * index / totalProds);
            setExecutionMessage("Sincronizando producto " + String.valueOf(index++) + "/" + String.valueOf(totalProds));
            if (StringUtils.isNotEmpty(sageProduct.getAR_Ref())) {
                Boolean res = importProduct(sageProduct);
                if (res != null) {
                    if (res) newProd++;
                    else updProd++;
                }
                dao.flushSession();
                dao.clearSession();
            }
        }
        addOutputMessage(newProd + " productos nuevos importados");
        addOutputMessage(updProd + " productos actualizados");
    }

    private Boolean importProduct(SAGEProduct sageProduct) {
        Boolean isNew = Boolean.FALSE;

        // importar producto
        Product product = dao.getProductByPartNumber(sageProduct.getAR_Ref());
        if (product == null) {
            if (!"Y".equalsIgnoreCase(getProperty("import.new.products", "Y"))) return null;
            isNew = Boolean.TRUE;
            product = new Product();
            product.setPartNumber(sageProduct.getAR_Ref());
        } else {
            if (!"Y".equalsIgnoreCase(getProperty("update.old.products", "Y"))) return null;
        }
        sageProduct.copyToProduct(product);

        // importar categorias
        Category beanCat = importCategory(sageProduct.getSageCategory());
        if (beanCat != null) {
            product.setCategory(beanCat);
            product.setProductCategories(new HashSet<Category>());
            product.getProductCategories().add(beanCat);
        }
        dao.save(product);

        // importar campos multiidioma
        for (String l : availableLanguages) {
            ProductLang pl = product.getLanguage(l);
            if (pl == null) {
                pl = new ProductLang();
                pl.setProduct(product);
                pl.setProductLang(l);
            }
            sageProduct.copyToProductLang(pl);
            if ("Y".equalsIgnoreCase(getStoreProperty(StoreProperty.PROP_AUTOGENERATE_METAS, StoreProperty.PROP_DEFAULT_AUTOGENERATE_METAS))) {
                String descriptionText = SomeUtils.extractText(pl.getDescription());
                if (StringUtils.isEmpty(pl.getMetaValue(PageMeta.META_TITLE))) pl.addMeta(PageMeta.META_TITLE, pl.getProductName(), false);
                if (StringUtils.isEmpty(pl.getMetaValue(PageMeta.META_DESCRIPTION))) pl.addMeta(PageMeta.META_DESCRIPTION, descriptionText, false);
                if (StringUtils.isEmpty(pl.getMetaValue(PageMeta.META_KEYWORDS))) pl.addMeta(PageMeta.META_KEYWORDS, SomeUtils.extractKeywords(pl.getProductName() + " " + descriptionText), false);
            }
            dao.save(pl);
        }

        //Importar categorias tarifarias
        if (userLevelsToImport!=null && !userLevelsToImport.isEmpty()) {
            for (SAGEUserLevel sageLevel : userLevelsToImport) {
                Double percent = (sageProduct.getSageTarif()!=null && sageProduct.getSageTarif().containsKey(sageLevel)) ? sageProduct.getSageTarif().get(sageLevel) : sageProduct.getAR_Coef();
                UserLevel userLevel = importUserLevel(sageLevel);
                ProductUserLevel pul = dao.getProductUserLevel(product, userLevel);
                if (pul == null) {
                    pul = new ProductUserLevel();
                    pul.setLevel(userLevel);
                    pul.setProduct(product);
                }
                pul.setPercentDiscount(percent);
                dao.save(pul);
            }
        }

        // actualizar otros datos del producto
        dao.updateProductUrlCode(product, null, defaultLanguage);
        dao.indexProduct(product, false);
        // todo actualizar los metas
        auditStock(product);

        //Hibernate.initialize(product.getForUsers());
        return isNew;
    }

    private Category importCategory(SAGECategory sageCategory) {
        if (sageCategory.getCategories() != null && !sageCategory.getCategories().isEmpty()) {
            Category cat = null;
            Category parentCat = dao.getRootCategory();
            for (SAGECategory.SAGECategoryItem sageItem : sageCategory.getCategories()) {
                cat = dao.getCategoryByExternalCode(sageItem.getId().toString());
                if (cat == null) {
                    cat = new Category();
                    cat.setVisible(true);
                    cat.setInventaryCode(storeCode);
                    cat.setIdParent((parentCat != null) ? parentCat.getIdCategory() : null);
                }
                sageItem.copyToCategory(cat);
                dao.save(cat);
                for (String l : availableLanguages) {
                    CategoryLang cl = cat.getLanguage(l);
                    if (cl == null) {
                        cl = new CategoryLang();
                        cl.setCategory(cat);
                        cl.setCategoryLang(l);
                        sageItem.copyToCategoryLang(cl);
                        dao.save(cl);
                    }
                }
                dao.updateCategoryUrlCode(cat, null, defaultLanguage);

                if (parentCat != null && !parentCat.equals(cat) && !parentCat.isRootCategory() && !cat.isRootCategory()) {
                    CategoryTree ct = dao.getCategoryTree(parentCat, cat);
                    if (ct == null) {
                        ct = new CategoryTree();
                        ct.setParent(parentCat);
                        ct.setChild(cat);
                        dao.save(ct);
                    }
                }
                parentCat = cat;
            }
            return cat;
        }
        return null;
    }


    private List<SAGEProduct> getProductsToImport() {
        List<SAGEProduct> result = new ArrayList<SAGEProduct>();
        try {
            PreparedStatement stmt = sageConnection.prepareStatement("select AR_Ref from F_ARTICLE where AR_Publie=1");
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) result.add(new SAGEProduct(sageConnection, rs.getString(1)));
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
        }
        return result;
    }

    private List<SAGECategory> getCategoriesToImport() {
        return null;
    }


    public ProductAuditStock auditStock(Product p) {
        ProductAuditStock bean = new ProductAuditStock();
        bean.setProduct(p);
        bean.setUser(null);
        bean.setStock(p.getStock());
        bean.setChangeDate(Calendar.getInstance().getTime());
        bean.setDescription("Automatically updated by SAGE task");
        dao.save(bean);
        return bean;
    }


}
