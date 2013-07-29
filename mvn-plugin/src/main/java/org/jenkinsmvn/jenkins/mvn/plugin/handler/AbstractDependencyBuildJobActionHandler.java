package org.jenkinsmvn.jenkins.mvn.plugin.handler;


import org.jenkinsmvn.jenkins.api.model.Job;
import org.jenkinsmvn.jenkins.mvn.plugin.BuildJobExecutorService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.util.*;

import static org.jenkinsmvn.jenkins.mvn.plugin.PropertyConstants.*;


public abstract class AbstractDependencyBuildJobActionHandler extends BuildJobsActionHandler {

    public static final int DEFAULT_MAX_NUMBER_OF_RETRIES = 5;

    public static final int DEFAULT_MAX_NUMBER_OF_RETRIES_PER_BUILD = 3;

    protected Map<String, List<String>> tree = new LinkedHashMap<String, List<String>>();

    protected final List<String> completed = Collections.synchronizedList(new LinkedList<String>());

    protected List<String> failure = Collections.synchronizedList(new LinkedList<String>());

    protected final List<String> pendingList = Collections.synchronizedList(new LinkedList<String>());

    protected int maxNumberOfRetries = DEFAULT_MAX_NUMBER_OF_RETRIES;

    protected int maxNumberOfRetriesPerJob = DEFAULT_MAX_NUMBER_OF_RETRIES_PER_BUILD;

    protected final List<String> retries = Collections.synchronizedList(new LinkedList<String>());

    public AbstractDependencyBuildJobActionHandler(String type) {
        super(type);
    }

    protected abstract void buildTree(List<String> jobNames) throws IOException;

    @Override
    public void execute() throws IOException {
        List<String> jobNames = getRelevantJobs();

        if(CollectionUtils.isEmpty(jobNames) && !isOnNoJobsContinue()) {
            return;
        }

        if(HandlerUtils.INSTANCE.isPropertyExist(action, MAXIMUM_BUILD_RETRIES)) {
            maxNumberOfRetries =  HandlerUtils.INSTANCE.getRequiredInteger(action, MAXIMUM_BUILD_RETRIES);
        }

        if(HandlerUtils.INSTANCE.isPropertyExist(action, MAXIMUM_BUILD_RETRIES_PER_JOB)) {
            maxNumberOfRetriesPerJob =  HandlerUtils.INSTANCE.getRequiredInteger(action, MAXIMUM_BUILD_RETRIES_PER_JOB);
        }


        buildTree(jobNames);
        pendingList.addAll(jobNames);

        logInfo(String.format("::>>> %d job%s to '%s', maximum retries is (all: '%d', job: '%d').", jobNames.size(), jobNames.size() > 1 ? "s" : "", getActionType(), maxNumberOfRetries, maxNumberOfRetriesPerJob));

        while(completed.size() < tree.size() && CollectionUtils.isEmpty(failure)) {
            String next = nextJobToBuild();

            if(next != null) {
                BuildJobExecutorService.INSTANCE.submit(new ExecuteBuildRunnable(next));
            }

            try {
                if(pendingList.isEmpty()) {
                    logInfo("Waiting for all builds to complete...");

                    synchronized (this) {
                        wait(120000l);
                    }
                } else if(!hasNext()) {
                    logInfo("Waiting for dependencies to build...");

                    synchronized (this) {
                        wait(120000l);
                    }
                }
            } catch (InterruptedException ignore) {}
        }

        if(CollectionUtils.isNotEmpty(failure)) {
            throw new IllegalStateException(String.format("Failure running build for '%s'", failure.toArray()));
        }
    }

    private Boolean hasNext() {
        synchronized (pendingList) {
            for (String pending : pendingList) {
                List<String> dependencies = tree.get(pending);

                if (completed.containsAll(dependencies)) {
                    return true;
                }
            }

            return false;
        }
    }

    private synchronized String nextJobToBuild() {
        synchronized (pendingList) {
            for(Iterator<String> itr = pendingList.iterator(); itr.hasNext();) {
                String pending = itr.next();
                List<String> dependencies = tree.get(pending);

                if(completed.containsAll(dependencies)) {
                    itr.remove();
                    return pending;
                }
            }
        }

        return null;
    }

    private class ExecuteBuildRunnable implements Runnable {

        private String jobName;

        private ExecuteBuildRunnable(String jobName) {
            this.jobName = jobName;
        }

        public boolean isMaximumRetries() {
            return retries.size() >= maxNumberOfRetries;
        }

        public boolean isMaximumRetriesPerBuild() {
            int count = 0;

            for(String job : retries) {
                if(StringUtils.equals(jobName, job)) {
                    count++;
                }
            }

            return count >= maxNumberOfRetriesPerJob;
        }

        @Override
        public void run() {
            try {
                try {
                    Job job = client.getJob(jobName);

                    doOnJob(job);
                    completed.add(jobName);

                    // do logging
                    logInfo(String.format("(%d of %d) Job %s completed.", completed.size(), tree.size(), jobName));
                } catch(Exception e) {
                    synchronized (retries) {
                        retries.add(jobName);
                        if(isMaximumRetries() || isMaximumRetriesPerBuild()) {
                            if(isMaximumRetries()) {
                                logInfo(String.format("Maximum number '%d' retries reached. %s", maxNumberOfRetries, retries));
                            } else {
                                logInfo(String.format("Maximum number '%d' retries reached for job '%s'.", maxNumberOfRetriesPerJob, jobName));
                            }

                            failure.add(jobName);
                        } else {
                            synchronized (pendingList) {
                                logInfo(String.format("Build '%s' failed, will retry.", jobName));
                                pendingList.add(jobName);
                            }
                        }
                    }
                }
            } finally {
                synchronized (AbstractDependencyBuildJobActionHandler.this) {
                    // notify that a job was completed
                    AbstractDependencyBuildJobActionHandler.this.notifyAll();
                }
            }
        }
    }
}
