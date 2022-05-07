package cn.edu.cqupt.dmb.player.listener;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.os.Handler;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.util.HashSet;

import cn.edu.cqupt.dmb.player.banner.bean.BannerBitmapDataBean;
import cn.edu.cqupt.dmb.player.banner.bean.CarouselBannerImageBitmapCache;
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
    private final byte[] fileBuffer = new byte[1024 * 1024 * 15];
    /**
     * 回调处理器
     */
    private final Handler handler;
    /**
     * 处理更新轮播图的消息类型
     */
    private final Integer MESSAGE_UPDATE_CAROUSEL = DmbPlayerConstant.MESSAGE_UPDATE_CAROUSEL.getDmbConstantValue();
    /**
     * 监听信号更新的 message 类型
     */
    private final int MESSAGE_UPDATE_SIGNAL = DmbPlayerConstant.MESSAGE_UPDATE_SIGNAL.getDmbConstantValue();
    private final HashSet<String> loadImage = new HashSet<>();
    /**
     * 发送更新信号消息的计数器,cnt==5 的时候发送一次更新信号消息,发送之后清零
     */
    private int cnt = 0;

    public DmbCarouselListener(Handler handler) {
        this.handler = handler;
    }

    @Override

    public void onSuccess(String fileName, byte[] bytes, int length, Context context) {
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
            Log.i(TAG, "onSuccess: 放了一张 bitmap 到缓存里面去");
            if (!loadImage.contains(bannerBitmapDataBean.getTitle())) {
                CarouselBannerImageBitmapCache.putBitMap(bannerBitmapDataBean);
                loadImage.add(bannerBitmapDataBean.getTitle());
                // 添加一张轮播图之后,发送一次更新轮播图的消息
                handler.sendEmptyMessage(MESSAGE_UPDATE_CAROUSEL);
                handler.sendEmptyMessage(0x56);
            }
        } else {
            Log.e(TAG, "onSuccess: 不兼容的图片!");
            Log.e(TAG, Thread.currentThread().getName() + "线程生成 bitmap 错误啦!");
            YuvImage yuvimage = new YuvImage(fileBuffer, ImageFormat.NV21, 20, 20, null);//20、20分别是图的宽度与高度
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            yuvimage.compressToJpeg(new Rect(0, 0, 20, 20), 80, byteArrayOutputStream);//80--JPG图片的质量[0-100],100最高
            byte[] byteArrays = byteArrayOutputStream.toByteArray();
            bitmap = BitmapFactory.decodeByteArray(byteArrays, 0, byteArrays.length);
            if (bitmap == null) {
                Log.e(TAG, "onSuccess: 生成 bitmap 还是失败了");
            } else {
                Log.i(TAG, "onSuccess: " + bitmap.getByteCount());
                // 添加到有界队列中
                Log.i(TAG, "onSuccess: 放了一张 bitmap 到缓存里面去");
                CarouselBannerImageBitmapCache.putBitMap(bannerBitmapDataBean);
                // 添加一张轮播图之后,发送一次更新轮播图的消息
                handler.sendEmptyMessage(MESSAGE_UPDATE_CAROUSEL);
                handler.sendEmptyMessage(0x88);
            }
        }
        if (cnt == 5) {
            // 如果现在计数器已经到 5 了,就发送一次更新信号的消息
            Log.i(TAG, Thread.currentThread().getName() + "线程发送了一次更新信号的消息");
            handler.sendEmptyMessage(MESSAGE_UPDATE_SIGNAL);
            // 清空缓存
            loadImage.clear();
            // 发送之后重置计数器
            cnt = 0;
        }
    }

    @Override
    public void onReceiveMessage(String msg) {

    }
}
