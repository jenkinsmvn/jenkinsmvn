package org.jenkinsmvn.jenkins.api.model;

import org.apache.commons.collections.CollectionUtils;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Determines a jenkins job detail
 *
 * todo there is something wrong in parsing modules for now don't parse modules.
 */
public class JobDetails extends BaseModel {

//    private List<Action> actions;
    
    private String description;
    
    private String displayName;
    
    private String name;
    
    private URL url;

    private List<Build> builds;

    private Boolean buildable;

    private String color;
    
    private Build firstBuild;
    
    private Build lastBuild;
    
    private Build lastCompletedBuild;

    private Build lastFailedBuild;
    
    private Build lastStableBuild;
    
    private Build lastSuccessfulBuild;
    
    private Build lastUnstableBuild;
    
    private Build lastUnsuccessfulBuild;
    
    private Boolean inQueue;
    
    private Boolean keepDependencies;

    private List<HealthReport> healthReport;

    private Integer nextBuildNumber;

    private QueueItem queueItem;
    
    private Boolean concurrentBuild;

    private List<UpstreamProject> upstreamProjects;

    private Scm scm;

    public List<UpstreamProject> getUpstreamProjects() {
        return upstreamProjects;
    }

    public void setUpstreamProjects(List<UpstreamProject> upstreamProjects) {
        this.upstreamProjects = upstreamProjects;
    }

    public List<String> getUpstreamProjectNames() {
        if(CollectionUtils.isEmpty(upstreamProjects)) {
            return Collections.emptyList();
        }

        List<String> names = new ArrayList<String>(upstreamProjects.size());

        for(UpstreamProject project : upstreamProjects) {
            names.add(project.getName());
        }

        return names;
    }

//    private Module modules;

//    public List<Action> getActions() {
//        return actions;
//    }
//
//    public void setActions(List<Action> actions) {
//        this.actions = actions;
//    }

    public Boolean getBuildable() {
        return buildable;
    }

    public void setBuildable(Boolean buildable) {
        this.buildable = buildable;
    }

    public List<Build> getBuilds() {
        return builds;
    }

    public void setBuilds(List<Build> builds) {
        this.builds = builds;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public Boolean getConcurrentBuild() {
        return concurrentBuild;
    }

    public void setConcurrentBuild(Boolean concurrentBuild) {
        this.concurrentBuild = concurrentBuild;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public Build getFirstBuild() {
        return firstBuild;
    }

    public void setFirstBuild(Build firstBuild) {
        this.firstBuild = firstBuild;
    }

    public List<HealthReport> getHealthReport() {
        return healthReport;
    }

    public void setHealthReport(List<HealthReport> healthReport) {
        this.healthReport = healthReport;
    }

    public Boolean getInQueue() {
        return inQueue;
    }

    public void setInQueue(Boolean inQueue) {
        this.inQueue = inQueue;
    }

    public Boolean getKeepDependencies() {
        return keepDependencies;
    }

    public void setKeepDependencies(Boolean keepDependencies) {
        this.keepDependencies = keepDependencies;
    }

    public Build getLastBuild() {
        return lastBuild;
    }

    public void setLastBuild(Build lastBuild) {
        this.lastBuild = lastBuild;
    }

    public Build getLastCompletedBuild() {
        return lastCompletedBuild;
    }

    public void setLastCompletedBuild(Build lastCompletedBuild) {
        this.lastCompletedBuild = lastCompletedBuild;
    }

    public Build getLastFailedBuild() {
        return lastFailedBuild;
    }

    public void setLastFailedBuild(Build lastFailedBuild) {
        this.lastFailedBuild = lastFailedBuild;
    }

    public Build getLastStableBuild() {
        return lastStableBuild;
    }

    public void setLastStableBuild(Build lastStableBuild) {
        this.lastStableBuild = lastStableBuild;
    }

    public Build getLastSuccessfulBuild() {
        return lastSuccessfulBuild;
    }

    public void setLastSuccessfulBuild(Build lastSuccessfulBuild) {
        this.lastSuccessfulBuild = lastSuccessfulBuild;
    }

    public Build getLastUnstableBuild() {
        return lastUnstableBuild;
    }

    public void setLastUnstableBuild(Build lastUnstableBuild) {
        this.lastUnstableBuild = lastUnstableBuild;
    }

    public Build getLastUnsuccessfulBuild() {
        return lastUnsuccessfulBuild;
    }

    public void setLastUnsuccessfulBuild(Build lastUnsuccessfulBuild) {
        this.lastUnsuccessfulBuild = lastUnsuccessfulBuild;
    }

//    public Module getModules() {
//        return modules;
//    }
//
//    public void setModules(Module modules) {
//        this.modules = modules;
//    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getNextBuildNumber() {
        return nextBuildNumber;
    }

    public void setNextBuildNumber(Integer nextBuildNumber) {
        this.nextBuildNumber = nextBuildNumber;
    }

    public QueueItem getQueueItem() {
        return queueItem;
    }

    public void setQueueItem(QueueItem queueItem) {
        this.queueItem = queueItem;
    }

    public Scm getScm() {
        return scm;
    }

    public void setScm(Scm scm) {
        this.scm = scm;
    }

    public URL getUrl() {
        return url;
    }

    public void setUrl(URL url) {
        this.url = url;
    }
}
