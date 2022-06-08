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

package cn.edu.cqupt.dmb.player.db.mapper;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import cn.edu.cqupt.dmb.player.domain.DefaultSense;

/**
 * @Author : Gouzhong
 * @Blog : www.gouzhong1223.com
 * @Description :
 * @Date : create by QingSong in 2022-05-14 15:31
 * @Email : gouzhong1223@gmail.com
 * @Since : JDK 1.8
 * @PackageName : cn.edu.cqupt.dmb.player.db.database
 * @ProjectName : DMB Player For Android
 * @Version : 1.0.0
 */
@Dao
public interface DefaultSenseMapper {

    @Insert
    @Transaction
    void insertDefaultSense(DefaultSense defaultSense);

    @Update
    @Transaction
    int updateDefaultSense(DefaultSense defaultSense);

    @Query(value = "delete from default_sense where id = :id")
    @Transaction
    int deleteDefaultSenseById(Integer id);

    @Query(value = "select * from default_sense where senseId = :senseId")
    DefaultSense selectDefaultSenseBySenseId(Integer senseId);

    @Query(value = "select * from default_sense where id = :id")
    DefaultSense selectDefaultSenseById(Integer id);
}
