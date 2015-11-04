package in.hktc.patterns.errorhandling;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import java.util.Date;

/**
 * Created by hari on 3/11/15.
 */
public class ErrorDump {
    private static final String TAG = "Patterns/Error";
    private static final String UNKNOWN = "unknown";

    private Context context;
    private String appPackage = UNKNOWN;
    private String versionReadable = UNKNOWN;
    private int versionCode = 0;
    private long timeStamp = new Date().getTime();
    private String manufacturer = UNKNOWN;
    private String brand = UNKNOWN;
    private String device = UNKNOWN;
    private String model = UNKNOWN;
    private String androidVersion = UNKNOWN;
    private String trace = UNKNOWN;

    public ErrorDump(Context context, Throwable throwable) {
        this.context = context;

        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName()
                    , 0);
            appPackage = info.packageName;
            versionCode = info.versionCode;
            versionReadable = info.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            Log.d(TAG, "Package of name " + context.getPackageName() + " not found");
        }

        manufacturer = Build.MANUFACTURER;
        brand = Build.BRAND;
        device = Build.DEVICE;
        model = Build.MODEL;
        androidVersion = Build.VERSION.RELEASE;

        StringBuffer stringBuffer = new StringBuffer();
        dumpError(stringBuffer, throwable);
        trace = stringBuffer.toString();
    }

    public Context getContext() {
        return context;
    }

    public String getAppPackage() {
        return appPackage;
    }

    public String getVersionReadable() {
        return versionReadable;
    }

    public int getVersionCode() {
        return versionCode;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public String getBrand() {
        return brand;
    }

    public String getDevice() {
        return device;
    }

    public String getModel() {
        return model;
    }

    public String getAndroidVersion() {
        return androidVersion;
    }

    public String getTrace() {
        return trace;
    }

    private void dumpError(StringBuffer stringBuffer, Throwable throwable) {
        if (throwable != null) {
            stringBuffer.append(throwable.toString() + "\n");
            for (StackTraceElement element:throwable.getStackTrace()) {
                stringBuffer.append(element.toString() + "\n");
            }
            Throwable cause = throwable.getCause();
            if (cause != null) {
                stringBuffer.append("... due to ...\n");
                dumpError(stringBuffer, cause);
            }
        }
    }
}
