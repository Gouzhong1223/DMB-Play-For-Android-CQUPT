package cn.edu.cqupt.dmb.player.processor.dmb;

import android.util.Log;

import java.io.IOException;
import java.io.PipedOutputStream;

import cn.edu.cqupt.dmb.player.common.DmbPlayerConstant;
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


    static PipedOutputStream pipedOutputStream = new PipedOutputStream();

    static {
        try {
            pipedOutputStream.connect(TpegDecoderImprovement.getPipedInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void processData(byte[] usbData) {
//        Log.i(TAG, "现在接收到的数据是 DMB 数据");
        int dataLength = (((int) usbData[7]) & 0x0FF);
        try {
            pipedOutputStream.write(usbData, DmbPlayerConstant.DEFAULT_DATA_READ_OFFSET.getDmbConstantValue(), dataLength);
            if (!DataReadWriteUtil.initFlag) {
                DataReadWriteUtil.initFlag = true;
            }
        } catch (IOException e) {
            Log.e(TAG, "处理 DMB 数据出错啦!---" + e);
            e.printStackTrace();
        }
    }
}
