package cn.eyecool.video.service.impl;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import cn.eyecool.video.service.HuanXinEasemobHttpService;
import cn.eyecool.video.util.HttpUtil;

/**
 * 实现环信远程视频的用户注册、创建组、绑定成员等功能
 */
@Service
public class HuanXinEasemobHttpServiceImpl implements HuanXinEasemobHttpService {
    private static Logger logger = LoggerFactory.getLogger(HuanXinEasemobHttpServiceImpl.class);
    /*    # 环信远程视频账号信息
    hx:
            if:
    use: 1 #1 使用环信视频通话
    hxorg: 1111180802177882
    hxApp: ecx332-test
    hxCsecret: YXA60wD3wisH9zJctivj1kx27zbJqbM
    hxCid: YXA6R3bLSX5-Te61Ag0xjxiKTw*/
    @Value("${hx.hxorg:1111180802177882}")
    private String orgName;
    @Value("${hx.hxApp:ecx332-test}")
    private String appName;
    @Value("${hx.hxCid:YXA6R3bLSX5-Te61Ag0xjxiKTw}")
    private String hxCid;
    @Value("${hx.hxCsecret:YXA60wD3wisH9zJctivj1kx27zbJqbM}")
    private String hxCsecret;
    @Value("${hx.if.use:1}")
    private String hxUse;
    private String token;
    private long expires = 0L;

    public HuanXinEasemobHttpServiceImpl() {}

    // 方便调试
    public HuanXinEasemobHttpServiceImpl(String orgName, String appName, String hxCid, String hxCsecret, String hxUse) {
        this.orgName = orgName;
        this.appName = appName;
        this.hxCid = hxCid;
        this.hxCsecret = hxCsecret;
        this.hxUse = hxUse;
    }

    // 方便调试
    public HuanXinEasemobHttpServiceImpl(String orgName, String appName, String token) {
        this.orgName = orgName;
        this.appName = appName;
        this.token = token;
        this.expires = System.currentTimeMillis() + 10000;
    }

    /**
     * @param username
     * @param password
     * @return
     * @throws IOException
     */
    public String registerIM(String username, String password) throws IOException {
        if (!"1".equals(hxUse)) {
            return null;
        }
        if (null == token || expires == 0) {
            getToken();
        }

        if (expires < new Date().getTime()) {
            getToken();
        }

        String url = String.format("https://a1.easemob.com/%s/%s/users", orgName, appName);

        Map<String, String> param = Maps.newHashMap();
        param.put("username", username);
        param.put("password", password);

        String contentType = "application/json";

        Map<String, String> header = Maps.newHashMap();
        header.put("Authorization", "Bearer " + token);

        return HttpUtil.doPost(url, JSON.toJSONString(param), contentType, header);

    }

    public String getIM(String username) throws IOException {
        if (!"1".equals(hxUse)) {
            return null;
        }
        if (null == token || expires == 0)
            getToken();

        if (expires < new Date().getTime()) {
            getToken();
        }

        String url = String.format("https://a1.easemob.com/%s/%s/users/%s", orgName, appName, username);

        Map<String, String> header = Maps.newHashMap();
        header.put("Authorization", "Bearer " + token);

        return HttpUtil.doGet(url, header);
    }

    @Override
    public String deleteIM(String username) throws IOException {
        if (!"1".equals(hxUse)) {
            return null;
        }
        if (null == token || expires == 0)
            getToken();

        if (expires < new Date().getTime()) {
            getToken();
        }

        String url = String.format("https://a1.easemob.com/%s/%s/users/%s", orgName, appName, username);

        Map<String, String> header = new HashMap<String, String>();
        header.put("Authorization", "Bearer " + token);

        return HttpUtil.doDelete(url, header);
    }

    /**
     * 注册消息用户(会一同创建组)
     *
     * @param mac
     * @return 环信id
     */
    @Override
    public String registerIM(String mac) {
        if (!"1".equals(hxUse)) {
            return null;
        }
        try {
            String password = mac.replace(":", "").replace("-", "");
            String username = "LO" + UUID.randomUUID().toString().replace("-", "").substring(5, 30);
            String regmes = null;
            try {
                regmes = getIM(username);
            } catch (Exception e) {
                regmes = "\"error\":";
            }
            if (regmes.indexOf("\"error\":") >= 0) {// 出错，说明未注册
                String result = registerIM(username, password);
                if (result.indexOf("\"error\":") < 0) {
                    return username;
                }
            } else {
                return username;
            }
        } catch (Exception e) {
            logger.error("registerIM cache failed to register user error [{}]", e);
        }
        return "";
    }

    /**
     * 创建组(同步执行)
     *
     * @param mac
     * @param hxid 注册获取的环信id return 环信群id
     */
    @Override
    public String createGroupGid(String mac, String hxid) {
        if (!"1".equals(hxUse)) {
            return null;
        }
        if (StringUtils.isBlank(hxid)) {
            return "";
        }
        try {
            // 创建组
            String result = createGroup(mac, hxid);
            System.out.println(result);

            Map<String, Object> map = JSON.parseObject(result, HashMap.class);

            if (map.get("error") == null) {
                return ((Map<String, Object>)map.get("data")).get("groupid") + "";
            }
        } catch (Exception e) {
            logger.error("haunxin createGroupGid error[{}]", e);
        }
        return "";
    }

    private void getToken() throws IOException {
        if (!"1".equals(hxUse)) {
            return;
        }
        String url = String.format("https://a1.easemob.com/%s/%s/token", orgName, appName);

        String contentType = "application/json";

        Map<String, String> param = Maps.newHashMap();
        param.put("grant_type", "client_credentials");
        param.put("client_id", hxCid);
        param.put("client_secret", hxCsecret);

        String result = HttpUtil.doPost(url, JSON.toJSONString(param), contentType);
        Map<String, Object> resultMap = JSON.parseObject(result, HashMap.class);
        if (resultMap.get("access_token") != null) {
            token = resultMap.get("access_token").toString();
            expires =
                System.currentTimeMillis() + Long.parseLong(resultMap.get("expires_in").toString()) - 1000L * 60 * 20;
        }
    }

    /**
     * 创建一个群
     *
     * @param owner
     * @param members
     * @return
     */
    public String createGroup(String groupName, String owner, String... members) throws IOException {
        if (!"1".equals(hxUse)) {
            return null;
        }
        String url = String.format("https://a1.easemob.com/%s/%s/chatgroups", orgName, appName);
        String contentType = "application/json";

        Map<String, String> header = Maps.newHashMap();
        if (null == token || expires == 0)
            getToken();

        if (expires < new Date().getTime()) {
            getToken();
        }
        header.put("Authorization", "Bearer " + token);

        Map<String, Object> param = Maps.newHashMap();
        param.put("groupname", groupName);// 群组名称，此属性为必须的
        param.put("desc", groupName);// 群组描述，此属性为必须的
        param.put("public", true);// 是否是公开群，此属性为必须的
        param.put("allowinvites", false);// 是否允许群成员邀请别人加入此群。 true：允许群成员邀请人加入此群，false：只有群主或者管理员才可以往群里加人
        param.put("owner", owner);// 群组的管理员，此属性为必须的
        if (members != null && members.length > 0) {
            List<String> list = Lists.newArrayList();
            for (String member : members) {
                list.add(member);
            }
            param.put("members", list);// 群组成员，此属性为可选的，但是如果加了此项，数组元素至少一个（注：群主jma1不需要写入到members里面
        }

        String result = HttpUtil.doPost(url, JSON.toJSONString(param), contentType, header);
        return result;
    }

    /**
     * 删除一个群
     *
     * @param groupId
     * @throws IOException
     */
    @Override
    public String deleteGroup(String groupId) throws IOException {
        if (!"1".equals(hxUse)) {
            return null;
        }
        String url = String.format("https://a1.easemob.com/%s/%s/chatgroups/%s", orgName, appName, groupId);

        Map<String, String> header = new HashMap<String, String>();
        if (null == token || expires == 0)
            getToken();

        if (expires < new Date().getTime()) {
            getToken();
        }
        header.put("Authorization", "Bearer " + token);

        return HttpUtil.doDelete(url, header);
    }

    /**
     * 添加组员
     *
     * @param groupId 组id
     * @param member 组员 通过唯一标识注册环信账号的编号，比如 手机号，其他代号等
     * @return
     */
    @Override
    public String addGroupMember(String groupId, String member) throws IOException {
        if (!"1".equals(hxUse)) {
            return null;
        }
        String url =
            String.format("https://a1.easemob.com/%s/%s/chatgroups/%s/users/%s", orgName, appName, groupId, member);

        String contentType = "application/json";

        Map<String, String> header = Maps.newHashMap();
        header.put("Authorization", "Bearer " + token);

        String result = HttpUtil.doPost(url, "", contentType, header);
        return result;
    }

    /**
     * 移除组员
     *
     * @param groupId
     * @param member
     * @return
     * @throws IOException
     */
    @Override
    public String removeGroupMember(String groupId, String member) throws IOException {
        if (!"1".equals(hxUse)) {
            return null;
        }
        String url =
            String.format("https://a1.easemob.com/%s/%s/chatgroups/%s/users/%s", orgName, appName, groupId, member);

        Map<String, String> header = new HashMap<String, String>();
        if (null == token || expires == 0)
            getToken();

        if (expires < new Date().getTime()) {
            getToken();
        }
        header.put("Authorization", "Bearer " + token);

        return HttpUtil.doDelete(url, header);
    }

    public boolean checkUser(String phone) {
        try {
            getIM(phone);
            return true;
        } catch (Throwable e) {
            return false;
        }
    }

    @Override
    public void checkUserOrCreate(String phone) throws IOException {
        if (!checkUser(phone)) {
            // 不存在 则注册
            registerIM(phone, phone);
        }
    }

    @Override
    public void test() {
        String deviceSn = "test12345679555";
        String hxid = registerIM(deviceSn);
        String gid = createGroupGid(deviceSn, hxid);
        // 通过手机号 建账号 或唯一编号
        String user = "185269845697";
        // 创建用户
        try {
            checkUserOrCreate(user);
        } catch (Exception e) {
            e.printStackTrace();
        }
        // 添加成员
        try {

            addGroupMember(gid, user);
        } catch (Exception e) {
            e.printStackTrace();
        }
        // 删除成员
        try {
            removeGroupMember(gid, user);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            deleteIM(user);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            // 删除群 144997973295106
            deleteGroup(gid);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            // 删除环信账号 LO334e2f44d2ba60dc2feaad405
            deleteIM(hxid);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception {
        HuanXinEasemobHttpServiceImpl easemobHttpHuanXinService = new HuanXinEasemobHttpServiceImpl("1111180802177882",
            "ecx332-test", "YXA6R3bLSX5-Te61Ag0xjxiKTw", "YXA60wD3wisH9zJctivj1kx27zbJqbM", "1");
        easemobHttpHuanXinService.test();
    }

}
