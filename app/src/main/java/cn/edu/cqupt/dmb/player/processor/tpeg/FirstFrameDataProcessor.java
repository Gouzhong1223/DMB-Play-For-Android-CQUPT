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
                tpegDecoder.setFileName("教学楼-" + tpegData[20] + ".jpg");
                Log.i(TAG, "没有从 TPEG 信息中解码出文件名,所以重命名为:" + tpegDecoder.getFileName());
            }
            Log.i(TAG, tpegDecoder.getTotal() + " " + tpegDecoder.getFileName());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
