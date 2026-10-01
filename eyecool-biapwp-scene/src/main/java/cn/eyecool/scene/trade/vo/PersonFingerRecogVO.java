package cn.eyecool.scene.trade.vo;

import cn.eyecool.common.utils.StringUtils;

/**
 * 指纹识别(1:N)VO
 * 
 * @author admin
 * @date 2019年11月14日
 */
public class PersonFingerRecogVO extends PersonBioRecogBaseVO {

    private static final long serialVersionUID = 1L;

    /** 手指编号 */
    private String fingerNo = StringUtils.EMPTY;

    public String getFingerNo() {
        return fingerNo;
    }

    public void setFingerNo(String fingerNo) {
        this.fingerNo = fingerNo;
    }

}
