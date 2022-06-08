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

package cn.edu.cqupt.dmb.player.processor.dmb;

import java.io.PipedOutputStream;

import cn.edu.cqupt.dmb.player.common.DongleType;

/**
 * @Author : Gouzhong
 * @Blog : www.gouzhong1223.com
 * @Description : 这是处理DMB数据的接口
 * @Date : create by QingSong in 2022-03-20 23:33
 * @Email : qingsong.qs@alibaba-inc.com
 * @Since : JDK 1.8
 * @PackageName : cn.edu.cqupt.dmb.player.task
 * @ProjectName : DMB Player For Android
 * @Version : 1.0.0
 */
public interface DataProcessing {

    /**
     * 处理 DMB 数据
     *
     * @param usbData    从 USB 中读取到的一段 DMB 数据
     * @param dongleType Dongle 类型
     */
    void processData(byte[] usbData, DongleType dongleType);

    /**
     * 处理 DMB 数据
     *
     * @param usbData           从 USB 中读取到的一段 DMB 数据
     * @param pipedOutputStream USB PIP 输出流
     * @param dongleType        Dongle 类型
     */
    default void processData(byte[] usbData, DongleType dongleType, PipedOutputStream pipedOutputStream) {
    }
}
