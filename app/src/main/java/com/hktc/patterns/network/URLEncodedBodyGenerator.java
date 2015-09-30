package com.hktc.patterns.network;

import org.apache.http.protocol.HTTP;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by hari on 11/9/15.
 */
public class URLEncodedBodyGenerator implements IBodyGenerator {
    private Map<String, String> headers = new TreeMap<String, String>();
    private ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

    URLEncodedBodyGenerator(ArrayList<HttpRequest.PostData> params) {
        headers.put(HttpRequest.Header.CONTENT_TYPE.getHeader()
                , HttpRequest.RequestMime.URL_ENCODED.getMime());

        boolean useDelimiter = false;
        for (HttpRequest.PostData postData:params) {
            try {
                if (useDelimiter) {
                    outputStream.write((byte) '&');
                }
                outputStream.write(postData.key.getBytes());
                outputStream.write((byte) '=');
                outputStream.write(postData.data);

                useDelimiter = true;

            } catch (IOException e) {}
        }
    }

    @Override
    public Map<String, String> getExtraHeaders() {
        return headers;
    }

    @Override
    public byte[] getBody() {
        return outputStream.toByteArray();
    }
}
