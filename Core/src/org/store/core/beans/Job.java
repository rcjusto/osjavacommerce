package org.store.core.beans;

import org.store.core.beans.utils.StoreBean;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.struts2.json.JSONException;
import org.apache.struts2.json.JSONUtil;
import org.quartz.DateIntervalTrigger;
import org.quartz.Trigger;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Jobs
 */
@Entity
@Table(name = "t_job")
public class Job extends BaseBean implements StoreBean {

    public static final String STATUS_CANCELLED = "cancelled";
    public static final String STATUS_RUNNING = "running";
    public static final String STATUS_OK = "ok";
    public static final String STATUS_ERROR = "error";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 250)
    private String name;

    private Boolean active;

    private Date beginHour;

    @Column(length = 20)
    private String intervalUnit;

    private Integer intervalNumber;

    // Tienda en la q esta configurado el Job
    @Column(length = 10)
    private String inventaryCode;

    @Lob
    private String jobProps;
    @Transient
    private Map<String,String> jobMap;

    @Column(length = 50)
    private String executionStatus;

    private Date lastExecution;

    @Column(length = 50)
    private String executionThread;

    @Lob
    private String executionMsg;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getInventaryCode() {
        return inventaryCode;
    }

    public void setInventaryCode(String inventaryCode) {
        this.inventaryCode = inventaryCode;
    }

    public Boolean getActive() {
        return active!=null && active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public String getIntervalUnit() {
        return intervalUnit;
    }

    public void setIntervalUnit(String intervalUnit) {
        this.intervalUnit = intervalUnit;
    }

    public Integer getIntervalNumber() {
        return intervalNumber;
    }

    public void setIntervalNumber(Integer intervalNumber) {
        this.intervalNumber = intervalNumber;
    }

    public Date getBeginHour() {
        return beginHour;
    }

    public void setBeginHour(Date beginHour) {
        this.beginHour = beginHour;
    }

    public Date getLastExecution() {
        return lastExecution;
    }

    public void setLastExecution(Date lastExecution) {
        this.lastExecution = lastExecution;
    }

    public void setBeginHour(String strBeginHour) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        try {
            this.beginHour = sdf.parse(strBeginHour);
        } catch (ParseException e) {
            log.error(e.getMessage(), e);
        }
    }

    public String getStrBeginHour() {
        if (this.beginHour!=null) {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
            return sdf.format(this.beginHour);
        }
        return null;
    }

    public String getExecutionStatus() {
        return executionStatus;
    }

    public void setExecutionStatus(String executionStatus) {
        this.executionStatus = executionStatus;
    }

    public String getExecutionMsg() {
        return executionMsg;
    }

    public void setExecutionMsg(String executionMsg) {
        this.executionMsg = executionMsg;
    }

    public String getExecutionThread() {
        return executionThread;
    }

    public void setExecutionThread(String executionThread) {
        this.executionThread = executionThread;
    }

    public Trigger getQuartzTrigger() {
        DateIntervalTrigger.IntervalUnit unit = null;
        if ("MINUTE".equalsIgnoreCase(intervalUnit)) unit = DateIntervalTrigger.IntervalUnit.MINUTE;
        else if ("HOUR".equalsIgnoreCase(intervalUnit)) unit = DateIntervalTrigger.IntervalUnit.HOUR;
        else if ("DAY".equalsIgnoreCase(intervalUnit)) unit = DateIntervalTrigger.IntervalUnit.DAY;
        else if ("WEEK".equalsIgnoreCase(intervalUnit)) unit = DateIntervalTrigger.IntervalUnit.WEEK;
        else if ("MONTH".equalsIgnoreCase(intervalUnit)) unit = DateIntervalTrigger.IntervalUnit.MONTH;
        else if ("YEAR".equalsIgnoreCase(intervalUnit)) unit = DateIntervalTrigger.IntervalUnit.YEAR;
        if (unit != null && intervalNumber != null && intervalNumber > 0) {
            Calendar c = Calendar.getInstance();
            if (beginHour != null) {
                Calendar beginCalendar = Calendar.getInstance();
                beginCalendar.setTime(beginHour);
                c.set(Calendar.HOUR, beginCalendar.get(Calendar.HOUR));
                c.set(Calendar.MINUTE, beginCalendar.get(Calendar.MINUTE));
            }
            while (c.before(Calendar.getInstance())) {
                c.add(Calendar.DAY_OF_MONTH, 1);
            }
            return new DateIntervalTrigger(getInventaryCode()+"-"+getName(), c.getTime(), null, unit, intervalNumber);
        }
        return null;
    }

    public String getJobProps() {
        return jobProps;
    }

    public void setJobProps(String jobProps) {
        this.jobProps = jobProps;
    }

    public String getJobProperty(String key) {
        if (jobMap == null || jobMap.size() < 1) deserialize();
        return (jobMap != null && jobMap.containsKey(key)) ? jobMap.get(key) : null;
    }

    public String getJobProperty(String key, String defaultValue) {
        if (jobMap == null || jobMap.size() < 1) deserialize();
        return (jobMap != null && jobMap.containsKey(key)) ? jobMap.get(key) : defaultValue;
    }

    public Properties getJobProperties() {
        if (jobMap == null || jobMap.size() < 1) deserialize();
        Properties res = new Properties();
        if (jobMap!=null) {
            for(Map.Entry<String,String> e: jobMap.entrySet()) {
                if (StringUtils.isNotEmpty(e.getValue())) res.put(e.getKey(), e.getValue());
            }
        }
        return res;
    }

    public void setJobProperty(String key, String value) {
        if (jobMap==null) jobMap = new HashMap<String,String>();
        jobMap.put(key, value);
        serialize();
    }

    public void serialize() {
        try {
            jobProps =  (jobMap!=null && jobMap.size()>0) ? JSONUtil.serialize(jobMap) :null;
        } catch (JSONException e) {
            log.error(e.getMessage(), e);
        }
    }

    public void deserialize() {
        try {
            jobMap =  (!isEmpty(jobProps)) ? (HashMap) JSONUtil.deserialize(jobProps) : null;
        } catch (JSONException e) {
            log.error(e.getMessage(), e);
        }
    }
    public String toString() {
        return new ToStringBuilder(this)
                .append("id", getId())
                .toString();
    }

    public boolean equals(Object other) {
        if ((this == other)) return true;
        if (!(other instanceof Job)) return false;
        Job castOther = (Job) other;
        return new EqualsBuilder()
                .append(this.getId(), castOther.getId())
                .isEquals();
    }

}