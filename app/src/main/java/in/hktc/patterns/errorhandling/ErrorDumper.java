package in.hktc.patterns.errorhandling;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import in.hktc.patterns.network.HttpRequest;

/**
 * Created by hari on 3/11/15.
 */
public class ErrorDumper {
    public interface ErrorDumperListener {
        public void onDumped(ErrorDumper errorDumper);
    }

    private static final String TAG = "Patterns/Error";

    private Handler handler;
    private ErrorDumperListener errorDumperListener;

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
            errorDumperListener.onDumped(ErrorDumper.this);
        }

        @Override
        public void onSuccess(HttpRequest request, String mime, byte[] data) {
            errorDumperListener.onDumped(ErrorDumper.this);
        }

        @Override
        public void onProgress(HttpRequest request, int percent) {

        }

        @Override
        public void onChunkAvailable(HttpRequest request, byte[] chunk) {

        }
    };

    public ErrorDumper(Handler handler, ErrorDumperListener listener) {
        this.handler = handler;
        this.errorDumperListener = listener;
    }

    public void dump(final ErrorDump dump) {
        Log.d(TAG, dump.getAppPackage());
        Log.d(TAG, dump.getVersionReadable());
        Log.d(TAG, String.valueOf(dump.getVersionCode()));
        Log.d(TAG, dump.getManufacturer());
        Log.d(TAG, dump.getBrand());
        Log.d(TAG, dump.getDevice());
        Log.d(TAG, dump.getModel());
        Log.d(TAG, dump.getAndroidVersion());
        Log.d(TAG, String.valueOf(dump.getTimeStamp()));
        Log.d(TAG, dump.getTrace());

        HttpRequest request = new HttpRequest();
        request.setContext(dump.getContext());
        request.setResponseListener(responseListener);
        request.setTimeout(API.TIMEOUT);
        request.setMethod(HttpRequest.Method.PUT);
        request.setUrl(API.URL);
        request.setRequestMime(HttpRequest.RequestMime.JSON);
        request.addParam(API.Fields.TIME_STAMP, String.valueOf(dump.getTimeStamp()));
        request.addParam(API.Fields.PACKAGE, dump.getAppPackage());
        request.addParam(API.Fields.PACKAGE_VERSION, dump.getVersionReadable());
        request.addParam(API.Fields.PACKAGE_VERSION_CODE, String.valueOf(
                dump.getVersionCode()));
        request.addParam(API.Fields.MANUFACTURER, dump.getManufacturer());
        request.addParam(API.Fields.BRAND, dump.getBrand());
        request.addParam(API.Fields.DEVICE, dump.getDevice());
        request.addParam(API.Fields.MODEL, dump.getModel());
        request.addParam(API.Fields.ANDROID_VERSION, dump.getAndroidVersion());
        request.addParam(API.Fields.TRACE, dump.getTrace());
        request.request();
    }
}
