package cn.eyecool.scene.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
import cn.eyecool.common.utils.bean.BeanUtils;
import cn.eyecool.common.utils.poi.ExcelUtil;
import cn.eyecool.scene.domain.ChannelInfo;
import cn.eyecool.scene.domain.ChannelSubtreasuryInfo;
import cn.eyecool.scene.domain.vo.SceneInfoVO;
import cn.eyecool.scene.service.IChannelInfoService;
import cn.eyecool.scene.service.IChannelSubtreasuryInfoService;

/**
 * 场景信息Controller
 * 
 * @author admin
 * @date 2021-03-22
 */
@RestController
@RequestMapping("/scene/channel")
public class ChannelInfoController extends BaseController {

    @Autowired
    private IChannelInfoService channelInfoService;
    @Autowired
    private IChannelSubtreasuryInfoService channelSubtreasuryInfoService;

    /**
     * 查询场景信息列表
     */
    @PreAuthorize("@ss.hasPermi('scene:channel:list')")
    @GetMapping("/treeList")
    public AjaxResult listAll(ChannelInfo channelInfo) {
        List<ChannelInfo> list = channelInfoService.selectChannelInfoList(channelInfo);
        List<SceneInfoVO> sceneList = list.stream().map(it -> {
            SceneInfoVO sceneInfoVO = new SceneInfoVO();
            BeanUtils.copyBeanProp(sceneInfoVO, it);
            sceneInfoVO.setSceneCode(it.getChannelCode());
            sceneInfoVO.setSceneName(it.getChannelName());
            ChannelSubtreasuryInfo subCondition = new ChannelSubtreasuryInfo();
            subCondition.setChannelId(it.getId());
            List<ChannelSubtreasuryInfo> subInfoList =
                channelSubtreasuryInfoService.selectChannelSubtreasuryInfoList(subCondition);
            List<SceneInfoVO> subSceneInfoList = subInfoList.stream().map(subinfo -> {
                SceneInfoVO subSceneInfoVO = new SceneInfoVO();
                BeanUtils.copyBeanProp(subSceneInfoVO, subinfo);
                subSceneInfoVO.setSceneCode(subinfo.getSubTreasuryCode());
                subSceneInfoVO.setSceneName(subinfo.getSubTreasuryName());
                subSceneInfoVO.setParentId(it.getId());
                return subSceneInfoVO;
            }).collect(Collectors.toList());
            sceneInfoVO.setChildren(subSceneInfoList);
            return sceneInfoVO;
        }).collect(Collectors.toList());
        return AjaxResult.success(sceneList);
    }

    /**
     * 查询场景信息列表
     */
    @PreAuthorize("@ss.hasPermi('scene:channel:list')")
    @GetMapping("/list")
    public TableDataInfo list(ChannelInfo channelInfo) {
        startPage();
        List<ChannelInfo> list = channelInfoService.selectChannelInfoList(channelInfo);
        return getDataTable(list);
    }

    /**
     * 导出场景信息列表
     */
    @PreAuthorize("@ss.hasPermi('scene:channel:export')")
    @Log(title = "channel.info.scene.title", businessType = BusinessType.EXPORT)
    @GetMapping("/export")
    public AjaxResult export(ChannelInfo channelInfo) {
        List<ChannelInfo> list = channelInfoService.selectChannelInfoList(channelInfo);
        ExcelUtil<ChannelInfo> util = new ExcelUtil<ChannelInfo>(ChannelInfo.class);
        return util.exportExcel(list, "channel");
    }

    /**
     * 获取场景信息详细信息
     */
    @PreAuthorize("@ss.hasPermi('scene:channel:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") String id) {
        return AjaxResult.success(channelInfoService.selectChannelInfoById(id));
    }

    /**
     * 新增场景信息
     */
    @PreAuthorize("@ss.hasPermi('scene:channel:add')")
    @Log(title = "channel.info.scene.title", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody ChannelInfo channelInfo) {
        return toAjax(channelInfoService.insertChannelInfo(channelInfo));
    }

    /**
     * 修改场景信息
     */
    @PreAuthorize("@ss.hasPermi('scene:channel:edit')")
    @Log(title = "channel.info.scene.title", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody ChannelInfo channelInfo) {
        return toAjax(channelInfoService.updateChannelInfo(channelInfo));
    }

    /**
     * 删除场景信息
     */
    @PreAuthorize("@ss.hasPermi('scene:channel:remove')")
    @Log(title = "channel.info.scene.title", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable String[] ids) {
        return toAjax(channelInfoService.deleteChannelInfoByIds(ids));
    }

    /**
     * 查询场景信息列表
     */
    @GetMapping("/listAll")
    public AjaxResult listAll() {
        List<ChannelInfo> list = channelInfoService.selectChannelInfoList(new ChannelInfo());
        return AjaxResult.success(list);
    }

    /**
     * 生成场景编码
     * 
     * @return
     */
    @GetMapping("/genSceneCode")
    public AjaxResult genSceneCode() {
        String sceneCode = channelInfoService.generateSceneCode();
        Map<String, String> map = new HashMap<>();
        map.put("sceneCode", sceneCode);
        return AjaxResult.success(map);
    }

}
