package org.store.core.utils.quartz;

import org.store.core.beans.Job;
import org.store.core.dao.HibernateDAO;
import org.store.core.globals.StoreThread;
import org.store.core.globals.config.Store20Database;
import org.store.core.hibernate.HibernateSessionFactory;
import org.hibernate.Session;
import org.hibernate.Transaction;

public class JobStoreThread extends StoreThread {

    private Job job;



    public Job getJob() {
        return job;
    }

    public void setJob(Job job) {
        this.job = job;
    }

    public Store20Database getDatabaseConfig() {
        return databaseConfig;
    }

    public void setDatabaseConfig(Store20Database databaseConfig) {
        this.databaseConfig = databaseConfig;
    }

    public String getStoreCode() {
        return storeCode;
    }

    public void setStoreCode(String storeCode) {
        this.storeCode = storeCode;
    }

    protected void setJobExecutionResult(String status) {
        if (getJob() != null) {
            try {
                Session session = HibernateSessionFactory.getNewSession(databaseConfig);
                Transaction tx = session.beginTransaction();
                try {
                    StringBuffer buffer = new StringBuffer("");
                    if (outputMessages != null && !outputMessages.isEmpty()) {
                        for (String msg : outputMessages) {
                            buffer.append(msg).append("<br/>");
                        }
                    }

                    HibernateDAO dao = new HibernateDAO(session, storeCode);
                    Job j = dao.getJob(getJob().getName());
                    j.setExecutionStatus(status);
                    j.setExecutionMsg(buffer.toString());
                    dao.save(j);
                    tx.commit();
                } catch (Exception e) {
                    tx.rollback();
                } finally {
                    if (session.isOpen()) session.close();
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
    }
}
