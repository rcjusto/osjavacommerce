package org.store.core.beans;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.hibernate.EntityMode;
import org.hibernate.event.EventSource;
import org.store.core.beans.utils.ExportedBean;
import org.store.core.beans.utils.MultiLangBean;
import org.store.core.beans.utils.PageMeta;
import org.store.core.beans.utils.StoreBean;
import org.store.core.dao.HibernateDAO;
import org.store.core.dto.VolumePriceDTO;
import org.store.core.globals.SomeUtils;
import org.store.core.search.LuceneIndexer;

import javax.persistence.*;
import java.lang.reflect.InvocationTargetException;
import java.util.*;


@Entity
@Table(name = "t_product")
public class Product extends BaseBean implements MultiLangBean, ExportedBean, StoreBean {
    public static final String TYPE_DIGITAL = "digital";
    public static final String TYPE_COMPLEMENT = "complement";
    public static final String TYPE_STANDARD = "standard";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idProduct;

    // Codigo del producto en la tienda
    @Column(length = 255)
    private String partNumber;

    // Identificador del producto en las urls amigables
    @Column(length = 1024)
    private String urlCode;

    // Codigo para el proveedor
    @Column(length = 255)
    private String mfgPartnumber;

    // Unique Product Code
    @Column(length = 255)
    private String upc;

    // Tienda en la q esta configurado el producto
    @Column(length = 10)
    private String inventaryCode;

    // Metodo con el que se calcula el precio
    private String priceMethod;

    // Precio del producto
    private Double price;

    // Precio anterior
    private Double oldPrice;

    // Precio de costo
    private Double costPrice;

    // Precio de listado
    private Double listPrice;

    // Precio base calculado para el usuario anonymous
    private Double calculatedPrice;

    // Cantidad en inventario de los proveedores
    private Long stock;

    // Inventario proporcionado por la tienda local
    private Boolean fixedStock;

    // Producto activo
    private Boolean active;
                                                                    
    // Cantidad minima para envio de email de alerta
    private Long stockMin;

    // Numero de veces que se ha vendido
    private Long sales;

    // Numero extra de veces q se ha vendido
    private Long extraSales;

    // Se le aplican impuestos
    private Boolean noTaxable;

    // Tipo de producto (fisico, digital, accesorio)
    @Column(length = 50)
    private String productType;

    // Tiene garantia adicional
    private boolean warranty;

    // Precio de la garantia adicional
    private Double warrantyPrice;

    // % del valor del producto = garantia
    private Double warrantyPercent;

    // Porciento de ganancia para los afiliados
    private Double affiliatePercent;

    // Tiempo para enviar el producto
    @Column(length = 255)
    private String deliveryTime;

    // Palabras claves para la busqueda
    @Lob
    private String searchKeywords;

    // Tamanno del producto
    private Double dimentionLength;
    private Double dimentionWidth;
    private Double dimentionHeight;

    // Peso del producto
    private Double weight;

    // Necesita seleccion de fecha para la venta
    @Column(length = 1)
    private String dateSelection;

    // Necesita seleccion de hora para la venta
    @Column(length = 1)
    private String timeSelection;

    // Fecha en q estara disponible
    @Column(length = 255)
    private String eta;

    // Se incluye en la distribucion
    @Column(length = 1)
    private String needShipping;

    // Tipo de stock (Normal, Open box, Refurbished)
    @Column(length = 5)
    private String stockType;

    // Rating promedio del producto
    private Double ratingBy;

    // Numero de reviews visibles
    private Long reviews;

    // Plantilla en q se muestra el producto
    @Column(length = 255)
    private String productTemplate;

    // Enviar email cuando se venda el producto
    @Column(length = 255)
    private String mailVendor;

    // Factor q se multiplica por el precio de costo para calcular el precio de venta
    private Double markupFactor;

    // Exchange Rate Markup. Factor q se multiplica por el precio de costo para calcular el precio de venta
    private Double erMarkupFactor;

    // NUmero de veces que se ha visto el producto
    private Long hits;

    // Imagen principal
    @Column(length = 50)
    private String mainImage;

    // buying limited by user
    private Long limitPerUser;

    // Max number of downloads
    private Integer maxDownloads;

    // Categoria alternativa, para sincronizacion con sistemas externos
    @Column(length = 250)
    private String altCategory;

    // Archivado, producto viejo
    private Boolean archived;

    private Date lastDateWithStock;
    private Date lastUpdate;

    @Column(length = 1024)
    private String urlManufacturer;

    // Si se adiciona al carro hay q enviar la compra para q nos asignen precio, se le oculta el precio
    @Column(length = 1)
    private String needQuote;

    // si esta false hay q actualizar el indice
    private Boolean indexed;

    @ManyToOne
    private ComplementGroup complementGroup;

    // Categoria de la que hereda las propiedades
    @ManyToOne
    private Category category;

    // Fabricante del producto
    @ManyToOne
    private Manufacturer manufacturer;


    // Datos de idioma
    @OneToMany(mappedBy = "product")
    private Set<ProductLang> productLangs;

    // proveedores
    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY)
    private Set<ProductProvider> productProviders;

    // Archivos digitales asociados al producto
    @ManyToMany
    @JoinTable(name = "t_product_t_resources",
            joinColumns = @JoinColumn(name = "t_product_idProduct"),
            inverseJoinColumns = @JoinColumn(name = "productResources_id"))
    private Set<Resource> productResources;

    // Etiquetas asignadas
    @ManyToMany
    @JoinTable(name = "t_product_t_productlabel",
            joinColumns = @JoinColumn(name = "t_product_idProduct"),
            inverseJoinColumns = @JoinColumn(name = "labels_id"))
    private Set<ProductLabel> labels;

    // Categorias a la que pertenece
    @ManyToMany
    @JoinTable(name = "t_product_t_category",
            joinColumns = @JoinColumn(name = "t_product_idProduct"),
            inverseJoinColumns = @JoinColumn(name = "productCategories_idCategory"))
    private Set<Category> productCategories;

    @ManyToMany
    @JoinTable(name = "t_product_t_complement_group",
            joinColumns = @JoinColumn(name = "t_product_idProduct"),
            inverseJoinColumns = @JoinColumn(name = "relatedGroups_idGroup"))
    private Set<ComplementGroup> relatedGroups;

    @ElementCollection
    @JoinTable(name = "t_product_forusers",
            joinColumns = @JoinColumn(name = "t_product_idProduct")
    )
    private Set<Long> forUsers;

    @Transient
    private String tracePrice;

    public Product() {
        extraSales = 0l;
    }

    public Long getIdProduct() {
        return idProduct;
    }

    public void setIdProduct(Long idProduct) {
        this.idProduct = idProduct;
    }

    public String getUrlCode() {
        return urlCode;
    }

    public void setUrlCode(String urlCode) {
        this.urlCode = urlCode;
    }

    public String getMfgPartnumber() {
        return mfgPartnumber;
    }

    public void setMfgPartnumber(String mfgPartnumber) {
        this.mfgPartnumber = mfgPartnumber;
    }

    public String getInventaryCode() {
        return inventaryCode;
    }

    public void setInventaryCode(String inventaryCode) {
        this.inventaryCode = inventaryCode;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Double getOldPrice() {
        return oldPrice;
    }

    public void setOldPrice(Double oldPrice) {
        this.oldPrice = oldPrice;
    }

    public Double getCostPrice() {
        return costPrice;
    }

    public void setCostPrice(Double costPrice) {
        this.costPrice = costPrice;
    }

    public Double getCalculatedPrice() {
        return calculatedPrice;
    }

    public void setCalculatedPrice(Double calculatedPrice) {
        this.calculatedPrice = calculatedPrice;
    }

    public Long getStock() {
        return (stock != null) ? stock : 0;
    }

    public void setStock(Long stock) {
        this.stock = stock;
    }

    public Boolean getFixedStock() {
        return fixedStock!=null && fixedStock;
    }

    public void setFixedStock(Boolean fixedStock) {
        this.fixedStock = fixedStock;
    }

    public String getPriceMethod() {
        return priceMethod;
    }

    public void setPriceMethod(String priceMethod) {
        this.priceMethod = priceMethod;
    }

    public Long getStockMin() {
        return stockMin;
    }

    public void setStockMin(Long stockMin) {
        this.stockMin = stockMin;
    }

    public Boolean getActive() {
        return active != null && active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Long getSales() {
        return (sales != null) ? sales : 0;
    }

    public void setSales(Long sales) {
        this.sales = sales;
    }

    public Long getExtraSales() {
        return (extraSales != null) ? extraSales : 0;
    }

    public void setExtraSales(Long extraSales) {
        this.extraSales = extraSales;
    }

    public Boolean getNoTaxable() {
        return noTaxable!=null && noTaxable ;
    }

    public boolean isTaxable() {
        return noTaxable==null || !noTaxable;
    }

    public void setNoTaxable(Boolean noTaxable) {
        this.noTaxable = noTaxable;
    }

    public String getProductType() {
        return (productType != null) ? productType : TYPE_STANDARD;
    }

    public void setProductType(String productType) {
        this.productType = productType;
    }

    public boolean isWarranty() {
        return warranty;
    }

    public void setWarranty(boolean warranty) {
        this.warranty = warranty;
    }

    public Double getWarrantyPrice() {
        return warrantyPrice;
    }

    public void setWarrantyPrice(Double warrantyPrice) {
        this.warrantyPrice = warrantyPrice;
    }

    public Double getWarrantyPercent() {
        return warrantyPercent;
    }

    public void setWarrantyPercent(Double warrantyPercent) {
        this.warrantyPercent = warrantyPercent;
    }

    public Double getListPrice() {
        return listPrice;
    }

    public void setListPrice(Double listPrice) {
        this.listPrice = listPrice;
    }

    public Double getAffiliatePercent() {
        return affiliatePercent;
    }

    public void setAffiliatePercent(Double affiliatePercent) {
        this.affiliatePercent = affiliatePercent;
    }

    public String getDeliveryTime() {
        return deliveryTime;
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
        return weight;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }

    public String getDateSelection() {
        return dateSelection;
    }

    public void setDateSelection(String dateSelection) {
        this.dateSelection = dateSelection;
    }

    public String getTimeSelection() {
        return timeSelection;
    }

    public void setTimeSelection(String timeSelection) {
        this.timeSelection = timeSelection;
    }

    public String getEta() {
        return eta;
    }

    public void setEta(String eta) {
        this.eta = eta;
    }

    public String getNeedShipping() {
        return (needShipping != null && ("Y".equalsIgnoreCase(needShipping) || "N".equalsIgnoreCase(needShipping))) ? needShipping : null;
    }

    public void setNeedShipping(String needShipping) {
        this.needShipping = needShipping;
    }

    public String getStockType() {
        return stockType;
    }

    public void setStockType(String stockType) {
        this.stockType = stockType;
    }

    public Double getRatingBy() {
        return ratingBy;
    }

    public void setRatingBy(Double ratingBy) {
        this.ratingBy = ratingBy;
    }

    public Long getReviews() {
        return reviews!=null ? reviews : 0;
    }

    public void setReviews(Long reviews) {
        this.reviews = reviews;
    }

    public String getProductTemplate() {
        return productTemplate;
    }

    public void setProductTemplate(String productTemplate) {
        this.productTemplate = productTemplate;
    }

    public String getMailVendor() {
        return mailVendor;
    }

    public void setMailVendor(String mailVendor) {
        this.mailVendor = mailVendor;
    }

    public String getPartNumber() {
        return partNumber;
    }

    public void setPartNumber(String partNumber) {
        this.partNumber = partNumber;
    }

    public String getUpc() {
        return upc;
    }

    public void setUpc(String upc) {
        this.upc = upc;
    }

    public String getSearchKeywords() {
        return searchKeywords;
    }

    public void setSearchKeywords(String searchKeywords) {
        this.searchKeywords = searchKeywords;
    }

    public Double getMarkupFactor() {
        return markupFactor;
    }

    public void setMarkupFactor(Double markupFactor) {
        this.markupFactor = markupFactor;
    }

    public Double getErMarkupFactor() {
        return erMarkupFactor;
    }

    public void setErMarkupFactor(Double erMarkupFactor) {
        this.erMarkupFactor = erMarkupFactor;
    }

    public Long getHits() {
        return hits;
    }

    public void setHits(Long hits) {
        this.hits = hits;
    }

    public String getMainImage() {
        return mainImage;
    }

    public void setMainImage(String mainImage) {
        this.mainImage = mainImage;
    }

    public Long getLimitPerUser() {
        return limitPerUser;
    }

    public void setLimitPerUser(Long limitPerUser) {
        this.limitPerUser = limitPerUser;
    }

    public Integer getMaxDownloads() {
        return maxDownloads;
    }

    public void setMaxDownloads(Integer maxDownloads) {
        this.maxDownloads = maxDownloads;
    }

    public String getUrlManufacturer() {
        return urlManufacturer;
    }

    public void setUrlManufacturer(String urlManufacturer) {
        this.urlManufacturer = urlManufacturer;
    }

    public String getNeedQuote() {
        return needQuote;
    }

    public void setNeedQuote(String needQuote) {
        this.needQuote = needQuote;
    }

    public Date getLastDateWithStock() {
        return lastDateWithStock;
    }

    public void setLastDateWithStock(Date lastDateWithStock) {
        this.lastDateWithStock = lastDateWithStock;
    }

    public Date getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(Date lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public String getAltCategory() {
        return altCategory;
    }

    public void setAltCategory(String altCategory) {
        this.altCategory = altCategory;
    }

    public Boolean getIndexed() {
        return indexed;
    }

    public void setIndexed(Boolean indexed) {
        this.indexed = indexed;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public Manufacturer getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(Manufacturer manufacturer) {
        this.manufacturer = manufacturer;
    }

    public Set<ProductLabel> getLabels() {
        return labels;
    }

    public void setLabels(Set<ProductLabel> labels) {
        this.labels = labels;
    }

    public Set<ProductLang> getProductLangs() {
        return productLangs;
    }

    public void setProductLangs(Set<ProductLang> productLangs) {
        this.productLangs = productLangs;
    }

    public Set<Category> getProductCategories() {
        return productCategories;
    }

    public void setProductCategories(Set<Category> productCategories) {
        this.productCategories = productCategories;
    }

    public Set<Resource> getProductResources() {
        return productResources;
    }

    public void setProductResources(Set<Resource> productResources) {
        this.productResources = productResources;
    }

    public ComplementGroup getComplementGroup() {
        return complementGroup;
    }

    public void setComplementGroup(ComplementGroup complementGroup) {
        this.complementGroup = complementGroup;
    }

    public Set<ComplementGroup> getRelatedGroups() {
        return relatedGroups;
    }

    public void setRelatedGroups(Set<ComplementGroup> relatedGroups) {
        this.relatedGroups = relatedGroups;
    }

    public Set<ProductProvider> getProductProviders() {
        return productProviders;
    }

    public void setProductProviders(Set<ProductProvider> productProviders) {
        this.productProviders = productProviders;
    }

    public Set<Long> getForUsers() {
        return forUsers;
    }

    public void setForUsers(Set<Long> forUsers) {
        this.forUsers = forUsers;
    }

    public Boolean getArchived() {
        return archived!=null && archived;
    }

    public void setArchived(Boolean archived) {
        this.archived = archived;
    }

    public void addStock(int cant) {
        if (!Product.TYPE_DIGITAL.equalsIgnoreCase(getProductType()))
            setStock(getStock() + cant);
    }

    public void addSales(int cant) {
        setSales(getSales() + cant);
    }

    public String getTracePrice() {
        return tracePrice;
    }

    public static final String[] getFieldsToSearch() {
        return new String[]{"name", "inventaryCode", "productType", "partNumber", "mfgPartNumber", "category_name", "manufacturer_name", "active", "stock", "eta"};
    }

// Languages

    public ProductLang getLanguage(String lang) {
        if (lang==null) lang="";
        if (getProductLangs() != null) {
            for (ProductLang bean : getProductLangs()) {
                if (lang.equals(bean.getProductLang())) return bean;
            }
        }
        return null;
    }

    public ProductLang getLanguage(String lang, String defLang) {
        if (lang==null) lang="";
        if (getProductLangs() != null) {
            for (ProductLang categoryLang : getProductLangs()) {
                if (lang.equals(categoryLang.getProductLang()) && categoryLang.getProductName() != null) {
                    return categoryLang;
                }
            }
            for (ProductLang categoryLang : getProductLangs()) {
                if (defLang.equals(categoryLang.getProductLang())) {
                    return categoryLang;
                }
            }
        }
        return null;
    }

    public String getProductName(String lang) {
        if (lang==null) lang="";
        if (getProductLangs() != null) {
            for (ProductLang productLang : getProductLangs()) {
                if (lang.equals(productLang.getProductLang()) && StringUtils.isNotEmpty(productLang.getProductName())) {
                    return productLang.getProductName();
                }
            }
            for (ProductLang productLang : getProductLangs()) {
                if (StringUtils.isNotEmpty(productLang.getProductName())) {
                    return productLang.getProductName();
                }
            }
        }
        return "";
    }

    public PageMeta getProductMeta(String meta, String lang) {
        if (lang==null) lang="";
        if (getProductLangs() != null) {
            for (ProductLang productLang : getProductLangs()) {
                if (lang.equals(productLang.getProductLang()) && StringUtils.isNotEmpty(productLang.getMetaValue(meta))) {
                    return productLang.getMeta(meta);
                }
            }
            for (ProductLang productLang : getProductLangs()) {
                if (StringUtils.isNotEmpty(productLang.getMetaValue(meta))) {
                    return productLang.getMeta(meta);
                }
            }
        }
        return null;
    }

    // PRICES
    private void addTracePrice(String cad) {
        if (tracePrice == null) tracePrice = cad;
        else tracePrice += cad;
        tracePrice += "<br/>";
    }

    public double getBasePrice() {
        return getBasePrice(null);
    }

    public double getBasePrice(ProductVariation variation) {
        return getAction()!=null ? getBasePrice(variation, getAction().getDao()) : 0d;
    }

    public double getBasePrice(ProductVariation variation, HibernateDAO dao) {
        tracePrice = null;
        Double mf = SomeUtils.strToDouble(dao.getParentPropertyValue(this, "markupFactor"));
        Double ermf = SomeUtils.strToDouble(dao.getParentPropertyValue(this, "erMarkupFactor"));
        if (mf == null) mf = 1.0d;
        if (ermf == null) ermf = 1.0d;
        if (price != null) {
            double res = price;
            addTracePrice("Base price is fixed: " + price.toString());
            if (variation != null && variation.getPriceInc() != null) res += variation.getPriceInc();
            else if (variation != null && variation.getCostPriceInc() != null) res += variation.getCostPriceInc() * mf * ermf;
            return res;
        } else if (costPrice != null) {
            double res = costPrice;
            addTracePrice("Base price is calculated CostPrice: " + costPrice.toString() + ", MF: " + mf.toString() + ", ERMF: " + ermf.toString());
            res *=  mf * ermf;
            if (variation != null && variation.getPriceInc() != null) res += variation.getPriceInc();
            else if (variation != null && variation.getCostPriceInc() != null) res += variation.getCostPriceInc() * mf * ermf;
            return res;
        } else return 0.0;
    }


    public Map<String, Object> getPriceMap(UserLevel level, int cartQuantity) {
        return getPriceMap(level, cartQuantity, null);
    }

    public Map<String, Object> getPriceMap(UserLevel level, int cartQuantity, ProductVariation variation) {
        Map<String, Object> map = new HashMap<String, Object>();
        double bp = getBasePrice(variation);
        map.put("BASE_PRICE", bp);

        // discount
        double offerDiscount = 0;
        ProductOffer selectedOffer = null;
        List<ProductOffer> offers = getAction().getDao().getOffersForProduct(this, true);
        if (offers != null && offers.size() > 0) {
            for (ProductOffer offer : offers) {
                if (offer.getDiscount() != null && offer.getDiscount() > offerDiscount) {
                    offerDiscount = offer.getDiscount();
                    selectedOffer = offer;
                }
                if (offer.getDiscountPercent() != null && offer.getDiscountPercent() * bp > offerDiscount) {
                    offerDiscount = offer.getDiscountPercent() * bp;
                    selectedOffer = offer;
                }
            }
        }
        if (selectedOffer != null) {
            map.put("OFFER", selectedOffer);
            addTracePrice("Offer ID: " + selectedOffer.getIdOffer().toString() + ", Value: " + String.valueOf(offerDiscount));
        }
        map.put("OFFER_DISCOUNT", offerDiscount);

        // member type
        double levelDiscountFactor = 1.0;
        if (level != null) {
            Double d = getAction().getDao().getParentProductUserLevelPercent(this, level);
            if (d != null && d > 0) {
                levelDiscountFactor = d;
                addTracePrice("User Level Discount: " + level.getCode() + ", Value: " + d.toString());
            }
        }
        map.put("LEVEL_DISOCUNT", levelDiscountFactor);


        // Volume Pricing
        double volumeFactor = 1;
        // - tiene que haber mas de un producto en el carro
        boolean applyVolume = (cartQuantity > 0);
        // - si hay oferta, tiene q ser Y la propiedad volumne_and_offer
        applyVolume = applyVolume && (selectedOffer == null || !"n".equalsIgnoreCase(getAction().getDao().getStorePropertyValue(StoreProperty.PROP_PRICE_OFFER_AND_VOLUME, StoreProperty.TYPE_GENERAL, StoreProperty.PROP_DEFAULT_PRICE_OFFER_AND_VOLUME)));
        // - si hay usuario verificar su nivel, sino verificar anonymous volume
        if (level != null) {applyVolume = applyVolume && !level.getDisableVolume();} else {applyVolume = applyVolume && getAction().getDao().anonymousVolumePrice();}

        Long volumeId = null;
        String volumeType = null;
        if (applyVolume) {
            List<VolumePriceDTO> listaVolumen = new ArrayList<VolumePriceDTO>();
            List<ProductVolume> volumesPro = getAction().getDao().getProductVolume(this);
            if (volumesPro != null && volumesPro.size() > 0) {
                for (ProductVolume pv : volumesPro) {
                    listaVolumen.add(new VolumePriceDTO(pv, bp * pv.getPercentDiscount() * levelDiscountFactor - offerDiscount));
                    if (pv.getVolume() <= cartQuantity && pv.getPercentDiscount() < volumeFactor) {
                        volumeFactor = pv.getPercentDiscount();
                        volumeId = pv.getId();
                        volumeType = "Product";
                    }
                }
            } else if (category != null) {
                List<CategoryVolume> volumesCat = getAction().getDao().getParentCategoryVolume(category);
                for (CategoryVolume cv : volumesCat) {
                    listaVolumen.add(new VolumePriceDTO(cv, bp * cv.getPercentDiscount() * levelDiscountFactor - offerDiscount));
                    if (cv.getVolume() <= cartQuantity && cv.getPercentDiscount() < volumeFactor) {
                        volumeFactor = cv.getPercentDiscount();
                        volumeId = cv.getId();
                        volumeType = "Category";
                    }
                }
            }
            map.put("VOLUME_LIST", listaVolumen);
        }
        if (volumeId != null) {
            addTracePrice("Volume Type: " + volumeType + ", ID: " + volumeId.toString() + ", Value: " + String.valueOf(volumeFactor));
        }
        Double finalPrice = bp * volumeFactor * levelDiscountFactor;
        map.put("FINAL_PRICE", finalPrice);

        Double finalOfferPrice = finalPrice - offerDiscount;
        map.put("FINAL_OFFER_PRICE", finalOfferPrice);

        if (listPrice != null && listPrice > 0 && listPrice > finalOfferPrice) map.put("SAVE_FROM_LIST", listPrice - finalOfferPrice);

        // sino se mando una variacion, entonces calcular precio para cada variacion
        if (variation == null) {
            List<ProductVariation> listaVar = getAction().getDao().getProductVariations(this);
            if (listaVar != null && listaVar.size() > 0) {
                for (ProductVariation pVar : listaVar)
                    if (pVar.getCostPriceInc() != null || pVar.getPriceInc() != null) {
                        Double bpVar = getBasePrice(pVar);
                        Double varIncPrice = bpVar * volumeFactor * levelDiscountFactor - finalPrice ;
                        map.put("VARIATION_INC_" + pVar.getId(), varIncPrice);
                    }
            }
        }

        return map;
    }

    public double getFinalPrice(UserLevel level, int cartQuantity) {
        return getFinalPrice(level, cartQuantity, null, getAction().getDao());
    }

    public double getFinalPrice(UserLevel level, int cartQuantity, ProductVariation pVar, HibernateDAO dao) {
        double bp = getBasePrice(pVar, dao);
        // discount
        double offerDiscount = 0;
        Long offerId = null;
        List<ProductOffer> offers = dao.getOffersForProduct(this, true);
        if (offers != null && offers.size() > 0) {
            for (ProductOffer offer : offers) {
                if (offer.getDiscount() != null && offer.getDiscount() > offerDiscount) {
                    offerDiscount = offer.getDiscount();
                    offerId = offer.getIdOffer();
                }
                if (offer.getDiscountPercent() != null && offer.getDiscountPercent() * bp > offerDiscount) {
                    offerDiscount = offer.getDiscountPercent() * bp;
                    offerId = offer.getIdOffer();
                }
            }
        }

        double volumeFactor = 1;
        // Determinar si se puede aplicar Volume Pricing
        // - tiene que haber mas de un producto en el carro
        boolean applyVolume = (cartQuantity > 1);
        // - si hay oferta, tiene q ser Y la propiedad volumne_and_offer 
        applyVolume = applyVolume && (offerId == null || !"n".equalsIgnoreCase(dao.getStorePropertyValue(StoreProperty.PROP_PRICE_OFFER_AND_VOLUME, StoreProperty.TYPE_GENERAL, StoreProperty.PROP_DEFAULT_PRICE_OFFER_AND_VOLUME)));
        // - si hay usuario verificar su nivel, sino verificar anonymous volume
        if (level != null) {applyVolume = applyVolume && !level.getDisableVolume();} else {applyVolume = applyVolume && dao.anonymousVolumePrice();}

        Long volumeId = null;
        String volumeType = null;
        if (applyVolume) {
            List<ProductVolume> volumesPro = dao.getProductVolume(this);
            if (volumesPro != null && volumesPro.size() > 0) {
                for (ProductVolume pv : volumesPro) {
                    if (pv.getVolume() <= cartQuantity && pv.getPercentDiscount() < volumeFactor) {
                        volumeFactor = pv.getPercentDiscount();
                        volumeId = pv.getId();
                        volumeType = "Product";
                    }
                }
            } else if (category != null) {
                List<CategoryVolume> volumesCat = dao.getParentCategoryVolume(category);
                for (CategoryVolume cv : volumesCat) {
                    if (cv.getVolume() <= cartQuantity && cv.getPercentDiscount() < volumeFactor) {
                        volumeFactor = cv.getPercentDiscount();
                        volumeId = cv.getId();
                        volumeType = "Category";
                    }
                }
            }
        }
        if (volumeId != null) {
            addTracePrice("Volume Type: " + volumeType + ", ID: " + volumeId.toString() + ", Value: " + String.valueOf(volumeFactor));
        }

        // member type
        double levelDiscountFactor = 1.0;
        if (level != null) {
            Double d = dao.getParentProductUserLevelPercent(this, level);
            if (d != null && d > 0) {
                levelDiscountFactor = d;
                addTracePrice("User Level Discount: " + level.getCode() + ", Value: " + d.toString());
            }
        }

        if (offerId != null) {
            addTracePrice("Offer ID: " + offerId.toString() + ", Value: " + String.valueOf(offerDiscount));
        }

        return bp * volumeFactor * levelDiscountFactor - offerDiscount;
    }

    /*
    public boolean canAddToCart(int n) {
        if (!getActive()) return false;
        String buyStockProp = getAction().getDao().getStorePropertyValue(StoreProperty.PROP_PRODUCT_BUY_UNAVAILABLE, StoreProperty.TYPE_GENERAL, StoreProperty.PROP_DEFAULT_PRODUCT_BUY_UNAVAILABLE);
        return StoreProperty.PROP_PRODUCT_HAS_STOCK_AND_PRICE.equalsIgnoreCase(buyStockProp) && getStock() >= n && getBasePrice()>0 ||
                StoreProperty.PROP_PRODUCT_HAS_STOCK.equalsIgnoreCase(buyStockProp) && getStock() >= n ||
                StoreProperty.PROP_PRODUCT_HAS_STOCK_OR_ETA.equalsIgnoreCase(buyStockProp) && (getStock() >= n || StringUtils.isNotEmpty(eta)) ||
                StoreProperty.PROP_PRODUCT_ALL_ACTIVE.equalsIgnoreCase(buyStockProp);
    }
    */

    public boolean canShow() {
        // verificar si esta activo
        if (!getActive()) return false;

        // si no es publico, validar q este habilitado al nivel de usuario
        if (!getPublic() && !getForUsers().contains(getAction().getFrontUserLevel().getId())) return false;

        // validar stock y ETA
        String showStockProp = getAction().getDao().getStorePropertyValue(StoreProperty.PROP_PRODUCT_SHOW_UNAVAILABLE, StoreProperty.TYPE_GENERAL, StoreProperty.PROP_DEFAULT_PRODUCT_SHOW_UNAVAILABLE);
        return StoreProperty.PROP_PRODUCT_HAS_STOCK.equalsIgnoreCase(showStockProp) && getStock() >= 1 ||
                StoreProperty.PROP_PRODUCT_HAS_STOCK_OR_ETA.equalsIgnoreCase(showStockProp) && (getStock() >= 1 || StringUtils.isNotEmpty(eta)) ||
                StoreProperty.PROP_PRODUCT_ALL_ACTIVE.equalsIgnoreCase(showStockProp);
    }

    public boolean belongToCategory(Category cat) {
        Category c = getCategory();
        if (c != null && c.equals(cat)) return true;
        while (c != null && c.getIdParent() != null) {
            c = getAction().getDao().getCategory(c.getIdParent());
            if (c != null && c.equals(cat)) return true;
        }
        return false;
    }

    public Boolean getPublic() {
        return forUsers==null || forUsers.isEmpty();
    }

    public Long getMaxToBuy(User u) {
        Long res = SomeUtils.strToLong(getAction().getDao().getStorePropertyValue(StoreProperty.PROP_PRODUCT_BUY_MAX, StoreProperty.TYPE_GENERAL, StoreProperty.PROP_DEFAULT_PRODUCT_BUY_MAX));

        // Tiene que estar activo
        if (!getActive()) return 0l;

        // Dependiendo de las propiedades del comercio se usa el stock como limite
        String buyStockProp = getAction().getDao().getStorePropertyValue(StoreProperty.PROP_PRODUCT_BUY_UNAVAILABLE, StoreProperty.TYPE_GENERAL, StoreProperty.PROP_DEFAULT_PRODUCT_BUY_UNAVAILABLE);
        if (StoreProperty.PROP_PRODUCT_HAS_STOCK_AND_PRICE.equalsIgnoreCase(buyStockProp)) {
            res = getBasePrice()>0 ? getStock() : 0l;
        } else if (StoreProperty.PROP_PRODUCT_HAS_STOCK.equalsIgnoreCase(buyStockProp)) {
            res = getStock();
        } else if (StoreProperty.PROP_PRODUCT_HAS_STOCK_OR_ETA.equalsIgnoreCase(buyStockProp) && StringUtils.isEmpty(eta)) {
            res = getStock();
        }

        // Si tiene limite por usuario
        if (limitPerUser != null) {
            // No hay usuario conectado
            if (u == null) return 0l;
            else {
                int l = getAction().getDao().getNumBuyedProduct(this, u);
                res = (res != null) ? Math.min(res, limitPerUser - l) : limitPerUser - l;
            }
        }

        return res!=null ? res : Long.MAX_VALUE;
    }

    // EXPORT OPTIONS

    public Map<String, Object> toMap(String lang) {
        Map res = new HashMap();
        try {
            Map m = describe();
            res.putAll(m);

            ProductLang beanL = getLanguage(lang);
            if (beanL != null) {
                Map ml = beanL.describe();
                for (Object key : ml.keySet()) res.put("lang." + key, ml.get(key));
            }

            if (category != null) {
                StringBuilder buff = new StringBuilder();
                List<Category> lCat = getAction().getDao().getCategoryHierarchy(category);
                if (lCat != null && !lCat.isEmpty()) {
                    for (Category c : lCat) {
                        if (StringUtils.isNotEmpty(buff.toString())) buff.append(" -> ");
                        buff.append(c.getCategoryName(lang));
                    }
                }
                res.put("categories", buff.toString());
                Map ml = category.toMap(lang);
                for (Object key : ml.keySet()) res.put("category." + key, ml.get(key));
            }

            if (manufacturer != null) {
                Map ml = manufacturer.toMap(lang);
                for (Object key : ml.keySet()) res.put("manufacturer." + key, ml.get(key));
            }

            if (getAction() != null) {
                List<ProductProvider> l = getAction().getDao().getProductProviders(this);
                if (l != null && !l.isEmpty()) {
                    for (ProductProvider pp : l)
                        if (pp.getProvider() != null && StringUtils.isNotEmpty(pp.getProvider().getProviderName())) {
                            if (StringUtils.isNotEmpty(pp.getSku())) res.put("supplier.sku." + pp.getProvider().getProviderName(), pp.getSku());
                            if (pp.getActive() != null) res.put("supplier.active." + pp.getProvider().getProviderName(), pp.getActive().toString());
                            if (pp.getStock() != null) res.put("supplier.stock." + pp.getProvider().getProviderName(), pp.getStock().toString());
                            if (pp.getCostPrice() != null) res.put("supplier.cost." + pp.getProvider().getProviderName(), pp.getCostPrice().toString());
                            if (pp.getCostCurrency() != null) res.put("supplier.currency." + pp.getProvider().getProviderName(), pp.getCostCurrency().getCode());
                        }
                }
            }

        } catch (IllegalAccessException e) {
            log.error(e.getMessage(), e); 
        } catch (InvocationTargetException e) {
            log.error(e.getMessage(), e); 
        } catch (NoSuchMethodException e) {
            log.error(e.getMessage(), e); 
        }

        return res;

    }

    public Map<String, Object> equalMap(Map<String, String> map, String lang) {
        Map<String, Object> res = new HashMap<String, Object>();
        Map productMap = toMap(lang);
        for (String key : map.keySet()) {
            if (StringUtils.isEmpty(map.get(key)) && (!productMap.containsKey(key) || productMap.get(key) == null || StringUtils.isEmpty(productMap.get(key).toString()))) {} else if (!new EqualsBuilder().append(normalizeZero(map.get(key)), normalizeZero(productMap.get(key))).isEquals()) {
                res.put(key, new Object[]{productMap.get(key), map.get(key)});
            }
        }
        return res;
    }

    private String normalizeZero(Object cad) {
        return (cad == null || "0".equals(cad) || "0.0".equals(cad) || "0.00".equals(cad)) ? "" : cad.toString();
    }

    public void fromMap(Map<String, String> m) {
        try {
            fixDecimal(m,"affiliatePercent");
            fixDecimal(m,"costPrice");
            fixDecimal(m,"dimentionHeight");
            fixDecimal(m,"dimentionLength");
            fixDecimal(m,"dimentionWidth");
            fixDecimal(m,"erMarkupFactor");
            fixDecimal(m,"listPrice");
            fixDecimal(m,"markupFactor");
            fixDecimal(m,"oldPrice");
            fixDecimal(m,"price");
            fixDecimal(m,"ratingBy");
            fixDecimal(m,"warrantyPercent");
            fixDecimal(m,"warrantyPrice");
            fixDecimal(m, "weight");
            removeIfEmpty(m,"extraSales");
            removeIfEmpty(m,"hits");
            removeIfEmpty(m,"limitPerUser");
            removeIfEmpty(m,"sales");
            removeIfEmpty(m,"stock");
            removeIfEmpty(m,"stockMin");
            removeIfEmpty(m,"reviews");
            removeIfEmpty(m,"lastDateWithStock");
            removeIfEmpty(m,"lastUpdate");
            removeIfEmpty(m,"maxDownloads");
            removeIfEmpty(m,"active");
            removeIfEmpty(m,"archived");
            removeIfEmpty(m,"fixedStock");
            removeIfEmpty(m,"warranty");
            removeIfEmpty(m,"noTaxable");

            BeanUtils.populate(this, m);
        } catch (IllegalAccessException e) {
            log.error(e.getMessage(), e); 
        } catch (InvocationTargetException e) {
            log.error(e.getMessage(), e); 
        }
    }

    private void fixDecimal(Map<String, String> m, String field) {
        if (m.containsKey(field) && m.get(field)!=null) {
            if (StringUtils.isEmpty(m.get(field))) m.remove(field);
            else {
                Double d = SomeUtils.forceStrToDouble(m.get(field));
                m.put(field, d.toString());
            }
        }
    }

    private void removeIfEmpty(Map<String, String> m, String field) {
        if (m.containsKey(field) && StringUtils.isEmpty(m.get(field))) m.remove(field);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("idProduct", getIdProduct())
                .toString();
    }

    @Override
    public boolean equals(Object other) {
        if ((this == other)) return true;
        if (!(other instanceof Product)) return false;
        Product castOther = (Product) other;
        return new EqualsBuilder()
                .append(this.getIdProduct(), castOther.getIdProduct())
                .isEquals();
    }

    public void addHit() {
        if (hits == null) hits = 0l;
        hits++;
    }

    public boolean hasLabel(String label) {
        if (StringUtils.isNotEmpty(label) && labels != null)
            for (ProductLabel l : labels)
                if (label.equalsIgnoreCase(l.getCode())) return true;
        return false;
    }

    // Product has label: FREE SHIPPING
    public boolean hasFreeShipping() {
        return hasLabel(ProductLabel.LABEL_FREE_SHIPPING);
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

    public Resource getResource(String type) {
        if (StringUtils.isNotEmpty(type) && productResources != null && productResources.size() > 0) {
            Date hoy = SomeUtils.dateIni(SomeUtils.today());
            for (Resource r : productResources) {
                if (type.equalsIgnoreCase(r.getResourceType())) {
                    if (r.getResourceDate() == null || hoy.before(r.getResourceDate())) return r;
                }
            }
        }
        return null;
    }

    public Resource getRebate() {
        return getResource(Resource.TYPE_REBATE);
    }

    public ProductProvider getProductProvider(Provider provider) {
        if (provider!=null && productProviders!=null && !productProviders.isEmpty()) {
            for(ProductProvider pp : productProviders)
                if (provider.equals(pp.getProvider())) return pp;
        }
        return null;
    }

    public boolean updateCalculatedPrice(HibernateDAO dao) {
        Double bp = getBasePrice(null, dao);

        UserLevel level = dao.getUserLevel(UserLevel.ANONYMOUS_LEVEL);
        if (level != null) {
            Double d = dao.getParentProductUserLevelPercent(this, level);
            if (d != null && d > 0) bp *= d;
        }

        if (!bp.equals(calculatedPrice)) {
            calculatedPrice = bp;
            return true;
        }
        return false;
    }

    @Override
    public boolean handlePreUpdate(EventSource session, boolean isNew) {
        lastUpdate = Calendar.getInstance().getTime();
        return super.handlePreUpdate(session, isNew);
    }


    @Override
    public void handlePostDelete(EventSource session) {
        HibernateDAO dao = new HibernateDAO(session, getInventaryCode());
        LuceneIndexer indexer = new LuceneIndexer(dao.getStorePath(), dao.getLanguages(), dao.getDefaultLanguage());
        indexer.deleteProduct(this);
        super.handlePostDelete(session);
    }


}
