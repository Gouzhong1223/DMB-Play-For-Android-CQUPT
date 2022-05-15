package cn.edu.cqupt.dmb.player.db.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import cn.edu.cqupt.dmb.player.db.mapper.DefaultSenseMapper;
import cn.edu.cqupt.dmb.player.domain.DefaultSense;

/**
 * @Author : Gouzhong
 * @Blog : www.gouzhong1223.com
 * @Description :
 * @Date : create by QingSong in 2022-05-14 15:32
 * @Email : gouzhong1223@gmail.com
 * @Since : JDK 1.8
 * @PackageName : cn.edu.cqupt.dmb.player.db.database
 * @ProjectName : DMB Player For Android
 * @Version : 1.0.0
 */
@Database(entities = {DefaultSense.class}, version = 1, exportSchema = false)
public abstract class DefaultSenseDatabase extends RoomDatabase {
    /**
     * 获取 DefaultSenseMapper
     *
     * @return DefaultSenseMapper
     */
    public abstract DefaultSenseMapper getDefaultSenseMapper();
}
