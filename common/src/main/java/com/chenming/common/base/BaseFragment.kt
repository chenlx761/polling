package com.chenming.common.base


import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.chenming.common.dialog.CommonDialogLoading
import com.chenming.common.dialog.CommonLoadingDialog
import com.chenming.common.listener.OnActivityResultListener
import com.chenming.common.utils.CommApplication
import com.chenming.common.utils.HandlerBackUtil.HandlerBackInterface
import com.chenming.common.utils.HandlerBackUtil.HandlerBackUtil
import com.chenming.common.utils.ToastUtil

/**
 * Created by wqx on 2018/4/25.
 */
abstract class BaseFragment<P : BaseViewModel<*>?, VB : ViewDataBinding?> : Fragment(),
    HandlerBackInterface {
    private var rootView: View? = null
    private var mDialogLoading: CommonLoadingDialog? = null
    private var mOnDialogDismissListener: onDialogDismissListener? = null
    protected var mViewModel: P? = null
    protected var mBinding: VB? = null
    private var myActivityLauncher: ActivityResultLauncher<Intent>? = null
    private var mOnActivityResultListener: OnActivityResultListener? = null
    fun setOnDialogDismissListener(onDialogDismissListener: onDialogDismissListener?) {
        mOnDialogDismissListener = onDialogDismissListener
    }

    interface onDialogDismissListener {
        fun onDismiss()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initActivityLauncher()
    }

    private fun initActivityLauncher() {
        myActivityLauncher = registerForActivityResult(
            StartActivityForResult()
        ) { result: ActivityResult? ->
            mOnActivityResultListener?.onActivityResult(
                result
            )
        }
    }

    /**
     * 获取ActivityLauncher
     *
     * @return
     */
    protected fun getActivityLauncher(onActivityResultListener: OnActivityResultListener):
            ActivityResultLauncher<Intent>? {
        mOnActivityResultListener = onActivityResultListener
        return myActivityLauncher!!
    }

    /**
     * 是不是使用databinding,默认是使用的
     *
     * @return
     */
    protected val isUseDataBinding: Boolean
        protected get() = true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (isUseDataBinding) {
            // 1、对布局需要绑定的内容进行加载
            mBinding = DataBindingUtil.inflate(inflater, getLayoutId(), container, false)
            mBinding?.setLifecycleOwner(this)
            // 2、获取到视图
            rootView = mBinding?.getRoot()
        } else {
            if (rootView == null) {
                rootView = inflater.inflate(getLayoutId(), container, false)
            }
            val parent = rootView!!.parent as ViewGroup
            parent?.removeView(rootView)
        }
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mViewModel = initViewModel()
        initData()
        initView(view)
        setData()
        setListener()
    }

    protected fun <T : BaseViewModel<*>?> createViewModel(clas: Class<T>): T {
        val viewModel = ViewModelProvider(this).get(clas)
        initLiveDataListener(viewModel)
        return viewModel
    }

    /**
     * 监听activity的viewmodel数据的
     *
     * @param clas
     * @param <T>
     * @return
    </T> */
    protected fun <T : BaseViewModel<*>> listenActivityViewModel(clas: Class<T>): T {
        val viewModel = ViewModelProvider(requireActivity()).get(clas)
        initLiveDataListener(viewModel)
        return viewModel
    }

    protected fun initLiveDataListener(model: BaseViewModel<*>?) {
        if (model != null) {
            model.mOnRequestError.observe(this, (Observer { o: ErrorRequestBean ->
                if (o.errorData != null) {
                    showErrorMsg(o.errorData.msg)
                } else if (o.throwable != null) {
                    showErrorMsg(o.throwable.message)
                }
                onApiRequestError(o.url, o.errorData, o.throwable)
                dismissDialog()
            } as Observer<ErrorRequestBean>))
            model.mHintString.observe(this) { info: String? -> showInfo(info) }
            model.mHintStringRes.observe(this) { resId: Int -> showInfo(resId) }
        }
    }

    open fun showInfo(info: String?) {
        if (TextUtils.isEmpty(info)) return
        ToastUtil.showShortToast(info)
    }

    open fun showInfo(resId: Int) {
        ToastUtil.showShortToast(CommApplication.getInstance().resources.getString(resId))
    }

    open fun showErrorMsg(msg: String?) {
        ToastUtil.showShortToast(msg)
    }

    protected fun onApiRequestError(requestUrl: String?, data: Any?, throwable: Throwable?) {}
    override fun onDestroyView() {
        super.onDestroyView()
        if (mDialogLoading != null) {
            mDialogLoading!!.dismiss()
            mDialogLoading = null
        }

        mBinding = null
        if (mViewModel != null) mViewModel!!.removeRequest()
    }

    override fun onDestroy() {
        super.onDestroy()
        myActivityLauncher?.unregister()
    }

    @JvmOverloads
    fun showLoading(listener: Runnable? = null) {
        if (mDialogLoading != null && mDialogLoading!!.isShowing()) {
            return
        }
        mDialogLoading = CommonLoadingDialog(requireActivity(), listener, true)
        mDialogLoading!!.show()
    }

    fun dismissDialog() {
        try {
            if (mDialogLoading != null) {
                mDialogLoading!!.dismiss()
                mDialogLoading = null
            }
        } catch (e: Exception) {

        }
    }

    /**
     * 设置ContentView
     *
     * @return R.layout.xxx
     */
    protected abstract fun getLayoutId(): Int

    /**
     * 初始化View(不用databinding就在这里初始化view)
     */
    open fun initView(view: View?) {}

    /**
     * add Listener
     */
    protected abstract fun setListener()

    /**
     * 初始化数据
     */
    protected abstract fun initData()

    /**
     * 设置数据
     */
    protected abstract fun setData()

    /**
     * 不需要可以返回EmptyViewModel
     *
     * @return
     */
    protected abstract fun initViewModel(): P
    override fun onBackPressed(): Boolean {
        return HandlerBackUtil.handleBackPress(this)
    }

}