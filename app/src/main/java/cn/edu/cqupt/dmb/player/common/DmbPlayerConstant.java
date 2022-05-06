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
    FREQKHZ("FREQKHZ", 220352, "重邮 DMB 频点"),
    FREQKHZ_NEW("FREQKHZ_NEW", 210572, "重邮 DMB 频点_新发射机"),
    FREQKHZ2("FREQKHZ2", 210432, "重邮第二个 DMB 频点"),
    DMB_READ_TIME("DMB_READ_TIME", 15, "一次 IO 从 USB 中读取数据的次数,"),
    DMB_V_ID("DMB_V_ID", 1155, "DMB接收机的厂商ID"),
    DMB_P_ID("DMB_P_ID", 22336, "DMB接收机的设备ID"),
    ACTION_USB_PERMISSION("ACTION_USB_PERMISSION", null, "cn.edu.cqupt.dmb.player.USB_PERMISSION"),
    DEFAULT_BANNER_BITMAP_CACHE_SIZE("DEFAULT_BANNER_BITMAP_CACHE_SIZE", 10, "默认的轮播图缓存数量"),
    MESSAGE_UPDATE_CURRICULUM("MESSAGE_UPDATE_CURRICULUM", 0x100, "监听课表更新的 message 类型"),
    MESSAGE_UPDATE_CAROUSEL("MESSAGE_UPDATE_CAROUSEL", 0x101, "监听轮播图更新的 message 类型"),
    MESSAGE_UPDATE_SIGNAL("MESSAGE_UPDATE_SIGNAL", 0x102, "监听信号更新的 message 类型"),
    MESSAGE_START_PLAY_VIDEO("MESSAGE_START_PLAY_VIDEO", 0x103, "监听开始播放视频的 message 类型"),
    DEFAULT_MPEG_TS_PACKET_SIZE_DECODE("DEFAULT_MPEG_TS_PACKET_SIZE_DECODE", 188, "一个已解码的MPEG-TS包的大小"),
    DEFAULT_MPEG_TS_PACKET_SIZE_ENCODE("DEFAULT_MPEG_TS_PACKET_SIZE_ENCODE", 188, "一个已解码的MPEG-TS包的大小"),
    DEFAULT_MPEG_TS_STREAM_SIZE_TIMES("DEFAULT_MPEG_TS_STREAM_SIZE_TIMES", 1024 * 512, "MPEG-TS输出流的计量倍数"),
    MESSAGE_JUMP_DEFAULT_ACTIVITY("MESSAGE_JUMP_DEFAULT_ACTIVITY", 0x104, "跳转到默认场景的消息"),
    DEFAULT_FREQUENCY_MODULE_KEY("defaultFrequencyModule", null, "默认的使用场景在本地存储中的 KEY"),
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
