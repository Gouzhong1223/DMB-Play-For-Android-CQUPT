package cn.edu.cqupt.dmb.player.processor.dmb;

import android.util.Log;

/**
 * @Author : Gouzhong
 * @Blog : www.gouzhong1223.com
 * @Description : 频点设置成功的数据处理器
 * @Date : create by QingSong in 2022-03-20 23:52
 * @Email : qingsong.qs@alibaba-inc.com
 * @Since : JDK 1.8
 * @PackageName : cn.edu.cqupt.dmb.player.task
 * @ProjectName : DMB Player For Android
 * @Version : 1.0.0
 */
public class FrequencySetSuccessProcessor implements DataProcessing {

    private static final String TAG = "FrequencySetSuccessProcessor";

    @Override
    public void processData(byte[] usbData, Integer dangleType) {
        Log.i(TAG, "现在接收到的是0x09,类型为频点设置成功返回信息");
    }
}
