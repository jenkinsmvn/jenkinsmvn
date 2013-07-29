package org.jenkinsmvn.jenkins.api.model;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import java.util.List;

/**
 * Defines a jenkins node.
 */
public class Node extends BaseModel {
    private List<AssignedLabel> assignedLabels;
    
    private String mode;
    
    private String nodeDescription;
    
    private String nodeName;
    
    private String description;
    
    private Integer numExecutors;
    
    private List<Job> jobs;

    private OverallLoad overallLoad;

    private View primaryView;

    private Boolean quietingDown;

    private Integer slaveAgentPort;

    private Boolean useCrumbs;

    private Boolean useSecurity;
    
    private List<View> views;
    
    public List<AssignedLabel> getAssignedLabels() {
        return assignedLabels;
    }

    public void setAssignedLabels(List<AssignedLabel> assignedLabels) {
        this.assignedLabels = assignedLabels;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<Job> getJobs() {
        return jobs;
    }

    public void setJobs(List<Job> jobs) {
        this.jobs = jobs;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getNodeDescription() {
        return nodeDescription;
    }

    public void setNodeDescription(String nodeDescription) {
        this.nodeDescription = nodeDescription;
    }

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public Integer getNumExecutors() {
        return numExecutors;
    }

    public void setNumExecutors(Integer numExecutors) {
        this.numExecutors = numExecutors;
    }

    public OverallLoad getOverallLoad() {
        return overallLoad;
    }

    public void setOverallLoad(OverallLoad overallLoad) {
        this.overallLoad = overallLoad;
    }

    public View getPrimaryView() {
        return primaryView;
    }

    public void setPrimaryView(View primaryView) {
        this.primaryView = primaryView;
    }

    public Boolean getQuietingDown() {
        return quietingDown;
    }

    public void setQuietingDown(Boolean quietingDown) {
        this.quietingDown = quietingDown;
    }

    public Integer getSlaveAgentPort() {
        return slaveAgentPort;
    }

    public void setSlaveAgentPort(Integer slaveAgentPort) {
        this.slaveAgentPort = slaveAgentPort;
    }

    public Boolean getUseCrumbs() {
        return useCrumbs;
    }

    public void setUseCrumbs(Boolean useCrumbs) {
        this.useCrumbs = useCrumbs;
    }

    public Boolean getUseSecurity() {
        return useSecurity;
    }

    public void setUseSecurity(Boolean useSecurity) {
        this.useSecurity = useSecurity;
    }

    public List<View> getViews() {
        return views;
    }

    public void setViews(List<View> views) {
        this.views = views;
    }
}
