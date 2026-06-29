package com.chenming.common.base.widget;

import android.app.Dialog;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.chenming.common.R;

import androidx.annotation.NonNull;

public abstract class BaseDialog extends Dialog {

    public BaseDialog(@NonNull Context context) {
        super(context, R.style.dialog_radius);
    }



    /**
     * 获取点击事件
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {

        if (isNeedHintSoftInput())
            if (ev.getAction() == MotionEvent.ACTION_DOWN) {
                View view = getCurrentFocus();
                if (isHideInput(view, ev)) {
                    HideSoftInput(view.getWindowToken());
                    view.clearFocus();
                }
            }
        return super.dispatchTouchEvent(ev);
    }

    /**
     * 隐藏软键盘
     */
    private void HideSoftInput(IBinder token) {
        if (token != null) {
            InputMethodManager manager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            manager.hideSoftInputFromWindow(token, InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    /**
     * 是不是需要点击隐藏键盘(默认需要的)
     *
     * @return
     */
    protected boolean isNeedHintSoftInput() {
        return true;
    }

    /**
     * 判定是否需要隐藏
     */
    private boolean isHideInput(View v, MotionEvent ev) {
        if (v != null && (v instanceof EditText)) {
            int[] l = {0, 0};
            v.getLocationInWindow(l);
            int left = l[0], top = l[1], bottom = top + v.getHeight(), right = left + v.getWidth();
            if (ev.getX() > left && ev.getX() < right && ev.getY() > top && ev.getY() < bottom) {
                return false;
            } else {
                return true;
            }
        }
        return false;
    }


    public BaseDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(setDialogLayout());
        setCanceledOnTouchOutside(setCancelable());
        setCancelable(setCancelable());
        Window window = this.getWindow();
        WindowManager.LayoutParams params = window.getAttributes();
        params.width = setDialogWidth();
        params.height = setDialogHeight();
        window.setAttributes(params);
        window.setGravity(setDialogGravity());


        initView();
        setData();
        initEvent();
    }

    private void fullScreenImmersive(View view) {
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
    public void show() {
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
        super.show();
        fullScreenImmersive(getWindow().getDecorView());
        this.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
    }


    protected boolean setCancelable() {
        return true;
    }

    protected int setDialogGravity() {
        return Gravity.CENTER;
    }

    protected int setDialogWidth() {
        return WindowManager.LayoutParams.WRAP_CONTENT;
    }

    protected int setDialogHeight() {
        return WindowManager.LayoutParams.WRAP_CONTENT;
    }


    protected abstract void initEvent();

    protected abstract void setData();

    protected abstract void initView();

    protected abstract int setDialogLayout();


    @Override
    public void dismiss() {
        super.dismiss();
        if (getContext() != null) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            | View.SYSTEM_UI_FLAG_FULLSCREEN);
        }
    }
}
