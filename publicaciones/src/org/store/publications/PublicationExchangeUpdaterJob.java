package org.store.publications;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.quartz.JobDetail;
import org.store.core.beans.Job;
import org.store.core.globals.config.Store20Database;
import org.store.core.utils.quartz.BaseJob;
import org.store.core.utils.quartz.JobPropertyGroup;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Rogelio Caballero
 * 11/06/12 17:41
 */
public class PublicationExchangeUpdaterJob extends BaseJob {

    @Override
    public String getName() {
        return "publications.currency.rate.updater";
    }

    @Override
    public String getDescription(String lang) {
        return null;
    }

    @Override
    public String getCronExpression() {
        return "0 0 * * * ?";
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
                        PublicationExchangeUpdater th = new PublicationExchangeUpdater(databaseConfig, storeCode);
                        th.setJob(job);
                        th.start();
                        buff.append("Publication Exchange Updater: OK");
                        job.setExecutionStatus(Job.STATUS_OK);
                        job.setExecutionThread(th.getName());
                    } catch (Exception e) {
                        buff.append("ERROR: ").append(e.getMessage());
                        job.setExecutionStatus(Job.STATUS_ERROR);
                        log.error(e.getMessage(), e);
                    }
                    job.setExecutionMsg(buff.toString());
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
        return job;    }
}
