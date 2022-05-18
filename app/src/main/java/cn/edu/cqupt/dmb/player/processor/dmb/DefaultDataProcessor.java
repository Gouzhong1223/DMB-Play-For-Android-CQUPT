package cn.edu.cqupt.dmb.player.processor.dmb;

import android.util.Log;

import java.io.PipedOutputStream;

import cn.edu.cqupt.dmb.player.common.DangleType;

/**
 * @Author : Gouzhong
 * @Blog : www.gouzhong1223.com
 * @Description : 默认的 DMB 数据处理器
 * @Date : create by QingSong in 2022-03-20 23:55
 * @Email : qingsong.qs@alibaba-inc.com
 * @Since : JDK 1.8
 * @PackageName : cn.edu.cqupt.dmb.player.task
 * @ProjectName : DMB Player For Android
 * @Version : 1.0.0
 */
public class DefaultDataProcessor implements DataProcessing {

    private static final String TAG = "DefaultDataProcessor";

    @Override
    public void processData(byte[] usbData, DangleType dangleType, PipedOutputStream pipedOutputStream) {
        this.processData(usbData, dangleType);
    }

    @Override
    public void processData(byte[] usbData, DangleType dangleType) {
        Log.e(TAG, "接收到类型未知的数据,无法处理!");
    }
}
