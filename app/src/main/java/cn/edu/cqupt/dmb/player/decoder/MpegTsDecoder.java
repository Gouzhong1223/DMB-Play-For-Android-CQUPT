package cn.edu.cqupt.dmb.player.decoder;

import android.util.Log;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import cn.edu.cqupt.dmb.player.jni.NativeMethod;
import cn.edu.cqupt.dmb.player.listener.DmbListener;
import cn.edu.cqupt.dmb.player.utils.DataReadWriteUtil;

/**
 * @Author : Gouzhong
 * @Blog : www.gouzhong1223.com
 * @Description : 这个是解码 TS 的任务,他的启动依赖 VideoActivity 的调度
 * @Date : create by QingSong in 2022-04-06 20:59
 * @Email : qingsong.qs@alibaba-inc.com
 * @Since : JDK 1.8
 * @PackageName : cn.edu.cqupt.dmb.player.decoder
 * @ProjectName : DMB Player For Android
 * @Version : 1.0.0
 */
public class MpegTsDecoder extends Thread {

    private static final String TAG = "MpegTsDecoder";

    /**
     * TS 视频的缓冲流
     */
    private static final BufferedInputStream bufferedInputStream;

    /**
     * TS 视频流的输入流
     */
    private static final PipedInputStream pipedInputStream = new PipedInputStream(1024 * 10);

    static {
        bufferedInputStream = new BufferedInputStream(pipedInputStream);
    }

    private final DmbListener dmbListener;

    /**
     * MPEG解码器构造
     *
     * @param dmbListener 视频监听器
     */
    public MpegTsDecoder(DmbListener dmbListener) {
        this.dmbListener = dmbListener;
    }

    @Override
    public void run() {
        // 初始化MPEG-TS解码器
        NativeMethod.mpegTsDecodeInit();
        // 在线程被中断之前都一直执行
        while (!this.isInterrupted()) {
            if (!DataReadWriteUtil.USB_READY) {
                Log.e(TAG, "USB 设备没有就绪,不能进行视频解码嗷!~");
                return;
            }
            byte[] bytes = new byte[204];
            // 读取一个MPEG-TS包长度为204
            if (readMpegTsPacket(bufferedInputStream, bytes)) {
                byte[] tsData = new byte[188];
                // 解码MPEG-TS包
                if (NativeMethod.decodeMpegTsFrame(bytes, tsData) == -1) {
                    // 如果解码得到的结果是-1代表解码失败
                    return;
                }
                // 如果解码成功就回调监听器
                dmbListener.onSuccess(null, tsData, tsData.length);
            }
        }
    }


    @Override
    public void interrupt() {
        Log.e(TAG, "解码 TS 的任务被中断啦!");
        super.interrupt();
    }

    /**
     * 从输入流中读取固定长度的数据,一次性读取204字节的数据
     *
     * @param inputStream 输入流
     * @param bytes       接收数组
     * @return 成功返回true, 失败返回false
     */
    public static boolean readMpegTsPacket(InputStream inputStream, byte[] bytes) {
        int nRead;
        try {
            bytes[0] = bytes[1] = bytes[2] = (byte) 0xff;
            // 寻找TS包头
            while ((nRead = inputStream.read(bytes, 3, 1)) > 0) {
                if (bytes[0] == (byte) 0x47
                        && (bytes[1] == (byte) 0x40
                        || bytes[1] == (byte) 0x41
                        || bytes[1] == (byte) 0x50
                        || bytes[1] == (byte) 0x01)
                        && (bytes[2] == (byte) 0x00
                        || bytes[2] == (byte) 0x11
                        || bytes[2] == 0x01)) {
                    break;
                }
                System.arraycopy(bytes, 1, bytes, 0, 3);
            }
            if (nRead <= 0) {
                return false;
            }
            /* 读取固定长度的字节 */
            int nLeft = 200;
            int pos = 4;
            while (nLeft > 0) {
                if ((nRead = inputStream.read(bytes, pos, nLeft)) <= 0) {
                    return false;
                }
                nLeft -= nRead;
                pos += nRead;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static PipedInputStream getPipedInputStream() {
        return pipedInputStream;
    }
}
