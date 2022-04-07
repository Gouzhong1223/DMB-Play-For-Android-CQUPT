package cn.edu.cqupt.dmb.player.actives;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import cn.edu.cqupt.dmb.player.R;
import cn.edu.cqupt.dmb.player.decoder.TpegDecoderImprovement;
import cn.edu.cqupt.dmb.player.listener.DmbCurriculumListener;

/**
 * 这个是显示课表的 Activity
 */
public class CurriculumActivity extends AppCompatActivity {

    private static final String TAG = "CurriculumActivity";
    private ImageView imageView;
    private TpegDecoderImprovement tpegDecoderImprovement;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_curriculum);
        initView();
        startPlayCurriculum();
    }

    private void initView() {
        Log.i(TAG, "正在初始化课表显示组件");
        imageView = findViewById(R.id.curriculum_image_view);
    }

    private void startPlayCurriculum() {
        DmbCurriculumListener dmbCurriculumListener = new DmbCurriculumListener(imageView);
        tpegDecoderImprovement = new TpegDecoderImprovement(dmbCurriculumListener);
        tpegDecoderImprovement.start();
    }

    @Override
    protected void onDestroy() {
        tpegDecoderImprovement.interrupt();
        super.onDestroy();
    }
}
