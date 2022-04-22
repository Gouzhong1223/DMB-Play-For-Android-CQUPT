package cn.edu.cqupt.dmb.player.processor.dmb;

import android.util.Log;

import java.io.IOException;
import java.io.PipedOutputStream;

import cn.edu.cqupt.dmb.player.common.DmbPlayerConstant;
import cn.edu.cqupt.dmb.player.common.FrequencyModule;
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
    private static final PipedOutputStream pipedOutputStream = new PipedOutputStream();

    static {
        try {
            // 直接获取抽象解码器的 pip 输出流
            pipedOutputStream.connect(AbstractDmbDecoder.getPipedInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void processData(byte[] usbData) {
//        Log.i(TAG, "现在接收到的数据是 DMB 类型!");
        int dataLength = (((int) usbData[7]) & 0x0FF);
        PipedOutputStream activeModulePip = getActiveModulePip();
        try {
            if (activeModulePip == null) {
                // 如果现在没有活跃的使用场景,就直接抛弃当前接收到的数据并返回
                return;
            }
            activeModulePip.write(usbData, DmbPlayerConstant.DEFAULT_DATA_READ_OFFSET.getDmbConstantValue(), dataLength);
            if (!DataReadWriteUtil.initFlag) {
                DataReadWriteUtil.initFlag = true;
            }
            // 写完 flush 一下
            activeModulePip.flush();
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
        FrequencyModule frequencyModule = DataReadWriteUtil.getActiveFrequencyModule();
        if (DataReadWriteUtil.inMainActivity) {
            // 如果现在用户正在主页面,就直接返回了
            return null;
        }
        if (frequencyModule == null) {
            // 如果当前还没有设置活跃模块,就直接返回一个空对象
            return null;
        }
        // TODO 其余类型的 pip 输出流
        return pipedOutputStream;
    }
}
