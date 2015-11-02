package in.hktc.patterns.errorhandling;

import android.os.Build;

import java.util.Date;

/**
 * Created by hari on 2/11/15.
 */
public class ErrorHandler implements Thread.UncaughtExceptionHandler {
    private static final String TAG = "Patterns/Error";

    private String appPackage;
    private String versionReadable;
    private String versionCode;

    public ErrorHandler(String appPackage, String versionReadable, String versionCode) {
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
        String timeStamp = (new Date()).toString();
        String exception = stringBuffer.toString();
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
