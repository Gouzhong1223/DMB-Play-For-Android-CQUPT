package cn.edu.cqupt.dmb.player.processor.tpeg;

import android.util.Log;

/**
 * @Author : Gouzhong
 * @Blog : www.gouzhong1223.com
 * @Description : 默认的 TPEG 数据处理器
 * @Date : create by QingSong in 2022-03-22 23:27
 * @Email : qingsong.qs@alibaba-inc.com
 * @Since : JDK 1.8
 * @PackageName : cn.edu.cqupt.dmb.player.processor.tpeg
 * @ProjectName : DMB Player For Android
 * @Version : 1.0.0
 */
public class DefaultTpegDataProcessing implements TpegDataProcessing {
    private static final String TAG = "DefaultTpegDataProcessing";


    @Override
    public void processData(byte[] tpegBuffer, byte[] tpegData, int[] tpegInfo) {
        Log.e(TAG, "未知的 TPEG 数据类型");
    }
}
