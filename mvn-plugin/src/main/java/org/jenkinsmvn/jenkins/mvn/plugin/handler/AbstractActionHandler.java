package org.jenkinsmvn.jenkins.mvn.plugin.handler;

import org.jenkinsmvn.jenkins.api.JenkinsClient;
import org.jenkinsmvn.jenkins.api.model.Job;
import org.jenkinsmvn.jenkins.mvn.plugin.Action;
import org.jenkinsmvn.jenkins.mvn.plugin.Reference;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.logging.Log;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Base class for action handlers
 */
public abstract class AbstractActionHandler {

    public static final int LOG_TOLERANCE_MILLIS = 15000;

    private String actionType;

    protected JenkinsClient client;
    
    protected Map<String, Reference> references;

    protected AbstractMojo owner;
    
    protected List<String> resultingJobs;
    
    protected String logPrefix = "";

    protected File targetDir;
    
    protected File jenkinsTargetDir;
    
    protected Action action;

    public AbstractActionHandler(String actionType) {
        this.actionType = actionType;
    }

    public String getActionType() {
        return actionType;
    }

    public void setClient(JenkinsClient client) {
        this.client = client;
    }

    public void setReferences(Map<String, Reference> references) {
        this.references = references;
    }

    public void setOwner(AbstractMojo owner) {
        this.owner = owner;
    }

    public void setTargetDir(File targetDir) {
        this.targetDir = targetDir;

        jenkinsTargetDir = new File(targetDir, "jenkins");

        if(!jenkinsTargetDir.isDirectory()) {
            jenkinsTargetDir.mkdirs();
        }
    }

    public void setAction(Action action) {
        this.action = action;
    }

    public Log getLog() {
        return owner.getLog();
    }
    
    public void addJobResult(String jobName) {
        if(resultingJobs == null) {
            resultingJobs = new ArrayList<String>();
        }

        resultingJobs.add(jobName);
    }

    public List<String> getResultingJobs() {
        return resultingJobs;
    }
    
    public void logInfo(String message) {
        getLog().info(String.format("%s%s %s", action.getLogPrefix(), logPrefix, message));
    }

    protected boolean isOnNoJobsContinue() {
        List<String> jobRefs = new ArrayList<String>();

        if(action.getJobListRef() != null) {
            jobRefs.add(action.getJobListRef());
        }
        if(CollectionUtils.isNotEmpty(action.getJobListRefs())) {
            jobRefs.addAll(action.getJobListRefs());
        }

        throw new IllegalStateException(String.format("%s Job reference with ids '%s' has no jobs.", logPrefix, jobRefs.toString()));
    }

    /**
     * Returns all relevant jobs for this action.
     *
     * @return the relevant jobs
     */
    protected List<String> getRelevantJobs() {
        List<String> tmp = new ArrayList<String>();

        if(StringUtils.isNotBlank(action.getJobName())) {
            tmp.add(action.getJobName());
        }

        addJobsFromRef(action.getJobListRef(), tmp);
        
        if(CollectionUtils.isNotEmpty(action.getJobNames())) {
            tmp.addAll(action.getJobNames());
        }
        
        if(CollectionUtils.isNotEmpty(action.getJobListRefs())) {
            for(String refId : action.getJobListRefs()) {
                addJobsFromRef(refId, tmp);
            }
        }

        return Collections.unmodifiableList(tmp);
    }
    
    private void addJobsFromRef(String refId, List<String> tmp) {
        if(StringUtils.isNotBlank(refId)) {
            refId = StringUtils.trim(refId);

            Reference reference = references.get(refId);

            if(reference != null && CollectionUtils.isNotEmpty(reference.getJobs())) {
                tmp.addAll(reference.getJobs());
            }
        }
    }

    protected List<String> getRefJobNames(String refId) {
        Reference reference = references.get(refId);

        if(reference == null) {
            return Collections.emptyList();
        }

        return Collections.unmodifiableList(reference.getJobs());
    }

    protected void waitForBuild(Job job) throws IOException, InterruptedException {
        HandlerUtils.INSTANCE.waitForBuild(action, job, this);
    }

    public abstract void execute() throws IOException;

    public JenkinsClient getClient() {
        return client;
    }

}
