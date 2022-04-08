package cn.edu.cqupt.dmb.player.actives;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import cn.edu.cqupt.dmb.player.R;
import cn.edu.cqupt.dmb.player.decoder.TpegDecoderImprovement;
import cn.edu.cqupt.dmb.player.domain.Dangle;
import cn.edu.cqupt.dmb.player.listener.DmbCurriculumListener;
import cn.edu.cqupt.dmb.player.utils.DataReadWriteUtil;
import cn.edu.cqupt.dmb.player.utils.UsbUtil;

/**
 * 这个是显示课表的 Activity
 */
public class CurriculumActivity extends AppCompatActivity {

    private static final String TAG = "CurriculumActivity";
    private ImageView imageView;
    private TpegDecoderImprovement tpegDecoderImprovement;
    private Dangle dangle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_curriculum);
        initView();
        startPlayCurriculum();
        dangle = UsbUtil.getDangle();
    }

    private void initView() {
        Log.i(TAG, "正在初始化课表显示组件");
        imageView = findViewById(R.id.curriculum_image_view);
    }

    private void startPlayCurriculum() {
        // 在进入课表播放界面之后,先清除一下 dangle 的设置
        dangle.clearRegister();
        // 然后把频点设置为活跃模块的频点
        dangle.setFrequency(DataReadWriteUtil.getActiveFrequencyModule().getFrequency());
        DmbCurriculumListener dmbCurriculumListener = new DmbCurriculumListener(imageView);
        tpegDecoderImprovement = new TpegDecoderImprovement(dmbCurriculumListener);
        tpegDecoderImprovement.start();
    }

    @Override
    protected void onDestroy() {
        tpegDecoderImprovement.interrupt();
        // 结束之后设置活跃场景为默认场景
        DataReadWriteUtil.setActiveFrequencyModule(DataReadWriteUtil.getDefaultFrequencyModule(this));
        super.onDestroy();
    }
}
