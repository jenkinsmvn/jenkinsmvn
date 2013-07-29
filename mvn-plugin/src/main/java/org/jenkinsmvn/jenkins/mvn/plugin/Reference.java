package org.jenkinsmvn.jenkins.mvn.plugin;

import java.util.List;

/**
 * Determines a jenkins job
 */
public class Reference {
    
    private String id;
    
    private String refId;
    
    private String suffix;
    
    private String prefix;
    
    private List<String> jobs;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<String> getJobs() {
        return jobs;
    }

    public void setJobs(List<String> jobs) {
        this.jobs = jobs;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getRefId() {
        return refId;
    }

    public void setRefId(String refId) {
        this.refId = refId;
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }
}
