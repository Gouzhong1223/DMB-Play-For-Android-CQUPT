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
import android.os.Message;
import android.util.Log;

import cn.edu.cqupt.dmb.player.listener.DmbListener;

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
public class DmbAudioListener implements DmbListener {

    private static final String TAG = "DmbAudioListener";
    /**
     * 自定义回调类
     */
    private final Handler handler;

    public DmbAudioListener(Handler handler) {
        this.handler = handler;
    }

    @Override
    public void onSuccess(String fileName, byte[] bytes, int length) {
        if (bytes.length == 0 || length == 0) {
            Log.e(TAG, "onSuccess: bytes.length == 0 or length == 0");
            return;
        }
        byte[] pcmBytes = new byte[length];
        System.arraycopy(bytes, 0, pcmBytes, 0, pcmBytes.length);
        Message message = new Message();
        message.what = 0x95;
        message.obj = pcmBytes;
        handler.sendMessage(message);
    }

    @Override
    public void onReceiveMessage(String msg) {
        Log.i(TAG, "onReceiveMessage: 无实现...");
    }
}
