package cn.eyecool.visitor.service;

import cn.eyecool.basedata.domain.BasePersonInfo;
import cn.eyecool.visitor.vo.InvitationMesVO;
import cn.eyecool.visitor.vo.InviterInfoVO;
import cn.eyecool.visitor.vo.SceneTreeMes;


import java.util.List;

/**
 * @Author Administrator
 * @create 2021/10/21 18:11
 */
public interface VisitorService {

    /**
      *H5相关接口
     */
    /**获取短信验证码 **/
    void getVerifyCode(InviterInfoVO inviterInfo);

    /**返回邀请人的token **/
    String login(InviterInfoVO inviterInfo);

    /** 获取邀请人信息**/
    BasePersonInfo getInviterName(String visitorInfoId);

    /**返回邀请人所在测子场景信息 **/
    List<SceneTreeMes> getPersonSubScene(String personInfoId);

    /**保存邀请人填写的邀请信息，保存访客信息，绑定对应的子场景，发送短信给访客 **/
    String saveInvitationMes(InvitationMesVO invitationMesVO,String inviterId);

    /**H5获取访客的邀请信息 **/
    InvitationMesVO getInvitationMes(BasePersonInfo basePersonInfo);

    /**更新访客填写的邀请信息，保存访客人脸，更新访客下的子场景信息 **/
    void updateInvitationMes(String personInfoId, InvitationMesVO invitationMesVO);

    /**检测访客上传的人脸信息是否正确 **/
    boolean checkVisitorFace(InvitationMesVO invitationMesVO);

    /**
     * 后台访客相关接口
     */





}
