package cn.eyecool.statistic.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.eyecool.statistic.mapper.SceneStatisticMapper;
import cn.eyecool.statistic.service.ISceneStatisticService;

/**
 * 场景统计业务层
 * 
 * @author mawj
 * @date 2021/08/26
 */
@Service
public class SceneStatisticServiceImpl implements ISceneStatisticService {

    @Autowired
    private SceneStatisticMapper sceneStatisticMapper;

    /**
     * 查询场景数量
     * 
     * @return
     */
    @Override
    public int countScene() {
        return sceneStatisticMapper.countScene();
    }

}
