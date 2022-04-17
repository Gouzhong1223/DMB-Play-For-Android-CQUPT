package cn.edu.cqupt.dmb.player.actives;


import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import com.youth.banner.Banner;
import com.youth.banner.indicator.CircleIndicator;

import cn.edu.cqupt.dmb.player.R;
import cn.edu.cqupt.dmb.player.banner.adapter.BitmapAdapter;
import cn.edu.cqupt.dmb.player.banner.bean.BannerBitmapDataBean;
import cn.edu.cqupt.dmb.player.common.DmbPlayerConstant;
import cn.edu.cqupt.dmb.player.common.FrequencyModule;
import cn.edu.cqupt.dmb.player.decoder.FicDecoder;
import cn.edu.cqupt.dmb.player.decoder.TpegDecoder;
import cn.edu.cqupt.dmb.player.listener.DmbCarouselListener;
import cn.edu.cqupt.dmb.player.processor.dmb.DataProcessingFactory;
import cn.edu.cqupt.dmb.player.processor.dmb.PseudoBitErrorRateProcessor;
import cn.edu.cqupt.dmb.player.utils.DataReadWriteUtil;
import cn.edu.cqupt.dmb.player.utils.UsbUtil;

public class CarouselActivity extends FragmentActivity {

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
        DataReadWriteUtil.inMainActivity = false;
        initView();
        // 开始执行轮播图解码
        startDecodeTpeg();
    }

    private void startDecodeTpeg() {
        // 重新设置一下MainActivity.id的 ID,方便 FicDecoder 解码
        MainActivity.id = DataReadWriteUtil.getActiveFrequencyModule().getDeviceID();
        // 先重置一下 Dangle
        UsbUtil.restDangle(FicDecoder.getInstance(MainActivity.id, true), DataReadWriteUtil.getActiveFrequencyModule());
        // 开始执行 TPEG 解码的任务
        tpegDecoder = new TpegDecoder(new DmbCarouselListener(new CarouselHandler(Looper.getMainLooper())));
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

    @Override
    protected void onDestroy() {
        banner.stop();
        tpegDecoder.interrupt();
        UsbUtil.dangleDestroy(this);
        super.onDestroy();
    }

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
                    banner.setDatas(BannerBitmapDataBean.getListBitMapData());
                    banner.start();
                    // 更新之后重置计数器
                    cnt = 0;
                }
            } else if (msg.what == MESSAGE_UPDATE_SIGNAL) {
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
