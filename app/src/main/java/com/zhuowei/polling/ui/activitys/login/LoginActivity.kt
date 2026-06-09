package com.zhuowei.polling.ui.activitys.login

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import android.text.TextUtils
import cn.tigersec.android.sdk.utils.ErrorCode
import com.chenming.common.utils.ToastUtil
import com.tencent.bugly.crashreport.CrashReport
import com.zhuowei.hudun.HuDunManager
import com.zhuowei.hudun.callback.InitFinishCallBack
import com.zhuowei.hudun.callback.LoginFinishCallBack
import com.zhuowei.hudun.callback.PrepareVpnCallBack
import com.zhuowei.hudun.callback.StartCallBack
import com.zhuowei.polling.BuildConfig
import com.zhuowei.polling.MainActivity
import com.zhuowei.polling.MyApplication
import com.zhuowei.polling.R
import com.zhuowei.polling.base.MyBaseActivity
import com.zhuowei.polling.constants.HttpConstants
import com.zhuowei.polling.contract.vm.LoginVm
import com.zhuowei.polling.databinding.ActivityLoginBinding
import com.zhuowei.polling.utils.AppCrashHandleCallback
import com.zhuowei.polling.utils.SpManager

class LoginActivity : MyBaseActivity<LoginVm, ActivityLoginBinding>() {


    companion object {
        fun newInstance(context: Context) {
            val intent = Intent(context, LoginActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun setStatusBarStyle() {

        setStatusBarThemeBackground(true)
    }


    override fun getLayoutId(): Int {
        return R.layout.activity_login
    }


    private fun requestFilePermission(){

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                intent.setData(Uri.parse("package:" + getPackageName()))
                startActivityForResult(intent, 100)
            }
        }
    }

    override fun setListener() {
        requestFilePermission()

        mBinding.btnLogin.setOnClickListener {
            if (mBinding.etUsername.text.isNullOrEmpty() || mBinding.etPassword.text.isNullOrEmpty()) {
                showInfo(R.string.username_or_password_empty)
                return@setOnClickListener
            }


            showLoading()
            SpManager.setUserName(mBinding.etUsername.text.toString())
            if (mBinding.cbRemember.isChecked) {
                SpManager.setUserPwd(mBinding.etPassword.text.toString())
            } else {
                SpManager.setUserPwd("")
            }
            SpManager.setUserRemember(mBinding.cbRemember.isChecked)

            mViewModel!!.getTsId(
                mBinding.etUsername.text.toString(), mBinding.etPassword.text.toString()
            )
        }
    }

    override fun setObserveListener() {
        mViewModel.mLoginResult.observe(this) {
            if (it != null) {

                if (MyApplication.getInstance().isLocationTest()) {
                    dismissDialog()
                    MainActivity.newInstance(this@LoginActivity)
                    finish()
                    return@observe
                }

                HuDunManager.instance.login(it.tsid, object : LoginFinishCallBack {
                    override fun onLoginFinish() {
                        HuDunManager.instance.prepareVpn(
                            this@LoginActivity,
                            object : PrepareVpnCallBack {
                                override fun onPrepareIntentNull() {
                                    onActivityResult(
                                        ErrorCode.REQUEST_START_VPN, RESULT_OK, null
                                    )
                                }

                            })
                    }

                    override fun onLoginError(errorCode: Int, errorMsg: String?) {
                    }

                })
            }
        }
    }

    override fun initData() {
    }

    override fun initViewModel(): LoginVm {
        return createViewModel(LoginVm::class.java)
    }

    override fun setData() {

        if (BuildConfig.DEBUG) {
            mBinding.etUsername.setText("admin")
            mBinding.etPassword.setText("admin123")
        }
        mBinding.cbRemember.isChecked = SpManager.getUserRemember()
        if (!TextUtils.isEmpty(SpManager.getUserName())) {
            mBinding.etUsername.setText(SpManager.getUserName())
        }
        if (!TextUtils.isEmpty(SpManager.getUserPwd())) {
            mBinding.etPassword.setText(SpManager.getUserPwd())
        }

        initHunDun()
    }

    private fun initHunDun(){
        showLoading()
        HuDunManager.instance.initSDK(object : InitFinishCallBack {
            override fun onInitFinish(hudunBaseUrl: String?) {
                dismissDialog()
                HttpConstants.HD_BASE_URL = hudunBaseUrl
                //虎盾初始化不知道为什么会把我的全部异常拦截覆盖了,只能在这里初始化
                initCrash()
            }

            override fun onInitError(type: Int, errorCode: Int, errorMsg: String?) {
                dismissDialog()
                ToastUtil.showShortToast(errorMsg)
            }

        })
    }

    private fun initCrash() {
        val strategy = CrashReport.UserStrategy(this)
        strategy.setAppReportDelay(20000)
        strategy.setCrashHandleCallback(AppCrashHandleCallback())
        CrashReport.initCrashReport(MyApplication.getInstance(), "37dc0727ad", false, strategy)
    }


    /**
     * 用于判断VPN服务权限有没有申请成功如果申请成功则调用start接口
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ErrorCode.REQUEST_START_VPN) {
            if (resultCode == RESULT_OK) {
                HuDunManager.instance.start(object : StartCallBack {
                    override fun onStartFinish() {
                        dismissDialog()


                        MainActivity.newInstance(this@LoginActivity)
                        finish()
                    }

                    override fun onStartError(errorCode: Int, errorMsg: String?) {
                        // 2020/9/16 错误处理
                        ToastUtil.showShortToast(errorMsg)
                        dismissDialog()
                    }

                })
            } else {

            }
        }
    }
}