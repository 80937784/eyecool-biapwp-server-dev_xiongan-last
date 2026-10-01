package cn.eyecool.area.controller;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.alibaba.fastjson.JSON;

import cn.eyecool.common.constant.Constants;
import cn.eyecool.common.constant.SsoConstants;
import cn.eyecool.common.core.domain.AjaxResult;
import cn.eyecool.common.core.domain.model.LoginBody;
import cn.eyecool.common.core.redis.RedisCache;
import lombok.extern.slf4j.Slf4j;

/**
 * @Author Administrator
 * @create 2021/9/9 9:30
 */
@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@AutoConfigureMockMvc
public class AreaModelControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private RedisCache redisCache;
    // 令牌自定义标识
    @Value("${token.header}")
    private String header;

    // token信息
    private static String token;
    // 验证码uuid
    private static String uuid;
    // 验证码计算结果
    private static String code;
    // 模拟http请求时,header中携带的认证信息
    private static String authorization;

    /**
     * @Author zgy
     * @Date 2021/9/9 9:30
     * @Description 测试请求区域列表
     * @Param []
     * @Return void
     */
    @Test
    void List() throws Exception {
        // 构造接口
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilders.get("/area/model/list")
            .header(header, authorization).header(SsoConstants.SSO_CLIENT_CREDENTIAL_HEADER,
                SsoConstants.CLIENT_CREDENTIAL_PREFIX + " " + "d2ViOjEyMzQ1Ng==");
        // 开始调用接口
        ResultActions perform = this.mockMvc.perform(mockHttpServletRequestBuilder);

        // 处理返回结果
        MvcResult mvcResult = perform
            // .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)).andReturn();

        String content = mvcResult.getResponse().getContentAsString();
        AjaxResult ajaxResult = JSON.parseObject(content, AjaxResult.class);
        cn.hutool.core.lang.Assert.notNull(ajaxResult);
        cn.hutool.core.lang.Assert.notNull(ajaxResult.get("data"));
        int rows = ((List<?>)ajaxResult.get("data")).size();
        log.info("获取到的数据条数：{}" + rows);

    }

    @BeforeEach
    @DisplayName("在测试前获取token")
    void get() throws Exception {
        if (token != null) {
            return;
        }
        getCaptchaMes();
        getToken();
        getAuthorization();
    }

    /**
     * @Author zgy
     * @Date 2021/9/9 14:51
     * @Description 在测试前获取验证码uuid以及计算结果
     * @Param []
     * @Return void
     */
    void getCaptchaMes() throws Exception {
        if (null != uuid && null != code) {
            return;
        }

        MvcResult mvcResult =
            mockMvc.perform(MockMvcRequestBuilders.get("/captchaImage").accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON).header(SsoConstants.SSO_CLIENT_CREDENTIAL_HEADER,
                    SsoConstants.CLIENT_CREDENTIAL_PREFIX + " " + "d2ViOjEyMzQ1Ng==")
            // 这里要特别注意和content传参数的不同，具体看你接口接受的是哪种
            // 传json参数,最后传的形式是 Body = {"password":"admin","userName":"admin"}
            // .content(JSON.toJSON(info).toString().getBytes())
            ).andExpect(MockMvcResultMatchers.status().isOk()).andReturn();

        // 得到返回结果
        String content = mvcResult.getResponse().getContentAsString();
        AjaxResult ajaxResult = JSON.parseObject(content, AjaxResult.class);
        cn.hutool.core.lang.Assert.notNull(ajaxResult);
        cn.hutool.core.lang.Assert.notNull(ajaxResult.get("uuid"));
        // 得到验证码计算结果以及uuid
        uuid = ajaxResult.get("uuid").toString();
        // 根据uuid从redis中获取验证码计算结果
        String verifyKey = Constants.CAPTCHA_CODE_KEY + uuid;
        code = redisCache.getCacheObject(verifyKey);

        log.info("获取到了uuid：{}", uuid);
        log.info("获取到了code：{}", code);
    }

    /**
     * @Author zgy
     * @Date 2021/9/9 14:52
     * @Description 在测试前获取token
     * @Param []
     * @Return void
     */
    void getToken() throws Exception {
        LoginBody loginBody = new LoginBody();
        loginBody.setUsername("admin");
        loginBody.setPassword("admin123");
        loginBody.setCode(code);
        loginBody.setUuid(uuid);

        MvcResult mvcResult = mockMvc
            .perform(MockMvcRequestBuilders.post("/login").contentType(MediaType.APPLICATION_JSON)
                .content(JSON.toJSONBytes(loginBody)).header(SsoConstants.SSO_CLIENT_CREDENTIAL_HEADER,
                    SsoConstants.CLIENT_CREDENTIAL_PREFIX + " " + "d2ViOjEyMzQ1Ng=="))
            // .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().isOk()).andReturn();
        // 得到返回结果
        String content = mvcResult.getResponse().getContentAsString();
        AjaxResult ajaxResult = JSON.parseObject(content, AjaxResult.class);
        cn.hutool.core.lang.Assert.notNull(ajaxResult);
        cn.hutool.core.lang.Assert.notNull(ajaxResult.get("token"));
        token = ajaxResult.get("token").toString();
        log.info("获取到用户token了：{}", token);
    }

    /**
     * @Author zgy
     * @Date 2021/9/9 14:52
     * @Description 在测试前组装前端发送到后台的 Authorization
     * @Param []
     * @Return void
     */
    void getAuthorization() {
        cn.hutool.core.lang.Assert.notNull(token);
        authorization = Constants.TOKEN_PREFIX + " " + token;
    }

}