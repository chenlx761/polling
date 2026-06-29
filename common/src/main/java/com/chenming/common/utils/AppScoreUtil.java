package com.chenming.common.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.widget.Toast;

import java.util.List;

/**
 * desc:App评分
 * date:2022/1/10
 * by:xiaoguoqing
 */
public class AppScoreUtil {

    public static void goToMarket(Activity activity) {
        if (!isMarketInstalled(activity)) {
            Toast.makeText(activity, "您的手机没有安装应用市场", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            Uri uri = Uri.parse("market://details?id=" + activity.getPackageName());
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if (intent.resolveActivity(activity.getPackageManager()) != null) {
                activity.startActivity(intent);
            }
        } catch (Exception e) {
            // 也可以调到某个网页应用市场
            Toast.makeText(activity, "手机没有安装应用市场", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 本手机是否安装了应用市场
     *
     * @param context
     * @return
     */
    public static boolean isMarketInstalled(Context context) {
        Intent intent = new Intent();
        intent.setData(Uri.parse("market://details?id=android.browser"));
        List list = context.getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        return 0 != list.size();
    }

}
