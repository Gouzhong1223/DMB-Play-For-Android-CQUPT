package cn.edu.cqupt.dmb.player.actives;


import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.concurrent.TimeUnit;

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
 * 这个是播放视频的 Activity
 * 原则上逻辑应该是这样的,当用户点击到这个组件之后,用于解码 MPEG-TS 的解码器线程才开始工作,会有一个同步
 * 也就是解码器已经生成了一点临时文件,也生成了临时的文件名,才开始执行播放的任务
 */
@RequiresApi(api = Build.VERSION_CODES.R)
public class VideoActivity extends Activity {

    private static final String TAG = "VideoActivity";
    /**
     * 自定义的视频播放组件
     */
    private VideoPlayerFrame videoPlayerFrame = null;

    /**
     * MPEG-TS解码器
     */
    private MpegTsDecoder mpegTsDecoder;

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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);
        // 初始化View
        initView();
        // 开始MPEG-TS解码
        startMpegTsCodec();
    }

    /**
     * 初始化组件
     */
    private void initView() {
        videoPlayerFrame = findViewById(R.id.video_surface);
        videoPlayerFrame.setVideoListener(new VideoPlayerListenerImpl(videoPlayerFrame));
        // 重新设置设备的ID
        MainActivity.id = FrequencyModule.OUTDOOR_SCREEN_VIDEO.getDeviceID();
        // 过去Fic解码器
        FicDecoder ficDecoder = FicDecoder.getInstance(MainActivity.id, true);
        // 重置一下Dangle
        UsbUtil.restDangle(ficDecoder, FrequencyModule.OUTDOOR_SCREEN_VIDEO);
    }

    /**
     * 这里开一个线程去执行 MPEG-TS 的解码任务<br/>
     * 已经解码的MPEG-TS流会被放进缓冲流
     */
    private void startMpegTsCodec() {
        // 构造已解码的TS输入流
        pipedInputStream = new PipedInputStream(1024 * 10);
        // 构造已解码的TS缓冲流
        bufferedInputStream = new BufferedInputStream(pipedInputStream);
        // 构造已解码的TS输出流
        pipedOutputStream = new PipedOutputStream();
        try {
            // 连接输入输出流
            pipedOutputStream.connect(pipedInputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 构造视频监听器,传入视频输出流以及回调类
        DmbListener videoPlayerListener = new DmbMpegListener(new VideoHandler(Looper.getMainLooper()), pipedOutputStream);
        // 构造解码器
        mpegTsDecoder = new MpegTsDecoder(videoPlayerListener);
        // 开始解码
        mpegTsDecoder.start();
    }

    /**
     * 播放视频
     */
    private void playVideo() {
        // 现在已经设置了临时文件的名字了,但是要 sleep 一会儿,不然播放器直接播放会黑屏
        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException e) {
            Log.e(TAG, "在等待 TS 流写入缓冲区的时候出错啦!");
            Toast.makeText(this, "在等待 TS 流写入缓冲区的时候出错啦!", Toast.LENGTH_SHORT).show();
            onDestroy();
            e.printStackTrace();
        }
        // 获取临时文件名
        String temporaryMpegTsVideoFilename = DataReadWriteUtil.getTemporaryMpegTsVideoFilename();
        if (temporaryMpegTsVideoFilename.equals("")) {
            Log.e(TAG, "视频临时文件名还没有准备好,播放出错啦!");
            return;
        }
        DmbMediaDataSource dmbMediaDataSource = new DmbMediaDataSource(bufferedInputStream);
        videoPlayerFrame.setDataSource(dmbMediaDataSource);
        try {
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
        mpegTsDecoder.interrupt();
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
        public VideoHandler(@NonNull Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            if (msg.what == MESSAGE_START_PLAY_VIDEO) {
                // 临时文件已经生成,开始播放视频
                playVideo();
            }
        }
    }
}
