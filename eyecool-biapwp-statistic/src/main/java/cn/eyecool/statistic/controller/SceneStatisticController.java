package cn.eyecool.statistic.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cn.eyecool.common.core.controller.BaseController;
import cn.eyecool.common.core.domain.AjaxResult;
import cn.eyecool.statistic.service.ISceneStatisticService;

/**
 * 场景信息统计请求处理
 * 
 * @author mawj
 * @date 2021/08/26
 */
@RestController
@RequestMapping("/statistic/scene")
public class SceneStatisticController extends BaseController {

    @Autowired
    private ISceneStatisticService sceneStatisticService;

    /**
     * 统计场景数量
     * 
     * @return
     */
    @GetMapping("/countAll")
    public AjaxResult countAllScene() {
        int count = sceneStatisticService.countScene();
        return AjaxResult.success(count);
    }
}
