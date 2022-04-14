package cn.edu.cqupt.dmb.player.frame;

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

    /**
     * MPEG-TS视频数据源输入缓冲流
     */
    private final BufferedInputStream bufferedInputStream;

    public DmbMediaDataSource(BufferedInputStream bufferedInputStream) {
        this.bufferedInputStream = bufferedInputStream;
    }

    @Override
    public int readAt(long position, byte[] buffer, int offset, int size) throws IOException {
        return bufferedInputStream.read(buffer, offset, size);
    }

    @Override
    public long getSize() throws IOException {
        return bufferedInputStream.available();
    }

    @Override
    public void close() throws IOException {
        bufferedInputStream.close();
    }
}
