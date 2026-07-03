package com.zhuowei.polling.ui.activitys.ticket

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.ActivityResult
import com.chenming.common.base.BaseActivity
import com.chenming.common.base.empty.EmptyViewModel
import com.chenming.common.listener.OnActivityResultListener
import com.zhuowei.polling.R
import com.zhuowei.polling.databinding.ActivityTicketListBinding
import com.zhuowei.polling.ui.fragment.TicketParentFragment

class TicketListActivity : BaseActivity<EmptyViewModel, ActivityTicketListBinding>() {


    companion object {
        fun newInstance(context: Context) {
            val intent = Intent(context, TicketListActivity::class.java)
            context.startActivity(intent)
        }
    }


    override fun getLayoutId(): Int {
        return R.layout.activity_ticket_list
    }

    override fun setListener() {
        mBinding.myTitleBar.setLeftLayoutClickListener {
            finish()
        }
        mBinding.ivAddTicket.setOnClickListener {
//            UploadTicketFileActivity.newInstance(requireActivity())
            TicketDetailActivity.newIntent(
                getActivityLauncher(object : OnActivityResultListener {
                    override fun onActivityResult(result: ActivityResult?) {
                        if (result != null && result.resultCode == Activity.RESULT_OK) {

                        }
                    }
                })!!,
                this@TicketListActivity,
                null,
                true
            )
        }
    }

    override fun initData() {
    }

    override fun initViewModel(): EmptyViewModel? {
        return createViewModel(EmptyViewModel::class.java)
    }

    override fun setData() {
        val ticketFragment = TicketParentFragment.newInstance()
        addFragment(R.id.fragment_container, ticketFragment)
    }
}