package in.hktc.patterns.errorhandling;

import android.content.Context;
import android.os.Build;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

import in.hktc.patterns.network.HttpRequest;

/**
 * Created by hari on 2/11/15.
 */
public class ErrorHandler implements Thread.UncaughtExceptionHandler {
    private static final String TAG = "Patterns/Error";

    private String appPackage;
    private String versionReadable;
    private String versionCode;
    private Context context;

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

        }

        @Override
        public void onProgress(HttpRequest request, int percent) {

        }

        @Override
        public void onChunkAvailable(HttpRequest request, byte[] chunk) {

        }
    };

    public ErrorHandler(Context context, String appPackage, String versionReadable
            , String versionCode) {

        this.context = context;
        this.appPackage = appPackage;
        this.versionCode = versionCode;
        this.versionReadable = versionReadable;
    }

    @Override
    public void uncaughtException(Thread thread, Throwable throwable) {
        StringBuffer stringBuffer = new StringBuffer();
        dumpError(stringBuffer, throwable);

        String manufacturer = Build.MANUFACTURER;
        String brand = Build.BRAND;
        String model = Build.MODEL;
        String device = Build.DEVICE;
        String androidVersion = Build.VERSION.RELEASE;
        long timeStamp = (new Date()).getTime();
        String trace = stringBuffer.toString();

        HttpRequest request = new HttpRequest();
        request.setContext(context);
        request.setResponseListener(responseListener);
        request.setTimeout(API.TIMEOUT);
        request.setMethod(HttpRequest.Method.PUT);
        request.setUrl(API.URL);
        request.setRequestMime(HttpRequest.RequestMime.JSON);
        request.addParam(API.Fields.TIME_STAMP, String.valueOf(timeStamp));
        request.addParam(API.Fields.PACKAGE, appPackage);
        request.addParam(API.Fields.PACKAGE_VERSION, versionReadable);
        request.addParam(API.Fields.PACKAGE_VERSION_CODE, versionCode);
        request.addParam(API.Fields.MANUFACTURER, manufacturer);
        request.addParam(API.Fields.BRAND, brand);
        request.addParam(API.Fields.DEVICE, device);
        request.addParam(API.Fields.MODEL, model);
        request.addParam(API.Fields.ANDROID_VERSION, androidVersion);
        request.addParam(API.Fields.TRACE, trace);
        request.request();
    }

    private void dumpError(StringBuffer stringBuffer, Throwable throwable) {
        if (throwable != null) {
            stringBuffer.append(throwable.toString() + "\n");
            for (StackTraceElement element:throwable.getStackTrace()) {
                stringBuffer.append(element.toString());
            }
            Throwable cause = throwable.getCause();
            if (cause != null) {
                stringBuffer.append("... due to ...\n");
                dumpError(stringBuffer, cause);
            }
        }
    }
}
