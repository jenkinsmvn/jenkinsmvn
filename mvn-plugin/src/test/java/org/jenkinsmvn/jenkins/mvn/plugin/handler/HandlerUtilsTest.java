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

import org.jenkinsmvn.jenkins.mvn.plugin.Action;
import org.jenkinsmvn.jenkins.mvn.plugin.handler.HandlerUtils;
import org.junit.Test;

import java.util.Properties;

import static org.jenkinsmvn.jenkins.mvn.plugin.PropertyConstants.*;
import static junit.framework.Assert.assertEquals;

/**
 * Test for {@link org.jenkinsmvn.jenkins.mvn.plugin.handler.HandlerUtils} class.
 */
public class HandlerUtilsTest {

    @Test(expected = IllegalArgumentException.class)
    public void testNoReplacement() throws Exception {
        Action action = new Action();
        HandlerUtils.INSTANCE.getReplacement("hello", action);
    }
    
    @Test
    public void testDirectReplacement() throws Exception {
        Action action = createAction("action");
        
        String replacement = HandlerUtils.INSTANCE.getReplacement("hello", action);
        assertEquals("not replaced.", "action", replacement);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRegexReplacementNotFound() throws Exception {
        Action action = createAction("2.24-rc", "\\d+\\.\\d+(\\-rc)?");

        HandlerUtils.INSTANCE.getReplacement("not found", action);
    }

    @Test
    public void testRegexReplacement() throws Exception {
        Action action = createAction("2.24-rc", "\\d+\\.\\d+(\\-rc)?");

        String replacement = HandlerUtils.INSTANCE.getReplacement("http://svnpath/test-project-for-auto-branching-1-2.23-rc", action);

        assertEquals(
                "not replaced.",
                replacement,
                "http://svnpath/test-project-for-auto-branching-1-2.24-rc"
        );
    }

    @Test
    public void testRegexReplacementWithGroup() throws Exception {
        Action action = createAction("2.24-rc", "test-project-for-auto-branching-1-(\\d+\\.\\d+(\\-rc)?)", "1");

        String replacement = HandlerUtils.INSTANCE.getReplacement("http://svnpath/test-project-for-auto-branching-1-2.23-rc", action);

        assertEquals(
                "not replaced.",
                replacement,
                "http://svnpath/test-project-for-auto-branching-1-2.24-rc"
        );
    }

    private Action createAction(String replacement) {
        return createAction(replacement, null);
    }

    private Action createAction(String replacement, String regexPattern) {
        return createAction(replacement, regexPattern, null);
    }

    private Action createAction(String replacement, String regexPattern, String group) {
        Action action = new Action();
        action.setName("action");

        Properties properties = new Properties();
        properties.setProperty(REPLACEMENT, replacement);

        action.setProperties(properties);

        if(regexPattern != null) {
            properties.setProperty(REGEX_REPLACE_PATTERN, regexPattern);

            if(group != null) {
                properties.setProperty(REGEX_REPLACE_PATTERN_GROUP, group);
            }
        }

        return action;
    }
}
