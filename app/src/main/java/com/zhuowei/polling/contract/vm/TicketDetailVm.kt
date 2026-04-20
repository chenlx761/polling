package com.zhuowei.polling.contract.vm

import com.chenming.common.base.BaseViewModel
import com.chenming.httprequest.http.bean.BaseBean
import com.zhuowei.polling.beans.TicketDetail
import com.zhuowei.polling.constants.HttpConstants
import com.zhuowei.polling.contract.TicketDetailContract
import com.zhuowei.polling.contract.model.TicketDetailModel

class TicketDetailVm : BaseViewModel<TicketDetailContract.ITicketDetailModel>(),
    TicketDetailContract.ITicketDetailVm {
    override fun getModel(): TicketDetailContract.ITicketDetailModel? {
        return TicketDetailModel()
    }

    override fun getTicketDetail(ticketID: String?) {
        mModel.getTicketDetail(ticketID,object :BaseCallBack<BaseBean<TicketDetail>>(HttpConstants.GET_TICKET_DETAIL_URL){
            override fun onSuccessful(t: BaseBean<TicketDetail>?) {

            }

        })
    }

    override fun postTicketDetail() {
    }
}