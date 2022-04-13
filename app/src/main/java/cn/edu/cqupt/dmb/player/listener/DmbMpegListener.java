package cn.edu.cqupt.dmb.player.listener;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.RequiresApi;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

import cn.edu.cqupt.dmb.player.common.DmbPlayerConstant;
import cn.edu.cqupt.dmb.player.utils.DataReadWriteUtil;

/**
 * @Author : Gouzhong
 * @Blog : www.gouzhong1223.com
 * @Description : 这是DMB播放器播放视频的监听器
 * @Date : create by QingSong in 2022-04-07 16:12
 * @Email : qingsong.qs@alibaba-inc.com
 * @Since : JDK 1.8
 * @PackageName : cn.edu.cqupt.dmb.player.listener
 * @ProjectName : DMB Player For Android
 * @Version : 1.0.0
 */
@RequiresApi(api = Build.VERSION_CODES.R)
public class DmbMpegListener implements DmbListener {

    private static final String TAG = "DmbMpegListener";

    public static final int MESSAGE_START_PLAY_VIDEO = DmbPlayerConstant.MESSAGE_START_PLAY_VIDEO.getDmbConstantValue();

    /**
     * 自定义的视频播放回调
     */
    private final Handler handler;

    /**
     * 时间格式化
     */
    @SuppressLint("SimpleDateFormat")
    private final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");

    public DmbMpegListener(Handler handler) {
        this.handler = handler;
    }

    /**
     * 文件输出流
     */
    private FileOutputStream fileOutputStream;

    /**
     * 临时文件名
     */
    private String tmpFileName;

    {
        try {
            String dateFormat = simpleDateFormat.format(new Date());
            // 临时文件名为时间戳-tmp.dmb
            tmpFileName = Environment.getStorageDirectory().getPath() + "/" + dateFormat + "-tmp.dmb";
            // 构造一个可以追加写的文件输出流
            fileOutputStream = new FileOutputStream(tmpFileName, true);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onSuccess(String fileName, byte[] bytes, int length) {
        if (DataReadWriteUtil.getTemporaryMpegTsVideoFilename() == null
                || Objects.equals(DataReadWriteUtil.getTemporaryMpegTsVideoFilename(), "")) {
            // 如果临时文件名是空的,就设置一下临时文件
            DataReadWriteUtil.setTemporaryMpegTsVideoFilename(tmpFileName);
        }
        try {
            Log.i(TAG, "往文件中写入了一段TS流");
            fileOutputStream.write(bytes, 0, bytes.length);
        } catch (IOException e) {
            e.printStackTrace();
        }
        handler.sendEmptyMessage(MESSAGE_START_PLAY_VIDEO);
    }

    @Override
    public void onReceiveMessage(String msg) {

    }
}
