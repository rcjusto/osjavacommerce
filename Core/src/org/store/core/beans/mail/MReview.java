package org.store.core.beans.mail;

import org.store.core.beans.ProductReview;
import org.store.core.globals.BaseAction;
import org.store.core.globals.SomeUtils;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * Rogelio Caballero
 * 20/07/12 19:21
 */
public class MReview {

    private ProductReview review;
    private BaseAction action;

    public MReview(ProductReview review, BaseAction action) {
        this.review = review;
        this.action = action;
    }

    public ProductReview getData() {
        return review;
    }

    public String getTitle() {
        return review.getTitle();
    }

    public String getOpinion() {
        return review.getOpinion();
    }

    public String getScore() {
        if (review.getAverageScore() != null) {
            DecimalFormat df = new DecimalFormat("0.#");
            return df.format(review.getAverageScore());
        }
        return "";
    }

    public String getEmail() {
        return review.getEmail();
    }

    public String getUsername() {
        return review.getUserName();
    }

    public String getCreated() {
        return (review.getCreated()!=null) ? SomeUtils.formatDate(review.getCreated(), action.getDefaultLanguage()) : "";
    }

    public String getAdminUrl() {
        Map<String,String> map = new HashMap<String,String>();
        map.put("idProduct", review.getProduct().getIdProduct().toString());
        map.put("openTab", "6");
        return action.url("productedit","admin", map, true);
    }

}
