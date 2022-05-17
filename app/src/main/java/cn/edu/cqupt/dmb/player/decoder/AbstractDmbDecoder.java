package cn.edu.cqupt.dmb.player.decoder;

import android.content.Context;

import java.io.BufferedInputStream;
import java.io.PipedInputStream;

import cn.edu.cqupt.dmb.player.listener.DmbListener;
import cn.edu.cqupt.dmb.player.utils.DataReadWriteUtil;

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
public abstract class AbstractDmbDecoder extends Thread {

    protected static final boolean DEBUG = false;
    /**
     * TS 视频流的输入流
     */
    protected static final PipedInputStream PIPED_INPUT_STREAM;
    /**
     * TS 视频的缓冲流
     */
    protected static final BufferedInputStream BUFFERED_INPUT_STREAM;
    /**
     * 单例 DataReadWriteUtil 对象
     */
    private static final DataReadWriteUtil DATA_READ_WRITE_UTIL;

    static {
        // 获取 DataReadWriteUtil 单例对象
        DATA_READ_WRITE_UTIL = DataReadWriteUtil.getInstance();
        // 获取 PIP 输入流
        PIPED_INPUT_STREAM = DATA_READ_WRITE_UTIL.getPipedInputStream();
        // 获取输入缓冲流
        BUFFERED_INPUT_STREAM = DATA_READ_WRITE_UTIL.getBufferedInputStream();
    }

    /**
     * 解码器的监听器
     */
    protected final DmbListener dmbListener;

    /**
     * 初始化解码器的 context
     */
    protected final Context context;

    public AbstractDmbDecoder(DmbListener dmbListener, Context context) {
        this.dmbListener = dmbListener;
        this.context = context;
    }

    /**
     * 获取 pip 输入流
     *
     * @return PipedInputStream
     */
    public static PipedInputStream getPipedInputStream() {
        return PIPED_INPUT_STREAM;
    }
}
