package com.chenming.common.base;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.chenming.common.R;
import com.chenming.common.dialog.CommonDialogLoading;
import com.chenming.common.dialog.CommonLoadingDialog;
import com.chenming.common.listener.OnActivityResultListener;
import com.chenming.common.utils.CommApplication;
import com.chenming.common.utils.StatusBarUtil;
import com.chenming.common.utils.ToastUtil;
import com.trello.rxlifecycle2.components.support.RxAppCompatActivity;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;


/**
 * Created by wqx on 2018/4/25.
 */

public abstract class BaseActivity<VM extends BaseViewModel, VB extends ViewDataBinding>
        extends RxAppCompatActivity {

    protected VB mBinding;
    private onDialogDismissListener mOnDialogDismissListener;
    private CommonLoadingDialog mDialogLoading;

    private ActivityResultLauncher<Intent> myActivityLauncher;
    private OnActivityResultListener mOnActivityResultListener;


    protected void setOnDialogDismissListener(onDialogDismissListener onDialogDismissListener) {
        mOnDialogDismissListener = onDialogDismissListener;
    }

    protected interface onDialogDismissListener {
        void onDismiss();
    }


    protected VM mViewModel;

    /**
     * 获取ActivityLauncher
     *
     * @return
     */
    protected ActivityResultLauncher<Intent> getActivityLauncher(OnActivityResultListener onActivityResultListener) {
        mOnActivityResultListener = onActivityResultListener;
        return myActivityLauncher;
    }


    public void showLoading() {
        showLoading(null, true);
    }

    protected void showLoading(boolean canCancel) {
        showLoading(null, canCancel);
    }

    protected void showLoading(Runnable listener, boolean canCancel) {
        if (mDialogLoading != null && mDialogLoading.isShowing()) {
            return;
        }
        mDialogLoading = new CommonLoadingDialog(this, listener, canCancel);
        mDialogLoading.show();
    }


    public void dismissDialog() {
        try {
            if (mDialogLoading != null) {
                mDialogLoading.dismiss();
                mDialogLoading = null;
            }
        } catch (Exception e) {

        }
    }

    /**
     * 设置ContentView
     *
     * @return R.layout.xxx
     */
    protected abstract int getLayoutId();

    /**
     * 初始化View(不使用databinding的时候可以在这里初始化view)
     */
    protected void initView(Bundle savedInstanceState) {

    }


    /**
     * add Listener
     */
    protected abstract void setListener();

    protected void setObserveListener() {

    }

    /**
     * 初始化数据
     */
    protected abstract void initData();

    /**
     * 不需要可以返回null
     *
     * @return
     */
    protected abstract VM initViewModel();

    /**
     * 是不是使用databinding,默认是使用的
     *
     * @return
     */
    protected boolean isUseDataBinding() {
        return true;
    }


    /**
     * 设置数据
     */
    protected abstract void setData();

    /**
     * 重写改方法可以修改状态栏样式(默认是白底黑字)
     * <p>
     * 可以修改成setStatusBarThemeBackground  setStatusBarThemeCustomBackground  setImmerseLayout
     */
    protected void setStatusBarStyle() {
        setStatusBarWhiteBackground();

    }

    protected void setStatusBarWhiteBackground() {

        //这里不能使用 ApplicationContext.getColor(R.color.white);获取
        //垃圾小米4.4系统更改状态栏颜色的API识别不了这个颜色，会导致崩溃
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            StatusBarUtil.setStatusBarMode(this, true, R.color.white_comm);
        }
    }

    /**
     * 设置状态栏透明   沉浸式状态栏
     */
    public void setStatusBarThemeBackground(boolean isTextDark) {
        StatusBarUtil.setStatusBarMode(this, isTextDark, R.color.colorPrimary);
    }


    protected void setStatusBarThemeCustomBackground(boolean isTextDark, int color) {
        StatusBarUtil.setStatusBarMode(this, isTextDark, color);
    }

    protected void setImmerseLayout() {// view为标题栏
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        if (savedInstanceState != null)
            finish();

        super.onCreate(savedInstanceState);


        setStatusBarStyle();

        if (isUseDataBinding()) {
            mBinding = DataBindingUtil.setContentView(this, getLayoutId());
            mBinding.setLifecycleOwner(this);
        } else {
            setContentView(getLayoutId());
        }

        mViewModel = initViewModel();

        initData();

        initView(savedInstanceState);
        setData();
        setObserveListener();
        setListener();

        //用下面这个启动界面会快一点
//        mBinding.getRoot().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                setData();
//                setObserveListener();
//                setListener();
//            }
//        },50);


        initActivityLauncher();

    }

    private void initActivityLauncher() {
        myActivityLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), result -> {

                    //setResult的回调
                    if (result != null && mOnActivityResultListener != null) {
                        mOnActivityResultListener.onActivityResult(result);
                        mOnActivityResultListener = null;
                    }
                });
    }

    protected void initLiveDataListener(BaseViewModel model) {
        if (model != null) {
            //显示等待弹窗的监听
            model.mStartLoadingDialog.observe(this, new Observer<Boolean>() {
                @Override
                public void onChanged(Boolean o) {
                    if (o) {
                        showLoading();
                    } else {
                        dismissDialog();
                    }
                }
            });

            //请求失败的监听
            model.mOnRequestError.observe(this, (Observer<ErrorRequestBean>) o -> {

                if (o.getErrorData() != null) {
                    if (o.isShowErrorHint())
                        showErrorMsg(o.getErrorData().getMsg());
                } else if (o.getThrowable() != null) {
                    if (o.isShowErrorHint())
                        showErrorMsg(o.getThrowable().getMessage());
                }

                onApiRequestError(o.getUrl(), o.getErrorData(), o.getThrowable());
                dismissDialog();
            });
            model.mHintString.observe(this, (Observer<String>) this::showInfo);
            model.mHintStringRes.observe(this, (Observer<Integer>) this::showInfo);
        }
    }


    protected <T extends BaseViewModel> T createViewModel(Class<T> clas) {
        //T viewModel = new ViewModelProvider.AndroidViewModelFactory(getApplication()).create(clas);
        T viewModel = new ViewModelProvider(this).get(clas);
        getLifecycle().addObserver(viewModel);
        initLiveDataListener(viewModel);
        return viewModel;
    }


    protected void onApiRequestError(String requestUrl, Object data, Throwable throwable) {

    }

    protected void showInfo(String info) {
        if (TextUtils.isEmpty(info))
            return;
        ToastUtil.showShortToast(info);
    }

    protected void showInfo(int resId) {
        showInfo(CommApplication.getInstance().getResources().getString(resId));
    }

    protected void showErrorMsg(String msg) {
        ToastUtil.showShortToast(msg);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //ToastUtil.cancel();
        if (mViewModel != null) {
            mViewModel.removeRequest();
            getLifecycle().removeObserver(mViewModel);
        }

        if (myActivityLauncher != null)
            myActivityLauncher.unregister();

        myActivityLauncher = null;
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

    /**
     * 重写 getResource 方法，防止系统字体影响
     */
    @Override
    public Resources getResources() {//禁止app字体大小跟随系统字体大小调节
        Resources resources = super.getResources();
        if (resources != null) {
            Configuration configuration = resources.getConfiguration();
            configuration.fontScale = 1.0f;
            resources.updateConfiguration(configuration, resources.getDisplayMetrics());
        }
        return resources;
    }


    /**
     * 隐藏软键盘
     */
    private void HideSoftInput(IBinder token) {
        if (token != null) {
            InputMethodManager manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            manager.hideSoftInputFromWindow(token, InputMethodManager.HIDE_NOT_ALWAYS);
            hideSoftInputFinish();
        }
    }


    protected void hideSoftInputFinish() {

    }

    /**
     * @param contentId 容器id
     * @param fragment  添加的frgment
     */
    protected void addFragment(int contentId, Fragment fragment) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.add(contentId, fragment);
        fragmentTransaction.commitAllowingStateLoss();
    }

    protected void replaceFragment(int contentId, Fragment fragment) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(contentId, fragment);
        fragmentTransaction.commitAllowingStateLoss();
    }

}
