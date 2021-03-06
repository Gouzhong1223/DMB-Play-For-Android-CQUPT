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
import android.os.Handler;
import android.util.Log;

import com.youth.banner.bean.BannerBitmapDataBean;

import java.util.Queue;

import cn.edu.cqupt.dmb.player.common.DmbPlayerConstant;
import cn.edu.cqupt.dmb.player.listener.CarouselListener;
import cn.edu.cqupt.dmb.player.utils.GlideUtils;

/**
 * @Author : Gouzhong
 * @Blog : www.gouzhong1223.com
 * @Description : 这个是宿舍监听器
 * @Date : create by QingSong in 2022-04-07 14:58
 * @Email : qingsong.qs@alibaba-inc.com
 * @Since : JDK 1.8
 * @PackageName : cn.edu.cqupt.dmb.player.listener
 * @ProjectName : DMB Player For Android
 * @Version : 1.0.0
 */
public class DmbDormitoryListenerImpl implements CarouselListener {

    private static final String TAG = "DmbDormitoryListenerImpl";
    /**
     * 处理更新轮播图的消息类型
     */
    private final Integer MESSAGE_UPDATE_CAROUSEL = DmbPlayerConstant.MESSAGE_UPDATE_CAROUSEL.getDmbConstantValue();
    /**
     * 监听信号更新的 message 类型
     */
    private final int MESSAGE_UPDATE_SIGNAL = DmbPlayerConstant.MESSAGE_UPDATE_SIGNAL.getDmbConstantValue();

    /**
     * 轮播图图片字节流
     */
    private final byte[] fileBuffer = new byte[1024 * 1024 * 10];

    /**
     * 回调
     */
    private final Handler handler;

    /**
     * 轮播图 FIFO 队列
     */
    private final Queue<BannerBitmapDataBean> bannerCache;

    private final Context context;

    /**
     * 发送更新信号消息的计数器,cnt==5 的时候发送一次更新信号消息,发送之后清零
     */
    private int cnt = 0;
    /**
     * [备用]装载所有的已经解码的 TPEG 数据
     */
    private byte[] alternativeBytes;

    public DmbDormitoryListenerImpl(Handler handler, Queue<BannerBitmapDataBean> bannerCache, Context context) {
        this.handler = handler;
        this.bannerCache = bannerCache;
        this.context = context;
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
            bitmap = GlideUtils.loadBitMap(context, fileBuffer);
            if (bitmap == null) {
                Log.i(TAG, "onSuccess: 第二次生成 bitmap 还是出错了...");
            } else {
                Log.i(TAG, "onSuccess: 第二次生成 bitmap 成功");
                // 添加到有界队列中
                bannerCache.add(bannerBitmapDataBean);
                // 添加一张轮播图之后,发送一次更新轮播图的消息
                handler.sendEmptyMessage(MESSAGE_UPDATE_CAROUSEL);
            }
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
