package com.chenming.common.base.databindrv;

import android.os.SystemClock;
import android.util.Log;
import android.view.View;

import androidx.databinding.BindingAdapter;

/**
 * 防多次点击的
 */
public class ViewClickRv {

    @BindingAdapter({"android:onClick", "android:clickable"})
    public static void setOnClick(View view, View.OnClickListener clickListener, boolean clickable) {
        setOnClick(view, clickListener);
        view.setClickable(clickable);

    }

    @BindingAdapter({"android:onClick"})
    public static void setOnClick(View view, final View.OnClickListener clickListener) {
        final long[] mHits = new long[2];
        view.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                System.arraycopy(mHits, 1, mHits, 0, mHits.length - 1);
                mHits[mHits.length - 1] = SystemClock.uptimeMillis();
                if (mHits[0] < (SystemClock.uptimeMillis() - 500)) {
                    clickListener.onClick(v);
                }
            }
        });

    }


}
