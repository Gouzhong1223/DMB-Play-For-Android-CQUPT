package cn.edu.cqupt.dmb.player.processor.tpeg;

import android.util.Log;

import java.io.BufferedInputStream;

import cn.edu.cqupt.dmb.player.listener.DmbListener;

/**
 * @Author : Gouzhong
 * @Blog : www.gouzhong1223.com
 * @Description : TPEG 结尾帧数据处理器
 * @Date : create by QingSong in 2022-03-22 21:59
 * @Email : gouzhong1223@gmail.com
 * @Since : JDK 1.8
 * @PackageName : cn.edu.cqupt.dmb.player.processor.tpeg
 * @ProjectName : DMB Player For Android
 * @Version : 1.0.0
 */
public class LastTpegFrameProcessor implements TpegDataProcessing {

    private static final int FILE_BUFFER_SIZE = 1024 * 1024 * 2;
    private static final String TAG = "TpegDecoder";

    private final BufferedInputStream inputStream = null;
    private final DmbListener listener = null;

    int total = 0;
    byte[] fileBuffer = new byte[FILE_BUFFER_SIZE];
    boolean isReceiveFirstFrame = false;
    String fileName = null;


    @Override
    public void processData(byte[] tpegBuffer, byte[] tpegData, int[] tpegInfo) {
        Log.e(TAG, "last frame");
        if (isReceiveFirstFrame && total + tpegInfo[1] < FILE_BUFFER_SIZE) {
            System.arraycopy(tpegData, 0, fileBuffer, total, tpegInfo[1]);
            total += tpegInfo[1];
            if (listener != null) {
                listener.onSuccess(fileName, fileBuffer, total);
            }
            isReceiveFirstFrame = false;
            fileName = null;
        }
    }
}
