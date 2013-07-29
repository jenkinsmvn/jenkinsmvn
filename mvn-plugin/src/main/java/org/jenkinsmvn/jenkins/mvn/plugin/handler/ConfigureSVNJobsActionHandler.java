package org.jenkinsmvn.jenkins.mvn.plugin.handler;

import org.jenkinsmvn.jenkins.api.JenkinsClient;
import org.jenkinsmvn.jenkins.api.model.*;
import org.apache.commons.lang.StringUtils;
import org.jenkinsmvn.jenkins.api.model.*;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;

import static org.jenkinsmvn.jenkins.mvn.plugin.PropertyConstants.TAG_IF_NO_BUILD;


/**
 * Configure svn job path
 */
public class ConfigureSVNJobsActionHandler extends AbstractForEachJobActionHandler {

    public static final String TYPE = "configure-svn";

    public ConfigureSVNJobsActionHandler() {
        super(TYPE);
    }

    @Override
    protected boolean isOnNoJobsContinue() {
        return false;
    }

    @Override
    protected void doOnJob(Job job) throws IOException, TransformerException, ParserConfigurationException {
        ConfigDocument config = client.getJobConfig(job.getName());

        String currentSVNPath = config.getSVNPath();

        if(currentSVNPath == null) {
            logInfo(String.format("SVN NOT configured since NO SVN path."));

            return;
        }

        String replacement = HandlerUtils.INSTANCE.getReplacement(currentSVNPath, action);

        if(replacement.equals(currentSVNPath)) {
            logInfo(String.format("SVN NOT configured since same SVN path: '%s'.", currentSVNPath));

            if(HandlerUtils.INSTANCE.isPropertyExist(action, TAG_IF_NO_BUILD)) {

                JobDetails jobDetails = client.getJobDetails(job.getName(), false);
                if(!jobDetails.getBuildable()) {
                    logInfo(String.format("Tagging job since job is disabled."));
                    addJobResult(job.getName());

                    return;
                }

                Build build = jobDetails.getLastBuild();

                BuildDetails details = client.getBuildDetails(build);
                if(details.getResult().equals(JenkinsClient.RESULT_FAILURE)) {
                    logInfo(String.format("Tagging job since last build is '%s'.", JenkinsClient.RESULT_FAILURE));
                    addJobResult(job.getName());

                    return;
                }
            }

            return;
        }

        config.setSVNPath(replacement);

        client.saveConfig(job.getName(), config);
        addJobResult(job.getName());

        String indent = StringUtils.repeat(" ", 10);
        logInfo(String.format("SVN configured \n%sfrom '%s' \n%sto '%s'", indent, currentSVNPath, indent, replacement));
    }
}
