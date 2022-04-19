package cn.edu.cqupt.dmb.player.decoder;

import java.io.BufferedInputStream;
import java.io.PipedInputStream;

import cn.edu.cqupt.dmb.player.listener.DmbListener;

/**
 * @Author : Gouzhong
 * @Blog : www.gouzhong1223.com
 * @Description : 这个是抽象的解码器
 * @Date : create by QingSong in 2022-04-17 13:12
 * @Email : qingsong.qs@alibaba-inc.com
 * @Since : JDK 1.8
 * @PackageName : cn.edu.cqupt.dmb.player.decoder
 * @ProjectName : DMB Player For Android
 * @Version : 1.0.4
 */
public abstract class AbstractDmbDecoder implements Runnable {

    /**
     * TS 视频流的输入流
     */
    protected static final PipedInputStream pipedInputStream = new PipedInputStream(1024 * 1024 * 20);

    /**
     * TS 视频的缓冲流
     */
    protected static final BufferedInputStream bufferedInputStream = new BufferedInputStream(pipedInputStream);

    /**
     * 解码器的监听器
     */
    protected final DmbListener dmbListener;

    public AbstractDmbDecoder(DmbListener dmbListener) {
        this.dmbListener = dmbListener;
    }
}
