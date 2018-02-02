package org.store.publications.publidisa;

import org.store.core.beans.Provider;
import org.store.core.beans.StoreProperty;
import org.store.core.dao.HibernateDAO;
import org.store.core.globals.config.Store20Database;
import org.store.core.hibernate.HibernateSessionFactory;
import org.store.core.utils.quartz.JobStoreThread;
import org.store.publications.LibrariesEventServiceImpl;
import org.store.publications.OnixProduct;
import org.store.publications.onix.Product;
import org.apache.commons.lang.StringUtils;
import org.apache.struts2.json.JSONException;
import org.apache.struts2.json.JSONUtil;
import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.hibernate.Transaction;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Rogelio Caballero
 * 18/09/11 3:00
 */
public class CatalogProcessor extends JobStoreThread {

    private Map configuration;
    private File ONIXFile;
    private Provider provider;
    private static final int ESTIMATED_TOTAL_PRODUCTS = 2000;
    public static final String SUPPLIER_NAME = "Publidisa";

    public void setONIXFile(File ONIXFile) {
        this.ONIXFile = ONIXFile;
    }

    public CatalogProcessor(Store20Database config, String storeCode, String basePath) {
        this.databaseConfig = config;
        this.storeCode = storeCode;
        this.basePath = basePath;
        this.configuration = readConfiguration();
    }

    @Override
    public void run() {
        try {
            String configurationErrors = validateConfiguration();
            if (configurationErrors==null) {
                File[] files = (ONIXFile!=null) ? new File[] {ONIXFile} : readONIXList();
                if (files!=null && files.length>0) {
                    for(File file : files) {
                        // extract
                        String folderPath = file.getAbsolutePath().substring(0, file.getAbsolutePath().length() - 4);
                        File folder = new File(folderPath);
                        unzipFileIntoDirectory(file, folder);

                        if (folder.exists() && folder.isDirectory()) {
                            file.renameTo(new File(file.getPath() +".bak"));
                            setExecutionMessage("Processing ONIX File: " + folder.getName());
                            setExecutionPercent(0d);
                            processONIXFolder(folder);
                        }
                    }
                }
                setJobExecutionResult("OK");
            } else {
                addOutputMessage(configurationErrors);
                setJobExecutionResult("Configuration Error");
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e); 
            addOutputMessage(e.getMessage());
            setJobExecutionResult("ERROR");
        }
    }

    private Map readConfiguration() {
        try {
            Session session = HibernateSessionFactory.getNewSession(this.databaseConfig);
            session.setFlushMode(FlushMode.MANUAL);
            Transaction tx = session.beginTransaction();
            try {
                Map result = null;
                HibernateDAO dao = new HibernateDAO(session, storeCode);
                StoreProperty bean = dao.getStoreProperty(LibrariesEventServiceImpl.PROP_CONFIGURATION_PROPERTY, StoreProperty.TYPE_GENERAL);
                if (bean != null && StringUtils.isNotEmpty(bean.getValue())) {
                    try {
                        Object o = JSONUtil.deserialize(bean.getValue());
                        if (o != null && o instanceof Map) {
                            result = (Map) o;
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

                tx.commit();
                return result;
            } catch (Exception e) {
                tx.rollback();
                throw e;
            } finally {
                if (session.isOpen()) session.close();
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e); 
        }

        return null;
    }

    private String validateConfiguration() {
        if (configuration==null) return "There are no configuration data for connecting with Publidisa";
        if (StringUtils.isEmpty((String) configuration.get("publidisa_login"))) return "Login is required to connect with Publidisa";
        if (StringUtils.isEmpty((String) configuration.get("publidisa_password"))) return "Password is required to connect with Publidisa";
        if (StringUtils.isEmpty((String) configuration.get("publidisa_userCode"))) return "User code is required to connect with Publidisa";
        return null;
    }

    private File[] readONIXList() {
        File onixPath = new File(basePath + File.separator + "stores" + File.separator + storeCode + File.separator + "publications"+File.separator+LibrariesEventServiceImpl.FOLDER_PUBLIDISA);
        if (onixPath.exists() && onixPath.isDirectory()) {
            File[] onixs = onixPath.listFiles(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return name.endsWith(".zip");
                }
            });
            if (onixs != null && onixs.length > 0) Arrays.sort(onixs);
            return onixs;
        }
        return null;
    }

    private void processONIXFolder(File folder) {
        // identificar datos del onix
        String[] arr = folder.getName().split("_");
        String onixDate = (arr!=null && arr.length>1) ? arr[1] : null;
        String onixType = (arr!=null && arr.length>2) ? arr[2] : null;

        File[] onixFiles = folder.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.endsWith(".xml");
            }
        });

        StringBuilder output = new StringBuilder();
        output.append(folder.getName()).append(" -> ");
        if (onixFiles!=null && onixFiles.length>0) {
            for(File file : onixFiles)
                output.append(processONIXFile(file));
        } else {
            output.append("ERROR: Xml file not founded");
        }
        addOutputMessage(output.toString());
    }

    private String processONIXFile(File file) {
        String downloadPreview = (String) configuration.get("publidisa_downloadPreview");
        // boolean processDigital = "Y".equalsIgnoreCase ((String) configuration.get("publidisa_processDigital"));
        // boolean processPaper = "Y".equalsIgnoreCase ((String) configuration.get("publidisa_processPaper"));
        boolean processPaper = false;
        boolean processDigital = true;
        try {
            XMLInputFactory xmlif = XMLInputFactory.newInstance();
            XMLStreamReader xmlr = xmlif.createXMLStreamReader(new FileInputStream(file), "utf-8");
            JAXBContext ucontext = JAXBContext.newInstance(Product.class);
            Unmarshaller unmarshaller = ucontext.createUnmarshaller();
            try {
                int index = 1;
                int eventType;
                setExecutionPercent(0d);
                while (xmlr.hasNext()) {
                    eventType = xmlr.next();
                    if (eventType == XMLStreamConstants.START_ELEMENT && xmlr.getLocalName().equals("Product")) {
                        JAXBElement<Product> op = unmarshaller.unmarshal(xmlr, Product.class);
                        setExecutionMessage("Processing product " + index);
                        OnixProduct onixProduct = new OnixProduct(op.getValue());
                        if (OnixProduct.TYPE_STANDARD.equalsIgnoreCase(onixProduct.getType()) && processPaper || OnixProduct.TYPE_DIGITAL.equalsIgnoreCase(onixProduct.getType()) && processDigital) {
                            String res = ONIXUtils.updateProduct(onixProduct, databaseConfig, storeCode, provider, basePath, file.getParent() , "Y".equalsIgnoreCase(downloadPreview));
                            if (StringUtils.isNotEmpty(res)) addOutputMessage(res);
                            setExecutionPercent(100d * index++ / ESTIMATED_TOTAL_PRODUCTS);
                        }
                    }
                }
                return file.getName() + " -> OK: " + (index - 1) + " product(s) processed.";
            } catch (JAXBException e) {
                log.error(e.getMessage(), e); 
                return "ERROR: " +e.getMessage();
            } catch (XMLStreamException e) {
                log.error(e.getMessage(), e); 
                return "ERROR: " +e.getMessage();
            } finally {
                xmlr.close();
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e); 
            return "ERROR: " +e.getMessage();
        }

    }


   public void unzipFileIntoDirectory(File archive, File destinationDir) throws IOException {

            final int BUFFER_SIZE = 1024;

            BufferedOutputStream dest = null;
            FileInputStream fis = new FileInputStream(archive);
            ZipInputStream zis = new ZipInputStream(new BufferedInputStream(fis));
            ZipEntry entry;
            File destFile;
            while((entry = zis.getNextEntry()) != null) {

                destFile = new File(destinationDir.getAbsolutePath() + File.separator + entry.getName());

                if (entry.isDirectory()) {
                    destFile.mkdirs();
                } else {
                    int count;
                    byte data[] = new byte[BUFFER_SIZE];

                    destFile.getParentFile().mkdirs();

                    FileOutputStream fos = new FileOutputStream(destFile);
                    dest = new BufferedOutputStream(fos, BUFFER_SIZE);
                    while ((count = zis.read(data, 0, BUFFER_SIZE)) != -1) {
                        dest.write(data, 0, count);
                    }

                    dest.flush();
                    dest.close();
                    fos.close();
                }
            }
            zis.close();
            fis.close();
        }

}
