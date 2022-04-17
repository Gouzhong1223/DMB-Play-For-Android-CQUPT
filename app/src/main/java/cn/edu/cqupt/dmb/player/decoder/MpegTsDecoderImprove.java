package cn.edu.cqupt.dmb.player.decoder;

import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.PipedInputStream;
import java.util.Arrays;

import cn.edu.cqupt.dmb.player.jni.NativeMethod;
import cn.edu.cqupt.dmb.player.listener.DmbListener;
import cn.edu.cqupt.dmb.player.listener.DmbMpegListener;
import cn.edu.cqupt.dmb.player.utils.BaseConversionUtil;
import cn.edu.cqupt.dmb.player.utils.DataReadWriteUtil;

/**
 * @Author : Gouzhong
 * @Blog : www.gouzhong1223.com
 * @Description : MPEG-TS改版解码器
 * @Date : create by QingSong in 2022-04-17 13:06
 * @Email : qingsong.qs@alibaba-inc.com
 * @Since : JDK 1.8
 * @PackageName : cn.edu.cqupt.dmb.player.decoder
 * @ProjectName : DMB Player For Android
 * @Version : 1.0.4
 */
public class MpegTsDecoderImprove implements Runnable {

    /**
     * TS 视频流的输入流
     */
    private static final PipedInputStream pipedInputStream = new PipedInputStream(1024 * 1024 * 20);

    /**
     * TS 视频的缓冲流
     */
    private static final BufferedInputStream bufferedInputStream = new BufferedInputStream(pipedInputStream);

    private final DmbMpegListener dmbListener;

    /**
     * 一个TPEG的包大小
     */
    private static final int TPEG_SIZE = 112;
    /**
     * 一个TPEG包的有效长度
     */
    private static final int DATA_SIZE = 80;
    /**
     * TPEG info 数组长度
     */
    private static final int TPEG_INFO_SIZE = 3;

    private static final String TAG = "MpegTsDecoderImprove";

    public MpegTsDecoderImprove(DmbListener dmbListener) {
        this.dmbListener = (DmbMpegListener) dmbListener;
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    @Override
    public void run() {

        // 装载未解码的TPEG包
        byte[] tpegBuffer = new byte[TPEG_SIZE];
        // 装载已解码的TPEG包
        byte[] tpegData = new byte[DATA_SIZE];
        // 装载TPEG信息
        int[] tpegInfo = new int[TPEG_INFO_SIZE];
        Log.i(TAG, Thread.currentThread().getName() + "线程开始了 MPEG-TS 的解码");
        NativeMethod.tpegInit();
        // 死循环,等待线程池的调度
        for (; ; ) {
            if (!DataReadWriteUtil.USB_READY) {
                // 如果当前 USB 没有就绪,就直接结束当前线程
                return;
            }
            if (!DataReadWriteUtil.initFlag) {
                // 如果目前还没有接收到 DMB 类型的数据,继续执行下一次任务
                continue;
            }
            tpegBuffer[0] = tpegBuffer[1] = tpegBuffer[2] = (byte) 0;
            if (!readTpegFrame(tpegBuffer)) {
                // 读取TPEG失败就放弃
                continue;
            }
//            Log.i(TAG, BaseConversionUtil.bytes2hex(tpegBuffer));
            Arrays.fill(tpegData, (byte) 0);
            Arrays.fill(tpegInfo, 0);
            NativeMethod.decodeTpegFrame(tpegBuffer, tpegData, tpegInfo);
            if (tpegInfo[0] == 1 || tpegInfo[0] == 2 || tpegInfo[0] == 3) {
                // 说明这是一个有效的TPEG数组
//                Log.i(TAG, "接收到一个有效的TPEG");
                dmbListener.onSuccess(tpegData, tpegInfo[1], tpegInfo);
            }
        }
    }

    /**
     * 读取一个TPEG包
     *
     * @param bytes 承载数组
     * @return 成功返回true
     */
    private boolean readTpegFrame(byte[] bytes) {
        int nRead;
        try {
            while ((nRead = MpegTsDecoderImprove.bufferedInputStream.read(bytes, 3, 1)) > 0) {
                if (bytes[1] == (byte) 0x01 && bytes[2] == (byte) 0x5b && bytes[3] == (byte) 0xF4) {
                    break;
                }
                System.arraycopy(bytes, 1, bytes, 0, 3);
            }
            if (nRead <= 0) {
                return false;
            }
            /* read n bytes method, according to unix network programming page 72 */
            /* read left data of the frame */
            int nLeft = 108;
            int pos = 4;
            while (nLeft > 0) {
                if ((nRead = MpegTsDecoderImprove.bufferedInputStream.read(bytes, pos, nLeft)) <= 0) {
                    return false;
                }
                nLeft -= nRead;
                pos += nRead;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    public static PipedInputStream getPipedInputStream() {
        Log.i(TAG, Thread.currentThread().getName() + "线程正在获取MPEG的PipedInputStream");
        return pipedInputStream;
    }
}
