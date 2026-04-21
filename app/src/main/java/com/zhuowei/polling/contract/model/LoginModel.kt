package com.zhuowei.polling.contract.model

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


//        if (TextUtils.isEmpty(HttpConstants.HD_BASE_URL)) {
//            callBack?.onRequestError("请先设置虎盾的baseUrl", null)
//            return
//        }

        addDisposable(
            RetrofitUtil.Builder(HttpConstants.GET_TS_ID_URL).addPara("username", userName)
                .addPara("password", "h1rm5WMo1azQ1FQLTtmH9E2GBCAOlrI9cUqJJQ/wOGg=")
//                .addPara("password", AES.getPasswordByEncrypt(userName,pwd))
                .addPara("grant_type", "password")
                .addPara("captchaUUID", "captcha:login_bdb7d162-a170-43aa-8-")
                .addPara("equipmentCoding", "13690777388")
                .addHeader("Host", HttpConstants.BASE_HOST)
//            .addHeader("Authorization","Basic Q0ESUENhZTZkZTQ4XZRlZWQwNThmODUOMDRiZmQZZDVkMmMSNTk1N2UxNmRkNQ==")
                .build().postForm(
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