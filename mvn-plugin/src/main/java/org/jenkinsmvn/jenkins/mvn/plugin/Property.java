package org.jenkinsmvn.jenkins.mvn.plugin;

public class Property {
    private String name;

    private String jobExpr;

    public Property() {
    }

    public Property(String name, String jobExpr) {
        this.name = name;
        this.jobExpr = jobExpr;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getJobExpr() {
        return jobExpr;
    }

    public void setJobExpr(String jobExpr) {
        this.jobExpr = jobExpr;
    }
}
