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

package cn.edu.cqupt.dmb.player.processor.tpeg;

import android.util.Log;

import cn.edu.cqupt.dmb.player.decoder.TpegDecoder;
import cn.edu.cqupt.dmb.player.utils.DmbUtil;

/**
 * @Author : Gouzhong
 * @Blog : www.gouzhong1223.com
 * @Description : TPEG 头帧数据处理器
 * @Date : create by QingSong in 2022-05-21 12:59
 * @Email : qingsong.qs@alibaba-inc.com
 * @Since : JDK 1.8
 * @PackageName : cn.edu.cqupt.dmb.player.processor.tpeg
 * @ProjectName : DMB Player For Android
 * @Version : 1.0.0
 */
public class FirstFrameDataProcessor implements TpegDataProcessor {
    private static final String TAG = "FirstFrameDataProcessor";

    @Override
    public void processData(TpegDecoder tpegDecoder, byte[] tpegData, byte[] fileBuffer, int[] tpegInfo, byte[] alternativeBytes) {
        Log.i(TAG, "现在接收到了头帧");
        tpegDecoder.setReceiveFirstFrame(true);
        System.arraycopy(tpegData, 0, fileBuffer, 0, tpegInfo[1]);
        tpegDecoder.setTotal(tpegInfo[1] - 35);
        System.arraycopy(tpegData, 0, alternativeBytes, 0, tpegInfo[1]);
        tpegDecoder.setAlternativeTotal(tpegInfo[1]);
        try {
            tpegDecoder.setFileName(new String(tpegData, 0, 35, DmbUtil.CHARACTER_SET));
            tpegDecoder.setFileName(tpegDecoder.getFileName().substring(0, tpegDecoder.getFileName().indexOf(0x00)));
            if (tpegDecoder.getFileName().equals("")) {
                if (tpegData[20] != 0) {
                    tpegDecoder.setFileName("教学楼-" + tpegData[20] + ".jpg");
                    Log.i(TAG, "processData: 接收到一张课表图片, 图片名称为: " + tpegDecoder.getFileName());
                } else {
                    tpegDecoder.setFileName("dab.jpg");
                    Log.i(TAG, "processData: 接收到一张没有文件名的图片, 图片重命名为: " + tpegDecoder.getFileName());
                }
            } else {
                Log.i(TAG, "processData: 接收到的文件名为：" + tpegDecoder.getFileName());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
