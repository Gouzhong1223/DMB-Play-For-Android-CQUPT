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

package cn.edu.cqupt.dmb.player.actives;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import cn.edu.cqupt.dmb.player.R;
import cn.edu.cqupt.dmb.player.common.DmbPlayerConstant;
import cn.edu.cqupt.dmb.player.decoder.TpegDecoder;
import cn.edu.cqupt.dmb.player.listener.impl.DmbCurriculumListenerImpl;
import cn.edu.cqupt.dmb.player.processor.dmb.DataProcessingFactory;
import cn.edu.cqupt.dmb.player.processor.dmb.PseudoBitErrorRateProcessor;

/**
 * 这个是显示课表的 Activity
 *
 * @author qingsong
 */
public class CurriculumActivity extends BaseActivity {

    public static final int MESSAGE_UPDATE_CURRICULUM = DmbPlayerConstant.MESSAGE_UPDATE_CURRICULUM.getDmbConstantValue();
    private static final String TAG = "CurriculumActivity";

    /**
     * 监听信号更新的 message 类型
     */
    private final int MESSAGE_UPDATE_SIGNAL = DmbPlayerConstant.MESSAGE_UPDATE_SIGNAL.getDmbConstantValue();

    /**
     * 显示课表的组件
     */
    private ImageView imageView;

    /**
     * 信号显示组件
     */
    private ImageView signalImageView;

    /**
     * 课表更新监听器
     */
    private DmbCurriculumListenerImpl dmbCurriculumListenerImpl;

    /**
     * 初始化 View
     */
    @Override
    public void initView() {
        Log.i(TAG, "正在初始化课表显示组件");
        imageView = findViewById(R.id.curriculum_image_view);
        signalImageView = findViewById(R.id.curriculum_signal);
    }

    @Override
    public void configView() {
        if (defaultSignalShowSetting != null) {
            int showSignal = Math.toIntExact(defaultSignalShowSetting.getSettingValue());
            signalImageView.setVisibility(showSignal == 0 ? View.INVISIBLE : View.VISIBLE);
        }
    }

    /**
     * 开始执行解码线程
     */
    @Override
    public void startDecode() {
        CurriculumHandler curriculumHandler = new CurriculumHandler(Looper.getMainLooper());
        dmbCurriculumListenerImpl = new DmbCurriculumListenerImpl(curriculumHandler, selectedSceneVO);
        // 构造TPEG解码器
        TpegDecoder tpegDecoder = new TpegDecoder(dmbCurriculumListenerImpl, this, bufferedInputStream, curriculumHandler);
        tpegDecoder.start();
    }

    /**
     * 监听课表生成的 Handler<br/>
     * 关于为什么重载这个构造方法,详情参见<p>https://www.codeleading.com/article/66486105877/<p/>
     */
    @SuppressLint("HandlerLeak")
    private final class CurriculumHandler extends Handler {

        public CurriculumHandler(@NonNull Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            if (msg.what == MESSAGE_UPDATE_CURRICULUM) {
                byte[] fileBuffer = dmbCurriculumListenerImpl.getFileBuffer();
                Integer length = dmbCurriculumListenerImpl.getLength();
                Bitmap bitmap = BitmapFactory.decodeByteArray(fileBuffer, 0, length);
                if (bitmap != null) {
                    Log.i(TAG, "重新设置了一下课表");
                    imageView.setImageBitmap(bitmap);
                    imageView.setBackground(null);
                } else {
                    Log.e(TAG, "生成课表 Bitmap 错误!");
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
