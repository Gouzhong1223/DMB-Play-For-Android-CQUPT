/*
 *
 *              Copyright 2022 By Gouzhong1223
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 */

package cn.edu.cqupt.dmb.player.actives;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.hdl.logcatdialog.LogcatDialog;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import cn.edu.cqupt.dmb.player.R;
import cn.edu.cqupt.dmb.player.common.DmbPlayerConstant;
import cn.edu.cqupt.dmb.player.decoder.Mp2Reader;
import cn.edu.cqupt.dmb.player.listener.impl.AudioPlayerListenerImpl;
import cn.edu.cqupt.dmb.player.listener.impl.DmbAudioListenerImpl;
import cn.edu.cqupt.dmb.player.video.frame.AudioPlayerFrame;
import cn.edu.cqupt.dmb.player.video.stream.DmbMediaDataSource;

public class AudioActivity extends BaseActivity {

    /**
     * 音频播放回调消息
     */
    public static final int MESSAGE_START_PLAY_AUDIO = DmbPlayerConstant.MESSAGE_START_PLAY_VIDEO.getDmbConstantValue();
    private static final String TAG = "AudioActivity";
    /**
     * 自定义的音频播放组件
     */
    private AudioPlayerFrame audioPlayerFrame = null;

    /**
     * 信号显示组件
     */
    private ImageView signalImageView;
    /**
     * 已经解码的MP2音频缓冲流
     */
    private BufferedInputStream mp2BufferedInputStream;
    /**
     * 已经解码的MP2音频输入流
     */
    private PipedInputStream mp2PipedInputStream;
    /**
     * 已经解码的MP2音频输出流
     */
    private PipedOutputStream mp2PipedOutputStream;


    @Override
    public void initView() {
        audioPlayerFrame = findViewById(R.id.audioPlayerFrame);
        signalImageView = findViewById(R.id.audio_signal);
    }

    @Override
    public void configView() {
        // 设置音频播放器的监听器
        audioPlayerFrame.setVideoListener(new AudioPlayerListenerImpl(audioPlayerFrame));
        if (defaultSignalShowSetting != null) {
            int showSignal = Math.toIntExact(defaultSignalShowSetting.getSettingValue());
            signalImageView.setVisibility(showSignal == 0 ? View.INVISIBLE : View.VISIBLE);
        }
        if (showDebugLogSetting != null) {
            int showLog = Math.toIntExact(showDebugLogSetting.getSettingValue());
            if (showLog == 1) {
                runOnUiThread(() -> new LogcatDialog(AudioActivity.this).show());
            }
        }
    }

    @Override
    public void startDecode() {
        // 初始化音频元数据管道
        initAudioPip();
        AudioHandler audioHandler = new AudioHandler(Looper.getMainLooper());
        DmbAudioListenerImpl dmbAudioListener = new DmbAudioListenerImpl(audioHandler, mp2PipedOutputStream);
        // 构造解码器
        Mp2Reader mp2Reader = new Mp2Reader(dmbAudioListener, this, bufferedInputStream, audioHandler);
        // 开始解码
        mp2Reader.start();
    }

    /**
     * 播放音频
     */
    private void playAudio() {
        // 构造自定义的数据源
        DmbMediaDataSource dmbMediaDataSource = new DmbMediaDataSource(mp2BufferedInputStream);
        // 设置MP2播放器的数据源为自定义数据源
        audioPlayerFrame.setDataSource(dmbMediaDataSource);
        try {
            // 加载数据源
            audioPlayerFrame.load();
        } catch (IOException e) {
            e.printStackTrace();
            onDestroy();
            Log.e(TAG, "播放音频失败啦！");
            Toast.makeText(this, "播放失败", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 初始化装载音频的管道
     */
    private void initAudioPip() {
        // 构造已解码的MP2输入流
        mp2PipedInputStream = new PipedInputStream(1024 * 1024 * 2);
        // 构造已解码的MP2输出流
        mp2PipedOutputStream = new PipedOutputStream();
        try {
            // 连接输入输出流
            mp2PipedOutputStream.connect(mp2PipedInputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 构造已解码的MP2缓冲流
        mp2BufferedInputStream = new BufferedInputStream(mp2PipedInputStream);
    }

    /**
     * 关闭管道流以及输入缓冲流
     */
    private void closeStream() {
        try {
            if (mp2PipedOutputStream != null) {
                mp2PipedOutputStream.close();
            }
            if (mp2PipedInputStream != null) {
                mp2PipedInputStream.close();
            }
            if (mp2BufferedInputStream != null) {
                mp2BufferedInputStream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        if (audioPlayerFrame != null) {
            audioPlayerFrame.release();
        }
        closeStream();
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
            if (msg.what == MESSAGE_START_PLAY_AUDIO) {
                playAudio();
            }
        }
    }
}
