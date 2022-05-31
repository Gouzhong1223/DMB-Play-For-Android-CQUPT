package cn.edu.cqupt.dmb.player.listener.impl;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;

import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Queue;

import cn.edu.cqupt.dmb.player.banner.bean.BannerBitmapDataBean;
import cn.edu.cqupt.dmb.player.common.DmbPlayerConstant;
import cn.edu.cqupt.dmb.player.listener.CarouselListener;

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
public class DmbCarouselListenerImpl implements CarouselListener {

    private static final String TAG = "DmbCarouselListenerImpl";
    /**
     * 轮播图图片字节流
     */
    private final byte[] fileBuffer = new byte[1024 * 1024 * 10];
    /**
     * 自定义回调
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
    /**
     * 轮播图 FIFO 队列
     */
    private final Queue<BannerBitmapDataBean> bannerCache;
    /**
     * 发送更新信号消息的计数器,cnt==5 的时候发送一次更新信号消息,发送之后清零
     */
    private int cnt = 0;
    /**
     * [备用]装载所有的已经解码的 TPEG 数据
     */
    private byte[] alternativeBytes;

    public DmbCarouselListenerImpl(Handler handler, Queue<BannerBitmapDataBean> bannerCache) {
        this.handler = handler;
        this.bannerCache = bannerCache;
    }

    @Override
    public void onSuccess(String fileName, byte[] bytes, int length) {
        cnt++;
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
            // 构造缓存图片路径
            String imagePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/cn.edu.cqupt.dmb.player/images/";
            File imageDir = new File(imagePath);
            // 校验缓存图片路径是否存在
            if (!imageDir.exists()) {
                imageDir.mkdirs();
            }

            // 重命名图片
            fileName = "dmb.jpg";
            try {
                // 构造输出流
                FileOutputStream fileOutputStream = new FileOutputStream(imagePath + fileName);
                // 把图片输入流写到输出流(使用备用数组)
                fileOutputStream.write(alternativeBytes, 0, length + 35);
                fileOutputStream.flush();
                // 关闭输出流
                fileOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            // 重新加载 bitmap
            ImageLoader imageLoader = ImageLoader.getInstance();
            bitmap = imageLoader.loadImageSync(imagePath + fileName);
            if (bitmap == null) {
                Log.i(TAG, "onSuccess: 第二次生成 bitmap 还是出错了...");
            } else {
                Log.i(TAG, "onSuccess: 第二次生成 bitmap 成功");
                // 重新设置 bitmap
                bannerBitmapDataBean.setImageRes(bitmap);
                // 添加到有界队列中
                bannerCache.add(bannerBitmapDataBean);
                // 添加一张轮播图之后,发送一次更新轮播图的消息
                handler.sendEmptyMessage(MESSAGE_UPDATE_CAROUSEL);
            }
            // 删除缓存的文件
            new File(imagePath + fileName).delete();
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
        Log.i(TAG, "onReceiveMessage: 空实现");
    }

    @Override
    public void onSuccess(String fileName, byte[] bytes, int length, byte[] alternativeBytes) {
        this.alternativeBytes = alternativeBytes;
        this.onSuccess(fileName, bytes, length);
    }
}
