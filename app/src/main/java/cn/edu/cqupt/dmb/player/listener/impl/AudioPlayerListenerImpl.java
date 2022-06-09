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

import android.util.Log;

import cn.edu.cqupt.dmb.player.listener.DmbPlayerListener;
import cn.edu.cqupt.dmb.player.video.frame.AudioPlayerFrame;
import tv.danmaku.ijk.media.player.IMediaPlayer;

/**
 * @Author : Gouzhong
 * @Blog : www.gouzhong1223.com
 * @Description : 音频播放监听器的实现类
 * @Date : create by QingSong in 2022-06-09 16:41
 * @Email : qingsong.qs@alibaba-inc.com
 * @Since : JDK 1.8
 * @PackageName : cn.edu.cqupt.dmb.player.listener.impl
 * @ProjectName : DMB Player For Android
 * @Version : 1.0.0
 */
public class AudioPlayerListenerImpl implements DmbPlayerListener {

    private static final String TAG = "AudioPlayerListenerImpl";
    private final AudioPlayerFrame audioPlayerFrame;

    public AudioPlayerListenerImpl(AudioPlayerFrame audioPlayerFrame) {
        this.audioPlayerFrame = audioPlayerFrame;
    }

    @Override
    public void onPrepared(IMediaPlayer mp) {
        Log.d(TAG, "onPrepared");
        Log.i(TAG, "接收到同步开始的通知,现在调用开始播放的方法");
        audioPlayerFrame.start();
    }

    @Override
    public void onCompletion(IMediaPlayer mp) {
        Log.i(TAG, "onCompletion: ");
    }

    @Override
    public void onBufferingUpdate(IMediaPlayer mp, int percent) {
        Log.i(TAG, "onBufferingUpdate: ");
    }

    @Override
    public void onSeekComplete(IMediaPlayer mp) {
        Log.i(TAG, "onSeekComplete: ");
    }

    @Override
    public void onVideoSizeChanged(IMediaPlayer mp, int width, int height, int sar_num, int sar_den) {
        mp.reset();
        Log.i(TAG, "onVideoSizeChanged");
    }

    @Override
    public boolean onError(IMediaPlayer mp, int what, int extra) {
        Log.e(TAG, "播放的时候出错了");
        audioPlayerFrame.stop();
        audioPlayerFrame.release();
        return false;
    }

    @Override
    public boolean onInfo(IMediaPlayer mp, int what, int extra) {
        return false;
    }
}
