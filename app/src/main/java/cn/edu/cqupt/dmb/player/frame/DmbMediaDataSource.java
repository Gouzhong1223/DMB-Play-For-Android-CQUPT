package cn.edu.cqupt.dmb.player.frame;

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
 * @PackageName : cn.edu.cqupt.dmb.player.frame
 * @ProjectName : DMB Player For Android
 * @Version : 1.0.0
 */
public class DmbMediaDataSource implements IMediaDataSource {

    private static final String TAG = "DmbMediaDataSource";

    private Integer cnt = 0;
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
//        Log.i(TAG, "自定义数据源缓冲现在还有" + bufferedInputStream.available());
        int read = bufferedInputStream.read(buffer, offset, size);
//        Log.i(TAG, BaseConversionUtil.bytes2hex(buffer));
        return read;
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
