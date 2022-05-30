package cn.edu.cqupt.dmb.player.processor.tpeg;

import cn.edu.cqupt.dmb.player.decoder.TpegDecoder;

/**
 * @Author : Gouzhong
 * @Blog : www.gouzhong1223.com
 * @Description : 未知的 TPEG 数据类型
 * @Date : create by QingSong in 2022-05-21 13:31
 * @Email : qingsong.qs@alibaba-inc.com
 * @Since : JDK 1.8
 * @PackageName : cn.edu.cqupt.dmb.player.processor.tpeg
 * @ProjectName : DMB Player For Android
 * @Version : 1.0.0
 */
public class DefaultTpegDataProcessor implements TpegDataProcessor {
    @Override
    public void processData(TpegDecoder tpegDecoder, byte[] tpegData, byte[] fileBuffer, int[] tpegInfo, byte[] alternativeBytes) {

    }
}
