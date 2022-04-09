package cn.edu.cqupt.dmb.player.decoder;

import android.util.Log;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;

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
     * 锁对象
     */
    private final Object WAIT_TMP_FILE_NAME_LOCK_OBJECT;

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

    private DmbListener dmbListener;

    public MpegTsDecoder(Object lockObject, DmbListener dmbListener) {
        WAIT_TMP_FILE_NAME_LOCK_OBJECT = lockObject;
        this.dmbListener = dmbListener;
    }

    @Override
    public void run() {
        // 尝试获取锁,由于播放器在播放之前都会先设置临时文件的路径,
        // 如果播放器在生成临时文件之前就设置路径就会报错,所以这里做一个同步
        synchronized (WAIT_TMP_FILE_NAME_LOCK_OBJECT) {
            DataReadWriteUtil.setTemporaryMpegTsVideoFilename("");
            // 设置临时文件的名字之后就唤醒播放器那边正在等待锁的线程
            WAIT_TMP_FILE_NAME_LOCK_OBJECT.notifyAll();
        }
        // 在线程被中断之前都一直执行
        while (!this.isInterrupted()) {
            if (!DataReadWriteUtil.USB_READY) {
                Log.e(TAG, "USB 设备没有就绪,不能进行视频解码嗷!~");
                return;
            }
            byte[] bytes = new byte[204];
            if (readMpegTsPacket(bufferedInputStream, bytes)) {

            }
        }
    }

    public static PipedInputStream getPipedInputStream() {
        return pipedInputStream;
    }

    @Override
    public void interrupt() {
        Log.e(TAG, "解码 TS 的任务被中断啦!");
        super.interrupt();
    }

    /**
     * 从输入流中读取固定长度的数据
     *
     * @param inputStream 输入流
     * @param bytes       接收数组
     * @return 成功返回true, 失败返回false
     */
    public static boolean readMpegTsPacket(InputStream inputStream, byte[] bytes) {
        int nRead;
        try {
            bytes[0] = bytes[1] = bytes[2] = (byte) 0xff;
            while ((nRead = inputStream.read(bytes, 3, 1)) > 0) {
                if (bytes[0] == (byte) 0x47 && (bytes[1] == (byte) 0x40 || bytes[1] == (byte) 0x41 || bytes[1] == (byte) 0x50 || bytes[1] == (byte) 0x01) && (bytes[2] == (byte) 0x00 || bytes[2] == (byte) 0x11 || bytes[2] == 0x01)) {
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
}
