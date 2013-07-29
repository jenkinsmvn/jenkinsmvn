package org.jenkinsmvn.jenkins.api.model;

import java.net.URL;

/**
 * Determines a jenkins job detail build.
 */
public class Build extends BaseModel {

    private Integer number;
    
    private URL url;

    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }

    public URL getUrl() {
        return url;
    }

    public void setUrl(URL url) {
        this.url = url;
    }
}
