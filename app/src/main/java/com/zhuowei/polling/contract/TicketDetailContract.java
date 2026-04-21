package com.zhuowei.polling.contract;

import com.chenming.common.base.IBaseModel;
import com.chenming.common.base.IBaseViewModel;
import com.chenming.httprequest.http.bean.BaseBean;
import com.chenming.httprequest.http.listener.OnHttpCallBack;
import com.zhuowei.polling.beans.LoginResult;
import com.zhuowei.polling.beans.TicketDetail;
import com.zhuowei.polling.beans.TicketListBean;

import java.util.Objects;

public class TicketDetailContract {

    public interface ITicketDetailVm extends IBaseViewModel {

        void postTicketDetail(TicketListBean.RowsDTO bean);
    }

    /**
     * 逻辑处理层
     */
    public interface ITicketDetailModel extends IBaseModel {


        void postTicketDetail(TicketListBean.RowsDTO bean, OnHttpCallBack<BaseBean<Objects>> callBack);
    }
}
