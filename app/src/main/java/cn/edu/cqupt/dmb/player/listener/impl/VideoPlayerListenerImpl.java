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
import cn.edu.cqupt.dmb.player.video.frame.VideoPlayerFrame;
import tv.danmaku.ijk.media.player.IMediaPlayer;

/**
 * @Author : Gouzhong
 * @Blog : www.gouzhong1223.com
 * @Description : 这个是 DmbPlayerListener 监听器的实现类
 * @Date : create by QingSong in 2022-04-06 20:08
 * @Email : qingsong.qs@alibaba-inc.com
 * @Since : JDK 1.8
 * @PackageName : cn.edu.cqupt.dmb.player.listener
 * @ProjectName : DMB Player For Android
 * @Version : 1.0.0
 */
public class VideoPlayerListenerImpl implements DmbPlayerListener {

    private static final String TAG = "VideoPlayerListenerImpl";

    private final VideoPlayerFrame videoPlayerFrame;

    /**
     * 构造方法,返回监听器实例
     *
     * @param videoPlayerFrame 自定义的 TS 播放器 View
     */
    public VideoPlayerListenerImpl(VideoPlayerFrame videoPlayerFrame) {
        this.videoPlayerFrame = videoPlayerFrame;
    }

    @Override
    public void onBufferingUpdate(IMediaPlayer iMediaPlayer, int i) {
        iMediaPlayer.reset();
        Log.i(TAG, "onBufferingUpdate i = " + i);
    }

    @Override
    public void onCompletion(IMediaPlayer iMediaPlayer) {
        Log.d(TAG, "onCompletion");
    }

    @Override
    public boolean onError(IMediaPlayer iMediaPlayer, int i, int i1) {
        Log.e(TAG, "播放的时候出错了");
        videoPlayerFrame.stop();
        videoPlayerFrame.release();
        return false;
    }

    @Override
    public boolean onInfo(IMediaPlayer iMediaPlayer, int i, int i1) {
        return false;
    }

    @Override
    public void onPrepared(IMediaPlayer iMediaPlayer) {
        Log.d(TAG, "onPrepared");
        Log.i(TAG, "接收到同步开始的通知,现在调用开始播放的方法");
        videoPlayerFrame.start();
    }

    @Override
    public void onSeekComplete(IMediaPlayer iMediaPlayer) {
        Log.i(TAG, "onSeekComplete");
    }

    @Override
    public void onVideoSizeChanged(IMediaPlayer iMediaPlayer, int i, int i1, int i2, int i3) {
        iMediaPlayer.reset();
        Log.i(TAG, "onVideoSizeChanged");
    }
}
