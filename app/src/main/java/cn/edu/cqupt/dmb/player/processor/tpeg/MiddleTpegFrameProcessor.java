package cn.edu.cqupt.dmb.player.processor.tpeg;

/**
 * @Author : Gouzhong
 * @Blog : www.gouzhong1223.com
 * @Description : TPEG 中间帧数据处理器
 * @Date : create by QingSong in 2022-03-22 21:59
 * @Email : gouzhong1223@gmail.com
 * @Since : JDK 1.8
 * @PackageName : cn.edu.cqupt.dmb.player.processor.tpeg
 * @ProjectName : DMB Player For Android
 * @Version : 1.0.0
 */
public class MiddleTpegFrameProcessor implements TpegDataProcessing {

    private static final int FILE_BUFFER_SIZE = 1024 * 1024 * 2;

    int total = 0;
    byte[] fileBuffer = new byte[FILE_BUFFER_SIZE];


    @Override
    public void processData(byte[] tpegBuffer, byte[] tpegData, int[] tpegInfo) {
        if (total + tpegInfo[1] >= FILE_BUFFER_SIZE) {
            total = 0;
        } else {
            System.arraycopy(tpegData, 0, fileBuffer, total, tpegInfo[1]);
            total += tpegInfo[1];
        }
    }
}
