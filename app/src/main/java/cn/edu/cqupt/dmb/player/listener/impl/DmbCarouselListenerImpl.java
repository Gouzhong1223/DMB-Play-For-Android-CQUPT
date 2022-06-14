/*
 *
 *              Copyright 2022 By Gouzhong1223
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 */

package cn.edu.cqupt.dmb.player.listener.impl;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.common.collect.EvictingQueue;
import com.youth.banner.bean.BannerBitmapDataBean;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Objects;

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

    public static final String IMAGES_PATH = "/cn.edu.cqupt.dmb.player/images/";
    public static final String DAB_JPG_NAME = "dab.jpg";
    private static final String TAG = "DmbCarouselListenerImpl";
    /**
     * 轮播图图片字节流
     */
    private final byte[] fileBuffer = new byte[1024 * 1024 * 3];
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
    private final EvictingQueue<BannerBitmapDataBean> bannerCache;
    /**
     * 调用解码器的上下文
     */
    private final Context context;
    /**
     * 装载轮播图中已有的图片名字,用于去重
     */
    private final HashSet<String> loadImageNames = new HashSet<>();
    /**
     * 发送更新信号消息的计数器,cnt==5 的时候发送一次更新信号消息,发送之后清零
     */
    private int cnt = 0;
    /**
     * [备用]装载所有的已经解码的 TPEG 数据
     */
    private byte[] alternativeBytes;

    /**
     * 构造方法
     *
     * @param handler     自定义回调
     * @param bannerCache 轮播图 FIFO 队列
     * @param context     调用解码器的上下文
     */
    public DmbCarouselListenerImpl(Handler handler, EvictingQueue<BannerBitmapDataBean> bannerCache, Context context) {
        this.handler = handler;
        this.bannerCache = bannerCache;
        this.context = context;
    }

    @Override
    public void onSuccess(String fileName, byte[] bytes, int length) {
        // 计数器自增
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
            // 接收图片
            acceptImage(fileName, bannerBitmapDataBean);
        } else {
            Log.e(TAG, Thread.currentThread().getName() + "线程第一次生成 bitmap 错误啦!");
            // 构造文件路径
            String imagePath = generateFilePath();
            // 重命名图片
            fileName = "dmb" + (length + 35) + ".jpg";
            Log.i(TAG, "onSuccess: fileName 为 dab.jpg, 重命名为 dmb" + (length + 35) + ".jpg");
            // 写出文件流
            writeImageSource(fileName, length, imagePath);
            // 重新加载图片
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
                // 接收图片
                acceptImage(fileName, bannerBitmapDataBean);
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

    /**
     * 生成文件路径
     *
     * @return 文件路径
     */
    @NonNull
    private String generateFilePath() {
        // 构造缓存图片路径
        String imagePath = Environment.getExternalStorageDirectory().getAbsolutePath() + IMAGES_PATH;
        File imageDir = new File(imagePath);
        // 校验缓存图片路径是否存在
        if (!imageDir.exists()) {
            imageDir.mkdirs();
        }
        return imagePath;
    }

    /**
     * 把文件写出到流
     *
     * @param fileName  文件名
     * @param length    文件长度
     * @param imagePath 文件路径
     */
    private void writeImageSource(String fileName, int length, String imagePath) {
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
    }

    /**
     * 接收图片
     *
     * @param fileName             文件名
     * @param bannerBitmapDataBean bannerBitmapDataBean
     */
    private void acceptImage(String fileName, BannerBitmapDataBean bannerBitmapDataBean) {
        if (bannerCache.remainingCapacity() == 0) {
            // 如果队列已满,则把队列中的第一个元素移除
            BannerBitmapDataBean firstBannerBitmap = bannerCache.poll();
            if (firstBannerBitmap != null) {
                // 去重集合中移除这张图
                loadImageNames.remove(firstBannerBitmap.getTitle());
                Log.i(TAG, "onSuccess: 队列已满,移除第一个元素,移除的元素为: " + firstBannerBitmap.getTitle());
            }
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
