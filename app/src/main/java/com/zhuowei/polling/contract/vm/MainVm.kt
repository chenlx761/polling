package com.zhuowei.polling.contract.vm

import androidx.databinding.ObservableArrayList
import androidx.lifecycle.MutableLiveData
import com.chenming.common.base.BaseViewModel
import com.chenming.httprequest.http.bean.BaseBean
import com.zhuowei.polling.beans.LoginResult
import com.zhuowei.polling.constants.HttpConstants
import com.zhuowei.polling.contract.MainContract.IMainModel
import com.zhuowei.polling.contract.MainContract.IMainVm
import com.zhuowei.polling.contract.model.MainModel
import com.zhuowei.polling.utils.SpManager
import java.util.Objects

class MainVm : BaseViewModel<IMainModel>(), IMainVm {
    val mLoginResult: MutableLiveData<LoginResult> = MutableLiveData()
    val mTicketDatas= ObservableArrayList<String>()

    override fun getModel(): IMainModel {
        return MainModel()
    }

    override fun getTsId(userName: String?, pwd: String?) {
        mModel.getTsId(userName,
            pwd,
            object : BaseCallBack<BaseBean<LoginResult>>(HttpConstants.GET_TS_ID_URL) {
                override fun onSuccessful(t: BaseBean<LoginResult>?) {
                    if (t != null && t.data!=null) {
                        SpManager.setToken(t.data.access_token)
                        SpManager.setRefreshToken(t.data.refresh_token)
                        mLoginResult.postValue(t.data)
                    }
                }
            })
    }

    override fun testGetInfo() {
        mModel.testGetInfo( object : BaseCallBack<BaseBean<Objects>>("") {
            override fun onSuccessful(t: BaseBean<Objects>?) {

            }
        })
    }
}