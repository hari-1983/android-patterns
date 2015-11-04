package in.hktc.patterns.network;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.util.Log;

import in.hktc.patterns.workflow.AsyncTimedWork;
import in.hktc.patterns.workflow.WorkFlow;

/**
 * Created by hari on 11/7/15.
 */
public class HttpRequest implements AsyncTimedWork.TimedWorkListener, Comparable<HttpRequest> {
    public enum Method {
        GET("GET"),
        POST("POST"),
        PUT("PUT"),
        DELETE("DELETE"),
        HEAD("HEAD");

        private String method;
        Method(String method) { this.method = method; }
        public String getMethod() { return method; }
    }

    public enum ResponseType { WHOLE, CHUNKED }

    public enum RequestMime {
        URL_ENCODED("application/x-www-form-urlencoded"),
        MULTIPART("multipart/form-data"),
        JSON("application/json");

        private String mime;
        RequestMime(String mime) { this.mime = mime; }
        public String getMime() { return mime; }
    }

    public enum Header {
        CONTENT_TYPE("Content-Type")
        , CONTENT_LENGTH("Content-Length");

        private String header;
        Header(String header) { this.header = header; }
        public String getHeader() { return header; }
    }

    public interface ResponseListener {
        public void onOffline(HttpRequest request);
        public void onConnected(HttpRequest request);
        public void onTimeout(HttpRequest request);
        public void onFailure(HttpRequest request, int statusCode, String mime, byte[] data);
        public void onSuccess(HttpRequest request, String mime, byte[] data);
        public void onProgress(HttpRequest request, int percent);
        public void onChunkAvailable(HttpRequest request, byte[] chunk);
    }

    class PostData {
        boolean isBinary;
        String mime;
        String filename;
        String key;
        byte[] data;
    }

    private static final String TAG = "Patterns/HttpRequest";
    private static final int RESPONSE_FAILURE_THRESHOLD = 400;
    private static final int DEFAULT_BUFFER_SIZE = 1048576;

    private String url;
    private long timeout;
    private ResponseListener responseListener;
    private Handler handlerCallback;
    private Context context;
    private Method method;
    private RequestMime requestMime;
    private ArrayList<PostData> requestParams;
    private int percentProgress;
    private String requestCode = "";
    private ResponseType responseType = ResponseType.WHOLE;
    private Map<String, String> headers = new TreeMap<String, String>();

    private byte[] response;
    private HttpURLConnection conn;
    private InputStream connInputStream;
    private OutputStream connOutputStream;
    private boolean hasTimedOut = false;
    private int responseCode;
    private int contentLength;
    private String mime;
    private boolean isCancelled = false;

    private AsyncTimedWork networkRequest = new AsyncTimedWork() {
        @Override
        public void workAsync() {
        	Log.d(TAG, "Sending request to " + url);
        	
            ConnectivityManager connManager = (ConnectivityManager)
        		context.getSystemService(Context.CONNECTIVITY_SERVICE);
            
            NetworkInfo networkInfo = connManager.getActiveNetworkInfo();
            if (networkInfo == null
            		|| !networkInfo.isConnected()) {

            	sendOffline();
            }
            
            Log.d(TAG, networkInfo.getTypeName() + " is up & running");
            sendConnected();

            HttpURLConnection conn = null;

            try {
                int bytesReadThisCall, bytesReadSoFar = 0;

                conn = (HttpURLConnection) new URL(url).openConnection();
                Log.d(TAG, "Connection opened to " + url);
                Log.d(TAG, "There are '" + requestParams.size() + "' params to send");
                Log.d(TAG, "Setting method to '" + method.getMethod() + "'");
                conn.setRequestMethod(method.getMethod());

                for (String headerName:headers.keySet()) {
                    Log.d(TAG, "Adding header " + headerName + ": " + headers.get(headerName));
                    conn.addRequestProperty(headerName, headers.get(headerName));
                }

                if (!method.equals(Method.GET) && requestParams.size() > 0) {
                    IBodyGenerator bodyGenerator
                        = BodyGeneratorFactory.getInstance().getBodyGenerator(
                            requestMime, requestParams);

                    for (String headerName : bodyGenerator.getExtraHeaders().keySet()) {
                        Log.d(TAG, "Adding header, " + headerName + ":"
                                + bodyGenerator.getExtraHeaders().get(headerName));
                        conn.addRequestProperty(headerName
                            , bodyGenerator.getExtraHeaders().get(headerName));
                    }

                    byte[] payload = bodyGenerator.getBody();
                    conn.setRequestProperty("Content-Length", "" + payload.length);
                    conn.getOutputStream().write(payload, 0, payload.length);
                    Log.d(TAG, "Wrote '" + payload.length + "' bytes of payload");
                }

                Map<String, List<String>> map = conn.getHeaderFields();
                for (String key:map.keySet()) {
                    Log.d(TAG, "Received header with key '" + key + "', value '"
                        + conn.getHeaderField(key) + "'");
                }

                mime = conn.getContentType();
                if (mime == null || mime.isEmpty()) {
                    mime = conn.getHeaderField("Content-Type");
                }

                contentLength = conn.getContentLength();
                if (contentLength < 0) {
                    contentLength = conn.getHeaderFieldInt("Content-Length", -1);
                }
                if (contentLength < 0) {
                    contentLength = DEFAULT_BUFFER_SIZE;
                }

                Log.d(TAG, "Reply received, Mime: " + mime
            		+ ", content length: " + contentLength);

                responseCode = conn.getResponseCode();
                Log.d(TAG, "Response code: " + responseCode);

                if (responseCode >= RESPONSE_FAILURE_THRESHOLD) {
                    connInputStream = conn.getErrorStream();
                } else {
                    connInputStream = conn.getInputStream();
                }

                /* Determine buffer size */
                int bufferSize = 0;
                if (responseType.equals(ResponseType.CHUNKED)) {
                    bufferSize = DEFAULT_BUFFER_SIZE;
                } else if (responseType.equals(ResponseType.WHOLE)) {
                    bufferSize = contentLength;
                }

                final byte[] buffer = new byte[bufferSize];
                while (bytesReadSoFar < contentLength) {
                    bytesReadThisCall = 0;

                    if (responseType.equals(ResponseType.WHOLE)) {
                        bytesReadThisCall = connInputStream.read(buffer, bytesReadSoFar
                                , contentLength - bytesReadSoFar);
                        if (bytesReadThisCall < 0) {
                            break;
                        }
                    } else if (responseType.equals(ResponseType.CHUNKED)) {
                        bytesReadThisCall = connInputStream.read(buffer, 0, buffer.length);
                    }

                    bytesReadSoFar += bytesReadThisCall;

                    if (contentLength > 0) {
                        percentProgress = (bytesReadSoFar * 100) / contentLength;

                        if (handlerCallback == null) {
                            responseListener.onProgress(HttpRequest.this, percentProgress);
                        } else {
                            Runnable runnable = new Runnable() {
                                @Override
                                public void run() {
                                    responseListener.onProgress(HttpRequest.this, percentProgress);
                                }
                            };
                            handlerCallback.post(runnable);
                        }
                    }

                    if (responseType.equals(ResponseType.CHUNKED)) {
                        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                        outputStream.write(buffer, 0, bytesReadThisCall);

                        if (handlerCallback == null) {
                            responseListener.onChunkAvailable(HttpRequest.this
                                    , outputStream.toByteArray());
                        } else {
                            Runnable runnable = new Runnable() {
                                @Override
                                public void run() {
                                    responseListener.onChunkAvailable(HttpRequest.this
                                            , outputStream.toByteArray());
                                }
                            };
                            handlerCallback.post(runnable);
                        }

                        outputStream.close();
                    }
                }

                if (responseType.equals(ResponseType.WHOLE)) {
                    if (handlerCallback == null) {
                        sendResponse(responseCode, mime, buffer, bytesReadSoFar);
                    } else {
                        final int totalBytes = bytesReadSoFar;

                        Runnable runnable = new Runnable() {
                            @Override
                            public void run() {
                                sendResponse(responseCode, mime, buffer, totalBytes);
                            }
                        };

                        handlerCallback.post(runnable);
                    }
                } else {
                    if (handlerCallback == null) {
                        sendResponse(responseCode, mime, null, 0);
                    } else {
                        final int totalBytes = bytesReadSoFar;

                        Runnable runnable = new Runnable() {
                            @Override
                            public void run() {
                                sendResponse(responseCode, mime, null, 0);
                            }
                        };

                        handlerCallback.post(runnable);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();

                if (!hasTimedOut && !isCancelled) {
                    if (handlerCallback == null) {
                        sendResponse(-1, null, null, 0);
                    } else {
                        Runnable runnable = new Runnable() {
                            @Override
                            public void run() {
                            sendResponse(-1, null, null, 0);
                            }
                        };

                        handlerCallback.post(runnable);
                    }
                }
            } finally {
                cleanup();
            }
        }
    };

    public HttpRequest() {
        requestParams = new ArrayList<PostData>();
    }

    public void setUrl(String url) { this.url = url; }
    public void setTimeout(long timeout) { this.timeout = timeout; }
    public void setResponseListener(ResponseListener listener) { responseListener = listener; }
    public void setCallbackHandler(Handler handler) { handlerCallback = handler; }
    public void setContext(Context context) { this.context = context; }
    public void setMethod(Method method) { this.method = method; }
    public void setRequestMime(RequestMime mime) { this.requestMime = mime; }
    public void setResponseType(ResponseType responseType) { this.responseType = responseType; }
    public void setRequestCode(String requestCode) { this.requestCode = requestCode; }

    public String getUrl() { return url; }
    public String getRequestCode() { return requestCode; }
    public int getPercentProgress() { return percentProgress; }
    public ResponseType getResponseType() { return responseType; }

    public void addParam(String key, String value){
        PostData postData = new PostData();
        postData.key = key;
        postData.isBinary = false;
        postData.data = value.getBytes();
        requestParams.add(postData);
    }

    public void addParam(String key, File file, String mime) {
        try {
            FileInputStream inputStream = new FileInputStream(file);
            int fileSize = (int) file.length();
            byte[] fileBytes = new byte[fileSize];
            inputStream.read(fileBytes, 0, fileSize);
            addParam(key, fileBytes, file.getName(), mime);
        } catch (IOException e) { return; }
    }

    public void addParam(String key, byte[] data, String fileName, String mime) {
        PostData postData = new PostData();
        postData.key = key;
        postData.isBinary = true;
        postData.data = data;
        postData.mime = mime;
        postData.filename = fileName;
        requestParams.add(postData);
    }

    public void addHeader(String headerName, String headerValue) {
        headers.put(headerName, headerValue);
    }

    public void request() {
    	networkRequest.setTimedWorkListener(this);
    	networkRequest.setInterval(timeout);
        networkRequest.work();
    }

    @Override
    public void onTimeout(AsyncTimedWork timedWork) {
        hasTimedOut = true;
        if (conn != null) {
            conn.disconnect();
            sendTimeout();
        }
    }

    @Override
    public void onDone(WorkFlow workFlow, Object object) {}

    @Override
    public void onFailed(WorkFlow workFlow, int reasonCode) {}

    @Override
    public void onFailed(WorkFlow workFlow, Throwable throwable) {}

    private void sendResponse(int responseCode, String mime, byte[] buffer, int bytesRequired) {
        if (buffer != null && buffer.length != bytesRequired) {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            stream.write(buffer, 0, bytesRequired);
            buffer = stream.toByteArray();
        }

        if (responseCode > 0 && responseCode < RESPONSE_FAILURE_THRESHOLD) {
            responseListener.onSuccess(this, mime, buffer);
        } else {
            responseListener.onFailure(this, responseCode, mime, buffer);
        }
    }

	private void sendOffline() {
		if (handlerCallback == null) {
			responseListener.onOffline(this);
		} else {
			Runnable runnableOffline = new Runnable() {
				@Override
				public void run() {
					responseListener.onOffline(HttpRequest.this);
				}
			};
			
			handlerCallback.post(runnableOffline);
		}
	}

    private void sendConnected() {
        if (handlerCallback == null) {
            responseListener.onConnected(this);
        } else {
            Runnable runnableConnected = new Runnable() {
                @Override
                public void run() {
                    responseListener.onConnected(HttpRequest.this);
                }
            };

            handlerCallback.post(runnableConnected);
        }
    }

    private void sendTimeout() {
        if (handlerCallback == null) {
            responseListener.onTimeout(this);
        } else {
            Runnable runnableTimeout = new Runnable() {
                @Override
                public void run() {
                    responseListener.onTimeout(HttpRequest.this);
                }
            };

            handlerCallback.post(runnableTimeout);
        }
    }

    public void cancel() {
        isCancelled = true;
        if (conn != null) { conn.disconnect(); }
    }

    private void cleanup() {
        if (connOutputStream != null) {
            try { connOutputStream.close();} catch (IOException e) {}
            connOutputStream = null;
        }

        if (connInputStream != null) {
            try { connInputStream.close(); } catch (IOException e) {}
            connInputStream = null;
        }

        if (conn != null) {
            conn.disconnect();
            conn = null;
        }
    }

    @Override
    public int compareTo(HttpRequest httpRequest) {
        return (getUrl().equals(httpRequest.getUrl())
            && getRequestCode().equals(httpRequest.getRequestCode()) ? 0 : 1);
    }
}
