package org.jenkinsmvn.jenkins.api.model;

/**
 * Jenkins Views
 */
public class View extends BaseModel {
    private String name;
    
    private String url;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
