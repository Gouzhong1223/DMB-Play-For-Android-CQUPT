package cn.edu.cqupt.dmb.player.common;

import androidx.annotation.NonNull;

import java.util.Objects;

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

    CURRICULUM_64(1, "CURRICULUM-64", 220352, 801),
    CURRICULUM_32(2, "CURRICULUM-32", 220352, 801),
    CURRICULUM_16(3, "CURRICULUM-16", 220352, 801),
    CURRICULUM_8(4, "CURRICULUM-8", 220352, 801),
    CURRICULUM_4(5, "CURRICULUM-4", 220352, 801),
    OUTDOOR_SCREEN_TPEG(6, "OUTDOOR_SCREEN_TPEG", 215072, 111),
    OUTDOOR_SCREEN_VIDEO(7, "OUTDOOR_SCREEN_VIDEO", 215072, 3),
    AUDIO(8, "AUDIO", 220352, 901),
    DORMITORY_SAFETY(9, "DORMITORY_SAFETY", 220352, 1),
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

    /**
     * 序号
     */
    private final Integer serialNumber;

    FrequencyModule(Integer serialNumber, String moduleName, Integer frequency, Integer deviceID) {
        this.serialNumber = serialNumber;
        this.moduleName = moduleName;
        this.frequency = frequency;
        this.deviceID = deviceID;
    }

    public static FrequencyModule getFrequencyModuleBySerialNumber(Integer serialNumber) {
        for (FrequencyModule value : values()) {
            if (Objects.equals(value.getSerialNumber(), serialNumber)) {
                return value;
            }
        }
        return null;
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

    public Integer getSerialNumber() {
        return serialNumber;
    }

    @NonNull
    @Override
    public String toString() {
        return "工作模块=" + moduleName +
                ", 工作频点=" + frequency +
                ", 设备 ID=" + deviceID;
    }
}
