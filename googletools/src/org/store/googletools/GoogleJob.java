package org.store.googletools;

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

public class GoogleJob extends BaseJob {


    @Override
    public String getName() {
        return "google.tools";
    }

    @Override
    public String getDescription(String lang) {
        return null;
    }

    @Override
    public String getCronExpression() {
        return "0 0/10 * * * ?";
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
                StringBuilder buff = new StringBuilder();
                job = getJob(session, storeCode);
                if (job != null && job.getActive()) {
                    try {
                        GoogleUtils th = new GoogleUtils( storeCode, databaseConfig);
                        th.start();
                        buff.append("Google XML Generation: OK");
                        job.setExecutionStatus(Job.STATUS_OK);
                        job.setExecutionThread(th.getName());
                    } catch (Exception e) {
                        buff.append("ERROR: ").append(e.getMessage());
                        job.setExecutionStatus(Job.STATUS_ERROR);
                        BaseJob.log.error(e.getMessage(), e);
                    }
                    job.setExecutionMsg(buff.toString());
                    job.setLastExecution(Calendar.getInstance().getTime());
                    session.saveOrUpdate(job);
                }
                tx.commit();
            } catch (HibernateException e) {
                tx.rollback();
                BaseJob.log.error(e.getMessage(), e);
            } finally {
                if (session != null && session.isOpen()) session.close();
            }

        } catch (Exception e) {
            BaseJob.log.error(e.getMessage(), e);
        }
         return job;
    }

}