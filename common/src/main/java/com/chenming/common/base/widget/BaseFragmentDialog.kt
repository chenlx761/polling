package com.chenming.common.base.widget

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.chenming.common.R
import com.chenming.common.base.BaseViewModel
import com.chenming.common.base.ErrorRequestBean
import com.chenming.common.dialog.CommonDialogLoading
import com.chenming.common.utils.CommApplication
import com.chenming.common.utils.ToastUtil
import com.chenming.httprequest.XLog

abstract class BaseFragmentDialog<P : BaseViewModel<*>?, VB : ViewDataBinding?> : DialogFragment() {
    // 窗口配置参数
    protected var mViewModel: P? = null
    protected var mBinding: VB? = null
    protected var rootView: View? = null
    private var mDialogLoading: CommonDialogLoading? = null
    private var mDialogDismissListener: DialogDismissListener? = null

    public interface DialogDismissListener {
        fun onDialogDismissed();
    }

    fun setDialogDismissListener(listener: DialogDismissListener) {
        mDialogDismissListener = listener
    }

    /**
     * 是不是使用databinding,默认是使用的
     *
     * @return
     */
    protected val isUseDataBinding: Boolean
        protected get() = true

    protected fun <T : BaseViewModel<*>?> createViewModel(clas: Class<T>): T {
        val viewModel = ViewModelProvider(this).get(clas)
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

    open fun showLoading(listener: Runnable? = null) {
        if (mDialogLoading != null) {
            return
        }
        mDialogLoading = CommonDialogLoading().init(activity, listener)
        mDialogLoading!!.show()
    }

    open fun dismissDialog() {
        if (mDialogLoading != null) {
            mDialogLoading!!.dismiss()
            mDialogLoading = null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, R.style.dialog_radius)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        dialog?.apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setCanceledOnTouchOutside(false)
        }
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
        // 添加布局监听（保持现有代码）
        mViewModel = initViewModel()
        initData()
        initView(view)
        setData()
        setListener()

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
    open fun initView(view: View?) {

    }

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


    fun show(manager: FragmentManager) {
        show(manager, javaClass.simpleName)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return object : Dialog(requireContext(), theme) {
            override fun dispatchTouchEvent(event: MotionEvent): Boolean {
                myDispatchTouchEvent(event)
                return super.dispatchTouchEvent(event)
            }

        }
    }

    open fun myDispatchTouchEvent(event: MotionEvent) {

    }

    override fun onStart() {
        dialog?.window?.apply {
            attributes = attributes.apply {
                gravity = setDialogGravity()
                width = setDialogWidth()
                height = setDialogHeight()
            }
            setLayout(setDialogWidth(), setDialogHeight())
        }
        super.onStart()
        this.dialog?.window?.clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE)
    }

    protected fun setDialogGravity(): Int {
        return Gravity.CENTER
    }

    protected open fun setDialogWidth(): Int {
        return WindowManager.LayoutParams.WRAP_CONTENT
    }

    protected open fun setDialogHeight(): Int {
        return WindowManager.LayoutParams.WRAP_CONTENT
    }

    override fun show(manager: FragmentManager, tag: String?) {
        try {
            val c = Class.forName("androidx.fragment.app.DialogFragment")
            val con = c.getConstructor()
            val obj = con.newInstance()
            val dismissed = c.getDeclaredField("mDismissed")
            dismissed.isAccessible = true
            dismissed.set(obj, false)
            val shownByMe = c.getDeclaredField("mShownByMe")
            shownByMe.isAccessible = true
            shownByMe.set(obj, false)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val ft = manager.beginTransaction()
        ft.add(this, tag)
        ft.commitAllowingStateLoss()
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        if (mDialogDismissListener != null)
            mDialogDismissListener!!.onDialogDismissed()
    }

}


