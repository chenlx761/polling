package com.zhuowei.hudun.callback;

public interface StartCallBack {
    void onStartFinish();

    void onStartError(int errorCode, String errorMsg);
}
