package cn.eyecool.device.vo;

import java.io.Serializable;
import java.util.List;

import cn.eyecool.scene.trade.vo.BasePersonLiveUpdateVO;
import lombok.Data;

/**
 * SummaryVO 数据同步数据对象
 * 
 * @author 段存明
 * @date 2019年11月12日
 */
@Data
public class SummaryVO implements Serializable {

    private static final long serialVersionUID = 1L;
    private int total;
    private boolean nextPullAllData;

    private String busiUpdateSeriaNum;
    private String sbusiUpdateSeriaNum;
    private String primarySubId;
    private String updateTime;

    private List<BasePersonLiveUpdateVO> list;

}
