package com.zhuowei.polling.contract.model

import com.chenming.common.base.BaseModel
import com.chenming.httprequest.http.bean.BaseBean
import com.chenming.httprequest.http.listener.OnHttpCallBack
import com.zhuowei.polling.beans.TicketDetail
import com.zhuowei.polling.contract.TicketDetailContract
import java.util.Objects

class TicketDetailModel : BaseModel(), TicketDetailContract.ITicketDetailModel {
    override fun getTicketDetail(
        ticketID: String?, callBack: OnHttpCallBack<BaseBean<TicketDetail?>?>?
    ) {

    }

    override fun postTicketDetail(callBack: OnHttpCallBack<BaseBean<Objects?>?>?) {
    }
}