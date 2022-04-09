package cn.edu.cqupt.dmb.player.utils;

import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import cn.edu.cqupt.dmb.player.common.DmbPlayerConstant;
import cn.edu.cqupt.dmb.player.common.FrequencyModule;
import cn.edu.cqupt.dmb.player.domain.Dangle;
import cn.edu.cqupt.dmb.player.task.ReceiveUsbDataTask;

/**
 * @Author : Gouzhong
 * @Blog : www.gouzhong1223.com
 * @Description : 这个是USB的工具类
 * @Date : create by QingSong in 2022-03-12 14:22
 * @Email : qingsong.qs@alibaba-inc.com
 * @Since : JDK 1.8
 * @PackageName : cn.edu.cqupt.dmb.player.utils
 * @ProjectName : DMB Player For Android
 * @Version : 1.0.0
 */
public class UsbUtil {


    private static Dangle dangle;

    public static final Object WAIT_USB_READY_LOCK_OBJECT = new Object();


    /**
     * 定时任务线程池
     */
    private static ScheduledExecutorService scheduledExecutorService;
    private static ExecutorService executorService;

    static {
        // JVM启动的时候初始化线程池,由于只有一个任务,所以核心线程就只设置一个
        // 这个线程池是专门用来定时执行接收USB数据任务的
        scheduledExecutorService = new ScheduledThreadPoolExecutor(1);
        // 这个线程池是用于执行普通的读写线程的线程池
        executorService = new ThreadPoolExecutor(5, 10,
                60L, TimeUnit.SECONDS, new ArrayBlockingQueue<>(5));
    }

    /**
     * 用于缓存USB设备的Map
     */
    public static HashMap<String, UsbDevice> deviceHashMap = new HashMap<>();
    /**
     * 缓存从USB读取到的数据,2048是一个待定值
     * TODO 后续根据测试情况修改大小
     */
    private static final byte[] bytes = new byte[DmbPlayerConstant.DEFAULT_DMB_DATA_SIZE.getDmbConstantValue()
            * DmbPlayerConstant.DMB_READ_TIME.getDmbConstantValue()];
    /**
     * 读取USB数据的UsbEndpoint
     */
    public static UsbEndpoint usbEndpointIn;
    /**
     * 写入USB数据的Endpoint
     */
    public static UsbEndpoint usbEndpointOut;

    /**
     * 已经打开的USB连接
     */
    public static UsbDeviceConnection usbDeviceConnection;


    /**
     * 初始化USB设备并且定时从USB中读取数据
     *
     * @param manager 用于管理USB的UsbManager
     */
    public void initUsb(UsbManager manager) {
        deviceHashMap = manager.getDeviceList();
        for (Map.Entry<String, UsbDevice> stringUsbDeviceEntry : deviceHashMap.entrySet()) {
            UsbDevice usbDevice = stringUsbDeviceEntry.getValue();
            UsbInterface usbInterface = usbDevice.getInterface(1);
            // 获取接口所有的Endpoint
            for (int i = 0; i < usbInterface.getEndpointCount(); i++) {
                if (usbInterface.getEndpoint(i).getType() == UsbConstants.USB_ENDPOINT_XFER_BULK) {
                    // 判断Endpoint是否是读数据端口然后分别绑定读写的端口
                    if (usbInterface.getEndpoint(i).getDirection() == UsbConstants.USB_DIR_IN) {
                        usbEndpointIn = usbInterface.getEndpoint(i);
                    } else {
                        usbEndpointOut = usbInterface.getEndpoint(i);
                    }
                }
            }
            // 打开USB连接
            usbDeviceConnection = manager.openDevice(usbDevice);
            // 获取读写USB权限
            usbDeviceConnection.claimInterface(usbInterface, true);
            dangle = new Dangle(usbEndpointIn, usbEndpointOut, usbDeviceConnection);
            // 先清除Dangle的设置
            dangle.clearRegister();
            // 获取当前系统的活跃模块
            FrequencyModule activeFrequencyModule = DataReadWriteUtil.getActiveFrequencyModule();
            if (activeFrequencyModule != null) {
                // 活跃模块不为空的时候,设置 Dangle 的频点
                dangle.setFrequency(activeFrequencyModule.getFrequency());
            }
            // 完成上述任务之后才可以开始定时从USB中读取数据
            // 交给定时任务线程池去做,延迟一秒之后,每三秒从USB读取一次数据
            if (UsbUtil.getScheduledExecutorService().isShutdown()) {
                // 关闭线程池之后重启创建一个线程池再提交一次任务
                UsbUtil.setScheduledExecutorService(new ScheduledThreadPoolExecutor(1));
            }
            if (UsbUtil.getExecutorService().isShutdown()) {
                executorService = new ThreadPoolExecutor(5, 10,
                        60L, TimeUnit.SECONDS, new ArrayBlockingQueue<>(5));
            }
            // 如果没有Shutdown就直接提交任务
            // 新开一个线程去接收 Dangle 接收器发过来的数据
            new Thread(new ReceiveUsbDataTask(bytes, usbEndpointIn,
                    usbDeviceConnection, DmbPlayerConstant.DMB_READ_TIME.getDmbConstantValue())).start();
        }
    }

    public static ScheduledExecutorService getScheduledExecutorService() {
        return scheduledExecutorService;
    }

    public static void setScheduledExecutorService(ScheduledExecutorService scheduledExecutorService) {
        UsbUtil.scheduledExecutorService = scheduledExecutorService;
    }

    public static ExecutorService getExecutorService() {
        return executorService;
    }

    public static Dangle getDangle() {
        return dangle;
    }
}
