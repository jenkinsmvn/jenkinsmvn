package org.jenkinsmvn.jenkins.mvn.plugin.handler;

import org.jenkinsmvn.jenkins.api.model.JobDetails;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class UpstreamDependencyBuildJobActionHandler extends AbstractDependencyBuildJobActionHandler {

    public static final String TYPE = "upstream-dependency-build";

    public UpstreamDependencyBuildJobActionHandler() {
        super(TYPE);
    }

    public UpstreamDependencyBuildJobActionHandler(String type) {
        super(type);
    }

    protected void buildTree(List<String> jobNames) throws IOException {
        logInfo("Generating dependency tree...");
        for(String jobName : jobNames) {
            List<String> dependency = tree.get(jobName);

            if(dependency == null) {
                dependency = new LinkedList<String>();
                tree.put(jobName, dependency);
            }

            JobDetails details = client.getJobDetails(jobName);
            for(String upstream : details.getUpstreamProjectNames()) {
                if(jobNames.contains(upstream)) {
                    dependency.add(upstream);
                }
            }
        }

        StringBuilder buf = new StringBuilder("Generated Dependency Tree (upstream projects):");
        for(Map.Entry<String, List<String>> entry : tree.entrySet()) {
            buf.append("\n    ").append(entry.getKey()).append(": ").append(entry.getValue());
        }

        logInfo(buf.toString());
    }

}
