package com.zhuowei.polling.contract.vm

import androidx.databinding.ObservableArrayList
import androidx.lifecycle.MutableLiveData
import com.chenming.common.base.BaseViewModel
import com.chenming.httprequest.http.bean.BaseBean
import com.zhuowei.polling.beans.TicketListBean
import com.zhuowei.polling.constants.HttpConstants
import com.zhuowei.polling.contract.MainContract.IMainModel
import com.zhuowei.polling.contract.MainContract.IMainVm
import com.zhuowei.polling.contract.model.MainModel

class MainVm : BaseViewModel<IMainModel>(), IMainVm {
    private var mCurPage = 1
    private val mPageSize = 10
    val mTicketDatas = ObservableArrayList<TicketListBean.RowsDTO>()
    val mStopFlash: MutableLiveData<Boolean> = MutableLiveData()
    val mStopLoadMore: MutableLiveData<Boolean> = MutableLiveData()
    override fun getModel(): IMainModel {
        return MainModel()
    }


    override fun flashTicketList() {

        mCurPage = 1
        mModel.getTicketList(
            mCurPage,
            mPageSize,
            object :
                BaseCallBack<BaseBean<List<TicketListBean.RowsDTO>>>(HttpConstants.GET_TICKET_LIST_URL) {
                override fun onSuccessful(t: BaseBean<List<TicketListBean.RowsDTO>>?) {
                    mStopFlash.postValue(true)
                    if (t != null && t.data != null) {
                        mTicketDatas.clear()
                        mTicketDatas.addAll(t.data)
                    }
                }

                override fun onRequestError(errorMsg: String?, throwable: Throwable?) {
                    super.onRequestError(errorMsg, throwable)
                    mStopFlash.postValue(true)
                }

                override fun onDataError(
                    errorMsg: String?,
                    t: BaseBean<List<TicketListBean.RowsDTO>>?
                ) {
                    super.onDataError(errorMsg, t)
                    mStopFlash.postValue(true)

                }
            })
    }

    override fun loadMoreTicketList() {
        mCurPage++
        mModel.getTicketList(
            mCurPage,
            mPageSize,
            object :
                BaseCallBack<BaseBean<List<TicketListBean.RowsDTO>>>(HttpConstants.GET_TICKET_LIST_URL) {

                override fun onSuccessful(t: BaseBean<List<TicketListBean.RowsDTO>>?) {
                    mStopLoadMore.postValue(true)
                    if (t != null && t.data != null) {
                        mTicketDatas.addAll(t.data)
                    }
                }

                override fun onRequestError(errorMsg: String?, throwable: Throwable?) {
                    super.onRequestError(errorMsg, throwable)
                    mStopLoadMore.postValue(true)
                }

                override fun onDataError(
                    errorMsg: String?,
                    t: BaseBean<List<TicketListBean.RowsDTO>>?
                ) {
                    super.onDataError(errorMsg, t)
                    mStopLoadMore.postValue(true)

                }
            })
    }
}