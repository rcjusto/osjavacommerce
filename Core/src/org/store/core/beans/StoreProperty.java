package org.store.core.beans;

import org.store.core.beans.utils.StoreBean;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.struts2.json.JSONException;
import org.apache.struts2.json.JSONUtil;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.util.HashMap;
import java.util.Map;

/**
 * Propiedades
 */
@Entity
@Table(name = "t_store_properties")
public class StoreProperty extends BaseBean implements StoreBean {
    // Tipos de propiedades
    public static final String TYPE_GENERAL = "GENERAL";

    // Propiedades principales
    public static final String PROP_TEMPLATE = "site.template";
    public static final String PROP_DEFAULT_TEMPLATE = "default";
    
    public static final String PROP_SKIN = "site.skin";
    public static final String PROP_DEFAULT_SKIN = "default";

    public static final String PROP_LANGUAGES = "languages";
    public static final String PROP_DEFAULT_LANGUAGE = "language.default";
    public static final String PROP_ADMIN_LANGUAGE = "language.admin";
    public static final String PROP_DIMENSION_UNIT = "dimension.unit";
    public static final String PROP_WEIGHT_UNIT = "weight.unit";
    public static final String PROP_DEFAULT_DIMENSION_UNIT = "Inch";
    public static final String PROP_DEFAULT_WEIGHT_UNIT = "Pound";

    public static final String PROP_DEFAULT_COUNTRY = "country.default";
   
    public static final String PROP_PRODUCT_IMAGES_ZOOM = "images.product.zoom";
    public static final String PROP_DEFAULT_PRODUCT_IMAGES_ZOOM = "500x500";
    public static final String PROP_PRODUCT_IMAGES_DETAIL = "images.product.detail";
    public static final String PROP_DEFAULT_PRODUCT_IMAGES_DETAIL = "300x300";
    public static final String PROP_PRODUCT_IMAGES_LIST = "images.product.list";
    public static final String PROP_DEFAULT_PRODUCT_IMAGES_LIST = "100x100";
    public static final String PROP_PRODUCT_IMAGES_MAX_WIDTH = "images.max.width";
    public static final String PROP_PRODUCT_IMAGES_MAX_HEIGHT = "images.max.height";
    public static final String PROP_DEFAULT_PRODUCT_IMAGES_MAX_WIDTH = "800";
    public static final String PROP_DEFAULT_PRODUCT_IMAGES_MAX_HEIGHT = "800";

    public static final String PROP_CUSTOMERS_SAVE_PASSWORD_ENCRYPTED = "customer.save.password.encripted";
    public static final String PROP_DEFAULT_CUSTOMERS_SAVE_PASSWORD_ENCRYPTED = "y";

    public static final String PROP_MAIL_SSL = "mail.ssl";
    public static final String PROP_MAIL_TLS = "mail.tls";
    public static final String PROP_MAIL_PORT = "mail.port";
    public static final String PROP_MAIL_HOST = "mail.host";
    public static final String PROP_MAIL_LOCALHOST = "mail.localhost";
    public static final String PROP_MAIL_USER = "mail.user";
    public static final String PROP_MAIL_PASSWORD = "mail.password";
    public static final String PROP_MAIL_REPLY = "mail.reply";
    public static final String PROP_MAIL_FROM = "mail.from";
    public static final String PROP_MAIL_BCC = "mail.bcc";
    public static final String PROP_MAIL_ATTEMPTS = "mail.attempts";
    public static final String PROP_MAIL_STOCK_ALERT = "mail.stock.alert";
    public static final String PROP_MAIL_REQUEST_QUOTE = "mail.request.quote";
    public static final String PROP_MAIL_CONTACT_US = "mail.contact.us";
    public static final String PROP_MAIL_FRONT_NEWSLETTER = "mail.from.newsletter";
    public static final String PROP_MAIL_DEBUG = "mail.debug";

    public static final String PROP_DEFAULT_CURRENCY = "currency.default";

    public static final String PROP_PRICE_OFFER_AND_VOLUME = "price.offer.and.volume";
    public static final String PROP_DEFAULT_PRICE_OFFER_AND_VOLUME = "n";

    public static final String PROP_PRODUCT_BUY_UNAVAILABLE = "product.buy.unavailable"; // VALUES: has-stock, has-stock-or-eta, all-active
    public static final String PROP_PRODUCT_SHOW_UNAVAILABLE = "product.show.unavailable"; // VALUES: has-stock, has-stock-or-eta, all-active
    public static final String PROP_PRODUCT_HAS_STOCK_AND_PRICE = "has-stock-and-price";
    public static final String PROP_PRODUCT_HAS_STOCK = "has-stock";
    public static final String PROP_PRODUCT_HAS_STOCK_OR_ETA = "has-stock-or-eta";
    public static final String PROP_PRODUCT_ALL_ACTIVE = "all-active";
    public static final String PROP_DEFAULT_PRODUCT_SHOW_UNAVAILABLE = PROP_PRODUCT_ALL_ACTIVE;
    public static final String PROP_DEFAULT_PRODUCT_BUY_UNAVAILABLE = PROP_PRODUCT_HAS_STOCK;

    public static final String PROP_PRODUCT_REVIEW_APPROVE_AUTO = "product.reviews.automatic.approve";
    public static final String PROP_DEFAULT_PRODUCT_REVIEW_APPROVE_AUTO = "n";

    public static final String PROP_PRODUCT_BUY_MAX = "product.buy.max";
    public static final String PROP_DEFAULT_PRODUCT_BUY_MAX = "100";

    public static final String PROP_FLAT_RATE_MAX = "flat.rate.max";
    public static final String PROP_DEFAULT_FLAT_RATE_MAX = "5";

    public static final String PROP_MAX_PROMOTIONAL_CODES = "promotional.codes.maximun";
    public static final String PROP_DEFAULT_MAX_PROMOTIONAL_CODES = "3";

    public static final String PROP_MAX_PRODUCT_DOWNLOADS = "product.downloads.maximun";
    public static final String PROP_DEFAULT_MAX_PRODUCT_DOWNLOADS = "3";

    public static final String PROP_SHOPCART_CROSS_SELLING_NUMBER = "shopcart.related.items";
    public static final String PROP_DEFAULT_SHOPCART_CROSS_SELLING_NUMBER = "12";

    public static final String PROP_PAYMENT_USE_CVD = "payment.use.cvd";
    public static final String PROP_DEFAULT_PAYMENT_USE_CVD = "Y";
    public static final String PROP_PAYMENT_USE_AVS = "payment.use.avs";
    public static final String PROP_DEFAULT_PAYMENT_USE_AVS = "N";

    public static final String PROP_USE_COMBINED_STOCK = "use.combined.stock";
    public static final String PROP_DEFAULT_USE_COMBINED_STOCK = "N";

    public static final String PROP_ALLOW_ANONYMOUS_CHECKOUT = "allow.anonymous.checkout";
    public static final String PROP_DEFAULT_ALLOW_ANONYMOUS_CHECKOUT = "N";

    public static final String PROP_ALLOW_PICK_IN_STORE = "allow.pick.in.store";
    public static final String PROP_DEFAULT_ALLOW_PICK_IN_STORE = "Y";

    public static final String PROP_SHOW_PRICE_WITHOUT_LOGIN = "show.price.without.login";
    public static final String PROP_DEFAULT_SHOW_PRICE_WITHOUT_LOGIN = "Y";
    public static final String PROP_SHOW_STOCK_WITHOUT_LOGIN = "show.stock.without.login";
    public static final String PROP_DEFAULT_SHOW_STOCK_WITHOUT_LOGIN = "Y";

    public static final String PROP_AUTOGENERATE_METAS = "autogenerate.metas";
    public static final String PROP_DEFAULT_AUTOGENERATE_METAS = "Y";


    public static final String PROP_SITE_URL = "site.url";
    public static final String PROP_SITE_HOST = "site.host";
    public static final String PROP_SITE_NAME = "site.name";
    public static final String PROP_SITE_PATH = "site.path";

    public static final String UNIT_DIMENSION_CENTIMETER = "cm";
    public static final String UNIT_DIMENSION_METER = "m";
    public static final String UNIT_DIMENSION_INCH = "Inch";
    public static final String UNIT_WEIGHT_POUND = "Pound";
    public static final String UNIT_WEIGHT_KILOGRAM = "Kg";

    public static final String PROP_ALLOW_REGISTRATIONS = "allow.registrations";
    public static final String PROP_DEFAULT_ALLOW_REGISTRATIONS = "Y";
    public static final String PROP_ALLOW_NEWSLETTERS = "allow.newsletters";
    public static final String PROP_DEFAULT_ALLOW_NEWSLETTERS = "Y";

    public static final String PROP_USE_SECURED_PAGES = "use.secured.pages";
    public static final String PROP_DEFAULT_USE_SECURED_PAGES = "N";

    public static final String PROP_CAN_REORDER = "can.re.order";
    public static final String PROP_DEFAULT_CAN_REORDER = "Y";

    public static final String PROP_CAN_RMA = "can.do.rma";
    public static final String PROP_DEFAULT_CAN_RMA = "Y";

    public static final String PROP_DEFAULT_USER_LEVEL = "default.user.level";
    public static final String PROP_SHOW_USER_LEVEL = "show.user.level";
    public static final String PROP_DEFAULT_SHOW_USER_LEVEL = "Y";

    public static final String PROP_EXPORT_PRODUCT_PROFILES = "export.product.profiles";
    public static final String PROP_EXPORT_CUSTOMER_PROFILES = "export.customer.profiles";
    public static final String PROP_EXPORT_CATEGORY_PROFILES = "export.categories.profiles";

    public static final String PROP_RMA_STATUS = "rma.status";

    public static final String PROP_CATEGORY_PAGE_ITEMS_OPTIONS = "page.items.options";
    public static final String PROP_DEFAULT_CATEGORY_PAGE_ITEMS_OPTIONS = "10,20,40,80";
    public static final String PROP_CATEGORY_PAGE_ITEMS = "default.page.items";
    public static final String PROP_DEFAULT_CATEGORY_PAGE_ITEMS = "10";

    public static final String PROP_ENCRYPTION_KEY = "encryption.key";

    public static final String PROP_FORGET_ACTION_RESET = "reset";
    public static final String PROP_FORGET_ACTION_SEND = "send";
    public static final String PROP_FORGET_PASSWORD_ACTION = "forget.password.action";
    public static final String PROP_DEFAULT_FORGET_PASSWORD_ACTION = PROP_FORGET_ACTION_RESET; // opciones: send, reset

    public static final String PROP_GOOGLE_ANALYTICS_CODE = "google.analytics.code";
    public static final String PROP_GOOGLE_ECOMMERCE_TRACKING = "google.ecommerce.tracking";
    public static final String PROP_GOOGLE_ECOMMERCE_TRACKING_STATUS = "google.ecommerce.tracking.status";

    public static final String PROP_SHOPCART_AUTO_SAVE = "shopcart.auto.save";
    public static final String PROP_DEFAULT_SHOPCART_AUTO_SAVE = "N";
    public static final String PROP_SHOPCART_CAN_SAVE = "shopcart.can.save";
    public static final String PROP_DEFAULT_SHOPCART_CAN_SAVE = "N";
    public static final String PROP_SHOPCART_CAN_QUOTE = "shopcart.can.quote";
    public static final String PROP_DEFAULT_SHOPCART_CAN_QUOTE = "N";

    public static final String PROP_STATISTICS_EXCLUDED_IPS = "statistics.excluded.ips";

    public static final String PROP_INVOICE_NUMBER_AUTOMATIC = "invoice.number.automatic";
    public static final String PROP_INVOICE_NUMBER_PREFIX = "invoice.number.prefix";
    public static final String PROP_INVOICE_START_FROM = "invoice.start.from";

    public static final String PROP_USE_EMAIL_AS_LOGIN = "use.email.as.login";
    public static final String PROP_DEFAULT_USE_EMAIL_AS_LOGIN = "Y";

    public static final String PROP_USE_FRIENDLY_URLS = "use.friendly.urls";
    public static final String PROP_DEFAULT_USE_FRIENDLY_URLS = "N";

    public static final String PROP_USE_TAX_PER_PRODUCT = "use.tax.per.product";
    public static final String PROP_DEFAULT_USE_TAX_PER_PRODUCT = "N";

    public static final String PROP_ALLOW_ORDER_CUSTOM_REFERENCE = "allow.order.custom.reference";
    public static final String PROP_DEFAULT_ALLOW_ORDER_CUSTOM_REFERENCE = "N";

    public static final String PROP_ALLOW_ORDER_CUSTOM_MESSAGE = "allow.order.custom.message";
    public static final String PROP_DEFAULT_ALLOW_ORDER_CUSTOM_MESSAGE = "N";

    public static final String PROP_SHOW_BACKLINKS = "show.product.backlinks";
    public static final String PROP_DEFAULT_SHOW_BACKLINKS = "N";

    public static final String PROP_LISTING_STYLE = "listing.default.style";
    public static final String PROP_DEFAULT_LISTING_STYLE = "grid";

    public static final String PROP_HOME_HOT_ITEMS = "home.hot.items";
    public static final String PROP_PRODUCT_RELATED_ITEMS = "product.related.items";

    public static final String PROP_PRODUCT_SORT_OPTIONS = "product.sort.options";
    public static final String PROP_DEFAULT_PRODUCT_SORT_OPTIONS = "hits:desc,ratingBy:desc,productName:asc,productName:desc,costPrice:asc,costPrice:desc";

    public static final String PROP_PRODUCT_SORT_DEFAULT_OPTION = "product.sort.default.option";
    public static final String PROP_DEFAULT_PRODUCT_SORT_DEFAULT_OPTION = "hits:desc";

    public static final String PROP_PRODUCT_SHOW_STOCK_INFORMATION = "product.show.stock.info";
    public static final String PROP_DEFAULT_PRODUCT_SHOW_STOCK_INFORMATION = "Y";

    public static final String PROP_PRODUCT_ONEPAGE_CHECKOUT = "onepage.checkout";
    public static final String PROP_DEFAULT_PRODUCT_ONEPAGE_CHECKOUT = "Y";

    public static final String PROP_EMAIL_PURCHASE_FROM = "email.purchase.from";

    public static final String PROP_MAX_MANUFACTURER_LIST = "max.manufacturer.list";
    public static final String PROP_PRODUCT_REVIEW_MAILTO = "product.review.mailto";

    public static final String PROP_ENABLE_PRODUCT_REVIEWS = "enable.product.reviews";
    public static final String PROP_DEFAULT_ENABLE_PRODUCT_REVIEWS = "Y";
    public static final String PROP_REVIEW_MUST_REGISTERED = "must.registered.to.review";
    public static final String PROP_DEFAULT_REVIEW_MUST_REGISTERED = "Y";

    public static final String PROP_USE_LUCENE_INDEXER = "use.lucene.indexer";
    public static final String PROP_DEFAULT_USE_LUCENE_INDEXER = "Y";

    public static final String PROP_SEARCH_MAX_RESULTS = "search.max.results";
    public static final String PROP_DEFAULT_SEARCH_MAX_RESULTS = "200";

    public static final String PROP_CLOSED_STORE = "closed.store";

    public static final String PROP_PRODUCT_NEW_LABEL_AUTO = "product.new.label.auto";
    public static final String PROP_DEFAULT_PRODUCT_NEW_LABEL_AUTO = "Y";


    // Codigo q identifica el texto
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 255)
    private String code;

    // Tipo de texto estatico
    @Column(length = 50)
    private String type;

    // Codigo de la tienda a la que pertenece la categoria
    @Column(length = 10)
    private String inventaryCode;


    // Valores del texto
    @Lob
    private String value;
    @Transient
    private Map<String, String> values;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getInventaryCode() {
        return inventaryCode;
    }

    public void setInventaryCode(String inventaryCode) {
        this.inventaryCode = inventaryCode;
    }

    public String getValue(String lang) {
        String res = null;
        if (values == null || values.size() < 1) deserialize();
        if (values != null && values.containsKey(lang)) res = values.get(lang);
        return res;
    }

    public void addValue(String lang, String value) {
        if (values == null) values = new HashMap<String, String>();
        values.put(lang, value);
        serialize();
    }

    public void serialize() {
        try {
            if (values != null && values.size() > 0) value = JSONUtil.serialize(values);
            else value = null;
        } catch (JSONException e) {
            log.error(e.getMessage(), e);
        }
    }

    public void deserialize() {
        try {
            if (!isEmpty(value)) values = (HashMap) JSONUtil.deserialize(value);
            else values = null;
        } catch (JSONException e) {
            log.error(e.getMessage(), e);
        }
    }

    public String toString() {
        return new ToStringBuilder(this)
                .append("id", getId())
                .toString();
    }



}