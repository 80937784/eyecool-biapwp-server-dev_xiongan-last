package cn.eyecool.common.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * List操作工具类
 * 
 * @author admin
 * @date 2020年3月17日
 */
public class ListUtil {

    /**
     * List分割
     * 
     * @param unitSize 分组单个数量
     * @param list 需要分割的List
     * @return 分割的list集合
     */
    public static <T> List<List<T>> groupListByUnitSize(int unitSize, List<T> list) {
        List<List<T>> listGroup = new ArrayList<List<T>>();
        int listSize = list.size();
        // 子集合的长度
        int toIndex = unitSize;
        for (int i = 0; i < list.size(); i += unitSize) {
            if (i + unitSize > listSize) {
                toIndex = listSize - i;
            }
            List<T> newList = list.subList(i, i + toIndex);
            listGroup.add(newList);
        }
        return listGroup;
    }

    /**
     * List分割操作
     *
     * @param sourceList 需要分割的List
     * @param groupNum 分组个数
     * @return 分割的list集合
     */
    public static <T> List<List<T>> groupListByGroupNum(List<T> sourceList, Integer groupNum) {
        List<List<T>> groupList = new ArrayList<>();
        int total = sourceList.size();
        // 计算出余数
        int remainder = total % groupNum;
        // 计算出商
        int number = total / groupNum;
        // 偏移量
        int offset = 0;
        for (int i = 0; i < groupNum; i++) {
            List<T> subList;
            if (remainder > 0) {
                subList = sourceList.subList(i * number + offset, (i + 1) * number + offset + 1);
                remainder--;
                offset++;
            } else {
                subList = sourceList.subList(i * number + offset, (i + 1) * number + offset);
            }
            groupList.add(subList);
        }
        return groupList;
    }

}
