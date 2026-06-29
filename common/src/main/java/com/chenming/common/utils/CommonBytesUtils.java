package com.chenming.common.utils;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by qixianWu on 2020/12/30.
 */
public class CommonBytesUtils {


    /**
     * 将大数组拆分为多个小数组
     *
     * @param superbyte 需要切割的数据
     * @param size      切割的长度
     * @return
     */
    public static List<byte[]> splitByteList(byte[] superbyte, int size) {
        List<byte[]> result = new ArrayList<>();
        int length = superbyte.length;
        int count = length / size;
        int r = length % size;
        for (int i = 0; i < count; i++) {
            byte[] newBytes = new byte[size];
            System.arraycopy(superbyte, size * i, newBytes, 0, size);
            result.add(newBytes);
        }
        if (r != 0) {
            byte[] newBytes = new byte[r];
            System.arraycopy(superbyte, length - r, newBytes, 0, r);
            result.add(newBytes);
        }

        return result;
    }

    /**
     * 获取字符串字节长度
     */
    public static int getLength(String s) {
        int length = 0;
        for (int i = 0; i < s.length(); i++) {
            int ascii = Character.codePointAt(s, i);
            if (ascii >= 0 && ascii <= 255) {
                length++;
            } else {
                length += 2;
            }
        }
        return length / 2;
    }

    private static String hexString = "0123456789ABCDEF";

    /*
     * 将字符串编码成16进制数字,适用于所有字符（包括中文）
     */
    public static String encode(String str) {
        // 根据默认编码获取字节数组
        byte[] bytes = new byte[0];
        try {
            //bytes = str.getBytes("GBK");
            bytes = str.getBytes("GB2312");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        // 将字节数组中每个字节拆解成2位16进制整数
        for (int i = 0; i < bytes.length; i++) {
            sb.append(hexString.charAt((bytes[i] & 0xf0) >> 4));
            sb.append(hexString.charAt((bytes[i] & 0x0f) >> 0));
        }
        return sb.toString();
    }

    /**
     * 字节数组转16进制字符串
     */
    public static String bytesToHexString(byte[] src, int length) {
        StringBuilder stringBuilder = new StringBuilder("");
        if (src == null || src.length <= 0) {
            return null;
        }
        for (int i = 0; i < length; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            hv.toUpperCase();
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv + "");
        }
        return stringBuilder.toString();
    }

    /**
     * 16进制字符串转byte数组
     *
     * @param hexString
     * @return
     */
    public static byte[] hexStringToBytes(String hexString) {
        if (hexString == null || hexString.equals("")) {
            return null;
        }
        hexString = hexString.toUpperCase();
        int length = hexString.length() / 2;
        char[] hexChars = hexString.toCharArray();
        byte[] d = new byte[length];
        for (int i = 0; i < length; i++) {
            int pos = i * 2;
            d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
        }
        return d;
    }

    private static byte charToByte(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }

    /**
     * 将指定byte数组以16进制的形式
     *
     * @param b
     */
    public static String printHexString(byte[] b) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < b.length; i++) {
            String hex = Integer.toHexString(b[i] & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            stringBuilder.append(hex + " ");
        }
        return stringBuilder.toString();
    }

    /**
     * 二进制转十六进制String
     *
     * @param src
     * @return
     */
    public static String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder();
        if (src == null || src.length <= 0) {
            return null;
        }
        for (int i = 0; i < src.length; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
        }
        return stringBuilder.toString();
    }

    /**
     * 16进制数转2进制字符串
     *
     * @param hexstr
     * @return
     */
    public static String hexToBinary(String hexstr) {
        Integer num = Integer.parseInt(hexstr, 16);
        String result = Integer.toBinaryString(num);
        if (result.length() == 1) {
            result = "0000000" + result;
        } else if (result.length() == 2) {
            result = "000000" + result;
        } else if (result.length() == 3) {
            result = "00000" + result;
        } else if (result.length() == 4) {
            result = "0000" + result;
        } else if (result.length() == 5) {
            result = "000" + result;
        } else if (result.length() == 6) {
            result = "00" + result;
        } else if (result.length() == 7) {
            result = "0" + result;
        }
        return result;
    }

    /**
     * 对象转换成字节数组
     */
    public static byte[] ObjectToByteArray(Object obj) {
        byte[] buff = null;

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(obj);
            oos.flush();
            buff = bos.toByteArray();
            oos.close();
            bos.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return buff;
    }

    /**
     * 字符串转换成为16进制(无需Unicode编码)
     *
     * @param str
     * @return
     */
    public static String str2HexStr(String str) {
        char[] chars = "0123456789ABCDEF".toCharArray();
        StringBuilder sb = new StringBuilder("");
        byte[] bs = str.getBytes();
        int bit;
        for (int i = 0; i < bs.length; i++) {
            bit = (bs[i] & 0x0f0) >> 4;
            sb.append(chars[bit]);
            bit = bs[i] & 0x0f;
            sb.append(chars[bit]);
            // sb.append(' ');
        }
        return sb.toString().trim();
    }

    public static int bytesToInt(byte[] bytes) {
        return bytes[0] << 24 | (bytes[1] & 0xff) << 16 | (bytes[2] & 0xff) << 8 | (bytes[3] & 0xff);
    }

    /**
     * @param hexString
     * @return 将十六进制转换为字节数组
     */
    public static byte[] HexStringToBinary(String hexString) {
        //hexString的长度对2取整，作为bytes的长度
        int len = hexString.length() / 2;
        byte[] bytes = new byte[len];
        byte high = 0;//字节高四位
        byte low = 0;//字节低四位

        for (int i = 0; i < len; i++) {
            //右移四位得到高位
            high = (byte) ((hexString.indexOf(hexString.charAt(2 * i))) << 4);
            low = (byte) hexString.indexOf(hexString.charAt(2 * i + 1));
            bytes[i] = (byte) (high | low);//高地位做或运算
        }
        return bytes;
    }

    /**
     * 将一个整形化为十六进制，并以字符串的形式返回
     */
    private final static String[] hexArray = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "a", "b", "c", "d", "e", "f"};

    public static String intToHexStr(int n) {
        if (n < 0) {
            n = n + 256;
        }
        int d1 = n / 16;
        int d2 = n % 16;
        return hexArray[d1] + hexArray[d2];
    }


    /**
     * 16进制字符串转成10进制字符串
     */
    public static String hexToStr(String str) {
        return new BigInteger(str, 16).toString(10);
    }

    /**
     * 16进制转ASCII
     *
     * @param hex
     * @return
     */
    public static String hextoAsciiStr(String hex) {
        StringBuilder sb = new StringBuilder();
        StringBuilder temp = new StringBuilder();
        for (int i = 0; i < hex.length() - 1; i += 2) {
            //grab the hex in pairs
            String output = hex.substring(i, (i + 2));
            //convert hex to decimal
            int decimal = Integer.parseInt(output, 16);
            //convert the decimal to character
            sb.append((char) decimal);
            temp.append(decimal);
        }
        return sb.toString();
    }

    public static byte convert(String sn, boolean isLittleEndian) {
        int v = 0;
        try {
            v = Integer.parseInt(sn);
        } catch (Exception ex) {
            ex.printStackTrace();
            return 0;
        }

        if (v > 255) {
            System.out.println("值" + String.valueOf(v) + "超出0~255, 取" + String.valueOf(v & 0xff));
            v &= 0xff;
        }
        int n = 0;
        if (isLittleEndian)
            n = 3;
        return Integer.toHexString((byte) v).getBytes()[n];
    }


    /**
     * 16进制byte转10进制的int
     *
     * @return
     */
    public static int hexByte2Int(byte b) {
        String res = String.format("%02x", new Integer(b & 0xff)).toUpperCase();
        int i = hex2decimal(res);
        return i;
    }

    /**
     * 16进制转10进制
     *
     * @param hex
     * @return
     */
    public static int hex2decimal(String hex) {
        return Integer.parseInt(hex, 16);
    }


    public static String longToHex(long n) {
        return Long.toHexString(n);
    }

    public static String intToHex(int n) {
        //StringBuffer s = new StringBuffer();
        StringBuilder sb = new StringBuilder();
        String a;
        char[] b = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
        while (n != 0) {
            sb = sb.append(b[n % 16]);
            n = n / 16;
        }

        a = sb.reverse().toString();

        if (a.length() == 1) {
            a = "0" + a;
        } else if (a.length() == 0) {
            a = "00";
        }
        return a;
    }

    public static String intToHexRepairZero(int n) {
        //StringBuffer s = new StringBuffer();
        StringBuilder sb = new StringBuilder();
        String a;
        char[] b = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
        while (n != 0) {
            sb = sb.append(b[n % 16]);
            n = n / 16;
        }


        a = sb.reverse().toString();
        if (a.length() == 2) {
            a = "00" + a;
        } else if (a.length() == 3) {
            a = "0" + a;
        } else if (a.length() == 1) {
            a = "000" + a;
        } else if (a.length() == 0) {
            a = "0000";
        }
        return a;
    }


    //int 转16
    public static String numToHex(int b) {
        if (b > 255) {
            return String.format("%04x", b);
        }
        return String.format("%02x", b);//2表示需要两个16进制数
    }


    //int 转16 强制4位
    public static String numToHexFour(int b) {
        return String.format("%04x", b);
    }

    //int 转16 强制4位
    public static String numToHexSix(int b) {
        return String.format("%06x", b);
    }

    /**
     * 获取数据长度(强制4位)
     *
     * @param msg
     * @return
     */
    public static String getMsgLength(String msg) {
        int i = msg.length() / 2;
        return CommonBytesUtils.numToHexFour(i);
    }


    public static byte[] HexCommandtoByte(String mstrRestartSend) {
        byte[] data = mstrRestartSend.getBytes();
        if (data == null) {
            return null;
        }
        int nLength = data.length;

        String strTemString = new String(data, 0, nLength);
        String[] strings = strTemString.split(" ");
        nLength = strings.length;
        data = new byte[nLength];
        for (int i = 0; i < nLength; i++) {
            if (strings[i].length() != 2) {
                data[i] = 00;
                continue;
            }
            try {
                data[i] = (byte) Integer.parseInt(strings[i], 16);
            } catch (Exception e) {
                data[i] = 00;
                continue;
            }
        }

        return data;
    }

    public static byte[] int2ByteCast(int i) {
        String s = Integer.toHexString(i);
        if (s.length() % 2 != 0)
            s = "0" + s;
        List<String> strings = new ArrayList<>();
        for (int j = 0; j < s.length(); j += 2) {
            strings.add(s.charAt(j) + "" + s.charAt(j + 1));
        }

        byte[] bytes = new byte[strings.size()];
        for (int j = 0; j < strings.size(); j++) {
            bytes[j] = (byte) Short.parseShort(strings.get(j), 16);
        }
        for (int j = 0; j < bytes.length; j++) {
            System.out.print(bytes[j]);
        }
        return bytes;
    }

    public static int[] string2ASCII(String s) {// 字符串转换为ASCII码
        if (s == null || "".equals(s)) {
            return null;
        }

        char[] chars = s.toCharArray();
        int[] asciiArray = new int[chars.length];

        for (int i = 0; i < chars.length; i++) {
            asciiArray[i] = char2ASCII(chars[i]);
        }
        return asciiArray;
    }

    public static int char2ASCII(char c) {
        return (int) c;
    }

    public static int byte2ASCII(byte c) {
        return (int) c;
    }

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }


    /**
     * Hex字符串转byte
     *
     * @param inHex 待转换的Hex字符串
     * @return 转换后的byte
     */
    public static byte hexToByte(String inHex) {
        return (byte) Integer.parseInt(inHex, 16);
    }

    /**
     * hex字符串转byte数组
     *
     * @param inHex 待转换的Hex字符串
     * @return 转换后的byte数组结果
     */
    public static byte[] hexToByteArray(String inHex) {
        int hexlen = inHex.length();
        byte[] result;
        if (hexlen % 2 == 1) {
            //奇数
            hexlen++;
            result = new byte[(hexlen / 2)];
            inHex = "0" + inHex;
        } else {
            //偶数
            result = new byte[(hexlen / 2)];
        }
        int j = 0;
        for (int i = 0; i < hexlen; i += 2) {
            result[j] = hexToByte(inHex.substring(i, i + 2));
            j++;
        }
        return result;
    }


    /**
     * 16进制中的字符集
     */
    private static final String HEX_CHAR = "0123456789ABCDEF";

    /**
     * 16进制中的字符集对应的字节数组
     */
    private static final byte[] HEX_STRING_BYTE = HEX_CHAR.getBytes();

    /**
     * 10进制字节数组转换为16进制字节数组
     * <p>
     * byte用二进制表示占用8位，16进制的每个字符需要用4位二进制位来表示，则可以把每个byte
     * 转换成两个相应的16进制字符，即把byte的高4位和低4位分别转换成相应的16进制字符，再取对应16进制字符的字节
     *
     * @param b 10进制字节数组
     * @return 16进制字节数组
     */
    public static byte[] byte2hex(byte[] b) {
        int length = b.length;
        byte[] b2 = new byte[length << 1];
        int pos;
        for (int i = 0; i < length; i++) {
            pos = 2 * i;
            b2[pos] = HEX_STRING_BYTE[(b[i] & 0xf0) >> 4];
            b2[pos + 1] = HEX_STRING_BYTE[b[i] & 0x0f];
        }
        return b2;
    }

    /**
     * 16进制字节数组转换为10进制字节数组
     * <p>
     * 两个16进制字节对应一个10进制字节，则将第一个16进制字节对应成16进制字符表中的位置(0~15)并向左移动4位，
     * 再与第二个16进制字节对应成16进制字符表中的位置(0~15)进行或运算，则得到对应的10进制字节
     *
     * @param b 10进制字节数组
     * @return 16进制字节数组
     */
    public static byte[] hex2byte(byte[] b) {
        if (b.length % 2 != 0) {
            throw new IllegalArgumentException("byte array length is not even!");
        }

        int length = b.length >> 1;
        byte[] b2 = new byte[length];
        int pos;
        for (int i = 0; i < length; i++) {
            pos = i << 1;
            b2[i] = (byte) (HEX_CHAR.indexOf(b[pos]) << 4 | HEX_CHAR.indexOf(b[pos + 1]));
        }
        return b2;
    }

    /**
     * 将16进制字节数组转成10进制字符串
     *
     * @param b 16进制字节数组
     * @return 10进制字符串
     */
    public static String hex2Str(byte[] b) {
        return new String(hex2byte(b));
    }

    /**
     * 将10进制字节数组转成16进制字符串
     *
     * @param b 10进制字节数组
     * @return 16进制字符串
     */
    public static String byte2HexStr(byte[] b) {
        return Integer.toHexString(Integer.parseInt(new String(b)));
    }


}
