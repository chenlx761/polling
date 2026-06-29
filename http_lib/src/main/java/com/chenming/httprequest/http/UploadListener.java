package com.chenming.httprequest.http;

public interface UploadListener {
    void onRequestProgress(long bytesWritten, long contentLength);
}
