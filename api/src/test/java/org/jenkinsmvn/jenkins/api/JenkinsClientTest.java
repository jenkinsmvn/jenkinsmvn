package org.jenkinsmvn.jenkins.api;

import org.jenkinsmvn.jenkins.api.model.BuildDetails;
import org.jenkinsmvn.jenkins.api.model.ConfigDocument;
import org.jenkinsmvn.jenkins.api.model.Job;
import org.jenkinsmvn.jenkins.api.model.JobDetails;
import org.junit.Ignore;
import org.junit.Test;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static junit.framework.Assert.*;

/**
 * Test for {@link JenkinsClientTest} class.
 */
@Ignore
public class JenkinsClientTest {

    public static final int MAX_FETCH_JOB_DETAILS_COUNT = 5;
    
    public static final String DISABLE_ENABLE_JOB_NAME = "test-job";

    public static final String CONFIG_TEST_JOB_NAME = "test-job-config";
    
    public static final String ALT_SVN_PATH_1 = "http://svnpath/test-project-for-auto-branching-1";

    public static final String ALT_SVN_PATH_2 = "http://svnpath/test-project-for-auto-branching-2";

    public static final String CI_URL = "http://localhost:8080/";

    @Test
    public void testJobDetailsAndBuildDetails() throws Exception {
        JenkinsClient client = JenkinsClientFactory.create(CI_URL);

        assertNotNull(client.getNode());
        
        Iterator<String> itr = client.getJobNames().iterator();
        for(int i = 0; i < MAX_FETCH_JOB_DETAILS_COUNT && itr.hasNext(); i++) {
            String name = itr.next();

            try {
                JobDetails details = client.getJobDetails(name);

                System.out.println(name);
                testJobDetails(client, details);
            } catch(Exception e) {
                Job job = client.getJob(name);

                throw new IllegalArgumentException(String.format("Error while parsing job '%s'.", String.valueOf(job)), e);
            }
        }
    }

    @Test
    // TODO - what is job details with upstream
    public void testJobDetailsWithUpstream() throws Exception {
        JenkinsClient client = JenkinsClientFactory.create(CI_URL);

        assertNotNull(client.getNode());

        JobDetails details = client.getJobDetails("test-project-branching", true);

        System.out.println(details.getUpstreamProjectNames());
    }
    
    @Test
    public void testBuild() throws IOException, InterruptedException {
        JenkinsClient client = JenkinsClientFactory.create(CI_URL);

        if(!client.build(DISABLE_ENABLE_JOB_NAME, true)) {
            System.out.println("unable to build maybe disabled or currently building.");
            client.enable(DISABLE_ENABLE_JOB_NAME);
            
            JobDetails details = client.getJobDetails(DISABLE_ENABLE_JOB_NAME, false);
            assertTrue("should be buildable.", details.getBuildable());
        }
        
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("sample", "value");

        assertTrue("unable to build.", client.build(DISABLE_ENABLE_JOB_NAME, parameters));

        client.waitTillAllBuildsDone(DISABLE_ENABLE_JOB_NAME);
        client.disable(DISABLE_ENABLE_JOB_NAME);
    }

    @Test
    public void testSaveConfig() throws IOException, TransformerException, ParserConfigurationException {
        JenkinsClient client = JenkinsClientFactory.create(CI_URL);

        ConfigDocument config = client.getJobConfig(DISABLE_ENABLE_JOB_NAME);
        
        String replacedSVNValue = ALT_SVN_PATH_2;
        if(!config.getSVNPath().equals(ALT_SVN_PATH_1)) {
            replacedSVNValue = ALT_SVN_PATH_1;
        }
        
        config.setSVNPath(replacedSVNValue);

        assertTrue(client.saveConfig(DISABLE_ENABLE_JOB_NAME, config));

        config = client.getJobConfig(DISABLE_ENABLE_JOB_NAME);
        assertEquals(replacedSVNValue, config.getSVNPath());
    }

    @Test
    public void testDisableAndEnable() throws Exception {
        JenkinsClient client = JenkinsClientFactory.create(CI_URL);

        JobDetails details = client.getJobDetails(DISABLE_ENABLE_JOB_NAME);

        assertFalse(details.getBuildable());
        assertTrue(client.enable(DISABLE_ENABLE_JOB_NAME));

        // retrieve a fresh details
        details = client.getJobDetails(DISABLE_ENABLE_JOB_NAME, false);

        assertTrue(details.getBuildable());
        assertTrue(client.disable(DISABLE_ENABLE_JOB_NAME));

        details = client.getJobDetails(DISABLE_ENABLE_JOB_NAME, false);
        assertFalse(details.getBuildable());
    }

    @Test
    public void testConfigDocument() throws Exception {
        JenkinsClient client = JenkinsClientFactory.create(CI_URL);
        
        ConfigDocument config = client.getJobConfig(CONFIG_TEST_JOB_NAME);
        
        assertNotNull(config.getStringDefaultParameterValue(1));
        assertNotNull(config.getStringDefaultParameterValue(2));
        assertNotNull(config.getStringDefaultParameterValue(3));
    }

    private void testJobDetails(JenkinsClient client, JobDetails details) throws IOException, TransformerException, ParserConfigurationException {
        assertNotNull(details);

        if(details.getLastBuild() != null) {
            BuildDetails buildDetails = client.getBuildDetails(details.getName(), details.getLastBuild().getNumber());
            assertNotNull(buildDetails);
        }

        ConfigDocument document = client.getJobConfig(details.getName());
        testConfigWrapper(document);
    }

    private void testConfigWrapper(ConfigDocument document) throws TransformerException, ParserConfigurationException {
        assertNotNull(document);
        //assertNotNull(document.getSVNPath());

        String newSVNPath = document.getSVNPath() + "/testing";
        document.setSVNPath(newSVNPath);
        String xml = document.toXMLString();
        
        assertTrue(xml.contains(newSVNPath));
    }
}
