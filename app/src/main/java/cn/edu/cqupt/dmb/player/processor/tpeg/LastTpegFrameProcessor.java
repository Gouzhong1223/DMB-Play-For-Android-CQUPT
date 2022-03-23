package cn.edu.cqupt.dmb.player.processor.tpeg;

import android.util.Log;

import cn.edu.cqupt.dmb.player.listener.DmbListener;
import cn.edu.cqupt.dmb.player.task.DecodeTpegTask;

/**
 * @Author : Gouzhong
 * @Blog : www.gouzhong1223.com
 * @Description : TPEG 结尾帧数据处理器
 * @Date : create by QingSong in 2022-03-22 21:59
 * @Email : qingsong.qs@alibaba-inc.com
 * @Since : JDK 1.8
 * @PackageName : cn.edu.cqupt.dmb.player.processor.tpeg
 * @ProjectName : DMB Player For Android
 * @Version : 1.0.0
 */
public class LastTpegFrameProcessor implements TpegDataProcessing {

    private static final int FILE_BUFFER_SIZE = 1024 * 1024 * 2;
    private static final String TAG = "TpegDecoder";

    @Override
    public void processData(byte[] tpegBuffer, byte[] tpegData, int[] tpegInfo) {
        Log.e(TAG, "last frame");
        if (DecodeTpegTask.isIsReceiveFirstFrame() && DecodeTpegTask.getTotal() + tpegInfo[1] < FILE_BUFFER_SIZE) {
            System.arraycopy(tpegData, 0, DecodeTpegTask.getFileBuffer(), DecodeTpegTask.getTotal(), tpegInfo[1]);
            DecodeTpegTask.setTotal(DecodeTpegTask.getTotal() + tpegInfo[1]);
            DmbListener dmbListener = DecodeTpegTask.getDmbListener();
            if (dmbListener != null) {
                dmbListener.onSuccess(DecodeTpegTask.getFileName(), DecodeTpegTask.getFileBuffer(), DecodeTpegTask.getTotal());
            }
            DecodeTpegTask.setIsReceiveFirstFrame(false);
            DecodeTpegTask.setFileName(null);
        }
    }
}
