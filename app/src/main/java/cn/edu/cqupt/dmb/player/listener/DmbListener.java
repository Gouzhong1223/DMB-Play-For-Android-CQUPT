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

package cn.edu.cqupt.dmb.player.listener;

import cn.edu.cqupt.dmb.player.utils.DataReadWriteUtil;
import cn.edu.cqupt.dmb.player.utils.DmbCmdUtil;

/**
 * @Author : Gouzhong
 * @Blog : www.gouzhong1223.com
 * @Description : 这个是 DMB 数据解码器的监听器接口
 * @Date : create by QingSong in 2022-03-22 21:22
 * @Email : qingsong.qs@alibaba-inc.com
 * @Since : JDK 1.8
 * @PackageName : cn.edu.cqupt.dmb.player.listener
 * @ProjectName : DMB Player For Android
 * @Version : 1.0.0
 */
public interface DmbListener {

    String BANNER_LOOP_TIME_DMBCMD_FILENAME = "bannerLoopTime.dmbcmd";

    /**
     * 这个是 DMB 数据解码成功之后的回调方法
     *
     * @param fileName 文件名称
     * @param bytes    文件数组
     * @param length   文件长度
     */
    void onSuccess(String fileName, byte[] bytes, int length);

    /**
     * 处理接收到的命令
     *
     * @param fileName 命令文件名
     * @param msg      命令字符串
     */
    default void onReceiveMessage(String fileName, String msg) {
        // 判断命令类型类型
        if (fileName.equals(BANNER_LOOP_TIME_DMBCMD_FILENAME)) {
            // 轮播图停留时间命令
            DmbCmdUtil.parseImageLoopTimeCmdString(msg, DataReadWriteUtil.imageLoopTimeMap);
        }
        // 后面其他的命令类型在这后面拓展,暂时我只写一个轮播图停留时间的命令解析
    }
}
