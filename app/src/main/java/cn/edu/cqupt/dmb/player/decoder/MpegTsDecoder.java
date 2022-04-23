package cn.edu.cqupt.dmb.player.decoder;

import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import java.io.IOException;
import java.io.InputStream;

import cn.edu.cqupt.dmb.player.common.DmbPlayerConstant;
import cn.edu.cqupt.dmb.player.listener.DmbListener;
import cn.edu.cqupt.dmb.player.listener.DmbMpegListener;
import cn.edu.cqupt.dmb.player.utils.ConvertUtils;
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
     * 一个标准 MPEG-TS 包的的大小
     */
    private static final Integer TS_PACKET_188_SIZE = DmbPlayerConstant.DEFAULT_MPEG_TS_PACKET_SIZE_DECODE.getDmbConstantValue();
    private static final String TAG = "MpegTsDecoder";
    private static final boolean DEBUG = true;

    public MpegTsDecoder(DmbListener dmbListener) throws Exception {
        super(dmbListener);
        if (!(dmbListener instanceof DmbMpegListener)) {
            // 如果监听器类型不对就直接抛异常!
            throw new Exception("错误的监听器类型!MPEG解码器构造只能接收DmbMpegListener类型的监听器!");
        }
    }

    /**
     * 从输入流中读取固定长度的数据,一次性读取204字节的数据<br/>
     * 20220422更新,由于现在的 DMB 发射机发射的视频是标准的 TS 流,所以现在不需要进行解交织,也不需要进行解 RS<br/>
     * 现在的策略就是直接从 Buffer 里面读 188 字节的 MPEG-TS 包
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
            /* 读取固定长度的字节 -> 188*/
            int nLeft = 184;
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

    @RequiresApi(api = Build.VERSION_CODES.R)
    @Override
    public void run() {
        while (true) {
            if (!DataReadWriteUtil.USB_READY) {
                // 如果当前 USB 没有就绪,就直接结束当前线程
                return;
            }
            if (!DataReadWriteUtil.initFlag) {
                // 如果目前还没有接收到 DMB 类型的数据,继续执行下一次任务
                continue;
            }
            byte[] mpegTsPacket = new byte[TS_PACKET_188_SIZE];
            if (readMpegTsPacket(bufferedInputStream, mpegTsPacket)) {
                // 读取成功之后直接调用监听器的 success 方法
                if (DEBUG) {
                    Log.i(TAG, "run: 接收到一个 MPEG-TS 包" + ConvertUtils.bytes2hex(mpegTsPacket));
                }
                dmbListener.onSuccess(null, mpegTsPacket, mpegTsPacket.length);
            }
        }

    }
}
