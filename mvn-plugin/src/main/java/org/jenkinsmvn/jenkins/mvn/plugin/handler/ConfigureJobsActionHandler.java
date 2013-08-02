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

package org.jenkinsmvn.jenkins.mvn.plugin.handler;

import org.jenkinsmvn.jenkins.api.model.ConfigDocument;
import org.jenkinsmvn.jenkins.api.model.Job;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;

import static org.jenkinsmvn.jenkins.mvn.plugin.PropertyConstants.IGNORE_NOT_FOUND_X_PATH;

/**
 * Configure a job
 */
public class ConfigureJobsActionHandler extends AbstractForEachJobActionHandler {

    public static final String TYPE = "configure";

    public ConfigureJobsActionHandler() {
        super(TYPE);
    }

    @Override
    protected void doOnJob(Job job) throws IOException, InterruptedException, TransformerException, ParserConfigurationException {
        Validate.notNull(action.getProperties(), "No action properties configured.");
        Validate.notEmpty(action.getProperties(), "No action properties configured.");

        ConfigDocument config = client.getJobConfig(job.getName());

        boolean ignore = false;

        if(HandlerUtils.INSTANCE.isPropertyExist(action, IGNORE_NOT_FOUND_X_PATH) &&
                HandlerUtils.INSTANCE.getRequiredBoolean(action, IGNORE_NOT_FOUND_X_PATH)) {
            ignore = true;
        }

        Properties props = action.getProperties();

        String indent = StringUtils.repeat(" ", 10);
        for(Map.Entry<Object, Object> entry : props.entrySet()) {
            String expression = (String) entry.getKey();

            if(IGNORE_NOT_FOUND_X_PATH.equals(expression)) {
                continue;
            }

            String newValue = (String) entry.getValue();
            String oldValue = config.getElementTextContent(expression);

            if(!newValue.equals(oldValue)) {
                config.setElementTextContent(expression, newValue, ignore);
                logInfo(String.format("Configured \n%sexpression '%s'\n%sfrom '%s'\n%sto '%s'", indent, expression, indent, oldValue, indent, newValue));
            }
        }

        if(config.isModified()) {
            client.saveConfig(job.getName(), config);
            logInfo("Configuration changes saved.");
        } else {
            logInfo("No configuration changes.");
        }
    }
}
