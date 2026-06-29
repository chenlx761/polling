package com.chenming.common.utils;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.CycleInterpolator;
import android.view.animation.LayoutAnimationController;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.chenming.httprequest.XLog;

/**
 * Created by Administrator on 2017/6/9 0009.
 */
public class AnimationUtil {

    private final String TAG = "AnimationUtil";

    public LayoutAnimationController getAnimationController(int time) {
        LayoutAnimationController controller;
// AnimationSet set = new AnimationSet(true);
        Animation anim = new ScaleAnimation(1.0f, 1.0f, 1.0f, 1.0f,
                Animation.RELATIVE_TO_SELF, 1.0f, Animation.RELATIVE_TO_SELF, 0.0f);//
        anim.setDuration(time);
        controller = new LayoutAnimationController(anim, 0.1f);
        controller.setOrder(LayoutAnimationController.ORDER_NORMAL);
        return controller;
    }

    public LayoutAnimationController getAnimationController2(int time) {
        LayoutAnimationController controller;
// AnimationSet set = new AnimationSet(true);
        Animation anim = new ScaleAnimation(1.0f, 1.0f, 1.0f, 1.0f,
                Animation.RELATIVE_TO_SELF, 1.0f, Animation.RELATIVE_TO_SELF, 0.0f);//
        anim.setDuration(time);
        controller = new LayoutAnimationController(anim, 0.1f);
        controller.setOrder(LayoutAnimationController.ORDER_NORMAL);
        return controller;
    }

    public LayoutAnimationController getAnimationController3(int time) {
        LayoutAnimationController controller;
// AnimationSet set = new AnimationSet(true);
        Animation anim = new ScaleAnimation(2.0f, 1.0f, 2.0f, 1.0f,
                Animation.RELATIVE_TO_SELF, 1.0f, Animation.RELATIVE_TO_SELF, 0.0f);//
        anim.setDuration(time);
        controller = new LayoutAnimationController(anim, 0.1f);
        controller.setOrder(LayoutAnimationController.ORDER_NORMAL);
        return controller;
    }

    public LayoutAnimationController getAnimationController4(int time) {
        LayoutAnimationController controller;
// AnimationSet set = new AnimationSet(true);
        Animation anim = new ScaleAnimation(0.5f, 1.0f, 0.5f, 1.0f,
                Animation.RELATIVE_TO_SELF, 1.0f, Animation.RELATIVE_TO_SELF, 0.0f);//
        anim.setDuration(time);
        controller = new LayoutAnimationController(anim, 0.1f);
        controller.setOrder(LayoutAnimationController.ORDER_NORMAL);
        return controller;
    }


    /**
     * 属性动画，重复缩放效果
     */
    private AnimatorSet set;

    /**
     * 从控件所在位置移动到控件的下部
     *
     * @return
     */
    public TranslateAnimation moveToViewBottom(int time) {
        TranslateAnimation mHiddenAction = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF,
                0.0f, Animation.RELATIVE_TO_SELF, 1.0f);
        mHiddenAction.setDuration(time);
        return mHiddenAction;
    }

    /**
     * 从控件的下部移动到控件所在位置
     *
     * @return
     */
    public TranslateAnimation moveToViewLocation(long time) {
        TranslateAnimation mHiddenAction = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF,
                1.0f, Animation.RELATIVE_TO_SELF, 0.0f);
        mHiddenAction.setDuration(time);
        return mHiddenAction;
    }

    /**
     * 从右边进入
     */
    public TranslateAnimation moveRightToViewLocation(float from, float to, int durationTime) {
        TranslateAnimation mHiddenAction = new TranslateAnimation(Animation.RELATIVE_TO_SELF, from,
                Animation.RELATIVE_TO_SELF, to, Animation.RELATIVE_TO_SELF,
                0.0f, Animation.RELATIVE_TO_SELF, 0.0f);
        mHiddenAction.setDuration(durationTime);
        return mHiddenAction;
    }

    /**
     * 从左边进入
     */
    public TranslateAnimation moveLeftToViewLocation(float from, float to, int durationTime) {
        TranslateAnimation mHiddenAction = new TranslateAnimation(Animation.RELATIVE_TO_SELF, from,
                Animation.RELATIVE_TO_SELF, to, Animation.RELATIVE_TO_SELF,
                0.0f, Animation.RELATIVE_TO_SELF, 0.0f);
        mHiddenAction.setDuration(durationTime);
        return mHiddenAction;
    }

    /**
     * 从下部进入
     */
    public TranslateAnimation moveBottomToViewLocation(float down, float up, int time) {
        TranslateAnimation mHiddenAction = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0,
                Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF,
                down, Animation.RELATIVE_TO_SELF, up);
        mHiddenAction.setDuration(time);
        return mHiddenAction;
    }

    /**
     * 左移
     */
    public TranslateAnimation moveOutToLeft(float from, float to, int durationTime) {
        TranslateAnimation mHiddenAction = new TranslateAnimation(Animation.RELATIVE_TO_SELF, from,
                Animation.RELATIVE_TO_SELF, to, Animation.RELATIVE_TO_SELF,
                0.0f, Animation.RELATIVE_TO_SELF, 0.0f);
        mHiddenAction.setDuration(durationTime);
        mHiddenAction.setFillAfter(true);
        return mHiddenAction;
    }

    /**
     * 右移
     */
    public TranslateAnimation moveOutToRight(float left, float right, int durationTime) {
        TranslateAnimation mHiddenAction = new TranslateAnimation(Animation.RELATIVE_TO_SELF, left,
                Animation.RELATIVE_TO_SELF, right, Animation.RELATIVE_TO_SELF,
                0.0f, Animation.RELATIVE_TO_SELF, 0.0f);
        mHiddenAction.setDuration(durationTime);
        mHiddenAction.setFillAfter(true);
        return mHiddenAction;
    }

    /**
     * 渐变消失
     */
    public AlphaAnimation alphaAnimation(int durationTime) {
        AlphaAnimation animation = new AlphaAnimation(1, 0);
        animation.setDuration(durationTime);
        return animation;
    }

    public void alphaAnimation_show(View view, int durationTime) {
        view.setVisibility(View.VISIBLE);
        AlphaAnimation animation = new AlphaAnimation(0, 1);
        animation.setDuration(durationTime);
        view.startAnimation(animation);
    }

    public void alphaAnimation_dismiss(View view, int durationTime) {
        view.setVisibility(View.GONE);
        AlphaAnimation animation = new AlphaAnimation(1, 0);
        animation.setDuration(durationTime);
        view.startAnimation(animation);
    }

    /**
     * 渐变消失加上移
     */
    public AnimationSet alpha_moveupAnimation(int durationTime) {
        AlphaAnimation alphaAnimation = new AlphaAnimation(1, 0);
        TranslateAnimation translateAnimation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0,
                Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF,
                0.0f, Animation.RELATIVE_TO_SELF, -1.0f);
        AnimationSet animationSet = new AnimationSet(true);
        animationSet.addAnimation(alphaAnimation);
        animationSet.addAnimation(translateAnimation);
        animationSet.setDuration(durationTime);
        return animationSet;
    }

    public void alpha_0_1_0(final View view, int durationTime) {
        view.setVisibility(View.VISIBLE);
        ObjectAnimator animator = ObjectAnimator.ofFloat(view, "alpha", 0, 1, 0);
        animator.setDuration(durationTime);
        animator.start();

        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                view.setVisibility(View.GONE);
            }
        });
    }

    /**
     * Y轴移动
     */
    public void moveY(final View view, float from_value, float to_value, int time) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(view, "translationY", from_value, to_value);
        animator.setDuration(time);
        animator.start();
    }

    /**
     * X轴移动
     */
    public void moveX(View view, float from_value, float to_value, int time) {

        ObjectAnimator animator = ObjectAnimator.ofFloat(view, "translationX", from_value, to_value);
        animator.setDuration(time);
        animator.start();

//        TranslateAnimation translateAnimation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, from_value,
//                Animation.RELATIVE_TO_SELF, to_value, Animation.RELATIVE_TO_SELF,
//                0, Animation.RELATIVE_TO_SELF, 0.0f);
//        translateAnimation.setFillAfter(true);
//        translateAnimation.setDuration(time);
//        view.startAnimation(translateAnimation);
    }

    public void moveX2(final View view, float from_value, float to_value, int time) {

        TranslateAnimation translateAnimation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, from_value,
                Animation.RELATIVE_TO_SELF, to_value, Animation.RELATIVE_TO_SELF,
                0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        translateAnimation.setFillAfter(true);
        translateAnimation.setDuration(time);
        view.startAnimation(translateAnimation);

        translateAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (view != null) {
                    view.setVisibility(View.GONE);
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    /**
     * 向上移动
     */
    public void moveUp(View view, float y_value, int time) {
        TranslateAnimation translateAnimation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0,
                Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF,
                0.0f, Animation.RELATIVE_TO_SELF, y_value);
        translateAnimation.setFillAfter(true);
        translateAnimation.setDuration(time);
        view.startAnimation(translateAnimation);
    }

    /**
     * @param fromXvalue   偏移量（）
     * @param durationTime 执行的时间
     * @return
     */
    public void moveRightToViewLocationAlpha(View view, float fromXvalue, int durationTime) {
        view.setVisibility(View.VISIBLE);
        AlphaAnimation alphaAnimation = new AlphaAnimation(0, 1);
        TranslateAnimation translateAnimation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, fromXvalue,
                Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF,
                0.0f, Animation.RELATIVE_TO_SELF, 0.0f);
        AnimationSet animationSet = new AnimationSet(true);
        animationSet.addAnimation(alphaAnimation);
        animationSet.addAnimation(translateAnimation);
        animationSet.setDuration(durationTime);
        view.startAnimation(animationSet);
//        final AnimatorSet set;
//        PropertyValuesHolder holder1 = PropertyValuesHolder.ofFloat("translationX", 200, 0.0f);
//        PropertyValuesHolder holder2 = PropertyValuesHolder.ofFloat("alpha", 0.0f, 1.0f);
//        ObjectAnimator objectAnimator = ObjectAnimator.ofPropertyValuesHolder(view, holder1, holder2);
//        set = new AnimatorSet();
//        set.play(objectAnimator);
//        set.setDuration(durationTime);
//        view.setLayerType(View.LAYER_TYPE_HARDWARE, null);
//        set.start();
    }

    /**
     * 同时旋转缩放
     */
    public AnimatorSet RepeatZoomAndRotation(View view) {
        PropertyValuesHolder holder1 = PropertyValuesHolder.ofFloat("scaleY", 0.4f, 1.0f);
        PropertyValuesHolder holder2 = PropertyValuesHolder.ofFloat("scaleX", 0.4f, 1.0f);
        PropertyValuesHolder holder3 = PropertyValuesHolder.ofFloat("scaleY", 1.0f, 0.4f);
        PropertyValuesHolder holder4 = PropertyValuesHolder.ofFloat("scaleX", 1.0f, 0.4f);
        PropertyValuesHolder holder5 = PropertyValuesHolder.ofFloat("rotation", 0, 360);
        PropertyValuesHolder holder6 = PropertyValuesHolder.ofFloat("rotation", 0, 360);
        ObjectAnimator objectAnimator1 = ObjectAnimator.ofPropertyValuesHolder(view, holder1, holder2);
        ObjectAnimator objectAnimator2 = ObjectAnimator.ofPropertyValuesHolder(view, holder3, holder4);
        ObjectAnimator objectAnimator3 = ObjectAnimator.ofPropertyValuesHolder(view, holder5);
        ObjectAnimator objectAnimator4 = ObjectAnimator.ofPropertyValuesHolder(view, holder6);
        set = new AnimatorSet();
        set.play(objectAnimator1).before(objectAnimator2);
        set.play(objectAnimator3).before(objectAnimator4);
        set.setDuration(2000);
        set.start();
        set.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                XLog.d(TAG, "onAnimationStart");
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                XLog.d(TAG, "onAnimationEnd");
                XLog.d("动画状态", "一周");
                if (set != null) {
                    try {
                        set.start();
                    } catch (Exception e) {
                    }
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                XLog.d(TAG, "onAnimationCancel:" + set);
//                set = null;
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });
        return set;
    }

    /**
     * 放大缩小效果
     *
     * @param view
     * @param time
     */
    public void Zoom(View view, long time) {
        final AnimatorSet set;
        PropertyValuesHolder holder1 = PropertyValuesHolder.ofFloat("scaleY", 1.0f, 1.5f);
        PropertyValuesHolder holder2 = PropertyValuesHolder.ofFloat("scaleX", 1.0f, 1.5f);
        PropertyValuesHolder holder3 = PropertyValuesHolder.ofFloat("scaleY", 1.5f, 1.0f);
        PropertyValuesHolder holder4 = PropertyValuesHolder.ofFloat("scaleX", 1.5f, 1.0f);
        ObjectAnimator objectAnimator1 = ObjectAnimator.ofPropertyValuesHolder(view, holder1, holder2);
        ObjectAnimator objectAnimator2 = ObjectAnimator.ofPropertyValuesHolder(view, holder3, holder4);
        set = new AnimatorSet();
        set.play(objectAnimator1).before(objectAnimator2);
        set.setDuration(time);
        set.start();
    }


    /**
     * 绕中心点转
     */
    public static ObjectAnimator RotationPoint(final View view, long time, float from, float to) {
        PropertyValuesHolder holder1 = PropertyValuesHolder.ofFloat("rotation", from, to);
        ObjectAnimator objectAnimator = ObjectAnimator.ofPropertyValuesHolder(view, holder1);
        objectAnimator.setDuration(time);
        objectAnimator.setRepeatCount(ValueAnimator.INFINITE);
        objectAnimator.setRepeatMode(ValueAnimator.RESTART);
        objectAnimator.start();
        objectAnimator.setInterpolator(new LinearInterpolator());
//        set = new AnimatorSet();
//        set.play(objectAnimator);
//        set.setDuration(time);
//        set.setInterpolator(new LinearInterpolator());
        view.setLayerType(View.LAYER_TYPE_HARDWARE, null);
//        set.start();
        objectAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if(view != null) {
                    view.setLayerType(View.LAYER_TYPE_NONE, null);
                }
            }
        });

        return objectAnimator;
    }


    public static void RotationPoint2(View view, float from, float to) {
        RotateAnimation animation = new RotateAnimation(from, to,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        animation.setDuration(300);
        animation.setFillAfter(true);
        view.startAnimation(animation);
    }

    public void RotationPoint3(View view, int time) {
        RotateAnimation animation = new RotateAnimation(90f, 0f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        animation.setDuration(time);
        animation.setFillAfter(true);
        view.startAnimation(animation);
    }

    /**
     * 旋转
     */
    public static void pivot(View view, int time) {
        RotateAnimation animation = new RotateAnimation(60f, 0f, Animation.RELATIVE_TO_SELF, 0.9f, Animation.RELATIVE_TO_SELF, 0.9f);
//        Animation animation = new TranslateAnimation(1, 0f, 1, 0.5f, 1,0f,1,0f);
        animation.setDuration(time);
        animation.setFillAfter(true);
        view.startAnimation(animation);
    }

    /**
     * x轴平移，从指定坐标平移到指定坐标
     */
    public void translationX(final View view, int fromValue, int toValue, int time) {
        ObjectAnimator objectAnimator1 = ObjectAnimator.ofFloat(view, "translationX",
                fromValue, toValue);
        objectAnimator1.setDuration(time);
        view.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        objectAnimator1.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                view.setLayerType(View.LAYER_TYPE_NONE, null);
            }
        });
        objectAnimator1.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                view.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationCancel(Animator animator) {
            }

            @Override
            public void onAnimationRepeat(Animator animator) {
            }
        });
        objectAnimator1.start();
    }

    public static void translationX(final View view, float form, float to, int time) {
        ObjectAnimator objectAnimator1 = ObjectAnimator.ofFloat(view, "translationX",
                form, to);
        objectAnimator1.setDuration(time);
        view.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        objectAnimator1.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                view.setLayerType(View.LAYER_TYPE_NONE, null);
            }
        });
        objectAnimator1.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                view.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationCancel(Animator animator) {
            }

            @Override
            public void onAnimationRepeat(Animator animator) {
            }
        });
        objectAnimator1.start();
    }

    /**
     * y轴平移，从指定坐标平移到指定坐标
     */
    public void translationY(final View view, float to, int time) {
        ObjectAnimator objectAnimator1 = ObjectAnimator.ofFloat(view, "translationY",
                view.getTranslationY(), to);
//        animator.set
        objectAnimator1.setDuration(time);
        view.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        objectAnimator1.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                view.setLayerType(View.LAYER_TYPE_NONE, null);
            }
        });
        objectAnimator1.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                view.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationCancel(Animator animator) {
            }

            @Override
            public void onAnimationRepeat(Animator animator) {
            }
        });
        objectAnimator1.start();
    }

    /**
     * y轴平移，从指定坐标平移到指定坐标
     */
    public void translationY(final View view, float from, float to, int time) {
        ObjectAnimator objectAnimator1 = ObjectAnimator.ofFloat(view, "translationY",
                view.getTranslationY(), from, to);
        objectAnimator1.setDuration(time);
        view.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        objectAnimator1.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                view.setLayerType(View.LAYER_TYPE_NONE, null);
            }
        });
        objectAnimator1.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
                view.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animator) {
//                view.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationCancel(Animator animator) {
            }

            @Override
            public void onAnimationRepeat(Animator animator) {
            }
        });
        objectAnimator1.start();
    }

    public ObjectAnimator scaleXYtoSmall(final View view, int time) {
        final AnimatorSet set;
        PropertyValuesHolder holder1 = PropertyValuesHolder.ofFloat("scaleY", 1.0f, 0.5f);
        PropertyValuesHolder holder2 = PropertyValuesHolder.ofFloat("scaleX", 1.0f, 0.5f);
        ObjectAnimator objectAnimator = ObjectAnimator.ofPropertyValuesHolder(view, holder1, holder2);
        set = new AnimatorSet();
        set.play(objectAnimator);
        set.setDuration(time);
        view.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        set.start();
        return objectAnimator;
    }

    public ObjectAnimator scaleXYtoBig(final View view, int time) {
        final AnimatorSet set;
        PropertyValuesHolder holder1 = PropertyValuesHolder.ofFloat("scaleY", 1.0f);
        PropertyValuesHolder holder2 = PropertyValuesHolder.ofFloat("scaleX", 1.0f);
        ObjectAnimator objectAnimator = ObjectAnimator.ofPropertyValuesHolder(view, holder1, holder2);
        set = new AnimatorSet();
        set.play(objectAnimator);
        set.setDuration(time);
        view.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        set.start();
        return objectAnimator;
    }

    /**
     * x轴放大
     */
    public void scaleX(final RelativeLayout view, int time) {
        view.setVisibility(View.VISIBLE);
        final AnimatorSet set;
        PropertyValuesHolder holder1 = PropertyValuesHolder.ofFloat("scaleY", 1.0f, 1.0f);
        PropertyValuesHolder holder2 = PropertyValuesHolder.ofFloat("scaleX", 0.0f, 1.0f);
        ObjectAnimator objectAnimator1 = ObjectAnimator.ofPropertyValuesHolder(view, holder1, holder2);
        set = new AnimatorSet();
        set.play(objectAnimator1);
        set.setDuration(time);
        view.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        objectAnimator1.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                view.setLayerType(View.LAYER_TYPE_NONE, null);
            }
        });
        set.start();
    }

    /**
     * y轴放大
     */
    public void scaleY(final RelativeLayout view, int time) {
        view.setVisibility(View.VISIBLE);
        view.setPivotY(0f);
        final AnimatorSet set;
        PropertyValuesHolder holder1 = PropertyValuesHolder.ofFloat("scaleY", 1.0f, 2.0f);
        PropertyValuesHolder holder2 = PropertyValuesHolder.ofFloat("scaleX", 1.0f, 1.0f);
        ObjectAnimator objectAnimator1 = ObjectAnimator.ofPropertyValuesHolder(view, holder1, holder2);
        set = new AnimatorSet();
        set.play(objectAnimator1);
        set.setDuration(time);
        view.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        objectAnimator1.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                view.setLayerType(View.LAYER_TYPE_NONE, null);
            }
        });
        set.start();
    }

    /**
     * 指示点上来来回抖动
     */
    public void shake(ImageView imageView, int time) {
        TranslateAnimation anim = new TranslateAnimation(0,
                0, 0, imageView.getHeight() / 2);
        anim.setInterpolator(new CycleInterpolator(2f));  //次数
        anim.setDuration(time);
        imageView.startAnimation(anim);
    }

    /**
     * x,y轴放大，缩小
     */
    public ObjectAnimator scaleXY(final View view, int time, float xfrom, float xto, float yfrom, float yto) {
        view.setVisibility(View.VISIBLE);
        final AnimatorSet set;
        PropertyValuesHolder holder1 = PropertyValuesHolder.ofFloat("scaleX", xfrom, xto);
        PropertyValuesHolder holder2 = PropertyValuesHolder.ofFloat("scaleY", yfrom, yto);
        ObjectAnimator objectAnimator = ObjectAnimator.ofPropertyValuesHolder(view, holder1, holder2);
        set = new AnimatorSet();
        set.play(objectAnimator);
        set.setDuration(time);
        view.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        //右上角为中心点
        view.setPivotX(view.getWidth());
        view.setPivotY(0);
        set.start();

        objectAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);

            }
        });

        return objectAnimator;
    }

    /**
     * 选择性别头像时的动画
     */
    public void scaleXY2(final View view, int time) {
        view.setVisibility(View.VISIBLE);
        final AnimatorSet set;
        PropertyValuesHolder holder1 = PropertyValuesHolder.ofFloat("scaleY", 0.0f, 1.0f);
        PropertyValuesHolder holder2 = PropertyValuesHolder.ofFloat("scaleX", 0.0f, 1.0f);
        PropertyValuesHolder holder3 = PropertyValuesHolder.ofFloat("scaleY", 1.0f, 0.8f);
        PropertyValuesHolder holder4 = PropertyValuesHolder.ofFloat("scaleX", 1.0f, 0.8f);
        ObjectAnimator objectAnimator = ObjectAnimator.ofPropertyValuesHolder(view, holder1, holder2);
        ObjectAnimator objectAnimator2 = ObjectAnimator.ofPropertyValuesHolder(view, holder3, holder4);
        set = new AnimatorSet();
        set.play(objectAnimator).before(objectAnimator2);
        set.setDuration(time);
        view.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        set.start();
    }

    /***/
    public void translationY(final View view, int time) {
        view.setVisibility(View.VISIBLE);
        view.setAlpha(1);
//        DecelerateInterpolator di = new DecelerateInterpolator();   /**减速*/
        ObjectAnimator animator = ObjectAnimator.ofFloat(view, "alpha",
                0f, 0.4f, 0.8f, 1.0f);
//        animator.setInterpolator(di);
        animator.setDuration(time).start();
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                ObjectAnimator animator1 = ObjectAnimator.ofFloat(view, "alpha", 1f, 1f, 1f, 0.5f, 0f);
                animator1.setDuration(3000);
                animator1.start();
            }

            @Override
            public void onAnimationCancel(Animator animator) {
            }

            @Override
            public void onAnimationRepeat(Animator animator) {
            }
        });
//
    }

    //-------------------------------------------------------------

    /**
     * x轴移动
     */
    public static void moveX(float from, float to, int durationTime, View view, int visible) {
        if (view.getVisibility() == visible) return;
        TranslateAnimation mHiddenAction = new TranslateAnimation(Animation.RELATIVE_TO_SELF, from,
                Animation.RELATIVE_TO_SELF, to, Animation.RELATIVE_TO_SELF,
                0.0f, Animation.RELATIVE_TO_SELF, 0.0f);
        mHiddenAction.setDuration(durationTime);
        view.setAnimation(mHiddenAction);
        view.setVisibility(visible);
    }

    /**
     * 从下部进入
     */
    public static void moveY(float from, float to, int time, View view, int visible) {
        if (view.getVisibility() == visible) return;
        TranslateAnimation mHiddenAction = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0,
                Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF,
                from, Animation.RELATIVE_TO_SELF, to);
        mHiddenAction.setDuration(time);
        view.setAnimation(mHiddenAction);
        view.setVisibility(visible);
    }
}
