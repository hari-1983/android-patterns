package com.hktc.patterns.network;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;
import java.util.TreeMap;

/**
 * Created by hari on 11/9/15.
 */
public class JSONBodyGenerator implements IBodyGenerator {
    @Override
    public Map<String, String> getExtraHeaders() {
        Map<String, String> headers = new TreeMap<String, String>();
        headers.put(HttpRequest.Header.CONTENT_TYPE.getHeader()
                , HttpRequest.RequestMime.JSON.getMime());
        return headers;
    }

    @Override
    public byte[] getBody(Map<String, byte[]> params) {
        JSONObject jsonObject = new JSONObject();

        for (String key:params.keySet()) {
            try {
                jsonObject.put(key, new String(params.get(key)));
            } catch (JSONException e) { return null; }
        }

        return jsonObject.toString().getBytes();
    }
}
