package org.jenkinsmvn.jenkins.api.model;

/**
 * Determines a jenkins job detail modules
 */
public class Module extends BaseModel {
    private String name;
    
    private String url;
    
    private String color;
    
    private String displayName;

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

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
