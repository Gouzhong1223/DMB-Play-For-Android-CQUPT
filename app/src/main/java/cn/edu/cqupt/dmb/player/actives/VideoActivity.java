package cn.edu.cqupt.dmb.player.actives;


import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.room.Room;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import cn.edu.cqupt.dmb.player.R;
import cn.edu.cqupt.dmb.player.common.CustomSettingByKey;
import cn.edu.cqupt.dmb.player.common.DmbPlayerConstant;
import cn.edu.cqupt.dmb.player.db.database.CustomSettingDatabase;
import cn.edu.cqupt.dmb.player.db.mapper.CustomSettingMapper;
import cn.edu.cqupt.dmb.player.decoder.FicDecoder;
import cn.edu.cqupt.dmb.player.decoder.MpegTsDecoder;
import cn.edu.cqupt.dmb.player.domain.CustomSetting;
import cn.edu.cqupt.dmb.player.domain.SceneVO;
import cn.edu.cqupt.dmb.player.frame.DmbMediaDataSource;
import cn.edu.cqupt.dmb.player.frame.VideoPlayerFrame;
import cn.edu.cqupt.dmb.player.listener.DmbListener;
import cn.edu.cqupt.dmb.player.listener.DmbMpegListener;
import cn.edu.cqupt.dmb.player.listener.VideoPlayerListenerImpl;
import cn.edu.cqupt.dmb.player.processor.dmb.DataProcessingFactory;
import cn.edu.cqupt.dmb.player.processor.dmb.PseudoBitErrorRateProcessor;
import cn.edu.cqupt.dmb.player.utils.DataReadWriteUtil;
import cn.edu.cqupt.dmb.player.utils.UsbUtil;


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
@RequiresApi(api = Build.VERSION_CODES.R)
public class VideoActivity extends Activity {

    /**
     * 视频播放回调消息
     */
    public static final int MESSAGE_START_PLAY_VIDEO = DmbPlayerConstant.MESSAGE_START_PLAY_VIDEO.getDmbConstantValue();
    /**
     * 监听信号更新的 message 类型
     */
    private final int MESSAGE_UPDATE_SIGNAL = DmbPlayerConstant.MESSAGE_UPDATE_SIGNAL.getDmbConstantValue();
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
     * 当前选择打开视频的场景设置
     */
    private SceneVO selectedSceneVO;

    /**
     * 操作自定义设置的 Mapper
     */
    private CustomSettingMapper customSettingMapper;

    /**
     * 是否显示信号的设置
     */
    private CustomSetting defaultSignalShowSetting;
    /**
     * PIP 输出流
     */
    private PipedOutputStream pipedOutputStream;
    /**
     * 输入缓冲流
     */
    private BufferedInputStream bufferedInputStream;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 强制全屏,全的不能再全的那种了
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_video);
        // 获取父传递过来的参数
        selectedSceneVO = (SceneVO) this.getIntent().getSerializableExtra(DetailsActivity.SCENE_VO);
        // 初始化数据库 Mapper
        initDataBase();
        // 加载自定义设置
        loadCustomSetting();
        initPip();
        // 初始化View
        initView();
        // 开始接收 DMB 数据
        UsbUtil.startReceiveDmbData(pipedOutputStream);
        // 开始MPEG-TS解码
        try {
            startMpegTsCodec();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 初始化管道流
     */
    private void initPip() {
        PipedInputStream pipedInputStream = new PipedInputStream(1024 * 1024 * 10);
        pipedOutputStream = new PipedOutputStream();
        try {
            pipedOutputStream.connect(pipedInputStream);
            bufferedInputStream = new BufferedInputStream(pipedInputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 初始化数据库
     */
    private void initDataBase() {
        //new a database
        customSettingMapper = Room.databaseBuilder(this, CustomSettingDatabase.class, "custom_setting_database")
                .allowMainThreadQueries().build().getCustomSettingMapper();
    }

    /**
     * 加载自定义设置
     */
    private void loadCustomSetting() {
        // 查询信号显示设置
        defaultSignalShowSetting = customSettingMapper.selectCustomSettingByKey(CustomSettingByKey.OPEN_SIGNAL.getKey());
    }

    /**
     * 初始化组件
     */
    private void initView() {
        videoPlayerFrame = findViewById(R.id.video_surface);
        videoPlayerFrame.setVideoListener(new VideoPlayerListenerImpl(videoPlayerFrame));
        signalImageView = findViewById(R.id.video_signal);
        if (defaultSignalShowSetting != null) {
            int showSignal = Math.toIntExact(defaultSignalShowSetting.getSettingValue());
            signalImageView.setVisibility(showSignal == 0 ? View.INVISIBLE : View.VISIBLE);
        }
        // 获取Fic解码器
        FicDecoder ficDecoder = FicDecoder.getInstance(selectedSceneVO.getDeviceId(), true);
        // 重置一下Dangle
        UsbUtil.restDangle(ficDecoder, selectedSceneVO);
    }

    /**
     * 这里开一个线程去执行 MPEG-TS 的解码任务<br/>
     * 已经解码的MPEG-TS流会被放进缓冲流
     */
    private void startMpegTsCodec() throws Exception {
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
        // 构造视频监听器,传入视频输出流以及回调类
        DmbListener videoPlayerListener = new DmbMpegListener(new VideoHandler(Looper.getMainLooper()), mpegTsPipedOutputStream);
        // 构造解码器
        MpegTsDecoder mpegTsDecoder = new MpegTsDecoder(videoPlayerListener, this, bufferedInputStream);
        mpegTsDecoder.start();
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
        videoPlayerFrame.release();
        closeStream();
        DataReadWriteUtil.inMainActivity = true;
        super.onDestroy();
    }

    private void closeStream() {
        try {
            mpegTsPipedOutputStream.close();
            mpegTsPipedInputStream.close();
            mPegTsBufferedInputStream.close();
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
            }
        }
    }
}
