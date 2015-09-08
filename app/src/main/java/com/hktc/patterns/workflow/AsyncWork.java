package com.hktc.patterns.workflow;

import android.os.Handler;
import android.os.HandlerThread;

import java.util.UUID;

/**
 * Created by hari on 9/7/15.
 */
public abstract class AsyncWork extends WorkFlow {
    protected HandlerThread threadBg;
    protected Handler handlerBg;
    protected Handler handlerPostBack;
    protected Object resultObject;

    public AsyncWork() {
        threadBg = new HandlerThread(UUID.randomUUID().toString());
        threadBg.start();
        handlerBg = new Handler(threadBg.getLooper());
    }

    public void setPostBackHandler(Handler handlerPostBack) {
        this.handlerPostBack = handlerPostBack;
    }

    @Override
    public void work() {
        Runnable runnableWork = new Runnable() {
            @Override
            public void run() {
                workAsync();
            }
        };

        handlerBg.post(runnableWork);
    }

    protected void finishWork() {
        if (handlerPostBack == null) {
            listener.onDone(AsyncWork.this, resultObject);
        } else {
            Runnable runnablePostBack = new Runnable() {
                @Override
                public void run() {
                    listener.onDone(AsyncWork.this, resultObject);
                }
            };

            handlerPostBack.post(runnablePostBack);
        }

        threadBg.quit();
    }

    protected void setResult(Object object) { this.resultObject = object; }

    protected void workAsync() {
        finishWork();
    }
}
