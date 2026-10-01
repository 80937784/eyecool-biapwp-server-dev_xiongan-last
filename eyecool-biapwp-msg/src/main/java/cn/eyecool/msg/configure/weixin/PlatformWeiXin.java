package cn.eyecool.msg.configure.weixin;

import java.beans.Transient;
import java.util.HashMap;
import java.util.Map;

import org.weixin4j.Weixin;
import org.weixin4j.WeixinConfig;
import org.weixin4j.WeixinException;
import org.weixin4j.component.AbstractComponent;
import org.weixin4j.model.base.Token;

import cn.eyecool.common.exception.CustomException;

/**
 * 微信平台基础支持对象
 * 
 * @author admin
 * @date 2020年3月24日
 */
public class PlatformWeiXin extends Weixin {
    private final Map<String, AbstractComponent> components = new HashMap<>();

    private static final long serialVersionUID = 1L;

    /** 同步锁 */
    private final static byte[] LOCK = new byte[0];
    /** 微信Token加载器 */
    private final WeixinTokenLoader weixinTokenLoader = new WeixinTokenLoader();

    public PlatformWeiXin(WeixinConfig weixinConfig) {
        super(weixinConfig);
    }

    /**
     * 获取Token对象
     */
    @Override
    @Transient
    public Token getToken() throws WeixinException {
        String appId = null == getWeixinConfig() ? null : getWeixinConfig().getAppid();
        Token token = weixinTokenLoader.get(appId);
        if (token == null) {
            synchronized (LOCK) {
                token = weixinTokenLoader.get(appId);
                if (token == null) {
                    try {
                        token = base().token();
                        weixinTokenLoader.refresh(token, appId);
                    } catch (WeixinException e) {
                        throw new CustomException(e.getMessage(), e);
                    }
                }
            }
        }
        return token;
    }

    /**
     * 获取TokenLoader对象
     * 
     * @author mawenjun
     * @return
     * @date 2020年3月26日
     *
     */
    public WeixinTokenLoader getTokenLoder() {
        return this.weixinTokenLoader;
    }

    /**
     * 分析组件
     * 
     * @return
     */
    public AnalysisComponent analysis() {
        String key = AnalysisComponent.class.getName();
        if (components.containsKey(key)) {
            return (AnalysisComponent)components.get(key);
        }
        AnalysisComponent component = new AnalysisComponent(this);
        components.put(key, component);
        return component;
    }
}
