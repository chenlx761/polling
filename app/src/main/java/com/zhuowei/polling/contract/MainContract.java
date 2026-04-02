package com.zhuowei.polling.contract;

import com.chenming.common.base.IBaseModel;
import com.chenming.common.base.IBaseViewModel;
import com.chenming.httprequest.http.listener.OnHttpCallBack;
import com.zhuowei.polling.beans.LoginResult;

import java.util.List;

public class MainContract {

    public interface IMainVm extends IBaseViewModel {
        void getTsId(String userName, String pwd);


    }

    /**
     * 逻辑处理层
     */
    public interface IMainModel extends IBaseModel {
        void getTsId(String userName, String pwd, OnHttpCallBack<LoginResult> callBack);

    }
}
