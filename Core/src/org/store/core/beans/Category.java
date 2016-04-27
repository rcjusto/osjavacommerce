package org.store.core.beans;

import org.store.core.beans.utils.ExportedBean;
import org.store.core.beans.utils.MultiLangBean;
import org.store.core.beans.utils.PageMeta;
import org.store.core.beans.utils.StoreBean;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.struts2.json.JSONException;
import org.apache.struts2.json.JSONUtil;

import javax.persistence.*;
import java.lang.reflect.InvocationTargetException;
import java.util.*;


@Entity
@Table(name = "t_category")
public class Category extends BaseBean implements MultiLangBean, ExportedBean, StoreBean {

    public static final String ROOT_CATEGORY_CODE = "_ROOT";
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idCategory;

    // Codigo de la tienda a la que pertenece la categoria
    @Column(length = 10)
    private String inventaryCode;

    @Column(length = 255)
    private String externalCode;

    // Categoria visible en la tienda
    private boolean visible;

    // Categoria visible en la tienda
    private boolean active;

    // Codigo usado para identificar la categoria en las urls amigables
    @Column(length = 512)
    private String urlCode;

    @Column
    private Long idParent;

    // Cantidad en la que se envia un correo de alerta
    private Integer stockMin;

    // Tiempo que se demora el envio
    @Column(length = 255)
    private String deliveryTime;

    // Largo de los productos
    private Double dimentionLength;

    // Ancho de los productos
    private Double dimentionWidth;

    // Alto de los productos
    private Double dimentionHeight;

    // Peso de los productos
    private Double weight;

    // Requiere seleccion de fecha para la compra
    @Column(length = 1)
    private String dateSelection;

    // Requiere seleccion de hora para la compra
    @Column(length = 1)
    private String timeSelection;

    // Productos incluidos en la distribucion
    @Column(length = 1)
    private String needShipping;

    // Habilitar comparacion de productos
    @Column(length = 1)
    private String compare;

    // Factor q se multiplica por el precio de costo para calcular el precio de venta
    private Double markupFactor;

    // Exchange Rate Markup. Factor q se multiplica por el precio de costo para calcular el precio de venta
    private Double erMarkupFactor;

    // Plantilla con que se muestran los productos de esta categoria
    @Column(length = 255)
    private String productTemplate;

    // Plantilla con que se muestra esta categoria
    @Column(length = 255)
    private String template;

    // Numero de productos a mostrar en una pagina del listado
    private Integer numItems;

    // Numero de productos a mostrar en una pagina del listado
    private Integer defaultPosition;

    @Column(length = 1024)
    private String countriesCanBuy;

    @Lob
    private String reviewCategories;

    // filtrado por precio
    @Column(length = 1)
    private String filterByPrice;

    // rangos de precio
    @Lob
    private String priceRanges;

    // Datos especificos para cada idioma
    @OneToMany(mappedBy = "category")
    private Set<CategoryLang> categoryLangs;

    @ManyToMany
    @JoinTable(name = "t_category_t_complement_group",
            joinColumns = @JoinColumn(name = "t_category_idCategory"),
            inverseJoinColumns = @JoinColumn(name = "relatedGroups_idGroup"))
    private Set<ComplementGroup> relatedGroups;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "t_product_t_category",
            joinColumns = @JoinColumn(name = "productCategories_idCategory"),
            inverseJoinColumns = @JoinColumn(name = "t_product_idProduct"))
    private Set<Product> products;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "t_promotional_code_apply_categories",
            joinColumns = @JoinColumn(name = "applyToCategories_idCategory"),
            inverseJoinColumns = @JoinColumn(name = "t_promotional_code_idPromotion"))
    private Set<PromotionalCode> applyPromotions;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "t_promotional_code_not_apply_categories",
            joinColumns = @JoinColumn(name = "notApplyToCategories_idCategory"),
            inverseJoinColumns = @JoinColumn(name = "t_promotional_code_idPromotion"))
    private Set<PromotionalCode> notApplyPromotions;

    // Etiquetas asignadas
    @ManyToMany
    @JoinTable(name = "t_category_t_productlabel",
            joinColumns = @JoinColumn(name = "t_category_idCategory"),
            inverseJoinColumns = @JoinColumn(name = "labels_id"))
    private Set<ProductLabel> labels;

    public Category() {
    }

    public Long getIdCategory() {
        return this.idCategory;
    }

    public void setIdCategory(Long idCategory) {
        this.idCategory = idCategory;
    }

    public Long getIdParent() {
        return idParent;
    }

    public void setIdParent(Long idParent) {
        if (idParent==null || !idParent.equals(idCategory)) this.idParent = idParent;
    }

    public boolean getVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getInventaryCode() {
        return inventaryCode;
    }

    public void setInventaryCode(String inventaryCode) {
        this.inventaryCode = inventaryCode;
    }

    public String getExternalCode() {
        return externalCode;
    }

    public void setExternalCode(String externalCode) {
        this.externalCode = externalCode;
    }

    public Integer getStockMin() {
        return this.stockMin;
    }

    public void setStockMin(Integer stockMin) {
        this.stockMin = stockMin;
    }

    public String getDeliveryTime() {
        return this.deliveryTime;
    }

    public void setDeliveryTime(String deliveryTime) {
        this.deliveryTime = deliveryTime;
    }

    public Double getDimentionLength() {
        return dimentionLength;
    }

    public void setDimentionLength(Double dimentionLength) {
        this.dimentionLength = dimentionLength;
    }

    public Double getDimentionWidth() {
        return dimentionWidth;
    }

    public void setDimentionWidth(Double dimentionWidth) {
        this.dimentionWidth = dimentionWidth;
    }

    public Double getDimentionHeight() {
        return dimentionHeight;
    }

    public void setDimentionHeight(Double dimentionHeight) {
        this.dimentionHeight = dimentionHeight;
    }

    public Double getWeight() {
        return this.weight;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }

    public String getDateSelection() {
        return this.dateSelection;
    }

    public void setDateSelection(String dateSelection) {
        this.dateSelection = dateSelection;
    }

    public String getTimeSelection() {
        return this.timeSelection;
    }

    public void setTimeSelection(String timeSelection) {
        this.timeSelection = timeSelection;
    }

    public String getNeedShipping() {
        return this.needShipping;
    }

    public void setNeedShipping(String needShipping) {
        this.needShipping = needShipping;
    }

    public String getCompare() {
        return compare;
    }

    public void setCompare(String compare) {
        this.compare = compare;
    }

    public String getFilterByPrice() {
        return filterByPrice;
    }

    public void setFilterByPrice(String filterByPrice) {
        this.filterByPrice = filterByPrice;
    }

    public String getProductTemplate() {
        return productTemplate;
    }

    public void setProductTemplate(String productTemplate) {
        this.productTemplate = productTemplate;
    }

    public String getTemplate() {
        return template;
    }

    public void setTemplate(String template) {
        this.template = template;
    }

    public Integer getNumItems() {
        return numItems;
    }

    public void setNumItems(Integer numItems) {
        this.numItems = numItems;
    }

    public String getUrlCode() {
        return urlCode;
    }

    public void setUrlCode(String urlCode) {
        this.urlCode = urlCode;
    }

    public Double getMarkupFactor() {
        return markupFactor;
    }

    public void setMarkupFactor(Double markupFactor) {
        this.markupFactor = markupFactor;
    }

    public Double getErMarkupFactor() {return erMarkupFactor;}

    public void setErMarkupFactor(Double erMarkupFactor) {
        this.erMarkupFactor = erMarkupFactor;
    }

    public Integer getDefaultPosition() {return defaultPosition;}

    public void setDefaultPosition(Integer defaultPosition) {this.defaultPosition = defaultPosition;}

    public Set<ProductLabel> getLabels() {
        return labels;
    }

    public void setLabels(Set<ProductLabel> labels) {
        this.labels = labels;
    }

    public String getReviewCategories() {
        return reviewCategories;
    }

    public void setReviewCategories(String reviewCategories) {
        this.reviewCategories = reviewCategories;
    }

    public String getPriceRanges() {
        return priceRanges;
    }

    public void setPriceRanges(String priceRanges) {
        this.priceRanges = priceRanges;
    }

    public String[] getReviewCategoryList() {
        return (StringUtils.isNotEmpty(reviewCategories)) ? StringUtils.split(reviewCategories, ',') : null;
    }

    public String getCountriesCanBuy() {
        return countriesCanBuy;
    }

    public void setCountriesCanBuy(String countriesCanBuy) {
        this.countriesCanBuy = countriesCanBuy;
    }

    public String[] getCountriesCanBuyList() {
        return (StringUtils.isNotEmpty(countriesCanBuy)) ? StringUtils.split(countriesCanBuy, ',') : null;
    }

    public void setCountriesCanBuyList(String[] arr) {
        if (arr!=null && arr.length>0) {
            this.countriesCanBuy = StringUtils.join(arr,",");
        } else {
            this.countriesCanBuy = null;
        }
    }

    public Set<ComplementGroup> getRelatedGroups() {
        return relatedGroups;
    }

    public void setRelatedGroups(Set<ComplementGroup> relatedGroups) {
        this.relatedGroups = relatedGroups;
    }

    public Set<CategoryLang> getCategoryLangs() {
        return categoryLangs;
    }

    public void setCategoryLangs(Set<CategoryLang> categoryLangs) {
        this.categoryLangs = categoryLangs;
    }

    public Set<Product> getProducts() {
        return products;
    }

    public void setProducts(Set<Product> products) {
        this.products = products;
    }

    public Set<PromotionalCode> getApplyPromotions() {
        return applyPromotions;
    }

    public void setApplyPromotions(Set<PromotionalCode> applyPromotions) {
        this.applyPromotions = applyPromotions;
    }

    public Set<PromotionalCode> getNotApplyPromotions() {
        return notApplyPromotions;
    }

    public void setNotApplyPromotions(Set<PromotionalCode> notApplyPromotions) {
        this.notApplyPromotions = notApplyPromotions;
    }

    public String toString() {
        return new ToStringBuilder(this)
                .append("idCategory", getIdCategory())
                .toString();
    }

    public CategoryLang getLanguage(String lang) {
        if (getCategoryLangs() != null && lang != null) {
            for (CategoryLang categoryLang : getCategoryLangs()) {
                if (lang.equals(categoryLang.getCategoryLang())) return categoryLang;
            }
        }
        return null;
    }

    public CategoryLang getLanguage(String lang, String defLang) {
        if (getCategoryLangs() != null) {
            for (CategoryLang categoryLang : getCategoryLangs()) {
                if (lang.equals(categoryLang.getCategoryLang()) && categoryLang.getCategoryName() != null) {
                    return categoryLang;
                }
            }
            for (CategoryLang categoryLang : getCategoryLangs()) {
                if (defLang.equals(categoryLang.getCategoryLang())) {
                    return categoryLang;
                }
            }
        }
        return null;
    }

    public String getCategoryName(String lang) {
        if (getCategoryLangs() != null) {
            for (CategoryLang categoryLang : getCategoryLangs()) {
                if (lang.equals(categoryLang.getCategoryLang()) && StringUtils.isNotEmpty(categoryLang.getCategoryName())) {
                    return categoryLang.getCategoryName();
                }
            }
            for (CategoryLang categoryLang : getCategoryLangs()) {
                if (StringUtils.isNotEmpty(categoryLang.getCategoryName())) {
                    return categoryLang.getCategoryName();
                }
            }
        }
        return "";
    }
    public PageMeta getCategoryMeta(String meta, String lang) {
        if (getCategoryLangs() != null) {
            for (CategoryLang categoryLang : getCategoryLangs()) {
                if (lang.equals(categoryLang.getCategoryLang()) && StringUtils.isNotEmpty(categoryLang.getMetaValue(meta))) {
                    return categoryLang.getMeta(meta);
                }
            }
            for (CategoryLang categoryLang : getCategoryLangs()) {
                if (StringUtils.isNotEmpty(categoryLang.getMetaValue(meta))) {
                    return categoryLang.getMeta(meta);
                }
            }
        }
        return null;
    }

    public void setPriceRange(List<Map<String, Double>> list) {
        try {
            priceRanges = (list!=null && !list.isEmpty()) ? JSONUtil.serialize(list) : null;
        } catch (JSONException ignored) {}
    }

    public List<Map<String, Double>> getPriceRange() {
        try {
            List<Map<String, Double>> list = (StringUtils.isNotEmpty(priceRanges)) ? (List<Map<String, Double>>) JSONUtil.deserialize(priceRanges) : null;
            return list;
        } catch (JSONException ignored) {}
        return null;
    }

    public void addLabel(ProductLabel label) {
        if (label != null) {
            if (labels == null) setLabels(new HashSet<ProductLabel>());
            if (!labels.contains(label)) labels.add(label);
        }
    }

    public void delLabel(String label) {
        if (StringUtils.isNotEmpty(label) && labels != null)
            for (ProductLabel l : labels)
                if (label.equalsIgnoreCase(l.getCode()))
                    labels.remove(l);
    }

    public boolean isRootCategory() {
        return Category.ROOT_CATEGORY_CODE.equalsIgnoreCase(getUrlCode());
    }

    public boolean equals(Object other) {
        if ((this == other)) return true;
        if (!(other instanceof Category)) return false;
        Category castOther = (Category) other;
        return new EqualsBuilder()
                .append(this.getIdCategory(), castOther.getIdCategory())
                .isEquals();
    }

    public Map<String, Object> toMap(String lang) {
        Map res = new HashMap();
        try {
            Map m = describe();
            res.putAll(m);
        } catch (IllegalAccessException e) {
            log.error(e.getMessage(), e); 
        } catch (InvocationTargetException e) {
            log.error(e.getMessage(), e); 
        } catch (NoSuchMethodException e) {
            log.error(e.getMessage(), e); 
        }
        CategoryLang beanL = getLanguage(lang);
        try {
            Map ml = beanL.describe();
            res.putAll(ml);
        } catch (IllegalAccessException e) {
            log.error(e.getMessage(), e);   //To change body of catch statement use File | Settings | File Templates.
        } catch (InvocationTargetException e) {
            log.error(e.getMessage(), e);   //To change body of catch statement use File | Settings | File Templates.
        } catch (NoSuchMethodException e) {
            log.error(e.getMessage(), e);   //To change body of catch statement use File | Settings | File Templates.
        }
        return res;
    }

    public void fromMap(Map<String, String> m) {
        /*
        CategoryLang beanL = (CategoryLang) getLanguage(lang);
        if (m.containsKey("Parent")) setIdParent(SomeUtils.strToLong(m.get("Parent")));
        if (m.containsKey("Name")) beanL.setCategoryName(m.get("Name"));
        if (m.containsKey("Description")) beanL.setDescription(m.get("Description"));
        if (m.containsKey("Characteristic 1")) beanL.setCaract1(m.get("Characteristic 1"));
        if (m.containsKey("Characteristic 2")) beanL.setCaract2(m.get("Characteristic 2"));
        if (m.containsKey("Characteristic 3")) beanL.setCaract3(m.get("Characteristic 3"));
        if (m.containsKey("Template")) setTemplate(m.get("Template"));
        if (m.containsKey("Date Selection")) setDateSelection(m.get("Date Selection"));
        if (m.containsKey("Time Selection")) setTimeSelection(m.get("Time Selection"));
        if (m.containsKey("Delivery Time")) setDeliveryTime(m.get("Delivery Time"));
        if (m.containsKey("Width")) setDimentionWidth(SomeUtils.strToDouble(m.get("Width")));
        if (m.containsKey("Height")) setDimentionHeight(SomeUtils.strToDouble(m.get("Height")));
        if (m.containsKey("Length")) setDimentionLength(SomeUtils.strToDouble(m.get("Length")));
        if (m.containsKey("Weight")) setWeight(SomeUtils.strToDouble(m.get("Weight")));
        if (m.containsKey("Need Shipping")) setNeedShipping(m.get("Need Shipping"));
        if (m.containsKey("Free Shipping")) setFreeShipping(m.get("Free Shipping"));
        if (m.containsKey("Left Order")) setPosByPlace("Left", SomeUtils.strToInteger(m.get("Left Order")));
        if (m.containsKey("Top Order")) setPosByPlace("Top", SomeUtils.strToInteger(m.get("Top Order")));
        */
    }

}
