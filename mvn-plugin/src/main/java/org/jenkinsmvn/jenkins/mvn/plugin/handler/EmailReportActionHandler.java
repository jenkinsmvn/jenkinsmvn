package org.jenkinsmvn.jenkins.mvn.plugin.handler;

import org.jenkinsmvn.jenkins.api.model.BuildDetails;
import org.jenkinsmvn.jenkins.api.model.ConfigDocument;
import org.jenkinsmvn.jenkins.api.model.Job;
import org.jenkinsmvn.jenkins.api.model.JobDetails;
import org.jenkinsmvn.jenkins.mvn.plugin.PropertyConstants;
import org.jenkinsmvn.jenkins.mvn.plugin.URLParser;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Generate report action handler.
 */
public class EmailReportActionHandler extends AbstractForEachJobActionHandler {

    public static final String TYPE = "generate-email";

    public static final Pattern VARIABLE = Pattern.compile("@[a-z0-9]+", Pattern.CASE_INSENSITIVE);

    private StringWriter stringWriter;

    private PrintWriter writer;

    private File templateFile;

    private String tableClass = "table";

    public EmailReportActionHandler() {
        super(TYPE);
    }

    @Override
    public void execute() throws IOException {
        FileWriter reportWriter = null;
        try {
            super.execute();
            printFooter();

            String content = stringWriter.toString();
            StringBuilder email = new StringBuilder(FileUtils.readFileToString(templateFile));

            Matcher matcher = VARIABLE.matcher(email);

            int index = 0;
            while(matcher.find(index)) {
                String variable = matcher.group().substring(1);

                if(StringUtils.equals(variable, "content")) {
                    email.replace(matcher.start(), matcher.end(), content);
                    index = matcher.start() + content.length();
                } else if(action.getProperties().containsKey(variable)) {
                    String replacement = action.getProperties().getProperty(variable);
                    email.replace(matcher.start(), matcher.end(), replacement);
                    index = matcher.start() + replacement.length();
                } else {
                    index = matcher.end();
                }
            }

            File baseReportFile = new File(jenkinsTargetDir, "email.html");

            reportWriter = new FileWriter(baseReportFile);
            IOUtils.write(email, reportWriter);
        } finally {
            IOUtils.closeQuietly(reportWriter);
            IOUtils.closeQuietly(stringWriter);
            IOUtils.closeQuietly(writer);
        }
    }

    @Override
    protected void initRelevantJobs(List<String> jobNames) throws IOException {
        templateFile = new File(HandlerUtils.INSTANCE.getRequiredProperty(action, PropertyConstants.EMAIL_TEMPLATE));

        if(HandlerUtils.INSTANCE.isPropertyExist(action, PropertyConstants.TABLE_CLASS)) {
            tableClass = HandlerUtils.INSTANCE.getRequiredProperty(action, PropertyConstants.TABLE_CLASS);
        }

        if(!templateFile.isFile()) {
            throw new IllegalArgumentException("Template file is not a valid file.");
        }

        stringWriter = new StringWriter();

        logInfo(String.format("Creating email report..."));
        writer = new PrintWriter(stringWriter, true);
        printHeader();
    }

    private void printFooter() {
        writer.println("</tbody>");
        writer.println("</table>");
    }

    private void printHeader() {
        writer.println(String.format("<table class=\"%s\">", tableClass));
        writer.println("<thead>");
        writer.println("<tr>");
        writer.println("<th>Project</th><th>Build Version</th><th>SVN Link</th>");
        writer.println("</tr>");
        writer.println("</thead>");
        writer.println("<tbody>");
    }

    private void writeRow(ConfigDocument config, JobDetails details, BuildDetails buildDetails) throws IOException, TransformerException {
        writer.println("<tr>");
        writer.println(
            String.format(
                "<td>%s</td><td><a href=\"%s\">%d</a></td><td>%s</td>",
                details.getName(),
                String.valueOf(buildDetails.getUrl()),
                buildDetails.getNumber(),
                URLParser.parseUrls(config.getSVNPath()).iterator().next())
        );

        writer.println("</tr>");
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
        writeRow(config, details, buildDetails);
    }
}
