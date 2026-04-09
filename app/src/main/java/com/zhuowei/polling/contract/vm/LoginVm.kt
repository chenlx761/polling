package com.zhuowei.polling.contract.vm

import androidx.lifecycle.MutableLiveData
import com.chenming.common.base.BaseViewModel
import com.zhuowei.polling.beans.LoginResult
import com.zhuowei.polling.constants.HttpConstants
import com.zhuowei.polling.contract.LoginContract
import com.zhuowei.polling.contract.model.LoginModel
import com.zhuowei.polling.utils.SpManager
import java.util.Objects

class LoginVm : BaseViewModel<LoginContract.ILoginModel>(), LoginContract.ILoginVm {
    val mLoginResult: MutableLiveData<LoginResult> = MutableLiveData()

    override fun getModel(): LoginContract.ILoginModel {
        return LoginModel()
    }

    override fun getTsId(userName: String?, pwd: String?) {
        mModel.getTsId(userName,
            pwd,
            object : BaseCallBack<LoginResult>(HttpConstants.GET_TS_ID_URL) {
                override fun onSuccessful(t: LoginResult?) {
                    if (t != null) {
                        SpManager.setToken(t.access_token)
                        SpManager.setRefreshToken(t.refresh_token)
                        mLoginResult.postValue(t)
                    }
                }
            })
    }

    override fun testGetInfo() {
        mModel.testGetInfo( object : BaseCallBack<Objects>("") {
            override fun onSuccessful(t: Objects?) {

            }
        })
    }
}