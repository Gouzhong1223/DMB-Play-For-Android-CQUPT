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

import java.io.IOException;
import java.io.PipedOutputStream;

import cn.edu.cqupt.dmb.player.common.DmbPlayerConstant;
import cn.edu.cqupt.dmb.player.common.DongleType;
import cn.edu.cqupt.dmb.player.utils.DataReadWriteUtil;

/**
 * @Author : Gouzhong
 * @Blog : www.gouzhong1223.com
 * @Description : DMB 类型的数据处理器
 * @Date : create by QingSong in 2022-03-20 23:41
 * @Email : qingsong.qs@alibaba-inc.com
 * @Since : JDK 1.8
 * @PackageName : cn.edu.cqupt.dmb.player.task
 * @ProjectName : DMB Player For Android
 * @Version : 1.0.0
 */
public class DmbDataProcessor implements DataProcessing {

    private static final String TAG = "DmbDataProcessor";


    /**
     * USB 数据输出流
     */
    private volatile PipedOutputStream pipedOutputStream;


    @Override
    public void processData(byte[] usbData, DongleType dongleType, PipedOutputStream pipedOutputStream) {
        this.pipedOutputStream = pipedOutputStream;
        this.processData(usbData, dongleType);
    }

    @Override
    public void processData(byte[] usbData, DongleType dongleType) {
        int dataLength;
        if (dongleType == DongleType.STM32) {
            dataLength = (((int) usbData[7]) & 0x0FF);
        } else {
            dataLength = (((int) usbData[6] & 0x0FF) << 8) | (((int) usbData[7]) & 0x0FF);
        }
        try {
            if (DataReadWriteUtil.inMainActivity) {
                // 如果现在没有活跃的使用场景,就直接抛弃当前接收到的数据并返回
                return;
            }
            pipedOutputStream.write(usbData, DmbPlayerConstant.DEFAULT_DATA_READ_OFFSET.getDmbConstantValue(), dataLength);
            if (!DataReadWriteUtil.initFlag) {
                DataReadWriteUtil.initFlag = true;
            }
            // 写完 flush 一下
            pipedOutputStream.flush();
        } catch (IOException e) {
            Log.e(TAG, "处理 DMB 数据出错啦!---" + e);
            e.printStackTrace();
        }
    }
}
