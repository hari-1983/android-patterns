package com.hktc.patterns.workflow;

import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

/**
 * Created by hari on 11/7/15.
 */
public abstract class AsyncTimedWork extends AsyncWork {
    public interface TimedWorkListener extends WorkFlowListener {
        public void onTimeout(AsyncTimedWork timedWork);
    }
    
    private static final String TAG = "Patterns/AsyncTimedWork";

    protected long interval;
    protected TimedWorkListener timedWorkListener;
    protected HandlerThread threadTimer;
    protected Handler handlerTimer;
    protected boolean isWorkDone = false;
    protected boolean hasTimedOut = false;

    public AsyncTimedWork() {
        super();
        threadTimer = new HandlerThread("timer");
        threadTimer.start();
        handlerTimer = new Handler(threadTimer.getLooper());
    }

    public void setTimedWorkListener(TimedWorkListener listener) { timedWorkListener = listener; }

    public void setInterval(long interval) { this.interval = interval; }

    public void work() {
        Runnable runnableWork = new Runnable() {
            @Override
            public void run() {
                workAsync();

                if (!hasTimedOut) {
                    isWorkDone = true;

                    if (handlerPostBack == null) {
                        timedWorkListener.onDone(AsyncTimedWork.this, resultObject);
                    } else {
                        Runnable runnablePostBack = new Runnable() {
                            @Override
                            public void run() {
                                timedWorkListener.onDone(AsyncTimedWork.this, resultObject);
                            }
                        };

                        handlerPostBack.post(runnablePostBack);
                    }
                }

                threadBg.quit();
            }
        };

        Log.d(TAG, "Starting work in background");
        handlerBg.post(runnableWork);

        Runnable runnableTimer = new Runnable() {
            @Override
            public void run() {
                if (!isWorkDone) {
                    hasTimedOut = true;

                    if (handlerPostBack == null) {
                        timedWorkListener.onTimeout(AsyncTimedWork.this);
                    } else {
                        Runnable runnableTimeout = new Runnable() {
                            @Override
                            public void run() {
                                timedWorkListener.onTimeout(AsyncTimedWork.this);
                            }
                        };

                        handlerPostBack.post(runnableTimeout);
                    }
                }

                threadTimer.quit();
            }
        };

        handlerTimer.postDelayed(runnableTimer, interval);
    }
}
