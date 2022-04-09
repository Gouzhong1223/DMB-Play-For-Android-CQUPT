package cn.edu.cqupt.dmb.player.actives;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import cn.edu.cqupt.dmb.player.R;
import cn.edu.cqupt.dmb.player.banner.bean.BannerBitmapDataBean;
import cn.edu.cqupt.dmb.player.banner.bean.CurriculumImageCache;
import cn.edu.cqupt.dmb.player.decoder.FicDecoder;
import cn.edu.cqupt.dmb.player.decoder.TpegDecoder;
import cn.edu.cqupt.dmb.player.listener.DmbCurriculumListener;
import cn.edu.cqupt.dmb.player.utils.DataReadWriteUtil;
import cn.edu.cqupt.dmb.player.utils.UsbUtil;

/**
 * 这个是显示课表的 Activity
 */
public class CurriculumActivity extends Activity {

    private static final String TAG = "CurriculumActivity";

    private final Object WAIT_IMAGE_LOCK_OBJECT = new Object();
    /**
     * 显示课表的组件
     */
    private ImageView imageView;
    /**
     * 解码课表 TPEG 的线程
     */
    private TpegDecoder tpegDecoder;

    /**
     * 执行更新课表图片的线程池
     */
    private ScheduledExecutorService scheduledExecutorService = new ScheduledThreadPoolExecutor(1);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_curriculum);
        DataReadWriteUtil.inMainActivity = false;
        initView();
        // 开始解码 TPEG 生成 TPEG
        startDecodeCurriculum();
        startUpdateCurriculum();
    }

    private void startUpdateCurriculum() {


        if (scheduledExecutorService.isShutdown()) {
            // 如果线程池已经Shutdown,就重新初始化一个
            scheduledExecutorService = new ScheduledThreadPoolExecutor(1);
        }
        scheduledExecutorService.scheduleAtFixedRate(() -> {
            synchronized (WAIT_IMAGE_LOCK_OBJECT) {
                while (CurriculumImageCache.getBannerCache().peek() == null) {
                    try {
                        Log.i(TAG, "现在课表图片缓存是空的,放弃锁");
                        WAIT_IMAGE_LOCK_OBJECT.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                BannerBitmapDataBean bannerBitmapDataBean = CurriculumImageCache.getBannerCache().poll();
                // 唤醒监听器重新放置图片
                Log.i(TAG, "现在从缓存中取了一张图片出来,唤醒监听器放置图片");
                WAIT_IMAGE_LOCK_OBJECT.notifyAll();
                if (bannerBitmapDataBean != null) {
                    Log.i(TAG, "更新了课表");
                    imageView.setImageBitmap(bannerBitmapDataBean.getImageRes());
                }

            }
        }, 15L, 5L, TimeUnit.SECONDS);
    }

    /**
     * 初始化 View
     */
    private void initView() {
        Log.i(TAG, "正在初始化课表显示组件");
        imageView = findViewById(R.id.curriculum_image_view);
    }

    /**
     * 开始执行解码线程
     */
    private void startDecodeCurriculum() {
        // 重新设置一下MainActivity.id的 ID,方便 FicDecoder 解码
        MainActivity.id = DataReadWriteUtil.getActiveFrequencyModule().getDeviceID();
        // 先重置一下 Dangle
        UsbUtil.restDangle(FicDecoder.getInstance(MainActivity.id, true), DataReadWriteUtil.getActiveFrequencyModule());
        DmbCurriculumListener dmbCurriculumListener = new DmbCurriculumListener(WAIT_IMAGE_LOCK_OBJECT);
        tpegDecoder = new TpegDecoder(dmbCurriculumListener);
        tpegDecoder.start();
    }

    @Override
    protected void onDestroy() {
        // 中断解码线程
        tpegDecoder.interrupt();
        // 停止线程池中正在执行的线程
        scheduledExecutorService.shutdownNow();
        // 销毁一下 dangle 的设置
        UsbUtil.dangleDestroy(this);
        super.onDestroy();
    }
}
