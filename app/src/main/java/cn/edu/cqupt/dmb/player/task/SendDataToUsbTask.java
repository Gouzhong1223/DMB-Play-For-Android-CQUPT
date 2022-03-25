package cn.edu.cqupt.dmb.player.task;

import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.util.Log;

import java.util.concurrent.Callable;

import cn.edu.cqupt.dmb.player.actives.MainActivity;
import cn.edu.cqupt.dmb.player.common.DmbPlayerConstant;
import cn.edu.cqupt.dmb.player.utils.DataReadWriteUtil;


/**
 * @Author : Gouzhong
 * @Blog : www.gouzhong1223.com
 * @Description : 这是往USB中写数据的任务
 * @Date : create by QingSong in 2022-03-15 15:49
 * @Email : qingsong.qs@alibaba-inc.com
 * @Since : JDK 1.8
 * @PackageName : cn.edu.cqupt.dmb.player.task
 * @ProjectName : DMB Player For Android
 * @Version : 1.0.0
 */
public class SendDataToUsbTask implements Callable<Integer> {

    private static final String TAG = "SendDataToUsbTask-";


    /**
     * 读写数据的超时时间
     */
    int TIMEOUT = DmbPlayerConstant.DEFAULT_READ_TIME_OUT.getDmbConstantValue();
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

    public SendDataToUsbTask(byte[] bytes, UsbEndpoint usbEndpointIn, UsbDeviceConnection usbDeviceConnection) {
        this.bytes = bytes;
        this.usbEndpointIn = usbEndpointIn;
        this.usbDeviceConnection = usbDeviceConnection;
    }


    @Override
    public Integer call() {
        int bulkTransfer = 0;
        // 必须是USB设备已经就绪的情况下才执行,如果USB设备是未就绪或是终端没有插入USB的情况下就直接退出
        if (DataReadWriteUtil.USB_READY) {
            bulkTransfer = usbDeviceConnection.bulkTransfer(usbEndpointIn, bytes, bytes.length, TIMEOUT);
            if (bulkTransfer != bytes.length) {
                Log.e(TAG, "发送数据到USB失败!");
            }
        }
        return bulkTransfer;
    }
}
