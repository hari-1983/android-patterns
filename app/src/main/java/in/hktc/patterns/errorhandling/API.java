package in.hktc.patterns.errorhandling;

/**
 * Created by hari on 3/11/15.
 */
public class API {
    public class Fields {
        public static final String TIME_STAMP = "timeStamp";
        public static final String MANUFACTURER = "manufacturer";
        public static final String DEVICE = "device";
        public static final String BRAND = "brand";
        public static final String MODEL = "model";
        public static final String ANDROID_VERSION = "androidVersion";
        public static final String PACKAGE = "package";
        public static final String PACKAGE_VERSION = "packageVersion";
        public static final String PACKAGE_VERSION_CODE = "packageVersionCode";
        public static final String TRACE = "trace";
    }

    public static final String URL = "http://192.168.1.102:8080/AppErrorLogger/v1/error-logs";
    public static final int TIMEOUT = 10000;
}
