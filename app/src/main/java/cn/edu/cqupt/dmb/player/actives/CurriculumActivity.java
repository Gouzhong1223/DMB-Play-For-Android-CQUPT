package cn.edu.cqupt.dmb.player.actives;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import cn.edu.cqupt.dmb.player.R;
import cn.edu.cqupt.dmb.player.common.FrequencyModule;
import cn.edu.cqupt.dmb.player.decoder.FicDecoder;
import cn.edu.cqupt.dmb.player.decoder.TpegDecoder;
import cn.edu.cqupt.dmb.player.listener.DmbCurriculumListener;
import cn.edu.cqupt.dmb.player.utils.DataReadWriteUtil;
import cn.edu.cqupt.dmb.player.utils.UsbUtil;

/**
 * 这个是显示课表的 Activity
 */
public class CurriculumActivity extends Activity {

    private static final String TAG = "CurriculumActivity";
    /**
     * 显示课表的组件
     */
    private ImageView imageView;
    /**
     * 解码课表 TPEG 的线程
     */
    private TpegDecoder tpegDecoder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_curriculum);
        initView();
        // 开始解码 TPEG 生成 TPEG
        startPlayCurriculum();
    }

    /**
     * 初始化 View
     */
    private void initView() {
        Log.i(TAG, "正在初始化课表显示组件");
        imageView = findViewById(R.id.curriculum_image_view);
    }

    /**
     * 开始执行解码线程
     */
    private void startPlayCurriculum() {
        // 重新设置一下MainActivity.id的 ID,方便 FicDecoder 解码
        MainActivity.id = DataReadWriteUtil.getActiveFrequencyModule().getDeviceID();
        // 先重置一下 Dangle
        UsbUtil.restDangle(FicDecoder.getInstance(MainActivity.id, true), DataReadWriteUtil.getActiveFrequencyModule());
        DmbCurriculumListener dmbCurriculumListener = new DmbCurriculumListener(imageView);
        tpegDecoder = new TpegDecoder(dmbCurriculumListener);
        tpegDecoder.start();
    }

    @Override
    protected void onDestroy() {
        // 中断解码线程
        tpegDecoder.interrupt();
        FrequencyModule defaultFrequencyModule = DataReadWriteUtil.getDefaultFrequencyModule(this);
        // 结束之后设置活跃场景为默认场景
        DataReadWriteUtil.setActiveFrequencyModule(defaultFrequencyModule);
        // 结束之后将 ID 设置成默认的场景 ID
        MainActivity.id = defaultFrequencyModule.getDeviceID();
        // 再重置一下 Dangle
        UsbUtil.restDangle(FicDecoder.getInstance(MainActivity.id, true), defaultFrequencyModule);
        super.onDestroy();
    }
}
