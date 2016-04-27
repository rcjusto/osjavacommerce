package org.store.publications.libranda;

import com.sejer.dwh.GetCatalogSummary;
import com.sejer.dwh.GetCatalogSummaryDocument;
import com.sejer.dwh.GetCatalogSummaryResponseDocument;
import com.sejer.dwh.ProductSummary;
import org.store.core.beans.Manufacturer;
import org.store.core.beans.Product;
import org.store.core.beans.ProductLabel;
import org.store.core.beans.ProductLang;
import org.store.core.beans.ProductProvider;
import org.store.core.beans.Provider;
import org.store.core.beans.StoreProperty;
import org.store.core.dao.HibernateDAO;
import org.store.core.globals.SomeUtils;
import org.store.core.globals.StoreThread;
import org.store.core.globals.config.Store20Database;
import org.store.core.hibernate.HibernateSessionFactory;
import org.store.publications.LibrariesEventServiceImpl;
import org.store.publications.OnixProduct;
import com.store.publications.libranda.axis2.OrderServiceImpl2ServiceStub;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.struts2.json.JSONException;
import org.apache.struts2.json.JSONUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;

/**
 * Rogelio Caballero
 * 18/09/11 3:00
 */
public class CatalogProcessor extends StoreThread {

    private static final Integer PRODUCT_PER_PAGE = 20;
    private String productCode;
    private Date fromDate;
    private Provider provider;
    public static final String PROP_LAST_UPDATE = "last_update";
    public static final String SUPPLIER_NAME = "Libranda";

    public CatalogProcessor(Store20Database config, String storeCode, String basePath) {
        this.databaseConfig = config;
        this.storeCode = storeCode;
        this.basePath = basePath;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    public void setFromDate(Date fromDate) {
        this.fromDate = fromDate;
    }

    @Override
    public void run() {
        try {
            OrderServiceImpl2ServiceStub stub = new OrderServiceImpl2ServiceStub();
            String[] eanArr = (StringUtils.isNotEmpty(productCode)) ? new String[]{productCode} : null;
            Calendar cal = null;

            // leer configuracion
            Session session = HibernateSessionFactory.getNewSession(this.databaseConfig);
            Transaction tx = session.beginTransaction();
            String genCode;
            String password;
            String outletName;
            String language;
            try {
                HibernateDAO dao = new HibernateDAO(session, storeCode);
                Map config = getConfiguration(dao);
                genCode = (config.containsKey("libranda_gencode")) ? (String) config.get("libranda_gencode") : null;
                password = (config.containsKey("libranda_password")) ? (String) config.get("libranda_password") : null;
                outletName = (config.containsKey("libranda_outletName")) ? (String) config.get("libranda_outletName") : null;

                if (fromDate != null) {
                    cal = Calendar.getInstance();
                    cal.setTime(fromDate);
                } else {
                    StoreProperty sp = dao.getStoreProperty(PROP_LAST_UPDATE, StoreProperty.TYPE_GENERAL);
                    if (sp != null && StringUtils.isNotEmpty(sp.getValue())) {
                        Date d = SomeUtils.strToDate(sp.getValue(), "en");
                        if (d != null) {
                            cal = Calendar.getInstance();
                            cal.setTime(d);
                        }
                    }
                }

                language = dao.getStorePropertyValue(StoreProperty.PROP_DEFAULT_LANGUAGE, StoreProperty.TYPE_GENERAL, "en");

                tx.commit();
            } catch (Exception e) {
                tx.rollback();
                throw e;
            } finally {
                if (session.isOpen()) session.close();
            }

            // procesar
            int processed = 0, total = 1;
            while (processed < total) {
                GetCatalogSummary summary = GetCatalogSummary.Factory.newInstance();
                summary.setGencode(genCode);
                summary.setPassword(password);
                summary.setOutletName(outletName);
                summary.setEan13Array(eanArr);
                summary.setStartIndex(processed);
                summary.setCount(PRODUCT_PER_PAGE);
                summary.setFromDate(cal);

                GetCatalogSummaryDocument request = GetCatalogSummaryDocument.Factory.newInstance();
                request.setGetCatalogSummary(summary);
                GetCatalogSummaryResponseDocument response = stub.getCatalogSummary(request  );
                if (response!=null && response.getGetCatalogSummaryResponse()!=null && response.getGetCatalogSummaryResponse().getReturn()!=null) {
                    Integer responseCount = SomeUtils.strToInteger(response.getGetCatalogSummaryResponse().getReturn().getCount());
                    Integer responseTotal = SomeUtils.strToInteger(response.getGetCatalogSummaryResponse().getReturn().getTotal());
                    if (responseCount != null) processed += responseCount;
                    if (responseTotal != null) total = responseTotal;

                    if (response.getGetCatalogSummaryResponse().getReturn().getProductsArray() != null) {
                        for (ProductSummary ps : response.getGetCatalogSummaryResponse().getReturn().getProductsArray()) {
                            if (StringUtils.isNotEmpty(ps.getOnix())) {
                                try {
                                    XMLInputFactory xmlif = XMLInputFactory.newInstance();
                                    XMLStreamReader xmlr = xmlif.createXMLStreamReader(new StringReader(ps.getOnix()));
                                    JAXBContext ucontext = JAXBContext.newInstance(org.store.publications.onix.Product.class);
                                    Unmarshaller unmarshaller = ucontext.createUnmarshaller();
                                    try {
                                        JAXBElement<org.store.publications.onix.Product> onixProduct = unmarshaller.unmarshal(xmlr, org.store.publications.onix.Product.class);
                                        ONIXUtils.updateProduct(new OnixProduct(onixProduct.getValue()), databaseConfig, storeCode, provider, basePath, null);
                                    } catch (JAXBException e) {
                                        updateProduct(ps);
                                    }
                                } catch (Exception e) {
                                    updateProduct(ps);
                                }

                            } else {
                                updateProduct(ps);
                            }

                        }
                    }

                }


            }
        } catch (Exception e) {
            log.error(e.getMessage(), e); 
        }
    }


    private void updateProduct(ProductSummary productSummary) {
        try {
            Session session = HibernateSessionFactory.getNewSession(this.databaseConfig);
            Transaction tx = session.beginTransaction();
            try {
                HibernateDAO dao = new HibernateDAO(session, storeCode);

                String[] languages = dao.getLanguages();
                String partNumber = productSummary.getEan13();
                Product product = null;
                if (StringUtils.isNotEmpty(partNumber)) {
                    product = dao.getProductOfProvider(provider, partNumber);
                }
                if (StringUtils.isNotEmpty(partNumber)) {

                    String title = "";

                    boolean isNew = false;
                    if (product == null) {
                        product = new Product();
                        product.setMfgPartnumber(partNumber);
                        product.setProductType(Product.TYPE_DIGITAL);
                        product.setStock(999999l);
                        product.setActive(true);
                        isNew = true;

                        // set new label
                        ProductLabel labelNew = dao.getProductLabelByCode(ProductLabel.LABEL_NEW);
                        if (labelNew != null) {
                            if (product.getLabels() == null) product.setLabels(new HashSet<ProductLabel>());
                            product.getLabels().add(labelNew);
                        }
                    }
                    if (StringUtils.isEmpty(product.getPartNumber())) product.setPartNumber(partNumber);


                    // titulos
                    title = productSummary.getName();
                    // autor
                    StringBuilder author = new StringBuilder();
                    if (StringUtils.isNotEmpty(productSummary.getAuthorFirstName())) author.append(productSummary.getAuthorFirstName());
                    if (StringUtils.isNotEmpty(productSummary.getAuthorLastName())) {
                        if (StringUtils.isNotEmpty(author.toString())) author.append(" ");
                        author.append(productSummary.getAuthorLastName());
                    }
                    if (StringUtils.isNotEmpty(author.toString())) {
                        Manufacturer m = dao.getManufacturerByName(author.toString());
                        if (m == null) {
                            m = new Manufacturer();
                            m.setInventaryCode(storeCode);
                            m.setManufacturerName(author.toString());
                            dao.updateManufacturerUrlCode(m, author.toString());
                            dao.save(m);
                        }
                        product.setManufacturer(m);
                    }

                    if (isNew && StringUtils.isNotEmpty(productSummary.getCoverUrl())) {
                        String extension = FilenameUtils.getExtension(productSummary.getCoverUrl());
                        BufferedInputStream in = null;
                        FileOutputStream fout = null;
                        try {
                            String filePath = basePath + File.separator + storeCode + File.separator + "uploads";
                            FileUtils.forceMkdir(new File(filePath));
                            filePath += File.separator + product.getPartNumber() + "." + extension;

                            in = new BufferedInputStream(new URL(productSummary.getCoverUrl()).openStream());
                            fout = new FileOutputStream(filePath);

                            byte data[] = new byte[1024];
                            int count;
                            while ((count = in.read(data, 0, 1024)) != -1) {
                                fout.write(data, 0, count);
                            }
                        } catch (Exception ignored) {

                        } finally {
                            if (in != null) try {
                                in.close();
                            } catch (IOException ignored) {
                            }
                            if (fout != null) try {
                                fout.close();
                            } catch (IOException ignored) {
                            }
                        }
                    }

                    if (productSummary.getPriceAmount() != null) {
                        Double price = SomeUtils.forceStrToDouble(productSummary.getPriceAmount());
                        if (price != null) product.setPrice(price);
                    }

                    dao.save(product);

                    if (isNew) {
                        ProductProvider pp = new ProductProvider();
                        pp.setActive(false);
                        pp.setProduct(product);
                        pp.setProvider(provider);
                        pp.setSku(partNumber);
                        pp.setCostPrice(product.getCostPrice());
                        pp.setStock(product.getStock());
                        dao.save(pp);
                    }

                    if (languages != null) {
                        for (String lang : languages) {
                            ProductLang productLang = dao.getProductLang(product, lang);
                            if (productLang == null) {
                                productLang = new ProductLang();
                                productLang.setProduct(product);
                                productLang.setProductLang(lang);
                            }
                            productLang.setProductName(title);
                            dao.save(productLang);
                        }
                    }
                }

                tx.commit();
            } catch (Exception e) {
                tx.rollback();
                throw e;
            } finally {
                if (session.isOpen()) session.close();
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e); 
            addOutputMessage(e.getMessage());
        }

    }


    private Map getConfiguration(HibernateDAO dao) {
        StoreProperty bean = dao.getStoreProperty(LibrariesEventServiceImpl.PROP_CONFIGURATION_PROPERTY, StoreProperty.TYPE_GENERAL);
        if (bean != null && StringUtils.isNotEmpty(bean.getValue())) {
            try {
                Object o = JSONUtil.deserialize(bean.getValue());
                if (o != null && o instanceof Map) {
                    return (Map) o;
                }
            } catch (JSONException e) {
                log.error(e.getMessage(), e); 
            }
        }

        provider = dao.getProviderByAltName(SUPPLIER_NAME);
        if (provider == null) {
            provider = new Provider();
            provider.setAlternateNo(SUPPLIER_NAME);
            provider.setProviderName(SUPPLIER_NAME);
            dao.save(provider);
        }

        return null;
    }

}
