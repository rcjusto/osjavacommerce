package org.store.core.admin;

import org.store.core.beans.Category;
import org.store.core.beans.Manufacturer;
import org.store.core.beans.Product;
import org.store.core.beans.ProductLabel;
import org.store.core.beans.PromotionalCode;
import org.store.core.beans.User;
import org.store.core.beans.UserLevel;
import org.store.core.beans.utils.BreadCrumb;
import org.store.core.globals.SomeUtils;
import org.store.core.globals.StoreMessages;
import org.apache.commons.lang.StringUtils;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;

import java.util.HashSet;

@Namespace(value = "/admin")
@ParentPackage(value = "store-admin")
public class promotionsAction extends AdminModuleAction implements StoreMessages {
    private static final int MAX_MANUFACTURERS = 150;

    @Override
    public void prepare() throws Exception {
        promotionalCode = (PromotionalCode) dao.get(PromotionalCode.class, idPromotionalCode);
    }

    public String list() throws Exception {
        addToStack("promotions", dao.getPromotionalCodes());
        getBreadCrumbs().add(new BreadCrumb(null, getText("admin.promotional.code.list"), null, null));
        return SUCCESS;
    }

    public String edit() throws Exception {
        addToStack("manufacturerFilter", dao.getManufacturersCount() < MAX_MANUFACTURERS);
        getBreadCrumbs().add(new BreadCrumb(null, getText("admin.promotional.code.list"), url("listpromotions", "/admin"), null));
        getBreadCrumbs().add(new BreadCrumb(null, getText(promotionalCode != null ? "admin.promotional.code.modify" : "admin.promotional.code.new"), null, null));
        return SUCCESS;
    }

    public String del() throws Exception {
        return SUCCESS;
    }

    public String save() throws Exception {
        if (promotionalCode != null) {
            boolean nueva = promotionalCode.getIdPromotion() == null || promotionalCode.getIdPromotion() == 0;
            promotionalCode.setProductInCart((Product) dao.get(Product.class, productInCart));
            promotionalCode.setCategoryInCart((Category) dao.get(Category.class, categoryInCart));
            promotionalCode.setManufacturerInCart((Manufacturer) dao.get(Manufacturer.class, manufacturerInCart));
            promotionalCode.setLabelInCart((ProductLabel) dao.get(ProductLabel.class, labelInCart));
            promotionalCode.setFreeProduct((Product) dao.get(Product.class, freeProduct));
            if (promotionName != null && promotionName.length == getLanguages().length) {
                for (int i = 0; i < getLanguages().length; i++) {
                    promotionalCode.setName(getLanguages()[i], StringUtils.isNotEmpty(promotionName[i]) ? promotionName[i] : "");
                }
            }
            promotionalCode.setUser((User) dao.get(User.class, idUser));
            promotionalCode.setUserLevel(userLevel);
            promotionalCode.setInventaryCode(getStoreCode());
            promotionalCode.setOnlyOnce(true);
            promotionalCode.setValidFrom(SomeUtils.strToDate(validFrom, getLocale().getLanguage()));
            promotionalCode.setValidTo(SomeUtils.strToDate(validTo, getLocale().getLanguage()));
            dao.save(promotionalCode);
            if (nueva) return INPUT;
        }
        return SUCCESS;
    }

    @Action(value = "promotionssetcondition", results = @Result(type = "velocity", location = "/WEB-INF/views/admin/promotionsedit.vm"))
    public String setCondition() throws Exception {
        if (promotionalCode != null && StringUtils.isNotEmpty(conditionName) && conditionValue != null) {
            if ("categoryAddYes".equalsIgnoreCase(conditionName)) {
                Category c = dao.getCategory(conditionValue);
                if (c != null) {
                    if (promotionalCode.getApplyToCategories() == null) promotionalCode.setApplyToCategories(new HashSet<Category>());
                    promotionalCode.getApplyToCategories().add(c);
                    dao.save(promotionalCode);
                }
            } else if ("categoryAddNo".equalsIgnoreCase(conditionName)) {
                Category c = dao.getCategory(conditionValue);
                if (c != null) {
                    if (promotionalCode.getNotApplyToCategories() == null) promotionalCode.setNotApplyToCategories(new HashSet<Category>());
                    promotionalCode.getNotApplyToCategories().add(c);
                    dao.save(promotionalCode);
                }
            } else if ("categoryDelYes".equalsIgnoreCase(conditionName)) {
                Category c = dao.getCategory(conditionValue);
                if (c != null && promotionalCode.getApplyToCategories() != null && promotionalCode.getApplyToCategories().contains(c)) {
                    promotionalCode.getApplyToCategories().remove(c);
                    dao.save(promotionalCode);
                }
            } else if ("categoryDelNo".equalsIgnoreCase(conditionName)) {
                Category c = dao.getCategory(conditionValue);
                if (c != null && promotionalCode.getNotApplyToCategories() != null && promotionalCode.getNotApplyToCategories().contains(c)) {
                    promotionalCode.getNotApplyToCategories().remove(c);
                    dao.save(promotionalCode);
                }
            } else if ("labelAddYes".equalsIgnoreCase(conditionName)) {
                ProductLabel l = (ProductLabel) dao.getProductLabel(conditionValue);
                if (l != null) {
                    if (promotionalCode.getApplyToLabels() == null) promotionalCode.setApplyToLabels(new HashSet<ProductLabel>());
                    promotionalCode.getApplyToLabels().add(l);
                    dao.save(promotionalCode);
                }
            } else if ("labelAddNo".equalsIgnoreCase(conditionName)) {
                ProductLabel l = (ProductLabel) dao.getProductLabel(conditionValue);
                if (l != null) {
                    if (promotionalCode.getNotApplyToLabels() == null) promotionalCode.setNotApplyToLabels(new HashSet<ProductLabel>());
                    promotionalCode.getNotApplyToLabels().add(l);
                    dao.save(promotionalCode);
                }
            } else if ("labelDelYes".equalsIgnoreCase(conditionName)) {
                ProductLabel l = (ProductLabel) dao.getProductLabel(conditionValue);
                if (l != null && promotionalCode.getApplyToLabels() != null && promotionalCode.getApplyToLabels().contains(l)) {
                    promotionalCode.getApplyToLabels().remove(l);
                    dao.save(promotionalCode);
                }
            } else if ("labelDelNo".equalsIgnoreCase(conditionName)) {
                ProductLabel l = (ProductLabel) dao.getProductLabel(conditionValue);
                if (l != null && promotionalCode.getNotApplyToLabels() != null && promotionalCode.getNotApplyToLabels().contains(l)) {
                    promotionalCode.getNotApplyToLabels().remove(l);
                    dao.save(promotionalCode);
                }
            }
        }
        getBreadCrumbs().add(new BreadCrumb(null, getText("admin.promotional.code.list"), url("listpromotions", "/admin"), null));
        getBreadCrumbs().add(new BreadCrumb(null, getText(promotionalCode != null ? "admin.promotional.code.modify" : "admin.promotional.code.new"), null, null));
        return SUCCESS;
    }

    private PromotionalCode promotionalCode;
    private Long idPromotionalCode;
    private Long productInCart;
    private Long categoryInCart;
    private Long manufacturerInCart;
    private Long labelInCart;
    private Long freeProduct;
    private String[] promotionName;
    private String conditionName;
    private Long conditionValue;
    private Long idUser;
    private UserLevel userLevel;

    private String validFrom;
    private String validTo;


    public PromotionalCode getPromotionalCode() {
        return promotionalCode;
    }

    public void setPromotionalCode(PromotionalCode promotionalCode) {
        this.promotionalCode = promotionalCode;
    }

    public Long getIdPromotionalCode() {
        return idPromotionalCode;
    }

    public void setIdPromotionalCode(Long idPromotionalCode) {
        this.idPromotionalCode = idPromotionalCode;
    }

    public Long getProductInCart() {
        return productInCart;
    }

    public void setProductInCart(Long productInCart) {
        this.productInCart = productInCart;
    }

    public Long getCategoryInCart() {
        return categoryInCart;
    }

    public void setCategoryInCart(Long categoryInCart) {
        this.categoryInCart = categoryInCart;
    }

    public Long getManufacturerInCart() {
        return manufacturerInCart;
    }

    public void setManufacturerInCart(Long manufacturerInCart) {
        this.manufacturerInCart = manufacturerInCart;
    }

    public Long getLabelInCart() {
        return labelInCart;
    }

    public void setLabelInCart(Long labelInCart) {
        this.labelInCart = labelInCart;
    }

    public Long getFreeProduct() {
        return freeProduct;
    }

    public void setFreeProduct(Long freeProduct) {
        this.freeProduct = freeProduct;
    }

    public String[] getPromotionName() {
        return promotionName;
    }

    public void setPromotionName(String[] promotionName) {
        this.promotionName = promotionName;
    }

    public String getConditionName() {
        return conditionName;
    }

    public void setConditionName(String conditionName) {
        this.conditionName = conditionName;
    }

    public Long getConditionValue() {
        return conditionValue;
    }

    public void setConditionValue(Long conditionValue) {
        this.conditionValue = conditionValue;
    }

    public Long getIdUser() {
        return idUser;
    }

    public void setIdUser(Long idUser) {
        this.idUser = idUser;
    }

    public UserLevel getUserLevel() {
        return userLevel;
    }

    public void setUserLevel(UserLevel userLevel) {
        this.userLevel = userLevel;
    }

    public String getValidFrom() {
        return validFrom;
    }

    public void setValidFrom(String validFrom) {
        this.validFrom = validFrom;
    }

    public String getValidTo() {
        return validTo;
    }

    public void setValidTo(String validTo) {
        this.validTo = validTo;
    }
}
