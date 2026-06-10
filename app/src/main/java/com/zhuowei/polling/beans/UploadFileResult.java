package com.zhuowei.polling.beans;

import com.chenming.httprequest.http.bean.BaseBean;
import com.google.gson.annotations.SerializedName;

public class UploadFileResult  {


    private String fileName;
    private String filePath;


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

}
