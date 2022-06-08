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
 * @Description : 这个是 Dongle 类型的枚举
 * @Date : create by QingSong in 2022-05-04 21:41
 * @Email : qingsong.qs@alibaba-inc.com
 * @Since : JDK 1.8
 * @PackageName : cn.edu.cqupt.dmb.player.common
 * @ProjectName : DMB Player For Android
 * @Version : 1.0.0
 */
public enum DongleType {
    /**
     * STM32 类型 Dongle<br/>
     * 单次接收数据是 64 字节<br/>
     * 单次发送数据书 48 字节
     */
    STM32,
    /**
     * NUC 类型 Dongle<br/>
     * 单次接收数据是 776 字节<br/>
     * 单次发送数据书 48 字节
     */
    NUC,
    /**
     * AT 类型 Dongle<br/>
     * 单次接收数据是 776 字节<br/>
     * 单次发送数据书 48 字节
     */
    AT,
}
