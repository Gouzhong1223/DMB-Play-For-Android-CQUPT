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

import java.util.concurrent.ConcurrentHashMap;

import cn.edu.cqupt.dmb.player.domain.SceneVO;

/**
 * @Author : Gouzhong
 * @Blog : www.gouzhong1223.com
 * @Description : 读取数据的工具类
 * @Date : create by QingSong in 2022-03-16 15:18
 * @Email : qingsong.qs@alibaba-inc.com
 * @Since : JDK 1.8
 * @PackageName : cn.edu.cqupt.dmb.player.utils
 * @ProjectName : DMB Player For Android
 * @Version : 1.0.0
 */
public class DataReadWriteUtil {

    /**
     * 是否已经进行了 USB 的第一次初始化
     */
    public static volatile boolean isFirstInitMainActivity = true;
    /**
     * USB 设备是否就绪
     */
    public volatile static boolean USB_READY = false;
    /**
     * 现在是否已经接收到了 DMB 类型的数据
     */
    public static volatile boolean initFlag = false;
    /**
     * 是否在主页
     */
    public static volatile boolean inMainActivity = true;
    /**
     * 被选中播放的场景
     */
    public static volatile SceneVO selectSceneVO = null;

    /**
     * 装载图片轮播时间的 Map 缓存
     */
    public static ConcurrentHashMap<String, String> imageLoopTimeMap = new ConcurrentHashMap<>();
}
