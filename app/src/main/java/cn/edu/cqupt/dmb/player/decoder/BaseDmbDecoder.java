package cn.edu.cqupt.dmb.player.decoder;

import android.content.Context;
import android.os.Handler;

import java.io.BufferedInputStream;

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
public abstract class BaseDmbDecoder extends Thread {

    protected static final boolean DEBUG = false;

    /**
     * TS 视频的缓冲流
     */
    protected final BufferedInputStream bufferedInputStream;


    /**
     * 解码器的监听器
     */
    protected final DmbListener dmbListener;

    /**
     * 初始化解码器的 context
     */
    protected final Context context;

    /**
     * 自定义回调器
     */
    protected final Handler handler;

    public BaseDmbDecoder(BufferedInputStream bufferedInputStream, DmbListener dmbListener, Context context, Handler handler) {
        this.bufferedInputStream = bufferedInputStream;
        this.dmbListener = dmbListener;
        this.context = context;
        this.handler = handler;
    }

}
