package com.chenming.common.base;

import android.util.Log;

import com.chenming.httprequest.http.bean.BaseBean;
import com.chenming.httprequest.http.listener.OnHttpCallBack;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ViewModel;


public abstract class BaseViewModel<M extends IBaseModel> extends ViewModel
        implements IBaseViewModel, LifecycleObserver {


    //---------------错误相关开始-------------------
    public MutableLiveData<ErrorRequestBean> mOnRequestError = new MutableLiveData<>();
    public MutableLiveData<String> mHintString = new MutableLiveData<>();
    public MutableLiveData<Integer> mHintStringRes = new MutableLiveData<>();
    //---------------错误相关结束-------------------
    public MutableLiveData<Boolean> mStartLoadingDialog = new MutableLiveData<>();


    protected M mModel;


    public BaseViewModel() {
        this.mModel = getModel();
    }

    protected abstract M getModel();

    @Override
    public void clearRequest() {
        mModel.clearRequest();
    }

    @Override
    public void removeRequest() {
        mModel.removeRequest();
    }


    public void apiRequestError(String errorUrl, BaseBean errorBean, boolean isShowErrorHint, Throwable throwable) {
        ErrorRequestBean errorRequestBean = new ErrorRequestBean(errorUrl, errorBean, throwable, isShowErrorHint);
        mOnRequestError.postValue(errorRequestBean);
    }


    public abstract class BaseCallBack<T extends BaseBean> implements OnHttpCallBack<T> {
        private String requestUrl;
        private boolean mIsShowErrorHint;

        public String getRequestUrl() {
            return requestUrl;
        }

        public BaseCallBack(String url) {
            this(url, true);
        }

        public BaseCallBack(String url, boolean isShowErrorHint) {
            this.requestUrl = url;
            this.mIsShowErrorHint = isShowErrorHint;
        }

        @Override
        public abstract void onSuccessful(T t);

        @Override
        public void onDataError(String errorMsg, T t) {
            mStartLoadingDialog.postValue(false);
            apiRequestError(requestUrl, t, mIsShowErrorHint, null);
        }

        @Override
        public void onRequestError(String errorMsg, Throwable throwable) {
            mStartLoadingDialog.postValue(false);
            apiRequestError(requestUrl, null, mIsShowErrorHint, throwable);
        }
    }


    //--------------------------生命周期相关-----------------------
    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    public final void onActivityCreate() {
        onCreate();
    }


    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    public final void onActivityStart() {
        onStart();
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    public final void onActivityResume() {
        onResume();
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    public final void onActivityPause() {
        onPause();
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    public final void onActivityStop() {
        onStop();
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    public final void onActivityDestroy() {
        onDestroy();
    }


    public void onCreate() {

    }

    public void onStart() {

    }

    public void onResume() {

    }

    public void onPause() {

    }

    public void onStop() {

    }

    public void onDestroy() {
        removeRequest();
    }

    //--------------------------生命周期相关-----------------------
}
