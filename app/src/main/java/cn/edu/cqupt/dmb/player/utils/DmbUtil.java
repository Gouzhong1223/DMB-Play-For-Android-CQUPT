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

package cn.edu.cqupt.dmb.player.utils;

import android.os.Environment;

import java.io.File;

/**
 * @Author : Gouzhong
 * @Blog : www.gouzhong1223.com
 * @Description : DMB 通用工具类
 * @Date : create by QingSong in 2022-03-17 22:45
 * @Email : qingsong.qs@alibaba-inc.com
 * @Since : JDK 1.8
 * @PackageName : cn.edu.cqupt.dmb.player.utils
 * @ProjectName : DMB Player For Android
 * @Version : 1.0.0
 */
public class DmbUtil {

    public static final String CACHE_DIRECTORY = Environment.getExternalStorageDirectory().getAbsolutePath() + "/cn.edu.cqupt.dmb.player/";
    public static final String CHARACTER_SET = "gb2312";

    /* init directory */
    static {
        File file = new File(CACHE_DIRECTORY);
        if (!file.exists() || !file.isDirectory()) {
            file.mkdirs();
        }
    }
}

