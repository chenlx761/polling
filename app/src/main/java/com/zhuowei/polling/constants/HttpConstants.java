package com.zhuowei.polling.constants;

import com.zhuowei.polling.BuildConfig;
import com.zhuowei.polling.MyApplication;

public class HttpConstants {

    public static String HD_BASE_URL = "";//虎盾的baseURl

    public static final String BASE_HOST_DEV = "szbl-api.dev.tw.com";//测试服
    public static final String BASE_HOST_DEV_WEB = "szbl-web.dev.tw.com";//测试服
    public static final String BASE_HOST_RELEASE = "szbl-api.cnhyt.com";//正式服
    public static final String BASE_HOST_WEB = "http://"+getWebBaseUrl();//web相关接口
    public static final String BASE_HOST_LOCAL = "192.168.3.54:8080";//本地金灵服务器
    public static final String BASE_URL = "http://" + getBaseHost();

    public static final String GET_TS_ID_URL = "appLogin";//登录1
    public static final String GET_TICKET_LIST_URL = "app/system/buildRecord/list";//获取台账待办列表信息
    public static final String POST_FILE = "app/system/buildRecord/upload";//上传图片接口信息
    public static final String POST_FILE_2 = "app/system/buildRecord/uploadWithFileName";//上传文件接口信息
    public static final String ADD_TICKET_DETAIL_URL = "app/system/buildRecord";//新增台账信息
    public static final String EDIT_TICKET_DETAIL_URL = "app/system/buildRecord";//修改台账信息
    public static final String GET_TICKET_DETAIL_URL = "app/system/buildRecord/getById";//获取详情
    public static final String GET_AREA_URL = "dev-api/system/powerOrg/topDepts";//获取详情
    public static final String UPLOAD_TICKET_FILE_URL = "dev-api/system/buildRecord/importData";//获取详情


    public static String getWebBaseUrl(){
        if (!BuildConfig.DEBUG){
            return BASE_HOST_DEV_WEB;
            //            return BASE_HOST_DEV;
        }
        //        return BASE_HOST_RELEASE;
        return BASE_HOST_DEV_WEB;
    }

    public static String getBaseHost() {
        if (MyApplication.getInstance().isLocationTest()){
            return BASE_HOST_LOCAL;
        }
        if (!BuildConfig.DEBUG){
            return BASE_HOST_RELEASE;
//            return BASE_HOST_DEV;
        }
//        return BASE_HOST_RELEASE;
        return BASE_HOST_DEV;
    }
}
