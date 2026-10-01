package cn.eyecool.basedata.event;

import org.springframework.context.ApplicationEvent;

/**
 * 人员信息改变事件
 * 
 * @author mawj
 * @date 2021/01/08
 */
public class PersonInfoChangeEvent extends ApplicationEvent {

    private static final long serialVersionUID = 8302384450488129664L;

    /** 事件类型 */
    private PersonChangeEventType personChangeEventType;
    /** 人员主键 */
    private String personId;
    /** 人员唯一标识 */
    private String uniqueId;
    /** 人脸是否变化 */
    private boolean faceChanged;
    /** 指纹是否变化 */
    private boolean fingerChanged;
    /** 虹膜是否变化 */
    private boolean irisChanged;
    /** 指静脉是否变化 */
    private boolean fveinChanged;
    /** 人脸虹膜多模态变化 */
    private boolean irisfaceChanged;
    /** 需要绑定关系的场景编码(多个用,分隔) */
    private String autoBindChannelCodes;
    /** 需要绑定关系的子场景编码集合(多个用,分隔) */
    private String autoBindSubCodes;
    /** 人员信息数据来源 */
    private String dataSource;
    /** 事件执行回调 */
    private EventCallback callback;

    public PersonInfoChangeEvent(Object source, PersonChangeEventType personChangeEventType, String personId,
        String uniqueId, EventCallback callback) {
        super(source);
        this.setPersonId(personId);
        this.setUniqueId(uniqueId);
        this.setCallback(callback);
    }

    public PersonInfoChangeEvent(Object source, PersonChangeEventType personChangeEventType, String personId,
        String uniqueId, boolean faceChanged, boolean fingerChanged, boolean irisChanged, boolean fveinChanged,
        boolean irisfaceChanged, EventCallback callback, String autoBindChannelCodes,
        String autoBindSubCodes, String dataSource) {
        this(source, personChangeEventType, personId, uniqueId, faceChanged, fingerChanged, irisChanged, fveinChanged,
            irisfaceChanged, callback);
        this.setAutoBindChannelCodes(autoBindChannelCodes);
        this.setAutoBindSubCodes(autoBindSubCodes);
        this.setDataSource(dataSource);
    }

    public PersonInfoChangeEvent(Object source, PersonChangeEventType personChangeEventType, String personId,
        String uniqueId, boolean faceChanged, boolean fingerChanged, boolean irisChanged, boolean fveinChanged,
        boolean irisfaceChanged, EventCallback callback) {
        super(source);
        this.setPersonChangeEventType(personChangeEventType);
        this.setPersonId(personId);
        this.setUniqueId(uniqueId);
        this.setFaceChanged(faceChanged);
        this.setFingerChanged(fingerChanged);
        this.setIrisChanged(irisChanged);
        this.setFveinChanged(fveinChanged);
        this.setIrisfaceChanged(irisfaceChanged);
        this.setCallback(callback);
    }

    public PersonChangeEventType getPersonChangeEventType() {
        return personChangeEventType;
    }

    public void setPersonChangeEventType(PersonChangeEventType personChangeEventType) {
        this.personChangeEventType = personChangeEventType;
    }

    public String getPersonId() {
        return personId;
    }

    public void setPersonId(String personId) {
        this.personId = personId;
    }

    public String getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    public boolean isFaceChanged() {
        return faceChanged;
    }

    public void setFaceChanged(boolean faceChanged) {
        this.faceChanged = faceChanged;
    }

    public boolean isFingerChanged() {
        return fingerChanged;
    }

    public void setFingerChanged(boolean fingerChanged) {
        this.fingerChanged = fingerChanged;
    }

    public boolean isIrisChanged() {
        return irisChanged;
    }

    public void setIrisChanged(boolean irisChanged) {
        this.irisChanged = irisChanged;
    }

    public boolean isFveinChanged() {
        return fveinChanged;
    }

    public void setFveinChanged(boolean fveinChanged) {
        this.fveinChanged = fveinChanged;
    }

    public boolean isIrisfaceChanged() {
        return irisfaceChanged;
    }

    public void setIrisfaceChanged(boolean irisfaceChanged) {
        this.irisfaceChanged = irisfaceChanged;
    }

    public EventCallback getCallback() {
        return callback;
    }

    public void setCallback(EventCallback callback) {
        this.callback = callback;
    }

    public String getAutoBindChannelCodes() {
        return autoBindChannelCodes;
    }

    public void setAutoBindChannelCodes(String autoBindChannelCodes) {
        this.autoBindChannelCodes = autoBindChannelCodes;
    }

    public String getAutoBindSubCodes() {
        return autoBindSubCodes;
    }

    public void setAutoBindSubCodes(String autoBindSubCodes) {
        this.autoBindSubCodes = autoBindSubCodes;
    }

    public String getDataSource() {
        return dataSource;
    }

    public void setDataSource(String dataSource) {
        this.dataSource = dataSource;
    }

}
