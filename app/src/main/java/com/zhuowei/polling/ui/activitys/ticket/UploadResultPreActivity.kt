package com.zhuowei.polling.ui.activitys.ticket

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.View
import androidx.activity.result.ActivityResult
import androidx.databinding.ObservableArrayList
import androidx.recyclerview.widget.LinearLayoutManager
import com.chenming.common.base.BaseActivity
import com.chenming.common.base.empty.EmptyViewModel
import com.chenming.common.listener.OnActivityResultListener
import com.chenming.common.listener.OnItemClickListener
import com.zhuowei.polling.MyApplication
import com.zhuowei.polling.R
import com.zhuowei.polling.adapter.MainOrderAdapter
import com.zhuowei.polling.beans.TicketListBean
import com.zhuowei.polling.databinding.ActivityUploadResultPreBinding

class UploadResultPreActivity : BaseActivity<EmptyViewModel, ActivityUploadResultPreBinding>() {

    companion object {
        fun newInstance(context: Context) {
            val intent = Intent(context, UploadResultPreActivity::class.java)
            context.startActivity(intent)
        }
    }

    private val mPreviewDatas = ObservableArrayList<TicketListBean.RowsDTO>()
    private var mMainOrderAdapter: MainOrderAdapter? = null

    override fun getLayoutId(): Int {
        return R.layout.activity_upload_result_pre
    }

    override fun setListener() {
        mBinding.myTitleBar.setLeftLayoutClickListener {
            finish()
        }

        mMainOrderAdapter?.setOnItemClickListener(object : OnItemClickListener {
            override fun onClick(position: Int, i: Any, view: View) {


                TicketDetailActivity.newIntent(
                    getActivityLauncher(object : OnActivityResultListener {
                        override fun onActivityResult(result: ActivityResult?) {
                            if (result != null && result.resultCode == Activity.RESULT_OK) {
                            }
                        }

                    })!!,
                    this@UploadResultPreActivity,
                    (i as TicketListBean.RowsDTO).id.toString(),
                    false
                )
            }

        })
    }

    override fun initData() {
        mPreviewDatas.clear()
        mPreviewDatas.addAll(MyApplication.getInstance().getUploadTicketUploadResultList())
    }

    override fun initViewModel(): EmptyViewModel? {
        return createViewModel(EmptyViewModel::class.java)
    }

    override fun setData() {
        mMainOrderAdapter = MainOrderAdapter(this, mPreviewDatas)
        mBinding.rvTicket.apply {
            layoutManager = LinearLayoutManager(this@UploadResultPreActivity)
            adapter = mMainOrderAdapter
        }
        bindPreviewState()
    }

    private fun bindPreviewState() {
        mBinding.tvCount.text = getString(R.string.upload_ticket_preview_count, mPreviewDatas.size)
        val isEmpty = mPreviewDatas.isEmpty()
        mBinding.tvEmpty.visibility = if (isEmpty) View.VISIBLE else View.GONE
        mBinding.rvTicket.visibility = if (isEmpty) View.GONE else View.VISIBLE
    }
}
