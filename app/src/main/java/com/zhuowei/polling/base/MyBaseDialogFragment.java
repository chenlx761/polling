package com.zhuowei.polling.base;

import android.os.Build;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import com.chenming.common.base.BaseViewModel;
import com.chenming.common.base.widget.BaseFragmentDialog;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.ViewDataBinding;

public abstract class MyBaseDialogFragment<VM extends BaseViewModel<?>, VB extends ViewDataBinding>
        extends BaseFragmentDialog<VM, VB> {


    protected void fullScreenImmersive(View view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_FULLSCREEN;
            view.setSystemUiVisibility(uiOptions);
        }
    }

    @Override
    public void initView(@Nullable View view) {
        super.initView(view);
        if (getDialog() != null && getDialog().getWindow() != null )
            getDialog().getWindow().getDecorView().setOnSystemUiVisibilityChangeListener(visibility -> {
                // 如果导航栏重新出现（低位标志消失），重新设置沉浸模式
                fullScreenImmersive(getDialog().getWindow().getDecorView());
            });

    }

    @Override
    protected int setDialogWidth() {
        return WindowManager.LayoutParams.MATCH_PARENT;
    }

    @Override
    protected int setDialogHeight() {
        return WindowManager.LayoutParams.MATCH_PARENT;
    }


    @Override
    public void onStart() {
        getDialog().getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
        super.onStart();
        fullScreenImmersive(getDialog().getWindow().getDecorView());
        getDialog().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
    }


}
