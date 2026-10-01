package cn.eyecool.scene.service.impl;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.beust.jcommander.internal.Lists;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Maps;

import cn.eyecool.basedata.domain.BasePersonInfo;
import cn.eyecool.basedata.manager.IPersonDataManagerLogicService;
import cn.eyecool.common.constant.DictConstants;
import cn.eyecool.common.core.domain.AjaxResult;
import cn.eyecool.common.core.text.Convert;
import cn.eyecool.common.exception.CustomException;
import cn.eyecool.common.utils.DateUtils;
import cn.eyecool.common.utils.IdWorker;
import cn.eyecool.common.utils.MessageUtils;
import cn.eyecool.common.utils.SecurityUtils;
import cn.eyecool.common.utils.bean.BeanUtils;
import cn.eyecool.common.utils.sql.SqlUtil;
import cn.eyecool.scene.constant.OperateLock;
import cn.eyecool.scene.domain.ChannelBusiParam.BindTypeEnum;
import cn.eyecool.scene.domain.ChannelSubBusiParam;
import cn.eyecool.scene.domain.ChannelSubtreasuryBusi;
import cn.eyecool.scene.domain.ChannelSubtreasuryInfo;
import cn.eyecool.scene.event.ISubTreasuryEventCallback;
import cn.eyecool.scene.event.SubtreasuryEventPublishlService;
import cn.eyecool.scene.mapper.ChannelSubtreasuryBusiMapper;
import cn.eyecool.scene.mapper.ChannelSubtreasuryInfoMapper;
import cn.eyecool.scene.service.IBusiLiveUpdateSeqService;
import cn.eyecool.scene.service.IChannelSubtreasuryBusiService;
import lombok.extern.slf4j.Slf4j;

/**
 * 子场景人员Service业务层处理
 * 
 * @author admin
 * @date 2021-03-22
 */
@Service
@Slf4j
public class ChannelSubtreasuryBusiServiceImpl implements IChannelSubtreasuryBusiService {

    @Autowired
    private IBusiLiveUpdateSeqService busiLiveUpdateSeqService;
    @Autowired
    private ChannelSubtreasuryBusiMapper channelSubtreasuryBusiMapper;
    @Autowired
    private ChannelSubtreasuryInfoMapper channelSubtreasuryInfoMapper;
    @Autowired
    private IPersonDataManagerLogicService personDataManagerLogicService;
    @Autowired
    private SubtreasuryEventPublishlService subtreasuryEventPublishlService;
    /** 自己注入自己实现同一个类内部方法调用事务起作用，spring框架解决了循环注入问题，但注入自己这种方式要慎用 */
    @Autowired
    private IChannelSubtreasuryBusiService channelSubtreasuryBusiService;

    /**
     * 查询子场景人员
     * 
     * @param id 子场景人员ID
     * @return 子场景人员
     */
    @Override
    public ChannelSubtreasuryBusi selectChannelSubtreasuryBusiById(String id) {
        return channelSubtreasuryBusiMapper.selectChannelSubtreasuryBusiById(id);
    }

    /**
     * 查询子场景人员列表
     * 
     * @param channelSubtreasuryBusi 子场景人员
     * @return 子场景人员
     */
    @Override
    public List<ChannelSubtreasuryBusi>
        selectChannelSubtreasuryBusiList(ChannelSubtreasuryBusi channelSubtreasuryBusi) {
        return channelSubtreasuryBusiMapper.selectChannelSubtreasuryBusiList(channelSubtreasuryBusi);
    }

    /**
     * 新增子场景人员
     * 
     * @param channelSubtreasuryBusi 子场景人员
     * @return 结果
     */
    @Override
    public int insertChannelSubtreasuryBusi(ChannelSubBusiParam param) {
        BindTypeEnum bindTypeEnum = BindTypeEnum.parse(param.getBindType());
        switch (bindTypeEnum) {
            case BIND_ALL:
                return bindAllPersonByCondition(param, false);
            case BIND_BY_DEPT:
                return bindAllPersonByCondition(param, true);
            case BIND_SELF_DEFINE:
                return bindPersonBySelfDefine(param);
            default:
                throw new CustomException(MessageUtils.message("channel.sub.busi.service.type.unknown"));
        }
    }

    /**
     * 根据条件查询人员添加到场景库
     * 
     * @param param
     * @param byDept
     * @return
     */
    private int bindAllPersonByCondition(ChannelSubBusiParam param, boolean byDept) {
        ChannelSubtreasuryInfo subtreasuryInfo =
            channelSubtreasuryInfoMapper.selectChannelSubtreasuryInfoById(param.getSubTreasuryId());
        String subTreasuryCode = subtreasuryInfo.getSubTreasuryCode();
        BasePersonInfo basePersonInfo = new BasePersonInfo();
        if (byDept) {
            basePersonInfo.setDeptId(param.getDeptId());
        }
        long total = 0; // 数据总量
        int pageNum = 1;// pageNum一直是第一页，因为中间查询待加入数量已经排除掉了已加入人员,分页一直查询第一页就可以
        int pageSize = 1000;// 分页数量
        String orderBy = SqlUtil.escapeOrderBySql("base.id asc, base.unique_id asc");
        // 序列号原子对象
        final AtomicInteger successNum = new AtomicInteger(0);// 更新成功数量原子对象
        final AtomicInteger failNum = new AtomicInteger(0);// 更新失败数量原子对象
        do {
            PageHelper.startPage(pageNum, pageSize, orderBy);
            List<BasePersonInfo> list = channelSubtreasuryBusiMapper
                .selectUnbindPersonInfoList(subtreasuryInfo.getChannelId(), param.getSubTreasuryId(), basePersonInfo);
            total = new PageInfo<BasePersonInfo>(list).getTotal();
            if (total == 0) {
                break;
            }
            list.stream().forEach(it -> {
                // 注意：如果使用并行流操作，禁止在外部创建对象或者使用同一引用
                ChannelSubtreasuryBusi busi = new ChannelSubtreasuryBusi();
                busi.setPersonId(it.getId());
                busi.setUniqueId(it.getUniqueId());
                busi.setChannelId(subtreasuryInfo.getChannelId());
                busi.setSubTreasuryId(param.getSubTreasuryId());
                busi.setRemark(param.getRemark());
                try {
                    channelSubtreasuryBusiService.insertSingleSubBusiData(busi, subTreasuryCode);
                    successNum.getAndIncrement();
                } catch (Exception e) {
                    log.error("Failed to add person to sub-scenario,subTreasuryCode:[{}],uiqueId:[{}]", subTreasuryCode, it.getUniqueId(), e);
                    failNum.getAndIncrement();
                }
            });
        } while (pageSize < total);
        log.info(MessageUtils.message("channel.sub.busi.service.bind.person.scene.success", successNum.get(),subTreasuryCode));
        if (failNum.get() > 0) {
            String msg = MessageUtils.message("channel.sub.busi.service.bind.person.scene.failed", failNum.get(),subTreasuryCode);
            log.info(msg);
            throw new CustomException(msg);
        }
        return successNum.get() > 0 ? successNum.get() : 1;
    }

    /**
     * 自定义选择人员添加到场景库
     * 
     * @param channelBusiness
     * @return
     */
    private int bindPersonBySelfDefine(ChannelSubBusiParam busiParam) {
        String[] personIds = Convert.toStrArray(busiParam.getPersonId());
        String[] uniqueIds = Convert.toStrArray(busiParam.getUniqueId());
        // 序列号原子对象
        final AtomicInteger successNum = new AtomicInteger(0);// 更新成功数量原子对象
        final AtomicInteger failNum = new AtomicInteger(0);// 更新失败数量原子对象
        ChannelSubtreasuryBusi targetBusi = new ChannelSubtreasuryBusi();
        BeanUtils.copyBeanProp(targetBusi, busiParam);
        try {
            targetBusi.setCreateBy(SecurityUtils.getUsername());
        } catch (Exception e) {
        }
        ChannelSubtreasuryInfo subtreasuryInfo =
            channelSubtreasuryInfoMapper.selectChannelSubtreasuryInfoById(busiParam.getSubTreasuryId());
        String subTreasuryCode = subtreasuryInfo.getSubTreasuryCode();
        String channelId = subtreasuryInfo.getChannelId();
        for (int i = 0; i < personIds.length; i++) {
            try {
                targetBusi.setChannelId(channelId);
                targetBusi.setPersonId(personIds[i]);
                targetBusi.setUniqueId(uniqueIds[i]);
                channelSubtreasuryBusiService.insertSingleSubBusiData(targetBusi, subTreasuryCode);
                successNum.getAndIncrement();
            } catch (Exception e) {
                log.error("Failed to add people to sub-scene library,subTreasuryCode:[{}],uiqueId:[{}]", subTreasuryCode, uniqueIds[i], e);
                failNum.getAndIncrement();
            }
        }
        
        log.info(MessageUtils.message("channel.sub.busi.service.bind.person.scene.success", successNum.get()));
        if (failNum.get() > 0) {
            String msg = MessageUtils.message("channel.sub.busi.service.bind.person.scene.failed", failNum.get());
            log.info(msg);
            throw new CustomException(msg);
        }
        return successNum.get();
    }

    /**
     * 修改子场景人员
     * 
     * @param channelSubtreasuryBusi 子场景人员
     * @return 结果
     */
    @Override
    @Transactional
    public int updateChannelSubtreasuryBusi(ChannelSubtreasuryBusi channelSubtreasuryBusi) {
        Long updateSeriaNum = busiLiveUpdateSeqService.incrementAndGetSubtreasuryBusiSeqNum();
        channelSubtreasuryBusi.setUpdateSeriaNum(updateSeriaNum);
        channelSubtreasuryBusi.setUpdateTime(DateUtils.getNowDate());
        try {
            channelSubtreasuryBusi.setUpdateBy(SecurityUtils.getUsername());
        } catch (Exception e) {
        }
        return channelSubtreasuryBusiMapper.updateChannelSubtreasuryBusi(channelSubtreasuryBusi);
    }

    /**
     * 批量删除子场景人员
     * 
     * @param ids 需要删除的子场景人员ID
     * @return 结果
     */
    @Override
    @Transactional
    public int deleteChannelSubtreasuryBusiByIds(String[] ids) {
        int result = 0;
        for (String id : ids) {
            result += deleteChannelSubtreasuryBusiById(id);
        }
        return result;
    }

    /**
     * 删除子场景人员信息
     * 
     * @param id 子场景人员ID
     * @return 结果
     */
    @Override
    @Transactional
    public int deleteChannelSubtreasuryBusiById(String id) {
        ChannelSubtreasuryBusi subtreasuryBusi = channelSubtreasuryBusiMapper.selectChannelSubtreasuryBusiById(id);
        subtreasuryBusi.setStatus(DictConstants.Status.DISABLE);
        // 更新标识序列号
        Long updateSeriaNum = busiLiveUpdateSeqService.incrementAndGetSubtreasuryBusiSeqNum();
        subtreasuryBusi.setUpdateSeriaNum(updateSeriaNum);
        subtreasuryBusi.setUpdateTime(DateUtils.getNowDate());
        try {
            subtreasuryBusi.setUpdateBy(SecurityUtils.getUsername());
        } catch (Exception e) {
        }
        int result = channelSubtreasuryBusiMapper.updateChannelSubtreasuryBusi(subtreasuryBusi);
        personDataManagerLogicService.deleteLibraryPerson(subtreasuryBusi.getSubTreasuryCode(),
            subtreasuryBusi.getUniqueId());
        return result;
    }

    /**
     * 查询未绑定子场景的人员列表
     * 
     * @param basePersonInfo 人员信息
     * @param subTreasuryId 子场景主键
     * @param channelId 场景主键
     * @return
     */
    @Override
    public List<BasePersonInfo> selectUnbindPersonInfoList(BasePersonInfo basePersonInfo, String subTreasuryId,
        String channelId) {
        return channelSubtreasuryBusiMapper.selectUnbindPersonInfoList(channelId, subTreasuryId, basePersonInfo);
    }

    /**
     * 单个人库关系绑定
     * 
     * @param targetBusi 子场景人员
     * @param subTreasuryCode 子场景编码
     */
    @Override
    @Transactional
    public void insertSingleSubBusiData(ChannelSubtreasuryBusi targetBusi, String subTreasuryCode) {
        ChannelSubtreasuryBusi condition = new ChannelSubtreasuryBusi();
        condition.setPersonId(targetBusi.getPersonId());
        condition.setChannelId(targetBusi.getChannelId());
        condition.setSubTreasuryId(targetBusi.getSubTreasuryId());
        List<ChannelSubtreasuryBusi> list = channelSubtreasuryBusiMapper.selectChannelSubtreasuryBusiList(condition);
        if (CollectionUtils.isEmpty(list)) {
            targetBusi.setId(IdWorker.getNextStringId());
            targetBusi.setCreateTime(DateUtils.getNowDate());
            // 更新标识序列号
            Long updateSeriaNum = busiLiveUpdateSeqService.incrementAndGetSubtreasuryBusiSeqNum();
            targetBusi.setUpdateSeriaNum(updateSeriaNum);
            channelSubtreasuryBusiMapper.insertChannelSubtreasuryBusi(targetBusi);
            personDataManagerLogicService.addLibraryPerson(subTreasuryCode, targetBusi.getUniqueId());
            return;
        }
        ChannelSubtreasuryBusi existObject = list.get(0);
        boolean isDisabled = DictConstants.Status.DISABLE.equals(existObject.getStatus());
        BeanUtils.copyProperties(targetBusi, existObject, "id");
        existObject.setStatus(DictConstants.Status.ENABLE);
        existObject.setUpdateTime(DateUtils.getNowDate());
        // 子场景人员更新标识序列号
        Long subSeqNum = busiLiveUpdateSeqService.incrementAndGetSubtreasuryBusiSeqNum();
        existObject.setUpdateSeriaNum(subSeqNum);
        channelSubtreasuryBusiMapper.updateChannelSubtreasuryBusi(existObject);
        if (isDisabled) {// 之前存在无效的信息，修改状态后需要重新加入datamanager
            personDataManagerLogicService.addLibraryPerson(subTreasuryCode, targetBusi.getUniqueId());
        }
    }

    /**
     * 同步人库关系信息到Datamanager
     * 
     * @param sbusi
     * @return
     */
    @Override
    public AjaxResult syncdata(ChannelSubtreasuryBusi sbusi) {
        boolean tryLock = OperateLock.synclock.tryLock();
        if (!tryLock) {
            throw new CustomException(MessageUtils.message("channel.busi.service.syn.taks.busy"));
        }
        try {
            long total = 0; // 数据总量
            int pageNum = 1;// 分页
            int pageSize = 1000;// 分页数量
            String orderBy = SqlUtil.escapeOrderBySql("sbusi.create_time asc, sbusi.id asc");
            // 序列号原子对象
            final AtomicInteger successNum = new AtomicInteger(0);// 更新成功数量原子对象
            final AtomicInteger failNum = new AtomicInteger(0);// 更新失败数量原子对象
            do {
                PageHelper.startPage(pageNum, pageSize, orderBy);
                List<ChannelSubtreasuryBusi> list =
                    channelSubtreasuryBusiMapper.selectChannelSubtreasuryBusiList(new ChannelSubtreasuryBusi());
                list.stream().forEach(info -> {
                    if (handleSyncSingleData(info)) {
                        successNum.getAndIncrement();
                    } else {
                        failNum.getAndIncrement();
                    }
                });
                total = new PageInfo<ChannelSubtreasuryBusi>(list).getTotal();
                pageNum++;
            } while ((pageNum - 1) * pageSize < total);
            Map<String, Integer> map = Maps.newHashMap();
            map.put("failNum", failNum.get());
            map.put("successNum", successNum.get());
            String msg = "";
            if (failNum.get() > 0) {
                msg = MessageUtils.message("channel.busi.service.syn.scene.sumary", successNum.get(),failNum.get());
                return AjaxResult.error(msg, map);
            } else {
                msg =  MessageUtils.message("channel.busi.service.syn.scene.all.success");
                return AjaxResult.success(msg, map);
            }
        } finally {
            OperateLock.synclock.unlock();
        }
    }

    /**
     * 同步单条子场景人员数据到Datamanager
     * 
     * @param sbusi
     */
    private boolean handleSyncSingleData(ChannelSubtreasuryBusi sbusi) {
        try {
            if (DictConstants.Status.ENABLE.equals(sbusi.getStatus())) {
                personDataManagerLogicService.addLibraryPerson(sbusi.getSubTreasuryCode(), sbusi.getUniqueId());
            } else {
                personDataManagerLogicService.deleteLibraryPerson(sbusi.getSubTreasuryCode(), sbusi.getUniqueId());
            }
            return true;
        } catch (Exception e) {
            log.error("Syncing scene library personnel data to Datamanager is abnormal,subTreasuryCode:[{}],uniqueId:[{}]", sbusi.getSubTreasuryCode(),
                sbusi.getUniqueId(), e);
            return false;
        }
    }

    /**
     * 清空子场景数据
     * 
     * @param subtreasuryIds
     */
    @Override
    @Transactional
    public void clearSubData(String[] subtreasuryIds) {
        List<String> subCodes = Lists.newArrayList();
        channelSubtreasuryBusiMapper.deleteChannelSubtreasuryBusiBySubIds(subtreasuryIds);
        for (String subId : subtreasuryIds) {
            ChannelSubtreasuryInfo subtreasuryInfo =
                channelSubtreasuryInfoMapper.selectChannelSubtreasuryInfoById(subId);
            if (null != subtreasuryInfo) {
                personDataManagerLogicService.deleteLibrary(subtreasuryInfo.getSubTreasuryCode());
                subCodes.add(subtreasuryInfo.getSubTreasuryCode());
            }
        }
        // 通过Spring的事件收发机制发布时间，通知设备模块更新全量拉取标志
        subtreasuryEventPublishlService.clearSubDataPublish(subCodes, new ISubTreasuryEventCallback() {
            @Override
            public void onError(String errmsg) {
                log.error("Clear sub-scene data event release processing failed:[]", errmsg);
            }
        });
    }
}
