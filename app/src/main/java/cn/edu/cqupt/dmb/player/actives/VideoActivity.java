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


import android.content.pm.ActivityInfo;
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
import cn.edu.cqupt.dmb.player.decoder.MpegTsReader;
import cn.edu.cqupt.dmb.player.listener.DmbListener;
import cn.edu.cqupt.dmb.player.listener.impl.DmbMpegListenerImpl;
import cn.edu.cqupt.dmb.player.listener.impl.VideoPlayerListenerImpl;
import cn.edu.cqupt.dmb.player.processor.dmb.DataProcessingFactory;
import cn.edu.cqupt.dmb.player.processor.dmb.PseudoBitErrorRateProcessor;
import cn.edu.cqupt.dmb.player.video.frame.VideoPlayerFrame;
import cn.edu.cqupt.dmb.player.video.stream.DmbMediaDataSource;


/**
 * 这个是播放视频的 Activity<br/>
 * 原则上逻辑应该是这样的,当用户点击到这个组件之后,用于解码 MPEG-TS 的解码器线程才开始工作,会有一个同步<br/>
 * 这里经过我的反复测试,发现一个无解的Bug,就是这个VideoActivity在播放视频的时候,会被创建两次,就很离谱!<br/>
 * 表现出来的异常就是,会唤起两次播放视频的Bug<br/>
 * 然后后播放的视频在视觉上会覆盖第一个播放的视频,但是声音不会,有重音,就像混响一样<br/>
 * 没办法最后我只能在下面加一个初始化判断<br/>
 *
 * @author qingsong
 */
public class VideoActivity extends BaseActivity {

    /**
     * 视频播放回调消息
     */
    public static final int MESSAGE_START_PLAY_VIDEO = DmbPlayerConstant.MESSAGE_START_PLAY_VIDEO.getDmbConstantValue();
    private static final String TAG = "VideoActivity";
    /**
     * 解码后一个MPEG-TS包的大小
     */
    private static final Integer DEFAULT_MPEG_TS_PACKET_SIZE_DECODE = DmbPlayerConstant.DEFAULT_MPEG_TS_PACKET_SIZE_DECODE.getDmbConstantValue();
    /**
     * 输出流计量倍数
     */
    private static final Integer DEFAULT_MPEG_TS_STREAM_SIZE_TIMES = DmbPlayerConstant.DEFAULT_MPEG_TS_STREAM_SIZE_TIMES.getDmbConstantValue();
    /**
     * 监听信号更新的 message 类型
     */
    private final int MESSAGE_UPDATE_SIGNAL = DmbPlayerConstant.MESSAGE_UPDATE_SIGNAL.getDmbConstantValue();
    /**
     * 自定义的视频播放组件
     */
    private VideoPlayerFrame videoPlayerFrame = null;
    /**
     * 信号显示组件
     */
    private ImageView signalImageView;
    /**
     * 已经解码的MPEG-TS视频缓冲流
     */
    private BufferedInputStream mPegTsBufferedInputStream;
    /**
     * 已经解码的MPEG-TS视频输入流
     */
    private PipedInputStream mpegTsPipedInputStream;
    /**
     * 已经解码的MPEG-TS视频输出流
     */
    private PipedOutputStream mpegTsPipedOutputStream;

    /**
     * 初始化组件
     */
    @Override
    public void initView() {
        videoPlayerFrame = findViewById(R.id.video_surface);
        signalImageView = findViewById(R.id.video_signal);
    }

    @Override
    public void configView() {
        // 设置视频播放器的监听器
        videoPlayerFrame.setVideoListener(new VideoPlayerListenerImpl(videoPlayerFrame));
        if (defaultSignalShowSetting != null) {
            int showSignal = Math.toIntExact(defaultSignalShowSetting.getSettingValue());
            signalImageView.setVisibility(showSignal == 0 ? View.INVISIBLE : View.VISIBLE);
        }
        if (showDebugLogSetting != null) {
            int showLog = Math.toIntExact(showDebugLogSetting.getSettingValue());
            if (showLog == 1) {
                runOnUiThread(() -> new LogcatDialog(VideoActivity.this).show());
            }
        }
    }

    /**
     * 这里开一个线程去执行 MPEG-TS 的解码任务<br/>
     * 已经解码的MPEG-TS流会被放进缓冲流
     */
    @Override
    public void startDecode() {
        // 初始化视频元数据管道
        initVideoPip();
        VideoHandler videoHandler = new VideoHandler(Looper.getMainLooper());
        // 构造视频监听器,传入视频输出流以及回调类
        DmbListener videoPlayerListener = new DmbMpegListenerImpl(videoHandler, mpegTsPipedOutputStream);
        // 构造解码器
        MpegTsReader mpegTsReader;
        try {
            mpegTsReader = new MpegTsReader(videoPlayerListener, this, bufferedInputStream, videoHandler);
            mpegTsReader.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 初始化装载视频的管道
     */
    private void initVideoPip() {
        // 构造已解码的TS输入流
        mpegTsPipedInputStream = new PipedInputStream(DEFAULT_MPEG_TS_PACKET_SIZE_DECODE * DEFAULT_MPEG_TS_STREAM_SIZE_TIMES);
        // 构造已解码的TS输出流
        mpegTsPipedOutputStream = new PipedOutputStream();
        try {
            // 连接输入输出流
            mpegTsPipedOutputStream.connect(mpegTsPipedInputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 构造已解码的TS缓冲流
        mPegTsBufferedInputStream = new BufferedInputStream(mpegTsPipedInputStream);
    }

    /**
     * 播放视频
     */
    private void playVideo() {
        // 构造自定义的数据源
        DmbMediaDataSource dmbMediaDataSource = new DmbMediaDataSource(mPegTsBufferedInputStream);
        // 设置MPEG-TS播放器的数据源为自定义数据源
        videoPlayerFrame.setDataSource(dmbMediaDataSource);
        try {
            // 加载数据源
            videoPlayerFrame.load();
        } catch (IOException e) {
            e.printStackTrace();
            onDestroy();
            Log.e(TAG, "播放视频失败啦！");
            Toast.makeText(this, "播放失败", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        if (getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        // 关闭线程池中的任务
        if (videoPlayerFrame != null) {
            videoPlayerFrame.release();
        }
        closeStream();
        super.onDestroy();
    }

    /**
     * 关闭管道流以及输入缓冲流
     */
    private void closeStream() {
        try {
            if (mpegTsPipedOutputStream != null) {
                mpegTsPipedOutputStream.close();
            }
            if (mpegTsPipedInputStream != null) {
                mpegTsPipedInputStream.close();
            }
            if (mPegTsBufferedInputStream != null) {
                mPegTsBufferedInputStream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 自定义的视频播放回调类
     */
    private class VideoHandler extends Handler {
        // 这个构造方法不重写有意想不到的Bug在等你🤬
        public VideoHandler(@NonNull Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            if (msg.what == MESSAGE_START_PLAY_VIDEO) {
                // 缓冲流里面已经有东西啦!开始播放视频!
                playVideo();
            } else if (msg.what == MESSAGE_UPDATE_SIGNAL) {
                // 如果没有进行信号显示设置或者关闭信号显示就直接跳过广播处理
                if (defaultSignalShowSetting == null || defaultSignalShowSetting.getSettingValue() == 0L) {
                    return;
                }
                PseudoBitErrorRateProcessor pseudoBitErrorRateProcessor = (PseudoBitErrorRateProcessor) DataProcessingFactory.getDataProcessor(0x00);
                // 这里为什么能直接获取ber,因为是从静态工厂里面去出来的,静态工厂里面的都是单例创建的对象,在系统初始化的时候就已经load了,然后就是ber是一个volatile变量
                // 不懂volatile是什么的可以搜一下Java多线程中的工作内存和主内存的区别,看他们是如何消除内存屏障的
                int ber = pseudoBitErrorRateProcessor.getBer();
                Log.i(TAG, "ber = " + ber);
                if (ber > 200) {
                    signalImageView.setImageResource(R.drawable.singlemark1);
                } else if (ber > 100) {
                    signalImageView.setImageResource(R.drawable.singlemark2);
                } else if (ber > 50) {
                    signalImageView.setImageResource(R.drawable.singlemark3);
                } else if (ber > 10) {
                    signalImageView.setImageResource(R.drawable.singlemark4);
                } else if (ber >= 0) {
                    signalImageView.setImageResource(R.drawable.singlemark5);
                }
            } else if (msg.what == 0x77) {
                // 接收到关闭 Activity 的消息
                if (!VideoActivity.this.isDestroyed()) {
                    VideoActivity.this.onDestroy();
                }
            }
        }
    }
}
