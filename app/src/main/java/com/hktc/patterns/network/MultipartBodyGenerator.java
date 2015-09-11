package com.hktc.patterns.network;

import java.util.Map;
import java.util.TreeMap;

/**
 * Created by hari on 11/9/15.
 */
public class MultipartBodyGenerator implements IBodyGenerator {
    MultipartBodyGenerator() {
        
    }

    @Override
    public Map<String, String> getExtraHeaders() {
        Map<String, String> headers = new TreeMap<String, String>();
        headers.put(HttpRequest.Header.CONTENT_TYPE.getHeader()
                , HttpRequest.RequestMime.JSON.getMime());
        return headers;
    }

    @Override
    public byte[] getBody(Map<String, byte[]> params) {
        return new byte[0];
    }
}
