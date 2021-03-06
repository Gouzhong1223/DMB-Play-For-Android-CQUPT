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

package cn.edu.cqupt.dmb.player.video.stream;

import android.util.Log;

import java.io.BufferedInputStream;
import java.io.IOException;

import tv.danmaku.ijk.media.player.misc.IMediaDataSource;

/**
 * @Author : Gouzhong
 * @Blog : www.gouzhong1223.com
 * @Description : 这是自定义的DMB视频源
 * @Date : create by QingSong in 2022-04-13 22:29
 * @Email : qingsong.qs@alibaba-inc.com
 * @Since : JDK 1.8
 * @PackageName : cn.edu.cqupt.dmb.player.video.frame
 * @ProjectName : DMB Player For Android
 * @Version : 1.0.0
 */
public class DmbMediaDataSource implements IMediaDataSource {

    private static final String TAG = "DmbMediaDataSource";

    /**
     * MPEG-TS视频数据源输入缓冲流
     */
    private final BufferedInputStream bufferedInputStream;

    public DmbMediaDataSource(BufferedInputStream bufferedInputStream) {
        this.bufferedInputStream = bufferedInputStream;
    }

    @Override
    public int readAt(long position, byte[] buffer, int offset, int size) throws IOException {

        if (size == 0) {
            // size=0 means there is a seek request.
            // You can handle it now, or ignore it, and handle new position at next readAt() call.
            return 0;
        }
        return bufferedInputStream.read(buffer, offset, size);
    }

    @Override
    public long getSize() throws IOException {
        return bufferedInputStream.available();
    }

    @Override
    public void close() throws IOException {
        Log.i(TAG, "输入流被关闭啦!");
        bufferedInputStream.close();
    }
}
