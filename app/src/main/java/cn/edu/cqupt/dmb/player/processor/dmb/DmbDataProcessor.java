package cn.edu.cqupt.dmb.player.processor.dmb;

import android.util.Log;

import java.io.IOException;
import java.io.PipedOutputStream;

import cn.edu.cqupt.dmb.player.common.DangleType;
import cn.edu.cqupt.dmb.player.common.DmbPlayerConstant;
import cn.edu.cqupt.dmb.player.decoder.AbstractDmbDecoder;
import cn.edu.cqupt.dmb.player.utils.DataReadWriteUtil;

/**
 * @Author : Gouzhong
 * @Blog : www.gouzhong1223.com
 * @Description : DMB 类型的数据处理器
 * @Date : create by QingSong in 2022-03-20 23:41
 * @Email : qingsong.qs@alibaba-inc.com
 * @Since : JDK 1.8
 * @PackageName : cn.edu.cqupt.dmb.player.task
 * @ProjectName : DMB Player For Android
 * @Version : 1.0.0
 */
public class DmbDataProcessor implements DataProcessing {

    private static final String TAG = "DmbDataProcessor";


    /**
     * USB 数据输出流
     */
    private static final PipedOutputStream PIPED_OUTPUT_STREAM = new PipedOutputStream();

    static {
        try {
            // 直接获取抽象解码器的 pip 输出流
            PIPED_OUTPUT_STREAM.connect(AbstractDmbDecoder.getPipedInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void processData(byte[] usbData, DangleType dangleType) {
        int dataLength;
        if (dangleType == DangleType.STM32) {
            dataLength = (((int) usbData[7]) & 0x0FF);
        } else {
            dataLength = (((int) usbData[6] & 0x0FF) << 8) | (((int) usbData[7]) & 0x0FF);
        }
        try {
            if (DataReadWriteUtil.inMainActivity) {
                // 如果现在没有活跃的使用场景,就直接抛弃当前接收到的数据并返回
                return;
            }
            PIPED_OUTPUT_STREAM.write(usbData, DmbPlayerConstant.DEFAULT_DATA_READ_OFFSET.getDmbConstantValue(), dataLength);
            if (!DataReadWriteUtil.initFlag) {
                DataReadWriteUtil.initFlag = true;
            }
            // 写完 flush 一下
            PIPED_OUTPUT_STREAM.flush();
        } catch (IOException e) {
            Log.e(TAG, "处理 DMB 数据出错啦!---" + e);
            e.printStackTrace();
        }
    }
}
