package com.zhuowei.hudun

import android.net.VpnService
import androidx.fragment.app.FragmentActivity
import cn.tigersec.android.sdk.ITVAPI
import cn.tigersec.android.sdk.utils.ActivationType
import cn.tigersec.android.sdk.utils.ErrorCode
import com.chenming.httprequest.XLog
import com.zhuowei.hudun.callback.InitFinishCallBack
import com.zhuowei.hudun.callback.LoginFinishCallBack
import com.zhuowei.hudun.callback.PrepareVpnCallBack
import com.zhuowei.hudun.callback.StartCallBack
import com.zhuowei.hudun.constants.NormalConstant

class HuDunManager private constructor() {


    companion object {
        @JvmStatic
        val instance: HuDunManager = HuDunManager()
    }


    fun initSDK(callBack: InitFinishCallBack) {
        ITVAPI.getMInstance(HuDunApplication.getInstance().application).initSDK { code, message ->

            XLog.e("initSdk" + "code:" + code + "message:" + message)


            if (ErrorCode.SUCCESS == code) {
                ITVAPI.getMInstance(HuDunApplication.getInstance().application).activation(
                    HuDunApplication.getInstance().activationSdp, ActivationType.CODE_ACTIVATION
                ) { code1, message1 ->
                    XLog.e("activation" + "code:" + code1 + "message:" + message1)
                    if (ErrorCode.SUCCESS == code1) {
                        callBack.onInitFinish(message + "/")
                    } else {
                        callBack.onInitError(NormalConstant.ERROR_TYPE_ACTIVATION, code1, message1)
                    }
                }
            } else {
                callBack.onInitError(NormalConstant.ERROR_TYPE_INIT, code, message)
            }

        }
    }

    fun login(tsid: String, callBack: LoginFinishCallBack, isDebug: Boolean = false) {
        val macOrSn = ITVAPI.getMInstance(HuDunApplication.getInstance().application).getMacOrSn()
        XLog.e("macOrSn:" + macOrSn)
        ITVAPI.getMInstance(HuDunApplication.getInstance().application).loginUser(
            tsid, { code, msg ->
                XLog.e("loginUser   " + "code:" + code + "     message:" + msg)
                if (ErrorCode.SUCCESS == code) {
                    callBack.onLoginFinish()
                } else {
                    callBack.onLoginError(code, msg)
                }
            }, isDebug
        )
    }


    //申请/检查VPN启动权限
    fun prepareVpn(activity: FragmentActivity, callBack: PrepareVpnCallBack?) {
        val intent = VpnService.prepare(activity)
        if (null != intent) {
            activity.startActivityForResult(intent, ErrorCode.REQUEST_START_VPN)
        } else {
            callBack?.onPrepareIntentNull()
        }
    }

    fun start(callBack: StartCallBack?) {
        ITVAPI.getMInstance(HuDunApplication.getInstance().application).start { code, message ->
            XLog.e("start  " + "code" + code + ":messgae" + message)
            if (ErrorCode.SUCCESS == code || ErrorCode.SOME_ROUTE_OR_CLOUD_ABNORMAL==code) {
                callBack?.onStartFinish()
            } else {
                callBack?.onStartError(code, message)
            }
        }
    }


    fun release(){
        ITVAPI.getMInstance(HuDunApplication.getInstance().application).release()
    }
}
