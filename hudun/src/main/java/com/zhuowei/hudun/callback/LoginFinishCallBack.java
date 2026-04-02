package com.zhuowei.hudun.callback;

public interface LoginFinishCallBack {

    void onLoginFinish();

    void onLoginError(int errorCode, String errorMsg);
}
