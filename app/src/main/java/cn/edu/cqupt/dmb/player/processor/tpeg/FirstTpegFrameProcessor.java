package cn.edu.cqupt.dmb.player.processor.tpeg;

import android.util.Log;

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

    private static final int FILE_BUFFER_SIZE = 1024 * 1024 * 2; /* file size should not be greater than 2M */

    int total = 0;
    byte[] fileBuffer = new byte[FILE_BUFFER_SIZE];
    boolean isReceiveFirstFrame = false;
    String fileName = null;

    @Override
    public void processData(byte[] tpegBuffer, byte[] tpegData, int[] tpegInfo) {
        Log.e(TAG, "first frame");
        isReceiveFirstFrame = true;
        System.arraycopy(tpegData, 0, fileBuffer, 0, tpegInfo[1]);
        total = tpegInfo[1] - 35;
        try {
            fileName = new String(tpegData, 0, 35, DmbUtil.CHARACTER_SET);
            fileName = fileName.substring(0, fileName.indexOf(0x00));
            if (fileName.equals("")) {
                fileName = "building" + tpegData[20] + ".jpg";
                Log.e(TAG, "fileName is empty,set fileName " + fileName);
            }
            Log.e(TAG, total + " " + fileName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
