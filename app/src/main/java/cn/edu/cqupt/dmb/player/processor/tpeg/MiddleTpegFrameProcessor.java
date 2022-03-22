package cn.edu.cqupt.dmb.player.processor.tpeg;

import cn.edu.cqupt.dmb.player.task.DecodeTpegTask;

/**
 * @Author : Gouzhong
 * @Blog : www.gouzhong1223.com
 * @Description : TPEG 中间帧数据处理器
 * @Date : create by QingSong in 2022-03-22 21:59
 * @Email : qingsong.qs@alibaba-inc.com
 * @Since : JDK 1.8
 * @PackageName : cn.edu.cqupt.dmb.player.processor.tpeg
 * @ProjectName : DMB Player For Android
 * @Version : 1.0.0
 */
public class MiddleTpegFrameProcessor implements TpegDataProcessing {

    private static final int FILE_BUFFER_SIZE = 1024 * 1024 * 2;

    @Override
    public void processData(byte[] tpegBuffer, byte[] tpegData, int[] tpegInfo) {

        if (DecodeTpegTask.getTotal() + tpegInfo[1] >= FILE_BUFFER_SIZE) {
            DecodeTpegTask.setTotal(0);
        } else {
            System.arraycopy(tpegData, 0, DecodeTpegTask.getFileBuffer(), DecodeTpegTask.getTotal(), tpegInfo[1]);
            DecodeTpegTask.setTotal(DecodeTpegTask.getTotal() + tpegInfo[1]);
        }
    }
}
