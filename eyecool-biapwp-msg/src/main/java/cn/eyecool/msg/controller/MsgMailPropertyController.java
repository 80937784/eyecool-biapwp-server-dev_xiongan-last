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
import cn.eyecool.msg.domain.MsgMailProperty;
import cn.eyecool.msg.service.IMsgMailPropertyService;
import cn.eyecool.common.utils.poi.ExcelUtil;
import cn.eyecool.common.core.page.TableDataInfo;

/**
 * 邮箱配置Controller
 * 
 * @author admin
 * @date 2021-04-15
 */
@RestController
@RequestMapping("/msg/mailProperty")
public class MsgMailPropertyController extends BaseController
{
    @Autowired
    private IMsgMailPropertyService msgMailPropertyService;

    /**
     * 查询邮箱配置列表
     */
    @PreAuthorize("@ss.hasPermi('msg:mailProperty:list')")
    @GetMapping("/list")
    public TableDataInfo list(MsgMailProperty msgMailProperty)
    {
        startPage();
        List<MsgMailProperty> list = msgMailPropertyService.selectMsgMailPropertyList(msgMailProperty);
        return getDataTable(list);
    }

    /**
     * 导出邮箱配置列表
     */
    @PreAuthorize("@ss.hasPermi('msg:mailProperty:export')")
    @Log(title = "msg.email.name", businessType = BusinessType.EXPORT)
    @GetMapping("/export")
    public AjaxResult export(MsgMailProperty msgMailProperty)
    {
        List<MsgMailProperty> list = msgMailPropertyService.selectMsgMailPropertyList(msgMailProperty);
        ExcelUtil<MsgMailProperty> util = new ExcelUtil<MsgMailProperty>(MsgMailProperty.class);
        return util.exportExcel(list, "mailProperty");
    }

    /**
     * 获取邮箱配置详细信息
     */
    @PreAuthorize("@ss.hasPermi('msg:mailProperty:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") String id)
    {
        return AjaxResult.success(msgMailPropertyService.selectMsgMailPropertyById(id));
    }

    /**
     * 新增邮箱配置
     */
    @PreAuthorize("@ss.hasPermi('msg:mailProperty:add')")
    @Log(title = "msg.email.name", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody MsgMailProperty msgMailProperty)
    {
        return toAjax(msgMailPropertyService.insertMsgMailProperty(msgMailProperty));
    }

    /**
     * 修改邮箱配置
     */
    @PreAuthorize("@ss.hasPermi('msg:mailProperty:edit')")
    @Log(title = "msg.email.name", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody MsgMailProperty msgMailProperty)
    {
        return toAjax(msgMailPropertyService.updateMsgMailProperty(msgMailProperty));
    }

    /**
     * 删除邮箱配置
     */
    @PreAuthorize("@ss.hasPermi('msg:mailProperty:remove')")
    @Log(title = "msg.email.name", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable String[] ids)
    {
        return toAjax(msgMailPropertyService.deleteMsgMailPropertyByIds(ids));
    }
}
