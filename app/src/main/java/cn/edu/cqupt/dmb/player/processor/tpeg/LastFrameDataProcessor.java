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
import cn.edu.cqupt.dmb.player.listener.CarouselListener;

/**
 * @Author : Gouzhong
 * @Blog : www.gouzhong1223.com
 * @Description : TPEG 尾帧数据处理器
 * @Date : create by QingSong in 2022-05-21 13:26
 * @Email : qingsong.qs@alibaba-inc.com
 * @Since : JDK 1.8
 * @PackageName : cn.edu.cqupt.dmb.player.processor.tpeg
 * @ProjectName : DMB Player For Android
 * @Version : 1.0.0
 */
public class LastFrameDataProcessor implements TpegDataProcessor {
    /* file size should not be greater than 2M */
    private static final int FILE_BUFFER_SIZE = 1024 * 1024 * 10;
    private static final String TAG = "LastFrameDataProcessor";

    @Override
    public void processData(TpegDecoder tpegDecoder, byte[] tpegData, byte[] fileBuffer, int[] tpegInfo, byte[] alternativeBytes) {
        Log.i(TAG, "processData: 接收到" + tpegDecoder.getFileName() + "的尾帧...");
        if (tpegDecoder.isReceiveFirstFrame() && tpegDecoder.getTotal() + tpegInfo[1] < FILE_BUFFER_SIZE) {
            System.arraycopy(tpegData, 0, fileBuffer, tpegDecoder.getTotal(), tpegInfo[1]);
            tpegDecoder.setTotal(tpegDecoder.getTotal() + tpegInfo[1]);

            System.arraycopy(tpegData, 0, alternativeBytes, tpegDecoder.getAlternativeTotal(), tpegInfo[1]);
            tpegDecoder.setAlternativeTotal(tpegDecoder.getAlternativeTotal() + tpegInfo[1]);

            if (tpegDecoder.getDmbListener() != null) {
                CarouselListener dmbListener = (CarouselListener) tpegDecoder.getDmbListener();
                dmbListener.onSuccess(tpegDecoder.getFileName(), fileBuffer, tpegDecoder.getTotal(), alternativeBytes);
            }

            tpegDecoder.setAlternativeTotal(0);
            tpegDecoder.setTotal(0);
            tpegDecoder.setReceiveFirstFrame(false);
            tpegDecoder.setFileName(null);
        }
    }
}
