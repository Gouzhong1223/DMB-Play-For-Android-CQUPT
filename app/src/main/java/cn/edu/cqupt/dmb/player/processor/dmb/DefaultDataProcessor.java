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

import android.util.Log;

import java.io.PipedOutputStream;

import cn.edu.cqupt.dmb.player.common.DongleType;

/**
 * @Author : Gouzhong
 * @Blog : www.gouzhong1223.com
 * @Description : 默认的 DMB 数据处理器
 * @Date : create by QingSong in 2022-03-20 23:55
 * @Email : qingsong.qs@alibaba-inc.com
 * @Since : JDK 1.8
 * @PackageName : cn.edu.cqupt.dmb.player.task
 * @ProjectName : DMB Player For Android
 * @Version : 1.0.0
 */
public class DefaultDataProcessor implements DataProcessing {

    private static final String TAG = "DefaultDataProcessor";

    @Override
    public void processData(byte[] usbData, DongleType dongleType, PipedOutputStream pipedOutputStream) {
        this.processData(usbData, dongleType);
    }

    @Override
    public void processData(byte[] usbData, DongleType dongleType) {
        Log.e(TAG, "接收到类型未知的数据,无法处理!");
    }
}
