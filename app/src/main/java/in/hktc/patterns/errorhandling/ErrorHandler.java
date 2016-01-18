package in.hktc.patterns.errorhandling;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

import in.hktc.patterns.network.HttpRequest;

/**
 * Created by hari on 2/11/15.
 */
public class ErrorHandler implements Thread.UncaughtExceptionHandler {
    private static final String TAG = "Patterns/Error";

    private Context context;
    private ErrorDumper errorDumper;
    private int layout;
    private Thread.UncaughtExceptionHandler defaultHandler;

    private ErrorDumper.ErrorDumperListener errorDumperListener
            = new ErrorDumper.ErrorDumperListener() {
        @Override
        public void onDumped(ErrorDumper errorDumper) {
            System.exit(0);
        }
    };

    public ErrorHandler(Context context, Thread.UncaughtExceptionHandler defaultHandler) {

        this.context = context;
        this.errorDumper = new ErrorDumper(errorDumperListener);
        this.defaultHandler = defaultHandler;
    }

    @Override
    public void uncaughtException(Thread thread, Throwable throwable) {
        ErrorDump errorDump = new ErrorDump(context, throwable);
        errorDumper.dump(errorDump);
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        defaultHandler.uncaughtException(thread, throwable);
    }
}
