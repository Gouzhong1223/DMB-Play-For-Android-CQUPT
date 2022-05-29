package cn.edu.cqupt.dmb.player.actives;


import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import com.google.common.collect.EvictingQueue;
import com.hdl.logcatdialog.LogcatDialog;
import com.youth.banner.Banner;
import com.youth.banner.indicator.CircleIndicator;

import java.util.ArrayList;
import java.util.Queue;

import cn.edu.cqupt.dmb.player.R;
import cn.edu.cqupt.dmb.player.banner.adapter.BitmapAdapter;
import cn.edu.cqupt.dmb.player.banner.adapter.ImageAdapter;
import cn.edu.cqupt.dmb.player.banner.bean.BannerBitmapDataBean;
import cn.edu.cqupt.dmb.player.banner.bean.BannerDataBean;
import cn.edu.cqupt.dmb.player.common.DmbPlayerConstant;
import cn.edu.cqupt.dmb.player.decoder.TpegDecoder;
import cn.edu.cqupt.dmb.player.listener.impl.DmbCarouselListener;
import cn.edu.cqupt.dmb.player.processor.dmb.DataProcessingFactory;
import cn.edu.cqupt.dmb.player.processor.dmb.PseudoBitErrorRateProcessor;

/**
 * @author qingsong
 */
public class CarouselActivity extends BaseActivity {

    private static final String TAG = "CarouselActivity";
    /**
     * 处理更新轮播图的消息类型
     */
    private final int MESSAGE_UPDATE_CAROUSEL = DmbPlayerConstant.MESSAGE_UPDATE_CAROUSEL.getDmbConstantValue();
    /**
     * 监听信号更新的 message 类型
     */
    private final int MESSAGE_UPDATE_SIGNAL = DmbPlayerConstant.MESSAGE_UPDATE_SIGNAL.getDmbConstantValue();
    /**
     * 轮播图缓存
     */
    Queue<BannerBitmapDataBean> bannerCache;
    /**
     * 轮播图组件
     */
    private Banner banner;
    /**
     * 信号组件
     */
    private ImageView signalImageView;

    /**
     * 开始解码TPEG
     */
    public void startDecode() {
        // 构造轮播图缓存
        bannerCache = EvictingQueue.create(Math.toIntExact(defaultCarouselNumSetting.getSettingValue()));
        // 构造TPEG解码器
        TpegDecoder tpegDecoder = new TpegDecoder(new DmbCarouselListener(new CarouselHandler(Looper.getMainLooper()), bannerCache, this), this, bufferedInputStream);
        // 开始执行 TPEG 解码的任务
        tpegDecoder.start();
    }

    /**
     * 初始化组件
     */
    @Override
    public void initView() {
        banner = findViewById(R.id.banner);
        useBanner();
        // 初始化轮播图中的信号显示组件
        signalImageView = findViewById(R.id.carousel_signal);
    }

    @Override
    public void configView() {
        if (defaultSignalShowSetting != null) {
            int showSignal = Math.toIntExact(defaultSignalShowSetting.getSettingValue());
            signalImageView.setVisibility(showSignal == 0 ? View.INVISIBLE : View.VISIBLE);
        }
        if (showDebugLogSetting != null) {
            int showLog = Math.toIntExact(showDebugLogSetting.getSettingValue());
            if (showLog == 1) {
                runOnUiThread(() -> new LogcatDialog(CarouselActivity.this).show());
            }
        }
    }

    /**
     * 播放轮播图
     */
    public void useBanner() {
        //添加生命周期观察者
        banner.addBannerLifecycleObserver(this).setAdapter(new ImageAdapter(BannerDataBean.getHelloViewData())).setIndicator(new CircleIndicator(this));
    }

    @Override
    protected void onDestroy() {
        if (banner != null) {
            banner.stop();
        }
        super.onDestroy();
    }

    /**
     * 自定义回调器
     */
    private class CarouselHandler extends Handler {
        /**
         * 计数器
         */
        private int cnt = 0;

        public CarouselHandler(@NonNull Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            // 更新轮播图的消息
            if (msg.what == MESSAGE_UPDATE_CAROUSEL) {
                cnt++;
                // 收到三次消息之后才更新一次轮播图,避免性能消耗
                if (cnt == 3) {
                    banner.stop();
                    banner.addBannerLifecycleObserver(CarouselActivity.this).setAdapter(new BitmapAdapter(new ArrayList<>(bannerCache))).setIndicator(new CircleIndicator(CarouselActivity.this)).start();
                    banner.start();
                    // 更新之后重置计数器
                    cnt = 0;
                }
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
