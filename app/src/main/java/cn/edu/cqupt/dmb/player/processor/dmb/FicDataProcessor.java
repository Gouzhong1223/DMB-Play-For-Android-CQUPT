package cn.edu.cqupt.dmb.player.processor.dmb;

import android.util.Log;

import cn.edu.cqupt.dmb.player.actives.MainActivity;
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
    public static volatile boolean isSelectId = false;
    /**
     * 初始化Fic解码器
     */
    private final FicDecoder ficDecoder = FicDecoder.getInstance(MainActivity.id, true);
    /**
     * 接收单个Fic
     */
    private final byte[] ficBuf = new byte[DmbPlayerConstant.DEFAULT_FIC_SIZE.getDmbConstantValue()];
    ChannelInfo channelInfo;
    Dangle dangle = new Dangle(UsbUtil.usbEndpointIn, UsbUtil.usbEndpointOut, UsbUtil.usbDeviceConnection);

    @Override
    public void processData(byte[] usbData, Integer dangleType) {
//        Log.i(TAG, "现在接收到的数据是 FIC 类型!");
        // 从接收到的数据中的第八位开始拷贝fic数据,长度为32
        if (dangleType == 2) {
            System.arraycopy(usbData, DmbPlayerConstant.DEFAULT_DATA_READ_OFFSET.getDmbConstantValue(), ficBuf, 0, DmbPlayerConstant.DEFAULT_FIC_SIZE.getDmbConstantValue());
            // 我们的 ID 是可变的,但是这个ficDecoder是单例生成的,所以这里,重新设置一下 ID,ficDecoder搞成单例的意义我也不知道是为什么,现在不想改了
            ficDecoder.setId(MainActivity.id);
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
            for (int i = 0; i < DmbPlayerConstant.DMB_READ_TIME.getDmbConstantValue(); i++) {
                System.arraycopy(usbData, DmbPlayerConstant.DEFAULT_DATA_READ_OFFSET.getDmbConstantValue() + i * DmbPlayerConstant.DEFAULT_FIC_SIZE.getDmbConstantValue(), ficBuf, 0, DmbPlayerConstant.DEFAULT_FIC_SIZE.getDmbConstantValue());
                // 我们的 ID 是可变的,但是这个ficDecoder是单例生成的,所以这里,重新设置一下 ID,ficDecoder搞成单例的意义我也不知道是为什么,现在不想改了
                ficDecoder.setId(MainActivity.id);
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
                    isSelectId = dangle.SetChannel(channelInfo);
//                    new Thread(() -> isSelectId = dangle.SetChannel(channelInfo)).start();
                    if (!isSelectId) {
                        Log.e(TAG, "设置channelInfo失败!这是往 USB 中设置的时候出错啦!" + channelInfo);
                    }
                }
            }
        }

    }
}
