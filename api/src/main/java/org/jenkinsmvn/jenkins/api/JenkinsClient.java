package org.jenkinsmvn.jenkins.api;

import org.jenkinsmvn.jenkins.api.http.GetJsonObject;
import org.jenkinsmvn.jenkins.api.http.GetXMLDocument;
import org.jenkinsmvn.jenkins.api.http.PostDataDocument;
import org.jenkinsmvn.jenkins.api.http.StatusOnlyHttpInvoker;
import org.jenkinsmvn.jenkins.api.model.*;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.apache.http.HttpHost;
import org.apache.http.HttpStatus;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.HttpClient;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HttpContext;
import org.codehaus.jackson.map.ObjectMapper;
import org.jenkinsmvn.jenkins.api.model.*;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.net.URI;
import java.util.*;

/**
 * Create a jenkins restful client using the apache http client.
 */
public class JenkinsClient {

    private static final String API_JSON_PATH = "/api/json";
    
    private static final String CONFIG_XML = "/config.xml";

    private static final String DISABLE_PATH = "/disable";

    private static final String ENABLE_PATH = "/enable";

    private static final String BUILD_PATH = "/build";

    private static final String BUILD_WITH_PARAMETERS_PATH = "/buildWithParameters";

    public static final long DEFAULT_TIME_OUT_IN_MILLIS = 1000l * 60l * 60l;

    public static final long DEFAULT_POLL_TIME_MILLIS = 5000l;
    
    public static final String RESULT_SUCCESS = "SUCCESS";

    public static final String RESULT_UNSTABLE = "UNSTABLE";

    public static final String RESULT_FAILURE = "FAILURE";

    private AbstractHttpClient client;

    private HttpContext context;

    private HttpHost targetHost;

    private ObjectMapper mapper;

    private Node node;

    private Map<String, JobDetails> jobsDetailCache;

    private Map<String, Job> jobsCache;

    private String contextPath;

    public JenkinsClient(AbstractHttpClient client, HttpContext context, ObjectMapper mapper, URI baseUri) throws IOException {
        this(client, context, mapper, baseUri, null);
    }

    public JenkinsClient(AbstractHttpClient client, HttpContext context, ObjectMapper mapper, URI baseUri, String contextPath) throws IOException {
        this.client = client;
        this.context = context;
        this.mapper = mapper;
        this.contextPath = contextPath;
        this.targetHost = new HttpHost(baseUri.getHost(), baseUri.getPort(), baseUri.getScheme());

        init();

        Validate.notNull(jobsCache, String.format("No jobs found for uri %s", baseUri.toString()));
    }

    public void authenticate(String username, String password) {
        UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(username, password);

        client.getCredentialsProvider().setCredentials(new AuthScope(targetHost.getHostName(), targetHost.getPort()), credentials);

        // Create AuthCache instance
        AuthCache authCache = new BasicAuthCache();

        // Generate BASIC scheme object and add it to the local auth cache
        BasicScheme basicAuth = new BasicScheme();
        authCache.put(targetHost, basicAuth);

        context.setAttribute(ClientContext.AUTH_CACHE, authCache);
    }

    private String getPath(String path) {
        if(StringUtils.isBlank(contextPath)) {
            return path;
        }

        return contextPath + path;
    }

    public String getContextPath() {
        return contextPath;
    }

    public void setContextPath(String contextPath) {
        this.contextPath = contextPath;
    }

    private void init() throws IOException {
        GetJsonObject<Node> retriever = createObjectRetriever(Node.class);

        node = retriever.getObject(getPath(API_JSON_PATH));
        List<Job> jobs = node.getJobs();

        if(CollectionUtils.isNotEmpty(jobs)) {
            Map<String, Job> tmp = new LinkedHashMap<String, Job>(jobs.size());

            for(Job job : jobs) {
                tmp.put(job.getName(), job);
            }

            jobsCache = Collections.unmodifiableMap(tmp);
            jobsDetailCache = Collections.synchronizedMap(new HashMap<String, JobDetails>(jobs.size()));
        }
    }
    
    private <T> GetJsonObject<T> createObjectRetriever(Class<T> clazz) {
        return new GetJsonObject<T>(client, context, targetHost, mapper, clazz);
    }

    private GetXMLDocument createXMLDocRetriever() {
        return new GetXMLDocument(client, context, targetHost, mapper);
    }

    private StatusOnlyHttpInvoker createPostStatusInvoker(int status) {
        StatusOnlyHttpInvoker invoker = new StatusOnlyHttpInvoker(client, context, targetHost, false);
        invoker.setHttpStatus(status);

        return invoker;
    }

    private PostDataDocument createPostDataInvoker() {
        return new PostDataDocument(client, context, targetHost);
    }

    public Node getNode() {
        return node;
    }
    
    public Job getJob(String jobName) {
        if(!jobsCache.containsKey(jobName)) {
            throw new IllegalArgumentException(String.format("Job with name '%s' not found.", jobName));
        }

        return jobsCache.get(jobName);
    }

    public void waitTillAllBuildsDone(String jobName) throws IOException, InterruptedException {
        waitTillAllBuildsDone(jobName, DEFAULT_POLL_TIME_MILLIS);
    }

    public void waitTillAllBuildsDone(String jobName, long pollTimeMillis) throws IOException, InterruptedException {
        waitTillAllBuildsDone(jobName, pollTimeMillis, DEFAULT_TIME_OUT_IN_MILLIS);
    }
    
    public void waitTillAllBuildsDone(String jobName, long pollTimeMillis, long timeOutInMillis) throws IOException, InterruptedException {
        JobDetails jobDetails = getJobDetails(jobName, false);

        if(!jobDetails.getBuildable()) {
            return;
        }

        synchronized (jobsCache.get(jobName)) {
            long start = System.currentTimeMillis();
            do {
                if(jobDetails.getQueueItem() == null) {
                    if(jobDetails.getLastBuild() == null) {
                        return;
                    }

                    BuildDetails buildDetails = getBuildDetails(jobDetails.getLastBuild());

                    if(!buildDetails.getBuilding()) {
                        return;
                    }
                }

                if(System.currentTimeMillis() - start > timeOutInMillis) {
                    throw new InterruptedException(String.format("time out of %dms reached.", timeOutInMillis));
                }

                Thread.sleep(pollTimeMillis);
                jobDetails = getJobDetails(jobName, false);
            } while(true);
        }
    }

    public boolean build(String jobName) throws IOException {
        return build(jobName, false, null);
    }

    public boolean build(String jobName, boolean failOnBuilding) throws IOException {
        return build(jobName, failOnBuilding, null);
    }

    public boolean build(String jobName, Map<String, String> parameters) throws IOException {
        return build(jobName, false, parameters);
    }

    public boolean build(String jobName, boolean failOnBuilding, Map<String, String> parameters) throws IOException {
        JobDetails jobDetails = getJobDetails(jobName);
        
        if(!jobDetails.getBuildable()) {
            return false;
        }

        if(failOnBuilding) {
            if(jobDetails.getQueueItem() != null) {
                return false;
            }

            Build lastBuild = jobDetails.getLastBuild();

            if(lastBuild != null) {
                BuildDetails buildDetails = getBuildDetails(lastBuild);

                if(buildDetails.getBuilding()) {
                    return false;
                }
            }
        }

        StatusOnlyHttpInvoker retriever = createPostStatusInvoker(HttpStatus.SC_MOVED_TEMPORARILY);

        if(MapUtils.isNotEmpty(parameters)) {
            for(Map.Entry<String, String> entry : parameters.entrySet()) {
                retriever.addParameter(new BasicNameValuePair(entry.getKey(), entry.getValue()));
            }

            retriever.forceParametersOnPath();
            return retriever.execute(jobDetails.getUrl().getPath() + BUILD_WITH_PARAMETERS_PATH);
        }

        return retriever.execute(jobDetails.getUrl().getPath() + BUILD_PATH);
    }

    public Set<String> getJobNames() {
        return jobsCache.keySet();
    }

    public JobDetails getJobDetails(String jobName) throws IOException {
        return getJobDetails(jobName, true);
    }
    
    public ConfigDocument getJobConfig(String jobName) throws IOException {
        Job job = getJob(jobName);

        GetXMLDocument retriever = createXMLDocRetriever();
        
        return new ConfigDocument(retriever.getDocument(job.getUrl().getPath() + CONFIG_XML));
    }

    public boolean saveConfig(String jobName, ConfigDocument document) throws TransformerException, ParserConfigurationException, IOException {
        Job job = getJob(jobName);

        PostDataDocument post = createPostDataInvoker();
        post.setData(document.toXMLString());

        return post.execute(job.getUrl().getPath() + CONFIG_XML);
    }
    
    public boolean disable(String jobName) throws IOException {
        Job job = getJob(jobName);

        StatusOnlyHttpInvoker retriever = createPostStatusInvoker(HttpStatus.SC_MOVED_TEMPORARILY);
        return retriever.execute(job.getUrl().getPath() + DISABLE_PATH);
    }
    
    public boolean enable(String jobName) throws IOException {
        Job job = getJob(jobName);

        StatusOnlyHttpInvoker retriever = createPostStatusInvoker(HttpStatus.SC_MOVED_TEMPORARILY);
        return retriever.execute(job.getUrl().getPath() + ENABLE_PATH);
    }
    
    public BuildDetails getBuildDetails(String jobName, Integer number) throws IOException {
        JobDetails details = getJobDetails(jobName);

        for(Build build : details.getBuilds()) {
            if(build.getNumber().equals(number)) {
                return getBuildDetails(build);
            }
        }

        throw new IllegalArgumentException(String.format("No build details for jobName '%s' and build number '%d'.", jobName, number));
    }
    
    public BuildDetails getBuildDetails(Build build) throws IOException {
        GetJsonObject<BuildDetails> retriever = createObjectRetriever(BuildDetails.class);

        return retriever.getObject(build.getUrl().getPath() + API_JSON_PATH);
    }

    public JobDetails getJobDetails(String jobName, boolean fromCache) throws IOException {
        synchronized (jobsCache.get(jobName)) {
            Job job = getJob(jobName);

            if(fromCache && jobsDetailCache.containsKey(jobName)) {
                return jobsDetailCache.get(jobName);
            }

            GetJsonObject<JobDetails> retriever = createObjectRetriever(JobDetails.class);
            JobDetails details = retriever.getObject(job.getUrl().getPath() + API_JSON_PATH);

            jobsDetailCache.put(jobName, details);

            return details;
        }
    }
}
