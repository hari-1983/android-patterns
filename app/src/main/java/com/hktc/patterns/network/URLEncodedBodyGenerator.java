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
    @Override
    public Map<String, String> getExtraHeaders() {
        Map<String, String> headers = new TreeMap<String, String>();
        headers.put(HttpRequest.Header.CONTENT_TYPE.getHeader()
            , HttpRequest.RequestMime.URL_ENCODED.getMime());
        return headers;
    }

    @Override
    public byte[] getBody(ArrayList<HttpRequest.PostData> params) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

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

            } catch (IOException e) { return null; }
        }

        return outputStream.toByteArray();
    }
}
