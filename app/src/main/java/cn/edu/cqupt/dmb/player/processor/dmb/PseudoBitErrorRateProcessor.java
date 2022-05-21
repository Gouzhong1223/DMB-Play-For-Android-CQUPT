package cn.edu.cqupt.dmb.player.processor.dmb;

import android.util.Log;

import java.io.PipedOutputStream;

import cn.edu.cqupt.dmb.player.common.DangleType;

/**
 * @Author : Gouzhong
 * @Blog : www.gouzhong1223.com
 * @Description : 伪误码率类型的数据处理器
 * @Date : create by QingSong in 2022-03-20 23:40
 * @Email : qingsong.qs@alibaba-inc.com
 * @Since : JDK 1.8
 * @PackageName : cn.edu.cqupt.dmb.player.task
 * @ProjectName : DMB Player For Android
 * @Version : 1.0.0
 */
public class PseudoBitErrorRateProcessor implements DataProcessing {

    private static final String TAG = "PseudoBitErrorRateProcessor";
    int bbReg0, bbReg3;
    private volatile int ber;
    private volatile int bitRate;

    @Override
    public void processData(byte[] usbData, DangleType dangleType, PipedOutputStream pipedOutputStream) {
        this.processData(usbData, dangleType);
    }

    @Override
    public void processData(byte[] usbData, DangleType dangleType) {
//        Log.i(TAG, "现在收到的的数据是伪误码率");
        if (usbData[6] == 0) {
            bbReg0 = ((((int) usbData[8]) & 0x00ff) << 8) + ((int) usbData[9] & 0x00ff);
            bbReg3 = (((int) usbData[14] & 0x00FF) << 8) | (((int) usbData[15]) & 0x00FF);
            if (bbReg3 == 0) {
                ber = 2500;
            } else {
                ber = bbReg0 * 104 / (32 + bitRate);
                Log.i(TAG, "processData: 重设 ber 为:" + ber);
            }
        }
    }

    public int getBer() {
        return ber;
    }

    public void setBitRate(int bitRate) {
        this.bitRate = bitRate;
    }
}
