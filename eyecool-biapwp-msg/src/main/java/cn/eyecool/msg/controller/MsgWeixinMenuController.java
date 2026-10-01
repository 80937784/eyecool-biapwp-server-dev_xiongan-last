package cn.eyecool.msg.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cn.eyecool.common.annotation.Log;
import cn.eyecool.common.core.controller.BaseController;
import cn.eyecool.common.core.domain.AjaxResult;
import cn.eyecool.common.core.page.TableDataInfo;
import cn.eyecool.common.enums.BusinessType;
import cn.eyecool.common.utils.poi.ExcelUtil;
import cn.eyecool.msg.domain.MsgWeixinMenu;
import cn.eyecool.msg.service.IMsgWeixinMenuService;

/**
 * 微信公众号菜单Controller
 * 
 * @author admin
 * @date 2021-04-15
 */
@RestController
@RequestMapping("/msg/weixinMenu")
public class MsgWeixinMenuController extends BaseController {
    @Autowired
    private IMsgWeixinMenuService msgWeixinMenuService;

    /**
     * 查询微信公众号菜单列表
     */
    @PreAuthorize("@ss.hasPermi('msg:weixinMenu:list')")
    @GetMapping("/list")
    public TableDataInfo list(MsgWeixinMenu msgWeixinMenu) {
        startPage();
        List<MsgWeixinMenu> list = msgWeixinMenuService.selectMsgWeixinMenuList(msgWeixinMenu);
        return getDataTable(list);
    }

    /**
     * 导出微信公众号菜单列表
     */
    @PreAuthorize("@ss.hasPermi('msg:weixinMenu:export')")
    @Log(title = "msg.weixin.menu.title", businessType = BusinessType.EXPORT)
    @GetMapping("/export")
    public AjaxResult export(MsgWeixinMenu msgWeixinMenu) {
        List<MsgWeixinMenu> list = msgWeixinMenuService.selectMsgWeixinMenuList(msgWeixinMenu);
        ExcelUtil<MsgWeixinMenu> util = new ExcelUtil<MsgWeixinMenu>(MsgWeixinMenu.class);
        return util.exportExcel(list, "weixinMenu");
    }

    /**
     * 获取微信公众号菜单详细信息
     */
    @PreAuthorize("@ss.hasPermi('msg:weixinMenu:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") String id) {
        return AjaxResult.success(msgWeixinMenuService.selectMsgWeixinMenuById(id));
    }

    /**
     * 新增微信公众号菜单
     */
    @PreAuthorize("@ss.hasPermi('msg:weixinMenu:add')")
    @Log(title = "msg.weixin.menu.title", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody MsgWeixinMenu msgWeixinMenu) {
        return toAjax(msgWeixinMenuService.insertMsgWeixinMenu(msgWeixinMenu));
    }

    /**
     * 修改微信公众号菜单
     */
    @PreAuthorize("@ss.hasPermi('msg:weixinMenu:edit')")
    @Log(title = "msg.weixin.menu.title", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody MsgWeixinMenu msgWeixinMenu) {
        return toAjax(msgWeixinMenuService.updateMsgWeixinMenu(msgWeixinMenu));
    }

    /**
     * 删除微信公众号菜单
     */
    @PreAuthorize("@ss.hasPermi('msg:weixinMenu:remove')")
    @Log(title = "msg.weixin.menu.title", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable String[] ids) {
        return toAjax(msgWeixinMenuService.deleteMsgWeixinMenuByIds(ids));
    }
}
