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

import org.apache.commons.collections.MapUtils;
import org.apache.maven.project.MavenProject;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.net.URL;
import java.util.ArrayList;
import java.util.Properties;

import static junit.framework.Assert.assertTrue;
import static org.mockito.Mockito.*;

@Ignore
public class PropertyMojoTest {

    public static final String CI_URL = "http://localhost:8080/";

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
