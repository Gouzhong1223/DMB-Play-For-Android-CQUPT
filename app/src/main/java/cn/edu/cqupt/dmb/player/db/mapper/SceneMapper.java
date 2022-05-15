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

    @Insert
    @Transaction
    void insertScene(SceneInfo sceneInfo);

    @Delete
    @Transaction
    int deleteScene(SceneInfo sceneInfo);

    @Query(value = "delete from scene_info where sceneName = :sceneName")
    @Transaction
    void deleteSceneBySceneName(String sceneName);

    @Query(value = "delete from scene_info where id  = :sceneId")
    @Transaction
    int deleteSceneById(Integer sceneId);

    @Query(value = "select * from scene_info where sceneName = :sceneName limit 1")
    SceneInfo selectSceneByScreenName(String sceneName);

    @Query(value = "select * from scene_info where sceneId = :sceneId and frequency = :frequency limit 1")
    SceneInfo selectSceneBySceneIdAndFrequency(Integer sceneId, Integer frequency);

    @Query(value = "select * from scene_info")
    List<SceneInfo> selectAllScenes();

    @Query(value = "select sceneName from scene_info")
    List<String> selectAllSceneNames();
}
