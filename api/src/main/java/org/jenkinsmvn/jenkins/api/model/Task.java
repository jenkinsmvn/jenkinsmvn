package org.jenkinsmvn.jenkins.api.model;

import java.net.URL;

/**
 * Jenkins task
 */
public class Task extends BaseModel {
    
    private String name;
    
    private URL url;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public URL getUrl() {
        return url;
    }

    public void setUrl(URL url) {
        this.url = url;
    }
}
