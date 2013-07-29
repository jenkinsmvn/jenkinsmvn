package org.jenkinsmvn.jenkins.mvn.plugin.handler;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class JobReferenceDependencyBuildJobActionHandler extends AbstractDependencyBuildJobActionHandler {

    public static final String TYPE = "reference-dependency-build";

    public JobReferenceDependencyBuildJobActionHandler() {
        super(TYPE);
    }

    public JobReferenceDependencyBuildJobActionHandler(String type) {
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

            List<String> jobReferences = getRefJobNames("dependencies." + jobName);
            for(String upstream : jobReferences) {
                if(jobNames.contains(upstream)) {
                    dependency.add(upstream);
                }
            }
        }

        StringBuilder buf = new StringBuilder("Generated Dependency Tree (references):");
        for(Map.Entry<String, List<String>> entry : tree.entrySet()) {
            buf.append("\n    ").append(entry.getKey()).append(": ").append(entry.getValue());
        }

        logInfo(buf.toString());
    }

}
