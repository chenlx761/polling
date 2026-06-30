package com.zhuowei.polling.ui.fragment

import android.app.Activity
import androidx.activity.result.ActivityResult
import com.chenming.common.base.BaseFragment
import com.chenming.common.base.empty.EmptyViewModel
import com.chenming.common.listener.OnActivityResultListener
import com.zhuowei.hudun.HuDunManager
import com.zhuowei.polling.R
import com.zhuowei.polling.databinding.FragmentMainBinding
import com.zhuowei.polling.dialog.LogoutConfirmDialog
import com.zhuowei.polling.ui.activitys.login.LoginActivity
import com.zhuowei.polling.ui.activitys.ticket.TicketDetailActivity
import com.zhuowei.polling.ui.activitys.ticket.TicketListActivity
import com.zhuowei.polling.ui.activitys.ticket.UploadTicketFileActivity

class MainFragment : BaseFragment<EmptyViewModel, FragmentMainBinding>() {

    companion object {
        fun newInstance(): MainFragment {
            return MainFragment()
        }
    }


    override fun getLayoutId(): Int {
        return R.layout.fragment_main
    }

    override fun setListener() {
        mBinding!!.rlTicketList.setOnClickListener {
            TicketListActivity.newInstance(requireActivity())
        }

        mBinding!!.rlTicketAdd.setOnClickListener {
            TicketDetailActivity.newIntent(
                getActivityLauncher(object : OnActivityResultListener {
                    override fun onActivityResult(result: ActivityResult?) {
                        if (result != null && result.resultCode == Activity.RESULT_OK) {

                        }
                    }
                })!!,
                requireActivity(),
                null,
                true
            )
        }

        mBinding!!.rlTicketImport.setOnClickListener {
            UploadTicketFileActivity.newInstance(requireActivity())
        }

        mBinding!!.btnLogout.setOnClickListener {
            showLogoutConfirmDialog()
        }
    }

    override fun initData() {

    }

    override fun setData() {

    }

    private fun showLogoutConfirmDialog() {
        LogoutConfirmDialog(requireContext()) {
            logout()
        }.show()
    }


    private fun logout() {
        HuDunManager.instance.logout()
        HuDunManager.instance.release()
        LoginActivity.newInstance(requireActivity())
        requireActivity().finish()
    }


    override fun initViewModel(): EmptyViewModel {
        return createViewModel(EmptyViewModel::class.java)
    }
}