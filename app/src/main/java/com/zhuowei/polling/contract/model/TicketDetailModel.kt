package com.zhuowei.polling.contract.model

import com.chenming.common.base.BaseModel
import com.chenming.httprequest.http.RetrofitUtil
import com.chenming.httprequest.http.bean.BaseBean
import com.chenming.httprequest.http.listener.OnHttpCallBack
import com.zhuowei.polling.beans.TicketListBean
import com.zhuowei.polling.constants.HttpConstants
import com.zhuowei.polling.contract.TicketDetailContract
import java.util.Objects

class TicketDetailModel : BaseModel(), TicketDetailContract.ITicketDetailModel {
    override fun getTicketDetail(
        ticketId: String?,
        callBack: OnHttpCallBack<BaseBean<TicketListBean.RowsDTO?>?>?
    ) {
        addDisposable(
            RetrofitUtil.Builder(HttpConstants.GET_TICKET_DETAIL_URL)
                .addPara("id", ticketId)
                .build().get(TicketListBean.RowsDTO::class.java, callBack)
        )
    }

    override fun postTicketDetail(
        bean: TicketListBean.RowsDTO?,
        callBack: OnHttpCallBack<BaseBean<Objects?>?>?
    ) {
        addDisposable(
            RetrofitUtil.Builder(HttpConstants.EDIT_TICKET_DETAIL_URL)
                .addPara(bean)
                .build().putJson(Objects::class.java, callBack)
        )
    }

}