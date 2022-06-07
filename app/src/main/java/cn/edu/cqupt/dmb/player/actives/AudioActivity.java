package cn.edu.cqupt.dmb.player.actives;

import android.media.AudioFormat;
import android.media.AudioTrack;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;

import cn.edu.cqupt.dmb.player.decoder.Mp2Decoder;
import cn.edu.cqupt.dmb.player.listener.impl.DmbAudioListener;

public class AudioActivity extends BaseActivity {

    private static final String TAG = "AudioActivity";
    /**
     * 音频播放器
     */
    private AudioTrack audioTrack;


    @Override
    public void initView() {
        int minBufferSize = AudioTrack.getMinBufferSize(48000, AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_16BIT);
        audioTrack = new AudioTrack(AudioTrack.MODE_STREAM, 48000, AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_16BIT, minBufferSize * 2, AudioTrack.MODE_STREAM);
    }

    @Override
    public void configView() {
        Log.i(TAG, "configView: " + getLocalClassName() + "不需要配置组件");
    }

    @Override
    public void startDecode() {
        AudioHandler audioHandler = new AudioHandler(Looper.getMainLooper());
        Mp2Decoder mp2Decoder = new Mp2Decoder(new DmbAudioListener(audioHandler), this, bufferedInputStream, audioHandler);
        mp2Decoder.start();
        audioTrack.play();
    }

    @Override
    protected void onDestroy() {
        if (audioTrack != null) {
            audioTrack.stop();
        }
        super.onDestroy();
    }

    /**
     * 自定义回调器
     */
    private class AudioHandler extends Handler {

        public AudioHandler(@NonNull Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            if (msg.what == 0x95) {
                byte[] pcmBytes = (byte[]) msg.obj;
                if (pcmBytes == null) {
                    return;
                }
                audioTrack.write(pcmBytes, 0, pcmBytes.length);
            }
        }
    }
}
