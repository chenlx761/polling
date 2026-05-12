package com.zhuowei.polling.contract;

import com.chenming.common.base.IBaseModel;
import com.chenming.common.base.IBaseViewModel;
import com.chenming.httprequest.http.bean.BaseBean;
import com.chenming.httprequest.http.listener.OnHttpCallBack;
import com.zhuowei.polling.beans.LoginResult;
import com.zhuowei.polling.beans.TicketListBean;

import java.util.List;
import java.util.Objects;

public class MainContract {

    public interface IMainVm extends IBaseViewModel {


        void flashTicketList(String ticketStaus,String account,String address);

        void loadMoreTicketList(String ticketStaus,String account,String address);

    }

    /**
     * 逻辑处理层
     */
    public interface IMainModel extends IBaseModel {

        void getTicketList(int page, int size,String ticketStaus,String account,String address, OnHttpCallBack<BaseBean<List<TicketListBean.RowsDTO>>> callBack);
    }
}
