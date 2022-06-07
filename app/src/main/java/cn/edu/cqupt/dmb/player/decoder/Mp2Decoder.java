package cn.edu.cqupt.dmb.player.decoder;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import cn.edu.cqupt.dmb.player.jni.NativeMethod;
import cn.edu.cqupt.dmb.player.listener.DmbListener;
import cn.edu.cqupt.dmb.player.utils.DataReadWriteUtil;

/**
 * @Author : Gouzhong
 * @Blog : www.gouzhong1223.com
 * @Description : 这是 MP2 音频的解码器
 * @Date : create by QingSong in 2022-03-18 13:53
 * @Email : qingsong.qs@alibaba-inc.com
 * @Since : JDK 1.8
 * @PackageName : cn.edu.cqupt.dmb.player.decoder
 * @ProjectName : DMB Player For Android
 * @Version : 1.0.0
 */
public class Mp2Decoder extends BaseDmbDecoder {

    private static final String TAG = "Mp2Decoder";

    public Mp2Decoder(DmbListener dmbListener, Context context, BufferedInputStream bufferedInputStream, Handler handler) {
        super(bufferedInputStream, dmbListener, context, handler);
    }

    @Override
    public void run() {
        Log.i(TAG, Thread.currentThread().getName() + "线程现在开始 MP2 解码");
        int[] info = new int[3];
        byte[] mp2Buffer = new byte[384];
        byte[] pcmBuffer = new byte[1024 * 1024];
        NativeMethod.mp2DecoderInit();
        while (!DataReadWriteUtil.inMainActivity) {
            if (!DataReadWriteUtil.USB_READY) {
                // 如果当前 USB 没有就绪,就直接结束当前线程
                // Log.e(TAG, "现在 USB 还没有就绪!");
                return;
            }
            if (!DataReadWriteUtil.initFlag) {
                // 如果目前还没有接收到 DMB 类型的数据,就直接返回
                // Log.e(TAG, "现在还没有接收到 DMB 类型的数据!");
                continue;
            }
            if (!readMp2Frame(mp2Buffer)) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            Arrays.fill(pcmBuffer, (byte) 0);
            Arrays.fill(info, 0);
            int length = NativeMethod.decodeMp2Frame(mp2Buffer, 384, pcmBuffer, info);
            if (dmbListener != null) {
                dmbListener.onSuccess(null, pcmBuffer, length);
            }
        }
    }

    /**
     * 读取一个 MP2 数据包
     *
     * @param bytes 承载 MP2 数据包的容器
     * @return 成功返回 true
     */
    private boolean readMp2Frame(byte[] bytes) {
        int nRead;
        try {
            bytes[0] = bytes[1] = (byte) 0xff;
            while ((nRead = (bufferedInputStream).read(bytes, 2, 1)) > 0) {
                if ((bytes[0] == (byte) 0xFF && bytes[1] == (byte) 0xFC) || (bytes[0] == (byte) 0xFF && bytes[1] == (byte) 0xF4)) {
                    break;
                }
                System.arraycopy(bytes, 1, bytes, 0, 2);
            }
            if (nRead <= 0) {
                return false;
            }
            /* read n bytes method, according to unix network programming page 72 */
            /* read left data of the frame */
            int nLeft = 382;
            int pos = 2;
            while (nLeft > 0) {
                if ((nRead = ((InputStream) bufferedInputStream).read(bytes, pos, nLeft)) <= 0) {
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
}
