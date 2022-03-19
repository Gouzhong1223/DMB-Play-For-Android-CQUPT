package cn.edu.cqupt.dmb.player.task;

import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.util.Log;

import cn.edu.cqupt.dmb.player.actives.MainActivity;
import cn.edu.cqupt.dmb.player.decoder.FicDecoder;
import cn.edu.cqupt.dmb.player.domain.ChannelInfo;
import cn.edu.cqupt.dmb.player.domain.Dangle;
import cn.edu.cqupt.dmb.player.utils.BaseConversionUtil;


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


    private final byte[] ficBuf = new byte[32];

    private static final String TAG = "DMB-";

    private final FicDecoder ficDecoder = new FicDecoder(MainActivity.mId, MainActivity.mIsEncrypted);

    private final Dangle dangle;

    /**
     * 存储从USB中读取到的数据
     */
    private byte[] bytes;
    /**
     * 读取USB数据的端口
     */
    private final UsbEndpoint usbEndpointIn;
    /**
     * 已经打开的USB链接
     */
    private final UsbDeviceConnection usbDeviceConnection;

    public byte[] getBytes() {
        return bytes;
    }

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
            // 每次从USB接收数据的时候,都先将isSelectId设置为false
            boolean isSelectId;
            // 这里读数据必须是获取到读数据的锁才可以操作,这样是为了同步写数据的操作
            usbDeviceConnection.bulkTransfer(usbEndpointIn, bytes, bytes.length, 5000);
            // 这里开始判断接收到的DMB数据类型
            // 第三位(从零开始)数据代表当前接收到的数据类型
            ChannelInfo channelInfo;
            switch (bytes[3]) {
                // 伪误码率
                case 0x00:
                    System.out.println(System.currentTimeMillis() + "现在接收到的是0x00,类型为伪误码率");
                    break;
                // dmb 数据
                case 0x03:
                    // 有效数据长度，从buf[8] 开始
                    int dataLength = (((int) bytes[7]) & 0x0FF);
                    break;
                case 0x04:
                    // 从接收到的数据中的第八位开始拷贝fic数据,长度为32
                    System.arraycopy(bytes, 8, ficBuf, 0, 32);
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
                case 0x05:
                    System.arraycopy(bytes, 8, ficBuf, 0, 32);
                    ficDecoder.decode(ficBuf);
                    break;
                case 0x06:
                    // FIC 数据,从buf[8] 开始，32 字节
                    System.out.println(System.currentTimeMillis() + "现在接收到的是0x06,类型为伪误码率");
                    break;
                case 0x07:
                    System.out.println(System.currentTimeMillis() + "现在接收到的是0x07,类型为伪误码率");
                    break;
                // 频点设置成功返回信息
                case 0x09:
                    System.out.println(System.currentTimeMillis() + "现在接收到的是0x09,类型为频点设置成功返回信息");
                    break;
                default:
                    break;
            }
            System.out.println(BaseConversionUtil.bytes2hex(bytes));
            System.out.println();
        }
    }
}

