package cn.edu.cqupt.dmb.player.domain;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * @Author : Gouzhong
 * @Blog : www.gouzhong1223.com
 * @Description :
 * @Date : create by QingSong in 2022-05-13 13:21
 * @Email : gouzhong1223@gmail.com
 * @Since : JDK 1.8
 * @PackageName : cn.edu.cqupt.dmb.player.domain
 * @ProjectName : DMB Player For Android
 * @Version : 1.0.0
 */
@Entity(tableName = "scene_info")
public class SceneInfo {
    @PrimaryKey
    @ColumnInfo(name = "id")
    private Integer id;
    @ColumnInfo(name = "sceneName")
    private String sceneName;
    @ColumnInfo(name = "sceneId")
    private Integer deviceId;
    @ColumnInfo(name = "frequency")
    private Integer frequency;
    @ColumnInfo(name = "sceneType")
    private Integer sceneType;
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
