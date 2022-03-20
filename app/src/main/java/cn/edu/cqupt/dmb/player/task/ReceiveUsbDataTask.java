package cn.edu.cqupt.dmb.player.task;

import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.util.Log;

import java.io.IOException;

import cn.edu.cqupt.dmb.player.actives.MainActivity;
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
     * 一个FIC的长度
     */
    private static final int DEFAULT_FIC_SIZE = 32;

    /**
     * 读取USB数据的延迟时间
     */
    private static final int DEFAULT_READ_TIME_OUT = 5000;

    /**
     * 读取USB只要数据的偏移量,从data[DEFAULT_DATA_READ_OFFSET]开始读
     */
    private static final int DEFAULT_DATA_READ_OFFSET = 8;


    /**
     * 接收单个Fic
     */
    private final byte[] ficBuf = new byte[DEFAULT_FIC_SIZE];

    /**
     * LOG TAG
     */
    private static final String TAG = "DMB-";

    /**
     * 初始化Fic解码器
     */
    private final FicDecoder ficDecoder = new FicDecoder(MainActivity.mId, MainActivity.mIsEncrypted);

    /**
     * dangle类
     */
    private final Dangle dangle;

    private volatile int ber;

    private volatile int bitRate;

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
    public ReceiveUsbDataTask(byte[] bytes, UsbEndpoint usbEndpointIn, UsbDeviceConnection usbDeviceConnection, Dangle dangle) {
        this.dangle = dangle;
        this.bytes = bytes;
        this.usbEndpointIn = usbEndpointIn;
        this.usbDeviceConnection = usbDeviceConnection;
    }

    @Override
    public void run() {
        // 必须是USB设备已经就绪的情况下才执行,如果USB设备是未就绪或是终端没有插入USB的情况下就直接退出
        if (MainActivity.USB_READY) {
            int dataLength, bbReg0, bbReg3;
            // 每次从USB接收数据的时候,都先将isSelectId设置为false
            boolean isSelectId;
            // 这里读数据必须是获取到读数据的锁才可以操作,这样是为了同步写数据的操作
            usbDeviceConnection.bulkTransfer(usbEndpointIn, bytes, bytes.length, DEFAULT_READ_TIME_OUT);
            // 这里开始判断接收到的DMB数据类型
            // 第三位(从零开始)数据代表当前接收到的数据类型
            ChannelInfo channelInfo;
            DataProcessing dataProcessor = DataProcessingFactory.getDataProcessor(bytes[3]);
            dataProcessor.processData(bytes);
            switch (bytes[3]) {
                // 伪误码率
                case 0x00:
                    if (bytes[6] == 0) {
                        bbReg0 = ((((int) bytes[8]) & 0x00ff) << 8) + ((int) bytes[9] & 0x00ff);
                        bbReg3 = (((int) bytes[14] & 0x00FF) << 8) | (((int) bytes[15]) & 0x00FF);
                        if (bbReg3 == 0) {
                            ber = 2500;
                        } else {
                            ber = bbReg0 * 104 / (32 + bitRate);
                        }
                    }
                    // dmb 数据
                case 0x03:
                    dataLength = (((int) bytes[7]) & 0x0FF);
                    try {
                        DataReadWriteUtil.getOutputStream().write(bytes, DEFAULT_DATA_READ_OFFSET, dataLength);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                case 0x04:
                case 0x05:
                case 0x06:
                case 0x07:
                    // 从接收到的数据中的第八位开始拷贝fic数据,长度为32
                    System.arraycopy(bytes, DEFAULT_DATA_READ_OFFSET, ficBuf, 0, DEFAULT_FIC_SIZE);
                    // 调用ficDecoder解码器解码fic数据
                    ficDecoder.decode(ficBuf);
                    // 如果现在的isSelectId为false,那就从fic数据中将ChannelInfo解码提取出来
                    if ((channelInfo = ficDecoder.getSelectChannelInfo()) != null) {
                        // 提取出来之后再写回到USB中,也就是设置ChannelInfo
                        isSelectId = dangle.SetChannel(channelInfo);
                        if (!isSelectId) {
                            Log.e(TAG, "设置channelInfo失败!" + channelInfo);
                        }
                        Log.e(TAG, channelInfo.toString());
                    }
                    break;
                // 频点设置成功返回信息
                case 0x09:
                    System.out.println(System.currentTimeMillis() + "现在接收到的是0x09,类型为频点设置成功返回信息");
                    break;
                default:
                    break;
            }
//            System.out.println(BaseConversionUtil.bytes2hex(bytes));
//            System.out.println();
        }
    }

    public int getBer() {
        return ber;
    }

    public int getBitRate() {
        return bitRate;
    }

    public byte[] getBytes() {
        return bytes;
    }
}

