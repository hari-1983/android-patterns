package com.hktc.patterns.network;

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

    public IBodyGenerator getBodyGenerator(HttpRequest.RequestMime mime) {
        if (mime.equals(HttpRequest.RequestMime.URL_ENCODED)) {
            return new URLEncodedBodyGenerator();
        } else if (mime.equals(HttpRequest.RequestMime.JSON)) {
            return new JSONBodyGenerator();
        } else { return null; }
    }
}
