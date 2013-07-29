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

        String replacement = HandlerUtils.INSTANCE.getReplacement("http://svn.adchemy.private/repo/production/branches/adchemy-leadgen-2.23-rc/adchemy-leadgen-frontend", action);

        assertEquals(
                "not replaced.",
                replacement,
                "http://svn.adchemy.private/repo/production/branches/adchemy-leadgen-2.24-rc/adchemy-leadgen-frontend"
        );
    }

    @Test
    public void testRegexReplacementWithGroup() throws Exception {
        Action action = createAction("2.24-rc", "adchemy-leadgen-(\\d+\\.\\d+(\\-rc)?)", "1");

        String replacement = HandlerUtils.INSTANCE.getReplacement("http://svn.adchemy.private/repo/production/branches/adchemy-leadgen-2.23-rc/adchemy-leadgen-frontend", action);

        assertEquals(
                "not replaced.",
                replacement,
                "http://svn.adchemy.private/repo/production/branches/adchemy-leadgen-2.24-rc/adchemy-leadgen-frontend"
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
