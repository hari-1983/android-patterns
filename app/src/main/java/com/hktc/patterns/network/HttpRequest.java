package com.hktc.patterns.network;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.TreeMap;

import android.app.DownloadManager;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.util.Log;

import com.hktc.patterns.workflow.AsyncTimedWork;
import com.hktc.patterns.workflow.WorkFlow;

/**
 * Created by hari on 11/7/15.
 */
public class HttpRequest implements AsyncTimedWork.TimedWorkListener {
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

    public enum RequestMime {
        URL_ENCODED("application/x-www-form-urlencoded"),
        MULTIPART("multipart/form-data"),
        JSON("application/json");

        private String mime;
        RequestMime(String mime) { this.mime = mime; }
        public String getMime() { return mime; }
    }

    public enum Header {
        CONTENT_TYPE("Content-Type");

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
    }

    private static final String TAG = "Patterns/HttpRequest";
    private static final int RESPONSE_FAILURE_THRESHOLD = 400;

    private String url;
    private long timeout;
    private ResponseListener responseListener;
    private Handler handlerCallback;
    private Context context;
    private Method method;
    private RequestMime requestMime;
    private Map<String, byte[]> requestParams;

    private byte[] response;
    private HttpURLConnection conn;
    private InputStream connInputStream;
    private OutputStream connOutputStream;
    private boolean hasTimedOut = false;
    private int responseCode;
    private int contentLength;
    private String mime;

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
                
                //TODO: Code to send post data
                
                mime = conn.getContentType();
                contentLength = conn.getContentLength();
                
                Log.d(TAG, "Reply received, Mime: " + mime
            		+ ", content length: " + contentLength);

                responseCode = conn.getResponseCode();
                Log.d(TAG, "Response code: " + responseCode);

                connInputStream = conn.getInputStream();
                final byte[] buffer = new byte[contentLength];
                while (bytesReadSoFar < contentLength) {
                    bytesReadThisCall = connInputStream.read(buffer, bytesReadSoFar
                        , contentLength - bytesReadSoFar);
                    bytesReadSoFar += bytesReadThisCall;
                }

                if (handlerCallback == null) {
                    sendResponse(responseCode, mime, buffer);
                } else {
                    Runnable runnable = new Runnable() {
                        @Override
                        public void run() {
                        sendResponse(responseCode, mime, buffer);
                        }
                    };

                    handlerCallback.post(runnable);
                }
            } catch (IOException e) {
                e.printStackTrace();

                if (!hasTimedOut) {
                    if (handlerCallback == null) {
                        sendResponse(-1, null, null);
                    } else {
                        Runnable runnable = new Runnable() {
                            @Override
                            public void run() {
                            sendResponse(-1, null, null);
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
        requestParams = new TreeMap<String, byte[]>();
    }

    public void setUrl(String url) { this.url = url; }
    public void setTimeout(long timeout) { this.timeout = timeout; }
    public void setResponseListener(ResponseListener listener) { responseListener = listener; }
    public void setCallbackHandler(Handler handler) { handlerCallback = handler; }
    public void setContext(Context context) { this.context = context; }
    public void setMethod(Method method) { this.method = method; }
    public void setRequestMime(RequestMime mime) { this.requestMime = mime; }

    public void addParam(String key, String value) {
        addParam(key, value.getBytes());
    }

    public void addParam(String key, File file) {
        try {
            FileInputStream inputStream = new FileInputStream(file);
            int fileSize = (int) file.length();
            byte[] fileBytes = new byte[fileSize];
            inputStream.read(fileBytes, 0, fileSize);
            addParam(key, fileBytes);
        } catch (IOException e) { return; }
    }

    public void addParam(String key, byte[] data) {
        requestParams.put(key, data);
    }

    public void request() {
    	networkRequest.setTimedWorkListener(this);
    	networkRequest.setInterval(timeout);
        networkRequest.work();
    }

    @Override
    public void onTimeout(AsyncTimedWork timedWork) {
        hasTimedOut = true;
        conn.disconnect();
        sendTimeout();
    }

    @Override
    public void onDone(WorkFlow workFlow, Object object) {}

    @Override
    public void onFailed(WorkFlow workFlow, int reasonCode) {}

    @Override
    public void onFailed(WorkFlow workFlow, Throwable throwable) {}

    private void sendResponse(int responseCode, String mime, byte[] buffer) {
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
}
