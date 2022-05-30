package cn.edu.cqupt.dmb.player.processor.tpeg;

import cn.edu.cqupt.dmb.player.decoder.TpegDecoder;

/**
 * @Author : Gouzhong
 * @Blog : www.gouzhong1223.com
 * @Description : TPEG 中间帧数据处理器
 * @Date : create by QingSong in 2022-05-21 13:24
 * @Email : qingsong.qs@alibaba-inc.com
 * @Since : JDK 1.8
 * @PackageName : cn.edu.cqupt.dmb.player.processor.tpeg
 * @ProjectName : DMB Player For Android
 * @Version : 1.0.0
 */
public class MiddleFrameDataProcessor implements TpegDataProcessor {

    /* file size should not be greater than 2M */
    private static final int FILE_BUFFER_SIZE = 1024 * 1024 * 10;

    @Override
    public void processData(TpegDecoder tpegDecoder, byte[] tpegData, byte[] fileBuffer, int[] tpegInfo, byte[] alternativeBytes) {
        if (tpegDecoder.getTotal() + tpegInfo[1] >= FILE_BUFFER_SIZE) {
            tpegDecoder.setTotal(0);
        } else {
            System.arraycopy(tpegData, 0, fileBuffer, tpegDecoder.getTotal(), tpegInfo[1]);
            tpegDecoder.setTotal(tpegDecoder.getTotal() + tpegInfo[1]);

            System.arraycopy(tpegData, 0, alternativeBytes, tpegDecoder.getAlternativeTotal(), tpegInfo[1]);
            tpegDecoder.setAlternativeTotal(tpegDecoder.getAlternativeTotal() + tpegInfo[1]);
        }
    }
}
