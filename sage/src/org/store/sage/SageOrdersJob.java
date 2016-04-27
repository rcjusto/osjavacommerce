package org.store.sage;

import org.store.core.beans.Job;
import org.store.core.beans.OrderStatus;
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

public class SageOrdersJob extends BaseJob {

    public static final String JOB_NAME = "sage.orders.synchronization";

    public static final String PROP_GROUP_OPTIONS = "sage.options";
    public static final String PROP_ORDER_STATUS_TO_EXPORT = "sage.order.status.to.export";
    public static final String PROP_ORDER_STATUS_TO_EXPORT_DEFAULT = OrderStatus.STATUS_APPROVED;
    public static final String PROP_SHIPPING_LINE_NAME = "sage.order.shipping.line.name";
    public static final String PROP_SHIPPING_LINE_NAME_DEFAULT = "PORTES";
    public static final String PROP_ORDER_CARRIER_PREFIX = "sage.order.carrier.";
    public static final String PROP_ORDER_PICK_IN_STORE = "sage.order.pick.in.store";

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
        params.put("activeTab", "3");
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
                job = getJob(session,  storeCode);
                if (job != null && job.getActive()) {
                    try {
                        SageOrdersTask sageUtils = new SageOrdersTask(storeCode, job, databaseConfig);
                        sageUtils.start();
                        job.setExecutionThread(sageUtils.getName());
                    } catch (Exception e) {
                        log.error(e.getMessage(), e); 
                        job.setExecutionStatus(Job.STATUS_ERROR);
                        job.setExecutionMsg(e.getMessage());
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