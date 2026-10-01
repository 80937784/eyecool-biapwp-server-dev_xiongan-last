package cn.eyecool.visitor.controller;

import java.util.Base64;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cn.eyecool.basedata.domain.BasePersonInfo;
import cn.eyecool.common.constant.DictConstants;
import cn.eyecool.common.constant.HttpStatus;
import cn.eyecool.common.context.TenantContextHolder;
import cn.eyecool.common.core.domain.AjaxResult;
import cn.eyecool.common.core.redis.RedisCache;
import cn.eyecool.common.enums.PersonTypeEnum;
import cn.eyecool.common.exception.CustomException;
import cn.eyecool.common.utils.AESUtils;
import cn.eyecool.common.utils.StringUtils;
import cn.eyecool.visitor.constant.VisitorSystemConstants;
import cn.eyecool.visitor.service.VisitorService;
import cn.eyecool.visitor.utils.VisitSystemValidate;
import cn.eyecool.visitor.vo.InvitationMesVO;

/**
 * 访客系统-访客-H5页面-相关接口Controller
 * 
 * @Author Administrator
 * @create 2021/10/21 14:45
 */
@RestController
@RequestMapping("/api/visit")
public class VisitorFrontController {

    @Autowired
    private VisitorService inviterService;

    @Autowired
    private RedisCache redisCache;

    /**
     * 检测token是否正确
     *
     * @return
     */
    @GetMapping("/checkToken")
    public AjaxResult saveInvitationMes(HttpServletRequest request) {
        // 获取token并验证
        String token = request.getHeader("visitor-token");
        checkToken(token, PersonTypeEnum.VISITOR);
        return AjaxResult.success("正确");
    }

    /**
     * 获取邀请人姓名
     *
     * @return
     */
    @GetMapping("/getInviterName")
    public AjaxResult getInviterName(HttpServletRequest request) {
        // 获取token并验证
        String token = request.getHeader("visitor-token");
        String visitorInfoId = checkToken(token, PersonTypeEnum.VISITOR);
        BasePersonInfo basePersonInfo = inviterService.getInviterName(visitorInfoId);
        if (null == basePersonInfo) {
            return AjaxResult.error("未查询到相关人员");
        }
        return AjaxResult.success("成功", basePersonInfo.getName());
    }

    /**
     * 检查人脸图片是否正确
     *
     * @return
     */
    @PostMapping("/checkVisitorFace")
    public AjaxResult checkVisitorFace(HttpServletRequest request, @RequestBody InvitationMesVO invitationMesVO) {
        // 获取token并验证
        String token = request.getHeader("visitor-token");
        checkToken(token, PersonTypeEnum.VISITOR);
        inviterService.checkVisitorFace(invitationMesVO);
        return AjaxResult.success("人脸检测通过");
    }

    /**
     * @Author zgy
     * @Date 2021/10/25 11:59
     * @Description 访客通过连接进入页面,获取简单的邀请信息
     * @Param [request] token包含四个字段的加密信息
     * @Return cn.eyecool.common.core.domain.AjaxResult
     */
    @GetMapping("/getInvitationMes")
    public AjaxResult getInvitationMes(HttpServletRequest request) {
        // 获取token并验证
        String token = request.getHeader("visitor-token");
        String personInfoId = checkToken(token, PersonTypeEnum.VISITOR);
        BasePersonInfo condition = new BasePersonInfo();
        condition.setId(personInfoId);
        condition.setPersonType(PersonTypeEnum.VISITOR.value());
        condition.setStatus(DictConstants.Status.ENABLE);
        InvitationMesVO invitationMesVO = inviterService.getInvitationMes(condition);
        return AjaxResult.success(invitationMesVO);
    }

    /**
     * @Author zgy
     * @Date 2021/10/25 15:59
     * @Description 访客更新访客信息
     * @Param [request, invitationMesVO] token跟访客基本信息
     * @Return cn.eyecool.common.core.domain.AjaxResult
     */
    @PostMapping("/updateInvitationMes")
    public AjaxResult updateInvitationMes(HttpServletRequest request, @RequestBody InvitationMesVO invitationMesVO) {
        // 获取token并验证
        String token = request.getHeader("visitor-token");
        String personInfoId = checkToken(token, PersonTypeEnum.VISITOR);
        if (null == personInfoId) {
            return AjaxResult.error("邀请人信息错误");
        }
        AjaxResult ajaxResult = VisitSystemValidate.validateVisitUpdateMes(invitationMesVO);
        if (!Integer.valueOf(HttpStatus.SUCCESS).equals(ajaxResult.get(AjaxResult.CODE_TAG))) {
            return ajaxResult;
        }
        inviterService.updateInvitationMes(personInfoId, invitationMesVO);
        return AjaxResult.success("提交成功");
    }

    String checkToken(String token, PersonTypeEnum personTypeEnum) {
        if (StringUtils.isEmpty(token)) {
            throw new CustomException("获取用户认证信息失败，请先登录！", 400);
        }
        // 解析好后的token的格式： 用户的basePersonInfoId;租户id;获取token时的毫秒级时间
        String userMes = null;
        try {
            userMes = AESUtils.decryptAES(new String(Base64.getDecoder().decode(token)));
        } catch (Exception e) {
            throw new CustomException("获取用户认证信息失败，请先登录！", 400);
        }

        String[] mesArr = userMes.split(";");
        if (mesArr.length != 4) {
            throw new CustomException("获取用户认证信息失败，请先登录！", 400);
        }
        String personInfoId = mesArr[0];
        String tenantId = mesArr[1];
        String personType = mesArr[2];
        String beginTime = mesArr[3];

        // 判断当前接口该token中的角色是否能访问
        if (!personTypeEnum.value().equals(personType)) {
            throw new CustomException("接口未授权！", 401);
        }
        // 判断token请求是否超时
        Long currentTime = System.currentTimeMillis();
        Long beginTimeLong;
        try {
            beginTimeLong = Long.parseLong(beginTime);
        } catch (NumberFormatException e) {
            throw new CustomException("获取用户认证信息失败，请先登录！", 400);
        }

        // 判断时间是否超过设定好的token过期时间
        if ((currentTime - beginTimeLong) > VisitorSystemConstants.TOKEN_EXPIRE_TIME * 1000) {
            throw new CustomException("用户认证信息超时已失效，请重新登录", 403);
        }
        String cacheToken =
            redisCache.getCacheObject(VisitorSystemConstants.VISITOR_SYSTEM_TOKEN_CACHE_PREFIX + personInfoId);
        if (null == cacheToken || !cacheToken.equals(token)) {
            throw new CustomException("获取用户认证信息失败，请先登录！", 403);
        }
        TenantContextHolder.setTenantId(tenantId);
        return personInfoId;
    }

}
