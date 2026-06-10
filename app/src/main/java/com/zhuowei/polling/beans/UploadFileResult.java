package com.zhuowei.polling.beans;

import com.chenming.httprequest.http.bean.BaseBean;
import com.google.gson.annotations.SerializedName;

public class UploadFileResult  {


    private String originalFileName;
    private String fileName;
    private String filePath;
    private String url;

    public String getOriginalFileName() {
        return originalFileName;
    }

    public void setOriginalFileName(String originalFileName) {
        this.originalFileName = originalFileName;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }


    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
