package com.chenming.common.base.widget

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.ThreadUtils


/**
 *  2021/1/4.
 */
open class BaseDialogFragment : DialogFragment() {

    protected var mDialogLayoutCallback: DialogLayoutCallback? = null
    protected var mDialogCallback: DialogCallback? = null

    protected var mActivity: FragmentActivity? = null
    protected var mContentView: View? = null

    fun init(context: Context, listener: DialogLayoutCallback?): BaseDialogFragment? {
        mActivity = getFragmentActivity(context)
        mDialogLayoutCallback = listener
        return this
    }

    fun init(context: Context, dialogCallback: DialogCallback?): BaseDialogFragment? {
        mActivity = getFragmentActivity(context)
        mDialogCallback = dialogCallback
        return this
    }

    private fun getFragmentActivity(context: Context): FragmentActivity? {
        val activity = ActivityUtils.getActivityByContext(context) ?: return null
        if (activity is FragmentActivity) {
            return activity
        }
        LogUtils.w(context.toString() + "not instanceof FragmentActivity")
        return null
    }

    override fun getTheme(): Int {
        if (mDialogLayoutCallback != null) {
            val theme: Int = mDialogLayoutCallback!!.bindTheme()
            if (theme != View.NO_ID) {
                return theme
            }
        }
        return super.getTheme()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog: Dialog = if (mDialogCallback != null) {
            mDialogCallback!!.bindDialog(mActivity)!!
        } else {
            super.onCreateDialog(savedInstanceState)
        }
        val window: Window = dialog.window!!
        if (mDialogCallback != null) {
            mDialogCallback!!.setWindowStyle(window)
        } else if (mDialogLayoutCallback != null) {
            mDialogLayoutCallback!!.setWindowStyle(window)
        }
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return if (mDialogLayoutCallback != null) {
            inflater.inflate(mDialogLayoutCallback!!.bindLayout(), container, false)
        } else super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        if (mDialogLayoutCallback != null) {
            mDialogLayoutCallback!!.initView(this, view)
            return
        }
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        if (mDialogLayoutCallback != null) {
            mDialogLayoutCallback!!.onCancel(this)
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        if (mDialogLayoutCallback != null) {
            mDialogLayoutCallback!!.onDismiss(this)
        }
    }

    fun show() {
        show(javaClass.simpleName)
    }

    fun show(tag: String?) {
        ThreadUtils.runOnUiThread {
            if (ActivityUtils.isActivityAlive(mActivity)) {
                val fm: FragmentManager = mActivity!!.supportFragmentManager
                val prev: Fragment? = fm.findFragmentByTag(tag)
                if (prev != null) {
                    fm.beginTransaction().remove(prev)
                }
                super@BaseDialogFragment.show(fm, tag)
            }
        }
    }

    override fun dismiss() {
        ThreadUtils.runOnUiThread {
            if (ActivityUtils.isActivityAlive(mActivity)) {
                super@BaseDialogFragment.dismissAllowingStateLoss()
            }
        }
    }


}