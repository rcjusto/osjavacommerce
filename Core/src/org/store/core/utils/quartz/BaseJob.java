package org.store.core.utils.quartz;

import org.store.core.globals.BaseAction;
import org.store.core.globals.config.Store20Database;
import org.store.core.hibernate.HibernateSessionFactory;
import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.util.List;

public abstract class BaseJob implements Job {

    public static Logger log = Logger.getLogger(BaseJob.class);
    public abstract String getName();
    public abstract String getDescription(String lang);
    public abstract String getCronExpression();
    public abstract List<JobPropertyGroup> getPropertyNames();
    public abstract org.store.core.beans.Job executeNow(JobDetail jobDetail);

    public Session getSession(Store20Database databaseConfig) {
        try {
            return HibernateSessionFactory.getNewSession(databaseConfig);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    public void execute(JobExecutionContext context) throws JobExecutionException {
        executeNow(context.getJobDetail());    
    }

    public String getConfigurationLink(BaseAction action) {
        return null;
    }


    public org.store.core.beans.Job getJob(Session session, String storeCode) {
        List<org.store.core.beans.Job> l = session.createCriteria(org.store.core.beans.Job.class)
                .add(Restrictions.eq("name",getName()))
                .add(Restrictions.eq("inventaryCode",storeCode))
                .list();
        return (l!=null && l.size()>0) ? l.get(0) : null;
    }

}
