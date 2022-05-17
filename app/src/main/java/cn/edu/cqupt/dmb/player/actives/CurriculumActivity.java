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
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.room.Room;

import cn.edu.cqupt.dmb.player.R;
import cn.edu.cqupt.dmb.player.common.CustomSettingByKey;
import cn.edu.cqupt.dmb.player.common.DmbPlayerConstant;
import cn.edu.cqupt.dmb.player.db.database.CustomSettingDatabase;
import cn.edu.cqupt.dmb.player.db.mapper.CustomSettingMapper;
import cn.edu.cqupt.dmb.player.decoder.FicDecoder;
import cn.edu.cqupt.dmb.player.decoder.TpegDecoder;
import cn.edu.cqupt.dmb.player.domain.CustomSetting;
import cn.edu.cqupt.dmb.player.domain.SceneVO;
import cn.edu.cqupt.dmb.player.listener.DmbCurriculumListener;
import cn.edu.cqupt.dmb.player.processor.dmb.DataProcessingFactory;
import cn.edu.cqupt.dmb.player.processor.dmb.PseudoBitErrorRateProcessor;
import cn.edu.cqupt.dmb.player.utils.DataReadWriteUtil;
import cn.edu.cqupt.dmb.player.utils.UsbUtil;

/**
 * 这个是显示课表的 Activity
 *
 * @author qingsong
 */
public class CurriculumActivity extends Activity {

    public static final int MESSAGE_UPDATE_CURRICULUM = DmbPlayerConstant.MESSAGE_UPDATE_CURRICULUM.getDmbConstantValue();
    private static final String TAG = "CurriculumActivity";

    /**
     * 监听信号更新的 message 类型
     */
    private final int MESSAGE_UPDATE_SIGNAL = DmbPlayerConstant.MESSAGE_UPDATE_SIGNAL.getDmbConstantValue();

    /**
     * 显示课表的组件
     */
    private ImageView imageView;

    /**
     * 信号显示组件
     */
    private ImageView signalImageView;

    /**
     * 课表更新监听器
     */
    private DmbCurriculumListener dmbCurriculumListener;
    /**
     * 选中的使用场景配置
     */
    private SceneVO selectedSceneVO;

    /**
     * 操作自定义设置的 Mapper
     */
    private CustomSettingMapper customSettingMapper;

    /**
     * 是否显示信号的设置
     */
    private CustomSetting defaultSignalShowSetting;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 强制全屏,全的不能再全的那种了
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_curriculum);
        // 获取父传递过来的参数
        selectedSceneVO = (SceneVO) this.getIntent().getSerializableExtra(DetailsActivity.SCENE_VO);
        // 初始化数据库 Mapper
        initDataBase();
        // 加载自定义设置
        loadCustomSetting();
        // 初始化组件
        initView();
        // 开始接收 DMB 数据
        UsbUtil.startReceiveDmbData();
        // 开始解码 TPEG 生成 TPEG
        startDecodeCurriculum();
    }

    /**
     * 初始化数据库
     */
    private void initDataBase() {
        //new a database
        customSettingMapper = Room.databaseBuilder(this, CustomSettingDatabase.class, "custom_setting_database")
                .allowMainThreadQueries().build().getCustomSettingMapper();
    }

    /**
     * 加载自定义设置
     */
    private void loadCustomSetting() {
        // 查询信号显示设置
        defaultSignalShowSetting = customSettingMapper.selectCustomSettingByKey(CustomSettingByKey.OPEN_SIGNAL.getKey());
    }

    /**
     * 初始化 View
     */
    private void initView() {
        Log.i(TAG, "正在初始化课表显示组件");
        imageView = findViewById(R.id.curriculum_image_view);
        signalImageView = findViewById(R.id.curriculum_signal);
        if (defaultSignalShowSetting != null) {
            int showSignal = Math.toIntExact(defaultSignalShowSetting.getSettingValue());
            signalImageView.setVisibility(showSignal == 0 ? View.INVISIBLE : View.VISIBLE);
        }
    }

    /**
     * 开始执行解码线程
     */
    private void startDecodeCurriculum() {
        // 先重置一下 Dangle
        UsbUtil.restDangle(FicDecoder.getInstance(selectedSceneVO.getDeviceId(), true), selectedSceneVO);
        dmbCurriculumListener = new DmbCurriculumListener(new CurriculumHandler(Looper.getMainLooper()), selectedSceneVO);
        // 构造TPEG解码器
        TpegDecoder tpegDecoder = new TpegDecoder(dmbCurriculumListener, this);
        tpegDecoder.start();
    }

    @Override
    protected void onDestroy() {
        DataReadWriteUtil.inMainActivity = true;
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
            } else if (msg.what == MESSAGE_UPDATE_SIGNAL) {
                // 如果没有进行信号显示设置或者关闭信号显示就直接跳过广播处理
                if (defaultSignalShowSetting == null || defaultSignalShowSetting.getSettingValue() == 0L) {
                    return;
                }
                PseudoBitErrorRateProcessor pseudoBitErrorRateProcessor = (PseudoBitErrorRateProcessor) DataProcessingFactory.getDataProcessor(0x00);
                // 这里为什么能直接获取ber,因为是从静态工厂里面去出来的,静态工厂里面的都是单例创建的对象,在系统初始化的时候就已经load了,然后就是ber是一个volatile变量
                // 不懂volatile是什么的可以搜一下Java多线程中的工作内存和主内存的区别,看他们是如何消除内存屏障的
                int ber = pseudoBitErrorRateProcessor.getBer();
                Log.i(TAG, "ber = " + ber);
                if (ber > 200) {
                    signalImageView.setImageResource(R.drawable.singlemark1);
                } else if (ber > 100) {
                    signalImageView.setImageResource(R.drawable.singlemark2);
                } else if (ber > 50) {
                    signalImageView.setImageResource(R.drawable.singlemark3);
                } else if (ber > 10) {
                    signalImageView.setImageResource(R.drawable.singlemark4);
                } else if (ber >= 0) {
                    signalImageView.setImageResource(R.drawable.singlemark5);
                }
            }
        }
    }
}
