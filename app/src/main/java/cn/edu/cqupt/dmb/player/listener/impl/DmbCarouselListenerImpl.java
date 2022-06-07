package cn.edu.cqupt.dmb.player.listener.impl;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Objects;
import java.util.Queue;

import cn.edu.cqupt.dmb.player.banner.bean.BannerBitmapDataBean;
import cn.edu.cqupt.dmb.player.common.DmbPlayerConstant;
import cn.edu.cqupt.dmb.player.listener.CarouselListener;
import cn.edu.cqupt.dmb.player.utils.GlideUtils;

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
    public static final String IMAGES_PATH = "/cn.edu.cqupt.dmb.player/images/";
    public static final String DAB_JPG_NAME = "dab.jpg";
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
     * 调用解码器的上下文
     */
    private final Context context;
    /**
     * 发送更新信号消息的计数器,cnt==5 的时候发送一次更新信号消息,发送之后清零
     */
    private int cnt = 0;
    /**
     * [备用]装载所有的已经解码的 TPEG 数据
     */
    private byte[] alternativeBytes;

    /**
     * 装载轮播图中已有的图片名字,用于去重
     */
    private final HashSet<String> loadImageNames = new HashSet<>();

    public DmbCarouselListenerImpl(Handler handler, Queue<BannerBitmapDataBean> bannerCache, Context context) {
        this.handler = handler;
        this.bannerCache = bannerCache;
        this.context = context;
    }

    @Override
    public void onSuccess(String fileName, byte[] bytes, int length) {
        cnt++;
        if (Objects.equals(fileName, DAB_JPG_NAME)) {
            // 重命名图片
            fileName = "dmb" + (length) + ".jpg";
            Log.i(TAG, "onSuccess: fileName 为 dab.jpg, 重命名为 dmb" + (length) + ".jpg");
        }
        System.arraycopy(bytes, 0, fileBuffer, 0, length);
        // 根据数据源生生成 bitmap
        Bitmap bitmap = BitmapFactory.decodeByteArray(fileBuffer, 0, length);
        // 构造 bannerBitmapDataBean
        BannerBitmapDataBean bannerBitmapDataBean = new BannerBitmapDataBean(bitmap, fileName, 1);
        if (bitmap != null) {
            // 判断是否已经加载过这一张图片了
            if (loadImageNames.contains(fileName)) {
                Log.i(TAG, "onSuccess: 跳过了一张重复的图片");
                return;
            }
            // 把文件名添加到 Set 集合中
            loadImageNames.add(fileName);
            Log.i(TAG, "onSuccess: 去重集合中加载了一张图片");
            // 添加到有界队列中
            bannerCache.add(bannerBitmapDataBean);
            // 添加一张轮播图之后,发送一次更新轮播图的消息
            Log.i(TAG, "onSuccess: 发送一次更新轮播图的消息");
            handler.sendEmptyMessage(MESSAGE_UPDATE_CAROUSEL);
        } else {
            Log.e(TAG, Thread.currentThread().getName() + "线程第一次生成 bitmap 错误啦!");
            // 构造缓存图片路径
            String imagePath = Environment.getExternalStorageDirectory().getAbsolutePath() + IMAGES_PATH;
            File imageDir = new File(imagePath);
            // 校验缓存图片路径是否存在
            if (!imageDir.exists()) {
                imageDir.mkdirs();
            }

            // 重命名图片
            fileName = "dmb" + (length + 35) + ".jpg";
            Log.i(TAG, "onSuccess: fileName 为 dab.jpg, 重命名为 dmb" + (length + 35) + ".jpg");
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
            bitmap = GlideUtils.loadBitMap(context, imagePath + fileName);
            if (bitmap == null) {
                Log.e(TAG, "onSuccess: 第二次生成 bitmap 还是出错了...");
            } else {
                Log.i(TAG, "onSuccess: 第二次生成 bitmap 成功");
                // 重新设置 bitmap
                bannerBitmapDataBean.setImageRes(bitmap);
                // 重新设置文件名
                bannerBitmapDataBean.setTitle(fileName);
                // 判断是否已经加载过这一张图片了
                if (loadImageNames.contains(fileName)) {
                    Log.i(TAG, "onSuccess: 跳过了一张重复的图片");
                }
                // 把文件名添加到 Set 集合中
                loadImageNames.add(fileName);
                Log.i(TAG, "onSuccess: 去重集合中加载了一张图片");
                // 添加到有界队列中
                bannerCache.add(bannerBitmapDataBean);
                // 添加一张轮播图之后,发送一次更新轮播图的消息
                Log.i(TAG, "onSuccess: 发送一次更新轮播图的消息");
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
