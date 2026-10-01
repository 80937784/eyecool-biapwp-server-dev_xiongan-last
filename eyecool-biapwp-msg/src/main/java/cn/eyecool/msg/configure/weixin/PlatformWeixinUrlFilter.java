package cn.eyecool.msg.configure.weixin;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weixin4j.Configuration;
import org.weixin4j.util.TokenUtil;

/**
 * 微信公众平台接受消息默认过滤器
 * 
 * @author mawenjun
 * @version 1.0
 * @date 2020年3月25日
 *
 */
public class PlatformWeixinUrlFilter implements Filter {

    private static final Logger LOG = LoggerFactory.getLogger(PlatformWeixinUrlFilter.class);

    @Override
    public void init(FilterConfig config) throws ServletException {
        if (Configuration.isDebug()) {
            LOG.debug("WeixinUrlFilter,Started successfully!");
        }
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
        throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest)req;
        HttpServletResponse response = (HttpServletResponse)res;
        // 微信服务器将发送GET请求到填写的URL上,这里需要判定是否为GET请求
        boolean isGet = request.getMethod().toLowerCase().equals("get");
        if (Configuration.isDebug()) {
            LOG.debug("Get WeChat request: [{}] method", request.getMethod());
            LOG.debug("WeChat request URL: [{}]", request.getServletPath());
            LOG.debug("Get WeChat public account appid:[{}]", request.getParameter("appid"));
        }
        // 消息来源可靠性验证
        String signature = request.getParameter("signature");// 微信加密签名
        String timestamp = request.getParameter("timestamp");// 时间戳
        String nonce = request.getParameter("nonce"); // 随机数
        // Token为weixin4j.properties中配置的Token
        String token = TokenUtil.get();
        if (isGet) {
            // 1.验证消息真实性
            // http://mp.weixin.qq.com/wiki/index.php?title=验证消息真实性
            // URL为http://www.weixin4j.org/api/公众号
            // 成为开发者验证
            String echostr = request.getParameter("echostr"); //
            // 确认此次GET请求来自微信服务器，原样返回echostr参数内容，则接入生效，成为开发者成功，否则接入失败
            if (TokenUtil.checkSignature(token, signature, timestamp, nonce)) {
                response.getWriter().write(echostr);
            }
        } else {
            // 确认此次GET请求来自微信服务器，原样返回echostr参数内容，则接入生效，成为开发者成功，否则接入失败
            if (!TokenUtil.checkSignature(token, signature, timestamp, nonce)) {
                // 消息不可靠，直接返回
                response.getWriter().write("");
                return;
            }
            // 用户每次向公众号发送消息、或者产生自定义菜单点击事件时，响应URL将得到推送
            response.setCharacterEncoding("UTF-8");
            response.setContentType("text/xml");
            chain.doFilter(request, response);
        }
    }

    @Override
    public void destroy() {}
}
