package org.store.publications.digitalbooks;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.struts2.json.JSONException;
import org.apache.struts2.json.JSONUtil;
import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.store.core.beans.Provider;
import org.store.core.beans.StoreProperty;
import org.store.core.dao.HibernateDAO;
import org.store.core.globals.config.Store20Database;
import org.store.core.hibernate.HibernateSessionFactory;
import org.store.core.utils.quartz.JobStoreThread;
import org.store.publications.LibrariesEventServiceImpl;
import org.store.publications.OnixProduct;
import org.store.publications.onix.Product;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.*;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Rogelio Caballero
 * 18/09/11 3:00
 */
public class CatalogProcessor extends JobStoreThread {

    private Map configuration;
    private File ONIXFile;
    private Provider provider;
    private static final int ESTIMATED_TOTAL_PRODUCTS = 2000;
    public static final String SUPPLIER_NAME = "DigitalBooks";

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
            if (configurationErrors == null) {
                File[] files = (ONIXFile != null) ? new File[]{ONIXFile} : readONIXList();
                if (files != null && files.length > 0) {
                    for (File file : files) {
                        String folderPath = file.getAbsolutePath().substring(0, file.getAbsolutePath().length() - 4);
                        File folder = new File(folderPath);
                        unzip(file, folderPath);

                        if (folder.exists() && folder.isDirectory()) {
                            file.renameTo(new File(file.getPath() + ".bak"));
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
        if (configuration == null) return "There are no configuration data for connecting with Digital Books";
        if (StringUtils.isEmpty((String) configuration.get("digitalbooks_user"))) return "User is required to connect with Digital Books";
        if (StringUtils.isEmpty((String) configuration.get("digitalbooks_password"))) return "Password is required to connect with Digital Books";
        if (StringUtils.isEmpty((String) configuration.get("digitalbooks_elibrary"))) return "E-Library ID is required to connect with Digital Books";
        return null;
    }

    private File[] readONIXList() {
        final String libreria = (String) configuration.get("digitalbooks_elibrary");
        File onixPath = new File(basePath + File.separator + "stores" + File.separator + storeCode + File.separator + "publications" + File.separator + LibrariesEventServiceImpl.FOLDER_DIGITALBOOKS);
        log.info("Searching xmls: " + onixPath.getPath());
        if (onixPath.exists()) {
            File[] onixs = onixPath.listFiles(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return name.endsWith(".zip") && name.startsWith(libreria + "_");
                }
            });
            if (onixs != null && onixs.length > 0) Arrays.sort(onixs);
            log.info("Found " + ((onixs != null) ? onixs.length : 0) + " xmls");
            return onixs;
        } else {
            log.info("No existe la carpeta: " + onixPath.getPath());
        }
        return null;
    }

    private void processONIXFolder(File folder) {
        // identificar datos del onix
        String[] arr = folder.getName().split("_");
//        String onixDate = (arr!=null && arr.length>1) ? arr[1] : null;
//        String onixType = (arr!=null && arr.length>2) ? arr[2] : null;

        File[] onixFiles = folder.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.endsWith(".xml");
            }
        });

        StringBuilder output = new StringBuilder();
        output.append(folder.getName()).append(" -> ");
        if (onixFiles != null && onixFiles.length > 0) {
            for (File file : onixFiles)
                output.append(processONIXFile(file));
        } else {
            output.append("ERROR: Xml file not founded");
        }
        addOutputMessage(output.toString());
    }

    private String processONIXFile(File file) {
        try {
            XMLInputFactory xmlif = XMLInputFactory.newInstance();
            XMLStreamReader xmlr = xmlif.createXMLStreamReader(new FileInputStream(file), "utf-8");
            JAXBContext ucontext = JAXBContext.newInstance(org.store.publications.onix.Product.class);
            Unmarshaller unmarshaller = ucontext.createUnmarshaller();
            try {
                int index = 1;
                int eventType;
                setExecutionPercent(0d);
                while (xmlr.hasNext()) {
                    eventType = xmlr.next();
                    if (eventType == XMLStreamConstants.START_ELEMENT && xmlr.getLocalName().equals("Product")) {
                        JAXBElement<Product> onixProduct = unmarshaller.unmarshal(xmlr, org.store.publications.onix.Product.class);
                        setExecutionMessage("Processing product " + index);
                        String res = ONIXUtils.updateProduct(new OnixProduct(onixProduct.getValue()), databaseConfig, storeCode, provider, basePath, file.getParent());
                        if (StringUtils.isNotEmpty(res)) addOutputMessage(res);
                        setExecutionPercent(100d * index++ / ESTIMATED_TOTAL_PRODUCTS);
                    }
                }
                return file.getName() + " -> OK: " + (index - 1) + " product(s) processed.";
            } catch (JAXBException e) {
                log.error(e.getMessage(), e);
                return "ERROR: " + e.getMessage();
            } catch (XMLStreamException e) {
                log.error(e.getMessage(), e);
                return "ERROR: " + e.getMessage();
            } finally {
                xmlr.close();
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return "ERROR: " + e.getMessage();
        }

    }

    public void unzip(File file, String newPath) throws IOException {

        int BUFFER = 2048;
        ZipFile zip = new ZipFile(file);

        FileUtils.forceMkdir(new File(newPath));
        Enumeration zipFileEntries = zip.entries();

        // Process each entry
        while (zipFileEntries.hasMoreElements()) {
            // grab a zip file entry
            ZipEntry entry = (ZipEntry) zipFileEntries.nextElement();

            String currentEntry = entry.getName();

            File destFile = new File(newPath, currentEntry);
            destFile = new File(newPath, destFile.getName());
            File destinationParent = destFile.getParentFile();

            // create the parent directory structure if needed
            FileUtils.forceMkdir(destinationParent);
            if (!entry.isDirectory()) {
                BufferedInputStream is = new BufferedInputStream(zip.getInputStream(entry));
                int currentByte;
                // establish buffer for writing file
                byte data[] = new byte[BUFFER];

                // write the current file to disk
                FileOutputStream fos = new FileOutputStream(destFile);
                BufferedOutputStream dest = new BufferedOutputStream(fos, BUFFER);

                // read and write until last byte is encountered
                while ((currentByte = is.read(data, 0, BUFFER)) != -1) {
                    dest.write(data, 0, currentByte);
                }
                dest.flush();
                dest.close();
                is.close();
            }
        }
    }

}
