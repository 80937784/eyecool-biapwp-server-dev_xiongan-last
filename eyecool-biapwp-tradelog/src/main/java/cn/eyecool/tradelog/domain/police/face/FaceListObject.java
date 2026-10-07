package cn.eyecool.tradelog.domain.police.face;

import com.alibaba.fastjson.annotation.JSONField;

import java.util.List;

/**
 * @author zfx
 * @ClassName FaceListObject
 * @description
 * @since 2026/9/29 16:23
 **/
public class FaceListObject {
    @JSONField(name = "FaceObject")
    private List<FaceObject> faceObject;

    public List<FaceObject> getFaceObject() {
        return faceObject;
    }

    public void setFaceObject(List<FaceObject> faceObject) {
        this.faceObject = faceObject;
    }
}
