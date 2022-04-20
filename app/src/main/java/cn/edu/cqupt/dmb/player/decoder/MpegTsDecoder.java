package cn.edu.cqupt.dmb.player.decoder;

import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;

import cn.edu.cqupt.dmb.player.common.DmbPlayerConstant;
import cn.edu.cqupt.dmb.player.decoder.interleaver.InterleaverDecoder;
import cn.edu.cqupt.dmb.player.jni.NativeMethod;
import cn.edu.cqupt.dmb.player.listener.DmbListener;
import cn.edu.cqupt.dmb.player.listener.DmbMpegListener;
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
public class MpegTsDecoder extends AbstractDmbDecoder {

    /**
     * 解码器监听器
     */
    private final DmbMpegListener dmbListener;

    private static final Integer tsBuf_204 = DmbPlayerConstant.DEFAULT_MPEG_TS_PACKET_SIZE_ENCODE.getDmbConstantValue();
    private static final Integer tsBuf_188 = DmbPlayerConstant.DEFAULT_MPEG_TS_PACKET_SIZE_DECODE.getDmbConstantValue();


    private static final String TAG = "MpegTsDecoder";

    public MpegTsDecoder(DmbListener dmbListener) throws Exception {
        super(dmbListener);
        if (!(dmbListener instanceof DmbMpegListener)) {
            // 如果监听器类型不对就直接抛异常!
            throw new Exception("错误的监听器类型!MPEG解码器构造只能接收DmbMpegListener类型的监听器!");
        }
        this.dmbListener = (DmbMpegListener) dmbListener;
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    @Override
    public void run() {
//        new TsParser()
        InterleaverDecoder interleaverDecoder = new InterleaverDecoder();
        while (true) {
            if (!DataReadWriteUtil.USB_READY) {
                // 如果当前 USB 没有就绪,就直接结束当前线程
                return;
            }
            if (!DataReadWriteUtil.initFlag) {
                // 如果目前还没有接收到 DMB 类型的数据,继续执行下一次任务
                continue;
            }
            byte[] enMpegTsPacket = new byte[tsBuf_204];
            byte[] deMpegTsPacket = new byte[tsBuf_188];
            if (readMpegTsPacket(bufferedInputStream, enMpegTsPacket)) {
                // 读取到一个 MPEG-TS 包
                byte[] deInterleaverBytes = new byte[tsBuf_204];
                // 解交织
                interleaverDecoder.deinterleaver(enMpegTsPacket, deInterleaverBytes);
                // 对已经解交织的包进行 RS 解码
                NativeMethod.mpegRsDecode(deInterleaverBytes, deMpegTsPacket);

            }
        }

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
        Log.i(TAG, Thread.currentThread().getName() + "线程正在获取MPEG的PipedInputStream");
        return pipedInputStream;
    }
}
