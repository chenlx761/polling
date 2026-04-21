package com.zhuowei.polling.contract.vm

import androidx.lifecycle.MutableLiveData
import com.chenming.common.base.BaseViewModel
import com.chenming.httprequest.http.bean.BaseBean
import com.zhuowei.polling.beans.TicketListBean
import com.zhuowei.polling.constants.HttpConstants
import com.zhuowei.polling.contract.TicketDetailContract
import com.zhuowei.polling.contract.model.TicketDetailModel
import java.util.Objects

class TicketDetailVm : BaseViewModel<TicketDetailContract.ITicketDetailModel>(),
    TicketDetailContract.ITicketDetailVm {

    val mUpdateFinish: MutableLiveData<Boolean> = MutableLiveData()
    override fun getModel(): TicketDetailContract.ITicketDetailModel? {
        return TicketDetailModel()
    }

    override fun postTicketDetail(bean: TicketListBean.RowsDTO?) {
        mStartLoadingDialog.postValue(true)
        mModel.postTicketDetail(bean, object : BaseCallBack<BaseBean<Objects>>(HttpConstants.EDIT_TICKET_DETAIL_URL) {


            override fun onSuccessful(t: BaseBean<Objects>?) {
                mStartLoadingDialog.postValue(false)
                mUpdateFinish.postValue(true)
            }

        })
    }


}