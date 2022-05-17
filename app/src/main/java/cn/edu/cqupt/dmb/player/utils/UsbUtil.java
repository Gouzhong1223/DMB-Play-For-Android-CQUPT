package cn.edu.cqupt.dmb.player.utils;

import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

import cn.edu.cqupt.dmb.player.broadcast.DmbBroadcastReceiver;
import cn.edu.cqupt.dmb.player.common.DangleType;
import cn.edu.cqupt.dmb.player.common.DmbPlayerConstant;
import cn.edu.cqupt.dmb.player.decoder.FicDecoder;
import cn.edu.cqupt.dmb.player.domain.Dangle;
import cn.edu.cqupt.dmb.player.domain.SceneInfo;
import cn.edu.cqupt.dmb.player.domain.SceneVO;
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
     */
    private static final byte[] BYTES_STM32 = new byte[DmbPlayerConstant.DEFAULT_DMB_DATA_SIZE.getDmbConstantValue()
            * DmbPlayerConstant.DMB_READ_TIME.getDmbConstantValue()];

    /**
     * 缓存从USB读取到的数据,2048是一个待定值
     */
    private static final byte[] BYTES_NUC = new byte[776];
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
    /**
     * Dangle 实例
     */
    private static Dangle dangle;
    /**
     * Dangel 类型
     */
    private static DangleType dangleType;

    public UsbUtil(DangleType dangleType) {
        UsbUtil.dangleType = dangleType;
    }

    /**
     * 重置一些 dangle,主要是清除设置,重新设置频点为默认场景的频点,清理一下ChannelInfo<br/>
     * 然后把 FicDataProcessor.isSelectId 设置为 false
     *
     * @param ficDecoder      ficDecoder
     * @param selectedSceneVO 工作场景
     */
    public static void restDangle(FicDecoder ficDecoder, SceneVO selectedSceneVO) {
        for (int i = 0; i < 3; i++) {
            // 先清除 dangle 的设置
            dangle.clearRegister();
        }
        for (int i = 0; i < 3; i++) {
            // 重新设置 dangle 的工作频点
            Log.i(TAG, "重置的频点是:" + selectedSceneVO.getFrequency());
            dangle.setFrequency(selectedSceneVO.getFrequency());
        }
        // 清空 ficDecoder 的ChannelInfo
        ficDecoder.resetChannelInfos();
        try {
            // 重置子信道数据之后等 300 毫秒
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        FicDataProcessor.isSelectId = false;
    }

    /**
     * 开始接收 DMB 数据
     */
    public static void startReceiveDmbData() {
        // 设置为非主页
        DataReadWriteUtil.inMainActivity = false;
        // 新开一个线程去接收 Dangle 接收器发过来的数据
        // 开始前根据 Dangle 的类型,选择数据接收容器
        new Thread(new ReceiveUsbDataTask(dangleType == DangleType.STM32 ? BYTES_STM32 : BYTES_NUC, usbEndpointIn,
                usbDeviceConnection, DmbPlayerConstant.DMB_READ_TIME.getDmbConstantValue(), dangleType)).start();
        Log.i(TAG, "startReceiveDmbData: 开始从 USB 中接收数据");
    }

    /**
     * 初始化USB设备并且定时从USB中读取数据
     *
     * @param manager 用于管理USB的UsbManager
     */
    public void initUsb(UsbManager manager, SceneInfo defaultSceneInfo) {
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
            if (defaultSceneInfo != null) {
                dangle.setFrequency(defaultSceneInfo.getFrequency());
            }
        }
    }
}
