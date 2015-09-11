package com.hktc.patterns.network;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by hari on 11/9/15.
 */
public interface IBodyGenerator {
    Map<String, String> getExtraHeaders();
    byte[] getBody(ArrayList<HttpRequest.PostData> params);
}
