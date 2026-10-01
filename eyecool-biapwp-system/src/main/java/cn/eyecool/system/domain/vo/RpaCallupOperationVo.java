package cn.eyecool.system.domain.vo;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;

@Data
public class RpaCallupOperationVo {
    private String userId;
    private String platName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date loginTime;

    @Data
    public static class RpaCallupOperationResponseVo {
        private String userId;
        private String result;
    }
}
