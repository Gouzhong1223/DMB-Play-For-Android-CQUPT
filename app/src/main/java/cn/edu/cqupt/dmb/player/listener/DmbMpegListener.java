package cn.edu.cqupt.dmb.player.listener;

import android.os.Build;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.RequiresApi;

import java.io.IOException;
import java.io.PipedOutputStream;
import java.io.UnsupportedEncodingException;

import cn.edu.cqupt.dmb.player.common.DmbPlayerConstant;
import cn.edu.cqupt.dmb.player.utils.DmbUtil;

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
@RequiresApi(api = Build.VERSION_CODES.R)
public class DmbMpegListener implements DmbListener {

    private static final String TAG = "DmbMpegListener";

    /**
     * 开始播放视频的消息
     */
    public static final int MESSAGE_START_PLAY_VIDEO = DmbPlayerConstant.MESSAGE_START_PLAY_VIDEO.getDmbConstantValue();

    /**
     * 自定义的视频播放回调
     */
    private final Handler handler;

    /**
     * 是否已经发送了播放视频的消息
     */
    private boolean sendMsg = false;

    /**
     * 头帧
     */
    private static final int FIRST_FRAME = 2;

    /**
     * 中间帧
     */
    private static final int MIDDLE_FRAME = 1;

    /**
     * 尾帧
     */
    private static final int LAST_FRAME = 3;

    /**
     * 文件名
     */
    private String fileName;

    public DmbMpegListener(Handler handler, PipedOutputStream pipedOutputStream) {
        this.handler = handler;
        this.pipedOutputStream = pipedOutputStream;
    }

    /**
     * 已经可以播放的TS流输出流<br/>
     * 读取这个流的线程在MPEG播放器里面
     */
    private final PipedOutputStream pipedOutputStream;

    @Override
    public void onSuccess(String fileName, byte[] tpegData, int length) {
        try {
            // 将已经解码的TS流写到输出流里面去
            pipedOutputStream.write(tpegData, 0, length);
            pipedOutputStream.flush();
        } catch (IOException e) {
            Log.e(TAG, "写入TS流的时候失败啦!");
            e.printStackTrace();
        }
    }

    /**
     * onSuccess 回调方法重载,加了一个参数,就是info数组
     *
     * @param tpegData TPEG有效字段数组
     * @param length   数组长度
     * @param infos    TPEG数据类型
     */
    public void onSuccess(byte[] tpegData, int length, int[] infos) {
        int tpegType = infos[0];
        if (tpegType == FIRST_FRAME && !sendMsg) {
            // 如果收到的是第一帧并且没有发送过播放视频的消息
            try {
                // 获取文件名
                fileName = new String(tpegData, 0, 35, DmbUtil.CHARACTER_SET);
                Log.i(TAG, "接收到" + fileName + "视频的头帧");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            // 设置播放视频的flag
            sendMsg = true;
            onSuccess(fileName, tpegData, length);
            // 发送播放视频的消息
            handler.sendEmptyMessage(MESSAGE_START_PLAY_VIDEO);
            Log.i(TAG, "发送了一条播放视频的消息");
        } else if (tpegType == MIDDLE_FRAME) {
            // 接收到的是中间帧
            Log.i(TAG, "接收到" + fileName + "的中间帧");
            if (sendMsg) {
                // 只有发送过播放视频的消息时才写入数据
                onSuccess(fileName, tpegData, length);
            }
            // 没有发送过播放视频的消息直接忽略
        } else if (tpegType == LAST_FRAME) {
            // 接收到的是尾帧
            Log.i(TAG, "接收到" + fileName + "尾帧");
            if (sendMsg) {
                onSuccess(fileName, tpegData, length);
                // 重置发型消息的flag
                sendMsg = false;
                // 重置文件名
                fileName = null;
            }
            // 没有发送过播放视频的消息直接忽略
        }
    }

    @Override
    public void onReceiveMessage(String msg) {

    }
}
