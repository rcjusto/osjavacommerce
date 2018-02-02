package org.store.publications.libranda;

import org.store.core.beans.Job;
import org.store.core.globals.BaseAction;
import org.store.core.globals.config.Store20Database;
import org.store.core.utils.quartz.BaseJob;
import org.store.core.utils.quartz.JobPropertyGroup;
import org.store.publications.LibrariesEventServiceImpl;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.quartz.JobDetail;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CatalogUpdateJob extends BaseJob {


    @Override
    public String getName() {
        return "libranda.catalog.updater";
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
    public String getConfigurationLink(BaseAction action) {
        Map<String, String> params = new HashMap<String, String>();
        params.put("plugin", LibrariesEventServiceImpl.PLUGIN_NAME);
        params.put("supplier", "LIBRANDA");
        return action.url("editpluginproperty","admin",params);
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
                StringBuilder buff = new StringBuilder();
                job = getJob(session, storeCode);
                if (job != null && job.getActive()) {
                    try {
                        CatalogProcessor th = new CatalogProcessor(databaseConfig, storeCode, pathApp);
                        th.start();
                        buff.append("WebHosting Database Updated: OK");
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
        return job;
    }




}