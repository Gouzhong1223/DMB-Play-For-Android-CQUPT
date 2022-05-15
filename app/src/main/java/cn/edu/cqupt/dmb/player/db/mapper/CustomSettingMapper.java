package cn.edu.cqupt.dmb.player.db.mapper;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import cn.edu.cqupt.dmb.player.domain.CustomSetting;

/**
 * @Author : Gouzhong
 * @Blog : www.gouzhong1223.com
 * @Description :
 * @Date : create by QingSong in 2022-05-14 15:46
 * @Email : gouzhong1223@gmail.com
 * @Since : JDK 1.8
 * @PackageName : cn.edu.cqupt.dmb.player.db.mapper
 * @ProjectName : DMB Player For Android
 * @Version : 1.0.0
 */
@Dao
public interface CustomSettingMapper {
    @Insert
    @Transaction
    void insertCustomSetting(CustomSetting customSetting);

    @Query(value = "delete from custom_setting where id = :id")
    @Transaction
    int deleteCustomSettingById(Integer id);

    @Update
    @Transaction
    int updateCustomSetting(CustomSetting customSetting);

    @Query(value = "select * from custom_setting where settingKey = :key limit 1")
    CustomSetting selectCustomSettingByKey(String key);
}
