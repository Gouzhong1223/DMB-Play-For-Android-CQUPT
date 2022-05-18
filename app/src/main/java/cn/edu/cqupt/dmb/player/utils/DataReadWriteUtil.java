package cn.edu.cqupt.dmb.player.utils;

/**
 * @Author : Gouzhong
 * @Blog : www.gouzhong1223.com
 * @Description : 读取数据的工具类
 * @Date : create by QingSong in 2022-03-16 15:18
 * @Email : qingsong.qs@alibaba-inc.com
 * @Since : JDK 1.8
 * @PackageName : cn.edu.cqupt.dmb.player.utils
 * @ProjectName : DMB Player For Android
 * @Version : 1.0.0
 */
public class DataReadWriteUtil {

    /**
     * 是否已经进行了 USB 的第一次初始化
     */
    public static volatile boolean isFirstInitMainActivity = true;
    /**
     * USB 设备是否就绪
     */
    public volatile static boolean USB_READY = false;
    /**
     * 现在是否已经接收到了 DMB 类型的数据
     */
    public static volatile boolean initFlag = false;
    /**
     * 是否在主页
     */
    public static volatile boolean inMainActivity = true;
}
