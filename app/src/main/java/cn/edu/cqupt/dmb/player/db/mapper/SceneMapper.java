package cn.edu.cqupt.dmb.player.db.mapper;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;

import java.util.List;

import cn.edu.cqupt.dmb.player.domain.SceneInfo;

/**
 * @Author : Gouzhong
 * @Blog : www.gouzhong1223.com
 * @Description : Scene 的 Mapper 接口
 * @Date : create by QingSong in 2022-05-13 13:20
 * @Email : gouzhong1223@gmail.com
 * @Since : JDK 1.8
 * @PackageName : cn.edu.cqupt.dmb.player.mapper
 * @ProjectName : DMB Player For Android
 * @Version : 1.0.0
 */
@Dao
public interface SceneMapper {
    /**
     * 插入预设场景
     *
     * @param sceneInfo 预设场景信息
     */
    @Insert
    @Transaction
    void insertScene(SceneInfo sceneInfo);

    /**
     * 根据 ID 查询预设场景信息
     *
     * @param id 主键
     * @return SceneInfo
     */
    @Query(value = "select * from scene_info where id = :id")
    SceneInfo selectSceneById(Integer id);

    /**
     * 删除预设场景信息
     *
     * @param sceneInfo sceneInfo
     * @return 删除数量
     */
    @Delete
    @Transaction
    int deleteScene(SceneInfo sceneInfo);

    /**
     * 根据预设名称删除预设场景信息
     *
     * @param sceneName 预设名字
     */
    @Query(value = "delete from scene_info where sceneName = :sceneName")
    @Transaction
    void deleteSceneBySceneName(String sceneName);

    /**
     * 根据预设 设备ID 删除预设场景
     *
     * @param sceneId 预设设备 ID
     * @return
     */
    @Query(value = "delete from scene_info where id  = :sceneId")
    @Transaction
    int deleteSceneById(Integer sceneId);

    /**
     * 根绝预设名字查询预设
     *
     * @param sceneName 预设名字
     * @return SceneInfo
     */
    @Query(value = "select * from scene_info where sceneName = :sceneName limit 1")
    SceneInfo selectSceneByScreenName(String sceneName);

    /**
     * 根绝预设设备 ID 和频点查询预设
     *
     * @param sceneId   设备 ID
     * @param frequency 频点
     * @return SceneInfo
     */
    @Query(value = "select * from scene_info where sceneId = :sceneId and frequency = :frequency limit 1")
    SceneInfo selectSceneBySceneIdAndFrequency(Integer sceneId, Integer frequency);

    /**
     * 查询所有的预设
     *
     * @return List<SceneInfo>
     */
    @Query(value = "select * from scene_info")
    List<SceneInfo> selectAllScenes();

    /**
     * 查询所有的预设名字
     *
     * @return List<String>
     */
    @Query(value = "select sceneName from scene_info")
    List<String> selectAllSceneNames();
}
