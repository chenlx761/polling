package com.zhuowei.polling.contract.vm

import androidx.lifecycle.MutableLiveData
import com.chenming.common.base.BaseViewModel
import com.zhuowei.polling.beans.LoginResult
import com.zhuowei.polling.constants.HttpConstants
import com.zhuowei.polling.contract.MainContract.IMainModel
import com.zhuowei.polling.contract.MainContract.IMainVm
import com.zhuowei.polling.contract.model.MainModel
import com.zhuowei.polling.utils.SpManager

class MainVm : BaseViewModel<IMainModel>(), IMainVm {
    val mLoginResult: MutableLiveData<LoginResult> = MutableLiveData()

    override fun getModel(): IMainModel {
        return MainModel()
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
}