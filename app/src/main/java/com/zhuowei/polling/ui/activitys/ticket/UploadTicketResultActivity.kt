package com.zhuowei.polling.ui.activitys.ticket

import android.content.Context
import android.content.Intent
import android.view.View
import com.chenming.common.base.BaseActivity
import com.chenming.common.base.empty.EmptyViewModel
import com.zhuowei.polling.R
import com.zhuowei.polling.databinding.ActivityUploadTicketResultBinding

class UploadTicketResultActivity : BaseActivity<EmptyViewModel, ActivityUploadTicketResultBinding>() {
    companion object {
        private const val EXTRA_IS_SUCCESS = "extra_is_success"
        private const val EXTRA_ERROR_MESSAGE = "extra_error_message"

        fun newInstance(context: Context, isSuccess: Boolean, errorMessage: String? = null) {
            val intent = Intent(context, UploadTicketResultActivity::class.java).apply {
                putExtra(EXTRA_IS_SUCCESS, isSuccess)
                putExtra(EXTRA_ERROR_MESSAGE, errorMessage)
            }
            context.startActivity(intent)
        }
    }

    private val isSuccess: Boolean by lazy {
        intent?.getBooleanExtra(EXTRA_IS_SUCCESS, true) ?: true
    }

    private val errorMessage: String by lazy {
        intent?.getStringExtra(EXTRA_ERROR_MESSAGE).orEmpty()
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_upload_ticket_result
    }

    override fun setListener() {
        mBinding.myTitleBar.setLeftLayoutClickListener {
            finish()
        }
        mBinding.btnBack.setOnClickListener {
            finish()
        }
        mBinding.btnRetry.setOnClickListener {
            finish()
        }
        mBinding.btnPreview.setOnClickListener {
            UploadResultPreActivity.newInstance(this)
        }
    }

    override fun initData() {
    }

    override fun initViewModel(): EmptyViewModel? {
        return createViewModel(EmptyViewModel::class.java)
    }

    override fun setData() {
        bindResultState()
    }

    private fun bindResultState() {
        mBinding.ivResultIcon.setImageResource(
            if (isSuccess) R.mipmap.import_hint_success else R.mipmap.import_hint_error
        )
        mBinding.tvResultTitle.text = getString(
            if (isSuccess) R.string.upload_ticket_result_success_title else R.string.upload_ticket_result_failed_title
        )
        mBinding.tvResultDesc.visibility = if (isSuccess) View.GONE else View.VISIBLE
        mBinding.btnRetry.visibility = if (isSuccess) View.GONE else View.VISIBLE
        mBinding.clSuccessActions.visibility = if (isSuccess) View.VISIBLE else View.GONE
        if (!isSuccess) {
            mBinding.tvResultDesc.text = errorMessage.ifBlank {
                getString(R.string.upload_ticket_result_failed_default_reason)
            }
        }
    }
}
