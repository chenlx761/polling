package com.chenming.common.listener;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

public abstract class SimpleTextWatcher implements TextWatcher {


    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    @Override
    public void afterTextChanged(Editable s) {
    }
}