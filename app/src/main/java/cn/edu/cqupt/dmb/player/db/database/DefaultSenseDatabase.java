/*
 *
 *              Copyright 2022 By Gouzhong1223
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 */

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
@Deprecated
@Database(entities = {DefaultSense.class}, version = 1, exportSchema = false)
public abstract class DefaultSenseDatabase extends RoomDatabase {
    /**
     * 获取 DefaultSenseMapper
     *
     * @return DefaultSenseMapper
     */
    public abstract DefaultSenseMapper getDefaultSenseMapper();
}
