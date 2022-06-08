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

package cn.edu.cqupt.dmb.player.common;

/**
 * @Author : Gouzhong
 * @Blog : www.gouzhong1223.com
 * @Description :
 * @Date : create by QingSong in 2022-05-14 15:50
 * @Email : gouzhong1223@gmail.com
 * @Since : JDK 1.8
 * @PackageName : cn.edu.cqupt.dmb.player.common
 * @ProjectName : DMB Player For Android
 * @Version : 1.0.0
 */
public enum CustomSettingByKey {
    DEFAULT_SENSE("default_sense"),
    DEFAULT_CAROUSEL_NUM("default_carousel_num"),
    SHOW_DEBUG_LOG("show_debug_log"),
    OPEN_SIGNAL("open_signal"),
    AUDIO_OUTPUT_MODE("audio_output_mode");

    private final String key;

    CustomSettingByKey(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }
}
