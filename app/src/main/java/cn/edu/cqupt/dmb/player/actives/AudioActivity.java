package cn.edu.cqupt.dmb.player.actives;

import android.media.AudioFormat;
import android.media.AudioTrack;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;

import cn.edu.cqupt.dmb.player.R;
import cn.edu.cqupt.dmb.player.decoder.Mp2Decoder;
import cn.edu.cqupt.dmb.player.listener.impl.DmbAudioListener;
import cn.edu.cqupt.dmb.player.utils.UsbUtil;

public class AudioActivity extends BaseActivity {

    /**
     * 音频播放器
     */
    private AudioTrack audioTrack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 强制全屏,全的不能再全的那种了
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_audio);
        // 初始化音频播放器
        initAudioTrack();
        // 开始音频解码
        startDecode();
        // 开始接收 DMB 数据
        UsbUtil.startReceiveDmbData(pipedOutputStream);
    }

    private void initAudioTrack() {
        int minBufferSize = AudioTrack.getMinBufferSize(48000, AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_16BIT);
        audioTrack = new AudioTrack(AudioTrack.MODE_STREAM, 48000, AudioFormat.CHANNEL_OUT_STEREO,
                AudioFormat.ENCODING_PCM_16BIT, minBufferSize * 2, AudioTrack.MODE_STREAM);
    }

    private void startDecode() {
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
