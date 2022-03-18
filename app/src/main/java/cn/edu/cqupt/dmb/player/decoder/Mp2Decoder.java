package cn.edu.cqupt.dmb.player.decoder;

import android.media.AudioFormat;
import android.media.AudioTrack;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;

import cn.edu.cqupt.dmb.player.jni.NativeMethod;

/**
 * @Author : Gouzhong
 * @Blog : www.gouzhong1223.com
 * @Description :
 * @Date : create by QingSong in 2022-03-18 13:53
 * @Email : gouzhong1223@gmail.com
 * @Since : JDK 1.8
 * @PackageName : com.gouzhong1223.androidtvtset_1.decoder
 * @ProjectName : Android TV Tset-1
 * @Version : 1.0.0
 */
public class Mp2Decoder extends Thread {
    private static final String TAG = "Mp2Decoder";
    private final BufferedInputStream inputStream;
    private PipedInputStream pipedInputStream;
    private AudioTrack audioTrack;
    private byte[] mp2Buffer;
    private byte[] pcmBuffer;

    public Mp2Decoder(InputStream inputStream) {
        this.inputStream = new BufferedInputStream(inputStream);
    }

    public Mp2Decoder() {
        pipedInputStream = new PipedInputStream(1024 * 1024);
        inputStream = new BufferedInputStream(pipedInputStream);
    }

    PipedInputStream getPipedInputStream() {
        return pipedInputStream;
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
        inputStream.read(mp2Buffer, 0, 2);
        while (isMp2Head(mp2Buffer)) {
            mp2Buffer[0] = mp2Buffer[1];
            inputStream.read(mp2Buffer, 1, 1);
        }
        int ret = inputStream.read(mp2Buffer, 2, 382);
        return ret + 2;
    }

    @Override
    public void run() {
        init();
        int[] info = new int[3];
        NativeMethod.mp2DecoderInit();
        while (true) {
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
}
