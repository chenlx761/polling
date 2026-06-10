package com.zhuowei.polling.constants;

public class HttpConstants {

    public static String HD_BASE_URL = "";//虎盾的baseURl

//    public static final String BASE_HOST = "szbl-api.dev.tw.com";
    public static final String BASE_HOST = "192.168.3.54:8080";
    public static final String BASE_URL = "http://" + BASE_HOST;

    public static final String GET_TS_ID_URL = "appLogin";//登录1
    public static final String GET_TICKET_LIST_URL = "app/system/buildRecord/list";//获取台账待办列表信息
    public static final String POST_FILE = "app/system/buildRecord/upload";//上传图片接口信息
    public static final String POST_FILE_2 = "app/system/buildRecord/uploadWithFileName";//上传文件接口信息
    public static final String EDIT_TICKET_DETAIL_URL = "app/system/buildRecord";//修改台账信息
    public static final String GET_TICKET_DETAIL_URL = "app/system/buildRecord/getById";//获取详情
}
