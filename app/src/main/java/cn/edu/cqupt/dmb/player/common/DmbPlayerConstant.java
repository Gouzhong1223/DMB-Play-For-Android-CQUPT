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
    DEFAULT_DMB_DATA_SIZE("DEFAULT_DMB_DATA_SIZE", 64, "一个 DMB 数据包的长度"),
    DMB_READ_TIME("DMB_READ_TIME", 15, "一次 IO 从 USB 中读取数据的次数,"),
    ACTION_USB_PERMISSION("ACTION_USB_PERMISSION", null, "cn.edu.cqupt.dmb.player.USB_PERMISSION"),
    DEFAULT_BANNER_BITMAP_CACHE_SIZE("DEFAULT_BANNER_BITMAP_CACHE_SIZE", 5, "默认的轮播图缓存数量"),
    MESSAGE_UPDATE_CURRICULUM("MESSAGE_UPDATE_CURRICULUM", 0x100, "监听课表更新的 message 类型"),
    MESSAGE_UPDATE_CAROUSEL("MESSAGE_UPDATE_CAROUSEL", 0x101, "监听轮播图更新的 message 类型"),
    MESSAGE_UPDATE_SIGNAL("MESSAGE_UPDATE_SIGNAL", 0x102, "监听信号更新的 message 类型"),
    MESSAGE_JUMP_DEFAULT_ACTIVITY("MESSAGE_JUMP_DEFAULT_ACTIVITY", 0x104, "跳转到默认场景的消息"),
    DEFAULT_TPEG_FILE_BUFFER_SIZE("DEFAULT_TPEG_FILE_BUFFER_SIZE", 1024 * 1024 * 10, "默认允许的TPEG数据包上限"),
    DEFAULT_TPEG_DATA_SIZE("DEFAULT_TPEG_DATA_SIZE", 80, "一个TPEG数据包中包含的原始数据大小"),
    DEFAULT_TPEG_SIZE("DEFAULT_TPEG_SIZE", 112, "一个TPEG数据包的长度"),
    DEFAULT_TPEG_TPEG_INFO_SIZE("DEFAULT_TPEG_TPEG_INFO_SIZE", 3, "一个TPEG信息数组的长度"),
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
