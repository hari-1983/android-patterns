package com.hktc.patterns.network;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
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
    public byte[] getBody(ArrayList<HttpRequest.PostData> params) {
        JSONObject jsonObject = new JSONObject();

        for (HttpRequest.PostData postData:params) {
            try {
                jsonObject.put(postData.key, new String(postData.data));
            } catch (JSONException e) { return null; }
        }

        return jsonObject.toString().getBytes();
    }
}
