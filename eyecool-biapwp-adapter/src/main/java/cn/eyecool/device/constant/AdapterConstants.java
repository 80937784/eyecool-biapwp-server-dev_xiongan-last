package cn.eyecool.device.constant;

import cn.eyecool.common.utils.MessageUtils;

/**
 * @author : 段存明
 * @version V1.0
 * @Project: eyecool-biapwp
 * @Package cn.eyecool.biapwp.system.constant
 * @date Date : 2021年02月24日 上午11:33
 */
public class AdapterConstants {

    /**
     * 数据字典配置信息 DevDictConf
     */
    public static final class DevDictConf {
        public static final String DICT_KEY = "dev_203_conf";
        public static final String BIAPWP_URL = "biapwp.url";
    }

    /**
     * PersonType
     */
    public static final class PersonType {
        public static final String TYPE1 = "1";
        public static final String TYPE2 = "2";
        public static final String TYPE3 = "3";
    }

    /**
     * OperFlag
     */
    public static final class OperFlag {
        public static final String ADD = "0";
        public static final String DELETE = "1";
    }

    /**
     * OperFlag
     */
    public static final class PermitDate {
        public static final String START_DATE = "1970-01-01";
        public static final String END_DATE = "2170-01-01";
    }

    /**
     * OperMethod
     */
    public static final class OperMethod {
        public static final String ADD = "personnelData.savePersons";
        public static final String DELETE = "personnelData.removePersons";
        public static final String FACE_CODE = "EventFaceRecognizeCutout";
        public static final String MATCH_CODE = "EventFaceAndIDCardsRecognize";
        public static final String FACE_IDCORD = "EventFaceAndCardsCompare";
        public static final String CARD_CODE = "CardDetect";
        public static final String CARD_CODE2 = "EventCardDetectSnapshot";
        // 升级任务发布
        public static final String UPGRADE = "cloud.firmwareUpgrade";
    }

    public static final String MALE_CODE = "0";
    public static final String DEF_GROUP = MessageUtils.message("default.permission.group");
    public static final String MALE = "male";
    public static final String FEMALE = "female";
    public static final String DEF_BIRTHDAY = "1995-01-01";
    public static final String JPG = ".jpg";
    public static final String TIME_ZONE = "GMT+8";
    public static final String PASS = "0";
    public static final String DEF_ADMIN = "admin";
    public static final String DEF_SERINUM = "0";
    public static final char SECRET = 'M';
    public static final int ERR_CODE = 589840;

    /**
     * unit
     */
    public static final class Unit {
        public static final String B = "B";
        public static final String K = "K";
        public static final String M = "M";
        public static final String G = "G";
    }

    /** 人员图片 */
    public static final int IMG_WIDTH = 800;
    public static final int IMG_HEIGH = 800;
    public static final int MAX_FILESIZE = 500;
    public static final int VALID_HOUR_NUM = 8;

    /** 设备升级任务缓存前缀 */
    public static String DEVICE_UPGRADE_TASK_CACHE_PREFIX = "device:upgradeTask:";
    /** 获取设备版本信息会话前缀 */
    public static String DEVICE_VERSION_QUERY_CACHE_PREFIX = "device:getVersion:";
}
