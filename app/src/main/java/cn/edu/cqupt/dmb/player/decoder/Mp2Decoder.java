package cn.edu.cqupt.dmb.player.decoder;

import android.media.AudioFormat;
import android.media.AudioTrack;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.PipedInputStream;

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
public class Mp2Decoder extends Thread {
    private static final String TAG = "Mp2Decoder";
    private AudioTrack audioTrack;
    private byte[] mp2Buffer;
    private byte[] pcmBuffer;
    private DmbListener dmbListener;
    private static final BufferedInputStream bufferedInputStream;

    private static final PipedInputStream pipedInputStream = new PipedInputStream(1024 * 2);

    static {
        bufferedInputStream = new BufferedInputStream(pipedInputStream);
    }

    public Mp2Decoder(DmbListener dmbListener) {
        this.dmbListener = dmbListener;
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
        if ((bytes[0] == 0xFF && bytes[1] == 0xFC) || (bytes[0] == 0xFF && bytes[1] == 0xF4)) {
            return true;
        }
        return false;
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
}
