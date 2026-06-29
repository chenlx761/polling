package com.chenming.common.base;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

public class BaseModel implements IBaseModel {

    private CompositeDisposable mCompositeDisposable = new CompositeDisposable();


    protected void addDisposable(Disposable disposable) {
        mCompositeDisposable.add(disposable);
    }

    @Override
    public void clearRequest() {
        mCompositeDisposable.clear();
    }

    @Override
    public void removeRequest() {
        mCompositeDisposable.dispose();
    }
}
