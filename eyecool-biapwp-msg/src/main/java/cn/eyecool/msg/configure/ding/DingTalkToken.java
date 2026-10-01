package cn.eyecool.msg.configure.ding;

import java.io.Serializable;
import java.util.Date;

import com.dingtalk.api.response.OapiGettokenResponse;

/**
 * 钉钉Token实体类
 * 
 * @author admin
 * @date 2020年3月31日
 */
public class DingTalkToken implements Serializable {

    private static final long serialVersionUID = 1L;
    private String access_token;
    private long expires_in = 7200;
    private long exprexpired_time;
    private long create_time;

    public DingTalkToken() {}

    public DingTalkToken(String accessToken, int expiresIn) {
        this.access_token = accessToken;
        setExpires_in(expiresIn);
    }

    public DingTalkToken(String accessToken, int expiresIn, long createTime) {
        this.access_token = accessToken;
        setExpires_in(expiresIn, createTime);
    }

    public DingTalkToken(OapiGettokenResponse response) {
        this.access_token = response.getAccessToken();
        // 根据当前时间的毫秒数+获取的秒数计算过期时间
        this.expires_in = response.getExpiresIn();
        setExpires_in(expires_in);
    }

    /**
     * 获取用户凭证
     *
     * @return 用户凭证
     */
    public String getAccess_token() {
        return access_token;
    }

    /**
     * 设置用户凭证
     *
     * @param access_token 用户凭证
     */
    public void setAccess_token(String access_token) {
        this.access_token = access_token;
    }

    /**
     * 判断用户凭证是否过期
     *
     * @return 过期返回 true,否则返回false
     */
    public boolean isExprexpired() {
        Date now = new Date();
        long nowLong = now.getTime();
        return nowLong >= exprexpired_time;
    }

    /**
     * 获取 凭证有效时间，单位：秒
     *
     * @return 凭证有效时间，单位：秒
     */
    public long getExpires_in() {
        return expires_in;
    }

    /**
     * 设置 凭证有效时间，单位：秒
     *
     * <p>
     * 为了与微信服务器保存同步，误差设置为提前1分钟，即：将创建时间提早1分钟
     * </p>
     *
     * @param expires_in 凭证有效时间，单位：秒
     */
    public void setExpires_in(long expires_in) {
        this.expires_in = expires_in;
        // 使用当前时间记录
        setExpires_in(expires_in, System.currentTimeMillis());
    }

    /**
     * 设置 凭证有效时间，单位：秒
     *
     * <p>
     * 为了与服务器保存同步，误差设置为提前1分钟，即：将创建时间提早1分钟
     * </p>
     *
     * @param expires_in 凭证有效时间，单位：秒
     * @param create_time 凭证创建时间
     */
    public void setExpires_in(long expires_in, long create_time) {
        this.expires_in = expires_in;
        // 获取当前时间毫秒数
        this.create_time = create_time - 60000;
        // 设置下次过期时间 = 当前时间 + (凭证有效时间(秒) * 1000)
        this.exprexpired_time = this.create_time + (expires_in * 1000);
    }

    /**
     * 获取 此次凭证创建时间 单位：毫秒数
     *
     * @return 创建时间 毫秒数
     */
    public long getCreate_time() {
        return this.create_time + 60000;
    }

    /**
     * 将数据转换为JSON数据包
     *
     * @return JSON数据包
     */
    @Override
    public String toString() {
        // 对外的时间 需要加上扣掉的 60秒
        return "{\"access_token\":\"" + this.getAccess_token() + "\",\"expires_in\":" + this.getExpires_in()
            + ",\"create_time\" : " + this.getCreate_time() + "}";
    }
}
