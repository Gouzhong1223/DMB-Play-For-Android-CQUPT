package cn.edu.cqupt.dmb.player.actives;

import android.media.AudioFormat;
import android.media.AudioTrack;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import androidx.annotation.NonNull;

import cn.edu.cqupt.dmb.player.decoder.Mp2Decoder;
import cn.edu.cqupt.dmb.player.listener.impl.DmbAudioListener;

public class AudioActivity extends BaseActivity {

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
    public void startDecode() {
        Mp2Decoder mp2Decoder = new Mp2Decoder(new DmbAudioListener(new AudioHandler(Looper.getMainLooper())), this, bufferedInputStream);
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
