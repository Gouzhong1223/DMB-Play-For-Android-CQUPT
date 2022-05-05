package cn.edu.cqupt.dmb.player.listener;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import cn.edu.cqupt.dmb.player.common.FrequencyModule;
import cn.edu.cqupt.dmb.player.utils.DataReadWriteUtil;

/**
 * @Author : Gouzhong
 * @Blog : www.gouzhong1223.com
 * @Description : 这个是监听课表数据的监听器
 * @Date : create by QingSong in 2022-04-07 14:52
 * @Email : qingsong.qs@alibaba-inc.com
 * @Since : JDK 1.8
 * @PackageName : cn.edu.cqupt.dmb.player.listener
 * @ProjectName : DMB Player For Android
 * @Version : 1.0.0
 */
public class DmbCurriculumListener implements DmbListener {

    public static final int MESSAGE_UPDATE_PICTURE = 0x100;
    private static final String TAG = "DmbCurriculumListener";
    private final Handler handler;
    /**
     * 文件缓冲区
     */
    private final byte[] fileBuffer = new byte[1024 * 1024 * 2];
    private Integer length;

    public DmbCurriculumListener(Handler handler) {
        this.handler = handler;
    }

    @Override
    public void onSuccess(String fileName, byte[] bytes, int length, Context context) {
        FrequencyModule frequencyModule = DataReadWriteUtil.getActiveFrequencyModule();
        if (frequencyModule == null) {
            Log.e(TAG, "出错啦!现在没有设置活跃的组件,所以这里的回调方法就直接抛弃!");
            return;
        }
        if (!frequencyModule.getModuleName().startsWith("CURRICULUM")) {
            // 活跃组件不是课表,直接返回
            Log.e(TAG, "当前活跃的组件不是课表");
            return;
        }
        String needBuilding = frequencyModule.getModuleName().split("-")[1];
        if (!fileName.contains(needBuilding)) {
            // 如果不是需要的课表,就直接返回
            return;
        }
        this.length = length;
        System.arraycopy(bytes, 0, fileBuffer, 0, length);
        handler.sendEmptyMessage(MESSAGE_UPDATE_PICTURE);
    }

    @Override
    public void onReceiveMessage(String msg) {

    }

    public byte[] getFileBuffer() {
        return fileBuffer;
    }

    public Integer getLength() {
        return length;
    }
}
