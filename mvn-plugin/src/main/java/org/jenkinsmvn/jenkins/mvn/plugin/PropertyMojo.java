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

import org.jenkinsmvn.jenkins.api.model.JobDetails;
import de.odysseus.el.ExpressionFactoryImpl;
import de.odysseus.el.TreeValueExpression;
import de.odysseus.el.util.SimpleContext;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.*;
import org.apache.maven.project.MavenProject;

import javax.el.ExpressionFactory;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Project parameter mojo
 *
 */
@Mojo(name="property", requiresProject = true, defaultPhase = LifecyclePhase.COMPILE, requiresDependencyResolution = ResolutionScope.RUNTIME)
public class PropertyMojo extends AbstractJenkinsMojo {

    private static final Pattern EXPRESSION_PATTERN = Pattern.compile("\\$\\[(.*)\\]", Pattern.CASE_INSENSITIVE);

    /**
     * The Maven project.
     */
    @Component
    protected MavenProject project;

    /**
     * the list of properties
     */
    @Parameter(required = true)
    protected List<Property> properties;

    /**
     * the list of variables
     */
    @Parameter
    protected List<String> variables;

    private ExpressionFactory expressionFactory = new ExpressionFactoryImpl();

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        initClient();

        SimpleContext context = new SimpleContext();

        for(String variable : variables) {
            context.getELResolver().setValue(context, null, variable, project.getProperties().getProperty(variable));
        }

        try {
            Map<String, String> changes = new HashMap<String, String>();
            for(Property property : properties) {
                String propertyValue = project.getProperties().getProperty(property.getName());

                getLog().info(String.format("%s = %s", property.getName(), propertyValue));

                if(StringUtils.equalsIgnoreCase(propertyValue, "latest")) {
                    String name = property.getJobExpr();

                    Matcher matcher = EXPRESSION_PATTERN.matcher(name);

                    if(matcher.find()) {
                        String content = matcher.group(1);

                        TreeValueExpression expr = (TreeValueExpression) expressionFactory.createValueExpression(context, String.format("${%s}", content), String.class);
                        name = (String) expr.getValue(context);
                    }


                    JobDetails jobDetails = client.getJobDetails(name);

                    changes.put(property.getName() + "-modified", String.valueOf(jobDetails.getLastSuccessfulBuild().getNumber()));
                } else {
                    changes.put(String.format("%s-modified", property.getName()), propertyValue);
                }
            }

            if(MapUtils.isNotEmpty(changes)) {
                getLog().info(String.format("Property changes: %s", changes));

                for(Map.Entry<String, String> entry : changes.entrySet()) {
                    if(entry.getValue() == null) {
                        continue;
                    }

                    project.getProperties().setProperty(entry.getKey(), entry.getValue());
                }
            }
        } catch(Exception e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }
    }
}
