package com.zhuowei.polling.contract.model

import android.text.TextUtils
import com.chenming.common.base.BaseModel
import com.chenming.httprequest.http.RetrofitUtil
import com.chenming.httprequest.http.bean.BaseBean
import com.chenming.httprequest.http.listener.OnHttpCallBack
import com.zhuowei.polling.MyApplication
import com.zhuowei.polling.beans.LoginResult
import com.zhuowei.polling.constants.HttpConstants
import com.zhuowei.polling.contract.LoginContract.ILoginModel


class LoginModel : BaseModel(), ILoginModel {
    override fun getTsId(
        userName: String?, pwd: String?, callBack: OnHttpCallBack<BaseBean<LoginResult>>?
    ) {


        if (!MyApplication.getInstance().isLocationTest()) {
            if (TextUtils.isEmpty(HttpConstants.HD_BASE_URL)) {
                callBack?.onRequestError("", Throwable("请先设置虎盾的baseUrl"))
                return
            }

        }

        addDisposable(
            RetrofitUtil.Builder(HttpConstants.GET_TS_ID_URL)
                .addPara("username", userName)
                .addPara("password", pwd)
                .addHeader("Host", HttpConstants.BASE_HOST)
                .build()
                .postJson(
                    LoginResult::class.java, object : OnHttpCallBack<BaseBean<LoginResult>> {
                        override fun onSuccessful(t: BaseBean<LoginResult>?) {
                            callBack?.onSuccessful(t)
                        }

                        override fun onDataError(errorMsg: String?, t: BaseBean<LoginResult>?) {
                            callBack?.onDataError(errorMsg!!, t)
                        }

                        override fun onRequestError(errorMsg: String?, throwable: Throwable?) {
                            callBack?.onRequestError(errorMsg!!, throwable)
                        }

                    }, if (MyApplication.getInstance().isLocationTest()) HttpConstants.BASE_URL
                    else HttpConstants.HD_BASE_URL
                )
        )
    }


}