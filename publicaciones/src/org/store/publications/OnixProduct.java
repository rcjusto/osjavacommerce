package org.store.publications;

import org.store.core.beans.Codes;
import org.store.core.dao.HibernateDAO;
import org.store.core.globals.SomeUtils;
import org.store.publications.onix.Contributor;
import org.store.publications.onix.ContributorRole;
import org.store.publications.onix.List17;
import org.store.publications.onix.Price;
import org.store.publications.onix.Product;
import org.store.publications.onix.ProductIdentifier;
import org.store.publications.onix.ProductSupply;
import org.store.publications.onix.ResourceLink;
import org.store.publications.onix.ResourceVersion;
import org.store.publications.onix.ResourceVersionFeature;
import org.store.publications.onix.Subject;
import org.store.publications.onix.SupplyDetail;
import org.store.publications.onix.SupportingResource;
import org.store.publications.onix.TextContent;
import org.store.publications.onix.TitleDetail;
import org.store.publications.onix.TitleElement;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Rogelio Caballero
 * 22/10/11 2:42
 */
public class OnixProduct {
    public static Logger log = Logger.getLogger(OnixProduct.class);
    private org.store.publications.onix.Product onixP;
    private static final String ID_TYPE_ISBN_13 = "15";
    private static final String TEXT_TYPE_DESCRIPTION = "03";
    private static final String TITLE_TYPE_CODE_TITLE = "01";
    private static final String TITLE_ELEMENT_LEVEL_PRODUCT = "01";
    public static final String TYPE_DIGITAL = "digital";
    public static final String TYPE_STANDARD = "standard";
    private static final String BIC_SUBJECT_CATEGORY = "12";
    public static final String BIC_GEOGRAPHICAL_QUALIFIER = "13";
    public static final String BIC_LANGUAGE_QUALIFIER = "14";
    public static final String BIC_TIME_PERIOD_QUALIFIER = "15";
    public static final String BIC_EDUCATIONAL_PURPOSE_QUALIFIER = "16";
    public static final String BIC_READING_LEVEL = "17";
    private static final String CONTENT_TYPE_FRONT_COVER = "01";
    private static final String CONTENT_TYPE_BACK_COVER = "02";
    private static final String CONTENT_TYPE_SAMPLE_CONTENT = "15";
    private static final String RESOURCE_FORM_EMBEDABLE = "03";
    private static final String FILE_FORMAT = "01";
    public static final String BIC_CODE = "bic";

    public OnixProduct(Product onixP) {
        this.onixP = onixP;
    }

    public String getId() {
        if (onixP != null && onixP.getRecordReference() != null) {
            return onixP.getRecordReference().getValue();
        }
        return null;
    }

    public String getCode() {
        if (onixP != null && onixP.getProductIdentifier() != null && !onixP.getProductIdentifier().isEmpty()) {
            for (ProductIdentifier pi : onixP.getProductIdentifier()) {
                if (pi != null && pi.getProductIDType() != null && ID_TYPE_ISBN_13.equalsIgnoreCase(pi.getProductIDType().getValue()) && pi.getIDValue() != null && StringUtils.isNotEmpty(pi.getIDValue().getValue())) {
                    return pi.getIDValue().getValue();
                }
            }
        }
        return null;
    }

    public String getNotificationType() {
        if (onixP != null && onixP.getNotificationType() != null) {
            return onixP.getNotificationType().getValue();
        }
        return null;
    }

    public String getName() {
        if (onixP != null && onixP.getDescriptiveDetail() != null && onixP.getDescriptiveDetail().getTitleDetail() != null && !onixP.getDescriptiveDetail().getTitleDetail().isEmpty()) {
            for (TitleDetail titleDetail : onixP.getDescriptiveDetail().getTitleDetail()) {
                if (TITLE_TYPE_CODE_TITLE.equalsIgnoreCase(titleDetail.getTitleType().getValue()) && titleDetail.getTitleElement() != null) {
                    for (TitleElement titleElement : titleDetail.getTitleElement()) {
                        if (TITLE_ELEMENT_LEVEL_PRODUCT.equalsIgnoreCase(titleElement.getTitleElementLevel().getValue()))
                            return titleElement.getTitleText().getValue();
                    }
                }
            }
        }
        return null;
    }

    public String getDescription() {
        if (onixP != null && onixP.getCollateralDetail() != null && onixP.getCollateralDetail().getTextContent() != null && !onixP.getCollateralDetail().getTextContent().isEmpty()) {
            for (TextContent textContent : onixP.getCollateralDetail().getTextContent()) {
                if (TEXT_TYPE_DESCRIPTION.equalsIgnoreCase(textContent.getTextType().getValue()) && textContent.getText().getContent() != null) {
                    StringBuilder str = new StringBuilder();
                    for (Serializable s : textContent.getText().getContent()) {
                        str.append(s.toString());
                    }
                    return str.toString();
                }
            }
        }
        return null;
    }

    public String getInformation() {
        return null;
    }

    public String getAuthor() {
        if (onixP != null && onixP.getDescriptiveDetail() != null && onixP.getDescriptiveDetail().getContributor() != null) {
            for (Contributor contributor : onixP.getDescriptiveDetail().getContributor()) {
                if (contributor.getContributorRole() != null && !contributor.getContributorRole().isEmpty()) {
                    for (ContributorRole role : contributor.getContributorRole()) {
                        if (List17.A_01.equals(role.getValue())) {
                            if (contributor.getPersonName() != null && StringUtils.isNotEmpty(contributor.getPersonName().getValue())) {
                                return contributor.getPersonName().getValue();
                            } else if (contributor.getCorporateName() != null && StringUtils.isNotEmpty(contributor.getCorporateName().getValue())) {
                                return contributor.getCorporateName().getValue();
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    public String getType() {
        if (onixP != null && onixP.getDescriptiveDetail() != null && onixP.getDescriptiveDetail().getProductForm() != null && onixP.getDescriptiveDetail().getProductForm().getValue() != null) {
            return (onixP.getDescriptiveDetail().getProductForm().getValue().startsWith("E")) ? TYPE_DIGITAL : TYPE_STANDARD;
        }
        return null;
    }

    public Long getStock() {
        if (onixP != null && onixP.getProductSupply() != null && !onixP.getProductSupply().isEmpty()) {
            for (ProductSupply productSupply : onixP.getProductSupply()) {
                if (productSupply.getSupplyDetail() != null)
                    for (SupplyDetail supplyDetail : productSupply.getSupplyDetail()) {
                        if (supplyDetail.getProductAvailability() != null && StringUtils.isNotEmpty(supplyDetail.getProductAvailability().getValue())) {
                            return SomeUtils.strToLong(supplyDetail.getProductAvailability().getValue());
                        }
                    }
            }
        }
        return null;
    }

    public java.util.Map<String, Double> getPrice() {
        java.util.Map<String, Double> map = new HashMap<String, Double>();
        if (onixP != null && onixP.getProductSupply() != null && !onixP.getProductSupply().isEmpty()) {
            for (ProductSupply productSupply : onixP.getProductSupply()) {
                if (productSupply.getSupplyDetail() != null)
                    for (SupplyDetail supplyDetail : productSupply.getSupplyDetail()) {
                        if (supplyDetail.getPrice() != null) {
                            for (Price price : supplyDetail.getPrice()) {
                                Double value = (price.getPriceAmount() != null && StringUtils.isNotEmpty(price.getPriceAmount().getValue())) ? SomeUtils.strToDouble(price.getPriceAmount().getValue()) : 0d;
                                if (value != null && price.getCurrencyCode() != null && price.getCurrencyCode().getValue() != null && StringUtils.isNotEmpty(price.getCurrencyCode().getValue().value()))
                                    map.put(price.getCurrencyCode().getValue().value().toUpperCase(), value);
                            }
                        }
                    }
            }
        }
        return (!map.isEmpty()) ? map : null;
    }

    public List<String> getCategories() {
        List<String> result = new ArrayList<String>();
        if (onixP != null && onixP.getDescriptiveDetail() != null && onixP.getDescriptiveDetail().getSubject() != null) {
            for (Subject subject : onixP.getDescriptiveDetail().getSubject()) {
                if (subject.getSubjectSchemeIdentifier() != null && BIC_SUBJECT_CATEGORY.equalsIgnoreCase(subject.getSubjectSchemeIdentifier().getValue())) {
                    String catName = null;
                    if (subject.getSubjectCode() != null && StringUtils.isNotEmpty(subject.getSubjectCode().getValue()))
                        catName = subject.getSubjectCode().getValue();
                    if (StringUtils.isEmpty(catName) && subject.getSubjectHeadingText() != null && StringUtils.isNotEmpty(subject.getSubjectHeadingText().getValue()))
                        catName = subject.getSubjectHeadingText().getValue();
                    if (StringUtils.isNotEmpty(catName)) result.add(catName);
                }
            }
        }
        return (!result.isEmpty()) ? result : null;
    }

    public List<java.util.Map<String, String>> getImages() {
        List<java.util.Map<String, String>> result = new ArrayList<Map<String, String>>();
        if (onixP != null && onixP.getCollateralDetail() != null && onixP.getCollateralDetail().getSupportingResource() != null && !onixP.getCollateralDetail().getSupportingResource().isEmpty()) {
            for (SupportingResource supportingResource : onixP.getCollateralDetail().getSupportingResource()) {
                if (supportingResource.getResourceContentType() != null && supportingResource.getResourceVersion() != null && (CONTENT_TYPE_FRONT_COVER.equalsIgnoreCase(supportingResource.getResourceContentType().getValue()) || CONTENT_TYPE_BACK_COVER.equalsIgnoreCase(supportingResource.getResourceContentType().getValue()))) {
                    for (ResourceVersion resourceVersion : supportingResource.getResourceVersion()) {
                        String mode = (resourceVersion.getResourceForm() != null && RESOURCE_FORM_EMBEDABLE.equalsIgnoreCase(resourceVersion.getResourceForm().getValue())) ? "included" : "download";
                        String fileType = null;
                        if (resourceVersion.getResourceVersionFeature() != null) {
                            for (ResourceVersionFeature rvf : resourceVersion.getResourceVersionFeature()) {
                                if (rvf.getResourceVersionFeatureType() != null && FILE_FORMAT.equalsIgnoreCase(rvf.getResourceVersionFeatureType().getValue()) && rvf.getFeatureValue() != null && StringUtils.isNotEmpty(rvf.getFeatureValue().getValue())) {
                                    if ("D501".equalsIgnoreCase(rvf.getFeatureValue().getValue())) fileType = "gif";
                                    if ("D502".equalsIgnoreCase(rvf.getFeatureValue().getValue())) fileType = "jpg";
                                    if ("D503".equalsIgnoreCase(rvf.getFeatureValue().getValue())) fileType = "png";
                                    if ("D504".equalsIgnoreCase(rvf.getFeatureValue().getValue())) fileType = "tiff";
                                }
                            }
                        }
                        if (resourceVersion.getResourceLink() != null && !resourceVersion.getResourceLink().isEmpty()) {
                            for (ResourceLink resourceLink : resourceVersion.getResourceLink())
                                if (StringUtils.isNotEmpty(resourceLink.getValue())) {
                                    if (StringUtils.isEmpty(fileType)) fileType = FilenameUtils.getExtension(resourceLink.getValue());
                                    Map<String, String> map = new HashMap<String, String>();
                                    map.put("mode", mode);
                                    map.put("type", fileType);
                                    map.put("file", resourceLink.getValue());
                                    result.add(map);
                                }
                        }
                    }
                }
            }
        }
        return (!result.isEmpty()) ? result : null;
    }

    public List<java.util.Map<String, String>> getPreview() {
        List<java.util.Map<String, String>> result = new ArrayList<Map<String, String>>();
        if (onixP != null && onixP.getCollateralDetail() != null && onixP.getCollateralDetail().getSupportingResource() != null && !onixP.getCollateralDetail().getSupportingResource().isEmpty()) {
            for (SupportingResource supportingResource : onixP.getCollateralDetail().getSupportingResource()) {
                if (supportingResource.getResourceContentType() != null && supportingResource.getResourceVersion() != null && (CONTENT_TYPE_SAMPLE_CONTENT.equalsIgnoreCase(supportingResource.getResourceContentType().getValue()))) {
                    for (ResourceVersion resourceVersion : supportingResource.getResourceVersion()) {
                        String mode = (resourceVersion.getResourceForm() != null && RESOURCE_FORM_EMBEDABLE.equalsIgnoreCase(resourceVersion.getResourceForm().getValue())) ? "included" : "download";
                        String fileType = null;
                        if (resourceVersion.getResourceVersionFeature() != null) {
                            for (ResourceVersionFeature rvf : resourceVersion.getResourceVersionFeature()) {
                                if (rvf.getResourceVersionFeatureType() != null && FILE_FORMAT.equalsIgnoreCase(rvf.getResourceVersionFeatureType().getValue()) && rvf.getFeatureValue() != null && StringUtils.isNotEmpty(rvf.getFeatureValue().getValue())) {
                                    if ("E101".equalsIgnoreCase(rvf.getFeatureValue().getValue())) fileType = "epub";
                                    else if ("application/epub+zip".equalsIgnoreCase(rvf.getFeatureValue().getValue())) fileType = "epub";
                                    else if ("D401".equalsIgnoreCase(rvf.getFeatureValue().getValue())) fileType = "pdf";
                                    else if ("D501".equalsIgnoreCase(rvf.getFeatureValue().getValue())) fileType = "gif";
                                    else if ("D502".equalsIgnoreCase(rvf.getFeatureValue().getValue())) fileType = "jpg";
                                    else if ("D503".equalsIgnoreCase(rvf.getFeatureValue().getValue())) fileType = "png";
                                    else if ("D504".equalsIgnoreCase(rvf.getFeatureValue().getValue())) fileType = "tiff";
                                }
                            }
                        }
                        if (resourceVersion.getResourceLink() != null && !resourceVersion.getResourceLink().isEmpty()) {
                            for (ResourceLink resourceLink : resourceVersion.getResourceLink()) if (StringUtils.isNotEmpty(resourceLink.getValue())) {
                                if (StringUtils.isEmpty(fileType)) fileType = FilenameUtils.getExtension(resourceLink.getValue());
                                Map<String, String> map = new HashMap<String, String>();
                                map.put("mode", mode);
                                map.put("type", fileType);
                                map.put("file", resourceLink.getValue());
                                result.add(map);
                            }
                        }
                    }
                }
            }
        }
        return (!result.isEmpty()) ? result : null;
    }

    public java.util.Map<String, String> getAttributes() {
        java.util.Map<String, String> result = new HashMap<String, String>();
        if (onixP != null && onixP.getDescriptiveDetail() != null && onixP.getDescriptiveDetail().getSubject() != null) {
            for (Subject subject : onixP.getDescriptiveDetail().getSubject()) {
                if (subject.getSubjectSchemeIdentifier() != null && BIC_EDUCATIONAL_PURPOSE_QUALIFIER.equalsIgnoreCase(subject.getSubjectSchemeIdentifier().getValue())) {
                    result.put(BIC_EDUCATIONAL_PURPOSE_QUALIFIER, subject.getSubjectCode().getValue());
                } else if (subject.getSubjectSchemeIdentifier() != null && BIC_GEOGRAPHICAL_QUALIFIER.equalsIgnoreCase(subject.getSubjectSchemeIdentifier().getValue())) {
                    result.put(BIC_GEOGRAPHICAL_QUALIFIER, subject.getSubjectCode().getValue());
                } else if (subject.getSubjectSchemeIdentifier() != null && BIC_LANGUAGE_QUALIFIER.equalsIgnoreCase(subject.getSubjectSchemeIdentifier().getValue())) {
                    result.put(BIC_LANGUAGE_QUALIFIER, subject.getSubjectCode().getValue());
                } else if (subject.getSubjectSchemeIdentifier() != null && BIC_READING_LEVEL.equalsIgnoreCase(subject.getSubjectSchemeIdentifier().getValue())) {
                    result.put(BIC_READING_LEVEL, subject.getSubjectCode().getValue());
                } else if (subject.getSubjectSchemeIdentifier() != null && BIC_TIME_PERIOD_QUALIFIER.equalsIgnoreCase(subject.getSubjectSchemeIdentifier().getValue())) {
                    result.put(BIC_TIME_PERIOD_QUALIFIER, subject.getSubjectCode().getValue());
                }
            }
        }
        return (!result.isEmpty()) ? result : null;
    }

    // BIC HELPERS

    public static List<Codes> getBICCategoryName(String catCode, HibernateDAO dao) {
        List<Codes> categories = new ArrayList<Codes>();
        while (catCode.length() > 0) {
            Codes codes = dao.getCode(catCode, BIC_CODE);
            if (codes != null && codes.getActive()) categories.add(codes);
            catCode = catCode.substring(0, catCode.length() - 1);
        }
        Collections.reverse(categories);
        return categories;
    }

    public static void readBICCategories(HibernateDAO dao, File file) {
        try {
            Properties prop = new Properties();
            prop.load(new FileInputStream(file));
            for (String key : prop.stringPropertyNames()) {
                Codes code = dao.getCode(key, BIC_CODE);
                if (code == null) {
                    code = new Codes();
                    code.setCode(key);
                    code.setType(BIC_CODE);
                    for (String lang : dao.getLanguages())
                        code.setText(lang, prop.getProperty(key));
                    dao.save(code);
                }
            }

        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }

}
