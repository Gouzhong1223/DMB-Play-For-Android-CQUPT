package cn.edu.cqupt.dmb.player.common;

/**
 * @Author : Gouzhong
 * @Blog : www.gouzhong1223.com
 * @Description : 这么枚举类是用来描述重邮 DMB 播放器使用到的模块
 * @Date : create by QingSong in 2022-04-07 13:32
 * @Email : qingsong.qs@alibaba-inc.com
 * @Since : JDK 1.8
 * @PackageName : cn.edu.cqupt.dmb.player.common
 * @ProjectName : DMB Player For Android
 * @Version : 1.0.0
 */
public enum FrequencyModule {

    CURRICULUM_64("CURRICULUM-64", 220352, 801),
    CURRICULUM_32("CURRICULUM-32", 220352, 801),
    CURRICULUM_16("CURRICULUM-16", 220352, 801),
    CURRICULUM_8("CURRICULUM-8", 220352, 801),
    CURRICULUM_4("CURRICULUM-4", 220352, 801),
    OUTDOOR_SCREEN_TPEG("OUTDOOR_SCREEN_TPEG", 220352, 802),
    OUTDOOR_SCREEN_VIDEO("OUTDOOR_SCREEN_VIDEO", 220352, 802),
    AUDIO("AUDIO", 220352, 901),
    DORMITORY_SAFETY("DORMITORY_SAFETY", 220352, 1),
    ;

    /**
     * 模块功能名称,如果是教学楼课表,那功能名称的规则即使moduleName-building,例如curriculum-64,就代表是二教的课表
     */
    private final String moduleName;

    /**
     * 模块功能的工作频点
     */
    private final Integer frequency;

    /**
     * 对应的设备 ID 号
     */
    private final Integer deviceID;

    FrequencyModule(String moduleName, Integer frequency, Integer deviceID) {
        this.moduleName = moduleName;
        this.frequency = frequency;
        this.deviceID = deviceID;
    }

    public String getModuleName() {
        return moduleName;
    }

    public Integer getFrequency() {
        return frequency;
    }

    public Integer getDeviceID() {
        return deviceID;
    }
}
