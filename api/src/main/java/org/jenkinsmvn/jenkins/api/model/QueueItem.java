package org.jenkinsmvn.jenkins.api.model;

/**
 * Jenkins queue item
 */
public class QueueItem extends BaseModel {
    private Boolean blocked;
    
    private Boolean buildable;
    
    private Long id;
    
    private String param;
    
    private Boolean stuck;

    private Task task;
    
    private String why;
    
    private Long buildableStartMilliseconds;

    public Boolean getBlocked() {
        return blocked;
    }

    public void setBlocked(Boolean blocked) {
        this.blocked = blocked;
    }

    public Boolean getBuildable() {
        return buildable;
    }

    public void setBuildable(Boolean buildable) {
        this.buildable = buildable;
    }

    public Long getBuildableStartMilliseconds() {
        return buildableStartMilliseconds;
    }

    public void setBuildableStartMilliseconds(Long buildableStartMilliseconds) {
        this.buildableStartMilliseconds = buildableStartMilliseconds;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getParam() {
        return param;
    }

    public void setParam(String param) {
        this.param = param;
    }

    public Boolean getStuck() {
        return stuck;
    }

    public void setStuck(Boolean stuck) {
        this.stuck = stuck;
    }

    public Task getTask() {
        return task;
    }

    public void setTask(Task task) {
        this.task = task;
    }

    public String getWhy() {
        return why;
    }

    public void setWhy(String why) {
        this.why = why;
    }
}
