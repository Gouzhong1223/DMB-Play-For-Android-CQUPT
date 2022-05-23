package cn.edu.cqupt.dmb.player.common;

/**
 * @Author : Gouzhong
 * @Blog : www.gouzhong1223.com
 * @Description :
 * @Date : create by QingSong in 2022-05-14 15:50
 * @Email : gouzhong1223@gmail.com
 * @Since : JDK 1.8
 * @PackageName : cn.edu.cqupt.dmb.player.common
 * @ProjectName : DMB Player For Android
 * @Version : 1.0.0
 */
public enum CustomSettingByKey {
    DEFAULT_SENSE("default_sense"),
    DEFAULT_CAROUSEL_NUM("default_carousel_num"),
    SHOW_DEBUG_LOG("show_debug_log"),
    OPEN_SIGNAL("open_signal");

    private final String key;

    CustomSettingByKey(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }
}
