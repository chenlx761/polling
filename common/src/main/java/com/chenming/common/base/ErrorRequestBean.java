package com.chenming.common.base;


import com.chenming.httprequest.http.bean.BaseBean;

public class ErrorRequestBean {
    private String url;
    private BaseBean errorData;
    private Throwable throwable;
    private boolean isShowErrorHint;

    public ErrorRequestBean() {
    }

    public ErrorRequestBean(String url, BaseBean errorData, Throwable throwable, boolean isShowErrorHint) {
        this.url = url;
        this.errorData = errorData;
        this.throwable = throwable;
        this.isShowErrorHint = isShowErrorHint;
    }

    public boolean isShowErrorHint() {
        return isShowErrorHint;
    }

    public void setShowErrorHint(boolean showErrorHint) {
        isShowErrorHint = showErrorHint;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public BaseBean getErrorData() {
        return errorData;
    }

    public void setErrorData(BaseBean errorData) {
        this.errorData = errorData;
    }

    public Throwable getThrowable() {
        return throwable;
    }

    public void setThrowable(Throwable throwable) {
        this.throwable = throwable;
    }

    @Override
    public String toString() {
        return "ErrorRequestBean{" +
                "url='" + url + '\'' +
                ", errorData=" + errorData +
                '}';
    }
}
