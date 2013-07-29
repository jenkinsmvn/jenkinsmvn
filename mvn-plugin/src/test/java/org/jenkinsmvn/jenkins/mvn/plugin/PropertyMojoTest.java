package org.jenkinsmvn.jenkins.mvn.plugin;

import org.apache.commons.collections.MapUtils;
import org.apache.maven.project.MavenProject;
import org.junit.Before;
import org.junit.Test;

import java.net.URL;
import java.util.ArrayList;
import java.util.Properties;

import static junit.framework.Assert.assertTrue;
import static org.mockito.Mockito.*;

public class PropertyMojoTest {

    public static final String CI_URL = "http://10.205.31.58:8080/";

    private PropertyMojo mojo;

    @Before
    public void setUp() throws Exception {
        mojo = new PropertyMojo();
        mojo.properties = new ArrayList<Property>();
        mojo.variables = new ArrayList<String>();
        mojo.project = mock(MavenProject.class);

        when(mojo.project.getProperties()).thenReturn(new Properties());

        mojo.jenkinsUrl = new URL(CI_URL);

    }

    @Test
    public void testExecuteNoChanges() throws Exception {
        mojo.properties.add(new Property("app", "test-job"));
        mojo.execute();

        assertTrue(MapUtils.isEmpty(mojo.project.getProperties()));
    }

    @Test
    public void testExecuteWithChanges() throws Exception {
        mojo.properties.add(new Property("app", "test-job"));
        mojo.project.getProperties().put("app", "5");

        mojo.execute();

        assertTrue(MapUtils.isNotEmpty(mojo.project.getProperties()));
    }

    @Test
    public void testExecuteWithChangesExpression() throws Exception {
        mojo.properties.add(new Property("app", "$[type eq 'trunk' ? 'test-job' : 'test-job-config']"));
        mojo.variables.add("type");
        mojo.project.getProperties().put("app", "latest");
        mojo.project.getProperties().put("type", "branch");

        mojo.execute();

        assertTrue(MapUtils.isNotEmpty(mojo.project.getProperties()));
    }
}
