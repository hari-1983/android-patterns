package in.hktc.patterns.network;

import java.util.ArrayList;

/**
 * Created by hari on 11/9/15.
 */
public class BodyGeneratorFactory {
    private static BodyGeneratorFactory instance;

    private BodyGeneratorFactory() {}

    public static BodyGeneratorFactory getInstance() {
        if (instance == null) { instance = new BodyGeneratorFactory(); }
        return instance;
    }

    public IBodyGenerator getBodyGenerator(
            HttpRequest.RequestMime mime, ArrayList<HttpRequest.PostData> params) {

        if (mime.equals(HttpRequest.RequestMime.URL_ENCODED)) {
            return new URLEncodedBodyGenerator(params);
        } else if (mime.equals(HttpRequest.RequestMime.JSON)) {
            return new JSONBodyGenerator(params);
        } else if (mime.equals(HttpRequest.RequestMime.MULTIPART)) {
            return new MultipartBodyGenerator(params);
        } else { return null; }
    }
}
