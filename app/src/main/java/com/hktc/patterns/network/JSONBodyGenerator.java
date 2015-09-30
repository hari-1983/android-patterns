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
    private Map<String, String> headers = new TreeMap<String, String>();
    private JSONObject json;

    JSONBodyGenerator(ArrayList<HttpRequest.PostData> params) {
        headers.put(HttpRequest.Header.CONTENT_TYPE.getHeader()
                , HttpRequest.RequestMime.JSON.getMime());

        JSONObject jsonObject = new JSONObject();

        for (HttpRequest.PostData postData:params) {
            try {
                jsonObject.put(postData.key, new String(postData.data));
            } catch (JSONException e) { continue; }
        }

    }

    @Override
    public Map<String, String> getExtraHeaders() {
        return headers;
    }

    @Override
    public byte[] getBody() {
        return json.toString().getBytes();
    }
}
