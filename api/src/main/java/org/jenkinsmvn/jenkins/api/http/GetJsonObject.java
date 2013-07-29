package org.jenkinsmvn.jenkins.api.http;

import org.apache.http.HttpHost;
import org.apache.http.client.HttpClient;
import org.apache.http.protocol.HttpContext;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;

/**
 * Retrieves a JSON object using http client.
 */
public class GetJsonObject<T> extends AbstractResponseHttpInvoker<T> {

    private Class<T> clazz;


    public GetJsonObject(HttpClient client, HttpContext context, HttpHost targetHost, ObjectMapper mapper, Class<T> clazz) {
        super(client, context, targetHost, mapper);
        this.clazz = clazz;
    }

    public T getObject(String path) throws IOException {
        return execute(path);
    }

    @Override
    protected T doOnResponse() throws IOException {
        return mapper.readValue(responseEntity.getContent(), clazz);
    }
}
