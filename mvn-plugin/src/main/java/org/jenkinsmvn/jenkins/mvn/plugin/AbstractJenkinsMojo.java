package org.jenkinsmvn.jenkins.mvn.plugin;

import org.jenkinsmvn.jenkins.api.JenkinsClient;
import org.jenkinsmvn.jenkins.api.JenkinsClientFactory;
import org.apache.commons.lang.StringUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.IOException;
import java.net.URL;

public abstract class AbstractJenkinsMojo extends AbstractMojo {
    /**
     * Determines the jenkins url
     *
     */
    @Parameter
    protected URL jenkinsUrl;

    /**
     * Determines the jenkins context path
     *
     */
    @Parameter
    protected String jenkinsContextPath;

    /**
     * Determines the jenkins username
     */
    @Parameter
    protected String userName;

    /**
     * Determines the jenkins password
     */
    @Parameter
    protected String password;

    /**
     * The jenkins client
     */
    @Parameter
    protected JenkinsClient client;

    /**
     * Determines the number of parallel threads
     */
    @Parameter
    protected int threads = 2;


    protected void initClient() throws MojoExecutionException {
        try {
            client = JenkinsClientFactory.create(jenkinsUrl.toString(), jenkinsContextPath, threads, threads);

            if(StringUtils.isNotBlank(userName) && StringUtils.isNotBlank(password)) {
                client.authenticate(userName, password);
            }
        } catch (IOException e) {
            throw new MojoExecutionException("Error creating jenkins client.", e);
        }
    }
}
