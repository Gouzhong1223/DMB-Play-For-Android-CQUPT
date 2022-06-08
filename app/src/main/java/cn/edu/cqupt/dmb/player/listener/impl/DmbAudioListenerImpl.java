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

import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.io.PipedOutputStream;

import cn.edu.cqupt.dmb.player.common.DmbPlayerConstant;
import cn.edu.cqupt.dmb.player.listener.DmbListener;
import cn.edu.cqupt.dmb.player.utils.DataReadWriteUtil;

/**
 * @Author : Gouzhong
 * @Blog : www.gouzhong1223.com
 * @Description : 音频解码监听器
 * @Date : create by QingSong in 2022-05-25 15:03
 * @Email : qingsong.qs@alibaba-inc.com
 * @Since : JDK 1.8
 * @PackageName : cn.edu.cqupt.dmb.player.listener.impl
 * @ProjectName : DMB Player For Android
 * @Version : 1.0.0
 */
public class DmbAudioListenerImpl implements DmbListener {

    /**
     * 开始播放音频的消息
     */
    public static final int MESSAGE_START_PLAY_AUDIO = DmbPlayerConstant.MESSAGE_START_PLAY_VIDEO.getDmbConstantValue();
    private static final String TAG = "DmbAudioListenerImpl";

    /**
     * 自定义的音频播放回调
     */
    private final Handler handler;
    /**
     * 已经可以播放的MP2流输出流<br/>
     * 读取这个流的线程在MPEG播放器里面
     */
    private final PipedOutputStream pipedOutputStream;

    /**
     * 是否已经发送了播放音频的消息
     */
    private boolean sendMsg = false;

    public DmbAudioListenerImpl(Handler handler, PipedOutputStream pipedOutputStream) {
        this.handler = handler;
        this.pipedOutputStream = pipedOutputStream;
    }

    @Override
    public void onSuccess(String fileName, byte[] bytes, int length) {
        if (bytes.length == 0 || length == 0) {
            Log.e(TAG, "onSuccess: bytes.length == 0 or length == 0");
            return;
        }
        try {
            if (DataReadWriteUtil.inMainActivity) {
                return;
            }
            // 将已经解码的MP2流写到输出流里面去
            pipedOutputStream.write(bytes, 0, length);
            pipedOutputStream.flush();
        } catch (IOException e) {
            Log.e(TAG, "写入MP2流的时候失败啦!");
            e.printStackTrace();
        }
        if (!sendMsg) {
            handler.sendEmptyMessage(MESSAGE_START_PLAY_AUDIO);
            sendMsg = true;
            Log.i(TAG, "onSuccess: 发送了一条播放音频的消息");
        }
    }

    @Override
    public void onReceiveMessage(String msg) {
        Log.i(TAG, "onReceiveMessage: 无实现...");
    }
}
