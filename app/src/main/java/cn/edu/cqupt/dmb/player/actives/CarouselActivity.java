package cn.edu.cqupt.dmb.player.actives;


import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import androidx.fragment.app.FragmentActivity;

import com.youth.banner.Banner;
import com.youth.banner.indicator.CircleIndicator;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import cn.edu.cqupt.dmb.player.R;
import cn.edu.cqupt.dmb.player.banner.adapter.BitmapAdapter;
import cn.edu.cqupt.dmb.player.banner.bean.BannerBitmapDataBean;
import cn.edu.cqupt.dmb.player.banner.bean.BannerImageBitmapCache;
import cn.edu.cqupt.dmb.player.common.FrequencyModule;
import cn.edu.cqupt.dmb.player.decoder.FicDecoder;
import cn.edu.cqupt.dmb.player.decoder.TpegDecoder;
import cn.edu.cqupt.dmb.player.listener.DmbTpegListener;
import cn.edu.cqupt.dmb.player.processor.dmb.DataProcessingFactory;
import cn.edu.cqupt.dmb.player.processor.dmb.PseudoBitErrorRateProcessor;
import cn.edu.cqupt.dmb.player.utils.DataReadWriteUtil;
import cn.edu.cqupt.dmb.player.utils.UsbUtil;

public class CarouselActivity extends FragmentActivity {

    private static final String TAG = "CarouselActivity";
    /**
     * 线程池中有两个线程,一个是定时更新信号的线程,一个是定时更新轮播图的线程
     */
    private final ScheduledExecutorService scheduledExecutorService = new ScheduledThreadPoolExecutor(2);

    /**
     * 轮播图组件
     */
    private Banner banner;
    /**
     * 信号组件
     */
    private ImageView signalImageView;
    /**
     * 轮播图解码器
     */
    private TpegDecoder tpegDecoder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_carousel);
        // 进入到轮播图组件之后,首先将活跃的工作模块设置成轮播图
        DataReadWriteUtil.setActiveFrequencyModule(FrequencyModule.OUTDOOR_SCREEN_TPEG);
        initView();
        // 开始执行轮播图解码
        startDecodeTpeg();
        // 执行定时更新信号图片的任务
        updateSignalImage();
        // 执行更新轮播图的任务
        updateCarouselImage();
    }

    private void startDecodeTpeg() {
        // 重新设置一下MainActivity.id的 ID,方便 FicDecoder 解码
        MainActivity.id = DataReadWriteUtil.getActiveFrequencyModule().getDeviceID();
        // 先重置一下 Dangle
        UsbUtil.restDangle(FicDecoder.getInstance(MainActivity.id, true), DataReadWriteUtil.getActiveFrequencyModule());
        // 开始执行 TPEG 解码的任务
        tpegDecoder = new TpegDecoder(new DmbTpegListener());
        tpegDecoder.start();
    }

    private void initView() {
        banner = findViewById(R.id.banner);
        useBanner();
        // 初始化轮播图中的信号显示组件
        signalImageView = findViewById(R.id.carousel_signal);
    }

    /**
     * 播放轮播图
     */
    public void useBanner() {
        //添加生命周期观察者
        banner.addBannerLifecycleObserver(this)
                .setAdapter(new BitmapAdapter(BannerBitmapDataBean.getListBitMapData()))
                .setIndicator(new CircleIndicator(this)).start();
    }

    /**
     * 更新轮播图图片,现在的设置是启动之后延迟 5 秒,然后每 30 秒更新一次轮播图
     */
    private void updateCarouselImage() {
        scheduledExecutorService.scheduleAtFixedRate(() -> {
            // 如果当前的 bitmap 缓存中有数据,就先暂停当前的轮播图,然后重新设置轮播图资源再开始
            if (BannerImageBitmapCache.getBannerCache().size() != 0) {
                banner.setDatas(BannerBitmapDataBean.getListBitMapData());
            }
        }, 0L, 3L, TimeUnit.SECONDS);
    }

    /**
     * 定时更新信号的线程,现在的设置是启动之后延迟 5 秒然后每 5 秒更新一次信号
     */
    private void updateSignalImage() {
        // 先延迟5秒，然后每5秒获取一次信号值,然后每5秒更新一次信号
        scheduledExecutorService.scheduleAtFixedRate(() -> {
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
        }, 0L, 5L, TimeUnit.SECONDS);
    }

    @Override
    protected void onDestroy() {
        // 如果activity被关闭了就应该立马销毁线程池并且终止正在运行的线程
        scheduledExecutorService.shutdownNow();
        banner.stop();
        tpegDecoder.interrupt();
        // 获取系统默认的工作模块
        FrequencyModule defaultFrequencyModule = DataReadWriteUtil.getDefaultFrequencyModule(this);
        // 退出组件应该将活跃模块设置为系统默认工作模块
        DataReadWriteUtil.setActiveFrequencyModule(defaultFrequencyModule);
        // 结束之后将 ID 设置成默认的场景 ID
        MainActivity.id = defaultFrequencyModule.getDeviceID();
        // 重置一下 Dangle
        UsbUtil.restDangle(FicDecoder.getInstance(MainActivity.id, true), defaultFrequencyModule);
        super.onDestroy();
    }
}
