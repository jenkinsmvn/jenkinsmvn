package org.jenkinsmvn.jenkins.api.model;

import java.net.URL;
import java.util.List;

/**
 * Defines a jenkins job details build details.
 */
public class BuildDetails extends BaseModel {

    private List<Artifact> artifacts;

    private Boolean building;
    
    private String description;
    
    private Long duration;
    
    private String fullDisplayName;
    
    private String id;
    
    private Boolean keepLog;
    
    private Integer number;
    
    private String result;
    
    private Long timestamp;
    
    private URL url;
    
    private String builtOn;
    
    private ChangeSet changeSet;
    
    private String mavenVersionUsed;

    // todo actions, culprits, mavenArtificats

    public List<Artifact> getArtifacts() {
        return artifacts;
    }

    public void setArtifacts(List<Artifact> artifacts) {
        this.artifacts = artifacts;
    }

    public Boolean getBuilding() {
        return building;
    }

    public void setBuilding(Boolean building) {
        this.building = building;
    }

    public String getBuiltOn() {
        return builtOn;
    }

    public void setBuiltOn(String builtOn) {
        this.builtOn = builtOn;
    }

    public ChangeSet getChangeSet() {
        return changeSet;
    }

    public void setChangeSet(ChangeSet changeSet) {
        this.changeSet = changeSet;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getDuration() {
        return duration;
    }

    public void setDuration(Long duration) {
        this.duration = duration;
    }

    public String getFullDisplayName() {
        return fullDisplayName;
    }

    public void setFullDisplayName(String fullDisplayName) {
        this.fullDisplayName = fullDisplayName;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Boolean getKeepLog() {
        return keepLog;
    }

    public void setKeepLog(Boolean keepLog) {
        this.keepLog = keepLog;
    }

    public String getMavenVersionUsed() {
        return mavenVersionUsed;
    }

    public void setMavenVersionUsed(String mavenVersionUsed) {
        this.mavenVersionUsed = mavenVersionUsed;
    }

    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public URL getUrl() {
        return url;
    }

    public void setUrl(URL url) {
        this.url = url;
    }
}
