package cn.edu.cqupt.dmb.player.decoder;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioTrack;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

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
public class Mp2Decoder extends AbstractDmbDecoder {
    private static final String TAG = "Mp2Decoder";

    private AudioTrack audioTrack;
    private byte[] mp2Buffer;
    private byte[] pcmBuffer;

    public Mp2Decoder(DmbListener dmbListener, Context context, BufferedInputStream bufferedInputStream) {
        super(bufferedInputStream, dmbListener, context);
    }

    @Override
    public void run() {
        init();
        int[] info = new int[3];
        NativeMethod.mp2DecoderInit();
        while (DataReadWriteUtil.USB_READY) {
            try {
                if (readMp2Frame() != 384) {
                    Log.e(TAG, "time out");
                    sleep(100);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            int len = NativeMethod.decodeMp2Frame(mp2Buffer, 384, pcmBuffer, info);
            audioTrack.write(pcmBuffer, 0, len);
        }
    }

    private void init() {
        mp2Buffer = new byte[1024];
        pcmBuffer = new byte[1024 * 1024];
        int minBufferSize = AudioTrack.getMinBufferSize(48000, AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_16BIT);
        audioTrack = new AudioTrack(AudioTrack.MODE_STREAM, 48000, AudioFormat.CHANNEL_OUT_STEREO,
                AudioFormat.ENCODING_PCM_16BIT, minBufferSize * 2, AudioTrack.MODE_STREAM);
        audioTrack.play();
    }

    private boolean isMp2Head(byte[] bytes) {
        return bytes[0] == (byte) 0xFF;
//        return (bytes[0] == (byte) 0xFF && bytes[1] == (byte) 0xFC) || (bytes[0] == (byte) 0xFF && bytes[1] == (byte) 0xF4);
//        return (bytes[0] == (byte) 0xFF && bytes[1] == (byte) 0xFC) || (bytes[0] == (byte) 0xFF && bytes[1] == (byte) 0xF4);
    }

    private int readMp2Frame() throws IOException {
        bufferedInputStream.read(mp2Buffer, 0, 2);
        while (isMp2Head(mp2Buffer)) {
            mp2Buffer[0] = mp2Buffer[1];
            bufferedInputStream.read(mp2Buffer, 1, 1);
        }
        int ret = bufferedInputStream.read(mp2Buffer, 2, 382);
        return ret + 2;
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
