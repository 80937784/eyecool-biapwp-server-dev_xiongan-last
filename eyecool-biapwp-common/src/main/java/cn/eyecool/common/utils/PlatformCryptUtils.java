package cn.eyecool.common.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yytech.frame.utils.HexUtils;

import cn.eyecool.common.exception.CustomException;
import cn.eyecool.common.utils.file.PlatformFileUtils;
import cn.eyecool.fox.security.CryptService;
import cn.eyecool.fox.security.ECS1CryptService;

/**
 * 平台加密解密工具类封装
 *
 * @author admin
 * @date 2019年11月1日
 */
public class PlatformCryptUtils {

    private static final Logger LOG = LoggerFactory.getLogger(PlatformCryptUtils.class);
    public static final CryptService cryptService = new ECS1CryptService();

    private PlatformCryptUtils() {}

    /**
     * 字节加密（支持仅仅包含16进制字符的字节数组，否则解密失败）
     *
     * @param data
     * @return
     */
    public static String encryptUTF8(byte[] data) {
        return HexUtils.encodeHexString(cryptService.encrypt(data), false);
    }

    /**
     * 字符串解密（支持仅仅包含16进制字符的字符串，否则解密失败）
     *
     * @param data
     * @return
     */
    public static String decryptUTF8(String data) {
        try {
            return new String(cryptService.decrypt(HexUtils.decodeHex(data)), StandardCharsets.UTF_8);
        } catch (DecoderException e) {
            LOG.error("Data decryption error", e);
            throw new CustomException(MessageUtils.message("platform.crypt.utils.decrypt.error"));
        }
    }

    /**
     * 图像base64加密
     *
     * @param data
     * @return
     */
    public static String encryptImageBase64(String imageBase64) {
        return Base64.encodeBase64String(cryptService.encrypt(Base64.decodeBase64(imageBase64)));
    }

    /**
     * 图像base64解密
     *
     * @param data
     * @return
     */
    public static String decryptImageBase64(String imageBase64) {
        if (StringUtils.isNotEmpty(imageBase64)) {
            return Base64.encodeBase64String(cryptService.decrypt(Base64.decodeBase64(imageBase64)));
        }
        return StringUtils.EMPTY;
    }

    /**
     * 文件流转Base64加密
     *
     * @param in
     * @return
     */
    public static String encryptStreamToBase64(InputStream in) {
        return encryptImageBase64(PlatformFileUtils.getImageBase64(in));
    }

    /**
     * 文件流转Base64解密
     *
     * @param in
     * @return
     */
    public static String decryptStreamToBase64(InputStream in) {
        return decryptImageBase64(PlatformFileUtils.getImageBase64(in));
    }

    /**
     * 文件流转字节数组加密
     *
     * @param in
     * @return
     */
    public static byte[] encryptStreamToBytes(InputStream in) {
        return cryptService.encrypt(Base64.decodeBase64(PlatformFileUtils.getImageBase64(in)));
    }

    /**
     * 文件流转字节数组解密
     *
     * @param in
     * @return
     */
    public static byte[] decryptStreamToBytes(InputStream in) {
        return cryptService.decrypt(Base64.decodeBase64(PlatformFileUtils.getImageBase64(in)));
    }

    /**
     * Description decryptBase64 解密C#前端加密的数据
     * 
     * @param encryptBase64 c# 加密后 转base64后字符串
     * @return java.lang.String
     * @author zfx
     * @date 2019/11/21 18:46
     */
    public static String decryptBase64(String encryptBase64) {
        byte[] decoded = Base64.decodeBase64(encryptBase64);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        cryptService.decrypt(null, new ByteArrayInputStream(decoded), baos);
        return new String(baos.toByteArray());
    }
}
