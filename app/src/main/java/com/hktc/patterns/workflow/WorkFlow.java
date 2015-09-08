package com.hktc.patterns.workflow;

import java.util.HashMap;

/**
 * Created by hari on 9/7/15.
 */
public abstract class WorkFlow {
    public interface WorkFlowListener {
        public void onDone(WorkFlow workFlow, Object object);
        public void onFailed(WorkFlow workFlow, int reasonCode);
        public void onFailed(WorkFlow workFlow, Throwable throwable);
    }

    protected WorkFlowListener listener;
    protected HashMap<String, Object> params;

    protected WorkFlow() {
        params = new HashMap<String, Object>();
    }

    public void setParams(HashMap<String, Object> params) {
        this.params = params;
    }

    public HashMap<String, Object> getParams() {
        return params;
    }

    public void addParam(String key, Object object) {
        params.put(key, object);
    }

    public void removeParam(String key) {
        params.remove(key);
    }

    public void setWorkFlowListener(WorkFlowListener listener) {
        this.listener = listener;
    }

    public void work() {
        workSync();
        listener.onDone(this, null);
    }

    protected void workSync() {}
}
