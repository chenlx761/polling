package com.zhuowei.polling.contract.vm

import android.text.TextUtils
import androidx.lifecycle.MutableLiveData
import com.chenming.common.base.BaseViewModel
import com.chenming.httprequest.http.bean.BaseBean
import com.zhuowei.polling.beans.TicketListBean
import com.zhuowei.polling.beans.UploadFileResult
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
                        val imageList = ArrayList<UploadFileResult>()
                        //设置图片列表
                        if (!TextUtils.isEmpty(t.data.images)) {
                            val split = t.data.images.split(",").filter { it.isNotEmpty() }
                            split.forEach {
                                val uploadFileResult = UploadFileResult()
                                uploadFileResult.filePath = HttpConstants.BASE_URL + it
                                imageList.add(uploadFileResult)
                            }
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

    override fun postTicketDetail(bean: TicketListBean.RowsDTO?, isCreateMode: Boolean) {
        mStartLoadingDialog.postValue(true)
        mModel.postTicketDetail(
            bean,
            isCreateMode,
            object : BaseCallBack<BaseBean<Objects>>(
                if (isCreateMode) HttpConstants.ADD_TICKET_DETAIL_URL
                else HttpConstants.EDIT_TICKET_DETAIL_URL
            ) {


                override fun onSuccessful(t: BaseBean<Objects>?) {
                    mStartLoadingDialog.postValue(false)
                    mUpdateFinish.postValue(true)
                }

            })
    }



}
