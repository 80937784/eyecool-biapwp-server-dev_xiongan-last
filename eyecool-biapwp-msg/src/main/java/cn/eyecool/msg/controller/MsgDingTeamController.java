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
import cn.eyecool.msg.domain.MsgDingTeam;
import cn.eyecool.msg.service.IMsgDingTeamService;

/**
 * 钉钉团队(企业)Controller
 * 
 * @author admin
 * @date 2021-04-15
 */
@RestController
@RequestMapping("/msg/dingTeam")
public class MsgDingTeamController extends BaseController {
    @Autowired
    private IMsgDingTeamService msgDingTeamService;

    /**
     * 查询钉钉团队(企业)列表
     */
    @PreAuthorize("@ss.hasPermi('msg:dingTeam:list')")
    @GetMapping("/list")
    public TableDataInfo list(MsgDingTeam msgDingTeam) {
        startPage();
        List<MsgDingTeam> list = msgDingTeamService.selectMsgDingTeamList(msgDingTeam);
        return getDataTable(list);
    }

    /**
     * 导出钉钉团队(企业)列表
     */
    @PreAuthorize("@ss.hasPermi('msg:dingTeam:export')")
    @Log(title = "ding.team.name", businessType = BusinessType.EXPORT)
    @GetMapping("/export")
    public AjaxResult export(MsgDingTeam msgDingTeam) {
        List<MsgDingTeam> list = msgDingTeamService.selectMsgDingTeamList(msgDingTeam);
        ExcelUtil<MsgDingTeam> util = new ExcelUtil<MsgDingTeam>(MsgDingTeam.class);
        return util.exportExcel(list, "dingTeam");
    }

    /**
     * 获取钉钉团队(企业)详细信息
     */
    @PreAuthorize("@ss.hasPermi('msg:dingTeam:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") String id) {
        return AjaxResult.success(msgDingTeamService.selectMsgDingTeamById(id));
    }

    /**
     * 新增钉钉团队(企业)
     */
    @PreAuthorize("@ss.hasPermi('msg:dingTeam:add')")
    @Log(title = "ding.team.name", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody MsgDingTeam msgDingTeam) {
        return toAjax(msgDingTeamService.insertMsgDingTeam(msgDingTeam));
    }

    /**
     * 修改钉钉团队(企业)
     */
    @PreAuthorize("@ss.hasPermi('msg:dingTeam:edit')")
    @Log(title = "ding.team.name", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody MsgDingTeam msgDingTeam) {
        return toAjax(msgDingTeamService.updateMsgDingTeam(msgDingTeam));
    }

    /**
     * 删除钉钉团队(企业)
     */
    @PreAuthorize("@ss.hasPermi('msg:dingTeam:remove')")
    @Log(title = "ding.team.name", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable String[] ids) {
        return toAjax(msgDingTeamService.deleteMsgDingTeamByIds(ids));
    }

    /**
     * 查询钉钉团队(企业)列表
     */
    @GetMapping("/listAll")
    public AjaxResult listAll(MsgDingTeam msgDingTeam) {
        List<MsgDingTeam> list = msgDingTeamService.selectMsgDingTeamList(msgDingTeam);
        return AjaxResult.success(list);
    }

}
