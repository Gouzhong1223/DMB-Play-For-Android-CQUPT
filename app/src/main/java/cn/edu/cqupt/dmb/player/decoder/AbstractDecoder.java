package cn.edu.cqupt.dmb.player.decoder;

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
public abstract class AbstractDecoder implements Runnable{

    private final DmbListener dmbListener;

    public AbstractDecoder(DmbListener dmbListener) {
        this.dmbListener = dmbListener;
    }
}
