package com.zhuowei.polling.contract.model

import com.chenming.common.base.BaseModel
import com.chenming.httprequest.XLog
import com.chenming.httprequest.http.RetrofitUtil
import com.chenming.httprequest.http.bean.BaseBean
import com.chenming.httprequest.http.listener.OnHttpCallBack
import com.zhuowei.polling.beans.LoginResult
import com.zhuowei.polling.constants.HttpConstants
import com.zhuowei.polling.contract.MainContract.IMainModel
import java.util.Objects


class MainModel : BaseModel(), IMainModel {
    override fun getTsId(userName: String?, pwd: String?, callBack: OnHttpCallBack<LoginResult>?) {
        addDisposable(
            RetrofitUtil.Builder(HttpConstants.GET_TS_ID_URL)
                .addPara("username", userName)
                .addPara("password", "h1rm5WMo1azQ1FQLTtmH9E2GBCAOlrI9cUqJJQ/wOGg=")
//                .addPara("password", AES.getPasswordByEncrypt(userName,pwd))
                .addPara("grant_type", "password")
                .addPara("captchaUUID", "captcha:login_bdb7d162-a170-43aa-8-")
                .addPara("equipmentCoding", "13690777388")
                .addHeader("Host", HttpConstants.BASE_HOST)
//            .addHeader("Authorization","Basic Q0ESUENhZTZkZTQ4XZRlZWQwNThmODUOMDRiZmQZZDVkMmMSNTk1N2UxNmRkNQ==")
                .build()
                .postForm(LoginResult::class.java, object : OnHttpCallBack<BaseBean<LoginResult>> {
                    override fun onSuccessful(t: BaseBean<LoginResult>?) {
                        callBack?.onSuccessful(t?.data)
                    }

                    override fun onDataError(errorMsg: String?, t: BaseBean<LoginResult>?) {
                        callBack?.onDataError(errorMsg!!, t?.data)
                    }

                    override fun onRequestError(errorMsg: String?, throwable: Throwable?) {
                        callBack?.onRequestError(errorMsg!!, throwable)
                    }

                }, HttpConstants.HD_BASE_URL)
        )
    }

    override fun testGetInfo(callBack: OnHttpCallBack<Objects>?) {
        addDisposable(  RetrofitUtil.Builder("hyt-aqsc/prod-api/api/admin/user/front/info")
            .build()
            .get(Objects::class.java, object : OnHttpCallBack<BaseBean<Objects>> {
                override fun onSuccessful(t: BaseBean<Objects>?) {
                    XLog.e("testGetInfo:" + "onSuccessful")
                }

                override fun onDataError(errorMsg: String?, t: BaseBean<Objects>?) {
                    XLog.e("onDataError:" + "onDataError")
                }

                override fun onRequestError(errorMsg: String?, throwable: Throwable?) {
                    XLog.e("onRequestError:" + "onRequestError")
                }

            }))
    }


}