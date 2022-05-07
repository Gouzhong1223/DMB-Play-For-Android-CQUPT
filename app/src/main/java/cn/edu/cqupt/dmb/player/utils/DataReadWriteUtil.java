package cn.edu.cqupt.dmb.player.utils;

import android.content.Context;

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
     * 是否已经进行了 USB 的第一次初始化
     */
    public static volatile boolean isFirstInitMainActivity = true;
    /**
     * USB 设备是否就绪
     */
    public volatile static boolean USB_READY = false;

    public volatile static boolean init = false;
    /**
     * 现在是否已经接收到了 DMB 类型的数据
     */
    public static volatile boolean initFlag = false;
    /**
     * 是否在主页
     */
    public static volatile boolean inMainActivity = true;
    /**
     * DataReadWriteUtil 单例对象
     */
    private static volatile DataReadWriteUtil dataReadWriteUtil;
    /**
     * 当前活跃(选中的模块)
     */
    private static volatile FrequencyModule activeFrequencyModule;
    /**
     * USB 数据的 PIP 输入流
     */
    private final PipedInputStream pipedInputStream;
    /**
     * USB 数据的输入缓冲流
     */
    private final BufferedInputStream bufferedInputStream;

    /**
     * 构造方法
     *
     * @param pipedInputStream pip 输入流
     */
    public DataReadWriteUtil(PipedInputStream pipedInputStream) {
        this.pipedInputStream = pipedInputStream;
        this.bufferedInputStream = new BufferedInputStream(pipedInputStream);
    }

    /**
     * 获取 DataReadWriteUtil 的单例对象
     *
     * @return DataReadWriteUtil 单例对象
     */
    public static DataReadWriteUtil getInstance() {
        if (dataReadWriteUtil == null) {
            synchronized (DataReadWriteUtil.class) {
                if (dataReadWriteUtil == null) {
                    dataReadWriteUtil = new DataReadWriteUtil(new PipedInputStream(1024 * 1024 * 30));
                }
            }
        }
        return dataReadWriteUtil;
    }

    public static FrequencyModule getActiveFrequencyModule() {
        return activeFrequencyModule;
    }

    public static void setActiveFrequencyModule(FrequencyModule activeFrequencyModule) {
        DataReadWriteUtil.activeFrequencyModule = activeFrequencyModule;
    }

    /**
     * 获取当前系统的默认工作场景
     *
     * @param context 当前 context
     * @return 频点模块
     */
    public static FrequencyModule getDefaultFrequencyModule(Context context) {
        // 从sharedPreferences中获取默认模块的序号,序号的范围是 1-9
        int serialNumber = DmbUtil.getInt(context, "defaultFrequencyModule", 20);
        // 根据序号获取模块信息
        return FrequencyModule.getFrequencyModuleBySerialNumber(serialNumber);
    }

    public PipedInputStream getPipedInputStream() {
        return pipedInputStream;
    }

    public BufferedInputStream getBufferedInputStream() {
        return bufferedInputStream;
    }
}
