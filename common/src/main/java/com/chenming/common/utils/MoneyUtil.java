package com.chenming.common.utils;

import java.math.BigDecimal;
import java.text.DecimalFormat;

public class MoneyUtil {

    public static String fen2Yuan(int fen) {
        return new DecimalFormat("0.00").format((double) fen / 100);
    }

    public static String fen2Yuan2(int fen) {
        return new DecimalFormat("0").format((double) fen / 100);
    }

    public static String fen2Yuan(double fen) {
        return new DecimalFormat("0.00").format((double) fen / 100);
    }

    /**
     * 转成分
     * @param str
     * @return
     */
    public static int strToMoney(String str) {
        return (int) ((Math.rint(Double.valueOf(str) * 100)));
    }

    /**
     * 设置小数为两位(double)
     *
     * @param price
     * @param decm
     * @return
     */
    public static String doubletostring(double price, int decm) {
        String sb = "";
        DecimalFormat df;
        switch (decm) {
            case 0:
                df = new DecimalFormat("#");
                sb += df.format(price);
                break;
            case 1:
                df = new DecimalFormat("#0.0");
                sb += df.format(price);
                break;
            case 2:
                df = new DecimalFormat("#0.00");
                sb += df.format(price);
                break;
            case 3:
                df = new DecimalFormat("#0.000");
                sb += df.format(price);
                break;
            case 4:
                df = new DecimalFormat("#0.0000");
                sb += df.format(price);
                break;
            case 5:
                df = new DecimalFormat("#0.00000");
                sb += df.format(price);
                break;
            case 6:
                df = new DecimalFormat("#0.000000");
                sb += df.format(price);
                break;
        }
        return sb;
    }

    public static String floatToString(float price) {
        String sb = "";
        DecimalFormat df;
        df = new DecimalFormat("#0.00");
        sb += df.format(price);
        return sb;
    }

    /**
     * 直接删除多余的小数位
     *
     * @return
     */
    public static String BigDecimalDouble(int i, double j) {
        BigDecimal b = new BigDecimal(j).setScale(i, BigDecimal.ROUND_HALF_UP);
        return b.toString();
    }

    /**
     * int分转元
     */
    public static String intDivisor(int number) {
        double format = (double) number / 100;
        DecimalFormat df = new DecimalFormat("0.00");
        return df.format(format);
    }
}
