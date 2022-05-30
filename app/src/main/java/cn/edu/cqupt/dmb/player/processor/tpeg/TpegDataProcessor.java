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
