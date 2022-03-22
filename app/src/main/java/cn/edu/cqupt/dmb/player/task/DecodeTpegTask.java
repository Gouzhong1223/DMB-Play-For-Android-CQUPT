package cn.edu.cqupt.dmb.player.task;

import android.util.Log;

import java.util.Arrays;

import cn.edu.cqupt.dmb.player.jni.NativeMethod;
import cn.edu.cqupt.dmb.player.processor.tpeg.TpegDataProcessing;
import cn.edu.cqupt.dmb.player.processor.tpeg.TpegDataProcessingFactory;
import cn.edu.cqupt.dmb.player.utils.DataReadWriteUtil;

/**
 * @Author : Gouzhong
 * @Blog : www.gouzhong1223.com
 * @Description : 这是解码 TPEG 的任务
 * @Date : create by QingSong in 2022-03-22 21:33
 * @Email : qingsong.qs@alibaba-inc.com
 * @Since : JDK 1.8
 * @PackageName : cn.edu.cqupt.dmb.player.task
 * @ProjectName : DMB Player For Android
 * @Version : 1.0.0
 */
public class DecodeTpegTask implements Runnable {

    private static final String TAG = "DecodeTpegTask";

    private static final int TPEG_SIZE = 112;
    private static final int DATA_SIZE = 80;
    private static final int TPEG_INFO_SIZE = 3;


    @Override
    public void run() {
        byte[] tpegBuffer = new byte[TPEG_SIZE];
        byte[] tpegData = new byte[DATA_SIZE];
        int[] tpegInfo = new int[TPEG_INFO_SIZE];
        Log.e(TAG, "tpeg decoder start");
        NativeMethod.tpegInit();
        tpegBuffer[0] = tpegBuffer[1] = tpegBuffer[2] = (byte) 0;
        if (!DataReadWriteUtil.readTpegFrame(tpegBuffer)) {
            Log.e(TAG, "读取 TPEG 数据失败啦!");
            // 如果读取数据失败了,就直接返回,等待下一次读取
            return;
        }
        Arrays.fill(tpegData, (byte) 0);
        Arrays.fill(tpegInfo, 0);
        NativeMethod.decodeTpegFrame(tpegBuffer, tpegData, tpegInfo);
        TpegDataProcessing tpegDataProcessing = TpegDataProcessingFactory.getDataProcessor(tpegInfo[0]);
        tpegDataProcessing.processData(tpegBuffer, tpegData, tpegInfo);
    }
}
