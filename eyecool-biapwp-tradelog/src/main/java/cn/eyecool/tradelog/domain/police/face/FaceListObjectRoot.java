package cn.eyecool.tradelog.domain.police.face;

import com.alibaba.fastjson.annotation.JSONField;

/**
 * @author zfx
 * @ClassName FaceListObjectRoot
 * @description 人脸上传对象
 * @since 2026/9/29 16:16
 **/
public class FaceListObjectRoot {
    @JSONField(name = "FaceListObject")
    private FaceListObject faceListObject;

    public FaceListObject getFaceListObject() {
        return faceListObject;
    }

    public void setFaceListObject(FaceListObject faceListObject) {
        this.faceListObject = faceListObject;
    }
}





