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

package cn.edu.cqupt.dmb.player.video.frame;

import android.content.Context;
import android.graphics.Color;
import android.media.AudioManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.FrameLayout;

import java.io.IOException;

import cn.edu.cqupt.dmb.player.listener.DmbPlayerListener;
import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;
import tv.danmaku.ijk.media.player.misc.IMediaDataSource;

/**
 * @Author : Gouzhong
 * @Blog : www.gouzhong1223.com
 * @Description :
 * @Date : create by QingSong in 2022-06-07 18:56
 * @Email : qingsong.qs@alibaba-inc.com
 * @Since : JDK 1.8
 * @PackageName : cn.edu.cqupt.dmb.player.video.frame
 * @ProjectName : DMB Player For Android
 * @Version : 1.0.0
 */
public class AudioPlayerFrame extends FrameLayout {
    private static final String TAG = "AudioPlayerFrame";
    /**
     * 调用播放器的 Context
     */
    private final Context context;
    /**
     * 播放器
     */
    private IMediaPlayer iMediaPlayer = null;
    /**
     * 播放器视图
     */
    private SurfaceView surfaceView = null;
    /**
     * 这个是 IMediaPlayer 用来监听视频大小改变的监听器的
     * 有时候我们收到的视频,前一个和后一个分辨率可能不一样,当视频切换的时候就需要重新设置 IMediaPlayer
     */
    private final IMediaPlayer.OnVideoSizeChangedListener videoSizeChangedListener = (iMediaPlayer, i, i1, i2, i3) -> {
        int videoWidth = iMediaPlayer.getVideoWidth();
        int videoHeight = iMediaPlayer.getVideoHeight();
        if (videoWidth != 0 && videoHeight != 0) {
            surfaceView.getHolder().setFixedSize(videoWidth, videoHeight);
        }
    };
    /**
     * 播放监听器
     */
    private DmbPlayerListener dmbPlayerListener;
    /**
     * 创建一个 IMediaPlayer 的前置处理监听器
     */
    private final IMediaPlayer.OnPreparedListener onPreparedListener = new IMediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(IMediaPlayer iMediaPlayer) {
            if (dmbPlayerListener != null) {
                dmbPlayerListener.onPrepared(iMediaPlayer);
            }
        }
    };
    /**
     * 数据源
     */
    private IMediaDataSource iMediaDataSource;

    public AudioPlayerFrame(Context context) {
        this(context, null);
    }

    public AudioPlayerFrame(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AudioPlayerFrame(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        init();
    }

    /**
     * 初始化 View
     */
    private void init() {
        setBackgroundColor(Color.BLACK);
        createSurfaceView();
        // 构造一个音频播放器
        context.getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
    }

    /**
     * SurfaceView 构造器
     */
    private void createSurfaceView() {
        surfaceView = new SurfaceView(context);
        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                Log.i(TAG, "SurfaceView 被创建了");
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                Log.i(TAG, "surfaceChanged");
                if (iMediaPlayer != null) {
                    // 重新设置holder
                    iMediaPlayer.setDisplay(holder);
                }
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                Log.i(TAG, "surfaceDestroyed");
            }
        });
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT, Gravity.CENTER);
        addView(surfaceView, 0, layoutParams);
    }

    /**
     * 设置ijkplayer的监听
     *
     * @param player IMediaPlayer
     */
    private void setListener(IMediaPlayer player) {
        player.setOnPreparedListener(onPreparedListener);
        player.setOnVideoSizeChangedListener(videoSizeChangedListener);
    }

    /**
     * 设置自己的player回调
     */
    public void setVideoListener(DmbPlayerListener listener) {
        dmbPlayerListener = listener;
    }

    /**
     * 设置播放器的视频源
     *
     * @param iMediaDataSource ijkplayer用于播放输入流的视频源
     */
    public void setDataSource(IMediaDataSource iMediaDataSource) {
        this.iMediaDataSource = iMediaDataSource;
    }

    /**
     * 加载视频
     */
    public void load() throws IOException {
        if (iMediaPlayer != null) {
            iMediaPlayer.stop();
            iMediaPlayer.release();
        }
        iMediaPlayer = createPlayer();
        setListener(iMediaPlayer);
        iMediaPlayer.setDisplay(surfaceView.getHolder());
        // 数据源设置成输入流类型的数据源
        Log.i(TAG, "设置视频源");
        iMediaPlayer.setDataSource(iMediaDataSource);
        Log.i(TAG, "同步开始");
        iMediaPlayer.prepareAsync();
    }

    /**
     * 开始播放视频
     */
    public void start() {
        if (iMediaPlayer != null) {
            Log.i(TAG, "开始播放视频");
            iMediaPlayer.start();
        }
    }

    /**
     * 暂停播放视频
     */
    public void pause() {
        if (iMediaPlayer != null) {
            iMediaPlayer.pause();
        }
    }

    /**
     * 停止播放视频
     */
    public void stop() {
        if (iMediaPlayer != null) {
            Log.i(TAG, "停止播放视频");
            iMediaPlayer.stop();
        }
    }

    /**
     * 重新播放视频
     */
    public void reset() {
        if (iMediaPlayer != null) {
            iMediaPlayer.reset();
        }
    }

    /**
     * 释放对象
     */
    public void release() {
        if (iMediaPlayer != null) {
            iMediaPlayer.reset();
            iMediaPlayer.release();
            iMediaPlayer = null;
        }
    }

    /**
     * 创建IMediaPlayer,后期如果需求变更,可以根据下面给出的参数详解来重新设置一下参数
     *
     * <p>
     * ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec", 0);//关闭mediacodec硬解，使用软解
     * ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec", 1);//开启mediacodec硬解
     * <p>
     * ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "framedrop", 5);   //丢帧  是在视频帧处理不过来的时候丢弃一些帧达到同步的效果
     * ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_CODEC, "skip_loop_filter", 0);  //设置是否开启环路过滤: 0开启，画面质量高，解码开销大，48关闭，画面质量差点，解码开销小
     * //播放延时的解决方案
     * ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "analyzeduration", 1);//设置播放前的探测时间 1,达到首屏秒开效果
     * //如果是rtsp协议，可以优先用tcp(默认是用udp)
     * ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "rtsp_transport", "tcp");
     * ijkMediaPlayer.setOption(1, "analyzemaxduration", 100L);
     * ijkMediaPlayer.setOption(1, "flush_packets", 1L);
     * ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "start-on-prepared", 1);   //需要准备好后自动播放
     * ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "fast", 1);//不额外优化
     * ijkMediaPlayer.setOption(4, "packet-buffering",  0);  //是否开启预缓冲，一般直播项目会开启，达到秒开的效果，不过带来了播放丢帧卡顿的体验
     * ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-auto-rotate", 0);  //自动旋屏
     * ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-handle-resolution-change", 0);   //处理分辨率变化
     * ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "max-buffer-size", 0);//最大缓冲大小,单位kb
     * ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "min-frames", 2);   //默认最小帧数2
     * ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER,  "max_cached_duration", 3);   //最大缓存时长
     * ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER,  "infbuf", 1);   //是否限制输入缓存数
     * ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "fflags", "nobuffer");
     * ijkMediaPlayer.setOption(1, "probesize", 200);  //播放前的探测Size，默认是1M, 改小一点会出画面更快
     * ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER,"reconnect",5);  //播放重连次数
     * <p>
     * <p>
     * ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "dns_cache_clear", 1); //因为项目中多次调用播放器，有网络视频，resp，本地视频，还有wifi上http视频，所以得清空DNS才能播放WIFI上的视频
     * 如果项目无法播放远程视频,可以试试这句话 Server returned 4XX Client Error, but not one of 40{0,1,3,4}报这个错误也可以试试
     *
     * @return IMediaPlayer
     */
    private IMediaPlayer createPlayer() {
        IjkMediaPlayer ijkMediaPlayer = new IjkMediaPlayer();

        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "opensles", 0);
        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec_all_videos ", 1);
        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec_mpeg2 ", 1);
        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "nodisp", 1);

//        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "overlay-format", IjkMediaPlayer.SDL_FCC_RV32);
//        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "framedrop", 1L);
//        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "start-on-prepared", 1);
//
//        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "http-detect-range-support", 1);
//
//        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_CODEC, "skip_loop_filter", 0);
//        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_CODEC, "min-frames", 2);
//        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "enable-accurate-seek", 1);
//
//        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "analyzeduration", 3);//设置播放前的探测时间 1,达到首屏秒开效果
//        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "analyzemaxduration", 100L);
////        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "probesize", 1024 * 100L);
//        // packet-buffering 不设置为 0 可能会卡顿
//        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "packet-buffering", 0L);
//        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "flush_packets", 1L);
//        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "reconnect", 5);
//        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "max-buffer-size", 1024 * 1024 * 5); //最大缓冲大小,单位kb

        ijkMediaPlayer.setVolume(1.0f, 1.0f);
        ijkMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        ijkMediaPlayer.setScreenOnWhilePlaying(true);
        // 是否打开硬编码
        boolean enableMediaCodec = true;
        Log.i(TAG, "createPlayer: 开启硬解码");
        setEnableMediaCodec(ijkMediaPlayer, enableMediaCodec);
        return ijkMediaPlayer;
    }

    /**
     * 是否开启硬解码
     *
     * @param ijkMediaPlayer ijkMediaPlayer
     * @param isEnable       是否开启,true->开启 false->关闭
     */
    private void setEnableMediaCodec(IjkMediaPlayer ijkMediaPlayer, boolean isEnable) {
        int value = isEnable ? 1 : 0;
        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec", value);//开启硬解码
        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-auto-rotate", value);
        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-handle-resolution-change", value);
    }
}
