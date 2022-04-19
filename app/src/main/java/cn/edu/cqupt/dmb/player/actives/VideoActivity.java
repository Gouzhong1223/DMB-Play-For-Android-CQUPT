package cn.edu.cqupt.dmb.player.actives;


import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import cn.edu.cqupt.dmb.player.R;
import cn.edu.cqupt.dmb.player.common.DmbPlayerConstant;
import cn.edu.cqupt.dmb.player.common.FrequencyModule;
import cn.edu.cqupt.dmb.player.decoder.FicDecoder;
import cn.edu.cqupt.dmb.player.decoder.MpegTsDecoder;
import cn.edu.cqupt.dmb.player.frame.DmbMediaDataSource;
import cn.edu.cqupt.dmb.player.frame.VideoPlayerFrame;
import cn.edu.cqupt.dmb.player.listener.DmbListener;
import cn.edu.cqupt.dmb.player.listener.DmbMpegListener;
import cn.edu.cqupt.dmb.player.listener.VideoPlayerListenerImpl;
import cn.edu.cqupt.dmb.player.utils.DataReadWriteUtil;
import cn.edu.cqupt.dmb.player.utils.UsbUtil;


/**
 * 这个是播放视频的 Activity<br/>
 * 原则上逻辑应该是这样的,当用户点击到这个组件之后,用于解码 MPEG-TS 的解码器线程才开始工作,会有一个同步<br/>
 * 这里经过我的反复测试,发现一个无解的Bug,就是这个VideoActivity在播放视频的时候,会被创建两次,就很离谱!<br/>
 * 表现出来的异常就是,会唤起两次播放视频的Bug<br/>
 * 然后后播放的视频在视觉上会覆盖第一个播放的视频,但是声音不会,有重音,就像混响一样<br/>
 * 没办法最后我只能在下面加一个初始化判断<br/>
 */
@RequiresApi(api = Build.VERSION_CODES.R)
public class VideoActivity extends Activity {

    private static final String TAG = "VideoActivity";
    /**
     * 自定义的视频播放组件
     */
    private VideoPlayerFrame videoPlayerFrame = null;

    /**
     * 单例线程池,运行MPEG解码线程的
     */
    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    /**
     * 已经解码的MPEG-TS视频缓冲流
     */
    private BufferedInputStream bufferedInputStream;

    /**
     * 已经解码的MPEG-TS视频输入流
     */
    private PipedInputStream pipedInputStream;

    /**
     * 已经解码的MPEG-TS视频输出流
     */
    private PipedOutputStream pipedOutputStream;

    /**
     * 视频播放回调消息
     */
    public static final int MESSAGE_START_PLAY_VIDEO = DmbPlayerConstant.MESSAGE_START_PLAY_VIDEO.getDmbConstantValue();

    /**
     * 解码后一个MPEG-TS包的大小
     */
    private static final Integer DEFAULT_MPEG_TS_PACKET_SIZE_DECODE = DmbPlayerConstant.DEFAULT_MPEG_TS_PACKET_SIZE_DECODE.getDmbConstantValue();

    /**
     * 输出流计量倍数
     */
    private static final Integer DEFAULT_MPEG_TS_STREAM_SIZE_TIMES = DmbPlayerConstant.DEFAULT_MPEG_TS_STREAM_SIZE_TIMES.getDmbConstantValue();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 强制全屏,全的不能再全的那种了
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_video);
        // 初始化View
        initView();
        // 开始MPEG-TS解码
        try {
            startMpegTsCodec();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 初始化组件
     */
    private void initView() {
        if (executorService.isShutdown()) {
            executorService = Executors.newSingleThreadExecutor();
        }
        videoPlayerFrame = findViewById(R.id.video_surface);
        videoPlayerFrame.setVideoListener(new VideoPlayerListenerImpl(videoPlayerFrame));
        // 重新设置设备的ID
        MainActivity.id = FrequencyModule.OUTDOOR_SCREEN_VIDEO.getDeviceID();
        // 标识当前不在主页
        DataReadWriteUtil.inMainActivity = false;
        // 获取Fic解码器
        FicDecoder ficDecoder = FicDecoder.getInstance(MainActivity.id, true);
        // 重置一下Dangle
        UsbUtil.restDangle(ficDecoder, FrequencyModule.OUTDOOR_SCREEN_VIDEO);
    }

    /**
     * 这里开一个线程去执行 MPEG-TS 的解码任务<br/>
     * 已经解码的MPEG-TS流会被放进缓冲流
     */
    private void startMpegTsCodec() throws Exception {
        // 构造已解码的TS输入流
        pipedInputStream = new PipedInputStream(DEFAULT_MPEG_TS_PACKET_SIZE_DECODE * DEFAULT_MPEG_TS_STREAM_SIZE_TIMES);
        // 构造已解码的TS输出流
        pipedOutputStream = new PipedOutputStream();
        try {
            // 连接输入输出流
            pipedOutputStream.connect(pipedInputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 构造已解码的TS缓冲流
        bufferedInputStream = new BufferedInputStream(pipedInputStream);
        // 构造视频监听器,传入视频输出流以及回调类
        DmbListener videoPlayerListener = new DmbMpegListener(new VideoHandler(Looper.getMainLooper()), pipedOutputStream);
        // 构造解码器
        MpegTsDecoder mpegTsDecoder = new MpegTsDecoder(videoPlayerListener);
        // 开始解码
        executorService.submit(mpegTsDecoder);
    }

    /**
     * 播放视频
     */
    private void playVideo() {
        // 构造自定义的数据源
        DmbMediaDataSource dmbMediaDataSource = new DmbMediaDataSource(bufferedInputStream);
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
        // 结束
        // 直接中断 TS 解码器
        executorService.shutdownNow();
        // 关闭线程池中的任务
        videoPlayerFrame.release();
        closeStream();
        // 重置Dangle
        UsbUtil.dangleDestroy(this);
        super.onDestroy();
    }

    private void closeStream() {
        try {
            pipedOutputStream.close();
            pipedInputStream.close();
            bufferedInputStream.close();
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
            }
        }
    }
}
