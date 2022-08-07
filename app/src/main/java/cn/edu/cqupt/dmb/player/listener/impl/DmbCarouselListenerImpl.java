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
import androidx.annotation.Nullable;

import com.google.common.collect.EvictingQueue;
import com.youth.banner.bean.BannerBitmapDataBean;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Objects;

import cn.edu.cqupt.dmb.player.common.DmbPlayerConstant;
import cn.edu.cqupt.dmb.player.listener.CarouselListener;
import cn.edu.cqupt.dmb.player.utils.DataReadWriteUtil;
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
    /**
     * 图片输出路径
     */
    public static final String IMAGES_PATH = "/cn.edu.cqupt.dmb.player/images/";
    /**
     * 默认的文件名
     */
    public static final String DAB_JPG_NAME = "dab.jpg";
    /**
     * 默认的轮播图停留时间
     */
    public static final long DEFAULT_LOOP_TIME = 0L;
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
        // 重命名
        fileName = renameFileIfNameAsDefault(fileName, length);
        System.arraycopy(bytes, 0, fileBuffer, 0, length);
        // 根据数据源生生成 bitmap
        // 这个 bitmap 可能是一个空的 bitmap, 因为有可能解码失败
        Bitmap bitmap = BitmapFactory.decodeByteArray(fileBuffer, 0, length);
        // 构造 bannerBitmapDataBean
        // 这个 bannerBitmapDataBean 同理
        BannerBitmapDataBean bannerBitmapDataBean = new BannerBitmapDataBean(bitmap, fileName, 1);
        if (bitmap != null) {
            // 判断是否应该接收图片并更新停留时间
            if (judgingImageHasLoadedAndUpdateLoopTime(fileName, bannerBitmapDataBean)) {
                return;
            }
            // 现在的缓存列表里面没有这张图片
            acceptImage(fileName, bannerBitmapDataBean);
        } else {
            // 如果 bitmap 为空,尝试第二次加载
            if (secondGenerateImage(length, bannerBitmapDataBean)) return;
        }
        // 发送更新信号的广播
        sendSignalMsg();
    }

    /**
     * 重命名图片
     *
     * @param fileName 原始文件名
     * @param length   文件长度
     * @return 新的文件名
     */
    @Nullable
    private String renameFileIfNameAsDefault(String fileName, int length) {
        if (Objects.equals(fileName, DAB_JPG_NAME)) {
            // 重命名图片
            fileName = "dmb" + length + ".jpg";
            Log.i(TAG, "onSuccess: fileName 为 dab.jpg, 重命名为 dmb" + length + ".jpg");
        }
        return fileName;
    }

    /**
     * 发送更新信号的消息
     */
    private void sendSignalMsg() {
        if (cnt == 5) {
            // 如果现在计数器已经到 5 了,就发送一次更新信号的消息
            Log.i(TAG, Thread.currentThread().getName() + "线程发送了一次更新信号的消息");
            handler.sendEmptyMessage(MESSAGE_UPDATE_SIGNAL);
            // 发送之后重置计数器
            cnt = 0;
        }
    }

    /**
     * 第二次加载图片
     *
     * @param length               图片长度
     * @param bannerBitmapDataBean 轮播图数据
     * @return true->缓存中已经存在该图片,更新了轮播图停留时间,false->缓存中不存在该图片,进行第一次加载
     */
    private boolean secondGenerateImage(int length, BannerBitmapDataBean bannerBitmapDataBean) {
        Bitmap bitmap;
        String fileName;
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
            if (judgingImageHasLoadedAndUpdateLoopTime(fileName, bannerBitmapDataBean)) {
                return true;
            }
            // 接收图片
            acceptImage(fileName, bannerBitmapDataBean);
        }
        // 删除缓存的文件
        new File(imagePath + fileName).delete();
        return false;
    }

    /**
     * 判断是否应该接收图片并更新轮播图停留时间
     *
     * @param fileName             文件名
     * @param bannerBitmapDataBean 轮播图数据
     * @return true->加载过这张图片,但是更新了停留时间,false->没有加载过这张图片
     */
    private boolean judgingImageHasLoadedAndUpdateLoopTime(String fileName, BannerBitmapDataBean bannerBitmapDataBean) {
        // 判断是否已经加载过这一张图片了
        if (loadImageNames.contains(fileName)) {
            // 找出这张图片的停留时间
            String loopTimeStr = DataReadWriteUtil.imageLoopTimeMap.get(fileName);
            if (loopTimeStr != null && !loopTimeStr.equals("")) {
                long loopTimeL = DEFAULT_LOOP_TIME;
                try {
                    // 尝试转换成 long 类型
                    loopTimeL = Long.parseLong(loopTimeStr);
                } catch (NumberFormatException e) {
                    // 如果转换失败,就设置为 0
                    Log.e(TAG, "onSuccess: 轮播图停留时间解析失败");
                    e.printStackTrace();
                }
                bannerBitmapDataBean.setLoopTime(loopTimeL);
                // 这里的 contains 只对比文件名还有 loopTime
                // 如果此集合包含指定元素，则返回true.
                // 更正式地说,当且仅当此集合包含至少一个元素e满足
                // (o==null ? e==null : o.equals(e))时,才返回true
                // 我重写了 BannerBitmapDataBean 的 equals 方法
                if (bannerCache.contains(bannerBitmapDataBean)) {
                    // 如果缓存中已经存在这张图片,并且停留时间没有改变过,就直接返回了
                    // 这里有两种情况,第一种是已经设置过停留时间
                    // 第二种是没有设置过停留时间并且命令行里面也没有找到命令参数也就是 loopTimeL 为 0 的情况
                    return true;
                }
                // 走到这里就是停留时间已经发生了改变,需要找到对应的元素进行一下更新
                for (BannerBitmapDataBean bitmapDataBean : bannerCache) {
                    updateImageLoopTime(fileName, bannerBitmapDataBean, bitmapDataBean);
                }
            } else {
                // 已经加载过这张图片但是没有找到这张图的命令参数
                Log.i(TAG, "onSuccess: 跳过了一张重复的图片");
            }
            return true;
        }
        // 没有加载过这张图片,则加入到加载过的图片集合中
        return false;
    }

    /**
     * 更新图片的停留时间
     *
     * @param fileName             文件名
     * @param bannerBitmapDataBean 轮播图数据
     * @param bitmapDataBean       FIFO 队列中的图片数据
     */
    private void updateImageLoopTime(String fileName, BannerBitmapDataBean bannerBitmapDataBean, BannerBitmapDataBean bitmapDataBean) {
        // 对比图片名字
        if (bitmapDataBean.getTitle().equals(fileName)) {
            // 删掉原来没有设置过停留时间的图片
            bannerCache.remove(bitmapDataBean);
            Log.i(TAG, "updateImageLoopTime: 已经加载过这张图片,但是没有设置停留时间,设置停留时间为 " + bannerBitmapDataBean.getLoopTime());
            // 把设置过停留时间的图片添加到缓存中
            bannerCache.add(bannerBitmapDataBean);
            Log.i(TAG, "updateImageLoopTime: 重新设置了停留时间所以发送一条更新轮播图的广播");
            // 发送更新轮播图的消息
            handler.sendEmptyMessage(MESSAGE_UPDATE_CAROUSEL);
            // 发送更新信号的消息
            sendSignalMsg();
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
            Log.e(TAG, "writeImageSource: 写出文件流出错了...");
            e.printStackTrace();
        }
    }

    /**
     * 接收图片<br/>
     * 接收之前会判断 FIFO 是否已满,如果已满会移除 FIFO 头部元素
     *
     * @param fileName             文件名
     * @param bannerBitmapDataBean 轮播图数据
     */
    private void acceptImage(String fileName, BannerBitmapDataBean bannerBitmapDataBean) {
        if (bannerCache.remainingCapacity() == 0) {
            // 如果队列已满,则把队列中的第一个元素移除
            BannerBitmapDataBean firstBannerBitmap = bannerCache.poll();
            if (firstBannerBitmap != null) {
                // 去重集合中移除这张图
                loadImageNames.remove(firstBannerBitmap.getTitle());
                Log.i(TAG, "acceptImage: 队列已满,移除第一个元素,移除的元素为: " + firstBannerBitmap.getTitle());
            }
        }
        // 把文件名添加到 Set 集合中
        loadImageNames.add(fileName);
        Log.i(TAG, "acceptImage: 去重集合中加载了一张图片");
        // 填充轮播图元素的停留时间
        fillBannerBeanLoopTime(fileName, bannerBitmapDataBean);
        // 添加到有界队列中
        bannerCache.add(bannerBitmapDataBean);
        // 添加一张轮播图之后,发送一次更新轮播图的消息
        Log.i(TAG, "acceptImage: 发送一次更新轮播图的消息");
        handler.sendEmptyMessage(MESSAGE_UPDATE_CAROUSEL);
    }

    /**
     * 填充轮播图元素的停留时间<br/>
     * 有命令参数就设置为对应的,没有就设置为 0L
     *
     * @param fileName             文件名
     * @param bannerBitmapDataBean 轮播图数据
     */
    private void fillBannerBeanLoopTime(String fileName, BannerBitmapDataBean bannerBitmapDataBean) {
        String loopTimeStr = DataReadWriteUtil.imageLoopTimeMap.get(fileName);
        if (loopTimeStr != null && !loopTimeStr.equals("")) {
            long loopTimeL = DEFAULT_LOOP_TIME;
            try {
                loopTimeL = Long.parseLong(loopTimeStr);
            } catch (NumberFormatException e) {
                Log.e(TAG, "onSuccess: 轮播图停留时间解析失败");
                e.printStackTrace();
            }
            bannerBitmapDataBean.setLoopTime(loopTimeL);
        } else {
            bannerBitmapDataBean.setLoopTime(DEFAULT_LOOP_TIME);
        }
    }

    @Override
    public void onSuccess(String fileName, byte[] bytes, int length, byte[] alternativeBytes) {
        this.alternativeBytes = alternativeBytes;
        this.onSuccess(fileName, bytes, length);
    }
}
