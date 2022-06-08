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

import cn.edu.cqupt.dmb.player.decoder.TpegDecoder;

/**
 * @Author : Gouzhong
 * @Blog : www.gouzhong1223.com
 * @Description : TPEG 数据帧的数据处理器接口
 * @Date : create by QingSong in 2022-05-05 14:26
 * @Email : qingsong.qs@alibaba-inc.com
 * @Since : JDK 1.8
 * @PackageName : cn.edu.cqupt.dmb.player.processor.tpeg
 * @ProjectName : DMB Player For Android
 * @Version : 1.0.0
 */
public interface TpegDataProcessor {
    /**
     * TPEG已解码数据包的原始数据处理器
     *
     * @param tpegDecoder TPEG 解码器
     * @param tpegData    TPEG 未解码的数据
     * @param fileBuffer  装载已解码的数据
     * @param tpegInfo    TPEG 数据信息
     */
    void processData(TpegDecoder tpegDecoder, byte[] tpegData, byte[] fileBuffer, int[] tpegInfo, byte[] alternativeBytes);
}
