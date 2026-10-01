package cn.eyecool.statistic.service.impl;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.beust.jcommander.internal.Lists;

import cn.eyecool.area.domain.AreaModel;
import cn.eyecool.common.utils.DateUtils;
import cn.eyecool.statistic.domain.AreaLogNumInfo;
import cn.eyecool.statistic.mapper.AreaStatisticMapper;
import cn.eyecool.statistic.mapper.TradeLogStatisticMapper;
import cn.eyecool.statistic.service.IAreaStatisticService;

/**
 * 区域信息统计业务层
 * 
 * @author mawj
 * @date 2021/08/26
 */
@Service
public class AreaStatisticServiceImpl implements IAreaStatisticService {

    @Autowired
    private AreaStatisticMapper areaStatisticMapper;
    @Autowired
    private TradeLogStatisticMapper tradeLogStatisticMapper;

    /**
     * 查询今日业务交易排名前5的区域交易数量
     * 
     * @return
     */
    @Override
    public List<AreaLogNumInfo> todayTop5AreaFaceLogNum() {
        List<AreaModel> level1AreaModelList = areaStatisticMapper.getLevel1AreaModel();
        if (CollectionUtils.isEmpty(level1AreaModelList)) {
            return Collections.emptyList();
        }
        Date startTime = DateUtils.getTodayStartTime();
        Date endTime = DateUtils.getNowDate();
        List<AreaLogNumInfo> list = Lists.newArrayList();
        level1AreaModelList.forEach(area -> {
            int face1v1AreaLogNum =
                tradeLogStatisticMapper.getFace1v1AreaLogNum(startTime, endTime, null, area.getId());
            int face1vNAreaLogNum =
                tradeLogStatisticMapper.getFace1vNAreaLogNum(startTime, endTime, null, area.getId());
            AreaLogNumInfo info =
                new AreaLogNumInfo(area.getId(), area.getAreaName(), face1v1AreaLogNum + face1vNAreaLogNum);
            list.add(info);
        });
        List<AreaLogNumInfo> resList = list.stream().sorted(Comparator.comparing(AreaLogNumInfo::getLogNum).reversed())
            .limit(5L).collect(Collectors.toList());
        return resList;
    }

}
