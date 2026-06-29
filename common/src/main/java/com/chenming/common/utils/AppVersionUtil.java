package com.chenming.common.utils;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;


/**
 * 作者：xiaoguoqing
 * 创建时间：2019-02-15 下午 3:54
 * 文件描述：
 */

public class AppVersionUtil {

    private static String version = "";
    private static int versionCode = 0;

    /**
     * 获取当前版本名字
     * Created by zmx
     *
     * @return
     */
    public static String getVersion() {
        //        try {
        //            PackageManager manager = ApplicationContext.getContext().getPackageManager();
        //            PackageInfo info = manager.getPackageInfo(ApplicationContext.getContext().getPackageName(), 0);
        //            return info.versionName;
        //        } catch (Exception e) {
        //            e.printStackTrace();
        //            return "can_not_find_version_name";
        //        }

        if (TextUtils.isEmpty(version)) {
            try {
                PackageManager manager = CommApplication.getInstance().getPackageManager();
                PackageInfo info = manager.getPackageInfo(CommApplication.getInstance().getPackageName(), 0);
                version = info.versionName;
            } catch (Exception e) {
                e.printStackTrace();
                return "can_not_find_version_name";
            }
        }
        return version;
    }

    /**
     * 获取当前版本号
     * Created by zmx
     *
     * @return
     */
    public static int getVersionCode() {
        if (versionCode == 0) {
            try {
                PackageManager manager = CommApplication.getInstance().getPackageManager();
                PackageInfo info = manager.getPackageInfo(CommApplication.getInstance().getPackageName(), 0);
                return info.versionCode;
            } catch (Exception e) {
                e.printStackTrace();
                return 0;
            }
        }
        return versionCode;
    }
}
