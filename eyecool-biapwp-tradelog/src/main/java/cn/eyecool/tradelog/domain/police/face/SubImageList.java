package cn.eyecool.tradelog.domain.police.face;

import com.alibaba.fastjson.annotation.JSONField;

import java.util.List;

/**
 * @author zfx
 * @ClassName SubImageList
 * @description
 * @since 2026/9/29 16:24
 **/
public class SubImageList {
    @JSONField(name = "SubImageInfoObject")
    private List<SubImageInfoObject> subImageInfoObject;

    public List<SubImageInfoObject> getSubImageInfoObject() {
        return subImageInfoObject;
    }

    public void setSubImageInfoObject(List<SubImageInfoObject> subImageInfoObject) {
        this.subImageInfoObject = subImageInfoObject;
    }
}
