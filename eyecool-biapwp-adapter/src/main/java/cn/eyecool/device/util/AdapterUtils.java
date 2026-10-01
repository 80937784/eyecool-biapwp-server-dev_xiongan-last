package cn.eyecool.device.util;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.codec.binary.Base64;

import cn.eyecool.common.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * V1.0 工具类
 *
 * @author 段存明
 * @date 2021/03/04 10:03
 **/
@Slf4j
public class AdapterUtils {

    public static final String PATTERN_YMD_HMS = "yyyy-MM-dd HH:mm:ss";
    public static final String ENCODING = "UTF-8";

    /**
     * 加密 encrypt
     *
     * @param value 字符串
     * @param secret secret
     * @return
     */
    public static String encrypt(String value, char secret) {
        if (StringUtils.isEmpty(value)) {
            return null;
        }
        byte[] bt = value.getBytes();
        for (int i = 0; i < bt.length; i++) {
            bt[i] = (byte)(bt[i] ^ secret);
        }
        String str = new String(bt, 0, bt.length);
        String result = "";
        try {
            byte[] encodedByte = Base64.encodeBase64URLSafe(str.getBytes(ENCODING));
            result = new String(encodedByte, ENCODING);
        } catch (Exception e) {
            log.error("encrypt error :[{}]", e.toString());
        }
        return result;

    }

    /**
     * 解密 dencrypt
     *
     * @param value 字符串
     * @param secret secret
     * @return
     */
    public static String dencrypt(String value, char secret) {
        if (StringUtils.isEmpty(value)) {
            return null;
        }
        String encoderStr = "";
        String str = "";
        try {
            byte[] decodedByte = Base64.decodeBase64(value.getBytes(ENCODING));
            encoderStr = new String(decodedByte, ENCODING);
            byte[] bt = encoderStr.getBytes(ENCODING);
            for (int i = 0; i < bt.length; i++) {
                bt[i] = (byte)(bt[i] ^ secret);
            }
            str = new String(bt, 0, bt.length);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            log.error("dencrypt error : [{}]", e.toString());
        }

        return str;

    }

    /**
     * 判断文件大小
     *
     * @param file 文件
     * @param size 限制大小
     * @param unit 限制单位（B,K,M,G）
     * @return
     */
    public static boolean checkFileSize(File file, int size, String unit) {
        long len = file.length();
        double fileSize = 0;
        // B K M G
        if ("B".equals(unit.toUpperCase())) {
            fileSize = len;
        } else if ("K".equals(unit.toUpperCase())) {
            fileSize = (double)len / 1024;
        } else if ("M".equals(unit.toUpperCase())) {
            fileSize = (double)len / 1048576;
        } else if ("G".equals(unit.toUpperCase())) {
            fileSize = (double)len / 1073741824;
        }
        if (fileSize > size) {
            return false;
        }
        return true;
    }

    /**
     * 格式化 yyyy-MM-dd HH:mm:ss
     * 
     * @param date 日期
     * @return
     */
    public static String getDateFormatForSecond(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat(PATTERN_YMD_HMS);
        String ymd = sdf.format(date);
        return ymd;
    }

    /**
     * 格式化 yyyy-MM-ddHH:mm:ss
     * 
     * @param date 日期
     * @return
     */
    public static String getDateFormatForSecond(Date date, String pt) {
        SimpleDateFormat sdf = new SimpleDateFormat(pt);
        String ymd = sdf.format(date);
        return ymd;
    }

    /**
     * getDate
     *
     * @param strDate 日期
     * @return
     */
    public static Date getDate(String strDate) {
        SimpleDateFormat format = new SimpleDateFormat(PATTERN_YMD_HMS);
        Date date = null;
        try {
            date = format.parse(strDate);
        } catch (Exception ex) {
            log.error("getDate is error:[{}]", ex.toString());
        }
        return date;
    }

    /**
     * 添加日期
     *
     * @param inDate 日期
     * @param hour 小时
     * @return
     */
    public static Date addDate(Date inDate, int hour) {
        Date date = null;
        if (inDate == null) {
            return date;
        }
        try {
            Calendar cal = Calendar.getInstance();
            cal.setTime(inDate);
            cal.add(Calendar.HOUR, hour);
            date = cal.getTime();
        } catch (Exception e) {
            log.error("addDateMinut error : [{}]", e.toString());
        }
        return date;
    }
}
