package org.jenkinsmvn.jenkins.mvn.plugin;

import java.util.List;
import java.util.Properties;

/**
 * Determines a jenkins action
 */
public class Action {
    
    private String jobListRef;

    private String jobName;
    
    private List<String> jobNames;
    
    private List<String> jobListRefs;

    private String type;
    
    private String name;
    
    private String logPrefix;

    private Properties properties;

    public String getJobListRef() {
        return jobListRef;
    }

    public void setJobListRef(String jobListRef) {
        this.jobListRef = jobListRef;
    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public Properties getProperties() {
        return properties;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getJobListRefs() {
        return jobListRefs;
    }

    public void setJobListRefs(List<String> jobListRefs) {
        this.jobListRefs = jobListRefs;
    }

    public List<String> getJobNames() {
        return jobNames;
    }

    public void setJobNames(List<String> jobNames) {
        this.jobNames = jobNames;
    }

    public String getLogPrefix() {
        return logPrefix;
    }

    public void setLogPrefix(String logPrefix) {
        this.logPrefix = logPrefix;
    }
}
