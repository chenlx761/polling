package com.chenming.common.utils;

import android.text.TextUtils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by Administrator on 2018/7/13 0013.
 */

public class TimeUtil {


    public static SimpleDateFormat getyyyyMMddHHmmssFormatter() {
        SimpleDateFormat yyyyMMddHHmmssFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        yyyyMMddHHmmssFormatter.setTimeZone(TimeZone.getDefault());
        return yyyyMMddHHmmssFormatter;
    }

    public static SimpleDateFormat getHHmmssFormatter() {
        SimpleDateFormat HHmmssFormatter = new SimpleDateFormat("HH:mm:ss");
        HHmmssFormatter.setTimeZone(TimeZone.getDefault());
        return HHmmssFormatter;
    }

    public static SimpleDateFormat getyyyyMMddHHmmFormatter() {
        SimpleDateFormat yyyyMMddHHmmFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        yyyyMMddHHmmFormatter.setTimeZone(TimeZone.getDefault());
        return yyyyMMddHHmmFormatter;
    }


    public static SimpleDateFormat getHHmmFormatter() {
        SimpleDateFormat HHmmFormatter = new SimpleDateFormat("HH:mm");
        HHmmFormatter.setTimeZone(TimeZone.getDefault());
        return HHmmFormatter;
    }

    /**
     * 返回哪一天
     * 今天
     * 昨天
     * 明天
     *
     * @param targetMillis
     * @return
     */
    public static int getWhichDay(long targetMillis) {
        long today = System.currentTimeMillis();
        return (int) (targetMillis - today) / (24 * 60 * 60 * 1000);
    }

    /**
     * 获取UTC 时间格式
     *
     * @param time
     * @return
     */
    public static String getUTCTimeStr(long time) {
        //        long utcTime = getUTCTime(time);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(new Date(time));
    }

    public static String get12TimeStr(long time) {
        //        long utcTime = getUTCTime(time);
        SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd ahh:mm");
        ;
        return mSimpleDateFormat.format(new Date(time));
    }

    public static String timeConversion(long time) {
        long minutes = 0;
        long sencond = 0;
        minutes = time / 60;
        if (time % 60 != 0) {
            sencond = time % 60;
        }
        return (minutes < 10 ? ("0" + minutes) : minutes) + ":" + (sencond < 10 ? ("0" + sencond) : sencond);
    }

    public static String timeConversionHasHour(long time) {
        long hour = 0;
        long minutes = 0;
        long sencond = 0;
        long temp = time % 3600;
        if (time > 3600) {
            hour = time / 3600;
            if (temp != 0) {
                if (temp > 60) {
                    minutes = temp / 60;
                    if (temp % 60 != 0) {
                        sencond = temp % 60;
                    }
                } else {
                    sencond = temp;
                }
            }
        } else {
            minutes = time / 60;
            if (time % 60 != 0) {
                sencond = time % 60;
            }
        }
        return (hour < 10 ? ("0" + hour) : hour) + ":" + (minutes < 10 ? ("0" + minutes) : minutes) + ":" + (sencond < 10 ? ("0" + sencond) : sencond);
    }

    /**
     * 获取UTC时间
     *
     * @param time
     * @return
     */
    public static String getUTCTime(String time) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        long milliseconds = 0;
        try {
            Date date = sdf.parse(time);
            milliseconds = date.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return getUTCTimeStr(milliseconds);
    }

    /**
     * 获取UTC时间
     *
     * @return
     */
    public static long getUTCTime() {
        return getUTCTime(System.currentTimeMillis());
    }


    public static long getUTCTime(Long time) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(time);
        int zoneOffset = cal.get(java.util.Calendar.ZONE_OFFSET);
        int dstOffset = cal.get(java.util.Calendar.DST_OFFSET);
        cal.add(java.util.Calendar.MILLISECOND, -(zoneOffset + dstOffset));
        return cal.getTimeInMillis();
    }

    /**
     * 通过utc时间转化到当地时间
     *
     * @param utcTime
     * @return
     */
    public static long utc2Local(long utcTime) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(utcTime);
        //        int zoneOffset = cal.get(java.util.Calendar.ZONE_OFFSET);
        //        int dstOffset = cal.get(java.util.Calendar.DST_OFFSET);
        //        cal.add(java.util.Calendar.MILLISECOND, (zoneOffset + dstOffset));
        return cal.getTimeInMillis();
    }

    public static String dateToString(Date date, String format) {

        DateFormat df = new SimpleDateFormat(TextUtils.isEmpty(format) ? "yyyy-MM-dd" : format);

        return df.format(date);
    }

    public static String dateToString(Calendar date, String format) {

        DateFormat df = new SimpleDateFormat(TextUtils.isEmpty(format) ? "yyyy-MM-dd" : format);
        return df.format(date.getTime());
    }

    public static String dateToString(Long timestamp, String format) {

        DateFormat df = new SimpleDateFormat(TextUtils.isEmpty(format) ? "yyyy-MM-dd" : format);
        return df.format(new Date(timestamp));
    }


    /**
     * String 转Date
     *
     * @param serverTime
     * @param format
     * @return
     */
    public static Date parseServerTime(String serverTime, String format) {
        if (format == null || format.isEmpty()) {
            format = "yyyy-MM-dd HH:mm:ss";
        }
        SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.CHINESE);
        sdf.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
        Date date = null;
        try {
            date = sdf.parse(serverTime);
        } catch (Exception e) {
        }
        return date;
    }

    /*
     * 秒数转时间
     * */
    public static String getTime(int second) {
        if (second < 10) {
            return "00:0" + second;
        }
        if (second < 60) {
            return "00:" + second;
        }
        if (second < 3600) {
            int minute = second / 60;
            second = second - minute * 60;
            if (minute < 10) {
                if (second < 10) {
                    return "0" + minute + ":0" + second;
                }
                return "0" + minute + ":" + second;
            }
            if (second < 10) {
                return minute + ":0" + second;
            }
            return minute + ":" + second;
        }
        int hour = second / 3600;
        int minute = (second - hour * 3600) / 60;
        second = second - hour * 3600 - minute * 60;
        if (hour < 10) {
            if (minute < 10) {
                if (second < 10) {
                    return "0" + hour + ":0" + minute + ":0" + second;
                }
                return "0" + hour + ":0" + minute + ":" + second;
            }
            if (second < 10) {
                return "0" + hour + ":" + minute + ":0" + second;
            }
            return "0" + hour + ":" + minute + ":" + second;
        }
        if (minute < 10) {
            if (second < 10) {
                return hour + ":0" + minute + ":0" + second;
            }
            return hour + ":0" + minute + ":" + second;
        }
        if (second < 10) {
            return hour + ":" + minute + ":0" + second;
        }
        return hour + ":" + minute + ":" + second;
    }

    public static String secondsToHMS(int second) {
        int h = 0;
        int d = 0;
        int s = 0;
        String hStr;
        String dStr;
        String sStr;
        int temp = second % 3600;
        if (second > 3600) {
            h = second / 3600;
            if (temp != 0) {
                if (temp > 60) {
                    d = temp / 60;
                    if (temp % 60 != 0) {
                        s = temp % 60;
                    }
                } else {
                    s = temp;
                }
            }
        } else {
            d = second / 60;
            if (second % 60 != 0) {
                s = second % 60;
            }
        }
        hStr = h < 10 ? "0" + h : "" + h;
        dStr = d < 10 ? "0" + d : "" + d;
        sStr = s < 10 ? "0" + s : "" + s;
        return hStr + ":" + dStr + ":" + sStr;
    }

    //获取某月的第一天是周几
    public static String getFirstDay(int year, int month) {
        Calendar cld = Calendar.getInstance();
        //设置年份
        cld.set(Calendar.YEAR, year);
        //设置月份
        cld.set(Calendar.MONTH, month - 1);
        cld.set(Calendar.DATE, 1);//将今天设为1号
        int firstDay = cld.get(Calendar.DAY_OF_WEEK);
        String day = "";
        switch (firstDay) {
            case 1:
                day = "周日";
                break;
            case 2:
                day = "周一";
                break;
            case 3:
                day = "周二";
                break;
            case 4:
                day = "周三";
                break;
            case 5:
                day = "周四";
                break;
            case 6:
                day = "周五";
                break;
            case 7:
                day = "周六";
                break;
        }
        return day;
    }

    //获取当前年月
    public static int[] getYearMonth() {
        Calendar calendar = Calendar.getInstance();
        int mYear = calendar.get(Calendar.YEAR); // 获取当前年份
        int mMonth = calendar.get(Calendar.MONTH) + 1;// 获取当前月份
        return new int[]{mYear, mMonth};
    }

    //获取当前年月
    public static String getCurrentTime() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date curDate = new Date(System.currentTimeMillis());
        return formatter.format(curDate);
    }

    //获取当前年月
    public static String getCurrentTime(String format) {
        SimpleDateFormat formatter = new SimpleDateFormat(format);
        Date curDate = new Date(System.currentTimeMillis());
        return formatter.format(curDate);
    }

    /**
     * String型时间戳格式化
     *
     * @return
     */
    public static String longFormatTime(String time) {
        if (TextUtils.isEmpty(time)) {
            return "";
        }
        //SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        //转换为日期
        Date date = new Date();
        try {
            date.setTime(Long.parseLong(time));
            if (isThisYear(date)) {//今年
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");
                if (isToday(date)) { //今天
                    int minute = minutesAgo(Long.parseLong(time));
                    if (minute < 60) {//1小时之内
                        if (minute <= 1) {//一分钟之内，显示刚刚
                            return "刚刚";
                        } else {
                            return minute + "分钟前";
                        }
                    } else {
                        return simpleDateFormat.format(date);
                    }
                } else {
                    if (isYestYesterday(date)) {//昨天，显示昨天
                        return "昨天 " + simpleDateFormat.format(date);
                    } else if (isThisWeek(date)) {//本周,显示周几
                        String weekday = null;
                        if (date.getDay() == 1) {
                            weekday = "周一";
                        }
                        if (date.getDay() == 2) {
                            weekday = "周二";
                        }
                        if (date.getDay() == 3) {
                            weekday = "周三";
                        }
                        if (date.getDay() == 4) {
                            weekday = "周四";
                        }
                        if (date.getDay() == 5) {
                            weekday = "周五";
                        }
                        if (date.getDay() == 6) {
                            weekday = "周六";
                        }
                        if (date.getDay() == 0) {
                            weekday = "周日";
                        }
                        return weekday + " " + simpleDateFormat.format(date);
                    } else {
                        SimpleDateFormat sdf = new SimpleDateFormat("MM-dd HH:mm");
                        return sdf.format(date);
                    }
                }
            } else {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                return sdf.format(date);
            }
        } catch (Exception e) {
            return "";
        }

    }

    private static int minutesAgo(long time) {
        return (int) ((System.currentTimeMillis() - time) / (60000));
    }

    private static boolean isToday(Date date) {
        Date now = new Date();
        return (date.getYear() == now.getYear()) && (date.getMonth() == now.getMonth()) && (date.getDate() == now.getDate());
    }

    private static boolean isYestYesterday(Date date) {
        Date now = new Date();
        return (date.getYear() == now.getYear()) && (date.getMonth() == now.getMonth()) && (date.getDate() + 1 == now.getDate());
    }

    private static boolean isThisWeek(Date date) {
        Date now = new Date();
        if ((date.getYear() == now.getYear()) && (date.getMonth() == now.getMonth())) {
            if (now.getDay() - date.getDay() < now.getDay() && now.getDate() - date.getDate() > 0 && now.getDate() - date.getDate() < 7) {
                return true;
            }
        }
        return false;
    }

    private static boolean isThisYear(Date date) {
        Date now = new Date();
        return date.getYear() == now.getYear();
    }

    /**
     * 通过参数时间获取时分格式 12:00
     *
     * @param time "2019-04-06 00:00:00"
     * @return
     */
    public static String getHourMinute(String time) {
        try {
            String[] strings = time.split(" ")[1].split(":");
            return strings[0] + ":" + strings[1];
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public static String getCustomTime(String startTime, String endTime) {
        String strTime = "";
        try {
            String start = startTime.substring(5, 7) + "月" + startTime.substring(8, 10) + "日 "
                    + startTime.substring(11, 16);
            String end = endTime.substring(11, 16);
            strTime = start + "-" + end;
        } catch (Exception e) {
            return strTime;
        }
        return strTime;
    }
}
