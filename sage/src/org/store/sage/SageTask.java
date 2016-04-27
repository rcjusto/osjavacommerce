package org.store.sage;

import com.opensymphony.xwork2.util.logging.Logger;
import com.opensymphony.xwork2.util.logging.LoggerFactory;
import org.store.core.beans.Job;
import org.store.core.beans.StoreProperty;
import org.store.core.beans.UserLevel;
import org.store.core.dao.HibernateDAO;
import org.store.core.globals.config.Store20Database;
import org.store.core.hibernate.HibernateSessionFactory;
import org.store.core.utils.quartz.JobStoreThread;
import org.store.sage.bean.SAGEUserLevel;
import org.apache.commons.lang.StringUtils;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;


public abstract class SageTask extends JobStoreThread {

    public static final String JDBC_DRIVER = "com.microsoft.sqlserver.jdbc.SQLServerDriver";

    public static final String PROP_GROUP_CONNECTION = "sage.connection";
    public static final String PROP_URL = "sage.url";
    public static final String PROP_USER = "sage.user";
    public static final String PROP_PASSWORD = "sage.password";

    protected static final Logger LOG = LoggerFactory.getLogger(SageTask.class);
    protected Properties properties;
    protected String defaultLanguage;
    protected String[] availableLanguages;
    protected HibernateDAO dao;
    protected Connection sageConnection;

    public SageTask(String storeCode, Job job, Store20Database databaseConfig) throws Exception {
        this.storeCode = storeCode;
        this.databaseConfig = databaseConfig;
        this.setJob(job);
        this.properties = job.getJobProperties();
        Class.forName(JDBC_DRIVER).newInstance();
    }

    public void run() {
        try {
            Session session = HibernateSessionFactory.getNewSession(databaseConfig);
            Transaction tx = session.beginTransaction();
            dao = new HibernateDAO(session, storeCode);
            try {
                defaultLanguage = dao.getStorePropertyValue(StoreProperty.PROP_DEFAULT_LANGUAGE, StoreProperty.TYPE_GENERAL, "en");
                StoreProperty bean = dao.getStoreProperty(StoreProperty.PROP_LANGUAGES, StoreProperty.TYPE_GENERAL);
                if (bean != null && !StringUtils.isEmpty(bean.getValue())) availableLanguages = bean.getValue().split(",");
                sageConnection = DriverManager.getConnection(properties.getProperty("sage.url"), properties.getProperty("sage.user"), properties.getProperty("sage.password"));
                try {
                    execute();
                    tx.commit();
                    setJobExecutionResult(Job.STATUS_OK);
                } catch (Exception e) {
                    log.error(e.getMessage(), e); 
                    LOG.error(e.getMessage());
                    addOutputMessage(e.getMessage());
                    setJobExecutionResult(Job.STATUS_ERROR);
                    throw e;
                } finally {
                    sageConnection.close();
                    LOG.info("Tarea Sage Terminada");
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e); 
                tx.rollback();
                addOutputMessage(e.getMessage());
                setJobExecutionResult(Job.STATUS_ERROR);
            } finally {
                if (session.isOpen()) session.close();
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e); 
        }
    }

    public Connection getConnection() {
        try {
            DriverManager.setLoginTimeout(15);
            sageConnection = DriverManager.getConnection(properties.getProperty("sage.url"), properties.getProperty("sage.user"), properties.getProperty("sage.password"));
        } catch (SQLException e) {
            log.error(e.getMessage(), e); 
            sageConnection = null;
        }
        return sageConnection;
    }

    protected abstract void execute();




    protected String getProperty(String key, String defaultValue) {
        return (properties.containsKey(key)) ? properties.getProperty(key) : defaultValue;
    }

    public String getStoreProperty(String propertyKey, String defaultValue) {
        StoreProperty bean = dao.getStoreProperty(propertyKey, StoreProperty.TYPE_GENERAL);
        return (bean != null && StringUtils.isNotEmpty(bean.getValue())) ? bean.getValue() : defaultValue;
    }

    /**
     * *****************************************************
     * metodos que se pueden usar desde cualquier modulo
     * ******************************************************
     */

    protected List<SAGEUserLevel> getUserLevelsToImport() {
        List<SAGEUserLevel> result = new ArrayList<SAGEUserLevel>();
        try {
            PreparedStatement stmt = sageConnection.prepareStatement("SELECT cbMarq, CT_Intitule FROM P_CATTARIF where CT_Intitule is not null and CT_Intitule <> ''");
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) result.add(new SAGEUserLevel(rs.getInt(1), rs.getString(2)));
        } catch (SQLException e) {
            log.error(e.getMessage(), e); 
        }
        return result;
    }

    protected UserLevel importUserLevel(SAGEUserLevel sageUserLevel) {
        if (sageUserLevel != null && sageUserLevel.getId() != null) {
            String name = (StringUtils.isNotEmpty(sageUserLevel.getName())) ? sageUserLevel.getName() : sageUserLevel.getId().toString();
            UserLevel bean = dao.getUserLevel(name);
            if (bean == null) {
                bean = new UserLevel();
                bean.setCode(name);
                for (String l : availableLanguages) bean.setName(l, name);
                dao.save(bean);
            }
            return bean;
        }
        return null;
    }

    protected SAGEUserLevel getSageLevel(UserLevel level) {
        if (level != null) {
            List<SAGEUserLevel> sageLevels = getUserLevelsToImport();
            for (SAGEUserLevel sageLevel : sageLevels) {
                if (level.getCode().equalsIgnoreCase(sageLevel.getName()) || level.getCode().equalsIgnoreCase(sageLevel.getId().toString()))
                    return sageLevel;
            }
        }
        return null;
    }


}


