package com.chenming.common.base.empty;

import com.chenming.common.base.BaseViewModel;
import com.chenming.common.base.IBaseModel;
import com.chenming.common.base.IBaseViewModel;

public class EmptyViewModel extends BaseViewModel<EmptyContract.IEmptyModel> implements IBaseViewModel {
    @Override
    protected EmptyContract.IEmptyModel getModel() {
        return new EmptyModel();
    }
}
