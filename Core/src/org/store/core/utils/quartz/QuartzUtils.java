package org.store.core.utils.quartz;

import com.opensymphony.xwork2.util.logging.Logger;
import com.opensymphony.xwork2.util.logging.LoggerFactory;
import org.apache.commons.io.FilenameUtils;
import org.store.core.beans.Job;
import org.store.core.dao.HibernateDAO;
import org.store.core.globals.config.Store20Config;
import org.store.core.globals.config.Store20Database;
import org.apache.commons.lang.StringUtils;
import org.quartz.CronTrigger;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.impl.StdSchedulerFactory;

import javax.servlet.ServletContext;
import java.text.ParseException;
import java.util.Enumeration;
import java.util.Map;

public class QuartzUtils {

    private static final Logger log = LoggerFactory.getLogger(QuartzUtils.class);
    private ServletContext servletContext;
    private HibernateDAO dao;
    private Map<String, Class> jobsMap;

    public QuartzUtils(ServletContext servletContext, HibernateDAO dao) {
        this.servletContext = servletContext;
        this.dao = dao;
        this.jobsMap = Store20Config.getInstance(servletContext).getMapJobs();
    }

    public Map<String, Class> getJobsMap() {
        return jobsMap;
    }

    public BaseJob getJob(String jobName) throws IllegalAccessException, InstantiationException {
        if (jobsMap.containsKey(jobName)) {
            Object o = jobsMap.get(jobName).newInstance();
            if (o instanceof BaseJob) return (BaseJob) o;
        }
        return null;
    }

    public void configureTask(String storeCode, String jobName, Store20Database databaseConfig, Store20Config storeConfig) throws IllegalAccessException, InstantiationException, SchedulerException {
        if (jobsMap.containsKey(jobName)) {
            Class jobClass = jobsMap.get(jobName);
            Trigger jobTrigger = null;
            boolean activate = true;
            Job bean = dao.getJob(jobName);
            if (bean != null) {
                activate = bean.getActive();
                if (activate) {
                    Trigger savedTrigger = bean.getQuartzTrigger();
                    if (savedTrigger != null) jobTrigger = savedTrigger;
                }
            }

            Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
            if (!scheduler.isStarted()) scheduler.start();
            if (activate) {
                if (jobTrigger == null) {
                    BaseJob bj = (BaseJob) jobClass.newInstance();
                    if (StringUtils.isNotEmpty(bj.getCronExpression())) {
                        CronTrigger cronTrigger = new CronTrigger();
                        try {
                            cronTrigger.setName(storeCode + "-" + bj.getName());
                            cronTrigger.setCronExpression(bj.getCronExpression());
                        } catch (ParseException e) {
                            log.error(e.getMessage(), e);
                            cronTrigger = null;
                        }
                        if (cronTrigger != null) jobTrigger = cronTrigger;
                    }
                }
                if (jobTrigger != null) {
                    JobDetail jobDetail = new JobDetail(storeCode + "-" + jobName, Scheduler.DEFAULT_GROUP, jobClass);
                    jobDetail.getJobDataMap().put("store", storeCode);
                    jobDetail.getJobDataMap().put("database", databaseConfig);
                    jobDetail.getJobDataMap().put("config", storeConfig);
                    String basePath = FilenameUtils.getFullPath(servletContext.getRealPath("/"));
                    log.debug("Base path for jobs: "+basePath);
                    jobDetail.getJobDataMap().put("pathApp", basePath);
                    Enumeration<String> e = servletContext.getAttributeNames();
                    while (e.hasMoreElements()) {
                        String key = e.nextElement();
                        jobDetail.getJobDataMap().put(key, servletContext.getAttribute(key));
                    }
                    scheduler.deleteJob(storeCode + "-" + jobName, Scheduler.DEFAULT_GROUP);
                    scheduler.scheduleJob(jobDetail, jobTrigger);
                } else {
                    log.warn("Default trigger not implemented for Job: " + jobName);
                }
            } else {
                scheduler.deleteJob(storeCode + "-" + jobName, Scheduler.DEFAULT_GROUP);
            }
        }
    }

    public void configureTasks(String storeCode, Store20Database databaseConfig, Store20Config storeConfig) {
        if (jobsMap != null && !jobsMap.isEmpty()) {
            try {
                for (String jobName : jobsMap.keySet()) {
                    configureTask(storeCode, jobName, databaseConfig, storeConfig);
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    public Trigger getJobTrigger(String storeCode, String jobName) throws SchedulerException {
        if (jobsMap.containsKey(jobName)) {
            Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
            Trigger[] arrTriggers = scheduler.getTriggersOfJob(storeCode + "-" + jobName, Scheduler.DEFAULT_GROUP);
            if (arrTriggers != null && arrTriggers.length > 0) {
                return arrTriggers[0];
            }
        }
        return null;
    }

    public Job executeNow(String storeCode, String jobName) throws SchedulerException, InstantiationException, IllegalAccessException {
        Job result = null;
        BaseJob job = getJob(jobName);
        if (job != null) {
            Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
            JobDetail jobDetail = scheduler.getJobDetail(storeCode + "-" + jobName, Scheduler.DEFAULT_GROUP);
            result = job.executeNow(jobDetail);
        }
        return result;
    }


}
