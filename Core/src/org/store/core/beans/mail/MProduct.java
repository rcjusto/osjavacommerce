package org.store.core.beans.mail;

import org.apache.commons.lang.StringUtils;
import org.store.core.beans.*;
import org.store.core.beans.utils.DataNavigator;
import org.store.core.beans.utils.ProdStaticText;
import org.store.core.globals.BaseAction;
import org.store.core.globals.SomeUtils;

import java.text.DecimalFormat;
import java.util.*;

/**
 * Rogelio Caballero
 * 20/07/12 14:47
 */
public class MProduct {

    private BaseAction action;
    private Product product;
    private ProductLang productLang;
    private Map<String, Object> priceMap;

    public MProduct(Product product, BaseAction action) {
        this.product = product;
        this.action = action;
        this.productLang = product.getLanguage(action.getDefaultLanguage());
        this.priceMap = product.getPriceMap(action.getFrontUserLevel(), 1);
    }

    public Product getData() {
        return product;
    }

    public String getPartNumber() {
        return product.getPartNumber();
    }

    public String getName() {
        return (productLang != null) ? productLang.getProductName() : "";
    }

    public String getDescription() {
        return (productLang != null) ? productLang.getDescription() : "";
    }

    public boolean hasInformation() {
        return StringUtils.isNotEmpty(getInformation());
    }

    public String getInformation() {
        return (productLang != null) ? productLang.getInformation() : "";
    }

    public boolean hasFeatures() {
        return StringUtils.isNotEmpty(getFeatures());
    }

    public String getFeatures() {
        return (productLang != null) ? productLang.getFeatures() : "";
    }

    public String getUrl() {
        return action.urlProduct(product, true);
    }

    public String getImage() {
        StringBuilder b = new StringBuilder();
        String img = action.getImageResolver().getImageForProduct(product, "");
        if (StringUtils.isEmpty(img)) img = "not-available.gif";
        b.append(action.getRequest().getRequestURL().toString().toLowerCase().startsWith("https") ? "https://" : "http://")
                .append(action.getRequest().getHeader("Host"))
                .append(action.storeFile("images/products/" + img));
        return b.toString();
    }

    public String getImageSmall() {
        StringBuilder b = new StringBuilder();
        String img = action.getImageResolver().getImageForProduct(product, "list/");
        if (StringUtils.isEmpty(img)) img = "not-available.gif";
        b.append(action.getRequest().getRequestURL().toString().toLowerCase().startsWith("https") ? "https://" : "http://")
                .append(action.getRequest().getHeader("Host"))
                .append(action.storeFile("images/products/list/" + img));
        return b.toString();
    }

    public String getStock() {
        if (action.getCanShowStock()) {
            if (product.getStock() > 0) return action.getText("on.stock");
            else if (StringUtils.isNotEmpty(product.getEta())) return action.getText("backorder.until") + " " + product.getEta();
            else return action.getText("out.of.stock");
        }
        return "";
    }

    public String getPrice() {
        return action.formatActualCurrency((Number) priceMap.get("FINAL_PRICE"));
    }

    public String getFinalPrice() {
        StringBuilder b = new StringBuilder();
        if (priceMap.containsKey("OFFER")) {
            ProductOffer offer = (ProductOffer) priceMap.get("OFFER");
            b.append("<p>")
                    .append(action.formatActualCurrency((Number) priceMap.get("FINAL_OFFER_PRICE")))
                    .append(" ").append(action.getText("product.offer.until")).append(" ")
                    .append(SomeUtils.formatDate(offer.getDateTo(), action.getLocale().getLanguage()))
                    .append("</p>");
            b.append("<p>")
                    .append(action.getText("product.our.price")).append(" ")
                    .append(action.formatActualCurrency((Number) priceMap.get("FINAL_PRICE")))
                    .append("</p>");
            if (priceMap.containsKey("SAVE_FROM_LIST")) {
                b.append("<p>").append(action.getText("product.list.price")).append(" ").append(action.formatActualCurrency(product.getListPrice())).append("</p>");
                b.append("<p>").append(action.getText("product.save.instantly")).append(" ").append(action.formatActualCurrency((Number) priceMap.get("SAVE_FROM_LIST"))).append("</p>");
            } else {
                b.append("<p>").append(action.getText("product.offer.save")).append(" ").append(action.formatActualCurrency((Number) priceMap.get("OFFER_DISCOUNT"))).append("</p>");
            }
        } else {
            b.append("<p>").append(action.formatActualCurrency((Number) priceMap.get("FINAL_PRICE"))).append("</p>");
            if (priceMap.containsKey("SAVE_FROM_LIST")) {
                b.append("<p>").append(action.getText("product.list.price")).append(" ").append(action.formatActualCurrency(product.getListPrice())).append("</p>");
                b.append("<p>").append(action.getText("product.save.instantly")).append(" ").append(action.formatActualCurrency((Number) priceMap.get("SAVE_FROM_LIST"))).append("</p>");
            }
        }
        return b.toString();
    }

    public String getVolumePrices() {
        StringBuilder b = new StringBuilder();
        if (priceMap.containsKey("VOLUME_LIST") && priceMap.get("VOLUME_LIST") != null && priceMap.get("VOLUME_LIST") instanceof Collection && !((Collection) priceMap.get("VOLUME_LIST")).isEmpty()) {
            Collection c = (Collection) priceMap.get("VOLUME_LIST");
            b.append("<ul>");
            for (Object o : c) {
                if (o instanceof Map) {
                    Map map = (Map) o;
                    b.append("<li>")
                            .append(action.getText("product.volume.buy")).append(" ")
                            .append(map.get("volume"))
                            .append(" ").append(action.getText("product.volume.at")).append(" ")
                            .append(action.formatActualCurrency((Number) map.get("unitPrice")))
                            .append(" ").append(action.getText("product.volume.eachone"))
                            .append("</li>");
                }
            }
            b.append("</ul>");
        }
        return b.toString();
    }

    public String getLabels() {
        List<String> labels = new ArrayList<String>();
        if (product.getLabels() != null && !product.getLabels().isEmpty()) {
            for (ProductLabel pl : product.getLabels()) {
                String labContent = pl.getContentDetail(action.getLocale().getLanguage());
                if (StringUtils.isNotBlank(labContent)) labels.add(labContent);
            }
        }

        StringBuilder b = new StringBuilder();
        if (!labels.isEmpty()) {
            b.append("<ul>");
            for (String s : labels) b.append("<li>").append(s).append("</li>");
            b.append("</ul>");
        }
        return b.toString();
    }

    public String getRebate() {
        StringBuilder b = new StringBuilder();
        if (product.getRebate() != null) {
            b.append("<p>").append(product.getRebate().getResourceName(action.getLocale().getLanguage())).append("</p>");
            if (product.getRebate().getResourceValue() != null && product.getRebate().getResourceValue() > 0) {
                b.append("<p>").append(action.formatActualCurrency(product.getRebate().getResourceValue()));
                if (product.getRebate().getResourceDate()!=null) b.append(" ").append(action.getText("valid.until")).append(" ").append(SomeUtils.formatDate(product.getRebate().getResourceDate(), action.getLocale().getLanguage()));
                b.append("</p>");
            }
            b.append("<p>").append(product.getRebate().getResourceDescription(action.getLocale().getLanguage())).append("</p>");
        }
        return b.toString();
    }

    public String getStaticTexts() {
        StringBuilder b = new StringBuilder();
        Map<String, List<ProdStaticText>> map = getStaticTextsMap();
        if (map.containsKey("tabs") && !map.get("tabs").isEmpty()) {
            String language = action.getLocale().getLanguage();
            for(ProdStaticText st : map.get("tabs")) {
                b.append("<h2>").append(st.getStaticText().getTitle(language)).append("</h2>");
                b.append("<div>").append(st.getStaticText().getContentValue(language)).append("</div>");
            }
        }
        return b.toString();
    }

    private Map<String, List<ProdStaticText>> getStaticTextsMap() {
        Map<String, List<ProdStaticText>> mapStaticText = new HashMap<String, List<ProdStaticText>>();
        List<ProductStaticText> lpst = action.getDao().getProductStaticText(product);
        if (lpst != null) {
            for (ProductStaticText pst : lpst) {
                String place = (StringUtils.isNotEmpty(pst.getContentPlace())) ? pst.getContentPlace() : "[no-place]";
                List<ProdStaticText> l = null;
                if (mapStaticText.containsKey(place)) l = mapStaticText.get(place);
                if (l == null) {
                    l = new ArrayList<ProdStaticText>();
                    mapStaticText.put(place, l);
                }
                l.add(pst);
            }
        }
        if (product.getCategory() != null) {
            List<CategoryStaticText> lcst = action.getDao().getParentCategoryStaticTexts(product.getCategory());
            if (lpst != null) {
                for (CategoryStaticText cst : lcst) {
                    String place = (StringUtils.isNotEmpty(cst.getContentPlace())) ? cst.getContentPlace() : "[no-place]";
                    List<ProdStaticText> l = null;
                    if (mapStaticText.containsKey(place)) l = mapStaticText.get(place);
                    if (l == null) {
                        l = new ArrayList<ProdStaticText>();
                        mapStaticText.put(place, l);
                    }
                    l.add(cst);
                }
            }
        }
        for (List<ProdStaticText> l : mapStaticText.values()) {
            Collections.sort(l, new Comparator<ProdStaticText>() {
                public int compare(ProdStaticText o1, ProdStaticText o2) {
                    return (o1.getContentOrder() != null) ? o1.getContentOrder().compareTo(o2.getContentOrder()) : -1;
                }
            });
        }
        return mapStaticText;
    }

    public String getReviews() {
        DataNavigator reviews = new DataNavigator(action.getRequest(), "reviews");
        reviews.setPageRows(5);
        List<ProductReview> list = action.getDao().getReviewsForProduct(reviews, product);
        StringBuilder b = new StringBuilder();
        if (!list.isEmpty()) {
            DecimalFormat df = new DecimalFormat("0.#");
            b.append("<h2>").append(action.getText("reviews")).append("</h2>");
            b.append("<p>").append(action.getText("product.customer.rating")).append(" ").append(df.format(product.getRatingBy())).append("</p>");
            for(ProductReview r : list) {
                b.append("<table width=100% border=0 cellpadding=0 cellspacing=0 class=reviews>");
                b.append("<tr>").append("<td>").append(r.getTitle()).append("</td>");
                b.append("<td style=\"text-align:right\">").append(action.getText("product.customer.rating")).append(" ").append(df.format(r.getAverageScore())).append("</td>");
                b.append("</tr>").append("<tr>").append("<td colspan=\"2\">").append(r.getOpinion()).append("</td>");
                b.append("</tr>").append("</table>");
            }
        }
        return b.toString();
    }

}
