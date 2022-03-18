package cn.edu.cqupt.dmb.player.utils;

import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import cn.edu.cqupt.dmb.player.task.PutDataToUsbTask;
import cn.edu.cqupt.dmb.player.task.ReceiveUsbDataTask;

/**
 * @Author : Gouzhong
 * @Blog : www.gouzhong1223.com
 * @Description : 这个是USB的工具类
 * @Date : create by QingSong in 2022-03-12 14:22
 * @Email : gouzhong1223@gmail.com
 * @Since : JDK 1.8
 * @PackageName : com.gouzhong1223.androidtvtset_1.utils
 * @ProjectName : Android TV Tset-1
 * @Version : 1.0.0
 */
public class UsbUtil {

    private final byte[] reqMsg = new byte[48];
    private final byte[] reqMsg2 = new byte[48];


    /**
     * 定时任务线程池
     */
    private static ScheduledExecutorService scheduledExecutorService;

    static {
        // JVM启动的时候初始化线程池,由于只有一个任务,所以核心线程就只设置一个
        scheduledExecutorService = new ScheduledThreadPoolExecutor(1);
    }

    /**
     * MX_RF_I2C_ADDRESS
     */
    private static final byte MX_RF_I2C_ADDRESS = (byte) 0xc0;

    /**
     * 重邮DMB频点
     */
    public static final int FREQKHZ = 220352;

    /**
     * 装载RF信息
     */
    private final static byte[] frequency = new byte[48];

    private final byte[] rxMsg = new byte[64];

    /**
     * 默认的任务延迟时间
     */
    private static final long TASK_DEFAULT_DELAY_TIME = 0L;
    /**
     * 默认的任务间隔时间
     */
    private static final long TASK_DEFAULT_INTERVAL = 10L;
    /**
     * 用于缓存USB设备的Map
     */
    public static HashMap<String, UsbDevice> deviceHashMap = new HashMap<>();
    /**
     * 缓存从USB读取到的数据,2048是一个待定值
     * TODO 后续根据测试情况修改大小
     */
    private static final byte[] bytes = new byte[1024];
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
            // 设置RF频段
            UsbUtil.initFrequency(frequency);
            // 发送RF设置
            new PutDataToUsbTask(frequency, usbEndpointOut, usbDeviceConnection).run();
            System.out.println("设置频点为:" + FREQKHZ);
            new ReceiveUsbDataTask(new byte[64], usbEndpointIn, usbDeviceConnection).run();
            // 生成第一次写入USB的数据
            generateReqMsg(1, reqMsg);
            // 生成第二次写入USB的数据
            generateReqMsg(2, reqMsg2);
            // 在真正读取数据之前,应该先完成一次收,两次发的动作,先后顺序是发收发
            new PutDataToUsbTask(reqMsg, usbEndpointOut, usbDeviceConnection).run();
            new ReceiveUsbDataTask(rxMsg, usbEndpointIn, usbDeviceConnection).run();
            new PutDataToUsbTask(reqMsg2, usbEndpointOut, usbDeviceConnection).run();
            // 完成上述任务之后才可以开始定时从USB中读取数据
            // 交给定时任务线程池去做,延迟一秒之后,每三秒从USB读取一次数据
            if (UsbUtil.getScheduledExecutorService().isShutdown()) {
                // 关闭线程池之后重启创建一个线程池再提交一次任务
                UsbUtil.setScheduledExecutorService(new ScheduledThreadPoolExecutor(1));
            }
            // 如果没有Shutdown就直接提交任务
            scheduledExecutorService.scheduleAtFixedRate(new ReceiveUsbDataTask(bytes, usbEndpointIn, usbDeviceConnection),
                    TASK_DEFAULT_DELAY_TIME, TASK_DEFAULT_INTERVAL, TimeUnit.SECONDS);
        }
    }

    public static ScheduledExecutorService getScheduledExecutorService() {
        return scheduledExecutorService;
    }

    public static void setScheduledExecutorService(ScheduledExecutorService scheduledExecutorService) {
        UsbUtil.scheduledExecutorService = scheduledExecutorService;
    }

    /**
     * 根据类型生成发送的数据格式
     *
     * @param type   类型
     * @param reqMsg 数据
     */
    public static void generateReqMsg(int type, byte[] reqMsg) {
        if (reqMsg == null || reqMsg.length == 0) {
            return;
        }
        reqMsg[0] = (byte) 0xff;
        reqMsg[1] = (byte) 0xff;
        if (type == 1) {
            reqMsg[2] = (byte) 0x10;
        } else if (type == 2) {
            reqMsg[2] = (byte) 0x11;
        }
        reqMsg[3] = (byte) '9';
        reqMsg[4] = (byte) 'C';
        reqMsg[5] = (byte) 'Q';
        reqMsg[6] = (byte) 'U';
        reqMsg[7] = (byte) 'P';
        reqMsg[8] = (byte) 'T';
        reqMsg[9] = (byte) 'D';
        reqMsg[10] = (byte) 'M';
        reqMsg[11] = (byte) 'B';
        reqMsg[12] = (byte) 0x00;
    }

    /**
     * 设置DMB接收机的频点并且发送
     */
    public static void initFrequency(byte[] frequency) {

        int byte_cnt = 0;

        frequency[byte_cnt++] = (byte) 0xff;
        frequency[byte_cnt++] = (byte) 0xff;
        frequency[byte_cnt++] = 0x01;
        frequency[byte_cnt++] = 0x2c;
        frequency[byte_cnt++] = MX_RF_I2C_ADDRESS;
        frequency[byte_cnt++] = 1;
        frequency[byte_cnt++] = 1;
        frequency[byte_cnt++] = 0;
        frequency[byte_cnt++] = 0;
        frequency[byte_cnt++] = 0;
        frequency[byte_cnt++] = 0;
        frequency[byte_cnt++] = 4;
        frequency[byte_cnt++] = (FREQKHZ >> 24);
        frequency[byte_cnt++] = (FREQKHZ >> 16);
        frequency[byte_cnt++] = (byte) (FREQKHZ >> 8);
        frequency[byte_cnt] = (byte) (FREQKHZ);
    }
}
