package cn.edu.cqupt.dmb.player.processor.dmb;

import android.util.Log;

import java.util.concurrent.TimeUnit;

import cn.edu.cqupt.dmb.player.common.DangleType;
import cn.edu.cqupt.dmb.player.common.DmbPlayerConstant;
import cn.edu.cqupt.dmb.player.decoder.FicDecoder;
import cn.edu.cqupt.dmb.player.domain.ChannelInfo;
import cn.edu.cqupt.dmb.player.domain.Dangle;
import cn.edu.cqupt.dmb.player.utils.UsbUtil;

/**
 * @Author : Gouzhong
 * @Blog : www.gouzhong1223.com
 * @Description : FIC 类型的数据处理器
 * @Date : create by QingSong in 2022-03-20 23:39
 * @Email : qingsong.qs@alibaba-inc.com
 * @Since : JDK 1.8
 * @PackageName : cn.edu.cqupt.dmb.player.task
 * @ProjectName : DMB Player For Android
 * @Version : 1.0.0
 */
public class FicDataProcessor implements DataProcessing {

    private static final String TAG = "FicDataProcessor";
    /**
     * 是否已经进行了选台
     */
    public static volatile boolean isSelectId;
    /**
     * 初始化Fic解码器
     */
    private static FicDecoder ficDecoder = FicDecoder.getInstance();
    /**
     * 接收单个Fic
     */
    private final byte[] ficBuf = new byte[DmbPlayerConstant.DEFAULT_FIC_SIZE.getDmbConstantValue()];
    /**
     * 子信道
     */
    ChannelInfo channelInfo;
    /**
     * Dangle 实例
     */
    Dangle dangle = new Dangle(UsbUtil.usbEndpointIn, UsbUtil.usbEndpointOut, UsbUtil.usbDeviceConnection);

    static {
        int cnt = 10;
        while (cnt-- > 0) {
            // 尝试获取 Fic 解码器实例
            ficDecoder = FicDecoder.getInstance();
            if (ficDecoder != null) {
                // 不为空直接返回
                break;
            } else {
                // 如果为空就睡一秒,总共尝试十次
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void processData(byte[] usbData, DangleType dangleType) {
        // 从接收到的数据中的第八位开始拷贝fic数据,长度为32
        if (dangleType == DangleType.STM32) {
            System.arraycopy(usbData, DmbPlayerConstant.DEFAULT_DATA_READ_OFFSET.getDmbConstantValue(), ficBuf, 0, DmbPlayerConstant.DEFAULT_FIC_SIZE.getDmbConstantValue());
            // 调用ficDecoder解码器解码fic数据
            ficDecoder.decode(ficBuf);
            // 如果现在的isSelectId为false,那就从fic数据中将ChannelInfo解码提取出来
            if (!isSelectId && (channelInfo = ficDecoder.getSelectChannelInfo()) != null) {
                PseudoBitErrorRateProcessor pseudoBitErrorRateProcessor = (PseudoBitErrorRateProcessor) DataProcessingFactory.getDataProcessor(0x00);
                // 这里需要获取重新设置pseudoBitErrorRateProcessor中的BitRate方便展示信号
                pseudoBitErrorRateProcessor.setBitRate(channelInfo.subChOrganization[6]);
                // 提取出来之后再写回到USB中,也就是设置ChannelInfo
                new Thread(() -> isSelectId = dangle.SetChannel(channelInfo)).start();
                if (!isSelectId) {
                    Log.e(TAG, "设置channelInfo失败!这是往 USB 中设置的时候出错啦!" + channelInfo);
                }
            }
        } else {
            // 这里接收到的 Fic 是 32*12 属于是一次性发完的,所以这里采用循环分包的方式处理
            // 大概是 i*32+8->i*32+40的区间一个包(i在[0,12]区间)
            for (int i = 0; i < DmbPlayerConstant.DMB_READ_TIME.getDmbConstantValue(); i++) {
                System.arraycopy(usbData, DmbPlayerConstant.DEFAULT_DATA_READ_OFFSET.getDmbConstantValue() + i * DmbPlayerConstant.DEFAULT_FIC_SIZE.getDmbConstantValue(), ficBuf, 0, DmbPlayerConstant.DEFAULT_FIC_SIZE.getDmbConstantValue());
                // 调用ficDecoder解码器解码fic数据
                ficDecoder.decode(ficBuf);
                // 如果现在的isSelectId为false,那就从fic数据中将ChannelInfo解码提取出来
                if (!isSelectId && (channelInfo = ficDecoder.getSelectChannelInfo()) != null) {
                    PseudoBitErrorRateProcessor pseudoBitErrorRateProcessor = (PseudoBitErrorRateProcessor) DataProcessingFactory.getDataProcessor(0x00);
                    // 这里需要获取重新设置pseudoBitErrorRateProcessor中的BitRate方便展示信号
                    pseudoBitErrorRateProcessor.setBitRate(channelInfo.subChOrganization[6]);
                    // 提取出来之后再写回到USB中,也就是设置ChannelInfo
                    if (isSelectId) {
                        continue;
                    }
                    // 老 Dangle 最好不要用多线程的方式设置子信道
                    isSelectId = dangle.SetChannel(channelInfo);
                    if (!isSelectId) {
                        Log.e(TAG, "设置channelInfo失败!这是往 USB 中设置的时候出错啦!" + channelInfo);
                    }
                }
            }
        }

    }
}
