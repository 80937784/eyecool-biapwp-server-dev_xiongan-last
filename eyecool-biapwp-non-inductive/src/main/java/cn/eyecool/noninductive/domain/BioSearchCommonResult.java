package cn.eyecool.noninductive.domain;

import lombok.Data;

@Data
public class BioSearchCommonResult {
    private String personId;
    private String result;
    private String matchTime;
    private double matchScore;
    private String deviceNo;
    private String tmplImageUrl;
    private String liveFaceDataUrl;
    private String liveFaceDataB64;
    private String tmplImageData64;
    private String deviceAddr;
}
