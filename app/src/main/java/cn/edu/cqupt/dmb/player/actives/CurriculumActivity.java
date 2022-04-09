package cn.edu.cqupt.dmb.player.actives;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import cn.edu.cqupt.dmb.player.R;
import cn.edu.cqupt.dmb.player.decoder.TpegDecoder;
import cn.edu.cqupt.dmb.player.domain.Dangle;
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
    /**
     * Dangle 实例
     */
    private Dangle dangle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_curriculum);
        initView();
        // 获取 dangle 实例
        dangle = UsbUtil.getDangle();
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
        // 在进入课表播放界面之后,先清除一下 dangle 的设置
        dangle.clearRegister();
        // 然后把频点设置为活跃模块的频点
        dangle.setFrequency(DataReadWriteUtil.getActiveFrequencyModule().getFrequency());
        // 重新设置一下MainActivity.id的 ID,方便 FicDecoder 解码
        MainActivity.id = DataReadWriteUtil.getActiveFrequencyModule().getDeviceID();
        DmbCurriculumListener dmbCurriculumListener = new DmbCurriculumListener(imageView);
        tpegDecoder = new TpegDecoder(dmbCurriculumListener);
        tpegDecoder.start();
    }

    @Override
    protected void onDestroy() {
        // 中断解码线程
        tpegDecoder.interrupt();
        // 结束之后设置活跃场景为默认场景
        DataReadWriteUtil.setActiveFrequencyModule(DataReadWriteUtil.getDefaultFrequencyModule(this));
        // 结束之后将 ID 设置成默认的场景 ID
        MainActivity.id = DataReadWriteUtil.getDefaultFrequencyModule(this).getDeviceID();
        super.onDestroy();
    }
}
