package cn.eyecool.noninductive.domain;

import java.io.Serializable;

import lombok.Data;

@Data
public class Bio1nSearchResult implements Serializable {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 1L;

    private String personCode;
    private String personName;
    private String imageId;
    private String featureId;
    private double score;

}
