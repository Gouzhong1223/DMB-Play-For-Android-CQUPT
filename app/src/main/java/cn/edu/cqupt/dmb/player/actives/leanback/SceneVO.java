package cn.edu.cqupt.dmb.player.actives.leanback;

import androidx.annotation.NonNull;

import java.io.Serializable;

/**
 * @author qingsong
 */
public class SceneVO implements Serializable {
    static final long serialVersionUID = 727566175075960653L;
    /**
     * 主键
     */
    private Long id;
    /**
     * 预设场景类型
     */
    private Integer sceneType;
    /**
     * 频点
     */
    private Integer frequency;
    /**
     * 设备 ID
     */
    private Integer deviceId;
    /**
     * 教学楼
     */
    private Integer building;
    /**
     * 标题
     */
    private String title;
    /**
     * 描述
     */
    private String description;
    private String videoUrl;
    private String subTitle;
    private Integer backgroundDrawableId;
    private Integer cardDrawableId;

    public SceneVO() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getSceneType() {
        return sceneType;
    }

    public void setSceneType(Integer sceneType) {
        this.sceneType = sceneType;
    }

    public Integer getFrequency() {
        return frequency;
    }

    public void setFrequency(Integer frequency) {
        this.frequency = frequency;
    }

    public Integer getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(Integer deviceId) {
        this.deviceId = deviceId;
    }

    public Integer getBuilding() {
        return building;
    }

    public void setBuilding(Integer building) {
        this.building = building;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public String getSubTitle() {
        return subTitle;
    }

    public void setSubTitle(String subTitle) {
        this.subTitle = subTitle;
    }

    public Integer getBackgroundDrawableId() {
        return backgroundDrawableId;
    }

    public void setBackgroundDrawableId(Integer backgroundDrawableId) {
        this.backgroundDrawableId = backgroundDrawableId;
    }

    public Integer getCardDrawableId() {
        return cardDrawableId;
    }

    public void setCardDrawableId(Integer cardDrawableId) {
        this.cardDrawableId = cardDrawableId;
    }

    @NonNull
    @Override
    public String toString() {
        return "SceneVO{" +
                "id=" + id +
                ", sceneType=" + sceneType +
                ", frequency=" + frequency +
                ", deviceId=" + deviceId +
                ", building=" + building +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", videoUrl='" + videoUrl + '\'' +
                ", subTitle='" + subTitle + '\'' +
                ", backgroundDrawableId=" + backgroundDrawableId +
                ", cardDrawableId=" + cardDrawableId +
                '}';
    }
}
