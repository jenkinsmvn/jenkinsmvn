package org.jenkinsmvn.jenkins.mvn.plugin.handler;

import org.jenkinsmvn.jenkins.api.model.BuildDetails;
import org.jenkinsmvn.jenkins.api.model.ConfigDocument;
import org.jenkinsmvn.jenkins.api.model.Job;
import org.jenkinsmvn.jenkins.api.model.JobDetails;
import org.apache.commons.io.IOUtils;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.List;

import static org.jenkinsmvn.jenkins.mvn.plugin.PropertyConstants.REPORT_HEADER;
import static org.jenkinsmvn.jenkins.mvn.plugin.PropertyConstants.REPORT_TYPE;

/**
 * Generate report action handler.
 */
public class GenerateReportActionHandler extends AbstractForEachJobActionHandler {

    public static final String TYPE = "generate-report";
    
    public static final String CONFLUENCE_REPORT_TYPE = "confluence";
    
    public static final String WIKI_REPORT_TYPE = "wiki";

    private PrintWriter writer;

    private String type;
    
    private String header;

    public GenerateReportActionHandler() {
        super(TYPE);
    }

    @Override
    public void execute() throws IOException {
        try {
            super.execute();
        } finally {
            printFooter();
            IOUtils.closeQuietly(writer);
        }
    }

    @Override
    protected void initRelevantJobs(List<String> jobNames) throws IOException {
        header = HandlerUtils.INSTANCE.getRequiredProperty(action, REPORT_HEADER);
        type = CONFLUENCE_REPORT_TYPE;

        if(HandlerUtils.INSTANCE.isPropertyExist(action, REPORT_TYPE)) {
            type = HandlerUtils.INSTANCE.getRequiredProperty(action, REPORT_TYPE);

            if(!CONFLUENCE_REPORT_TYPE.equals(type) && !WIKI_REPORT_TYPE.equals(type)) {
                throw new IllegalArgumentException(String.format("Invalid report type '%s', only supports '%s' or '%s'", type, CONFLUENCE_REPORT_TYPE, WIKI_REPORT_TYPE));
            }
        }

        File baseReportFile = new File(jenkinsTargetDir, type + "-report.txt");
        
        logInfo(String.format("Creating report '%s' with file '%s'...", type, baseReportFile.toString()));
        writer = new PrintWriter(new FileWriter(baseReportFile), true);
        printHeader();
    }

    private void printFooter() {
        if(writer == null) {
            return;
        }

        if(WIKI_REPORT_TYPE.equals(type)) {
            writer.println("|}");
        }
    }

    private void printHeader() {
        writer.println(header);
        
        if(CONFLUENCE_REPORT_TYPE.equals(type)) {
            writer.println("|| Project || Build Version || SVN Link ||");
        } else {
            writer.println("{| border=\"1\"");
            writer.println("|-");
            writer.println("! Project");
            writer.println("! Build Version");
            writer.println("! SVN Link");
        }
    }

    private void writeConfluenceRow(ConfigDocument config, JobDetails details, BuildDetails buildDetails) throws IOException, TransformerException {
        writer.println(
                String.format(
                        "| %s | [%d|%s] (%s) | %s |",
                        details.getName(),
                        buildDetails.getNumber(),
                        String.valueOf(buildDetails.getUrl()),
                        new Date(buildDetails.getTimestamp()),
                        config.getSVNPath())
        );
    }
    
    private void writeWikiRow(ConfigDocument config, JobDetails details, BuildDetails buildDetails) throws TransformerException {
        writer.println("|-");
        writer.println(String.format("| %s", details.getName()));
        writer.println(String.format("| %d (%s)", buildDetails.getNumber(), new Date(buildDetails.getTimestamp())));
        writer.println(String.format("| %s ", config.getSVNPath()));
    }

    @Override
    protected void doOnJob(Job job) throws IOException, InterruptedException, TransformerException, ParserConfigurationException {
        JobDetails details = client.getJobDetails(job.getName(), false);

        if(details.getLastBuild() == null) {
            logInfo("Excluded since no last build.");

            return;
        }

        ConfigDocument config = client.getJobConfig(job.getName());

        logInfo("Adding as report row.");
        BuildDetails buildDetails = client.getBuildDetails(details.getLastBuild());
        if(CONFLUENCE_REPORT_TYPE.equals(type)) {
            writeConfluenceRow(config, details, buildDetails);
        } else {
            writeWikiRow(config, details, buildDetails);
        }
    }
}
