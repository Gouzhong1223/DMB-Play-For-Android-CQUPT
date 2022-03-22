package cn.edu.cqupt.dmb.player.processor.tpeg;

/**
 * @Author : Gouzhong
 * @Blog : www.gouzhong1223.com
 * @Description : TPEG 数据处理接口
 * @Date : create by QingSong in 2022-03-22 21:56
 * @Email : qingsong.qs@alibaba-inc.com
 * @Since : JDK 1.8
 * @PackageName : cn.edu.cqupt.dmb.player.processor.tpeg
 * @ProjectName : DMB Player For Android
 * @Version : 1.0.0
 */
public interface TpegDataProcessing {
    /**
     * 处理 TPEG 数据
     *
     * @param tpegBuffer tpegBuffer
     * @param tpegData   tpegData
     * @param tpegInfo   tpegInfo
     */
    void processData(byte[] tpegBuffer, byte[] tpegData, int[] tpegInfo);
}
