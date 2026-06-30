package com.zhuowei.polling.contract;

import com.chenming.common.base.IBaseModel;
import com.chenming.common.base.IBaseViewModel;
import com.chenming.httprequest.http.bean.BaseBean;
import com.chenming.httprequest.http.listener.OnHttpCallBack;
import com.zhuowei.polling.bean.AreaListBean;
import com.zhuowei.polling.beans.TicketListBean;
import com.zhuowei.polling.contract.vm.TicketDetailVm;

import java.io.File;
import java.util.List;
import java.util.Objects;

public class UploadTicketFileContract {


    public interface IUploadTicketFileVm extends IBaseViewModel {

        void uploadFile(File file);
    }

    /**
     * 逻辑处理层
     */
    public interface IUploadTicketFileModel extends IBaseModel {


        void uploadFile(File file, OnHttpCallBack<BaseBean<List<TicketListBean.RowsDTO>>> callBack);

    }
}
