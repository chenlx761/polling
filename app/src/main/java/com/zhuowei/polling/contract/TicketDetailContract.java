package com.zhuowei.polling.contract;

import com.chenming.common.base.IBaseModel;
import com.chenming.common.base.IBaseViewModel;
import com.chenming.httprequest.http.bean.BaseBean;
import com.chenming.httprequest.http.listener.OnHttpCallBack;
import com.zhuowei.polling.bean.AreaListBean;
import com.zhuowei.polling.beans.LoginResult;
import com.zhuowei.polling.beans.TicketDetail;
import com.zhuowei.polling.beans.TicketListBean;
import com.zhuowei.polling.contract.vm.TicketDetailVm;

import java.util.List;
import java.util.Objects;

public class TicketDetailContract {

    public interface GetAreaListCallBack {
        void onSuccessful(TicketDetailVm.AreaPickerDisplayData data);
    }

    public interface ITicketDetailVm extends IBaseViewModel {

        void getTicketDetail(String ticketId);
        void postTicketDetail(TicketListBean.RowsDTO bean, boolean isCreateMode);

        void getAreaPickerData(GetAreaListCallBack callBack);
    }

    /**
     * 逻辑处理层
     */
    public interface ITicketDetailModel extends IBaseModel {


        void getTicketDetail(String ticketId, OnHttpCallBack<BaseBean<TicketListBean.RowsDTO>> callBack);

        void postTicketDetail(TicketListBean.RowsDTO bean, boolean isCreateMode, OnHttpCallBack<BaseBean<Objects>> callBack);

        void getAreaList(OnHttpCallBack<BaseBean<AreaListBean>> callBack);
    }
}
