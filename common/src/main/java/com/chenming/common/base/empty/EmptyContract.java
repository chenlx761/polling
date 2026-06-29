package com.chenming.common.base.empty;


import com.chenming.common.base.IBaseModel;
import com.chenming.common.base.IBaseViewModel;

/**
 * Created by Admin on 2018/3/9.
 */

public class EmptyContract {


    /**
     * P视图与逻辑处理的连接层
     */
    public interface IEmptyViewModel extends IBaseViewModel {

    }

    /**
     * 逻辑处理层
     */
    public interface IEmptyModel extends IBaseModel {

    }
}
