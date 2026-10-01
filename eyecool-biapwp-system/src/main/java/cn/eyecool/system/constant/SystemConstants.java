package cn.eyecool.system.constant;

/**
 * @author : sunhuayu
 * @version V1.0
 * @Project: eyecool-biapwp
 * @Package cn.eyecool.system.constant
 * @Description: TODO
 * @date Date : 2021年01月19日 上午11:33
 */
public interface SystemConstants {
    interface BioFeatureType {
        public static final String FACE_FEATURE = "1";
        public static final String FINGER_FEATURE_UNKNOWN = "2";
        public static final String IRIS_FEATURE_UNKNOWN = "3";
        public static final String FVEIN_FEATURE_UNKNOWN = "4";
    }

    interface SYS_USER_BIO_IMAGE_DIR {
        public static final String FACE_DIR = "./eyecool/system/bio/face/";
        public static final String FINGER_DIR = "./eyecool/system/bio/finger/";
        public static final String IRIS_DIR = "./eyecool/system/bio/iris/";
        public static final String FVEIN_DIR = "./eyecool/system/bio/fvein/";
        public static final String MULTI_DIR = "./eyecool/system/bio/multi/";
    }

    public static final String SDK_UPLOAD_DIR = "./eyecool/system/sdk/";
}
