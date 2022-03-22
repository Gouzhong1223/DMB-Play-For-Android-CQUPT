package cn.edu.cqupt.dmb.player.processor;

import android.util.Log;

/**
 * @Author : Gouzhong
 * @Blog : www.gouzhong1223.com
 * @Description : 默认的 DMB 数据处理器
 * @Date : create by QingSong in 2022-03-20 23:55
 * @Email : gouzhong1223@gmail.com
 * @Since : JDK 1.8
 * @PackageName : cn.edu.cqupt.dmb.player.task
 * @ProjectName : DMB Player For Android
 * @Version : 1.0.0
 */
public class DefaultDataProcessor implements DataProcessing {

    private static final String TAG = "DefaultDataProcessor";


    @Override
    public void processData(byte[] usbData) {
        Log.i(TAG, "现在接收到的数据是未知的类型!");
    }
}
