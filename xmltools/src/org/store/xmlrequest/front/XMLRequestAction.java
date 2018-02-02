package org.store.xmlrequest.front;

import org.store.core.beans.*;
import org.store.core.front.FrontModuleAction;
import org.store.core.globals.SomeUtils;
import org.store.xmlrequest.BeanRequest;
import org.store.xmlrequest.admin.XMLToolAction;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.json.JSONException;
import org.apache.struts2.json.JSONUtil;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.StringWriter;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Rogelio Caballero
 * 27/04/12 23:13
 */
@Namespace(value="/")
@ParentPackage(value = "store-front")
public class XMLRequestAction extends FrontModuleAction {

    public static final String XML_ACTION = "xmlcatalog";
  public static final String USER_CAN_ACCESS = "can.access.xmltool";
    public static final String USER_LAST_ACCESS = "last.access.xmltool";

    @Action(value = XML_ACTION, results = @Result(type = "stream", params = {"allowCaching","false","contentType","text/html"}))
    public String xmlcatalog() throws Exception {
        String error = null;
        Map config = null;

        StoreProperty bean = getDao().getStoreProperty(XMLToolAction.PROP_XML_REQUEST_PROPERTY, StoreProperty.TYPE_GENERAL);
        if (bean != null && StringUtils.isNotEmpty(bean.getValue())) {
            try {
                Object o = JSONUtil.deserialize(bean.getValue());
                if (o != null && o instanceof Map) config = (Map) o;
            } catch (JSONException ignored) {}
        }

        if (config != null && config.containsKey("enabled") && config.get("enabled") != null && Boolean.TRUE.equals(config.get("enabled"))) {
            // ver si tiene un parametro xml
            if (StringUtils.isEmpty(xml)) {
                // verificar si viene en el body
                try {
                    StringWriter writer = new StringWriter();
                    IOUtils.copy(getRequest().getInputStream(), writer);
                    xml = writer.toString();
                } catch (IOException e) {
                    LOG.error(e.getMessage(), e);
                }
            }
            if (StringUtils.isNotEmpty(xml)) {
                if (LOG.isDebugEnabled()) LOG.debug(xml);
                try {
                    BeanRequest beanXml = new BeanRequest(xml);
                    // get user
                    User user = null;
                    if (StringUtils.isNotEmpty(beanXml.getUsername()) && StringUtils.isNotEmpty(beanXml.getPassword())) {
                        user = getDao().getUserByUserId(beanXml.getUsername());
                        String encPwdMD5 = DigestUtils.md5Hex(beanXml.getPassword());
                        String encPwdDES = SomeUtils.encrypt3Des(beanXml.getPassword(), getEncryptionKey());
                        if (user != null && !user.getPassword().equalsIgnoreCase(encPwdMD5)  && !user.getPassword().equalsIgnoreCase(encPwdDES) && !user.getPassword().equalsIgnoreCase(beanXml.getPassword())) user = null;
                        if (user!=null && !user.hasPreference(USER_CAN_ACCESS,"Y")) user = null;
                    }
                    // llamar al metodo q le toca
                    boolean needCredentials = config.containsKey("needCredentials") && config.get("needCredentials") != null && Boolean.TRUE.equals(config.get("needCredentials"));
                    int maxItemsForRequest = (config.containsKey("maxItemsForRequest") && StringUtils.isNotEmpty((String) config.get("maxItemsForRequest"))) ? NumberUtils.toInt((String) config.get("maxItemsForRequest"), 1) : 1;
                    if (!needCredentials || user != null) {
                        if ("PriceAndAvailability".equalsIgnoreCase(beanXml.getAction())) {
                            if (beanXml.getPartNumbers() != null && !beanXml.getPartNumbers().isEmpty()) {
                                productData(beanXml, user, true, maxItemsForRequest);
                            }
                            else
                                error = "PartNumber is required";
                        } else if ("ProductDetail".equalsIgnoreCase(beanXml.getAction())) {
                            if (beanXml.getPartNumbers() != null && !beanXml.getPartNumbers().isEmpty())
                                productData(beanXml, user, false, maxItemsForRequest);
                            else
                                error = "PartNumber is required";
                        } else {
                            error = "Incorrect Action";
                        }
                    } else {
                        error = "Access Denied";
                    }
                    if (user!=null) {
                        List<UserPreference> listPref = user.getPreferencesByCode(USER_LAST_ACCESS);
                        UserPreference pref = (listPref!=null && !listPref.isEmpty()) ? listPref.get(0) : null;
                        if (pref==null) {
                            pref = new UserPreference();
                            pref.setUser(user);
                            pref.setPreferenceCode(USER_LAST_ACCESS);
                        }
                        pref.setPreferenceValue(Calendar.getInstance().getTime().toString());
                        getDao().save(pref);
                    }
                } catch (IOException e) {
                    LOG.error(e.getMessage(), e);
                    error = "Malformed Xml";
                } catch (SAXException e) {
                    LOG.error(e.getMessage(), e);
                    error = "Malformed Xml";
                } catch (Exception e) {
                    LOG.error(e.getMessage(), e);
                    error = e.getMessage();
                }
            } else {
                error = "Malformed Xml";
            }
            if (StringUtils.isNotEmpty(error)) {
                try {
                    StringBuilder buff = new StringBuilder();
                    buff.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
                    buff.append("<CatalogResponse>");
                    buff.append("<Error>").append(error).append("</Error>");
                    buff.append("</CatalogResponse>");
                    getResponse().setContentType("text/xml");
                    getResponse().getOutputStream().println(buff.toString());
                } catch (IOException e) {
                    LOG.error(e.getMessage(), e);
                }
            }
        }
        return null;
    }



    private void productData(BeanRequest beanXml, User user, boolean onlyPrice, int maxItemsForRequest) throws IOException {
        String lang = getDefaultLanguage();
        DecimalFormatSymbols dfs = new DecimalFormatSymbols(new Locale("en"));
        dfs.setDecimalSeparator('.');
        DecimalFormat df = new DecimalFormat("0.00", dfs);
        StringBuilder xmlResp = new StringBuilder();
        xmlResp.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
        xmlResp.append("<CatalogResponse>");
        java.util.List<String> partNumbers = beanXml.getPartNumbers();
        for (int i = 0, partNumbersSize = partNumbers.size(); i < Math.min(partNumbersSize,maxItemsForRequest); i++) {
            String partNumber = partNumbers.get(i);
            xmlResp.append("<ProductDetail>");
            xmlResp.append("<PartNumber>").append(StringEscapeUtils.escapeXml(partNumber)).append("</PartNumber>");
            Product product = getDao().getProductByPartNumber(partNumber);
            if (product != null) {
                UserLevel level = (user != null) ? user.getLevel() : null;
                Map priceMap = product.getPriceMap(level, 0, null);
                Double price = (priceMap.containsKey("FINAL_PRICE")) ? (Double) priceMap.get("FINAL_PRICE") : null;
                if (price != null && price > 0) {
                    xmlResp.append("<Stock>").append(product.getStock()).append("</Stock>");
                    xmlResp.append("<Price currency=\"").append(getActualCurrency().getCode()).append("\">").append(df.format(toActualCurrency(price))).append("</Price>");
                    if (product.getStock()<1 && StringUtils.isNotEmpty(product.getEta()))
                        xmlResp.append("<Eta>").append(product.getEta()).append("</Eta>");
                    if (!onlyPrice) {
                        ProductLang pLang = product.getLanguage(lang, getDefaultLanguage());
                        xmlResp.append("<Name>").append(StringEscapeUtils.escapeXml(product.getProductName(lang))).append("</Name>");
                        if (pLang != null && StringUtils.isNotEmpty(pLang.getDescription())) xmlResp.append("<Description>").append(StringEscapeUtils.escapeXml(pLang.getDescription())).append("</Description>");
                        if (pLang != null && StringUtils.isNotEmpty(pLang.getFeatures())) xmlResp.append("<Features>").append(StringEscapeUtils.escapeXml(pLang.getFeatures())).append("</Features>");
                        if (pLang != null && StringUtils.isNotEmpty(pLang.getInformation())) xmlResp.append("<AdditionalInformation>").append(StringEscapeUtils.escapeXml(pLang.getInformation())).append("</AdditionalInformation>");
                        if (product.getManufacturer()!=null) xmlResp.append("<Manufacturer>").append(StringEscapeUtils.escapeXml(product.getManufacturer().getManufacturerName())).append("</Manufacturer>");
                        if (product.getCategory()!=null) {
                            List<Category> lCat = getDao().getCategoryHierarchy(product.getCategory());
                            StringBuilder buffCat = new StringBuilder();
                            for(Category c : lCat) {
                                if (StringUtils.isNotEmpty(buffCat.toString())) buffCat.append("/");
                                buffCat.append(c.getCategoryName(getDefaultLanguage()));
                            }
                            xmlResp.append("<Category>").append(StringEscapeUtils.escapeXml(buffCat.toString())).append("</Category>");
                        }
                        // dimesion and weight
                        String dimensionUnit = getDimensionUnit();
                        Double length = NumberUtils.createDouble(getParentProperty(product, "dimentionLength"));
                        if (length != null) xmlResp.append("<DimensionLength unit=\"").append(dimensionUnit).append("\">").append(df.format(length)).append("</DimensionLength>");
                        Double width = NumberUtils.createDouble(getParentProperty(product, "dimentionWidth"));
                        if (width != null) xmlResp.append("<DimensionWidth unit=\"").append(dimensionUnit).append("\">").append(df.format(width)).append("</DimensionWidth>");
                        Double height = NumberUtils.createDouble(getParentProperty(product, "dimentionHeight"));
                        if (height != null) xmlResp.append("<DimensionHeight unit=\"").append(dimensionUnit).append("\">").append(df.format(height)).append("</DimensionHeight>");
                        Double weight = NumberUtils.createDouble(getParentProperty(product, "weight"));
                        if (weight != null) xmlResp.append("<Weight unit=\"").append(getWeightUnit()).append("\">").append(df.format(weight)).append("</Weight>");
                    }
                } else {
                    xmlResp.append("<Error>Unavailable Product</Error>");
                }
            } else {
                xmlResp.append("<Error>Product Not Found</Error>");
            }
            xmlResp.append("</ProductDetail>");
        }
        xmlResp.append("</CatalogResponse>");
        getResponse().setContentType("text/xml");
        getResponse().getOutputStream().print(xmlResp.toString());
    }


    protected Map getConfiguration(String propName) {
        StoreProperty bean = getDao().getStoreProperty(propName, StoreProperty.TYPE_GENERAL);
        if (bean != null && StringUtils.isNotEmpty(bean.getValue())) {
            try {
                Object o = JSONUtil.deserialize(bean.getValue());
                if (o != null && o instanceof Map) {
                    return (Map) o;
                }
            } catch (JSONException ignored) {
            }
        }
        return null;
    }

    private String xml;

    public String getXml() {
        return xml;
    }

    public void setXml(String xml) {
        this.xml = xml;
    }
}
