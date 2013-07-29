package org.jenkinsmvn.jenkins.api.http;

import com.sun.org.apache.xerces.internal.parsers.DOMParser;
import org.apache.http.HttpHost;
import org.apache.http.client.HttpClient;
import org.apache.http.protocol.HttpContext;
import org.codehaus.jackson.map.ObjectMapper;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;

/**
 * Retrieves a xml document using http
 */
public class GetXMLDocument extends AbstractResponseHttpInvoker<Document> {

    public GetXMLDocument(HttpClient client, HttpContext context, HttpHost targetHost, ObjectMapper mapper) {
        super(client, context, targetHost, mapper);
    }
    
    public Document getDocument(String path) throws IOException {
        return execute(path);
    }

    @Override
    protected Document doOnResponse() throws IOException {
        assert responseEntity != null;

        try {
            DOMParser parser = new DOMParser();
            parser.parse(new InputSource(responseEntity.getContent()));

            return parser.getDocument();
        } catch (SAXException e) {
            throw new IOException(e);
        }
    }
}
