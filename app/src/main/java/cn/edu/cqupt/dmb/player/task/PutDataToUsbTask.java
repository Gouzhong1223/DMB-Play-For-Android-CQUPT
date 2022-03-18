package cn.edu.cqupt.dmb.player.task;

import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;

import cn.edu.cqupt.dmb.player.actives.MainActivity;
import cn.edu.cqupt.dmb.player.utils.BaseConversionUtil;


/**
 * @Author : Gouzhong
 * @Blog : www.gouzhong1223.com
 * @Description : 这是往USB中写数据的任务
 * @Date : create by QingSong in 2022-03-15 15:49
 * @Email : gouzhong1223@gmail.com
 * @Since : JDK 1.8
 * @PackageName : com.gouzhong1223.androidtvtset_1.task
 * @ProjectName : DMB Player For Android 
 * @Version : 1.0.0
 */
public class PutDataToUsbTask implements Runnable {


    /**
     * 读取数据的超时时间
     */
    int TIMEOUT = 0;
    /**
     * 存储从USB中读取到的数据
     */
    private final byte[] bytes;
    /**
     * 读取USB数据的端口
     */
    private final UsbEndpoint usbEndpointIn;
    /**
     * 已经打开的USB链接
     */
    private final UsbDeviceConnection usbDeviceConnection;

    public PutDataToUsbTask(byte[] bytes, UsbEndpoint usbEndpointIn, UsbDeviceConnection usbDeviceConnection) {
        this.bytes = bytes;
        this.usbEndpointIn = usbEndpointIn;
        this.usbDeviceConnection = usbDeviceConnection;
    }


    @Override
    public void run() {
        // 必须是USB设备已经就绪的情况下才执行,如果USB设备是未就绪或是终端没有插入USB的情况下就直接退出
        if (MainActivity.USB_READY) {
            int bulkTransfer = usbDeviceConnection.bulkTransfer(usbEndpointIn, bytes, bytes.length, 1000);
            if (bulkTransfer != bytes.length) {
                System.out.println("写入数据失败了!");
            }
            System.out.println("写入:" + BaseConversionUtil.bytes2hex(bytes));
        }
    }
}
