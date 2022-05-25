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
