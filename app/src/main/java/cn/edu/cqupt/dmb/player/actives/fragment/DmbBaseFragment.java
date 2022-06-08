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

package cn.edu.cqupt.dmb.player.actives.fragment;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.room.Room;

import cn.edu.cqupt.dmb.player.db.database.CustomSettingDatabase;
import cn.edu.cqupt.dmb.player.db.database.SceneDatabase;
import cn.edu.cqupt.dmb.player.db.mapper.CustomSettingMapper;
import cn.edu.cqupt.dmb.player.db.mapper.SceneMapper;

/**
 * @Author : Gouzhong
 * @Blog : www.gouzhong1223.com
 * @Description : DMB Fragment 父类
 * @Date : create by QingSong in 2022-05-23 16:50
 * @Email : qingsong.qs@alibaba-inc.com
 * @Since : JDK 1.8
 * @PackageName : cn.edu.cqupt.dmb.player.actives.fragment
 * @ProjectName : DMB Player For Android
 * @Version : 1.0.0
 */
public abstract class DmbBaseFragment extends Fragment {

    /**
     * 父 Context
     */
    protected Context context;

    /**
     * 操作场景的Mapper
     */
    protected SceneMapper sceneMapper;

    /**
     * 操作默认设置的Mapper
     */
    protected CustomSettingMapper customSettingMapper;

    /**
     * 自定义设置的数据库
     */
    protected CustomSettingDatabase customSettingDatabase;

    /**
     * 预设场景的数据库
     */
    protected SceneDatabase sceneDatabase;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initDataBase();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    /**
     * 初始化数据库
     */
    private void initDataBase() {
        sceneDatabase = Room.databaseBuilder(context, SceneDatabase.class, "scene_database")
                .allowMainThreadQueries().build();
        //new a database
        sceneMapper = sceneDatabase.getSceneMapper();
        customSettingDatabase = Room.databaseBuilder(context, CustomSettingDatabase.class, "custom_setting_database")
                .allowMainThreadQueries().build();
        //new a database
        customSettingMapper = customSettingDatabase.getCustomSettingMapper();
    }

    @Override
    public void onDestroy() {
        if (customSettingDatabase != null) {
            customSettingDatabase.close();
        }
        if (sceneDatabase != null) {
            sceneDatabase.close();
        }
        super.onDestroy();
    }
}
