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
public abstract class AbstractDmbDecoder implements Runnable {

    protected static final boolean DEBUG = false;
    /**
     * TS 视频流的输入流
     */
    protected static final PipedInputStream pipedInputStream;
    /**
     * TS 视频的缓冲流
     */
    protected static final BufferedInputStream bufferedInputStream;
    /**
     * 单例 DataReadWriteUtil 对象
     */
    private static final DataReadWriteUtil dataReadWriteUtil;

    static {
        // 获取 DataReadWriteUtil 单例对象
        dataReadWriteUtil = DataReadWriteUtil.getInstance();
        // 获取 PIP 输入流
        pipedInputStream = dataReadWriteUtil.getPipedInputStream();
        // 获取输入缓冲流
        bufferedInputStream = dataReadWriteUtil.getBufferedInputStream();
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
        return pipedInputStream;
    }
}
