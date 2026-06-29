package com.chenming.common.utils;

import java.util.ArrayList;
import java.util.List;

public class StringUtils {

    /**
     * 字符串补FF
     *
     * @param source
     * @param length
     * @return
     */
    public static String strFillFF(String source, int length) {
        return (source + "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF").substring(0, length);
    }

    /**
     * 去掉尾部的F
     *
     * @param source
     * @return
     */
    public static String removeTailF(String source) {
        String newStr = source.replaceAll("F+$", "");
        return newStr;
    }


    /**
     * 字符串是不是全部都是一个K
     *
     * @param source
     * @param key
     * @return
     */
    public static boolean stringIsAllKey(String source, String key) {
        String newStr = source.replaceAll(key + "+$", "");
        return newStr.length() == 0;
    }

    /**
     * 字符串是不是都是同一个
     *
     * @return
     */
    public static boolean stringCharIsSame(String source, String key) {
        List<String> strList = getStrList(source, 1);

        for (int i = 0; i < strList.size() - 1; i++) {
            if (!strList.get(i).equals(key)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 把原始字符串分割成指定长度的字符串列表 * * @param inputString * 原始字符串 * @param length * 指定长度 * @return
     */
    public static List<String> getStrList(String inputString, int length) {
        int size = inputString.length() / length;
        if (inputString.length() % length != 0) {
            size += 1;
        }
        return getStrList(inputString, length, size);
    }

    /**
     * 把原始字符串分割成指定长度的字符串列表 * * @param inputString * 原始字符串 * @param length * 指定长度 * @param size * 指定列表大小 * @return
     */
    public static List<String> getStrList(String inputString, int length,
                                          int size) {
        List<String> list = new ArrayList<String>();
        for (int index = 0; index < size; index++) {
            String childStr = substring(inputString, index * length,
                    (index + 1) * length);
            list.add(childStr);
        }
        return list;
    }

    /**
     * 分割字符串，如果开始位置大于字符串长度，返回空 * * @param str * 原始字符串 * @param f * 开始位置 * @param t * 结束位置 * @return
     */
    public static String substring(String str, int f, int t) {
        if (f > str.length())
            return null;
        if (t > str.length()) {
            return str.substring(f, str.length());
        } else {
            return str.substring(f, t);
        }
    }
}
