package org.jenkinsmvn.jenkins.api.model;

import java.net.URL;

/**
 * Defines a jenkins job
 */
public class Job extends BaseModel {
    
    private String name;
    
    private URL url;
    
    private String color;

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

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
