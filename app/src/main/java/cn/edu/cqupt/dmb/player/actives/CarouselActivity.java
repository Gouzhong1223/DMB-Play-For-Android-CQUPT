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
import cn.edu.cqupt.dmb.player.banner.adapter.ImageAdapter;
import cn.edu.cqupt.dmb.player.banner.bean.BannerBitmapDataBean;
import cn.edu.cqupt.dmb.player.banner.bean.BannerDataBean;
import cn.edu.cqupt.dmb.player.banner.bean.BannerImageBitmapCache;
import cn.edu.cqupt.dmb.player.processor.dmb.DataProcessingFactory;
import cn.edu.cqupt.dmb.player.processor.dmb.PseudoBitErrorRateProcessor;

public class CarouselActivity extends FragmentActivity {

    private static final String TAG = "CarouselActivity";
    // 线程池中有两个线程,一个是定时更新信号的线程,一个是定时更新轮播图的线程
    private final ScheduledExecutorService scheduledExecutorService = new ScheduledThreadPoolExecutor(2);

    private Banner banner;
    private ImageView signalImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_carousel);
        initView();
        // 执行定时更新信号图片的任务
        updateSignalImage();
        // 执行更新轮播图的任务
        updateCarouselImage();
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
                .setAdapter(new ImageAdapter(BannerDataBean.getHelloViewData()))
                .setIndicator(new CircleIndicator(this)).start();
    }

    /**
     * 更新轮播图图片,现在的设置是启动之后延迟 5 秒,然后每 30 秒更新一次轮播图
     */
    private void updateCarouselImage() {
        scheduledExecutorService.scheduleAtFixedRate(() -> {
            // 如果当前的 bitmap 缓存中有数据,就先暂停当前的轮播图,然后重新设置轮播图资源再开始
            if (BannerImageBitmapCache.getBannerCache().size() != 0) {
                Log.i(TAG, "现在已经有 bitmap 数据了,重新设置一下轮播图");
                banner.stop();
                banner.addBannerLifecycleObserver(this)
                        .setAdapter(new BitmapAdapter(BannerBitmapDataBean.getListBitMapData()))
                        .setIndicator(new CircleIndicator(this)).start();
            }
        }, 0L, 15L, TimeUnit.SECONDS);
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
        }, 5L, 5L, TimeUnit.SECONDS);
    }

    @Override
    protected void onDestroy() {
        // 如果activity被关闭了就应该立马销毁线程池并且终止正在运行的线程
        scheduledExecutorService.shutdownNow();
        banner.stop();
        super.onDestroy();
    }
}
