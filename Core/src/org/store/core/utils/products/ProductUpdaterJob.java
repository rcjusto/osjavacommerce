package org.store.core.utils.products;

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

public class ProductUpdaterJob extends BaseJob {

 
    @Override
    public String getName() {
        return "product.updater";
    }

    @Override
    public String getDescription(String lang) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String getCronExpression() {
        return "0 0 2 * * ?";
    }

    @Override
    public List<JobPropertyGroup> getPropertyNames() {
        List<JobPropertyGroup> result = new ArrayList<JobPropertyGroup>();
        result.add(new JobPropertyGroup("general").addProperty("days_to_remove_new_label"));
        return result;
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
                job = getJob(session, storeCode);
                if (job != null && job.getActive()) {
                    ProductUpdaterThreat th = new ProductUpdaterThreat(storeCode, databaseConfig);
                    th.setJob(job);
                    job.setExecutionStatus(Job.STATUS_OK);
                    th.start();
                    job.setLastExecution(Calendar.getInstance().getTime());
                    job.setExecutionThread(th.getName());
                    job.setExecutionMsg("Product Updated: OK");
                    session.saveOrUpdate(job);
                }
                tx.commit();
            } catch (HibernateException e) {
                tx.rollback();
                job.setLastExecution(Calendar.getInstance().getTime());
                job.setExecutionStatus(Job.STATUS_ERROR);
                job.setExecutionMsg(e.getMessage());
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
