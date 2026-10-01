package cn.eyecool.msg.controller;

import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.weixin4j.WeixinException;


import cn.eyecool.common.annotation.Log;
import cn.eyecool.common.core.controller.BaseController;
import cn.eyecool.common.core.domain.AjaxResult;
import cn.eyecool.common.core.page.TableDataInfo;
import cn.eyecool.common.enums.BusinessType;
import cn.eyecool.common.utils.poi.ExcelUtil;
import cn.eyecool.msg.domain.MsgOfficalAccountUser;
import cn.eyecool.msg.service.IMsgOfficalAccountUserService;

/**
 * 微信用户Controller
 * 
 * @author admin
 * @date 2021-04-15
 */
@RestController
@RequestMapping("/msg/officalAccountUser")
public class MsgOfficalAccountUserController extends BaseController {
    @Autowired
    private IMsgOfficalAccountUserService msgOfficalAccountUserService;

    /**
     * 查询微信用户列表
     */
    @PreAuthorize("@ss.hasPermi('msg:officalAccountUser:list')")
    @GetMapping("/list")
    public TableDataInfo list(MsgOfficalAccountUser msgOfficalAccountUser) {
        startPage();
        List<MsgOfficalAccountUser> list =
            msgOfficalAccountUserService.selectMsgOfficalAccountUserList(msgOfficalAccountUser);
        return getDataTable(list);
    }

    /**
     * 导出微信用户列表
     */
    @PreAuthorize("@ss.hasPermi('msg:officalAccountUser:export')")
    @Log(title = "msg.offical.account.user.name", businessType = BusinessType.EXPORT)
    @GetMapping("/export")
    public AjaxResult export(MsgOfficalAccountUser msgOfficalAccountUser) {
        List<MsgOfficalAccountUser> list =
            msgOfficalAccountUserService.selectMsgOfficalAccountUserList(msgOfficalAccountUser);
        ExcelUtil<MsgOfficalAccountUser> util = new ExcelUtil<MsgOfficalAccountUser>(MsgOfficalAccountUser.class);
        return util.exportExcel(list, "officalAccountUser");
    }

    /**
     * 获取微信用户详细信息
     */
    @PreAuthorize("@ss.hasPermi('msg:officalAccountUser:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") String id) {
        return AjaxResult.success(msgOfficalAccountUserService.selectMsgOfficalAccountUserById(id));
    }

    /**
     * 修改微信用户
     */
    @PreAuthorize("@ss.hasPermi('msg:officalAccountUser:edit')")
    @Log(title = "msg.offical.account.user.name", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody MsgOfficalAccountUser msgOfficalAccountUser) {
        MsgOfficalAccountUser info =
            msgOfficalAccountUserService.selectMsgOfficalAccountUserById(msgOfficalAccountUser.getId());
        MsgOfficalAccountUser condition = new MsgOfficalAccountUser();
        condition.setAppId(msgOfficalAccountUser.getAppId());
        condition.setPhone(msgOfficalAccountUser.getPhone());
        List<MsgOfficalAccountUser> list = msgOfficalAccountUserService.selectMsgOfficalAccountUserList(condition);
        if (CollectionUtils.isNotEmpty(list)) {
            MsgOfficalAccountUser userInfo = list.get(0);
            if (!userInfo.getOpenId().equals(info.getOpenId())) {
                return AjaxResult.error(cn.eyecool.common.utils.MessageUtils.message("msg.offical.account.user.phone.bind.another"));
            }
        }
        return toAjax(msgOfficalAccountUserService.updateMsgOfficalAccountUser(msgOfficalAccountUser));
    }

    /**
     * 拉取公众号用户
     */
    @PreAuthorize("@ss.hasPermi('msg:officalAccountUser:pull')")
    @Log(title = "msg.offical.account.user.weixinuser", businessType = BusinessType.OTHER)
    @PostMapping("/pull")
    public AjaxResult pullWeixinUser(@RequestBody MsgOfficalAccountUser msgOfficalAccountUser) {
        try {
            msgOfficalAccountUserService.pullWeixinUser(msgOfficalAccountUser);
            return AjaxResult.success();
        } catch (WeixinException e) {
            e.printStackTrace();
            return AjaxResult.error(e.getMessage());
        }
    }
}
