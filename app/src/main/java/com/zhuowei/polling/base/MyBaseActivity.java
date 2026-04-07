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

import androidx.annotation.Nullable;
import androidx.databinding.ViewDataBinding;

public abstract class MyBaseActivity<VM extends BaseViewModel, VB extends ViewDataBinding> extends BaseActivity<VM, VB> {


    @Override
    protected void hideSoftInputFinish() {
        hideNavKey(this);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
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




    public static void hideNavKey(Context context) {
        try {
            if (Build.VERSION.SDK_INT > 11 && Build.VERSION.SDK_INT < 19) {
                View v = ((Activity) context).getWindow().getDecorView();
                v.setSystemUiVisibility(View.GONE);
            } else if (Build.VERSION.SDK_INT >= 19) {
                //for new api versions.
                View decorView = ((Activity) context).getWindow().getDecorView();
                int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
                decorView.setSystemUiVisibility(uiOptions);
            }
        } catch (Exception e) {

        }

    }





}

