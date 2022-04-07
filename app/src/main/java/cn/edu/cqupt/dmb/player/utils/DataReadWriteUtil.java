package cn.edu.cqupt.dmb.player.utils;

import java.io.BufferedInputStream;
import java.io.PipedInputStream;

import cn.edu.cqupt.dmb.player.common.FrequencyModule;

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
     * 这个是室外屏数据的输入流
     */
    private static final PipedInputStream tpegPipedInputStream;
    /**
     * 这个是课表的数据输入流
     */
    private static final PipedInputStream curriculumPipedInputStream;
    /**
     * 这是宿舍安全信息的数据输入流
     */
    private static final PipedInputStream dormitoryPipedInputStream;
    private static final BufferedInputStream bufferedInputStream;

    private static volatile boolean INITIALIZE_TEMPORARY_FILES = false;

    /**
     * 临时视频文件名
     */
    private static volatile String TEMPORARY_MPEG_TS_VIDEO_FILENAME = "";

    /**
     * 是否已经进行了 USB 的第一次初始化
     */
    public static volatile boolean isFirstInitMainActivity = true;

    /**
     * 是否已经改变了设备的工作频点
     */
    private volatile boolean isChangeFrequency = false;

    /**
     * 是否已经改变了设备的 ID
     */
    private volatile boolean isChangeDeviceId = false;

    /**
     * USB 设备是否就绪
     */
    public volatile static boolean USB_READY = false;

    /**
     * 现在是否已经接收到了 DMB 类型的数据
     */
    public static volatile boolean initFlag = false;

    /**
     * 当前活跃(选中的模块)
     */
    private static volatile FrequencyModule frequencyModule;

    static {
        tpegPipedInputStream = new PipedInputStream(1024 * 2);
        curriculumPipedInputStream = new PipedInputStream(1024 * 2);
        dormitoryPipedInputStream = new PipedInputStream(1024 * 2);
        bufferedInputStream = new BufferedInputStream(tpegPipedInputStream);
    }

    public static PipedInputStream getTpegPipedInputStream() {
        return tpegPipedInputStream;
    }

    public static PipedInputStream getCurriculumPipedInputStream() {
        return curriculumPipedInputStream;
    }

    public static PipedInputStream getDormitoryPipedInputStream() {
        return dormitoryPipedInputStream;
    }

    public boolean isIschangeFrequency() {
        return isChangeFrequency;
    }

    public void setIschangeFrequency(boolean isChangeFrequency) {
        this.isChangeFrequency = isChangeFrequency;
    }

    public boolean isChangeDeviceId() {
        return isChangeDeviceId;
    }

    public void setChangeDeviceId(boolean changeDeviceId) {
        isChangeDeviceId = changeDeviceId;
    }

    public static String getTemporaryMpegTsVideoFilename() {
        return TEMPORARY_MPEG_TS_VIDEO_FILENAME;
    }

    public static void setTemporaryMpegTsVideoFilename(String temporaryMpegTsVideoFilename) {
        INITIALIZE_TEMPORARY_FILES = true;
        TEMPORARY_MPEG_TS_VIDEO_FILENAME = temporaryMpegTsVideoFilename;
    }

    public static boolean isInitializeTemporaryFiles() {
        return INITIALIZE_TEMPORARY_FILES;
    }

    public static void setInitializeTemporaryFiles(boolean initializeTemporaryFiles) {
        INITIALIZE_TEMPORARY_FILES = initializeTemporaryFiles;
    }

    public static FrequencyModule getFrequencyModule() {
        return frequencyModule;
    }

    public static void setFrequencyModule(FrequencyModule frequencyModule) {
        DataReadWriteUtil.frequencyModule = frequencyModule;
    }
}
