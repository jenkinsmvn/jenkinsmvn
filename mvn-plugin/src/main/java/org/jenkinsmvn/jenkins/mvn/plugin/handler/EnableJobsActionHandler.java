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

import org.jenkinsmvn.jenkins.api.model.Job;

import java.io.IOException;

/**
 * Start disabling the job.
 */
public class EnableJobsActionHandler extends AbstractForEachJobActionHandler {

    public static final String TYPE = "enable";

    public EnableJobsActionHandler() {
        super(TYPE, "enabled");
    }

    @Override
    protected void doOnJob(Job job) throws IOException {
        client.enable(job.getName());
    }
}
