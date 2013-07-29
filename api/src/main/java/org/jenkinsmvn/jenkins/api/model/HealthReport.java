package org.jenkinsmvn.jenkins.api.model;

/**
 * Defines a jenkins job details health report
 */
public class HealthReport extends BaseModel {

    private String description;
    
    private String iconUrl;
    
    private Integer score;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }
}
