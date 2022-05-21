package cn.edu.cqupt.dmb.player.processor.tpeg;

import cn.edu.cqupt.dmb.player.decoder.TpegDecoder;

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

    @Override
    public void processData(TpegDecoder tpegDecoder, byte[] tpegData, byte[] fileBuffer, int[] tpegInfo) {
        if (tpegDecoder.isReceiveFirstFrame() && tpegDecoder.getTotal() + tpegInfo[1] < FILE_BUFFER_SIZE) {
            System.arraycopy(tpegData, 0, fileBuffer, tpegDecoder.getTotal(), tpegInfo[1]);
            tpegDecoder.setTotal(tpegDecoder.getTotal() + tpegInfo[1]);
            if (tpegDecoder.getDmbListener() != null) {
                tpegDecoder.getDmbListener().onSuccess(tpegDecoder.getFileName(), fileBuffer, tpegDecoder.getTotal());
            }
            tpegDecoder.setReceiveFirstFrame(false);
            tpegDecoder.setFileName(null);
        }
    }
}
