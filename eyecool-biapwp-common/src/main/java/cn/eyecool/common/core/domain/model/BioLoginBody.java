package cn.eyecool.common.core.domain.model;

public class BioLoginBody {

    private String bioUsername;

    private String bioData;

    private String bioLoginType;

    private String uuid;

    
    public String getBioUsername() {
        return bioUsername;
    }

    public void setBioUsername(String bioUsername) {
        this.bioUsername = bioUsername;
    }

    public String getBioData() {
        return bioData;
    }

    public void setBioData(String bioData) {
        this.bioData = bioData;
    }

    public String getBioLoginType() {
        return bioLoginType;
    }

    public void setBioLoginType(String bioLoginType) {
        this.bioLoginType = bioLoginType;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
    
}
