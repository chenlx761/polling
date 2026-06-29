package com.chenming.common.base.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;


import com.blankj.utilcode.util.ScreenUtils;
import com.chenming.common.R;
import com.chenming.common.utils.CommScreenUtils;

import androidx.annotation.Nullable;


/**
 * Created by wqx on 2018/2/1.
 */

public class MyTitleBar extends LinearLayout {

    private LinearLayout leftLayout;
    private RelativeLayout rightLayout, titleLayout, right2Layout;
    private ImageView leftImage, rightImage, right2Image;
    private TextView leftTextView, titleTextView;
    private View bottomLine;
    private LinearLayout topLayout;
    private ImageView mIcon;

    public MyTitleBar(Context context) {
        super(context);
    }

    public MyTitleBar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public MyTitleBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private void init(Context context, AttributeSet attrs) {
        LayoutInflater.from(context).inflate(R.layout.layout_title_bar, this);
        topLayout = findViewById(R.id.linearLayout);
        mIcon = findViewById(R.id.icon);
        leftLayout = findViewById(R.id.left_layout);
        titleLayout = findViewById(R.id.title_bar);
        rightLayout = findViewById(R.id.right_layout);
        rightLayout.setEnabled(false);
        right2Layout = findViewById(R.id.right2_layout);
        right2Layout.setEnabled(false);
        leftImage = findViewById(R.id.left_iv);
        rightImage = findViewById(R.id.right_iv);
        right2Image = findViewById(R.id.right2_iv);
        leftTextView = findViewById(R.id.left_tv);
        titleTextView = findViewById(R.id.center_tv);
        bottomLine = findViewById(R.id.bottom_line);

        if (attrs != null) {
            TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.MyTitleBar);
            String title = typedArray.getString(R.styleable.MyTitleBar_titleBarTitle);
            int titleColor = typedArray.getColor(R.styleable.MyTitleBar_titleBarTitleColor, getResources().getColor(R.color.text_black_color));

            titleTextView.setText(title);
            //titleTextView.setTextSize(typedArray.getDimension(R.styleable.MyTitleBar_titleBarTitleSize,12));
            titleTextView.setTextColor(titleColor);

            String leftText = typedArray.getString(R.styleable.MyTitleBar_titleBarLeftText);
            leftTextView.setText(leftText);

            Drawable leftDrawable = typedArray.getDrawable(R.styleable.MyTitleBar_titleBarLeftImg);
            if (leftDrawable != null) {
                leftImage.setImageDrawable(leftDrawable);
            }

            Drawable leftIconDrawable = typedArray.getDrawable(R.styleable.MyTitleBar_titleBarLeftIcon);
            if (leftIconDrawable != null) {
                leftImage.setVisibility(GONE);
                mIcon.setVisibility(VISIBLE);
                mIcon.setImageDrawable(leftIconDrawable);
            }

            Drawable leftBgDrawable = typedArray.getDrawable(R.styleable.MyTitleBar_titleBarLeftImgBg);
            if (leftBgDrawable != null) {
                leftImage.setBackground(leftBgDrawable);
            }

            Drawable rightDrawable = typedArray.getDrawable(R.styleable.MyTitleBar_titleBarRightImg);
            if (rightDrawable != null) {
                rightImage.setImageDrawable(rightDrawable);
            }

            Drawable right2Drawable = typedArray.getDrawable(R.styleable.MyTitleBar_titleBarRight2Img);
            if (rightDrawable != null) {
                right2Image.setImageDrawable(right2Drawable);
            }

            Drawable background = typedArray.getDrawable(R.styleable.MyTitleBar_titleBarBackground);
            if (background != null) {
                titleLayout.setBackground(background);
            }

            typedArray.recycle();
        }

        topLayout.getLayoutParams().height = CommScreenUtils.getStatusHeight(context);

    }

    public ImageView getLeftIcon() {
        return mIcon;
    }

    //    public LinearLayout getTopLayout() {
    //        return topLayout;
    //    }

    public void setTopLayoutColor(int color) {
        topLayout.setBackgroundColor(color);
    }

    public void setTopLayoutVisible() {
        topLayout.setVisibility(VISIBLE);
    }

    public void setLeftImageResource(int resId) {
        leftImage.setImageResource(resId);
    }

    public void setRightImageResource(int resId) {
        rightImage.setImageResource(resId);
    }

    public void setLeftImageBackground(int resId) {
        leftImage.setBackgroundResource(resId);
    }

    public void setLeftImageBackground(Drawable drawable) {
        leftImage.setBackground(drawable);
    }

    public void setLeftLayoutClickListener(OnClickListener listener) {
        if (listener != null) {
            leftLayout.setOnClickListener(listener);
        }
    }

    public void setBottomLineVisiable(int visible) {
        bottomLine.setVisibility(visible);
    }

    public void setRightLayoutClickListener(OnClickListener listener) {
        rightLayout.setEnabled(true);
        if (listener != null) {
            rightLayout.setOnClickListener(listener);
        }
    }

    public void setRight2LayoutClickListener(OnClickListener listener) {
        right2Layout.setEnabled(true);
        if (listener != null) {
            right2Layout.setOnClickListener(listener);
        }
    }

    public void setLeftLayoutVisibility(int visibility) {
        leftLayout.setVisibility(visibility);
    }

    public void setRightLayoutVisibility(int visibility) {
        rightLayout.setVisibility(visibility);
    }

    public void setRight2LayoutVisibility(int visibility) {
        right2Layout.setVisibility(visibility);
    }

    public TextView getTitle() {
        return titleTextView;
    }

    public void setTitle(String title) {
        titleTextView.setText(title);
    }

    public void setLeftTextView(String text) {
        leftTextView.setText(text);
    }

    public void setBackgroundColor(int color) {
        titleLayout.setBackgroundColor(color);
    }

    public LinearLayout getLeftLayout() {
        return leftLayout;
    }

    public RelativeLayout getRightLayout() {
        return rightLayout;
    }

    //    public void setTopGone() {
    //        topLayout.setVisibility(GONE);
    //    }
}
