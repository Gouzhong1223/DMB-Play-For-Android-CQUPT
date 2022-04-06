package cn.edu.cqupt.dmb.player.frame;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.FrameLayout;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;

import cn.edu.cqupt.dmb.player.listener.VideoPlayerListener;
import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

/**
 * @Author : Gouzhong
 * @Blog : www.gouzhong1223.com
 * @Description : 一个自定义 View,专门用来播放 DMB 发射机发射出来的 TS 流视频的
 * @Date : create by QingSong in 2022-04-06 16:47
 * @Email : qingsong.qs@alibaba-inc.com
 * @Since : JDK 1.8
 * @PackageName : cn.edu.cqupt.dmb.player.frame
 * @ProjectName : DMB Player For Android
 * @Version : 1.0.0
 */
public class VideoPlayerFrame extends FrameLayout {

    private static final String TAG = "VideoPlayerFrame";

    private IMediaPlayer iMediaPlayer = null;

    private FileDescriptor fileDescriptor;

    private SurfaceView surfaceView = null;

    private final Context context;

    private VideoPlayerListener videoPlayerListener;

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
        LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT
                , LayoutParams.MATCH_PARENT, Gravity.CENTER);
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
    public void setVideoListener(VideoPlayerListener listener) {
        videoPlayerListener = listener;
    }

    /**
     * 设置播放地址
     *
     * @param path 视频源路径
     */
    public void setPath(String path) {
        setPath(path, null);
    }

    /**
     * 设置视频源路径
     *
     * @param path   视频源路径
     * @param header header
     */
    public void setPath(String path, Map<String, String> header) {
        try {
            FileInputStream fileInputStream = new FileInputStream(path);
            fileDescriptor = fileInputStream.getFD();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
        iMediaPlayer.setDataSource(fileDescriptor);

        iMediaPlayer.prepareAsync();
    }

    /**
     * 开始播放视频
     */
    public void start() {
        if (iMediaPlayer != null) {
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
        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "opensles", 1);

        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "overlay-format", IjkMediaPlayer.SDL_FCC_RV32);
        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "framedrop", 1);
        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "start-on-prepared", 0);

        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "http-detect-range-support", 1);

        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_CODEC, "skip_loop_filter", 48);
        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_CODEC, "min-frames", 100);
        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "enable-accurate-seek", 1);

        ijkMediaPlayer.setVolume(1.0f, 1.0f);
        // 是否打开硬编码
        boolean enableMediaCodec = true;
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


    /**
     * 创建一个 IMediaPlayer 的前置处理监听器
     */
    private final IMediaPlayer.OnPreparedListener onPreparedListener = new IMediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(IMediaPlayer iMediaPlayer) {
            if (videoPlayerListener != null) {
                videoPlayerListener.onPrepared(iMediaPlayer);
            }
        }
    };

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

    public VideoPlayerFrame(Context context) {
        this(context, null);
    }

    public VideoPlayerFrame(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VideoPlayerFrame(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        init();
    }
}
