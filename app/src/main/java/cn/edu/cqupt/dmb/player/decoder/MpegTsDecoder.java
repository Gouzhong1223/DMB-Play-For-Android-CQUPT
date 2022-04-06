package cn.edu.cqupt.dmb.player.decoder;

import cn.edu.cqupt.dmb.player.utils.DataReadWriteUtil;

/**
 * @Author : Gouzhong
 * @Blog : www.gouzhong1223.com
 * @Description : 这个是解码 TS 的任务,他的启动依赖 VideoActivity 的调度
 * @Date : create by QingSong in 2022-04-06 20:59
 * @Email : qingsong.qs@alibaba-inc.com
 * @Since : JDK 1.8
 * @PackageName : cn.edu.cqupt.dmb.player.decoder
 * @ProjectName : DMB Player For Android
 * @Version : 1.0.0
 */
public class MpegTsDecoder extends Thread {

    private final Object LOCK_OBJECT;

    public MpegTsDecoder(Object lock_object) {
        LOCK_OBJECT = lock_object;
    }

    @Override
    public void run() {
        synchronized (LOCK_OBJECT) {
            DataReadWriteUtil.setTemporaryMpegTsVideoFilename("");
            LOCK_OBJECT.notifyAll();
        }
    }
}
