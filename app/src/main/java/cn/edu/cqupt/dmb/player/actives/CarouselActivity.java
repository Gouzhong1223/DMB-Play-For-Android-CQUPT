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
import cn.edu.cqupt.dmb.player.banner.adapter.ImageAdapter;
import cn.edu.cqupt.dmb.player.banner.bean.BannerDataBean;
import cn.edu.cqupt.dmb.player.processor.DataProcessingFactory;
import cn.edu.cqupt.dmb.player.processor.PseudoBitErrorRateProcessor;

public class CarouselActivity extends FragmentActivity {

    private static final String TAG = "CarouselActivity";
    private final ScheduledExecutorService scheduledExecutorService = new ScheduledThreadPoolExecutor(1);

    private Banner banner;
    private ImageView signalImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_carousel);
        initView();
        // 执行定时更新信号图片的任务
        updateSignalImage();
    }

    private void initView() {
        banner = findViewById(R.id.banner);
        useBanner();
        // 初始化轮播图中的信号显示组件
        findViewById(R.id.carousel_signal);
    }

    /**
     * 播放轮播图
     */
    public void useBanner() {
        //添加生命周期观察者
        banner.addBannerLifecycleObserver(this)
                .setAdapter(new ImageAdapter(BannerDataBean.getTestData()))
                .setIndicator(new CircleIndicator(this)).start();
    }

    private void updateSignalImage() {
        // 先延迟5秒，然后每5秒获取一次信号值,然后更新
        scheduledExecutorService.scheduleAtFixedRate(() -> {
            PseudoBitErrorRateProcessor pseudoBitErrorRateProcessor = (PseudoBitErrorRateProcessor) DataProcessingFactory.getDataProcessor(0x00);
            int ber = pseudoBitErrorRateProcessor.getBer();
            if (ber > 200) {
                Log.i(TAG, "ber = " + ber);
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
        }, 5000L, 5000L, TimeUnit.SECONDS);
    }

    @Override
    protected void onDestroy() {
        // 如果activity被关闭了就应该立马销毁线程池并且终止正在运行的线程
        scheduledExecutorService.shutdownNow();
        super.onDestroy();
    }
}
