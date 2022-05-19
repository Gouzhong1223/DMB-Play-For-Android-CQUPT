package cn.edu.cqupt.dmb.player.domain;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * @Author : Gouzhong
 * @Blog : www.gouzhong1223.com
 * @Description : 这个是预设场景的数据库实体类
 * @Date : create by QingSong in 2022-05-13 13:21
 * @Email : gouzhong1223@gmail.com
 * @Since : JDK 1.8
 * @PackageName : cn.edu.cqupt.dmb.player.domain
 * @ProjectName : DMB Player For Android
 * @Version : 1.0.0
 */
@Entity(tableName = "scene_info")
public class SceneInfo {
    /**
     * 数据库主键
     */
    @PrimaryKey
    @ColumnInfo(name = "id")
    private Integer id;
    /**
     * 场景名字
     */
    @ColumnInfo(name = "sceneName")
    private String sceneName;
    /**
     * 设备 ID
     */
    @ColumnInfo(name = "sceneId")
    private Integer deviceId;
    /**
     * 频点
     */
    @ColumnInfo(name = "frequency")
    private Integer frequency;
    /**
     * 场景播放类型
     */
    @ColumnInfo(name = "sceneType")
    private Integer sceneType;
    /**
     * 教学楼
     */
    @ColumnInfo(name = "building")
    private Integer building;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getSceneName() {
        return sceneName;
    }

    public void setSceneName(String sceneName) {
        this.sceneName = sceneName;
    }

    public Integer getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(Integer deviceId) {
        this.deviceId = deviceId;
    }

    public Integer getFrequency() {
        return frequency;
    }

    public void setFrequency(Integer frequency) {
        this.frequency = frequency;
    }

    public Integer getSceneType() {
        return sceneType;
    }

    public void setSceneType(Integer sceneType) {
        this.sceneType = sceneType;
    }

    public Integer getBuilding() {
        return building;
    }

    public void setBuilding(Integer building) {
        this.building = building;
    }

    @NonNull
    @Override
    public String toString() {
        return "预设名称:" + sceneName + '\n' + "终端ID:" + deviceId + '\n' + "工作频点:" + frequency + '\n' + "播放类型:" + sceneType + '\n' + "教学楼:" + building;
    }
}
