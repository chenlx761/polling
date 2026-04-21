package com.zhuowei.polling.constants;

public class HttpConstants {

    public static String HD_BASE_URL = "";//虎盾的baseURl

    public static final String BASE_HOST = "172.22.38.13:8080";
    public static final String BASE_URL = "http://" + BASE_HOST;

    public static final String GET_TS_ID_URL = "hyt-aqsc/prod-api/api/stage3/hd/login";
    public static final String GET_TICKET_LIST_URL = "app/system/buildRecord/list";//获取台账待办列表信息
    public static final String POST_FILE = "system/buildRecord/upload";//上传图片接口信息
    public static final String EDIT_TICKET_DETAIL_URL = "system/buildRecord";//修改台账信息
}
