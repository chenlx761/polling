package com.chenming.common.base.databindrv;

import android.text.InputFilter;
import android.view.View;
import android.widget.TextView;

import com.chenming.common.view.InputFilterMinMax;

import androidx.databinding.BindingAdapter;

public class TextViewFilterRv {
    @BindingAdapter(value = {"filter_min", "filter_max"})
    public static void setOnClick(TextView view, Integer filter_min, Integer filter_max) {
        view.setFilters(new InputFilter[]{
                new InputFilterMinMax(filter_min, filter_max)
        });

    }
}
