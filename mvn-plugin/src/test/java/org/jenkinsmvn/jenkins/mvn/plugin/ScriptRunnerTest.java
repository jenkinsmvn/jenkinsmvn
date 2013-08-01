package org.jenkinsmvn.jenkins.mvn.plugin;

import org.jenkinsmvn.jenkins.api.JenkinsClient;
import org.jenkinsmvn.jenkins.api.JenkinsClientFactory;
import org.jenkinsmvn.jenkins.api.model.ConfigDocument;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.jenkinsmvn.jenkins.mvn.plugin.handler.*;
import org.junit.Ignore;
import org.junit.Test;

import javax.xml.transform.TransformerException;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import static org.jenkinsmvn.jenkins.mvn.plugin.PropertyConstants.*;
import static junit.framework.Assert.*;

/**
 * Test for {@link org.jenkinsmvn.jenkins.mvn.plugin.ScriptRunnerMojo} class.
 */
@Ignore
public class ScriptRunnerTest {

    public static final String DISABLE_ENABLE_JOB_NAME = "test-job";

    public static final String CI_URL = "http://localhost:8080/";

    public static final String ALT_SVN_PATH_1 = "http://svnpath/test-project-for-auto-branching-1/";

    public static final String ALT_SVN_PATH_2 = "http://svnpath/test-project-for-auto-branching-2/";

    public static final List<String> JOBS = Arrays.asList(
            "job1",
            "job2",
            "job3"
    );

    private ScriptRunnerMojo createScript() throws MalformedURLException {
        ScriptRunnerMojo scriptRunner = new ScriptRunnerMojo();

        scriptRunner.setJenkinsUrl(new URL(CI_URL));

        return scriptRunner;
    }
    
    @Test(expected = MojoExecutionException.class)
    public void testNoAction() throws Exception {
        ScriptRunnerMojo scriptRunner = new ScriptRunnerMojo();
        scriptRunner.execute();
    }

    @Test
    public void testVerify() throws Exception {
        Action verify = new Action();

        verify.setType(VerifyJobsActionHandler.TYPE);
        verify.setJobName(DISABLE_ENABLE_JOB_NAME);

        ScriptRunnerMojo scriptRunner = createScript();

        scriptRunner.setActions(Arrays.asList(verify));
        
        try {
            scriptRunner.execute();
        } catch(MojoExecutionException e) {
            e.printStackTrace();
            fail("Should have no exceptions.");
        }
    }

    @Test(expected = MojoExecutionException.class)
    public void testJobNotFound() throws Exception {
        Action verify = new Action();

        verify.setType(VerifyJobsActionHandler.TYPE);
        verify.setJobName("notfound");

        ScriptRunnerMojo scriptRunner = createScript();

        scriptRunner.setActions(Arrays.asList(verify));
        scriptRunner.execute();
    }

    @Test
    public void testEnableDisableAction() throws Exception {
        Action disable = new Action();
        
        disable.setJobName(DISABLE_ENABLE_JOB_NAME);
        disable.setType(DisableJobsActionHandler.TYPE);
        
        Action enable = new Action();

        enable.setJobName(DISABLE_ENABLE_JOB_NAME);
        enable.setType(EnableJobsActionHandler.TYPE);

        ScriptRunnerMojo scriptRunner = createScript();

        scriptRunner.setActions(Arrays.asList(enable, disable));
        scriptRunner.execute();

        JenkinsClient client = JenkinsClientFactory.create(CI_URL);

        assertFalse("Should be disabled.", client.getJobDetails(DISABLE_ENABLE_JOB_NAME).getBuildable());
    }

    @Test(expected = MojoExecutionException.class)
    public void testFailedConfigureSVNAndBuildAction() throws Exception {        
        Action configure = new Action();

        configure.setJobName(DISABLE_ENABLE_JOB_NAME);
        configure.setType(ConfigureSVNJobsActionHandler.TYPE);

        ScriptRunnerMojo scriptRunner = createScript();

        scriptRunner.setActions(Arrays.asList(configure));
        scriptRunner.execute();
    }
    
    public String getReplacement(ConfigDocument config) throws IOException, TransformerException {
        String replacedSVNValue = ALT_SVN_PATH_2;
        if(!config.getSVNPath().equals(ALT_SVN_PATH_1)) {
            replacedSVNValue = ALT_SVN_PATH_1;
        }

        return replacedSVNValue;
    }

    @Test
    public void testConfigureSVNAndBuildAction() throws Exception {
        JenkinsClient client = JenkinsClientFactory.create(CI_URL);

        ConfigDocument config = client.getJobConfig(DISABLE_ENABLE_JOB_NAME);

        String oldValue = config.getSVNPath();
        String replacement = getReplacement(config);
        
        Action configure = new Action();

        configure.setJobName(DISABLE_ENABLE_JOB_NAME);
        configure.setType(ConfigureSVNJobsActionHandler.TYPE);

        Properties props = new Properties();
        props.setProperty(REPLACEMENT, replacement);

        configure.setProperties(props);
        
        Action filter = new Action();
        filter.setName("filter");
        filter.setJobListRef("configure-svn{0}.result");
        filter.setType(FilterJobsActionHandler.TYPE);

        props = new Properties();
        props.setProperty(FILTERS, StringUtils.join(Arrays.asList(FILTER_NOT_BUILDABLE), ","));

        filter.setProperties(props);

        Action build = new Action();
        build.setType(BuildJobsActionHandler.TYPE);
        build.setJobListRef("filter.result");
        props = new Properties();
        props.setProperty(WAIT_TILL_DONE, "true");
        props.setProperty(ENABLE_AND_BUILD, "true");
        props.setProperty(PARAMETER_PREFIX + "sample", "example_value");

        build.setProperties(props);

        Action disable = new Action();

        disable.setJobName(DISABLE_ENABLE_JOB_NAME);
        disable.setType(DisableJobsActionHandler.TYPE);

        ScriptRunnerMojo scriptRunner = createScript();

        scriptRunner.setActions(Arrays.asList(configure, filter, build, disable));
        scriptRunner.execute();

        config = client.getJobConfig(DISABLE_ENABLE_JOB_NAME);
        String newValue = config.getSVNPath();
        
        assertEquals("Replacement value.", newValue, replacement);
        assertFalse("Should not be equal.", newValue.equals(oldValue));
    }

    @Test
    public void testDependencyBuild() throws Exception {
        Action build = new Action();

        build.setType(TestUpstreamDependencyBuildJobActionHandler.TYPE);
        build.setJobNames(JOBS);

        ScriptRunnerMojo scriptRunner = createScript();
        scriptRunner.setThreads(5);
        scriptRunner.setActions(Arrays.asList(build));

        scriptRunner.execute();
    }

    @Test
    public void buildReferences() throws Exception {
        String references = new ReferenceDependenciesBuilder(JenkinsClientFactory.create(CI_URL)).buildDependenciesReference(JOBS);

        System.out.println(references);
    }

    @Test(expected = MojoExecutionException.class)
    public void testFailedVerifyHttp() throws Exception {
        Action verifyHttp = new Action();

        verifyHttp.setName("verifyURL");
        verifyHttp.setType(VerifyHttpURLActionHandler.TYPE);
        verifyHttp.setJobName(DISABLE_ENABLE_JOB_NAME);

        Properties props = new Properties();
        props.setProperty(URL_TO_VERIFY, "http://svnpath/notfound");
        props.setProperty(FAIL_ON_URL_UNVERIFIED, "true");
        props.setProperty(HTTP_AUTHORIZATION, "Basic YWRlbGVvbjp1Y244cERhag==");

        verifyHttp.setProperties(props);

        ScriptRunnerMojo scriptRunner = createScript();

        scriptRunner.setActions(Arrays.asList(verifyHttp));
        scriptRunner.execute();
    }

    @Test
    public void testSuccessVerifyHttp() throws Exception {
        Action verifyHttp = new Action();

        verifyHttp.setName("verifyURL");
        verifyHttp.setType(VerifyHttpURLActionHandler.TYPE);
        verifyHttp.setJobName(DISABLE_ENABLE_JOB_NAME);

        Properties props = new Properties();
        props.setProperty(URL_TO_VERIFY, "http://svnpath/");
        props.setProperty(FAIL_ON_URL_UNVERIFIED, "true");
        props.setProperty(HTTP_AUTHORIZATION, "Basic YWRlbGVvbjp1Y244cERhag==");

        verifyHttp.setProperties(props);

        Action enable = new Action();

        enable.setJobListRef("verifyURL.result");
        enable.setType(EnableJobsActionHandler.TYPE);

        Action disable = new Action();

        disable.setJobListRef("verifyURL.result");
        disable.setType(DisableJobsActionHandler.TYPE);

        ScriptRunnerMojo scriptRunner = createScript();

        scriptRunner.setActions(Arrays.asList(verifyHttp, enable, disable));
        scriptRunner.execute();

        // asserts
        Reference reference = scriptRunner.getMapping().get("verifyURL.result");
        assertNotNull("Jobs reference should not be null", reference);
        assertTrue("verifyURL.result mapping should be created.", scriptRunner.getMapping().containsKey("verifyURL.result"));
    }

    @Test
    public void testVerifyBuild() throws Exception {
        Action verifyBuild = new Action();

        verifyBuild.setName("verifyBuild");
        verifyBuild.setType(VerifyBuildActionHandler.TYPE);
        verifyBuild.setJobName(DISABLE_ENABLE_JOB_NAME);

        ScriptRunnerMojo scriptRunner = createScript();
        scriptRunner.setActions(Arrays.asList(verifyBuild));

        try {
            scriptRunner.execute();
        } catch(MojoExecutionException e) {
            fail("Should not fail since configured build is successful.");
        }
    }

    @Test
    public void testSuffixPrefix() throws Exception {
        Reference ref = new Reference();
        ref.setId("a");
        ref.setJobs(Arrays.asList("t"));
        
        Reference ref2 = new Reference();
        ref2.setId("b");
        ref2.setRefId("a");
        ref2.setPrefix("tes");
        ref2.setSuffix("job");
        
        Action verifyBuild = new Action();

        verifyBuild.setName("verifyBuild");
        verifyBuild.setType(VerifyBuildActionHandler.TYPE);
        verifyBuild.setJobListRef("b");

        ScriptRunnerMojo scriptRunner = createScript();
        scriptRunner.setReferences(Arrays.asList(ref, ref2));
        scriptRunner.setActions(Arrays.asList(verifyBuild));
        scriptRunner.execute();

        // asserts
        Reference reference = scriptRunner.getMapping().get("b");
        assertTrue("jobs should not be empty", CollectionUtils.isNotEmpty(reference.getJobs()));
        assertEquals("Job name should be 'testjob'.", reference.getJobs().iterator().next(), DISABLE_ENABLE_JOB_NAME);
    }

    @Test
    public void testGenerateReport() throws Exception {
        Action report = new Action();

        report.setType(GenerateReportActionHandler.TYPE);
        report.setJobName(DISABLE_ENABLE_JOB_NAME);

        Properties props = new Properties();

        props.setProperty(REPORT_HEADER, "Test Header");
        report.setProperties(props);
        
        File targetDir = new File(System.getProperty("java.io.tmpdir"));

        ScriptRunnerMojo scriptRunner = createScript();
        scriptRunner.setTargetDir(targetDir);
        scriptRunner.setActions(Arrays.asList(report));
        scriptRunner.execute();

        File jenkins = new File(targetDir, "jenkins");
        
        assertTrue(jenkins.isDirectory());
        
        File reportFile = new File(jenkins, GenerateReportActionHandler.CONFLUENCE_REPORT_TYPE + "-report.txt");
        assertTrue(reportFile.isFile());
        
        String content = IOUtils.toString(new FileReader(reportFile));
        
        assertTrue(content.contains("Test Header"));
        assertTrue(content.contains(DISABLE_ENABLE_JOB_NAME));
    }
}
