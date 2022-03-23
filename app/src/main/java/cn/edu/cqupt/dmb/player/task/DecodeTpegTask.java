package cn.edu.cqupt.dmb.player.task;

import android.util.Log;

import java.util.Arrays;

import cn.edu.cqupt.dmb.player.actives.MainActivity;
import cn.edu.cqupt.dmb.player.jni.NativeMethod;
import cn.edu.cqupt.dmb.player.listener.DmbListener;
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

    private static final int FILE_BUFFER_SIZE = 1024 * 1024 * 2;

    private static DmbListener dmbListener;


    public DecodeTpegTask(DmbListener dmbListener) {
        DecodeTpegTask.dmbListener = dmbListener;
    }

    private static volatile int length = 0;
    private static volatile int total = 0;
    private static volatile byte[] fileBuffer = new byte[FILE_BUFFER_SIZE];
    private static volatile boolean isReceiveFirstFrame = false;
    private static volatile String fileName = null;
    private final byte[] tpegBuffer = new byte[TPEG_SIZE];
    private final byte[] tpegData = new byte[DATA_SIZE];
    private final int[] tpegInfo = new int[TPEG_INFO_SIZE];

    @Override
    public void run() {
        if (MainActivity.USB_READY) {
            Log.i(TAG, "开始处理 TPEG 数据");
            tpegBuffer[0] = tpegBuffer[1] = tpegBuffer[2] = (byte) 0;
            if (!DataReadWriteUtil.initFlag) {
                // 如果当前的initFlag还没有被设置成 true,说明还没有被写入过 TPEG 数据,那就直接返回等下一次执行
                return;
            }
            if (!DataReadWriteUtil.readTpegFrame(tpegBuffer)) {
                Log.e(TAG, "读取 TPEG 数据失败啦!");
                // 如果读取数据失败了,就直接返回,等待下一次读取
                return;
            }
            Arrays.fill(tpegData, (byte) 0);
            Arrays.fill(tpegInfo, 0);
            NativeMethod.tpegrsdec(tpegBuffer, tpegData, tpegInfo);
            TpegDataProcessing tpegDataProcessing = TpegDataProcessingFactory.getDataProcessor(tpegInfo[0]);
            tpegDataProcessing.processData(tpegBuffer, tpegData, tpegInfo);
        }
    }

    public static int getLength() {
        return length;
    }

    public static int getTotal() {
        return total;
    }

    public static void setLength(int length) {
        DecodeTpegTask.length = length;
    }

    public static void setTotal(int total) {
        DecodeTpegTask.total = total;
    }

    public static byte[] getFileBuffer() {
        return fileBuffer;
    }

    public static void setFileBuffer(byte[] fileBuffer) {
        DecodeTpegTask.fileBuffer = fileBuffer;
    }

    public static boolean isIsReceiveFirstFrame() {
        return isReceiveFirstFrame;
    }

    public static void setIsReceiveFirstFrame(boolean isReceiveFirstFrame) {
        DecodeTpegTask.isReceiveFirstFrame = isReceiveFirstFrame;
    }

    public static String getFileName() {
        return fileName;
    }

    public static void setFileName(String fileName) {
        DecodeTpegTask.fileName = fileName;
    }

    public static DmbListener getDmbListener() {
        return dmbListener;
    }
}
