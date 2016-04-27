package org.store.core.beans.utils;

import org.store.core.beans.Product;
import org.store.core.beans.ProductLang;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.hibernate.search.bridge.FieldBridge;
import org.hibernate.search.bridge.LuceneOptions;

public class ProductLangBridge implements FieldBridge {

    public void set(String name, Object o, Document document, LuceneOptions luceneOptions) {
        if (o instanceof Product) {
            Product p = (Product) o;
            try {
                if (p.getProductLangs() != null) {
                    for (ProductLang l : p.getProductLangs()) {
                        StringBuilder buff = new StringBuilder();
                        if (StringUtils.isNotEmpty(p.getPartNumber())) buff.append(p.getPartNumber()).append(" ");
                        if (StringUtils.isNotEmpty(p.getMfgPartnumber())) buff.append(p.getMfgPartnumber()).append(" ");
                        if (p.getManufacturer()!=null && StringUtils.isNotEmpty(p.getManufacturer().getManufacturerName())) buff.append(p.getManufacturer().getManufacturerName()).append(" ");
                        if (StringUtils.isNotEmpty(p.getSearchKeywords())) buff.append(p.getSearchKeywords()).append(" ");
                        if (StringUtils.isNotEmpty(l.getProductName())) buff.append(l.getProductName()).append(" ");
                        if (StringUtils.isNotEmpty(l.getDescription())) buff.append(l.getDescription()).append(" ");
                        // todo: probar la indexacion de los otros campos
                        if (StringUtils.isNotEmpty(l.getFeatures())) buff.append(l.getFeatures()).append(" ");
                        if (StringUtils.isNotEmpty(l.getInformation())) buff.append(l.getInformation()).append(" ");
                        Field field = new Field(name + "_"+l.getProductLang(), buff.toString(), luceneOptions.getStore(), luceneOptions.getIndex(), luceneOptions.getTermVector());
                        field.setBoost(luceneOptions.getBoost());
                        document.add(field);
                    }
                }

                StringBuilder userLevels = new StringBuilder();
                if (p.getForUsers() != null && !p.getForUsers().isEmpty()) {
                    for(Long lid : p.getForUsers())
                        userLevels.append("l").append(lid).append("l ");
                } else {
                    userLevels.append("public");
                }
                Field fieldL = new Field("forUsers", userLevels.toString(), luceneOptions.getStore(), luceneOptions.getIndex(), luceneOptions.getTermVector());
                document.add(fieldL);

                Field field = new Field("store", p.getInventaryCode(), luceneOptions.getStore(), luceneOptions.getIndex(), luceneOptions.getTermVector());
                document.add(field);
            } catch (Exception ignored) {}

        }

    }
}
