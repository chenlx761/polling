package com.zhuowei.hudun;

import android.app.Application;

public class HuDunApplication {

    private static final HuDunApplication INSTANCE = new HuDunApplication();

    private String mActivationSdp="eyJ0eXBlIjoxLCJzZHBBZGRyZXNzIjoiNjEuMTQyLjI0Ni40IiwiZGVmYXVsdENsb3VkSWQiOiIxNjEwOTU5NTg0NzE1ODcwMjA5IiwicG9ydCI6MTAwODEsImNvZGVXb3JkIjoiMmQ1NDgwZTkiLCJwb3J0V2ViR3dIdHRwcyI6NDQzLCJhY3RpdmF0aW9uTW9kZSI6IjEwMSIsIm1hbmFnZXJOYW1lIjoi6JmO55u+6Zu25L+h5Lu76K6/6Zeu5a6J5YWo57O757ufIiwicG9ydGFsQWRkcmVzcyI6Imh0dHBzOi8vMTcyLjIzLjE1LjEzMi8iLCJlbnByaXNlQ29kZSI6IjIzZGUyYzY0MDBhYWFjOGMifQ==";
    private Application mApplication;

    private HuDunApplication() {
    }

    public static HuDunApplication getInstance() {
        return INSTANCE;
    }

    public void setApplication(Application application) {
        mApplication = application;

    }

    public Application getApplication() {
        return mApplication;
    }

    public String getActivationSdp() {
        return mActivationSdp;
    }

    public void setActivationSdp(String activationSdp) {
        mActivationSdp = activationSdp;
    }
}
