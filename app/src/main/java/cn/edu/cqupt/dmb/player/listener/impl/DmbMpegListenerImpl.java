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
 * @Description : 这是DMB播放器播放视频的监听器
 * @Date : create by QingSong in 2022-04-07 16:12
 * @Email : qingsong.qs@alibaba-inc.com
 * @Since : JDK 1.8
 * @PackageName : cn.edu.cqupt.dmb.player.listener
 * @ProjectName : DMB Player For Android
 * @Version : 1.0.0
 */
public class DmbMpegListenerImpl implements DmbListener {

    /**
     * 开始播放视频的消息
     */
    public static final int MESSAGE_START_PLAY_VIDEO = DmbPlayerConstant.MESSAGE_START_PLAY_VIDEO.getDmbConstantValue();
    private static final String TAG = "DmbMpegListenerImpl";

    /**
     * 自定义的视频播放回调
     */
    private final Handler handler;
    /**
     * 已经可以播放的TS流输出流<br/>
     * 读取这个流的线程在MPEG播放器里面
     */
    private final PipedOutputStream pipedOutputStream;

    /**
     * 是否已经发送了播放视频的消息
     */
    private boolean sendMsg = false;


    public DmbMpegListenerImpl(Handler handler, PipedOutputStream pipedOutputStream) {
        this.handler = handler;
        this.pipedOutputStream = pipedOutputStream;
    }

    @Override
    public void onSuccess(String fileName, byte[] tpegData, int length) {
        try {
            if (DataReadWriteUtil.inMainActivity) {
                return;
            }
            // 将已经解码的TS流写到输出流里面去
            pipedOutputStream.write(tpegData, 0, length);
            pipedOutputStream.flush();
        } catch (IOException e) {
            Log.e(TAG, "写入TS流的时候失败啦!");
            e.printStackTrace();
        }
        if (!sendMsg) {
            handler.sendEmptyMessage(MESSAGE_START_PLAY_VIDEO);
            sendMsg = true;
            Log.i(TAG, "onSuccess: 发送了一条播放视频的消息");
        }
    }
}
