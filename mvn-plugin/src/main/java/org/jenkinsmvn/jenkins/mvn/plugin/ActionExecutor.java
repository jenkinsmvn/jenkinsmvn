package org.jenkinsmvn.jenkins.mvn.plugin;

import org.jenkinsmvn.jenkins.api.JenkinsClient;
import org.apache.commons.collections.CollectionUtils;
import org.jenkinsmvn.jenkins.mvn.plugin.handler.*;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Action handler registry
 */
public final class ActionExecutor {

    private static final Map<String, Class> HANDLERS = Collections.unmodifiableMap(
            new HashMap<String, Class>(10) {{
                put(VerifyJobsActionHandler.TYPE, VerifyJobsActionHandler.class);
                put(DisableJobsActionHandler.TYPE, DisableJobsActionHandler.class);
                put(EnableJobsActionHandler.TYPE, EnableJobsActionHandler.class);
                put(BuildJobsActionHandler.TYPE, BuildJobsActionHandler.class);
                put(ConfigureSVNJobsActionHandler.TYPE, ConfigureSVNJobsActionHandler.class);
                put(VerifyHttpURLActionHandler.TYPE, VerifyHttpURLActionHandler.class);
                put(VerifyBuildActionHandler.TYPE, VerifyBuildActionHandler.class);
                put(FilterJobsActionHandler.TYPE, FilterJobsActionHandler.class);
                put(GenerateReportActionHandler.TYPE, GenerateReportActionHandler.class);
                put(ConfigureJobsActionHandler.TYPE, ConfigureJobsActionHandler.class);
                put(EmailReportActionHandler.TYPE, EmailReportActionHandler.class);
                put(UpstreamDependencyBuildJobActionHandler.TYPE, UpstreamDependencyBuildJobActionHandler.class);
                put(JobReferenceDependencyBuildJobActionHandler.TYPE, JobReferenceDependencyBuildJobActionHandler.class);
                put(TestUpstreamDependencyBuildJobActionHandler.TYPE, TestUpstreamDependencyBuildJobActionHandler.class);
                put(TestJobReferenceDependencyBuildJobActionHandler.TYPE, TestJobReferenceDependencyBuildJobActionHandler.class);
            }}
    );

    @SuppressWarnings("unchecked")
    public static void execute(ScriptRunnerMojo owner, Action action, JenkinsClient client, Map<String, Reference> mapping) throws IllegalAccessException, InstantiationException, IOException {
        if(!HANDLERS.containsKey(action.getType())) {
            throw new IllegalArgumentException(String.format("No action handler for type '%s'.", action.getType()));
        }

        if(action.getJobListRef() == null && action.getJobName() == null && CollectionUtils.isEmpty(action.getJobListRefs()) &&
                CollectionUtils.isEmpty(action.getJobNames())) {
            throw new IllegalArgumentException(String.format("Action with name '%s' should have a jobListRef or a jobName.", action.getName()));
        }

        Class<? extends AbstractActionHandler> handlerClass = HANDLERS.get(action.getType());

        AbstractActionHandler handler = handlerClass.newInstance();
        handler.setClient(client);
        handler.setReferences(mapping);
        handler.setOwner(owner);
        handler.setTargetDir(owner.getTargetDir());
        handler.setAction(action);

        handler.execute();

        // register a new mapping
        if(CollectionUtils.isNotEmpty(handler.getResultingJobs())) {
            Reference reference = new Reference();
            reference.setId(action.getName() + ".result");
            reference.setJobs(handler.getResultingJobs());

            mapping.put(reference.getId(), reference);
        }
    }
}
