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
import cn.eyecool.msg.domain.MsgTemplate;
import cn.eyecool.msg.service.IMsgTemplateService;
import cn.eyecool.common.utils.poi.ExcelUtil;
import cn.eyecool.common.core.page.TableDataInfo;

/**
 * 消息模板Controller
 * 
 * @author admin
 * @date 2021-04-15
 */
@RestController
@RequestMapping("/msg/template")
public class MsgTemplateController extends BaseController
{
    @Autowired
    private IMsgTemplateService msgTemplateService;

    /**
     * 查询消息模板列表
     */
    @PreAuthorize("@ss.hasPermi('msg:template:list')")
    @GetMapping("/list")
    public TableDataInfo list(MsgTemplate msgTemplate)
    {
        startPage();
        List<MsgTemplate> list = msgTemplateService.selectMsgTemplateList(msgTemplate);
        return getDataTable(list);
    }

    /**
     * 导出消息模板列表
     */
    @PreAuthorize("@ss.hasPermi('msg:template:export')")
    @Log(title = "msg.template.title", businessType = BusinessType.EXPORT)
    @GetMapping("/export")
    public AjaxResult export(MsgTemplate msgTemplate)
    {
        List<MsgTemplate> list = msgTemplateService.selectMsgTemplateList(msgTemplate);
        ExcelUtil<MsgTemplate> util = new ExcelUtil<MsgTemplate>(MsgTemplate.class);
        return util.exportExcel(list, "template");
    }

    /**
     * 获取消息模板详细信息
     */
    @PreAuthorize("@ss.hasPermi('msg:template:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") String id)
    {
        return AjaxResult.success(msgTemplateService.selectMsgTemplateById(id));
    }

    /**
     * 新增消息模板
     */
    @PreAuthorize("@ss.hasPermi('msg:template:add')")
    @Log(title = "msg.template.title", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody MsgTemplate msgTemplate)
    {
        return toAjax(msgTemplateService.insertMsgTemplate(msgTemplate));
    }

    /**
     * 修改消息模板
     */
    @PreAuthorize("@ss.hasPermi('msg:template:edit')")
    @Log(title = "msg.template.title", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody MsgTemplate msgTemplate)
    {
        return toAjax(msgTemplateService.updateMsgTemplate(msgTemplate));
    }

    /**
     * 删除消息模板
     */
    @PreAuthorize("@ss.hasPermi('msg:template:remove')")
    @Log(title = "msg.template.title", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable String[] ids)
    {
        return toAjax(msgTemplateService.deleteMsgTemplateByIds(ids));
    }
}
