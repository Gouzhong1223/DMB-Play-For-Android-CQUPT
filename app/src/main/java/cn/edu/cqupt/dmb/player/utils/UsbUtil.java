package cn.edu.cqupt.dmb.player.utils;

import android.content.Context;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

import cn.edu.cqupt.dmb.player.actives.MainActivity;
import cn.edu.cqupt.dmb.player.broadcast.DmbBroadcastReceiver;
import cn.edu.cqupt.dmb.player.common.DmbPlayerConstant;
import cn.edu.cqupt.dmb.player.common.FrequencyModule;
import cn.edu.cqupt.dmb.player.decoder.FicDecoder;
import cn.edu.cqupt.dmb.player.domain.Dangle;
import cn.edu.cqupt.dmb.player.processor.dmb.FicDataProcessor;
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


    private static final String TAG = "UsbUtil";
    /**
     * 缓存从USB读取到的数据,2048是一个待定值
     * TODO 后续根据测试情况修改大小
     */
    private static final byte[] bytes = new byte[DmbPlayerConstant.DEFAULT_DMB_DATA_SIZE.getDmbConstantValue()
            * DmbPlayerConstant.DMB_READ_TIME.getDmbConstantValue()];
    /**
     * 用于缓存USB设备的Map
     */
    public static HashMap<String, UsbDevice> deviceHashMap = new HashMap<>();
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
    private static Dangle dangle;
    private final Integer dangleType;

    public UsbUtil(Integer dangleType) {
        this.dangleType = dangleType;
    }

    /**
     * 重置一些 dangle,主要是清除设置,重新设置频点为默认场景的频点,清理一下ChannelInfo<br/>
     * 然后把 FicDataProcessor.isSelectId 设置为 false
     *
     * @param ficDecoder      ficDecoder
     * @param frequencyModule 默认的工作场景
     */
    public static void restDangle(FicDecoder ficDecoder, FrequencyModule frequencyModule) {
        // 先清除 dangle 的设置
        dangle.clearRegister();
        // 重新设置 dangle 的工作频点
        Log.i(TAG, "重置的频点是:" + frequencyModule.getFrequency());
        // 重置 dangle 的时候将活跃场景设置为参数传入的 FrequencyModule
        DataReadWriteUtil.setActiveFrequencyModule(frequencyModule);
        dangle.setFrequency(frequencyModule.getFrequency());
        // 清空 ficDecoder 的ChannelInfo
        ficDecoder.resetChannelInfos();
        FicDataProcessor.isSelectId = false;
    }

    /**
     * 重置一些 dangle,主要是清除设置,重新设置频点为默认场景的频点,清理一下ChannelInfo<br/>
     * 然后把 FicDataProcessor.isSelectId 设置为 false
     *
     * @param ficDecoder ficDecoder
     * @param frequency  频点
     */
    public static void restDangle(FicDecoder ficDecoder, int frequency) {
        // 先清除 dangle 的设置
        dangle.clearRegister();
        // 重新设置 dangle 的工作频点
        Log.i(TAG, "重置的频点是:" + frequency);
        dangle.setFrequency(frequency);
        // 清空 ficDecoder 的ChannelInfo
        ficDecoder.resetChannelInfos();
        FicDataProcessor.isSelectId = false;
    }

    /**
     * 自定义的 dangle 销毁方法<br/>
     * 主要是把默认的工作场景切换为默认的工作场景,然后MainActivity.id设置为默认的 ID<br/>
     * DataReadWriteUtil.inMainActivity设置为 true
     *
     * @param context 当前context
     */
    public static void dangleDestroy(Context context) {
        FrequencyModule defaultFrequencyModule = DataReadWriteUtil.getDefaultFrequencyModule(context);
        // 结束之后设置活跃场景为默认场景
        DataReadWriteUtil.setActiveFrequencyModule(defaultFrequencyModule);
        // 结束之后将 ID 设置成默认的场景 ID
        MainActivity.id = defaultFrequencyModule.getDeviceID();
        // 再重置一下 Dangle
        restDangle(FicDecoder.getInstance(MainActivity.id, true), defaultFrequencyModule);
        DataReadWriteUtil.inMainActivity = true;
    }

    /**
     * 初始化USB设备并且定时从USB中读取数据
     *
     * @param manager 用于管理USB的UsbManager
     */
    public void initUsb(UsbManager manager) {
        deviceHashMap = manager.getDeviceList();
        for (Map.Entry<String, UsbDevice> stringUsbDeviceEntry : deviceHashMap.entrySet()) {
            UsbDevice usbDevice = stringUsbDeviceEntry.getValue();
            DmbBroadcastReceiver.DmbUsbDevice dmbUsbDevice = new DmbBroadcastReceiver.DmbUsbDevice(usbDevice.getVendorId(), usbDevice.getProductId());
            if (!DmbBroadcastReceiver.checkUsbDevice(dmbUsbDevice)) {
                continue;
            }
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
            // 如果没有Shutdown就直接提交任务
            // 新开一个线程去接收 Dangle 接收器发过来的数据
            new Thread(new ReceiveUsbDataTask(bytes, usbEndpointIn,
                    usbDeviceConnection, DmbPlayerConstant.DMB_READ_TIME.getDmbConstantValue(), dangleType)).start();
        }
    }

}
