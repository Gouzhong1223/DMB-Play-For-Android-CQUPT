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

/**
 * @Author : Gouzhong
 * @Blog : www.gouzhong1223.com
 * @Description : 增强版的 DMB 数据解码器的监听器接口
 * @Date : create by QingSong in 2022-05-30 14:11
 * @Email : qingsong.qs@alibaba-inc.com
 * @Since : JDK 1.8
 * @PackageName : cn.edu.cqupt.dmb.player.listener
 * @ProjectName : DMB Player For Android
 * @Version : 1.0.0
 */
public interface CarouselListener extends DmbListener {
    /**
     * 增强的 DMB 解码监听器回调方法
     *
     * @param fileName         文件名
     * @param bytes            已经解码的 TPEG 数组
     * @param length           解码的数据长度
     * @param alternativeBytes 备用的已解码的 TPEG 数组
     */
    void onSuccess(String fileName, byte[] bytes, int length, byte[] alternativeBytes);
}
