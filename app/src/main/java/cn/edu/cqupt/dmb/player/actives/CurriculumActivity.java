package cn.edu.cqupt.dmb.player.actives;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import cn.edu.cqupt.dmb.player.R;
import cn.edu.cqupt.dmb.player.common.DmbPlayerConstant;
import cn.edu.cqupt.dmb.player.decoder.FicDecoder;
import cn.edu.cqupt.dmb.player.decoder.TpegDecoder;
import cn.edu.cqupt.dmb.player.listener.DmbCurriculumListener;
import cn.edu.cqupt.dmb.player.utils.DataReadWriteUtil;
import cn.edu.cqupt.dmb.player.utils.UsbUtil;

/**
 * 这个是显示课表的 Activity
 */
public class CurriculumActivity extends Activity {

    public static final int MESSAGE_UPDATE_CURRICULUM = DmbPlayerConstant.MESSAGE_UPDATE_CURRICULUM.getDmbConstantValue();
    private static final String TAG = "CurriculumActivity";
    /**
     * 单例线程池,运行TPEG解码线程的
     */
    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    /**
     * 显示课表的组件
     */
    private ImageView imageView;

    private DmbCurriculumListener dmbCurriculumListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_curriculum);
        DataReadWriteUtil.inMainActivity = false;
        initView();
        // 开始解码 TPEG 生成 TPEG
        startDecodeCurriculum();
    }

    /**
     * 初始化 View
     */
    private void initView() {
        Log.i(TAG, "正在初始化课表显示组件");
        imageView = findViewById(R.id.curriculum_image_view);
        if (executorService.isShutdown()) {
            executorService = Executors.newSingleThreadExecutor();
        }
    }

    /**
     * 开始执行解码线程
     */
    private void startDecodeCurriculum() {
        // 重新设置一下MainActivity.id的 ID,方便 FicDecoder 解码
        MainActivity.id = DataReadWriteUtil.getActiveFrequencyModule().getDeviceID();
        // 先重置一下 Dangle
        UsbUtil.restDangle(FicDecoder.getInstance(MainActivity.id, true, null), DataReadWriteUtil.getActiveFrequencyModule());
        dmbCurriculumListener = new DmbCurriculumListener(new CurriculumHandler(Looper.getMainLooper()));
        // 构造TPEG解码器
        TpegDecoder tpegDecoder = new TpegDecoder(dmbCurriculumListener, this);
        executorService.submit(tpegDecoder);
    }

    @Override
    protected void onDestroy() {
        // 中断解码线程
        executorService.shutdown();
        // 销毁一下 dangle 的设置
        UsbUtil.dangleDestroy(this);
        super.onDestroy();
    }

    /**
     * 监听课表生成的 Handler<br/>
     * 关于为什么重载这个构造方法,详情参见<p>https://www.codeleading.com/article/66486105877/<p/>
     */
    @SuppressLint("HandlerLeak")
    private final class CurriculumHandler extends Handler {

        public CurriculumHandler(@NonNull Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            if (msg.what == MESSAGE_UPDATE_CURRICULUM) {
                byte[] fileBuffer = dmbCurriculumListener.getFileBuffer();
                Integer length = dmbCurriculumListener.getLength();
                Bitmap bitmap = BitmapFactory.decodeByteArray(fileBuffer, 0, length);
                if (bitmap != null) {
                    Log.i(TAG, "重新设置了一下课表");
                    imageView.setImageBitmap(bitmap);
                } else {
                    Log.e(TAG, "生成课表 Bitmap 错误!");
                }
            }
        }
    }
}
