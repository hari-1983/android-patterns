package in.hktc.patterns.network;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by hari on 11/9/15.
 */
public class JSONBodyGenerator implements IBodyGenerator {
    private static final String TAG = "Patterns/JSONBody";
    private Map<String, String> headers = new TreeMap<String, String>();
    private JSONObject json = new JSONObject();

    JSONBodyGenerator(ArrayList<HttpRequest.PostData> params) {
        headers.put(HttpRequest.Header.CONTENT_TYPE.getHeader()
                , HttpRequest.RequestMime.JSON.getMime());

        for (HttpRequest.PostData postData:params) {
            try {
                json.put(postData.key, new String(postData.data));
            } catch (JSONException e) { continue; }
        }

    }

    @Override
    public Map<String, String> getExtraHeaders() {
        return headers;
    }

    @Override
    public byte[] getBody() {
        Log.d(TAG, "JSON is of " + json.toString().length() + " bytes");
        Log.d(TAG, json.toString());
        return json.toString().getBytes();
    }
}
