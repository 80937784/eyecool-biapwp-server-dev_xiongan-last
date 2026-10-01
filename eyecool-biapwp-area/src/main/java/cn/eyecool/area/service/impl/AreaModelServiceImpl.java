package cn.eyecool.area.service.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cn.eyecool.area.domain.AreaModel;
import cn.eyecool.area.mapper.AreaModelMapper;
import cn.eyecool.area.service.IAreaModelService;
import cn.eyecool.common.constant.DictConstants;
import cn.eyecool.common.core.domain.TreeSelect;
import cn.eyecool.common.exception.CustomException;
import cn.eyecool.common.utils.DateUtils;
import cn.eyecool.common.utils.MessageUtils;
import cn.eyecool.common.utils.StringUtils;

/**
 * 区域Service业务层处理
 * 
 * @author admin
 * @date 2021-03-26
 */
@Service
public class AreaModelServiceImpl implements IAreaModelService {
    @Autowired
    private AreaModelMapper areaModelMapper;

    /**
     * 查询区域
     * 
     * @param id 区域ID
     * @return 区域
     */
    @Override
    public AreaModel selectAreaModelById(Long id) {
        return areaModelMapper.selectAreaModelById(id);
    }

    /**
     * 查询区域列表
     * 
     * @param areaModel 区域
     * @return 区域
     */
    @Override
    public List<AreaModel> selectAreaModelList(AreaModel areaModel) {
        return areaModelMapper.selectAreaModelList(areaModel);
    }

    /**
     * 新增区域
     * 
     * @param areaModel 区域
     * @return 结果
     */
    @Override
    @Transactional
    public int insertAreaModel(AreaModel areaModel) {
        if (null == areaModel.getParentId()) {
            areaModel.setParentId(0L);
            areaModel.setAncestors("0");
        }
        AreaModel info = areaModelMapper.selectAreaModelById(areaModel.getParentId());
        // 如果父节点不为正常状态,则不允许新增子节点
        if (null != info) {
            if (DictConstants.Status.DISABLE.equals(areaModel.getStatus())) {
                throw new CustomException(MessageUtils.message("area.add.fail.parent.disabled"));
            }
            areaModel.setAncestors(info.getAncestors() + "," + areaModel.getParentId());
        }
        areaModel.setCreateTime(DateUtils.getNowDate());
        return areaModelMapper.insertAreaModel(areaModel);
    }

    /**
     * 修改区域
     * 
     * @param areaModel 区域
     * @return 结果
     */
    @Override
    @Transactional
    public int updateAreaModel(AreaModel areaModel) {
        AreaModel newParentArea = areaModelMapper.selectAreaModelById(areaModel.getParentId());
        AreaModel oldArea = areaModelMapper.selectAreaModelById(areaModel.getId());
        if (StringUtils.isNotNull(newParentArea) && StringUtils.isNotNull(oldArea)) {
            String newAncestors = newParentArea.getAncestors() + "," + newParentArea.getId();
            String oldAncestors = oldArea.getAncestors();
            areaModel.setAncestors(newAncestors);
            updateAreaChildren(areaModel.getId(), newAncestors, oldAncestors);
        }
        areaModel.setUpdateTime(DateUtils.getNowDate());
        int result = areaModelMapper.updateAreaModel(areaModel);
        if (DictConstants.Status.ENABLE.equals(areaModel.getStatus())) {
            // 如果该部门是启用状态，则启用该部门的所有上级部门
            updateParentAreaStatus(areaModel);
        }
        return result;
    }

    /**
     * 修改该区域的父级区域状态
     * 
     * @param areaModel 当前区域
     */
    private void updateParentAreaStatus(AreaModel areaModel) {
        String updateBy = areaModel.getUpdateBy();
        areaModel = areaModelMapper.selectAreaModelById(areaModel.getId());
        areaModel.setUpdateBy(updateBy);
        areaModel.setUpdateTime(DateUtils.getNowDate());
        areaModelMapper.updateAreaStatus(areaModel);
    }

    /**
     * 修改子元素关系
     * 
     * @param areaId 被修改的区域ID
     * @param newAncestors 新的父ID集合
     * @param oldAncestors 旧的父ID集合
     */
    public void updateAreaChildren(Long areaId, String newAncestors, String oldAncestors) {
        List<AreaModel> children = areaModelMapper.selectChildrenAreaById(areaId);
        for (AreaModel child : children) {
            child.setAncestors(child.getAncestors().replace(oldAncestors, newAncestors));
        }
        if (children.size() > 0) {
            areaModelMapper.updateAreaChildren(children);
        }
    }

    /**
     * 批量删除区域
     * 
     * @param ids 需要删除的区域ID
     * @return 结果
     */
    @Override
    public int deleteAreaModelByIds(Long[] ids) {
        return areaModelMapper.deleteAreaModelByIds(ids);
    }

    /**
     * 删除区域信息
     * 
     * @param id 区域ID
     * @return 结果
     */
    @Override
    public int deleteAreaModelById(Long id) {
        return areaModelMapper.deleteAreaModelById(id);
    }

    /**
     * 校验区域名称是否唯一
     * 
     * @param areaModel
     * @return
     */
    @Override
    public boolean checkAreaNameUnique(AreaModel areaModel) {
        Long id = StringUtils.isNull(areaModel.getId()) ? -1L : areaModel.getId();
        Long parentId = StringUtils.isNull(areaModel.getParentId()) ? 0L : areaModel.getParentId();
        AreaModel info = areaModelMapper.checkAreaNameUnique(areaModel.getAreaName(), parentId);
        return StringUtils.isNull(info) || info.getId().longValue() == id.longValue();
    }

    /**
     * 根据ID查询所有子区域（正常状态）
     *
     * @param id 区域ID
     * @return 子区域数
     */
    @Override
    public int selectNormalChildrenCountById(Long id) {
        return areaModelMapper.selectChildrenCountById(id, DictConstants.Status.ENABLE);
    }

    /**
     * 查询区域数量
     * 
     * @param id
     * @return
     */
    @Override
    public int selectChildrenCount(Long id) {
        return areaModelMapper.selectChildrenCountById(id, null);
    }

    /**
     * 构建前端所需要树结构
     * 
     * @param list 区域列表
     * @return 树结构列表
     */
    @Override
    public List<AreaModel> buildAreaTree(List<AreaModel> list) {
        List<AreaModel> returnList = new ArrayList<AreaModel>();
        List<Long> tempList = new ArrayList<Long>();
        for (AreaModel area : list) {
            tempList.add(area.getId());
        }
        for (Iterator<AreaModel> iterator = list.iterator(); iterator.hasNext();) {
            AreaModel area = iterator.next();
            // 如果是顶级节点, 遍历该父节点的所有子节点
            if (!tempList.contains(area.getParentId())) {
                recursionFn(list, area);
                returnList.add(area);
            }
        }
        if (returnList.isEmpty()) {
            returnList = list;
        }
        return returnList;
    }

    /**
     * 递归列表
     */
    private void recursionFn(List<AreaModel> list, AreaModel t) {
        // 得到子节点列表
        List<AreaModel> childList = getChildList(list, t);
        t.setChildren(childList);
        for (AreaModel tChild : childList) {
            if (hasChild(list, tChild)) {
                recursionFn(list, tChild);
            }
        }
    }

    /**
     * 得到子节点列表
     */
    private List<AreaModel> getChildList(List<AreaModel> list, AreaModel t) {
        List<AreaModel> tlist = new ArrayList<AreaModel>();
        Iterator<AreaModel> it = list.iterator();
        while (it.hasNext()) {
            AreaModel n = it.next();
            if (StringUtils.isNotNull(n.getParentId()) && n.getParentId().longValue() == t.getId().longValue()) {
                tlist.add(n);
            }
        }
        return tlist;
    }

    /**
     * 判断是否有子节点
     */
    private boolean hasChild(List<AreaModel> list, AreaModel t) {
        return getChildList(list, t).size() > 0 ? true : false;
    }

    /**
     * 构建前端所需要下拉树结构
     * 
     * @param list 区域列表
     * @return 下拉树结构列表
     */
    @Override
    public List<TreeSelect> buildAreaTreeSelect(List<AreaModel> list) {
        List<AreaModel> areaTrees = buildAreaTree(list);
        return areaTrees.stream().map(area -> {
            TreeSelect treeSelect = new TreeSelect();
            treeSelect.setId(area.getId());
            treeSelect.setLabel(area.getAreaName());
            treeSelect.setChildren(area.getChildren().stream().map(item -> {
                AreaModel areaItem = (AreaModel)item;
                TreeSelect childTreeSelect = new TreeSelect();
                childTreeSelect.setId(areaItem.getId());
                childTreeSelect.setLabel(areaItem.getAreaName());
                return childTreeSelect;
            }).collect(Collectors.toList()));
            return treeSelect;
        }).collect(Collectors.toList());
    }

}
