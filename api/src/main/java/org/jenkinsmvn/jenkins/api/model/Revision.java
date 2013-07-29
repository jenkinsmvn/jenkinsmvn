package org.jenkinsmvn.jenkins.api.model;

/**
 * Defines a jenkins job details build detail change set revision
 */
public class Revision extends BaseModel {
    private String module;
    
    private Long revision;

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public Long getRevision() {
        return revision;
    }

    public void setRevision(Long revision) {
        this.revision = revision;
    }
}
