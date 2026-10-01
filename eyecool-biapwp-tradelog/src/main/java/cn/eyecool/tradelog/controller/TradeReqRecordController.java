package cn.eyecool.tradelog.controller;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;

import cn.eyecool.common.core.controller.BaseController;
import cn.eyecool.common.core.domain.AjaxResult;
import cn.eyecool.common.core.page.TableDataInfo;
import cn.eyecool.tradelog.domain.TradeReqRecord;
import cn.eyecool.tradelog.domain.TradeReqRecordDetail;
import cn.eyecool.tradelog.service.ITradeReqRecordService;
import lombok.extern.slf4j.Slf4j;

/**
 * 接口交易请求记录Controller
 * 
 * @author admin
 * @date 2021-05-13
 */
@Slf4j
@RestController
@RequestMapping("/tradelog/reqrecord")
public class TradeReqRecordController extends BaseController {

    @Autowired
    private ITradeReqRecordService tradeReqRecordService;

    /**
     * 查询接口交易请求记录列表
     */
    @PreAuthorize("@ss.hasPermi('tradelog:reqrecord:list')")
    @GetMapping("/list")
    public TableDataInfo list(TradeReqRecord tradeReqRecord) {
        startPage();
        List<TradeReqRecord> list = tradeReqRecordService.selectTradeReqRecordList(tradeReqRecord);
        return getDataTable(list);
    }

    /**
     * 获取接口交易请求记录详细信息
     */
    @PreAuthorize("@ss.hasPermi('tradelog:reqrecord:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") String id) {
        TradeReqRecord reqRecord = tradeReqRecordService.selectTradeReqRecordById(id);
        String detailFilePath = reqRecord.getDetailFilePath();
        if (StringUtils.isNotBlank(detailFilePath)) {
            BufferedReader reader = null;
            try {
                reader = new BufferedReader(new FileReader(detailFilePath));
                String readLine = reader.readLine();
                JSONObject parseObject = JSONObject.parseObject(readLine);
                TradeReqRecordDetail detail = new TradeReqRecordDetail();
                detail.setRequestMsg(parseObject.getString("requestMsg"));
                detail.setResponseMsg(parseObject.getString("responseMsg"));
                reqRecord.setRecordDetail(detail);
            } catch (FileNotFoundException e) {
                log.error(e.getMessage(), e);
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            } finally {
                if (null != reader) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        log.error(e.getMessage(), e);
                    }
                }
            }
        }
        return AjaxResult.success(reqRecord);
    }

}
