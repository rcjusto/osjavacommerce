package org.store.sage;

import org.store.core.beans.Job;
import org.store.core.globals.BaseAction;
import org.store.core.globals.config.Store20Database;
import org.store.core.utils.quartz.BaseJob;
import org.store.core.utils.quartz.JobPropertyGroup;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.quartz.JobDetail;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SageCustomersJob extends BaseJob {

    public static final String JOB_NAME = "sage.customers.synchronization";

    public static final String PROP_GROUP_IMPORT = "sage.import.from.sage";
    public static final String PROP_IMPORT_NEW_CLIENTS = "import.new.clients";
    public static final String PROP_UPDATE_OLD_CLIENTS = "update.old.clients";
    public static final String PROP_GROUP_EXPORT = "sage.export.to.sage";
    public static final String PROP_UPDATE_CLIENT_ADDRESSES = "update.client.addresses.to.sage";
    public static final String PROP_UPDATE_CLIENT_NAMES = "update.client.names.to.sage";
    public static final String PROP_UPDATE_CONTACT_INFO = "update.client.contact-info.to.sage";

    @Override
    public String getName() {
        return JOB_NAME;
    }

    @Override
    public String getDescription(String lang) {
        return null;
    }

    @Override
    public String getCronExpression() {
        return "0 0 4 * * ?";
    }

    @Override
    public List<JobPropertyGroup> getPropertyNames() {
        return null;
    }

    @Override
    public String getConfigurationLink(BaseAction action) {
        Map<String, String> params = new HashMap<String, String>();
        params.put("plugin", SageEventService.EVENT_SERVICE_NAME);
        params.put("activeTab", "2");
        return action.url("editpluginproperty","admin",params);
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
                job = getJob(session,  storeCode);
                if (job != null && job.getActive()) {
                    try {
                        SageCustomersTask sageUtils = new SageCustomersTask( storeCode, job, databaseConfig);
                        sageUtils.start();
                        buff.append("Customers Synchronization: OK");
                        job.setExecutionStatus(Job.STATUS_OK);
                        job.setExecutionThread(sageUtils.getName());
                    } catch (Exception e) {
                        buff.append("ERROR: ").append(e.getMessage());
                        log.error(e.getMessage(), e); 
                        job.setExecutionStatus(Job.STATUS_ERROR);
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