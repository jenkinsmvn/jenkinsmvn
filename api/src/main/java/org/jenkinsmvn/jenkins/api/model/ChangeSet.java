package org.jenkinsmvn.jenkins.api.model;

import java.util.List;

/**
 * Defines a jenkins job details build detail change set
 */
public class ChangeSet extends BaseModel {

    // todo for items

    private String kind;

    private List<Revision> revision;

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public List<Revision> getRevision() {
        return revision;
    }

    public void setRevision(List<Revision> revision) {
        this.revision = revision;
    }
}
