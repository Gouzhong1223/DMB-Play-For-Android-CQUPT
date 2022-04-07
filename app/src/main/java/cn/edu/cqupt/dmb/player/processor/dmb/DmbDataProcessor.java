package cn.edu.cqupt.dmb.player.processor.dmb;

import android.util.Log;

import java.io.IOException;
import java.io.PipedOutputStream;

import cn.edu.cqupt.dmb.player.common.DmbPlayerConstant;
import cn.edu.cqupt.dmb.player.common.FrequencyModule;
import cn.edu.cqupt.dmb.player.decoder.MpegTsDecoder;
import cn.edu.cqupt.dmb.player.decoder.TpegDecoderImprovement;
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
     * 图片的输出流
     */
    static PipedOutputStream tpegPipedOutputStream = new PipedOutputStream();
    /**
     * 视频的输出流
     */
    static PipedOutputStream mpegTsPipedOutputStream = new PipedOutputStream();

    static {
        try {
            tpegPipedOutputStream.connect(TpegDecoderImprovement.getPipedInputStream());
            mpegTsPipedOutputStream.connect(MpegTsDecoder.getPipedInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void processData(byte[] usbData) {
        int dataLength = (((int) usbData[7]) & 0x0FF);
        try {
            PipedOutputStream activeModulePip = getActiveModulePip();
            if (activeModulePip == null) {
                return;
            }
            activeModulePip.write(usbData, DmbPlayerConstant.DEFAULT_DATA_READ_OFFSET.getDmbConstantValue(), dataLength);
            if (!DataReadWriteUtil.initFlag) {
                DataReadWriteUtil.initFlag = true;
            }
        } catch (IOException e) {
            Log.e(TAG, "处理 DMB 数据出错啦!---" + e);
            e.printStackTrace();
        }
    }

    /**
     * 获取现在活跃的模块输出流
     *
     * @return PipedOutputStream
     */
    private PipedOutputStream getActiveModulePip() {
        FrequencyModule frequencyModule = DataReadWriteUtil.getFrequencyModule();
        if (frequencyModule == null) {
            // 如果当前还没有设置活跃模块,就直接返回一个空对象
            return null;
        }
        if (frequencyModule.getModuleName().equals(FrequencyModule.OUTDOOR_SCREEN_VIDEO.getModuleName())) {
            // 视频的输入流
            return mpegTsPipedOutputStream;
        } else {
            // 直接返回 图片的输入流
            return tpegPipedOutputStream;
        }
    }
}
