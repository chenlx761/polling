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
    val mDetail: MutableLiveData<TicketListBean.RowsDTO> = MutableLiveData()
    override fun getModel(): TicketDetailContract.ITicketDetailModel? {
        return TicketDetailModel()
    }

    override fun getTicketDetail(ticketId: String?) {
        mStartLoadingDialog.postValue(true)
        mModel.getTicketDetail(
            ticketId,
            object :
                BaseCallBack<BaseBean<TicketListBean.RowsDTO>>(HttpConstants.GET_TICKET_DETAIL_URL) {
                override fun onSuccessful(t: BaseBean<TicketListBean.RowsDTO>?) {
                    mStartLoadingDialog.postValue(false)
                    if (t != null) {
                        //设置图片列表
                        val split = t.data.images.split(",").filter { it.isNotEmpty() }
                        val imageList = ArrayList<String>()
                        split.forEach {
                            imageList.add(HttpConstants.BASE_URL + it)
                        }
                        t.data.serverPhotosList = imageList

                        val governmentSplit = t.data.governmentImages
                        governmentSplit.forEach { it ->
                            it.filePath = HttpConstants.BASE_URL + it.filePath
                        }

                        mDetail.postValue(t.data)
                    }
                }


            })
    }

    override fun postTicketDetail(bean: TicketListBean.RowsDTO?) {
        mStartLoadingDialog.postValue(true)
        mModel.postTicketDetail(
            bean, object : BaseCallBack<BaseBean<Objects>>(HttpConstants.EDIT_TICKET_DETAIL_URL) {


                override fun onSuccessful(t: BaseBean<Objects>?) {
                    mStartLoadingDialog.postValue(false)
                    mUpdateFinish.postValue(true)
                }

            })
    }


}