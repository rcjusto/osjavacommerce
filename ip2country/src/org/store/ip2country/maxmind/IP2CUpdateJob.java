package org.store.ip2country.maxmind;

import org.store.core.beans.Job;
import org.store.core.globals.config.Store20Database;
import org.store.core.utils.quartz.BaseJob;
import org.store.core.utils.quartz.JobPropertyGroup;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.quartz.JobDetail;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class IP2CUpdateJob extends BaseJob {


    @Override
    public String getName() {
        return "maxmind.database.updater";
    }

    @Override
    public String getDescription(String lang) {
        return "Update MaxMind GeoIP Country Database";
    }

    @Override
    public String getCronExpression() {
        return "0 0 * * * ?";
    }

    @Override
    public List<JobPropertyGroup> getPropertyNames() {
        List<JobPropertyGroup> l = new ArrayList<JobPropertyGroup>();
        l.add(new JobPropertyGroup("general").addProperty("licenseNumber"));
        return l;
    }

    @Override
    public Job executeNow(JobDetail jobDetail) {
        Job job = null;
        try {
            String storeCode = (String) jobDetail.getJobDataMap().get("store");
            Store20Database databaseConfig = (Store20Database) jobDetail.getJobDataMap().get("database");
            String pathApp = (String) jobDetail.getJobDataMap().get("pathApp");
            Session session = getSession(databaseConfig);
            Transaction tx = session.beginTransaction();
            try {
                job = getJob(session, storeCode);
                if (job != null && job.getActive()) {
                    try {
                        IP2CUtils th = new IP2CUtils(pathApp);
                        th.setJob(job);
                        th.setStoreCode(storeCode);
                        th.setDatabaseConfig(databaseConfig);
                        th.start();
                        job.setExecutionThread(th.getName());
                    } catch (Exception e) {
                        job.setExecutionMsg(e.getMessage());
                        job.setExecutionStatus(Job.STATUS_ERROR);
                        log.error(e.getMessage(), e);
                    }
                    job.setLastExecution(Calendar.getInstance().getTime());
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