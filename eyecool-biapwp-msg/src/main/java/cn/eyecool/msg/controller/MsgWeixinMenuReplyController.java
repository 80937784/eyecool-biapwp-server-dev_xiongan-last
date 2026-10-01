package cn.eyecool.msg.controller;

import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import cn.eyecool.common.annotation.Log;
import cn.eyecool.common.core.controller.BaseController;
import cn.eyecool.common.core.domain.AjaxResult;
import cn.eyecool.common.enums.BusinessType;
import cn.eyecool.msg.domain.MsgWeixinMenuReply;
import cn.eyecool.msg.service.IMsgWeixinMenuReplyService;
import cn.eyecool.common.utils.poi.ExcelUtil;
import cn.eyecool.common.core.page.TableDataInfo;

/**
 * 微信公众号菜单回复Controller
 * 
 * @author admin
 * @date 2021-04-15
 */
@RestController
@RequestMapping("/msg/weixinMenuReply")
public class MsgWeixinMenuReplyController extends BaseController
{
    @Autowired
    private IMsgWeixinMenuReplyService msgWeixinMenuReplyService;

    /**
     * 查询微信公众号菜单回复列表
     */
    @PreAuthorize("@ss.hasPermi('msg:weixinMenuReply:list')")
    @GetMapping("/list")
    public TableDataInfo list(MsgWeixinMenuReply msgWeixinMenuReply)
    {
        startPage();
        List<MsgWeixinMenuReply> list = msgWeixinMenuReplyService.selectMsgWeixinMenuReplyList(msgWeixinMenuReply);
        return getDataTable(list);
    }

    /**
     * 导出微信公众号菜单回复列表
     */
    @PreAuthorize("@ss.hasPermi('msg:weixinMenuReply:export')")
    @Log(title = "msg.weixin.menu.reply.title", businessType = BusinessType.EXPORT)
    @GetMapping("/export")
    public AjaxResult export(MsgWeixinMenuReply msgWeixinMenuReply)
    {
        List<MsgWeixinMenuReply> list = msgWeixinMenuReplyService.selectMsgWeixinMenuReplyList(msgWeixinMenuReply);
        ExcelUtil<MsgWeixinMenuReply> util = new ExcelUtil<MsgWeixinMenuReply>(MsgWeixinMenuReply.class);
        return util.exportExcel(list, "weixinMenuReply");
    }

    /**
     * 获取微信公众号菜单回复详细信息
     */
    @PreAuthorize("@ss.hasPermi('msg:weixinMenuReply:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") String id)
    {
        return AjaxResult.success(msgWeixinMenuReplyService.selectMsgWeixinMenuReplyById(id));
    }

    /**
     * 新增微信公众号菜单回复
     */
    @PreAuthorize("@ss.hasPermi('msg:weixinMenuReply:add')")
    @Log(title = "msg.weixin.menu.reply.title", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody MsgWeixinMenuReply msgWeixinMenuReply)
    {
        return toAjax(msgWeixinMenuReplyService.insertMsgWeixinMenuReply(msgWeixinMenuReply));
    }

    /**
     * 修改微信公众号菜单回复
     */
    @PreAuthorize("@ss.hasPermi('msg:weixinMenuReply:edit')")
    @Log(title = "msg.weixin.menu.reply.title", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody MsgWeixinMenuReply msgWeixinMenuReply)
    {
        return toAjax(msgWeixinMenuReplyService.updateMsgWeixinMenuReply(msgWeixinMenuReply));
    }

    /**
     * 删除微信公众号菜单回复
     */
    @PreAuthorize("@ss.hasPermi('msg:weixinMenuReply:remove')")
    @Log(title = "msg.weixin.menu.reply.title", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable String[] ids)
    {
        return toAjax(msgWeixinMenuReplyService.deleteMsgWeixinMenuReplyByIds(ids));
    }
}
