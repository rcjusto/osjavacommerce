package org.store.suppliers;

import org.store.core.beans.Job;
import org.store.core.globals.config.Store20Config;
import org.store.core.globals.config.Store20Database;
import org.store.core.utils.quartz.BaseJob;
import org.store.core.utils.quartz.JobPropertyGroup;
import org.store.core.utils.suppliers.SupplierUtils;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.quartz.JobDetail;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

public class SuppliersJob extends BaseJob {

    @Override
    public String getName() {
        return "suppliers.tool";
    }

    @Override
    public String getDescription(String lang) {
        return null;
    }

    @Override
    public String getCronExpression() {
        return "0 0 1 * * ?";
    }

    @Override
    public List<JobPropertyGroup> getPropertyNames() {
        return new ArrayList<JobPropertyGroup>();
    }

    @Override
    public Job executeNow(JobDetail jobDetail) {
        Job job = null;
        try {
            String storeCode = (String) jobDetail.getJobDataMap().get("store");
            Store20Database databaseConfig = (Store20Database) jobDetail.getJobDataMap().get("database");
            Session session = getSession(databaseConfig);
            Transaction tx = session.beginTransaction();
            try {
                StringBuffer buff = new StringBuffer();
                job = getJob(session, storeCode);
                if (job != null && job.getActive()) {
                    try {
                        Store20Config config = (Store20Config) jobDetail.getJobDataMap().get(Store20Config.ATTRIBUTE_APPLICATION_CONFIG);
                        Map<String,Class> map =  config.getMapSuplier();
                        SupplierUtils su = new SupplierUtils(map,session,storeCode,databaseConfig);
                        su.start();
                        buff.append("Process Supliers: OK");
                        job.setExecutionStatus(Job.STATUS_OK);
                        job.setExecutionThread(su.getName());
                    } catch (Exception e) {
                        log.error(e.getMessage(), e); 
                        buff.append("ERROR: ").append(e.getMessage());
                        job.setExecutionStatus(Job.STATUS_ERROR);
                        job.setExecutionThread(null);
                    }
                    job.setLastExecution(Calendar.getInstance().getTime());
                    job.setExecutionMsg(buff.toString());
                    session.saveOrUpdate(job);
                }
                tx.commit();
            } catch (HibernateException e) {
                tx.rollback();
                log.error(e.getMessage(), e); 
            } finally {
                if (session != null && session.isOpen()) session.close();
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e); 
        }
        return job;
    }

}