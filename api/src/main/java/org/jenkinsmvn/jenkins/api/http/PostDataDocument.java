package org.jenkinsmvn.jenkins.api.http;

import org.apache.commons.lang.Validate;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HttpContext;

import java.io.UnsupportedEncodingException;

/**
 * Post the given data
 */
public class PostDataDocument extends StatusOnlyHttpInvoker {
    
    private String data;

    public PostDataDocument(HttpClient client, HttpContext context, HttpHost targetHost) {
        super(client, context, targetHost, false);
    }

    public void setData(String data) {
        this.data = data;
    }

    @Override
    protected HttpRequest createHttpRequest(String path) throws UnsupportedEncodingException {
        Validate.notNull(path, "path should not be null.");
        Validate.notNull(data, "data should not be null.");

        HttpPost post = new HttpPost(path);

        StringEntity entity = new StringEntity(data, "utf-8");
        post.setEntity(entity);

        return post;
    }
}
