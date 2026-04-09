package com.zhuowei.polling.base;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import com.chenming.common.base.BaseActivity;
import com.chenming.common.base.BaseViewModel;
import com.chenming.common.dialog.CommonLoadingDialog;
import com.zhuowei.polling.dialog.MyCommonLoadingDialog;

import androidx.annotation.Nullable;
import androidx.databinding.ViewDataBinding;

public abstract class MyBaseActivity<VM extends BaseViewModel, VB extends ViewDataBinding> extends BaseActivity<VM, VB> {

    private MyCommonLoadingDialog mDialogLoading;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //hideNavKey(this);
        //        overridePendingTransition(
        //                com.chenming.common.R.anim.slide_left_in,
        //                com.chenming.common.R.anim.slide_right_out
        //        );
    }


    @Override
    public void finish() {
        super.finish();
        //overridePendingTransition(0, com.chenming.common.R.anim.slide_left_out);
    }


    @Override
    public void showLoading() {
        super.showLoading();
    }

    protected void showLoading(boolean canCancel) {
        showLoading(null, canCancel);
    }

    protected void showLoading(Runnable listener, boolean canCancel) {
        if (mDialogLoading != null && mDialogLoading.isShowing()) {
            return;
        }
        mDialogLoading = new MyCommonLoadingDialog(this, listener, canCancel);
        mDialogLoading.show();
    }


    @Override
    public void dismissDialog() {
        try {
            if (mDialogLoading != null) {
                mDialogLoading.dismiss();
                mDialogLoading = null;
            }
        } catch (Exception e) {

        }
    }



}

