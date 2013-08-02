/*
 * Copyright (c) 2013. Jenkinsmvn. All Rights Reserved.
 *
 * See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The Jenkinsmvn licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jenkinsmvn.jenkins.mvn.plugin;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.*;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Run a simple jenkins script
 *
 */
@Mojo(name="execute", defaultPhase = LifecyclePhase.NONE, requiresDependencyResolution = ResolutionScope.NONE)
public class ScriptRunnerMojo extends AbstractJenkinsMojo {

    /**
     * The job list references
     */
    @Parameter
    private List<Reference> references;

    /**
     * The list of actions
     */
    @Parameter
    private List<Action> actions;

    /**
     * The target Dir
     */
    @Parameter(defaultValue = "${project.build.directory}")
    private File targetDir;

    /**
     * Mapping for random access
     */
    private Map<String, Reference> mapping = new HashMap<String, Reference>();

    /**
     * Start executing all actions.
     *
     * @throws MojoExecutionException
     */
    @Override
    public void execute() throws MojoExecutionException {
        if(CollectionUtils.isEmpty(actions)) {
            throw new MojoExecutionException("No actions specified.");
        }

        // initialize mapping for random access
        // and jenkins client
        initMapping();
        initClient();

        // only initialize
        BuildJobExecutorService.INSTANCE.init(threads);

        getLog().info(String.format("Jenkins url: %s", jenkinsUrl.toString()));
        
        // start iterating actions
        try {
            getLog().info(String.format("Starting to execute %d action%s.", actions.size(), actions.size() > 1 ? "s" : ""));

            for(int i = 0; i < actions.size(); i++) {
                Action action = actions.get(i);

                if(action.getName() == null) {
                    action.setName(String.format("%s{%d}",  action.getType(), i));
                }

                action.setLogPrefix(String.format("[%d of %d][%s]", i + 1, actions.size(), action.getName()));

                long start = System.currentTimeMillis();
                ActionExecutor.execute(this, action, client, mapping);

                getLog().info(String.format("(%s) Completed.", prettyPrintElapse(System.currentTimeMillis() - start)));
            }

            getLog().info("Finished.");
        } catch(Exception e) {
            throw new MojoExecutionException(e.getMessage(), e);
        } finally {
            BuildJobExecutorService.INSTANCE.shutdown();
        }
    }

    private String prettyPrintElapse(long time) {
        long days = 0;
        long hours = 0;
        long minutes = 0;
        long seconds = 0;

        if(time > 0) {
            days = time / (1000 * 60 * 60 * 24);
            time -= days * (1000 * 60 * 60 * 24);
        }
        if(time > 0) {
            hours = time / (1000 * 60 * 60);
            time -= hours * (1000 * 60 * 60);
        }
        if(time > 0) {
            minutes = time / (1000 * 60);
            time -= minutes * (1000 * 60);
        }
        if(time > 0) {
            seconds = time / 1000;
            time -= seconds * 1000;
        }

        StringBuilder buf = new StringBuilder();

        if(days > 0)    buf.append(days).append("d");
        if(hours > 0)   buf.append(buf.length() > 0 ? " " : "").append(hours).append("h");
        if(minutes > 0) buf.append(buf.length() > 0 ? " " : "").append(minutes).append("m");
        if(seconds > 0) buf.append(buf.length() > 0 ? " " : "").append(seconds).append("s");
        if(time > 0)    buf.append(buf.length() > 0 ? " " : "").append(time).append("ms");

        if(buf.length() <= 0) {
            buf.append("0ms");
        }

        return buf.toString();
    }

    private void initMapping() throws MojoExecutionException {
        if(CollectionUtils.isEmpty(references)) {
            return;
        }
        
        for(Reference ref : references) {
            populateReferenceJobNames(ref);

            mapping.put(StringUtils.trim(ref.getId()), ref);
        }
    }

    private void populateReferenceJobNames(Reference ref) throws MojoExecutionException {
        if(ref.getRefId() != null) {
            List<String> jobs = new ArrayList<String>();

            if(!mapping.containsKey(StringUtils.trim(ref.getRefId()))) {
                throw new MojoExecutionException(String.format("Reference with id '%s', no refid with '%s'", ref.getId(), ref.getRefId()));
            }

            for(String jobName : mapping.get(StringUtils.trim(ref.getRefId())).getJobs()) {
                String refJobName = jobName;

                if(ref.getPrefix() != null) {
                    refJobName = String.format("%s%s", StringUtils.trim(ref.getPrefix()), refJobName);
                }

                if(ref.getSuffix() != null) {
                    refJobName = String.format("%s%s", refJobName, StringUtils.trim(ref.getSuffix()));
                }

                jobs.add(refJobName);
            }

            ref.setJobs(jobs);
        }
    }

    public void setActions(List<Action> actions) {
        this.actions = actions;
    }

    public void setJenkinsUrl(URL jenkinsUrl) {
        this.jenkinsUrl = jenkinsUrl;
    }

    public void setReferences(List<Reference> references) {
        this.references = references;
    }

    public Map<String, Reference> getMapping() {
        return mapping;
    }

    public File getTargetDir() {
        return targetDir;
    }

    public void setTargetDir(File targetDir) {
        this.targetDir = targetDir;
    }

    public void setThreads(int threads) {
        this.threads = threads;
    }
}
