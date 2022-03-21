package cn.edu.cqupt.dmb.player.task;

import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.util.Log;

import java.io.IOException;

import cn.edu.cqupt.dmb.player.actives.MainActivity;
import cn.edu.cqupt.dmb.player.common.DmbPlayerConstant;
import cn.edu.cqupt.dmb.player.decoder.FicDecoder;
import cn.edu.cqupt.dmb.player.domain.ChannelInfo;
import cn.edu.cqupt.dmb.player.domain.Dangle;
import cn.edu.cqupt.dmb.player.processor.DataProcessing;
import cn.edu.cqupt.dmb.player.processor.DataProcessingFactory;
import cn.edu.cqupt.dmb.player.utils.DataReadWriteUtil;


/**
 * @Author : Gouzhong
 * @Blog : www.gouzhong1223.com
 * @Description : 这是一个从USB中读取数据的线程,启动依赖{@link java.util.concurrent.ScheduledExecutorService}的调度
 * @Date : create by QingSong in 2022-03-11 16:21
 * @Email : gouzhong1223@gmail.com
 * @Since : JDK 1.8
 * @PackageName : com.gouzhong1223.androidtvtset_1.task
 * @ProjectName : DMB Player For Android
 * @Version : 1.0.0
 */
public class ReceiveUsbDataTask implements Runnable {

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

    /**
     * 定时任务构造器
     *
     * @param bytes               用于存储从USB中读取到的数据
     * @param usbEndpointIn       读取USB数据的端口
     * @param usbDeviceConnection 已经打开的USB链接
     */
    public ReceiveUsbDataTask(byte[] bytes, UsbEndpoint usbEndpointIn, UsbDeviceConnection usbDeviceConnection) {
        this.bytes = bytes;
        this.usbEndpointIn = usbEndpointIn;
        this.usbDeviceConnection = usbDeviceConnection;
    }

    @Override
    public void run() {
        // 必须是USB设备已经就绪的情况下才执行,如果USB设备是未就绪或是终端没有插入USB的情况下就直接退出
        if (MainActivity.USB_READY) {
            // 每次从USB接收数据的时候,都先将isSelectId设置为false
            // 这里读数据必须是获取到读数据的锁才可以操作,这样是为了同步写数据的操作
            usbDeviceConnection.bulkTransfer(usbEndpointIn, bytes, bytes.length, DmbPlayerConstant.DEFAULT_READ_TIME_OUT.getDmbConstantValue());
            // 这里开始判断接收到的DMB数据类型
            // 第三位(从零开始)数据代表当前接收到的数据类型
            DataProcessing dataProcessor = DataProcessingFactory.getDataProcessor(bytes[3]);
            dataProcessor.processData(bytes);
        }
    }

    public byte[] getBytes() {
        return bytes;
    }
}

