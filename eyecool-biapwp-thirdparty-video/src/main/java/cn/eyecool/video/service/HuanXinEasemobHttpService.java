package cn.eyecool.video.service;

import java.io.IOException;

/**
 * 实现环信远程视频的用户注册、创建组、绑定成员等功能
 */
public interface HuanXinEasemobHttpService {
    /**
     * 设备注册环信用户
     * @param deviceSn
     * @return hxid 环信账号id
     */
    String registerIM(String deviceSn);

    /**
     *
     * @param deviceSn 设备编号
     * @param hxid 环信账号id
     * @return gid  环信群组id
     */
    String createGroupGid(String deviceSn, String hxid);

    /**
     *   检测 该 member 是否注册的环信账号，没有注册的 进行注册
     * @param member 用户id 可以是手机号 ，也可以是其他 唯一标识
     * @throws IOException
     */
    void checkUserOrCreate(String member) throws IOException;

    /**
     *
     * @param gid  环信群组id
     * @param member
     * @return
     * @throws IOException
     */
    String addGroupMember(String gid, String member) throws IOException;

    /**
     *
     * @param gid 环信组id
     * @param member 环信用户
     * @return
     * @throws IOException
     */
    String removeGroupMember(String gid, String member) throws IOException ;

    /**
     * 删除环信用户
     * @param username 是 registerIM 注册返回的  hxid 环信账号id 或者是 checkUserOrCreate的 member
     * @return
     * @throws IOException
     */
    String deleteIM(String username) throws IOException;

    /**
     *
     * @param groupId 环信组id
     * @return
     * @throws IOException
     */
    String deleteGroup(String groupId) throws IOException ;

    /**
     * 调用接口案例
     */
    void test();

}
