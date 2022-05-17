package cn.edu.cqupt.dmb.player.listener;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.util.Log;

import java.util.Queue;

import cn.edu.cqupt.dmb.player.banner.bean.BannerBitmapDataBean;
import cn.edu.cqupt.dmb.player.common.DmbPlayerConstant;
import cn.edu.cqupt.dmb.player.utils.DmbUtil;

/**
 * @Author : Gouzhong
 * @Blog : www.gouzhong1223.com
 * @Description : 用于监听轮播图的监听器
 * @Date : create by QingSong in 2022-03-22 21:24
 * @Email : qingsong.qs@alibaba-inc.com
 * @Since : JDK 1.8
 * @PackageName : cn.edu.cqupt.dmb.player.listener
 * @ProjectName : DMB Player For Android
 * @Version : 1.0.0
 */
public class DmbCarouselListener implements DmbListener {

    private static final String TAG = "DmbCarouselListener";
    /**
     * 轮播图图片字节流
     */
    private final byte[] fileBuffer = new byte[1024 * 1024 * 10];

    private final Handler handler;
    /**
     * 处理更新轮播图的消息类型
     */
    private final Integer MESSAGE_UPDATE_CAROUSEL = DmbPlayerConstant.MESSAGE_UPDATE_CAROUSEL.getDmbConstantValue();
    /**
     * 监听信号更新的 message 类型
     */
    private final int MESSAGE_UPDATE_SIGNAL = DmbPlayerConstant.MESSAGE_UPDATE_SIGNAL.getDmbConstantValue();
    /**
     * 轮播图 FIFO 队列
     */
    private final Queue<BannerBitmapDataBean> bannerCache;
    /**
     * 发送更新信号消息的计数器,cnt==5 的时候发送一次更新信号消息,发送之后清零
     */
    private int cnt = 0;

    public DmbCarouselListener(Handler handler, Queue<BannerBitmapDataBean> bannerCache) {
        this.handler = handler;
        this.bannerCache = bannerCache;
    }

    @Override

    public void onSuccess(String fileName, byte[] bytes, int length) {
        cnt++;
        // 生成轮播图的文件名
        fileName = DmbUtil.CACHE_DIRECTORY + fileName;
        System.arraycopy(bytes, 0, fileBuffer, 0, length);
        // 根据数据源生生成 bitmap
        Bitmap bitmap = BitmapFactory.decodeByteArray(fileBuffer, 0, length);
        // 构造 bannerBitmapDataBean
        BannerBitmapDataBean bannerBitmapDataBean = new BannerBitmapDataBean(bitmap, fileName, 1);
        if (bitmap != null) {
            // 添加到有界队列中
            bannerCache.add(bannerBitmapDataBean);
            // 添加一张轮播图之后,发送一次更新轮播图的消息
            handler.sendEmptyMessage(MESSAGE_UPDATE_CAROUSEL);
        } else {
            Log.e(TAG, Thread.currentThread().getName() + "线程生成 bitmap 错误啦!");
        }
        if (cnt == 5) {
            // 如果现在计数器已经到 5 了,就发送一次更新信号的消息
            Log.i(TAG, Thread.currentThread().getName() + "线程发送了一次更新信号的消息");
            handler.sendEmptyMessage(MESSAGE_UPDATE_SIGNAL);
            // 发送之后重置计数器
            cnt = 0;
        }
    }

    @Override
    public void onReceiveMessage(String msg) {

    }
}
