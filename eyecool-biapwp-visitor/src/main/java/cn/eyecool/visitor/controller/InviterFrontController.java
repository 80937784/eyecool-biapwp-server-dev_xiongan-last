package cn.eyecool.visitor.controller;

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
import cn.eyecool.visitor.vo.InviterInfoVO;
import cn.eyecool.visitor.vo.SceneTreeMes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Base64;
import java.util.List;

/**
 * 访客系统-邀请人-H5页面-相关接口Controller
 * @Author Administrator
 * @create 2021/10/21 14:45
 */
@RestController
@RequestMapping("/api/visit")
public class InviterFrontController {


    @Autowired
    private VisitorService inviterService;

    @Autowired
    private RedisCache redisCache;


    /**
     * 邀请人获取验证码接口
     *
     * @param inviterInfo
     * @return
     * @throws
     */
    @GetMapping("/getVerifyCode")
    public AjaxResult getVerifyCode(InviterInfoVO inviterInfo) {
        inviterService.getVerifyCode(inviterInfo);
        return AjaxResult.success("短信发送成功");
    }

    /**
     * 邀请人登录接口,返回登录token
     * @return
     */
    @PostMapping("/login")
    public AjaxResult login(@RequestBody InviterInfoVO inviterInfo){
        // 参数校验
        AjaxResult ajaxResult = VisitSystemValidate.validateInviterInfo(inviterInfo);
        if (!Integer.valueOf(HttpStatus.SUCCESS).equals(ajaxResult.get(AjaxResult.CODE_TAG))) {
            return ajaxResult;
        }
        String token=inviterService.login(inviterInfo);
        return  AjaxResult.success("登录成功", token);
    }

    /**
     * 获取邀请人所在的子场景
     * @return
     */
    @GetMapping("/getPersonSubScene")
    public AjaxResult getPersonSubScene(HttpServletRequest request){
        // 获取token并验证
        String token = request.getHeader("inviter-token");
        String personInfoId = checkToken(token, PersonTypeEnum.USER);
        List<SceneTreeMes> treeList=inviterService.getPersonSubScene(personInfoId);
        return AjaxResult.success(treeList);
    }

    /**
     * 保存邀请人填写的邀请信息并发送短信
     *
     * @return
     */
    @PostMapping("/saveInvitationMes")
    public AjaxResult saveInvitationMes(HttpServletRequest request, @RequestBody InvitationMesVO invitationMesVO){
        // 获取token并验证
        String token = request.getHeader("inviter-token");
        String personInfoId = checkToken(token,PersonTypeEnum.USER);
        // 验证一下邀请基本信息数据格式是否正确，该必填的是否填写
        AjaxResult ajaxResult=VisitSystemValidate.validateInvitationMes(invitationMesVO);
        if (!Integer.valueOf(HttpStatus.SUCCESS).equals(ajaxResult.get(AjaxResult.CODE_TAG))) {
            return ajaxResult;
        }
        // TODO 这里后面短信中可以放链接了后，service方法改成void
        String url=inviterService.saveInvitationMes(invitationMesVO,personInfoId);
        return AjaxResult.success("邀请成功请注意",url);
    }





    String checkToken(String token, PersonTypeEnum personTypeEnum){
        if (StringUtils.isEmpty(token)) {
            throw new CustomException("获取用户认证信息失败，请先登录！", 400);
        }
        // 解析好后的token的格式： 用户的basePersonInfoId;租户id;获取token时的毫秒级时间
        String userMes = null;
        try{
            userMes = AESUtils.decryptAES(new String(Base64.getDecoder().decode(token)));
        }catch (Exception e){
            throw new CustomException("获取用户认证信息失败，请先登录！", 400);
        }

        String[] mesArr = userMes.split(";");
        if(mesArr.length!=4){
            throw new CustomException("获取用户认证信息失败，请先登录！", 400);
        }
        String personInfoId=mesArr[0];
        String tenantId=mesArr[1];
        String personType=mesArr[2];
        String beginTime=mesArr[3];

        // 判断当前接口该token中的角色是否能访问
        if(!personTypeEnum.value().equals(personType)){
            throw new CustomException("接口未授权！", 401);
        }
        // 判断token请求是否超时
        Long currentTime=System.currentTimeMillis();
        Long beginTimeLong;
        try {
            beginTimeLong=Long.parseLong(beginTime);
        }catch (NumberFormatException e){
            throw new CustomException("获取用户认证信息失败，请先登录！", 400);
        }

        // 判断时间是否超过设定好的token过期时间
        if(currentTime-beginTimeLong> VisitorSystemConstants.TOKEN_EXPIRE_TIME*1000){
            throw new CustomException("用户认证信息超时已失效，请重新登录", 403);
        }
        String cacheToken= redisCache.getCacheObject(VisitorSystemConstants.VISITOR_SYSTEM_TOKEN_CACHE_PREFIX+personInfoId);
        if(null==cacheToken || !cacheToken.equals(token)){
            throw new CustomException("获取用户认证信息失败，请先登录！", 403);
        }
        TenantContextHolder.setTenantId(tenantId);
        return personInfoId;
    }

}
