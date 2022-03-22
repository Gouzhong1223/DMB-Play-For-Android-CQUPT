package cn.edu.cqupt.dmb.player.processor.tpeg;

import android.util.Log;

import cn.edu.cqupt.dmb.player.task.DecodeTpegTask;
import cn.edu.cqupt.dmb.player.utils.DmbUtil;

/**
 * @Author : Gouzhong
 * @Blog : www.gouzhong1223.com
 * @Description : TPEG 头帧数据处理器
 * @Date : create by QingSong in 2022-03-22 21:58
 * @Email : qingsong.qs@alibaba-inc.com
 * @Since : JDK 1.8
 * @PackageName : cn.edu.cqupt.dmb.player.processor.tpeg
 * @ProjectName : DMB Player For Android
 * @Version : 1.0.0
 */
public class FirstTpegFrameProcessor implements TpegDataProcessing {

    private static final String TAG = "FirstTpegFrameProcessor";

    @Override
    public void processData(byte[] tpegBuffer, byte[] tpegData, int[] tpegInfo) {
        Log.e(TAG, "first frame");
        DecodeTpegTask.setIsReceiveFirstFrame(true);
        System.arraycopy(tpegData, 0, DecodeTpegTask.getFileBuffer(), 0, tpegInfo[1]);
        DecodeTpegTask.setTotal(tpegInfo[1] - 35);
        try {
            DecodeTpegTask.setFileName(new String(tpegData, 0, 35, DmbUtil.CHARACTER_SET));
            DecodeTpegTask.setFileName(DecodeTpegTask.getFileName().substring(0, DecodeTpegTask.getFileName().indexOf(0x00)));
            if (DecodeTpegTask.getFileName().equals("")) {
                DecodeTpegTask.setFileName("building" + tpegData[20] + ".jpg");
                Log.e(TAG, "fileName is empty,set fileName " + DecodeTpegTask.getFileName());
            }
            Log.e(TAG, DecodeTpegTask.getTotal() + " " + DecodeTpegTask.getFileName());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
