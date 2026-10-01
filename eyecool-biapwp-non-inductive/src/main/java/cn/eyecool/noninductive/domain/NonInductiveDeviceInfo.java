package cn.eyecool.noninductive.domain;

import cn.eyecool.common.annotation.Excel;
import cn.eyecool.common.utils.StringUtils;
import cn.eyecool.device.domain.DeviceInfo;
import org.apache.logging.log4j.util.Strings;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;


/**
 * @author : sunhuayu
 * @version V1.0
 * @Project: eyecool-biapwp
 * @Package cn.eyecool.dfrs.domain
 * @Description: TODO
 * @date Date : 2021年01月25日 上午10:28
 */
public class NonInductiveDeviceInfo extends DeviceInfo {

    private static final long serialVersionUID = 1L;

    @Excel(name = "登录名")
    private String loginUsername;
    @Excel(name = "登录密码")
    private String loginPassword;
    @Excel(name = "设备RTSP地址")
    private String rtspUrl;
    @Excel(name = "设备测温RTSP地址")
    private String rtspUrlExtra;

    @Override
    public void afterDataSet() {
        this.setExtInfo();
    }

    public void setExtInfo() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("loginUsername", this.loginUsername);
        jsonObject.put("loginPassword", this.loginPassword);
        jsonObject.put("rtspUrl", this.rtspUrl);
        jsonObject.put("rtspUrlExtra", this.rtspUrlExtra);
        super.setExtInfo(jsonObject.toJSONString());
    }

    public String getLoginUsername() {
        String extInfo = super.getExtInfo();
        if (StringUtils.isNotEmpty(extInfo)) {
            return JSON.parseObject(extInfo).getString("loginUsername");
        } else {
            return Strings.EMPTY;
        }
    }

    public void setLoginUsername(String loginUsername) {
        this.loginUsername = loginUsername;
    }

    public String getLoginPassword() {
        String extInfo = super.getExtInfo();
        if (StringUtils.isNotEmpty(extInfo)) {
            return JSON.parseObject(extInfo).getString("loginPassword");
        } else {
            return Strings.EMPTY;
        }
    }

    public void setLoginPassword(String loginPassword) {
        this.loginPassword = loginPassword;
    }

    public String getRtspUrl() {
        String extInfo = super.getExtInfo();
        if (StringUtils.isNotEmpty(extInfo)) {
            return JSON.parseObject(extInfo).getString("rtspUrl");
        } else {
            return Strings.EMPTY;
        }
    }

    public void setRtspUrl(String rtspUrl) {
        this.rtspUrl = rtspUrl;
    }

    public String getRtspUrlExtra() {
        String extInfo = super.getExtInfo();
        if (StringUtils.isNotEmpty(extInfo)) {
            return JSON.parseObject(extInfo).getString("rtspUrlExtra");
        } else {
            return Strings.EMPTY;
        }
    }

    public void setRtspUrlExtra(String rtspUrlExtra) {
        this.rtspUrlExtra = rtspUrlExtra;
    }
}
