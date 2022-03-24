package cn.edu.cqupt.dmb.player.common;

/**
 * @Author : Gouzhong
 * @Blog : www.gouzhong1223.com
 * @Description : 这是 DMB 播放器的一些常量枚举类
 * @Date : create by QingSong in 2022-03-21 13:28
 * @Email : qingsong.qs@alibaba-inc.com
 * @Since : JDK 1.8
 * @PackageName : cn.edu.cqupt.dmb.player.common
 * @ProjectName : DMB Player For Android
 * @Version : 1.0.0
 */
public enum DmbPlayerConstant {

    DEFAULT_DATA_READ_OFFSET("DEFAULT_DATA_READ_OFFSET", 8, "读取 DMB 数据你默认的偏移量,也就是从 data[DEFAULT_DATA_READ_OFFSET]开始读取"),
    DEFAULT_FIC_SIZE("DEFAULT_FIC_SIZE", 32, "一个 FIC 数据包的长度"),
    DEFAULT_READ_TIME_OUT("DEFAULT_READ_TIME_OUT", 1000, "读取 USB 数据的时候默认的等待时间"),
    DEFAULT_REQ_MSG_SIZE("DEFAULT_REQ_MSG_SIZE", 48, "默认发送到 DMB 一个数据包的大小"),
    DEFAULT_DMB_DATA_SIZE("DEFAULT_DMB_DATA_SIZE", 64, "一个 DMB 数据包的长度"),
    FREQKHZ("FREQKHZ", 220352, "重邮 DMB 频点"),
    FREQKHZ2("FREQKHZ2", 210432, "重邮第二个 DMB 频点"),
    DMB_READ_TIME("DMB_READ_TIME", 48, "一次 IO 从 USB 中读取数据的次数,"),
    DMB_V_ID("DMB_V_ID", 1155, "DMB接收机的厂商ID"),
    DMB_P_ID("DMB_P_ID", 22336, "DMB接收机的设备ID"),
    ACTION_USB_PERMISSION("ACTION_USB_PERMISSION", null, "cn.edu.cqupt.dmb.player.USB_PERMISSION"),
    DEFAULT_BANNER_BITMAP_CACHE_SIZE("DEFAULT_BANNER_BITMAP_CACHE_SIZE", 10, "默认的轮播图缓存数量"),
    ;
    private final String dmbConstantName;
    private final Integer dmbConstantValue;
    private final String dmbConstantDescribe;

    DmbPlayerConstant(String dmbConstantName, Integer dmbConstantValue, String dmbConstantDescribe) {
        this.dmbConstantName = dmbConstantName;
        this.dmbConstantValue = dmbConstantValue;
        this.dmbConstantDescribe = dmbConstantDescribe;
    }

    public String getDmbConstantName() {
        return dmbConstantName;
    }

    public Integer getDmbConstantValue() {
        return dmbConstantValue;
    }

    public String getDmbConstantDescribe() {
        return dmbConstantDescribe;
    }
}
