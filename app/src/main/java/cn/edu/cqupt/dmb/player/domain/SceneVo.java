package cn.edu.cqupt.dmb.player.domain;

import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;

/**
 * @Author : Gouzhong
 * @Blog : www.gouzhong1223.com
 * @Description : 这个是主页上显示一个场景的信息
 * @Date : create by QingSong in 2022-05-15 21:16
 * @Email : gouzhong1223@gmail.com
 * @Since : JDK 1.8
 * @PackageName : cn.edu.cqupt.dmb.player.domain
 * @ProjectName : DMB Player For Android
 * @Version : 1.0.0
 */
public class SceneVo {
    /**
     * 场景ID
     */
    private Integer sceneId;
    /**
     * 场景名字
     */
    private String SceneName;
    /**
     * 场景的背景图
     */
    private Drawable background;
    /**
     * 场景的播放类型
     */
    private Integer sceneType;
    /**
     * 教学楼课表，仅当播放类型为教学楼课表时
     */
    private Integer buildingId;

    public SceneVo(Integer sceneId, String sceneName, Drawable background, Integer sceneType, Integer buildingId) {
        this.sceneId = sceneId;
        SceneName = sceneName;
        this.background = background;
        this.sceneType = sceneType;
        this.buildingId = buildingId;
    }

    public SceneVo() {
    }

    public Integer getSceneId() {
        return sceneId;
    }

    public void setSceneId(Integer sceneId) {
        this.sceneId = sceneId;
    }

    public String getSceneName() {
        return SceneName;
    }

    public void setSceneName(String sceneName) {
        SceneName = sceneName;
    }

    public Drawable getBackground() {
        return background;
    }

    public void setBackground(Drawable background) {
        this.background = background;
    }

    public Integer getSceneType() {
        return sceneType;
    }

    public void setSceneType(Integer sceneType) {
        this.sceneType = sceneType;
    }

    public Integer getBuildingId() {
        return buildingId;
    }

    public void setBuildingId(Integer buildingId) {
        this.buildingId = buildingId;
    }

    @NonNull
    @Override
    public String toString() {
        return "SceneVo{" + "sceneId=" + sceneId + ", SceneName='" + SceneName + '\'' + ", background=" + background + ", sceneType=" + sceneType + ", buildingId=" + buildingId + '}';
    }
}
