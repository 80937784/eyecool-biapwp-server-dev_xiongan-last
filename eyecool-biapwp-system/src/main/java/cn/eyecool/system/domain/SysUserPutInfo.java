package cn.eyecool.system.domain;

import java.util.List;

import cn.eyecool.common.core.domain.entity.SysUser;

public class SysUserPutInfo extends SysUser {
    private static final long serialVersionUID = 1L;
    /**
     * 人脸入库信息
     */
    private SysUserFacePutInfo facePutInfo;
    /**
     * 指纹入库信息列表
     */
    private List<SysUserFingerPutInfo> fingerPutInfoList;
    /**
     * 虹膜入库信息列表
     */
    private SysUserIrisPutInfo irisPutInfo;
    /**
     * 人脸虹膜多模态入库信息
     */
    private SysUserIrisFacePutInfo irisFacePutInfo;

    public static class SysUserFacePutInfo {

        private String id;
        /**
         * 图片Base64
         */
        private String imageBase64;

        public String getImageBase64() {
            return imageBase64;
        }

        public void setImageBase64(String imageBase64) {
            this.imageBase64 = imageBase64;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public SysUserFacePutInfo() {}
    }

    public static class SysUserFingerPutInfo {

        private String id;
        /**
         * 手指编号
         */
        private String fingerNo;
        /**
         * 图片Base64
         */
        private String imageBase64;

        public String getFingerNo() {
            return fingerNo;
        }

        public void setFingerNo(String fingerNo) {
            this.fingerNo = fingerNo;
        }

        public String getImageBase64() {
            return imageBase64;
        }

        public void setImageBase64(String imageBase64) {
            this.imageBase64 = imageBase64;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public SysUserFingerPutInfo() {}
    }

    public static class SysUserIrisPutInfo {
        private String id;
        /**
         * 图片Base64
         */
        private String imageBase64;

        /** 图片特征 */
        private String feature;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getImageBase64() {
            return imageBase64;
        }

        public void setImageBase64(String imageBase64) {
            this.imageBase64 = imageBase64;
        }

        public String getFeature() {
            return feature;
        }

        public void setFeature(String feature) {
            this.feature = feature;
        }

        public SysUserIrisPutInfo() {}
    }

    public static class SysUserIrisFacePutInfo {

        private String id;
        /** 人脸base64 */
        private String faceImgBase64;
        /** 虹膜base64 */
        private String irisImgBase64;
        /** 虹膜特征 */
        private String irisFeature;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getFaceImgBase64() {
            return faceImgBase64;
        }

        public void setFaceImgBase64(String faceImgBase64) {
            this.faceImgBase64 = faceImgBase64;
        }

        public String getIrisImgBase64() {
            return irisImgBase64;
        }

        public void setIrisImgBase64(String irisImgBase64) {
            this.irisImgBase64 = irisImgBase64;
        }

        public String getIrisFeature() {
            return irisFeature;
        }

        public void setIrisFeature(String irisFeature) {
            this.irisFeature = irisFeature;
        }

        public SysUserIrisFacePutInfo() {
            super();
        }

    }

    public SysUserFacePutInfo getFacePutInfo() {
        return facePutInfo;
    }

    public void setFacePutInfo(SysUserFacePutInfo facePutInfo) {
        this.facePutInfo = facePutInfo;
    }

    public List<SysUserFingerPutInfo> getFingerPutInfoList() {
        return fingerPutInfoList;
    }

    public void setFingerPutInfoList(List<SysUserFingerPutInfo> fingerPutInfoList) {
        this.fingerPutInfoList = fingerPutInfoList;
    }

    public SysUserIrisPutInfo getIrisPutInfo() {
        return irisPutInfo;
    }

    public void setIrisPutInfo(SysUserIrisPutInfo irisPutInfo) {
        this.irisPutInfo = irisPutInfo;
    }

    public SysUserIrisFacePutInfo getIrisFacePutInfo() {
        return irisFacePutInfo;
    }

    public void setIrisFacePutInfo(SysUserIrisFacePutInfo irisFacePutInfo) {
        this.irisFacePutInfo = irisFacePutInfo;
    }

    public SysUserPutInfo() {
        super();
    }

}
