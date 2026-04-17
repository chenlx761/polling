package com.zhuowei.polling.contract.vm

import androidx.lifecycle.MutableLiveData
import com.chenming.common.base.BaseViewModel
import com.chenming.httprequest.http.bean.BaseBean
import com.zhuowei.polling.beans.LoginResult
import com.zhuowei.polling.constants.HttpConstants
import com.zhuowei.polling.contract.LoginContract
import com.zhuowei.polling.contract.model.LoginModel
import com.zhuowei.polling.utils.SpManager

class LoginVm : BaseViewModel<LoginContract.ILoginModel>(), LoginContract.ILoginVm {
    val mLoginResult: MutableLiveData<LoginResult> = MutableLiveData()

    override fun getModel(): LoginContract.ILoginModel {
        return LoginModel()
    }

    override fun getTsId(userName: String?, pwd: String?) {
        mModel.getTsId(
            userName,
            pwd,
            object : BaseCallBack<BaseBean<LoginResult>>(HttpConstants.GET_TS_ID_URL) {
                override fun onSuccessful(t: BaseBean<LoginResult>?) {
                    if (t != null && t.data != null) {
                        SpManager.setToken(t.data.access_token)
                        SpManager.setRefreshToken(t.data.refresh_token)
                        mLoginResult.postValue(t.data)
                    }
                }
            })
    }


}