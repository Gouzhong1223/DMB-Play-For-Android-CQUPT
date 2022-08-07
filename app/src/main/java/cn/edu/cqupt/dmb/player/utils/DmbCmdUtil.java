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

/**
 * @Author : Gouzhong
 * @Blog : www.gouzhong1223.com
 * @Description : 解析 CMD 的工具类
 * @Date : create by QingSong in 2022-06-14 16:00
 * @Email : qingsong.qs@alibaba-inc.com
 * @Since : JDK 1.8
 * @PackageName : cn.edu.cqupt.dmb.player.utils
 * @ProjectName : DMB Player For Android
 * @Version : 1.0.0
 */
public class DmbCmdUtil {

    /**
     * 获取 cmd 字符串
     *
     * @param tpegData  tpeg 数据
     * @param cmdLength tpeg 长度
     * @return cmd 字符串
     */
    public static String getCmdString(byte[] tpegData, int cmdLength) {
        byte[] cmdBytes = new byte[cmdLength];
        System.arraycopy(tpegData, 0, cmdBytes, 0, cmdBytes.length);
        return new String(cmdBytes);
    }

    /**
     * 解析轮播图停留时间 CMD 字符串
     *
     * @param cmdString  cmd 字符串
     * @param cmdHashMap cmdMap 缓存
     */
    public static void parseImageLoopTimeCmdString(String cmdString, ConcurrentHashMap<String, String> cmdHashMap) {
        String[] cmdKeyAndValues = cmdString.split(",");
        for (String cmdKeyAndValue : cmdKeyAndValues) {
            String[] cmdKeyAndValueArray = cmdKeyAndValue.split("=");
            if (cmdKeyAndValueArray.length == 2) {
                cmdHashMap.put(cmdKeyAndValueArray[0], cmdKeyAndValueArray[1]);
            }
        }
    }
}
