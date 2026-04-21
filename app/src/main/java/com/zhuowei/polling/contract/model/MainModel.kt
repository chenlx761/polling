package com.zhuowei.polling.contract.model

import com.chenming.common.base.BaseModel
import com.chenming.httprequest.http.RetrofitUtil
import com.chenming.httprequest.http.bean.BaseBean
import com.chenming.httprequest.http.listener.OnHttpCallBack
import com.zhuowei.polling.beans.TicketListBean
import com.zhuowei.polling.constants.HttpConstants
import com.zhuowei.polling.contract.MainContract.IMainModel


class MainModel : BaseModel(), IMainModel {
    override fun getTicketList(
        page: Int,
        size: Int,
        callBack: OnHttpCallBack<BaseBean<List<TicketListBean.RowsDTO>?>?>
    ) {
        addDisposable(
            RetrofitUtil.Builder(HttpConstants.GET_TICKET_LIST_URL)
            .addPara("pageNum", page)
            .addPara("pageSize", size)
            .build()
            .getList(TicketListBean.RowsDTO::class.java, callBack)
        )
    }


}