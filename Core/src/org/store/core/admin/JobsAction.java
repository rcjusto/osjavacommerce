package org.store.core.admin;

import org.store.core.beans.Job;
import org.store.core.beans.utils.BreadCrumb;
import org.store.core.globals.StoreMessages;
import org.store.core.globals.config.Store20Config;
import org.store.core.utils.quartz.BaseJob;
import org.store.core.utils.quartz.QuartzUtils;
import org.store.core.utils.quartz.JobStoreThread;
import org.store.core.utils.quartz.ThreadUtilities;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Namespace(value = "/admin")
@ParentPackage(value = "store-admin")
public class JobsAction extends AdminModuleAction implements StoreMessages {

    @Action(value = "jobpropertieslist", results = @Result(type = "velocity", location = "/WEB-INF/views/admin/jobpropertieslist.vm"))
    public String jobPropertiesList() throws Exception {
        QuartzUtils qu = new QuartzUtils(getServletContext(), dao);
        List<String> jobNames = new ArrayList<String>();
        jobNames.addAll(qu.getJobsMap().keySet());
        Collections.sort(jobNames);
        List<Map<String, Object>> jobList = new ArrayList<Map<String, Object>>();
        for (String jobName : jobNames) {
            Map<String, Object> m = new HashMap<String, Object>();
            Job aJob = dao.getJob(jobName);
            m.put("job", aJob);
            m.put("base", qu.getJob(jobName));
            m.put("trigger", qu.getJobTrigger(getStoreCode(), jobName));
            jobList.add(m);
        }
        addToStack("jobList", jobList);
        getBreadCrumbs().add(new BreadCrumb(null, getText("background.jobs"), null, null));
      return SUCCESS;
    }

    @Action(value = "jobproperties", results = @Result(type = "velocity", location = "/WEB-INF/views/admin/jobproperties.vm"))
    public String jobProperties() throws Exception {
        QuartzUtils qu = new QuartzUtils(getServletContext(), dao);
        List<String> jobNames = new ArrayList<String>();
        jobNames.addAll(qu.getJobsMap().keySet());
        Collections.sort(jobNames);
        addToStack("jobNames", jobNames);
        job = dao.getJob(jobName);
        BaseJob bo = qu.getJob(jobName);
        if (bo != null) {
            String conflink = bo.getConfigurationLink(this);
            if (StringUtils.isNotEmpty(conflink)) addToStack("configLink", conflink);
            addToStack("propertyGroups", bo.getPropertyNames());
            addToStack("trigger", qu.getJobTrigger(getStoreCode(), jobName));
        }
        getBreadCrumbs().add(new BreadCrumb(null, getText("background.jobs"), url("jobpropertieslist","/admin"), null));
        getBreadCrumbs().add(new BreadCrumb(null, getText(jobName), null, null));
        return SUCCESS;
    }

    @Action(value = "jobpropertiessave", results = @Result(type = "redirectAction", location = "jobproperties?jobName=${job.name}"))
    public String jobPropertiesSave() throws Exception {
        if (job != null) {
            job.setBeginHour(jobStartAt);
            job.setInventaryCode(getStoreCode());
            if (jobPropertyKey != null && ArrayUtils.isSameLength(jobPropertyKey, jobPropertyValue))
                for (int i = 0; i < jobPropertyKey.length; i++)
                    if (StringUtils.isNotEmpty(jobPropertyKey[i]))
                        job.setJobProperty(jobPropertyKey[i], jobPropertyValue[i]);
            dao.save(job);
            QuartzUtils qu = new QuartzUtils(getServletContext(), dao);
            qu.configureTask(getStoreCode(), job.getName(), getDatabaseConfig(), Store20Config.getInstance(getServletContext()));
        }
        return SUCCESS;
    }

    @Action(value = "jobexecute", results = @Result(type = "json", params = {"root","jsonResp"}))
    public String jobExecuteNow() throws Exception {
        jsonResp = new HashMap<String, Serializable>();
        try {
            if (jobName != null) {
                jsonResp.put("job", jobName);
                QuartzUtils qu = new QuartzUtils(getServletContext(), dao);
                Job job = qu.executeNow(getStoreCode(), jobName);
                if (job != null) {
                    jsonResp.put("result", "ok");
                } else {
                    jsonResp.put("result", "error");
                    jsonResp.put("msg", "Job not found");
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            jsonResp.put("result", "error");
            jsonResp.put("msg", e.getMessage());
        }
        return SUCCESS;
    }

    @Action(value = "jobexecutestate", results = @Result(type = "json", params = {"root","jsonResp"}))
    public String jobExecuteState() throws Exception {
        jsonResp = new HashMap<String, Serializable>();
        if (jobName != null) {
            Job job = dao.getJob(jobName);
            if (job != null && StringUtils.isNotEmpty(job.getExecutionThread())) {
                jsonResp.put("result", "ok");
                Thread th = ThreadUtilities.getThread(job.getExecutionThread());
                if (th != null && th instanceof JobStoreThread) {
                    JobStoreThread sth = (JobStoreThread) th;
                    jsonResp.put("msg", sth.getExecutionMessage());
                    jsonResp.put("percent", sth.getExecutionPercent());
                } else {
                    jsonResp.put("msg", "FINISHED");
                    jsonResp.put("percent", 100);
                    job.setExecutionThread(null);
                    dao.save(job);
                }
            } else {
                jsonResp.put("result", "error");
                jsonResp.put("msg", "Job not found");
            }
        }
        return SUCCESS;
    }

    private Job job;
    private String jobName;
    private String jobStartAt;
    private String jobIntervalUnit;
    private String[] jobPropertyKey;
    private String[] jobPropertyValue;

    public Job getJob() {
        return job;
    }

    public void setJob(Job job) {
        this.job = job;
    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public String getJobStartAt() {
        return jobStartAt;
    }

    public void setJobStartAt(String jobStartAt) {
        this.jobStartAt = jobStartAt;
    }

    public String getJobIntervalUnit() {
        return jobIntervalUnit;
    }

    public void setJobIntervalUnit(String jobIntervalUnit) {
        this.jobIntervalUnit = jobIntervalUnit;
    }

    public String[] getJobPropertyKey() {
        return jobPropertyKey;
    }

    public void setJobPropertyKey(String[] jobPropertyKey) {
        this.jobPropertyKey = jobPropertyKey;
    }

    public String[] getJobPropertyValue() {
        return jobPropertyValue;
    }

    public void setJobPropertyValue(String[] jobPropertyValue) {
        this.jobPropertyValue = jobPropertyValue;
    }
}
