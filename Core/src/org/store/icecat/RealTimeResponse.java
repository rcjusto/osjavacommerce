package org.store.icecat;

import org.apache.commons.digester.Digester;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Rogelio Caballero
 * 24/07/11 3:11
 */
public class RealTimeResponse {

    public static Logger log = Logger.getLogger(RealTimeResponse.class);
    private String icecatId;
    private String name;
    private String shortDesc;
    private String longDesc;

    private String highPictureUrl;
    private String lowPictureUrl;
    private String thumbnailUrl;

    private String categoryName;

    private String manualPDFURL;
    private String otherPDFURL;
    private String url;

    private String warrantyInfo;

    private List<Feature> features = new ArrayList<Feature>();

    private List<String> gallery = new ArrayList<String>();

    private String shortSummaryDescription;
    private String LongSummaryDescription;

    private String manufacturer;


    public RealTimeResponse(String xml) {
        Digester digester = new Digester();
        digester.push(this);

        digester.addCallMethod("ICECAT-interface/Product", "addProductData", 15);
        digester.addCallParam("ICECAT-interface/Product", 0, "Prod_id");
        digester.addCallParam("ICECAT-interface/Product", 1, "Title");
        digester.addCallParam("ICECAT-interface/Product", 2, "HighPic");
        digester.addCallParam("ICECAT-interface/Product", 3, "LowPic");
        digester.addCallParam("ICECAT-interface/Product", 4, "ThumbPic");
        digester.addCallParam("ICECAT-interface/Product/Category/Name", 5, "Value");
        digester.addCallParam("ICECAT-interface/Product/ProductDescription", 6, "LongDesc");
        digester.addCallParam("ICECAT-interface/Product/ProductDescription", 7, "ShortDesc");
        digester.addCallParam("ICECAT-interface/Product/ProductDescription", 8, "ManualPDFURL");
        digester.addCallParam("ICECAT-interface/Product/ProductDescription", 9, "PDFURL");
        digester.addCallParam("ICECAT-interface/Product/ProductDescription", 10, "URL");
        digester.addCallParam("ICECAT-interface/Product/ProductDescription", 11, "WarrantyInfo");
        digester.addCallParam("ICECAT-interface/Product/SummaryDescription/ShortSummaryDescription", 12);
        digester.addCallParam("ICECAT-interface/Product/SummaryDescription/LongSummaryDescription", 13 );
        digester.addCallParam("ICECAT-interface/Product/Supplier", 14, "Name");

        digester.addCallMethod("ICECAT-interface/Product/ProductFeature", "addFeature", 2);
        digester.addCallParam("ICECAT-interface/Product/ProductFeature", 0, "Presentation_Value");
        digester.addCallParam("ICECAT-interface/Product/ProductFeature/Feature/Name", 1, "Value");

        digester.addCallMethod("ICECAT-interface/Product/ProductGallery/ProductPicture", "addGallery", 1);
        digester.addCallParam("ICECAT-interface/Product/ProductGallery/ProductPicture", 0, "Pic");

        try {
            digester.parse(new StringReader(xml));
        } catch (IOException e) {
            log.error(e.getMessage(), e); 
        } catch (SAXException e) {
            log.error(e.getMessage(), e); 
        }

    }

    public void addProductData(String p0,String p1,String p2,String p3,String p4,String p5,String p6,String p7,String p8,String p9,String p10,String p11,String p12,String p13,String p14) {
        setIcecatId(p0);
        setName(p1);
        setHighPictureUrl(p2);
        setLowPictureUrl(p3);
        setThumbnailUrl(p4);
        setCategoryName(p5);
        setLongDesc(p6);
        setShortDesc(p7);
        setManualPDFURL(p8);
        setOtherPDFURL(p9);
        setUrl(p10);
        setWarrantyInfo(p11);
        setShortSummaryDescription(p12);
        setLongSummaryDescription(p13);
        setManufacturer(p14);
    }

    public void addGallery(String p0) {
        if (StringUtils.isNotEmpty(p0)) gallery.add(p0);
    }

    public void addFeature(String p0, String p1) {
        if (StringUtils.isNotEmpty(p1))
            features.add(new Feature(p1,p0));
    }

    public String getIcecatId() {
        return icecatId;
    }

    public void setIcecatId(String icecatId) {
        this.icecatId = icecatId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getShortDesc() {
        return shortDesc;
    }

    public void setShortDesc(String shortDesc) {
        this.shortDesc = shortDesc;
    }

    public String getLongDesc() {
        return longDesc;
    }

    public void setLongDesc(String longDesc) {
        this.longDesc = longDesc;
    }

    public String getHighPictureUrl() {
        return highPictureUrl;
    }

    public void setHighPictureUrl(String highPictureUrl) {
        this.highPictureUrl = highPictureUrl;
    }

    public String getLowPictureUrl() {
        return lowPictureUrl;
    }

    public void setLowPictureUrl(String lowPictureUrl) {
        this.lowPictureUrl = lowPictureUrl;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getManualPDFURL() {
        return manualPDFURL;
    }

    public void setManualPDFURL(String manualPDFURL) {
        this.manualPDFURL = manualPDFURL;
    }

    public String getOtherPDFURL() {
        return otherPDFURL;
    }

    public void setOtherPDFURL(String otherPDFURL) {
        this.otherPDFURL = otherPDFURL;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getWarrantyInfo() {
        return warrantyInfo;
    }

    public void setWarrantyInfo(String warrantyInfo) {
        this.warrantyInfo = warrantyInfo;
    }

    public List<Feature> getFeatures() {
        return features;
    }

    public void setFeatures(List<Feature> features) {
        this.features = features;
    }

    public List<String> getGallery() {
        return gallery;
    }

    public void setGallery(List<String> gallery) {
        this.gallery = gallery;
    }

    public String getShortSummaryDescription() {
        return shortSummaryDescription;
    }

    public void setShortSummaryDescription(String shortSummaryDescription) {
        this.shortSummaryDescription = shortSummaryDescription;
    }

    public String getLongSummaryDescription() {
        return LongSummaryDescription;
    }

    public void setLongSummaryDescription(String longSummaryDescription) {
        LongSummaryDescription = longSummaryDescription;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public class Feature {

        public Feature(String name, String value) {
            this.name = name;
            this.value = value;
        }

        private String name;
        private String value;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }

}
