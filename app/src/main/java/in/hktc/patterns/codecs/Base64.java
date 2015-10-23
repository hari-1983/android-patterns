package in.hktc.patterns.codecs;

import android.util.Log;

/**
 * Created by hari on 22/10/15.
 */
public class Base64 {
    private static final String TAG = "Patterns/Base64";

    public static String toB64(String raw) {
        Log.d(TAG, "Encoding " + raw);

        byte[] encoded = android.util.Base64.encode(raw.getBytes(), android.util.Base64.DEFAULT);
        String encodedString = new String(encoded);
        encodedString = encodedString.replace("\n", "");

        return encodedString;
    }

    public static String fromB64(String b64) {
        return null;
    }
}
