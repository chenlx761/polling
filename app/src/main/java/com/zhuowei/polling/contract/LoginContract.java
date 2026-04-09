package com.zhuowei.polling.contract;

import com.chenming.common.base.IBaseModel;
import com.chenming.common.base.IBaseViewModel;
import com.chenming.httprequest.http.listener.OnHttpCallBack;
import com.zhuowei.polling.beans.LoginResult;

import java.util.Objects;

public class LoginContract {

    public interface ILoginVm extends IBaseViewModel {
        void getTsId(String userName, String pwd);

        void testGetInfo();
    }

    /**
     * 逻辑处理层
     */
    public interface ILoginModel extends IBaseModel {
        void getTsId(String userName, String pwd, OnHttpCallBack<LoginResult> callBack);


        void testGetInfo(OnHttpCallBack<Objects> callBack);
    }
}
