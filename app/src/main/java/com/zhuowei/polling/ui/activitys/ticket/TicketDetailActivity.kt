package com.zhuowei.polling.ui.activitys.ticket

import android.content.Context
import android.content.Intent
import androidx.databinding.ObservableArrayList
import androidx.recyclerview.widget.GridLayoutManager
import com.chenming.common.base.BaseActivity
import com.chenming.common.base.empty.EmptyViewModel
import com.zhuowei.polling.R
import com.zhuowei.polling.adapter.AddPhotoAdapter
import com.zhuowei.polling.databinding.ActivityTicketDetailBinding

class TicketDetailActivity : BaseActivity<EmptyViewModel, ActivityTicketDetailBinding>() {
    private val mAllPhotos = ObservableArrayList<String>()
    private var mAddPhotoAdapter: AddPhotoAdapter? = null

    companion object {
        fun newInstance(context: Context) {
            val intent = Intent(context, TicketDetailActivity::class.java)
            context.startActivity(intent)
        }
    }


    override fun getLayoutId(): Int {
        return R.layout.activity_ticket_detail
    }

    override fun setListener() {
        mBinding!!.myTitleBar.setLeftLayoutClickListener {
            finish()
        }
    }

    override fun initData() {
    }

    override fun initViewModel(): EmptyViewModel {
        return createViewModel(EmptyViewModel::class.java)
    }

    override fun setData() {
        mAddPhotoAdapter = AddPhotoAdapter(this, mAllPhotos)
        mBinding!!.rvPhoto.apply {
            adapter = mAddPhotoAdapter
            layoutManager = GridLayoutManager(this@TicketDetailActivity, 3)
        }
    }
}