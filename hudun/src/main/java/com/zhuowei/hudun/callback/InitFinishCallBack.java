package com.zhuowei.hudun.callback;

public interface InitFinishCallBack {

    void onInitFinish(String hudunBaseUrl);

    void onInitError(int type, int errorCode, String errorMsg);
}
