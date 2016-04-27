package org.store.publications.libranda;

import org.store.core.beans.Provider;
import org.store.core.dao.HibernateDAO;
import org.store.core.globals.BaseAction;
import org.store.core.globals.StoreThread;
import org.store.core.globals.config.Store20Database;
import org.store.core.hibernate.HibernateSessionFactory;
import org.store.publications.OnixProduct;
import org.apache.commons.lang.StringUtils;
import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.hibernate.Transaction;

import javax.servlet.http.HttpSession;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.File;
import java.io.FileInputStream;

/**
 * Rogelio Caballero
 * 8/09/11 18:44
 */
public class ONIXProcessor extends StoreThread {

    private String language;
    private String onixFile;
    private Provider provider;
    private static final String ONIX_TASK_PREFIX = "onix_task_";

    public ONIXProcessor(Store20Database config, String storeCode, String language, String basePath, HttpSession session) {
        this.databaseConfig = config;
        this.storeCode = storeCode;
        this.language = language;
        this.basePath = basePath;
        setName(ONIX_TASK_PREFIX + session.getId());
    }

    public ONIXProcessor(BaseAction action) {
        this.databaseConfig = action.getDatabaseConfig();
        this.storeCode = action.getStoreCode();
        this.language = action.getDefaultLanguage();
        this.basePath = action.getServletContext().getRealPath("/");
        setName(ONIX_TASK_PREFIX + action.getRequest().getSession().getId());
    }

    public void setOnixFile(String onixFile) {
        this.onixFile = onixFile;
    }

    public void initialize() {
        try {
            Session session = HibernateSessionFactory.getNewSession(this.databaseConfig);
            session.setFlushMode(FlushMode.MANUAL);
            Transaction tx = session.beginTransaction();
            try {
                HibernateDAO dao = new HibernateDAO(session, storeCode);
                provider = dao.getProviderByAltName("Libranda");
                if (provider == null) {
                    provider = new Provider();
                    provider.setAlternateNo("Libranda");
                    provider.setProviderName("Libranda");
                    dao.save(provider);
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
        }
    }

    @Override
    public void run() {
        if (StringUtils.isNotEmpty(onixFile)) {
            File file = new File(onixFile);
            if (file.exists()) {
                processONIX(file);
            } else {
                setExecutionResult(file.getName() + " -> ERROR: ONIX File not found. (" + onixFile + ")");
            }
        } else {
            setExecutionResult("ERROR: Empty ONIX file name");
        }

    }

    public void processONIX(File file) {
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
                        JAXBElement<org.store.publications.onix.Product> onixProduct = unmarshaller.unmarshal(xmlr, org.store.publications.onix.Product.class);
                        setExecutionMessage("Processing product " + index);
                        String res = ONIXUtils.updateProduct(new OnixProduct(onixProduct.getValue()), databaseConfig, storeCode, provider, basePath, file.getParent());
                        if (StringUtils.isNotEmpty(res)) addOutputMessage(res);
                        setExecutionPercent(100d * index++ / 400d);
                    }
                }
                setExecutionResult(file.getName() + " -> OK: " + (index - 1) + " product(s) processed.");
            } catch (JAXBException e) {
                log.error(e.getMessage(), e); 
            } catch (XMLStreamException e) {
                log.error(e.getMessage(), e); 
            } finally {
                xmlr.close();
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e); 
        }

    }


}
