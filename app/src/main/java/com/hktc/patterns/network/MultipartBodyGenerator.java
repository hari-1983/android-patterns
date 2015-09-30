package com.hktc.patterns.network;

import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Array;
import java.security.cert.CRL;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by hari on 11/9/15.
 */
public class MultipartBodyGenerator implements IBodyGenerator {
    private static final String CRLF = "\r\n";
    private static final String DASHES = "--";
    private static final String BOUNDARY = "130UND4RY";
    private static final String DECL = "Content-Disposition: form-data; name=";
    private static final String TAG = "Patterns/Multipart";
    private Map<String, String> headers = new TreeMap<String, String>();
    private ByteArrayOutputStream stream = new ByteArrayOutputStream();

    public MultipartBodyGenerator(ArrayList<HttpRequest.PostData> params) {
        headers.put(HttpRequest.Header.CONTENT_TYPE.getHeader()
                , HttpRequest.RequestMime.MULTIPART.getMime() + "; boundary=" + BOUNDARY);

        for (HttpRequest.PostData postData:params) {
            String leadup = DASHES + BOUNDARY + CRLF + DECL + "\"" + postData.key + "\"";
            if (postData.isBinary) {
                leadup += "; filename=\"" + postData.filename + "\"" + CRLF;
                leadup += HttpRequest.Header.CONTENT_TYPE.getHeader() + ": " + postData.mime + CRLF;
                leadup += "Content-Transfer-Encoding: binary";
            }
            leadup += CRLF + CRLF;

            stream.write(leadup.getBytes(), 0, leadup.length());
            stream.write(postData.data, 0, postData.data.length);
            stream.write(CRLF.getBytes(), 0, CRLF.length());
        }

        stream.write((DASHES + BOUNDARY + DASHES + CRLF).getBytes(), 0
                , (DASHES + BOUNDARY + DASHES + CRLF).length());

        headers.put(HttpRequest.Header.CONTENT_LENGTH.getHeader(), "" + stream.size());
    }

    @Override
    public Map<String, String> getExtraHeaders() {
        return headers;
    }

    @Override
    public byte[] getBody() {
        Log.d(TAG, new String(stream.toByteArray()));
        return stream.toByteArray();
    }
}
