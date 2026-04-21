package com.zhuowei.polling.utils;

import com.chenming.common.utils.SpUtils;
import com.zhuowei.polling.MyApplication;

public class SpManager {
    public static final String SP_TOKEN_KEY = "acc_token";
    public static final String SP_REFRESH_TOKEN_KEY = "refresh_token";
    public static final String SP_USER_NAME_KEY = "user_name";
    public static final String SP_USER_PWD_KEY = "user_pwd";
    public static String getToken() {
        //return (String) SpUtils.getParam(MyApplication.getInstance(), SP_TOKEN_KEY, "");
        return "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhZG1pbiIsImxvZ2luX3VzZXJfa2V5IjoiNWYxY2JjY2QtZDE2NC00MmU5LWFhNTEtZDA1ZjEyN2FmNTZiIn0.k_XtP1n62yIBBqoF2CMUsFFj6yah4twCHpv1ieBWHOUsGpA_QFxjspH-CHYS6FHe2hqYqd-xIKmEIc3QL3Cd6w";
    }


    public static void setToken(String token) {
        if (token == null) {
            token = "";
        }
        SpUtils.setParam(MyApplication.getInstance(), SP_TOKEN_KEY, token);
    }


    public static String getRefreshToken() {
        return (String) SpUtils.getParam(MyApplication.getInstance(), SP_REFRESH_TOKEN_KEY, "");
    }


    public static void setRefreshToken(String refreshToken) {
        if (refreshToken == null) {
            refreshToken = "";
        }
        SpUtils.setParam(MyApplication.getInstance(), SP_REFRESH_TOKEN_KEY, refreshToken);
    }

    public static String getUserName() {
        return (String) SpUtils.getParam(MyApplication.getInstance(), SP_USER_NAME_KEY, "");
    }

    public static void setUserName(String userName) {
        if (userName == null) {
            userName = "";
        }
        SpUtils.setParam(MyApplication.getInstance(), SP_USER_NAME_KEY, userName);
    }

    public static String getUserPwd() {
        return (String) SpUtils.getParam(MyApplication.getInstance(), SP_USER_PWD_KEY, "");
    }

    public static void setUserPwd(String userPwd) {
        if (userPwd == null) {
            userPwd = "";
        }
        SpUtils.setParam(MyApplication.getInstance(), SP_USER_PWD_KEY, userPwd);
    }

}
