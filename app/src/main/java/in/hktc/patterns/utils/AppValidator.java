package in.hktc.patterns.utils;

import android.content.Context;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import in.hktc.patterns.network.HttpRequest;

/**
 * Created by hari on 26/10/15.
 */
public class AppValidator {
    public interface ValidationListener {
        public void onValid(AppValidator validator);
        public void onInvalid(AppValidator validator);
    }

    public static final String TAG = "Patterns/AppValidator";
    public static final String VALIDATION_URL = "http://hktc.in:8080/AppValidator/v1/apps/";
    public static final String JSON_KEY_IS_ACTIVE = "isActive";
    public static final long TIMEOUT = 10000;

    private ValidationListener validationListener;
    private final Context context;
    private final String appID;
    private HttpRequest requestValidation;

    private HttpRequest.ResponseListener responseListener
            = new HttpRequest.ResponseListener() {

        @Override
        public void onOffline(HttpRequest request) {

        }

        @Override
        public void onConnected(HttpRequest request) {

        }

        @Override
        public void onTimeout(HttpRequest request) {

        }

        @Override
        public void onFailure(HttpRequest request, int statusCode, String mime, byte[] data) {

        }

        @Override
        public void onSuccess(HttpRequest request, String mime, byte[] data) {
            if (request.equals(requestValidation)) {
                String response = new String(data);
                Log.d(TAG, response);
                try {
                    JSONObject json = new JSONObject(response);
                    boolean isActive = json.getBoolean(JSON_KEY_IS_ACTIVE);
                    if (isActive) {
                        validationListener.onValid(AppValidator.this);
                    } else {
                        validationListener.onInvalid(AppValidator.this);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void onProgress(HttpRequest request, int percent) {

        }

        @Override
        public void onChunkAvailable(HttpRequest request, byte[] chunk) {

        }
    };

    public AppValidator(Context context, String appID) {
        this.context = context;
        this.appID = appID;
    }

    public void setValidationListener(ValidationListener listener) {
        validationListener = listener;
    }

    public void validate() {
        requestValidation = new HttpRequest();
        requestValidation.setResponseListener(responseListener);
        requestValidation.setContext(context);
        requestValidation.setTimeout(TIMEOUT);
        requestValidation.setMethod(HttpRequest.Method.GET);
        requestValidation.setUrl(VALIDATION_URL + appID);
        requestValidation.request();
    }
}
