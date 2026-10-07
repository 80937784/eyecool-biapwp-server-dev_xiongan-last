package cn.eyecool.tradelog;

import java.security.SecureRandom;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author zfx
 * @ClassName ViidIdGenerator
 * @description 公安接口涉及字段生成
 * @since 2026/8/21 16:49
 **/
public class ViidIdGenerator {
    private ViidIdGenerator(){}

    private static final char[] ALPHABET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    /**
     * 生成FaceId 48位（GA/T1400）
     * @return 48位大写数字字母ID
     */
    public static String generateFaceId() {
        int targetLen = 48;
        char[] buffer = new char[targetLen];
        for (int i = 0; i < targetLen; i++) {
            buffer[i] = ALPHABET[SECURE_RANDOM.nextInt(ALPHABET.length)];
        }
        return new String(buffer);
    }

    /**
     * 生成ImageId 41位（GA/T1400）
     * @return 41位ID
     */
    public static String generateImageId() {
        int targetLen = 41;
        char[] buffer = new char[targetLen];
        for (int i = 0; i < targetLen; i++) {
            buffer[i] = ALPHABET[SECURE_RANDOM.nextInt(ALPHABET.length)];
        }
        return new String(buffer);
    }
    /**
     * 生成指定位数纯数字随机字符串，最大支持48位
     * @param digit 位数 1 ~ 48
     * @param allowLeadingZero 是否允许前导0；true=允许(例如001234)，false=第一位不为0
     * @return 定长数字字符串
     */
    public static String getRandomDigitStr(int digit, boolean allowLeadingZero) {
        if (digit < 1 || digit > 48) {
            throw new IllegalArgumentException("位数必须在1~48之间");
        }
        StringBuilder sb = new StringBuilder(digit);
        ThreadLocalRandom random = ThreadLocalRandom.current();

        //第一位处理
        if (!allowLeadingZero) {
            //第一位：1‑9
            sb.append(random.nextInt(1, 10));
            //剩余位
            for (int i = 1; i < digit; i++) {
                sb.append(random.nextInt(10));
            }
        } else {
            //全部位0‑9，允许前导零
            for (int i = 0; i < digit; i++) {
                sb.append(random.nextInt(10));
            }
        }
        return sb.toString();
    }
    //测试
    public static void main(String[] args) {
        String faceId = generateFaceId();
        System.out.println("FaceId["+faceId.length()+"]:" + faceId);

        String imageId = generateImageId();
        System.out.println("ImageId["+imageId.length()+"]:" + imageId);
    }
}
